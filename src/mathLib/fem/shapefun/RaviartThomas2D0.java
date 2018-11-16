package mathLib.fem.shapefun;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import mathLib.fem.core.CoordinateTransform;
import mathLib.fem.core.Edge;
import mathLib.fem.core.EdgeLocal;
import mathLib.fem.core.Element;
import mathLib.fem.util.FutureyeException;
import mathLib.fem.util.container.ObjList;
import mathLib.func.symbolic.MultiVarFunc;
import mathLib.func.symbolic.Variable;
import mathLib.func.symbolic.VecMathFuncBase;
import mathLib.func.symbolic.basic.FXY;
import mathLib.func.symbolic.basic.SpaceVectorFunction;
import mathLib.func.symbolic.intf.MathFunc;
import mathLib.func.symbolic.intf.ScalarShapeFunction;
import mathLib.func.symbolic.intf.VecMathFunc;
import mathLib.func.symbolic.intf.VectorShapeFunction;
import mathLib.matrix.algebra.SpaceVector;
import mathLib.matrix.algebra.intf.Vector;

/**
 * Raviart-Thomas 2D0 triangle element
 * 
 * for j=1,2,3
 * \psi_{E_{j}} = \sigma_{j} \frac{|E_{j}|}{2|T|} (x-P_{j})
 *
 * where
 *   x = (x1,x2) \in T
 *   E_{j} is the edge of a triangle T opposite to its vertex P_{j}
 *   sigma_{j} = \nu_{j} \dot \nu_{E_{j}}
 *   \nu_{j} the unit normal of T along E_{j} 
 *   \nu_{E_{j}} the unit normal vector of E_{j} chosen with a global fixed orientation
 * 
 * Property:
 *   \psi_{E_{j}} \dot \nu_{j} = 1, along E_{j}
 *                             = 0, along other edge
 * Ref.
 * C. Bahriawati, Three matlab implementations of the lowest-order Raviart-Thomas 
 * mfem with a posteriori error control, Computational Methods in Applied Mathematics, 
 * Vol.5(2005), No.4, pp.333-361.

 * e.g.
 * 
 *    P3
 *    | \ 
 *  E2|  \ E1
 *    |   \
 *    |    \
 *   P1-----P2 
 *       E3
 *       
 *
 */
public class RaviartThomas2D0 extends VecMathFuncBase implements VectorShapeFunction {
	int funIndex = 0;
	
	private VecMathFunc funCompose = null;
	private VecMathFunc funOuter = null;
	private ObjList<String> innerVarNames = null;

	public RaviartThomas2D0(int funID) {
		funIndex = funID - 1;
		varNames.add("x");
		varNames.add("y");
		innerVarNames = new ObjList<String>("x","y");
	}
	
	
	@Override
	public void assignElement(final Element e) {
		//Space Vector Function
		MathFunc fx = new FXY("fx", varNames,1,0,0);
		MathFunc fy = new FXY("fy", varNames,0,1,0);
		SpaceVectorFunction svf = new SpaceVectorFunction(fx,fy);
	
		EdgeLocal lEdge = e.edges().at(funIndex+1);
		Edge gEdge = lEdge.getGlobalEdge();
		double sigma = lEdge.getNormVector().dot(gEdge.getNormVector());
		//|T| = area
		double area = e.getElementArea();
		//|E_{j}| = edgeLength
		double edgeLength = gEdge.getEdgeLength();
		double coef = sigma * edgeLength / (2.0 * area);
		
		int[] nodeIndex = {3, 1, 2};
		SpaceVector v = new SpaceVector(e.nodes.at(nodeIndex[funIndex]).coords());
		this.funOuter = svf.S(v).scale(coef);
		
		//�?�?�函数
		Map<String, MathFunc> fInners = new HashMap<String, MathFunc>();
		List<String> varNamesInner = new LinkedList<String>();
		varNamesInner.add("r");
		varNamesInner.add("s");

		//x = x(r,s,t)
		//y = y(r,s,t)
		//	where t = 1 - r - s
		for(final String varName : varNames) {
			fInners.put(varName, new MultiVarFunc(varName, varNamesInner) {
				
				protected CoordinateTransform trans = new CoordinateTransform(2);
				
				public MathFunc diff(String varName) {
					return null;
				}
//				@Override
//				public double apply(Variable v) {
//					//根�?��?�?�的varName给出�?�?�的表达�?
//					List<MathFunc> transFun = trans.getTransformFunction(
//							trans.getTransformLinear2DShapeFunction(e));
//					if(varName.equals("x")) {//x = x(r,s,t)
//						return transFun.get(0).apply(v);
//					} else if(varName.equals("y")) {//y = y(r,s,t)
//						return transFun.get(1).apply(v);
//					} else {
//						Exception e = new FutureyeException("Error!");
//						e.printStackTrace();
//					}
//					return 0.0;
//				}
				
				@Override
				public double apply(double... args) {
					//根�?��?�?�的varName给出�?�?�的表达�?
					List<MathFunc> transFun = trans.getTransformFunction(
							trans.getTransformLinear2DShapeFunction(e));
					if(varName.equals("x")) {//x = x(r,s,t)
						return transFun.get(0).apply(args);
					} else if(varName.equals("y")) {//y = y(r,s,t)
						return transFun.get(1).apply(args);
					} else {
						Exception e = new FutureyeException("Error!");
						e.printStackTrace();
					}
					return 0.0;
				}
			});
		}
		
		//使用�?�?�函数构造形函数
		//funOuter.setVarNames(varNames); //!!!
		funCompose = funOuter.compose(fInners);
	}

	@Override
	public ScalarShapeFunction restrictTo(int funIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MathFunc dot(VecMathFunc b) {
		return this.funCompose.dot(b);
	}

	@Override
	public MathFunc get(int index) {
		return this.funCompose.get(index);
	}
	

	@Override
	public void set(int index, MathFunc value) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public int getDim() {
		return this.funCompose.getDim();
	}

	@Override
	public Vector value(Variable v) {
		return this.funCompose.value(v);
	}

	@Override
	public ObjList<String> innerVarNames() {
		return innerVarNames;
	}
}
