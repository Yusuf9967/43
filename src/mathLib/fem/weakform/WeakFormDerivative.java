package mathLib.fem.weakform;


import mathLib.fem.core.DOF;
import mathLib.fem.core.Element;
import mathLib.fem.core.Node;
import mathLib.fem.util.container.DOFList;
import mathLib.func.symbolic.Variable;
import mathLib.func.symbolic.basic.FC;
import mathLib.func.symbolic.basic.Vector2MathFunc;
import mathLib.func.symbolic.intf.MathFunc;
import mathLib.func.symbolic.intf.ScalarShapeFunction;

/**
 * 
 * Solve: (w, v) = (U_x, v)
 * 
 * 
 * Test for:
 *   \eps*(\nabla{w},\nabla{v}) + (w,v) = (U_x, v)
 * where
 *   \eps -> 0
 * 
 * where w is unknown
 *   U_x is the piecewise derivative on the mesh
 *   w is an approximation of U_x
 *   
 */
public class WeakFormDerivative extends AbstractScalarWeakForm {
	protected Vector2MathFunc g_U = null;
	protected String varName; // "x" or "y"
	protected double eps = -1.0;

	public WeakFormDerivative(String varName) {
		this.varName = varName;
	}
	
	@Override
	public MathFunc leftHandSide(Element e, ItemType itemType) {
		if(itemType==ItemType.Domain)  {
			//Integrand part of Weak Form on element e
			MathFunc integrand = null;
			if(eps > 0.0) {
				integrand = u.diff("x").M(v.diff("x")).A(
						    u.diff("y").M(v.diff("y"))).M(eps).A(
						    u.M(v));
			} else {
				integrand = u.M(v);
			}
			return integrand;
		}
		return null;
	}

	@Override
	public MathFunc rightHandSide(Element e, ItemType itemType) {
		if(itemType==ItemType.Domain)  {
			MathFunc rlt = new FC(0.0);
			int nNode = e.nodes.size();
			for(int i=1;i<=nNode;i++) {
				DOFList dofListI = e.getNodeDOFList(i);
				for(int k=1;k<=dofListI.size();k++) {
					DOF dofI = dofListI.at(k);
					Variable var = Variable.createFrom(g_U, (Node)dofI.getOwner(), dofI.getGlobalIndex());
					MathFunc PValue = new FC(g_U.apply(var));
					ScalarShapeFunction shape = dofI.getSSF();
					
					rlt = rlt.A(PValue.M(shape.diff(varName)));
				}
			}
			
			MathFunc integrand = rlt.M(v);
			return integrand;
		}
		return null;
	}

	public void setParam(Vector2MathFunc U) {
		this.g_U = U;
	}
	
	public void setParam(Vector2MathFunc U, double eps) {
		this.g_U = U;
		this.eps = eps;
	}
}
