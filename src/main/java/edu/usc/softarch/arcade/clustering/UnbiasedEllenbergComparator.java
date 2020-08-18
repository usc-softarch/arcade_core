package edu.usc.softarch.arcade.clustering;

import java.io.Serializable;
import java.util.Comparator;

import edu.usc.softarch.arcade.util.ExtractionContext;

/**
 * @author joshua
 */
class UnbiasedEllenbergComparator extends SimMeasureComparator implements
		Comparator<Cluster>, Serializable {

	private static final long serialVersionUID = -5406183535441641044L;
	
	public int compare(Cluster c1, Cluster c2) {
		System.out.println("\tIn "
				+ ExtractionContext.getCurrentClassAndMethodName());
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
		if (getRefCluster() == null)
			return 0;

		Double unbiasedEllenbergC2 = Double.valueOf(SimCalcUtil.getUnbiasedEllenbergMeasure(c2,
			getRefCluster()));

		int returnValue = unbiasedEllenbergC2.compareTo(SimCalcUtil.getUnbiasedEllenbergMeasure(c1,
			getRefCluster()));
		System.out.println("\tUnbiasedEllenbergComparator's return value: " + returnValue);

		return returnValue;
	}
}
