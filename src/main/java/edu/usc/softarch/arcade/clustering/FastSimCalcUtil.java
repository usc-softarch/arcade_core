package edu.usc.softarch.arcade.clustering;

import java.util.BitSet;
import java.util.Set;

import cc.mallet.util.Maths;
import edu.usc.softarch.arcade.topics.DistributionSizeMismatchException;

public class FastSimCalcUtil {
	public static double getUnbiasedEllenbergMeasure(Cluster currCluster,
			Cluster otherCluster) {
		// Gets rid of rounding error arbitrariness
		double sumOfFeaturesInBothEntities1 = getSumOfFeaturesInBothEntities(currCluster, otherCluster);
		double sumOfFeaturesInBothEntities2 = getSumOfFeaturesInBothEntities(otherCluster, currCluster);
		double sumOfFeaturesInBothEntities = (sumOfFeaturesInBothEntities1 + sumOfFeaturesInBothEntities2) / 2;
		int numberOf10Features = getNumOf10Features(currCluster, otherCluster);
		int numberOf01Features = getNumOf01Features(currCluster, otherCluster);

		return 1 - (0.5 * sumOfFeaturesInBothEntities /
			(0.5 * sumOfFeaturesInBothEntities + (double) numberOf10Features
				+ (double) numberOf01Features));
	}
	
	public static double getUnbiasedEllenbergMeasureNM(Cluster currCluster,
			Cluster otherCluster, int numFeatures) {
		// Gets rid of rounding error arbitrariness
		double sumOfFeaturesInBothEntities1 = getSumOfFeaturesInBothEntities(currCluster, otherCluster);
		double sumOfFeaturesInBothEntities2 = getSumOfFeaturesInBothEntities(otherCluster, currCluster);
		double sumOfFeaturesInBothEntities = (sumOfFeaturesInBothEntities1 + sumOfFeaturesInBothEntities2) / 2;
		int num10Features = getNumOf10Features(currCluster, otherCluster);
		int num01Features = getNumOf01Features(currCluster, otherCluster);
		int num00Features = getNumOf00Features(currCluster, otherCluster, numFeatures);
		int numSharedFeatures = getNumOfFeaturesInBothEntities(currCluster, otherCluster);
		
		return 1 - (0.5 * sumOfFeaturesInBothEntities /
			(0.5 * sumOfFeaturesInBothEntities + 2
				* ((double) num10Features + (double) num01Features)
				+ num00Features + numSharedFeatures));
	}

	private static int getNumOf01Features(Cluster currCluster,
			Cluster otherCluster) {
		Set<Integer> otherIndices = otherCluster.getFeatureMap().keySet();
		
		int num01Features = 0;
		for (Integer otherIndex : otherIndices)
			if (currCluster.getFeatureMap().get(otherIndex) == null)
				num01Features++;

		return num01Features;
	}
	
	private static int getNumOf00Features(Cluster currCluster,
			Cluster otherCluster, int numFeatures) {
		BitSet currIndices = new BitSet(numFeatures);
		for (Integer integer : currCluster.getFeatureMap().keySet())
			currIndices.set(integer);

		BitSet otherIndices = new BitSet(numFeatures);
		for (Integer integer : otherCluster.getFeatureMap().keySet())
			otherIndices.set(integer);

		currIndices.or(otherIndices);
		currIndices.flip(0, numFeatures);
		
		return currIndices.cardinality();
	}

	private static int getNumOf10Features(Cluster currCluster,
			Cluster otherCluster) {
		return getNumOf01Features(otherCluster, currCluster);
	}
	
	private static int getNumOfFeaturesInBothEntities(Cluster currCluster,
			Cluster otherCluster) {
		Set<Integer> currIndices = currCluster.getFeatureMap().keySet();
		
		int numSharedFeatures = 0;
		for (Integer currIndex : currIndices)
			if (currCluster.getFeatureMap().get(currIndex) != null
					&& otherCluster.getFeatureMap().get(currIndex) !=null)
				numSharedFeatures++;
		
		return numSharedFeatures;
	}

	private static double getSumOfFeaturesInBothEntities(Cluster currCluster,
			Cluster otherCluster) {
		Set<Integer> currIndices = currCluster.getFeatureMap().keySet();
		
		double sumSharedFeatures = 0;
		for (Integer currIndex : currIndices) {
			if (currCluster.getFeatureMap().get(currIndex) != null && otherCluster.getFeatureMap().get(currIndex) !=null) {
				Double currFeatureValue = currCluster.getFeatureMap().get(currIndex);
				Double otherFeatureValue = otherCluster.getFeatureMap().get(currIndex);
				sumSharedFeatures += currFeatureValue + otherFeatureValue;
			}
		}
		
		return sumSharedFeatures;
	}

	public static double getStructAndConcernMeasure(Cluster cluster,
			Cluster otherCluster, int numFeatures) {
		double[] firstDist = new double[numFeatures];
		double[] secondDist = new double[numFeatures];
		
		normalizeFeatureVectorOfCluster(cluster, numFeatures, firstDist);
		normalizeFeatureVectorOfCluster(otherCluster, numFeatures, secondDist);
		
		double jsDivergenceStruct = Maths.jensenShannonDivergence(firstDist, secondDist);
		if (Double.isInfinite(jsDivergenceStruct))
			jsDivergenceStruct = Double.MAX_VALUE;

		
		double jsDivergenceConcern = 0;
		try {
			jsDivergenceConcern =
				cluster.docTopicItem.getJsDivergence(otherCluster.docTopicItem);
		} catch (DistributionSizeMismatchException e) {
			e.printStackTrace(); //TODO handle it
		}
		
		if (Double.isInfinite(jsDivergenceConcern))
			jsDivergenceConcern = Double.MIN_VALUE;

		double structAndConcernMeasure =
			0.5 * jsDivergenceStruct + 0.5 * jsDivergenceConcern;
		
		if (Double.isNaN(structAndConcernMeasure))
			throw new RuntimeException("infoLossMeasure is NaN");
		
		return structAndConcernMeasure;
	}
	
	public static double getInfoLossMeasure(int numberOfEntitiesToBeClustered,
			Cluster cluster, Cluster otherCluster, int numFeatures) {
		double[] firstDist = new double[numFeatures];
		double[] secondDist = new double[numFeatures];
		
		normalizeFeatureVectorOfCluster(cluster, numFeatures, firstDist);
		normalizeFeatureVectorOfCluster(otherCluster, numFeatures, secondDist);
		
		double jsDivergence = Maths.jensenShannonDivergence(firstDist, secondDist);
		if (Double.isInfinite(jsDivergence))
			jsDivergence = Double.MAX_VALUE;

		double infoLossMeasure = ((double) cluster.getNumEntities() /
			numberOfEntitiesToBeClustered	+ (double) otherCluster.getNumEntities() /
			numberOfEntitiesToBeClustered) * jsDivergence;
		
		if (Double.isNaN(infoLossMeasure))
			throw new RuntimeException("infoLossMeasure is NaN");

		return infoLossMeasure;
	}

	private static void normalizeFeatureVectorOfCluster(Cluster cluster,
			int featuresLength, double[] firstDist) {
		for (int i = 0; i < featuresLength; i++) {
			if (cluster.getFeatureMap().get(i) != null) {
				double featureValue = cluster.getFeatureMap().get(i);
				firstDist[i] = featureValue/cluster.getFeatureMap().size();
			}
			else { // this feature is zero
				firstDist[i] = 0;
			}
		}
	}
}
