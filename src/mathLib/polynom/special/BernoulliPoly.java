package mathLib.polynom.special;

import static mathLib.polynom.ComplexPolynomial.Xc;
import static mathLib.polynom.Polynomial.X;
import static mathLib.polynom.Polynomial.ZERO;
import static mathLib.util.MathUtils.combination;
import static mathLib.util.MathUtils.deltaKronecker;

import mathLib.polynom.ComplexPolynomial;
import mathLib.polynom.Polynomial;
import mathLib.sequence.Sequence;
import mathLib.sequence.Series;

public class BernoulliPoly {

	public static Polynomial bernoulli(int degree) {
		if (degree == 0)
			return 0 * X + 1;
		if (degree == 1)
			return X - 0.5;
		if (degree == 2)
			return X * X - X + 1.0 / 6.0;
		Polynomial result = ZERO;
		for (int k = 0; k <= degree; k++) {
			result = result + combination(degree, k) * bernoulliNumber(degree - k) * X.pow(k);
		}
		return result;
	}

	public static double bernoulliNumber(int m) {
		if (m == 0)
			return 1.0;
		if (m == 1)
			return -0.5;
		if (m == 2)
			return 1.0 / 6.0;
		if (m % 2 != 0 && m > 2)
			return 0.0;

		Sequence func = k -> combination(m, k) * bernoulliNumber((int) k) / (m - k + 1) ;
		Series series = new Series(func) ;
		return deltaKronecker((int) m, 0) - series.sum(0, m - 1);
	}

	/**
	 * Complex version of the bernoulli polynomial
	 * @param degree
	 * @return
	 */

	public static ComplexPolynomial bernoulliC(int degree) {
		return bernoulli(degree)+0*Xc ;
	}


	// for test
	public static void main(String[] args) {
		System.out.println(bernoulli(0));
		System.out.println(bernoulli(1));
		System.out.println(bernoulli(2));
		System.out.println(bernoulli(3));
		System.out.println(bernoulli(4));
		System.out.println(bernoulli(5));
		System.out.println(bernoulli(6));
		System.out.println(bernoulli(7));
		System.out.println(bernoulli(8));
		System.out.println(bernoulli(9));
		System.out.println(bernoulli(10));
		System.out.println(bernoulli(11));
		System.out.println(bernoulli(12));
		System.out.println(bernoulli(13));
	}

}
