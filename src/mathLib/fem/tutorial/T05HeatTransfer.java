package mathLib.fem.tutorial;

import java.io.File;
import java.util.HashMap;

import mathLib.fem.assembler.AssemblerScalar;
import mathLib.fem.core.Mesh;
import mathLib.fem.core.NodeType;
import mathLib.fem.element.FELinearTriangleOld;
import mathLib.fem.util.container.ElementList;
import mathLib.fem.weakform.WeakFormLaplace2D;
import mathLib.func.symbolic.basic.FC;
import mathLib.func.symbolic.basic.FX;
import mathLib.func.symbolic.basic.Vector2MathFunc;
import mathLib.func.symbolic.intf.MathFunc;
import mathLib.matrix.algebra.intf.Matrix;
import mathLib.matrix.algebra.intf.Vector;
import mathLib.matrix.algebra.solver.external.SolverJBLAS;
import mathLib.util.io.MeshReader;
import mathLib.util.io.MeshWriter;

/**
 * <blockquote><pre>
 * Heat transfer problem:
 * d(u)/dt - Laplace(u) = f
 * =>
 *  (u_{n+1}-u_{n})/Dt  - Laplace(u) = f
 * =>
 *  -Dt*Laplace(u) + u_{n+1} = Dt*f + u_{n}
 *
 *  u(t=0)=0;
 *  u(x,t)=0, x on border of \Omega
 * <blockquote><pre>
 *
 * @author liuyueming
 */
public class T05HeatTransfer {
	String outputFolder = "HeatTranfer";
	Mesh mesh = null;

	//Laplace2D weak form
	WeakFormLaplace2D weakForm = new WeakFormLaplace2D();

	//Source term
	MathFunc f = null;

	//Time step size
	double Dt;

	public void readMesh() {
		//Read a triangle mesh from an input file
		MeshReader reader = new MeshReader("triangle.grd");
		mesh = reader.read2DMesh();
		//Geometry relationship
		mesh.computeNodeBelongsToElements();
	}

	public void initParam() {
		//Right hand side(RHS): f = -2*(x^2+y^2)+36
		f = FC.c(-2.0)
			.M(
				FX.x.M(FX.x).A(FX.y.M(FX.y)) )
			.A(
				FC.c(36.0)
			);

		//Mark border type
		HashMap<NodeType, MathFunc> mapNTF = new HashMap<NodeType, MathFunc>();
		mapNTF.put(NodeType.Dirichlet, null);
		mesh.markBorderNode(mapNTF);

		//Use element library to assign degree of freedom (DOF) to element
		ElementList eList = mesh.getElementList();
		FELinearTriangleOld linearTriangle = new FELinearTriangleOld();
		for(int i=1;i<=eList.size();i++)
			linearTriangle.assignTo(eList.at(i));

	    File file = new File(outputFolder);
		if(!file.exists()) {
			file.mkdirs();
		}
	}

	public Vector solverOneStep(int step, MathFunc u_n) {
		FC FDt = new FC(Dt);

		weakForm.setF(FDt.M(f).A(u_n));
		weakForm.setParam(FDt, new FC(1.0), null, null);

		//Assemble
		AssemblerScalar assembler = new AssemblerScalar(mesh, weakForm);
		assembler.assemble();
		Matrix stiff = assembler.getStiffnessMatrix();
		Vector load = assembler.getLoadVector();
		//Boundary condition
		assembler.imposeDirichletCondition(new FC(0.0));

		SolverJBLAS solver = new SolverJBLAS();
		Vector u = solver.solveDGESV(stiff, load);
		System.out.println("u=");
		for(int i=1;i<=u.getDim();i++)
			System.out.println(String.format("%.3f", u.get(i)));

	    MeshWriter writer = new MeshWriter(mesh);
	    writer.writeTechplot(String.format("%s/u_t%02d.dat",outputFolder, step), u);
	    return u;

	}

	public void run() {
		readMesh();
		initParam();

		//Time step size
		Dt = 0.2;

		MathFunc u_n = new FC(0.0);
		for(int i=1;i<=25;i++) {
			Vector rlt = solverOneStep(i, u_n);
			u_n = new Vector2MathFunc(rlt);
		}
	}

	public static void main(String[] args) {
		T05HeatTransfer htf = new T05HeatTransfer();
		htf.run();
	}
}
