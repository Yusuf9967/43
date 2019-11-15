package tests;

import mathLib.util.StringUtils;
import static mathLib.numbers.Complex.*;

import mathLib.sfg.SFG;

public class TestSFG2 {
	public static void main(String[] args) {


		String[] nodes = {"I", "A", "B", "C", "D", "O"} ;
		SFG sfg = new SFG(StringUtils.toArrayList(nodes)) ;

		sfg.addArrow("I", "A", 1);
		sfg.addArrow("A", "B", j);
		sfg.addArrow("B", "C", 2+j);
		sfg.addArrow("C", "A", -1);
		sfg.addArrow("C", "I", -1);
		sfg.addArrow("B", "A", -1);
		sfg.addArrow("I", "I", -1);
		sfg.addArrow("A", "D", 2);
		sfg.addArrow("D", "C", 1);
		sfg.addArrow("C", "C", 1);
		sfg.addArrow("C", "O", 1);
		sfg.addArrow("C", "C", 1);
		sfg.addArrow("O", "C", 1);
		sfg.buildForwardPaths("I", "O");

		System.out.print(sfg.printAllLoops_compactForm());
		System.out.println(sfg.printDelta_compactForm());
		System.out.println(sfg.printForwardPaths_noGains());
		System.out.println(sfg.printCofactors());
	}

}
