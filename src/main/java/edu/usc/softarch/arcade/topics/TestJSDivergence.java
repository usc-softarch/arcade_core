package edu.usc.softarch.arcade.topics;

import cc.mallet.util.Maths;

public class TestJSDivergence {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		//double[] p = {0.5,0.2,0.3};
		//double[] q = {0.1,0.0,0.8};
		double[] p = {0.8,0.2,0.0,0.0};
		double[] q = {0.5,0.5,0.0,0.0};
		
		double[] p2 = {0.65,0.35,0.0,0.0};
		double[] q2 = {0.0,0.0,0.5,0.4};
		
		/*
		double[] p = {0.5,0.5,0.0};
		double[] q = {0.0,0.5,0.5};
		double[] p = {0.5,0.2,0.3};
		double[] q = {0.1,0.0,0.8};
		*/
		
		double divergence = Maths.jensenShannonDivergence(p, q);
		double divergence2 = Maths.jensenShannonDivergence(p2, q2);
		
		System.out.println(divergence);
		System.out.println(divergence2);

	}

}
