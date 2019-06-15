package mathLib.fem.tutorial;

import static mathLib.func.symbolic.FMath.C0;
import static mathLib.func.symbolic.FMath.grad;
import static mathLib.func.symbolic.FMath.x;
import static mathLib.func.symbolic.FMath.y;

import java.util.HashMap;

import mathLib.fem.assembler.Assembler;
import mathLib.fem.core.Element;
import mathLib.fem.core.Mesh;
import mathLib.fem.core.NodeType;
import mathLib.fem.element.FELinearTriangle;
import mathLib.fem.util.Utils;
import mathLib.fem.weakform.WeakForm;
import mathLib.func.symbolic.intf.MathFunc;
import mathLib.matrix.algebra.SparseMatrixRowMajor;
import mathLib.matrix.algebra.SparseVectorHashMap;
import mathLib.matrix.algebra.intf.SparseMatrix;
import mathLib.matrix.algebra.intf.SparseVector;
import mathLib.matrix.algebra.intf.Vector;
import mathLib.matrix.algebra.solver.external.SolverJBLAS;
import mathLib.util.io.MeshReader;
import mathLib.util.io.MeshWriter;

/**
 * Use assembleLocal() to get local stiff matrix and local load vector in an
 * element This gives user the ability to assemble their own global stiff matrix
 * and global load vector <blockquote>
 *
 * <pre>
 * Problem:
 *   -\Delta{u} = f
 *   u(x,y)=0, (x,y) \in \partial{\Omega}
 * where
 *   \Omega = [-3,3]*[-3,3]
 *   f = -2*(x^2+y^2)+36
 * Solution:
 *   u = (x^2-9)*(y^2-9)
 * </blockquote>
 * </pre>
 *
 */

public class LaplaceLocalAssemble {
	public void run() {
		// 1. Read in mesh
		MeshReader reader = new MeshReader("grids/triangle.grd");
		Mesh mesh = reader.read2DMesh();
		// Compute geometry relationship between nodes and elements
		mesh.computeNodeBelongsToElements();

		// 2. Mark boundary types
		HashMap<NodeType, MathFunc> mapNTF = new HashMap<NodeType, MathFunc>();
		mapNTF.put(NodeType.Dirichlet, null); //null => mark all boundary nodes
		mesh.markBorderNode(mapNTF);

		// 3. Weak form definition
		FELinearTriangle fe = new FELinearTriangle(); //Linear triangular finite element
		final MathFunc f = -2 * (x * x + y * y) + 36; //Right hand side (RHS)
		WeakForm wf = new WeakForm(fe,
				(u,v) -> grad(u, "x", "y").dot(grad(v, "x", "y")),
				v -> f * v
			);
		wf.compile();

		// 5. Assembly and boundary condition(s)
		Assembler assembler = new Assembler(mesh, wf);
		int dim = mesh.getNodeList().size();
		SparseMatrix stiff = new SparseMatrixRowMajor(dim, dim);
		SparseVector load = new SparseVectorHashMap(dim);
		int nDOFs = fe.getNumberOfDOFs();
		for (Element e : mesh.getElementList()) {
			assembler.assembleLocal(e);
			double[][] A = assembler.getLocalStiffMatrix();
			double[] b = assembler.getLocalLoadVector();
			for (int j = 0; j < nDOFs; j++) {
				int nGlobalRow = fe.getGlobalIndex(mesh, e, j+1);
				for (int i = 0; i < nDOFs; i++) {
					int nGlobalCol = fe.getGlobalIndex(mesh, e, i+1);
					stiff.add(nGlobalRow, nGlobalCol, A[j][i]);
				}
				// Local load vector
				load.add(nGlobalRow, b[j]);
			}
		}
		// Apply zero Dirichlet boundary condition
		Utils.imposeDirichletCondition(stiff, load, fe, mesh, C0);

		// 6. Solve linear system
		SolverJBLAS solver = new SolverJBLAS();
		Vector u = solver.solveDGESV(stiff, load);
		System.out.println("u=");
		for (int i = 1; i <= u.getDim(); i++)
			System.out.println(String.format("%.3f ", u.get(i)));

		// 7. Output the result to a file with Techplot format
		MeshWriter writer = new MeshWriter(mesh);
		writer.writeTechplot("Laplace2D.dat", u);
	}

	public static void main(String[] args) {
		LaplaceLocalAssemble ex1 = new LaplaceLocalAssemble();
		ex1.run();
	}
}
