package edu.usc.softarch.arcade.clustering;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import cc.mallet.util.Maths;

import edu.usc.softarch.arcade.topics.TopicUtil;

public class FastSimCalcUtil {
	 
	public static double getUnbiasedEllenbergMeasure(FastCluster currCluster,
			FastCluster otherCluster) {
		
		double sumOfFeaturesInBothEntities = getSumOfFeaturesInBothEntities(currCluster,otherCluster);
		int numberOf10Features = getNumOf10Features(currCluster,otherCluster);
		int numberOf01Features = getNumOf01Features(currCluster,otherCluster);
		
		return 0.5*sumOfFeaturesInBothEntities/(0.5*sumOfFeaturesInBothEntities+(double)numberOf10Features+(double)numberOf01Features);
	}
	
	public static double getUnbiasedEllenbergMeasureNM(FastCluster currCluster,
			FastCluster otherCluster) {
		
		double sumOfFeaturesInBothEntities = getSumOfFeaturesInBothEntities(currCluster,otherCluster);
		int num10Features = getNumOf10Features(currCluster,otherCluster);
		int num01Features = getNumOf01Features(currCluster,otherCluster);
		int num00Features = getNumOf00Features(currCluster,otherCluster);
		int numSharedFeatures = getNumOfFeaturesInBothEntities(currCluster,otherCluster);
		
		return 0.5*sumOfFeaturesInBothEntities/(0.5*sumOfFeaturesInBothEntities+2*((double)num10Features+(double)num01Features) + (double)num00Features + (double)numSharedFeatures);
	}

	private int getNumSharedFeatures(FastCluster currCluster,
			FastCluster otherCluster) {
		// TODO Auto-generated method stub
		return 0;
	}

	private static int getNumOf01Features(FastCluster currCluster,
			FastCluster otherCluster) {
		/*double[] currFeatures = currCluster.getFeatures();
		double[] otherFeatures = otherCluster.getFeatures();
		
		int num01Features = 0;
		for (int i=0;i<currCluster.getFeatures().length;i++) {
			if (currFeatures[i] == 0) {
				if (otherFeatures[i] > 0) {
					num01Features++;
				}
			}
		}*/
		
		Set<Integer> otherIndices = otherCluster.getNonZeroFeatureMap().keySet();
		
		int num01Features = 0;
		for (Integer otherIndex : otherIndices) {
			if (currCluster.getNonZeroFeatureMap().get(otherIndex) == null) {
				num01Features++;
			}
		}
				
		
		return num01Features;
	}
	
	private static int getNumOf00Features(FastCluster currCluster,
			FastCluster otherCluster) {
		/*double[] currFeatures = currCluster.getFeatures();
		double[] otherFeatures = otherCluster.getFeatures();
		
		int num10Features = 0;
		for (int i=0;i<currCluster.getFeatures().length;i++) {
			if (otherFeatures[i] == 0) {
				if (currFeatures[i] > 0) {
					num10Features++;
				}
			}
		}*/
		
		Set<Integer> currIndices = currCluster.getNonZeroFeatureMap().keySet();
		
		int num00Features = 0;
		for (Integer currIndex : currIndices) {
			if (otherCluster.getNonZeroFeatureMap().get(currIndex) == null && currCluster.getNonZeroFeatureMap().get(currIndex) == null) {
				num00Features++;
			}
		}
				
		
		return num00Features;
	}

	private static int getNumOf10Features(FastCluster currCluster,
			FastCluster otherCluster) {
		/*double[] currFeatures = currCluster.getFeatures();
		double[] otherFeatures = otherCluster.getFeatures();
		
		int num10Features = 0;
		for (int i=0;i<currCluster.getFeatures().length;i++) {
			if (otherFeatures[i] == 0) {
				if (currFeatures[i] > 0) {
					num10Features++;
				}
			}
		}*/
		
		Set<Integer> currIndices = currCluster.getNonZeroFeatureMap().keySet();
		
		int num10Features = 0;
		for (Integer currIndex : currIndices) {
			if (otherCluster.getNonZeroFeatureMap().get(currIndex) == null) {
				num10Features++;
			}
		}
				
		
		return num10Features;
	}
	
	private static int getNumOfFeaturesInBothEntities(
			FastCluster currCluster, FastCluster otherCluster) {
		
		/*double[] currFeatures = currCluster.getFeatures();
		double[] otherFeatures = otherCluster.getFeatures();
		
		double sumSharedFeatures = 0;
		for (int i=0;i<currCluster.getFeatures().length;i++) {
			if (currFeatures[i] > 0 && otherFeatures[i] > 0) {
				sumSharedFeatures += currFeatures[i] +  otherFeatures[i];
			}
		}*/
		
		Set<Integer> currIndices = currCluster.getNonZeroFeatureMap().keySet();
		
		int numSharedFeatures = 0;
		for (Integer currIndex : currIndices) {
			if (currCluster.getNonZeroFeatureMap().get(currIndex) != null && otherCluster.getNonZeroFeatureMap().get(currIndex) !=null) {
				numSharedFeatures++;
			}
		}
		
		return numSharedFeatures;
	}

	private static double getSumOfFeaturesInBothEntities(
			FastCluster currCluster, FastCluster otherCluster) {
		
		/*double[] currFeatures = currCluster.getFeatures();
		double[] otherFeatures = otherCluster.getFeatures();
		
		double sumSharedFeatures = 0;
		for (int i=0;i<currCluster.getFeatures().length;i++) {
			if (currFeatures[i] > 0 && otherFeatures[i] > 0) {
				sumSharedFeatures += currFeatures[i] +  otherFeatures[i];
			}
		}*/
		
		Set<Integer> currIndices = currCluster.getNonZeroFeatureMap().keySet();
		
		double sumSharedFeatures = 0;
		for (Integer currIndex : currIndices) {
			if (currCluster.getNonZeroFeatureMap().get(currIndex) != null && otherCluster.getNonZeroFeatureMap().get(currIndex) !=null) {
				Double currFeatureValue = currCluster.getNonZeroFeatureMap().get(currIndex);
				Double otherFeatureValue = otherCluster.getNonZeroFeatureMap().get(currIndex);
				sumSharedFeatures = currFeatureValue + otherFeatureValue;
			}
		}
		
		return sumSharedFeatures;
	}

	public static double getStructAndConcernMeasure(int numberOfEntitiesToBeClustered, FastCluster cluster,
			FastCluster otherCluster) {
		if (cluster.getFeaturesLength()!=otherCluster.getFeaturesLength()) {
			throw new IllegalArgumentException("cluster.getFeaturesLength()!=otherCluster.getFeaturesLength()()");
		}
		int featuresLength = cluster.getFeaturesLength();
		double[] firstDist = new double[featuresLength];
		double[] secondDist = new double[featuresLength];
		
		normalizeFeatureVectorOfCluster(cluster, featuresLength, firstDist);
		normalizeFeatureVectorOfCluster(otherCluster, featuresLength, secondDist);
		
		double jsDivergenceStruct = Maths.jensenShannonDivergence(firstDist, secondDist);
		if (Double.isInfinite(jsDivergenceStruct)) {
			jsDivergenceStruct = Double.MAX_VALUE;
		}
		
		double jsDivergenceConcern = SimCalcUtil.getJSDivergence(cluster, otherCluster);
		if (Double.isInfinite(jsDivergenceConcern)) {
			jsDivergenceConcern = Double.MIN_VALUE;
		}
		double structAndConcernMeasure =  0.5*jsDivergenceStruct+0.5*jsDivergenceConcern;
		
		if (Double.isNaN(structAndConcernMeasure)) {
			throw new RuntimeException("infoLossMeasure is NaN");
		}
		
		return structAndConcernMeasure;
	}
	
	public static double getInfoLossMeasure(int numberOfEntitiesToBeClustered, FastCluster cluster,
			FastCluster otherCluster) {
		if (cluster.getFeaturesLength()!=otherCluster.getFeaturesLength()) {
			throw new IllegalArgumentException("cluster.getFeaturesLength()!=otherCluster.getFeaturesLength()()");
		}
		int featuresLength = cluster.getFeaturesLength();
		double[] firstDist = new double[featuresLength];
		double[] secondDist = new double[featuresLength];
		
		normalizeFeatureVectorOfCluster(cluster, featuresLength, firstDist);
		normalizeFeatureVectorOfCluster(otherCluster, featuresLength, secondDist);
		
		double jsDivergence = Maths.jensenShannonDivergence(firstDist, secondDist);
		if (Double.isInfinite(jsDivergence)) {
			jsDivergence = Double.MAX_VALUE;
		}
		double infoLossMeasure = (cluster.getNumEntities()/numberOfEntitiesToBeClustered + otherCluster.getNumEntities()/numberOfEntitiesToBeClustered) * jsDivergence;
		
		if (Double.isNaN(infoLossMeasure)) {
			throw new RuntimeException("infoLossMeasure is NaN");
		}
		
		return infoLossMeasure;
	}

	private static void normalizeFeatureVectorOfCluster(FastCluster cluster,
			int featuresLength, double[] firstDist) {
		for (int i=0;i<featuresLength;i++) {
			if (cluster.getNonZeroFeatureMap().get(i) != null) {
				double featureValue = cluster.getNonZeroFeatureMap().get(i);
				firstDist[i] = featureValue/cluster.getNonZeroFeatureMap().size();
			}
			else { // this feature is zero
				firstDist[i] = 0;
			}
			
			/*if (otherCluster.getNonZeroFeatureMap().get(i) != null) {
				double featureValue = otherCluster.getNonZeroFeatureMap().get(i);
				secondDist[i] = featureValue/otherCluster.getNonZeroFeatureMap().size();
			}
			else { // this feature is zero
				secondDist[i] = 0;
			}*/
		}
	}
}
