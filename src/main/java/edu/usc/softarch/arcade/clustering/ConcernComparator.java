package edu.usc.softarch.arcade.clustering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import edu.usc.softarch.arcade.topics.DistributionSizeMismatchException;

/**
 * @author joshua
 */
public class ConcernComparator
		extends SimMeasureComparator {
	private static Map<List<FeatureVector>, Double> divergenceMap =
		new HashMap<>();

	public int compare(Cluster c1, Cluster c2) {
		// If the refCluster wasn't set, return MAX_VALUE
		//TODO Should probably throw an exception
		if (getRefCluster() == null) {
			System.out.println("In ConcernComparator, refCluster is null");
			return Integer.MAX_VALUE;
		}
		
		// If any of the clusters' DocTopicItem wasn't set, return MAX_VALUE
		//TODO Should probably throw an exception
		if (c2.getDocTopicItem() == null 
				|| getRefCluster().getDocTopicItem() == null
				|| c1.getDocTopicItem() == null)
			return Integer.MAX_VALUE;

		// Calculate the comparison parameter
		Double concernMeasureC2 =
			Double.valueOf(getJSDivergence(c1, getRefCluster()));

		return concernMeasureC2.compareTo(
			getJSDivergence(c2, getRefCluster()));
	}

	private double getJSDivergence(FeatureVector fv1, FeatureVector fv2) {
		String strippedLeafSplitClusterName1 = fv1.getInnermostPackageName();
		String strippedLeafSplitClusterName2 = fv2.getInnermostPackageName();
		
		if (Pattern.matches("^.*\\$\\d+$", strippedLeafSplitClusterName1)
				|| Pattern.matches("^.*\\$.*\\$.*$", strippedLeafSplitClusterName1)
				|| Pattern.matches("^.*\\$\\d+$", strippedLeafSplitClusterName2)
				|| Pattern.matches("^.*\\$.*\\$.*$", strippedLeafSplitClusterName2))
			return Integer.MAX_VALUE;
		
		List<FeatureVector> featureVecPair =
			new ArrayList<>(Arrays.asList(fv1, fv2));
		
		double divergence = 0; 
		
		if (divergenceMap.containsKey(featureVecPair))
			divergence = divergenceMap.get(featureVecPair);
		else {
			try {
				divergence = fv1.getDocTopicItem().getJsDivergence(fv2.getDocTopicItem());
			} catch (DistributionSizeMismatchException e) {
				e.printStackTrace(); //TODO handle it
			}
			divergenceMap.put(new ArrayList<>(featureVecPair), divergence);
		}
		
		return divergence;
	}
}