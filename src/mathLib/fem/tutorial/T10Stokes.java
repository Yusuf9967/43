package mathLib.fem.tutorial;

import static mathLib.func.symbolic.FMath.C0;
import static mathLib.func.symbolic.FMath.C1;

import java.util.HashMap;

import mathLib.fem.assembler.AssemblerVector;
import mathLib.fem.core.EdgeLocal;
import mathLib.fem.core.Element;
import mathLib.fem.core.Mesh;
import mathLib.fem.core.Node;
import mathLib.fem.core.NodeLocal;
import mathLib.fem.core.NodeType;
import mathLib.fem.core.Vertex;
import mathLib.fem.element.FEQuadraticV_LinearPOld;
import mathLib.fem.util.Constant;
import mathLib.fem.util.container.ElementList;
import mathLib.fem.util.container.ObjIndex;
import mathLib.fem.util.container.ObjList;
import mathLib.fem.weakform.WeakFormStokes;
import mathLib.func.symbolic.MultiVarFunc;
import mathLib.func.symbolic.Variable;
import mathLib.func.symbolic.basic.SpaceVectorFunction;
import mathLib.func.symbolic.intf.MathFunc;
import mathLib.func.symbolic.intf.VecMathFunc;
import mathLib.matrix.algebra.SparseBlockMatrix;
import mathLib.matrix.algebra.SparseBlockVector;
import mathLib.matrix.algebra.solver.SchurComplementStokesSolver;
import mathLib.util.io.MeshReader;

/**
 * Problem:
 *   -\Nabla{k*\Nabla{\vec{u}} + \Nabla{p} = \vec{f}
 *   div{\vec{u}} = 0
 *
 * Each dim:
 *   -k*(u1_xx+u1_yy) + p_x = f1
 *   -k*(u2_xx+u2_yy) + p_y = f2
 *   u1_x+u2_y              = 0

 * Weak form:
 *   find \vec{u} \in H_0^1(div;\Omega), p \in L_2(\Omega)
 *   such that, for all \vec{v} \in H_0^1(div;\Omega), q \in L_2(\Omega)
 *
 *   (\Nabla{\vec{v}},k*\Nabla{\vec{u}}) - (div{\vec{v}},p)
 *                   + (q,div{\vec{u}}) = (\vec{v},\vec{f})
 *
 *   (v1_x,k*u1_x) + (v1_y,k*u1_y) + (v2_x,k*u2_x) + (v2_y,k*u2_y)
 *                   - (v1_x+v2_y,p) + (q,u1_x+u2_y) = (v1*f1+v2*f2)
 *
 * where
 *   \vec{u}=(u1,u2): velocity vector field
 *   \vec{f}=(f1,f2): body force
 *
 * @author liuyueming
 *
 */
public class T10Stokes {
	public static String outputFolder = "tutorial\\Stokes";

	public static void cylinder() {
		//Read a triangle mesh from an input file
//		String file = "cylinder1";
//		String file = "cylinder2";
//		String file = "cylinder2_test";
//		String file = "cylinder4";
//		String file = "cylinder_patch";
		String file = "u_shape";

		MeshReader reader = new MeshReader(file+".grd");
		MeshReader reader2 = new MeshReader(file+".grd");
		Mesh mesh = reader.read2DMesh();
		Mesh meshOld = reader2.read2DMesh();
		mesh.nVertex = mesh.getNodeList().size();

		//Add nodes for quadratic element
		for(int i=1;i<=mesh.getElementList().size();i++) {
			Element e = mesh.getElementList().at(i);
			e.adjustVerticeToCounterClockwise();
			ObjList<EdgeLocal> edges = e.edges();
			int nNode = e.nodes.size();
			for(int j=1;j<=edges.size();j++) {
				EdgeLocal edge = edges.at(j);
				Vertex l = edge.beginVertex();
				Vertex r = edge.endVertex();
				double cx = (l.coord(1)+r.coord(1))/2.0;
				double cy = (l.coord(2)+r.coord(2))/2.0;
				Node node = new Node(mesh.getNodeList().size()+1, cx,cy);
				Node findNode = mesh.findNode(node);
				if(findNode == null) {
					edge.addEdgeNode(new NodeLocal(++nNode,node));
					mesh.addNode(node);
				} else {
					edge.addEdgeNode(new NodeLocal(++nNode,findNode));
				}
			}
			e.applyChange();
		}

		//Geometry relationship
		mesh.computeNodeBelongsToElements();

		ElementList eList = mesh.getElementList();
		//NodeList nodes = mesh.getNodeList();

		for(int i=1;i<=eList.size();i++) {
			System.out.println(i+"  " + eList.at(i));
		}

		//Mark border type
		HashMap<NodeType, MathFunc> mapNTF_uv = new HashMap<NodeType, MathFunc>();

		//mapNTF.put(NodeType.Dirichlet,null);
		mapNTF_uv.put(NodeType.Dirichlet, new MultiVarFunc("x","y") {
			@Override
			public double apply(Variable v) {
				double x = v.get("x");
				double y = v.get("y");
				//all
				if(Math.abs(y-2.5)<Constant.meshEps|| Math.abs(y+2.5)<Constant.meshEps)
					return 1;

				//cylinder1 and cylinder2 and cylinder4
//				else if(Math.abs(x+5.0)<0.01)
//						return 1;

				//u_shape
				else if(Math.abs(x+5.0)<Constant.meshEps && y>0)
					return 1;
				else if(Math.abs(x-5.0)<Constant.meshEps)
					return 1;
				else if(Math.abs(x-4.0)<Constant.meshEps)
					return 1;
				else if(Math.abs(y-0.5)<Constant.meshEps|| Math.abs(y+0.5)<Constant.meshEps)
					return 1;

				//cylinder1
				else if(Math.sqrt(x*x+y*y)<(0.5+0.1))
					return 1;

				//cylinder2 and cylinder4
//				else if(Math.sqrt(x*x+(y-1)*(y-1))<(0.5+0.15) ||
//						Math.sqrt(x*x+(y+1)*(y+1))<(0.5+0.15) )
//					return 1;

				//cylinder4
//				else if(Math.sqrt((x-2.5)*(x-2.5)+(y-0.3)*(y-0.3))<(0.5+0.15) ||
//						Math.sqrt((x+2.5)*(x+2.5)+(y+0.3)*(y+0.3))<(0.5+0.15) )
//					return 1;

				//all
				else
					return 0;
			}

			@Override
			public double apply(double... args) {
				// TODO Auto-generated method stub
				return 0;
			}
		});
		//uv????????????
		mapNTF_uv.put(NodeType.Neumann, null);


		HashMap<NodeType, MathFunc> mapNTF_p = new HashMap<NodeType, MathFunc>();
		mapNTF_p.put(NodeType.Dirichlet, new MultiVarFunc("x","y") {
			@Override
			public double apply(Variable v) {
				double x = v.get("x");
				double y = v.get("y");
				//cylinder1 cylinder2 cylinder4
//				if(Math.abs(x-5.0)<0.01)
//					return 1;
//				else
//					return 0;

				//u_shape
				if(Math.abs(x+5.0)<0.01 && y<0)
					return 1;
				else
					return 0;
			}

			@Override
			public double apply(double... args) {
				// TODO Auto-generated method stub
				return 0;
			}
		});

		mapNTF_p.put(NodeType.Neumann, null);

		mesh.markBorderNode(new ObjIndex(1,2),mapNTF_uv);
		mesh.markBorderNode(3,mapNTF_p);

		//Use element library to assign degree of freedom (DOF) to element
		for(int i=1;i<=eList.size();i++) {
			System.out.println(i+"  " + eList.at(i));
		}

		FEQuadraticV_LinearPOld fe = new FEQuadraticV_LinearPOld();
		fe.initDOFIndexGenerator(mesh);
		for(int i=1;i<=eList.size();i++) {
			fe.assignTo(eList.at(i));
			//eList.at(i).printDOFInfo();
		}

		//Stokes weak form
		WeakFormStokes weakForm = new WeakFormStokes();

		//Right hand side(RHS): f = (0,0)'
		weakForm.setF(new SpaceVectorFunction(C0,C0));
		weakForm.setParam(C1);
		//Robin:  k*u_n + d*u - p\vec{n} = 0
		VecMathFunc d = new SpaceVectorFunction(2);
		d.set(1, C0);
		d.set(2, C0);
		weakForm.setRobin(d);

		//Assemble
		AssemblerVector assembler = new AssemblerVector(mesh, weakForm,fe);
		assembler.assemble();
		SparseBlockMatrix stiff = assembler.getStiffnessMatrix();
		SparseBlockVector load = assembler.getLoadVector();
		//stiff.print();
		//load.print();
		//System.out.println(load.norm2());
		//Boundary condition
		VecMathFunc diri = new SpaceVectorFunction(3);
		diri.set(1, new MultiVarFunc("x","y") {
					@Override
					public double apply(Variable v) {
						double x = v.get("x");
						double y = v.get("y");

						//cylinder1 cylinder2 cylinder4
//						if(Math.abs(x+5.0)<0.01)
//							return Math.cos(Math.PI/2*(y/2.5));

						//u_shape
						if(Math.abs(x+5.0)<0.01 && y>0)
							return Math.cos(Math.PI/2*(y-1.5));

//						//all
						else
							return 0.0;
					}

					@Override
					public double apply(double... args) {
						// TODO Auto-generated method stub
						return 0;
					}
				});
		diri.set(2, C0);
		diri.set(3, C0);
		assembler.imposeDirichletCondition(diri);
		load.getBlock(1).print();
		load.getBlock(2).print();
		load.getBlock(3).print();//cylinder1 : 0.026739   0.000000   0.000000   0.029822 ...


		SchurComplementStokesSolver solver =
			new SchurComplementStokesSolver(stiff,load);

		SparseBlockVector u = solver.solve2D();

		System.out.println("u=");
		for(int i=1;i<=u.getDim();i++)
			System.out.println(String.format("%.3f", u.get(i)));

		Tools.plotVector(mesh, outputFolder, String.format("%s_uv.dat",file),
				u.getBlock(1), u.getBlock(2));
		Tools.plotVector(meshOld, outputFolder, String.format("%s_p.dat",file),
				u.getBlock(3));

	}

	public static void main(String[] args) {
		cylinder();
	}
}
