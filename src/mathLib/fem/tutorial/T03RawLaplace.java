package mathLib.fem.tutorial;

import static mathLib.func.symbolic.FMath.*;

import java.util.HashMap;

import mathLib.fem.assembler.AssemblerScalar;
import mathLib.fem.core.DOF;
import mathLib.fem.core.Element;
import mathLib.fem.core.Mesh;
import mathLib.fem.core.NodeType;
import mathLib.fem.shapefun.SFLinearLocal2D;
import mathLib.fem.weakform.WeakFormLaplace2D;
import mathLib.func.symbolic.intf.MathFunc;
import mathLib.matrix.algebra.intf.Matrix;
import mathLib.matrix.algebra.intf.Vector;
import mathLib.matrix.algebra.solver.external.SolverJBLAS;
import mathLib.util.io.MeshReader;
import mathLib.util.io.MeshWriter;

/**
 * Assign degree of freedom(DOF) to element by hand instead of using element library
 * <p>
 * <blockquote><pre>
 * Problem:
 *   -\Delta{u} = f
 *   u(x,y)=0, (x,y) \in \partial{\Omega}
 * where
 *   \Omega = [-3,3]*[-3,3]
 *   f = -2*(x^2+y^2)+36
 * Solution:
 *   u = (x^2-9)*(y^2-9)
 * </blockquote></pre>
 * 
 * @author liuyueming
 */
public class T03RawLaplace {
	public static void triangle() {
		//Read a triangle mesh from an input file
		MeshReader reader = new MeshReader("grids/triangle.grd");
		Mesh mesh = reader.read2DMesh();
		
		//Geometry relationship
		mesh.computeNodeBelongsToElements();
		
		//Mark border type
		HashMap<NodeType, MathFunc> mapNTF = new HashMap<NodeType, MathFunc>();
		mapNTF.put(NodeType.Dirichlet, null);		
		mesh.markBorderNode(mapNTF);

		//Create 2D linear triangle shape function
		SFLinearLocal2D[] shapeFun = new SFLinearLocal2D[3];
		for(int i=0;i<3;i++)
			shapeFun[i] = new SFLinearLocal2D(i+1);
		
		//Assign degree of freedom(DOF) to element
		for(int i=1;i<=mesh.getElementList().size();i++) {
			Element e = mesh.getElementList().at(i);
			for(int j=1;j<=e.nodes.size();j++) {
				//Create degree of freedom(DOF) object
				DOF dof = new DOF(j,e.nodes.at(j).globalIndex,shapeFun[j-1]);
				e.addNodeDOF(j, dof);
			}
		}
		
		//Laplace2D weak form
		WeakFormLaplace2D weakForm = new WeakFormLaplace2D();
		
		//Right hand side(RHS): f = -2*(x^2+y^2)+36
        weakForm.setF(x.M(x).A(y.M(y)).M(-2.0).A(36.0));
		
		//Assemble
		AssemblerScalar assembler = new AssemblerScalar(mesh, weakForm);
		assembler.assemble();
		Matrix stiff = assembler.getStiffnessMatrix();
		Vector load = assembler.getLoadVector();
		//Boundary condition
		assembler.imposeDirichletCondition(C0);
		
		SolverJBLAS solver = new SolverJBLAS();
		Vector u = solver.solveDGESV(stiff, load);
		System.out.println("u=");
		for(int i=1;i<=u.getDim();i++)
			System.out.println(String.format("%.3f", u.get(i)));	
	    
	    MeshWriter writer = new MeshWriter(mesh);
	    writer.writeTechplot("RawLaplace.dat", u);
		
	}
	
	public static void main(String[] args) {
		triangle();
	}	
}
