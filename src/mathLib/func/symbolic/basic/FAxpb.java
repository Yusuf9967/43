package mathLib.func.symbolic.basic;

import java.util.Map;

import mathLib.fem.util.Constant;
import mathLib.func.symbolic.FMath;
import mathLib.func.symbolic.SingleVarFunc;
import mathLib.func.symbolic.Variable;
import mathLib.func.symbolic.VariableArray;
import mathLib.func.symbolic.intf.MathFunc;

/**
 * f(x) = a*x + b
 */
public class FAxpb extends SingleVarFunc {
	protected double a;
	protected double b;

	public FAxpb(double a, double b) {
		super("", Constant.x);
		this.a = a;
		this.b = b;
	}
	
	public FAxpb(String varName, double a, double b) {
		super("", varName);
		this.a = a;
		this.b = b;
	}
	
	@Override
	public MathFunc diff(String varName) {
		if(this.varName.equals(varName))
			return new FC(a);
		else
			return FMath.C0;
	}

	@Override
	public double apply(Variable v) {
		return a*v.get(varName)+b;
	}

	@Override
	public double apply(double... args) {
		return a*args[argIdx]+b;
	}
	
	@Override
	public double apply(Variable v, Map<Object,Object> cache) {
		return a*v.get(varName)+b;
	}
	
	@Override
	public double[] applyAll(VariableArray v, Map<Object,Object> cache) {
		int len = v.length();
		double[] rlt = new double[len];
		double[] vs = v.get(varName);
		for(int i=0;i<len;i++)
			rlt[i] = a*vs[i]+b;
		return rlt;
	}
	
	@Override
	public int getOpOrder() {
		if(Double.compare(a, 0.0) == 0)
			return OP_ORDER0;
		if(Double.compare(b, 0.0) == 0)
			return OP_ORDER2;
		else
			return OP_ORDER3;
	}
	
	public String getExpr() {
		if(Double.compare(a, 1.0) == 0) {
			if(Double.compare(b, 0.0) == 0)
				return varName;
			else
				return varName+"+"+b;
		} else if(Double.compare(a, 0.0) == 0) {
				return b+"";
		} else if(Double.compare(b, 0.0) == 0) {
			return a+"*"+varName;
		}
		return a+"*"+varName+"+"+b;
	}

//TODO	
//	/**
//	 * varNames在由�?�个自�?��?表达�?�?算组�?�而�?的多自�?��?函数情况下计算导数�?��?�能被修改，
//	 * 这�?修改是�?许的，例如：(x+1)*(y+1)关于x求导数�?�，原�?�(y+1)的varNames由[y]�?�为[x,y]
//	 * 
//	 */
//	@Override
//	public MathFunc setVarNames(List<String> varNames) {
//		this.varNames = varNames;
//		return this;
//	}
}
