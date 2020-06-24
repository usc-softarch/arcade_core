package edu.usc.softarch.arcade.clustering;

import java.io.Serializable;
import java.util.Comparator;

import edu.usc.softarch.arcade.util.ExtractionContext;


/**
 * @author joshua
 *
 */
class UnbiasedEllenbergComparator extends SimMeasureComparator implements
		Comparator<Cluster>, Serializable {

	boolean DEBUG = false;

	/**
* 
*/
	private static final long serialVersionUID = -5406183535441641044L;
	
	public int compare(Cluster c1, Cluster c2) {
		if (DEBUG) {
			System.out.println("\tIn "
					+ ExtractionContext.getCurrentClassAndMethodName());
			System.out.println("\tc1: " + c1);
			System.out.println("\tc2: " + c2);
			System.out.println("\trefCluster: " + refCluster);

			System.out.println("\tc1 in binary form: " + c1.toBinaryForm());
			System.out.println("\trefCluster in binary form: "
					+ refCluster.toBinaryForm());
			System.out.println();
			System.out.println("\tc2 in binary form: " + c2.toBinaryForm());
			System.out.println("\trefCluster in binary form: "
					+ refCluster.toBinaryForm());
		}
		if (refCluster == null) {
			return 0;
		}

		Double unbiasedEllenbergC2 = new Double(SimCalcUtil.getUnbiasedEllenbergMeasure(c2,
				refCluster));

		int returnValue = unbiasedEllenbergC2.compareTo(SimCalcUtil.getUnbiasedEllenbergMeasure(c1,
				refCluster));
		if (DEBUG)
			System.out.println("\tUnbiasedEllenbergComparator's return value: " + returnValue);

		return returnValue;
	}
}
