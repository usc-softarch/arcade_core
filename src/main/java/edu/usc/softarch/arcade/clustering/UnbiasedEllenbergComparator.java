package edu.usc.softarch.arcade.clustering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author joshua
 */
public class UnbiasedEllenbergComparator extends SimMeasureComparator {
	private static Map<List<FeatureVector>, Integer> oneZeroFeaturesMap = new HashMap<>();
	private static Map<List<FeatureVector>, Integer> zeroOneFeaturesMap = new HashMap<>();
	private static Map<Set<FeatureVector>, Double> sumSharedFeaturesMap = new HashMap<>();

	public int compare(Cluster c1, Cluster c2) {
		// If the refCluster wasn't set, return 0
		//TODO Should probably throw an exception
		if (getRefCluster() == null)
			return 0;

		// Calculate the comparison parameter
		Double unbiasedEllenbergC2 = Double.valueOf(
			getUnbiasedEllenbergMeasure(c2,	getRefCluster()));

		return unbiasedEllenbergC2.compareTo(
			getUnbiasedEllenbergMeasure(c1,	getRefCluster()));
	}

	private double getUnbiasedEllenbergMeasure(FeatureVector fv1, FeatureVector fv2) {
		List<FeatureVector> featureVecPair = new ArrayList<>(2);
		featureVecPair.add(fv1);
		featureVecPair.add(fv2);
		double sumSharedFeatures = 0;
		int num10Features = 0;
		int num01Features = 0;

		if (sumSharedFeaturesMap.containsKey(new HashSet<>(featureVecPair)))
			sumSharedFeatures = sumSharedFeaturesMap.get(new HashSet<>(featureVecPair));
		else {
			sumSharedFeatures = fv1.getSumSharedFeatures(fv2);
			sumSharedFeaturesMap.put(new HashSet<>(featureVecPair), sumSharedFeatures);
		}
		
		if (oneZeroFeaturesMap.containsKey(featureVecPair))
			num10Features = oneZeroFeaturesMap.get(featureVecPair);
		else {
			num10Features = fv1.getNum10Features(fv2);
			oneZeroFeaturesMap.put(featureVecPair, num10Features);
		}
		
		if (zeroOneFeaturesMap.containsKey(featureVecPair))
			num01Features = zeroOneFeaturesMap.get(featureVecPair);
		else {
			num01Features = fv1.getNum01Features(fv2);
			zeroOneFeaturesMap.put(featureVecPair, num01Features);
		}
		
		double denom = 0.5 * sumSharedFeatures + num10Features + num01Features;
		if (denom == 0)	return denom;
		return 0.5 * sumSharedFeatures / (denom);
	}
}