package edu.usc.softarch.arcade.clustering;

import java.io.Serializable;
import java.util.Comparator;

/**
 * @author joshua
 */
public class ConcernComparator extends SimMeasureComparator implements
Comparator<Cluster>, Serializable {
	private static final long serialVersionUID = -5406183535441641044L;
	
	public int compare(Cluster c1, Cluster c2) {
		if (getRefCluster() == null) {
			System.out.println("In ConcernComparator, refCluster is null");
			return Integer.MAX_VALUE;
		}
		
		System.out.println("\tIn "
				+ Thread.currentThread().getStackTrace()[2].getClassName() + "."
				+ Thread.currentThread().getStackTrace()[2].getMethodName());
		System.out.println("\tc1: " + c1);
		System.out.println("\tc2: " + c2);
		System.out.println("\trefCluster: " + getRefCluster());

		System.out.println("\tc1 in binary form: " + c1.toBinaryForm());
		System.out.println("\trefCluster in binary form: "
				+ getRefCluster().toBinaryForm());
		System.out.println();
		System.out.println("\tc2 in binary form: " + c2.toBinaryForm());
		System.out.println("\trefCluster in binary form: "
				+ getRefCluster().toBinaryForm());
		
		if (c2.getDocTopicItem() == null || getRefCluster().getDocTopicItem() == null || c1.getDocTopicItem() == null)
			return Integer.MAX_VALUE;

		Double concernMeasureC2 = Double.valueOf(SimCalcUtil.getJSDivergence(c1,
			getRefCluster()));

		int returnValue = concernMeasureC2.compareTo(SimCalcUtil.getJSDivergence(c2,
			getRefCluster()));
		System.out.println("\tConcernComparator's return value: " + returnValue);

		return returnValue;
	}
}