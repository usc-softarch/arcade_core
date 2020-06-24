package edu.usc.softarch.arcade.clustering;

import java.io.Serializable;
import java.util.Comparator;

import edu.usc.softarch.arcade.topics.DocTopics;
import edu.usc.softarch.arcade.topics.TopicUtil;
import edu.usc.softarch.arcade.util.ExtractionContext;


/**
 * @author joshua
 *
 */
public class ConcernComparator extends SimMeasureComparator implements
Comparator<Cluster>, Serializable {
	boolean DEBUG = false;

	/**
* 
*/
	private static final long serialVersionUID = -5406183535441641044L;
	
	public int compare(Cluster c1, Cluster c2) {
		if (refCluster == null) {
			if (DEBUG)
					System.out.println("In ConcernComparator, refCluster is null");
			return Integer.MAX_VALUE;
		}
		
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
		
		
		if (c2.docTopicItem == null || refCluster.docTopicItem == null || c1.docTopicItem == null) {
			return Integer.MAX_VALUE;
		}

		Double concernMeasureC2 = new Double(SimCalcUtil.getJSDivergence(c1,
				refCluster));

		int returnValue = concernMeasureC2.compareTo(SimCalcUtil.getJSDivergence(c2,
				refCluster));
		if (DEBUG)
			System.out.println("\tConcernComparator's return value: " + returnValue);

		return returnValue;
	}
}
