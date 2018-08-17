package mathLib.fitting.lmse;

public interface LeastSquareFunction{
    /**
     * Returns the functions evaluated at the specific parameter set
     * @return needs to evaluate the function
     * */
    public double evaluate(double[] values, double[] parameters);
    public int getNParameters();
    public int getNInputs();
    
}

