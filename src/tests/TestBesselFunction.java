package tests;

import mathLib.func.Bessel;
import mathLib.util.MathUtils;
import plotter.chart.MatlabChart;

public class TestBesselFunction {
	public static void main(String[] args) {
		// calculating bessel m = -1/2
		double[] x = MathUtils.linspace(0.1, 100.0, 100) ;
		double[] y = new double[x.length] ;
		for(int i=0; i<x.length; i++)
			y[i] = Bessel.jn(-0.5, x[i]) ;

		MatlabChart fig = new MatlabChart() ;
		fig.plot(x, y);
		fig.RenderPlot();
		fig.run(true);

		// explicit equation
		double[] yExplicit = new double[x.length] ;
		for(int i=0; i<x.length; i++)
			yExplicit[i] = Math.sqrt(2.0/(Math.PI*x[i]))*Math.cos(x[i]) ;

		fig.plot(x, yExplicit, "r");
		fig.RenderPlot();

	}
}