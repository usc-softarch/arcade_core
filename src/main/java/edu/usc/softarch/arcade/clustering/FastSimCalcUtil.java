package edu.usc.softarch.arcade.clustering;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import cc.mallet.util.Maths;
import edu.usc.softarch.arcade.topics.DistributionSizeMismatchException;

public class FastSimCalcUtil {
	 
	public static double getUnbiasedEllenbergMeasure(Cluster currCluster,
																									 Cluster otherCluster) {
		// Gets rid of rounding error arbitrariness
		double sumOfFeaturesInBothEntities1 = getSumOfFeaturesInBothEntities(currCluster,otherCluster);
		double sumOfFeaturesInBothEntities2 = getSumOfFeaturesInBothEntities(otherCluster,currCluster);
		double sumOfFeaturesInBothEntities = (sumOfFeaturesInBothEntities1 + sumOfFeaturesInBothEntities2) / 2;
		int numberOf10Features = getNumOf10Features(currCluster,otherCluster);
		int numberOf01Features = getNumOf01Features(currCluster,otherCluster);

		if (sumOfFeaturesInBothEntities + (double) numberOf10Features
				+ (double) numberOf01Features == 0)
			throw new IllegalArgumentException("Attempted to cluster featureless entities");

		return 1 - (0.5 * sumOfFeaturesInBothEntities /
			(0.5 * sumOfFeaturesInBothEntities + (double) numberOf10Features
				+ (double) numberOf01Features));
	}
	
	public static double getUnbiasedEllenbergMeasureNM(Cluster currCluster,
																										 Cluster otherCluster) {
		// Gets rid of rounding error arbitrariness
		double sumOfFeaturesInBothEntities1 = getSumOfFeaturesInBothEntities(currCluster,otherCluster);
		double sumOfFeaturesInBothEntities2 = getSumOfFeaturesInBothEntities(otherCluster,currCluster);
		double sumOfFeaturesInBothEntities = (sumOfFeaturesInBothEntities1 + sumOfFeaturesInBothEntities2) / 2;
		int num10Features = getNumOf10Features(currCluster,otherCluster);
		int num01Features = getNumOf01Features(currCluster,otherCluster);
		int num00Features = getNumOf00Features(currCluster,otherCluster);
		int numSharedFeatures = getNumOfFeaturesInBothEntities(currCluster,otherCluster);

		if (sumOfFeaturesInBothEntities + (double) num10Features
				+ (double) num01Features == 0)
			throw new IllegalArgumentException("Attempted to cluster featureless entities");
		
		return 1 - (0.5 * sumOfFeaturesInBothEntities /
			(0.5 * sumOfFeaturesInBothEntities + 2
				* ((double) num10Features + (double) num01Features)
				+ (double) num00Features + (double) numSharedFeatures));
	}

	private static int getNumOf01Features(Cluster currCluster,
																				Cluster otherCluster) {
	
		Set<Integer> otherIndices = otherCluster.getFeatureMap().keySet();
		
		int num01Features = 0;
		for (Integer otherIndex : otherIndices) {
			if (currCluster.getFeatureMap().get(otherIndex) == null) {
				num01Features++;
			}
		}
				
		
		return num01Features;
	}
	
	private static int getNumOf00Features(Cluster currCluster,
			Cluster otherCluster) {
		int numFeatures = currCluster.getNumFeatures();
		Set<Integer> ooIndices =
			IntStream.rangeClosed(0, numFeatures - 1) // Creates the range of numbers
			.boxed().collect(Collectors.toSet());     // Puts them in a list

		Set<Integer> currIndices = currCluster.getFeatureMap().keySet();
		ooIndices.removeAll(currIndices);
		Set<Integer> otherIndices = otherCluster.getFeatureMap().keySet();
		ooIndices.removeAll(otherIndices);
		
		return ooIndices.size();
	}

	private static int getNumOf10Features(Cluster currCluster,
																				Cluster otherCluster) {
		
		Set<Integer> currIndices = currCluster.getFeatureMap().keySet();
		
		int num10Features = 0;
		for (Integer currIndex : currIndices) {
			if (otherCluster.getFeatureMap().get(currIndex) == null) {
				num10Features++;
			}
		}
				
		
		return num10Features;
	}
	
	private static int getNumOfFeaturesInBothEntities(
					Cluster currCluster, Cluster otherCluster) {
		
		Set<Integer> currIndices = currCluster.getFeatureMap().keySet();
		
		int numSharedFeatures = 0;
		for (Integer currIndex : currIndices) {
			if (currCluster.getFeatureMap().get(currIndex) != null && otherCluster.getFeatureMap().get(currIndex) !=null) {
				numSharedFeatures++;
			}
		}
		
		return numSharedFeatures;
	}

	private static double getSumOfFeaturesInBothEntities(
					Cluster currCluster, Cluster otherCluster) {
		
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
																									Cluster otherCluster) {
		if (cluster.getNumFeatures()!=otherCluster.getNumFeatures()) {
			throw new IllegalArgumentException("cluster.getFeaturesLength()!=otherCluster.getFeaturesLength()()");
		}
		int featuresLength = cluster.getNumFeatures();
		double[] firstDist = new double[featuresLength];
		double[] secondDist = new double[featuresLength];
		
		normalizeFeatureVectorOfCluster(cluster, featuresLength, firstDist);
		normalizeFeatureVectorOfCluster(otherCluster, featuresLength, secondDist);
		
		double jsDivergenceStruct = Maths.jensenShannonDivergence(firstDist, secondDist);
		if (Double.isInfinite(jsDivergenceStruct)) {
			jsDivergenceStruct = Double.MAX_VALUE;
		}
		
		double jsDivergenceConcern = 0;
		try {
			jsDivergenceConcern =
				cluster.docTopicItem.getJsDivergence(otherCluster.docTopicItem);
		} catch (DistributionSizeMismatchException e) {
			e.printStackTrace(); //TODO handle it
		}
		
		if (Double.isInfinite(jsDivergenceConcern)) {
			jsDivergenceConcern = Double.MIN_VALUE;
		}
		double structAndConcernMeasure =  0.5*jsDivergenceStruct+0.5*jsDivergenceConcern;
		
		if (Double.isNaN(structAndConcernMeasure)) {
			throw new RuntimeException("infoLossMeasure is NaN");
		}
		
		return structAndConcernMeasure;
	}
	
	public static double getInfoLossMeasure(int numberOfEntitiesToBeClustered,
			Cluster cluster, Cluster otherCluster) {
		if (cluster.getNumFeatures()!=otherCluster.getNumFeatures()) {
			throw new IllegalArgumentException("cluster.getFeaturesLength()!=otherCluster.getFeaturesLength()()");
		}
		int featuresLength = cluster.getNumFeatures();
		double[] firstDist = new double[featuresLength];
		double[] secondDist = new double[featuresLength];
		
		normalizeFeatureVectorOfCluster(cluster, featuresLength, firstDist);
		normalizeFeatureVectorOfCluster(otherCluster, featuresLength, secondDist);
		
		double jsDivergence = Maths.jensenShannonDivergence(firstDist, secondDist);
		if (Double.isInfinite(jsDivergence)) {
			jsDivergence = Double.MAX_VALUE;
		}
		double infoLossMeasure = (cluster.getNumEntities()/numberOfEntitiesToBeClustered
			+ otherCluster.getNumEntities()/numberOfEntitiesToBeClustered) * jsDivergence;
		
		if (Double.isNaN(infoLossMeasure)) {
			throw new RuntimeException("infoLossMeasure is NaN");
		}
		
		return infoLossMeasure;
	}

	private static void normalizeFeatureVectorOfCluster(Cluster cluster,
																											int featuresLength, double[] firstDist) {
		for (int i=0;i<featuresLength;i++) {
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
