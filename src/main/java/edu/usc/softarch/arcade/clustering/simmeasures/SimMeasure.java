package edu.usc.softarch.arcade.clustering.simmeasures;

import edu.usc.softarch.arcade.clustering.Cluster;
import edu.usc.softarch.arcade.topics.exceptions.DistributionSizeMismatchException;

import java.util.BitSet;
import java.util.Set;

public abstract class SimMeasure {
	public enum SimMeasureType { JS, SCM, UEM, UEMNM, IL, ARCIL, ARCUEM, ARCUEMNM }

	public abstract double computeCellValue(int numEntitiesToCluster, Cluster row,
		Cluster col, int numFeatures) throws DistributionSizeMismatchException;

	protected void normalizeFeatureVectorOfCluster(Cluster cluster,
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

	protected int getNumOf01Features(Cluster currCluster,
			Cluster otherCluster) {
		Set<Integer> otherIndices = otherCluster.getFeatureMap().keySet();

		int num01Features = 0;
		for (Integer otherIndex : otherIndices)
			if (currCluster.getFeatureMap().get(otherIndex) == null)
				num01Features++;

		return num01Features;
	}

	protected int getNumOf10Features(Cluster currCluster,
			Cluster otherCluster) {
		return getNumOf01Features(otherCluster, currCluster);
	}

	protected int getNumOf00Features(Cluster currCluster,
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

	protected int getNumOfFeaturesInBothEntities(Cluster currCluster,
			Cluster otherCluster) {
		Set<Integer> currIndices = currCluster.getFeatureMap().keySet();

		int numSharedFeatures = 0;
		for (Integer currIndex : currIndices)
			if (currCluster.getFeatureMap().get(currIndex) != null
				&& otherCluster.getFeatureMap().get(currIndex) !=null)
				numSharedFeatures++;

		return numSharedFeatures;
	}

	protected double getSumOfFeaturesInBothEntities(Cluster currCluster,
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

	public static SimMeasure makeSimMeasure(SimMeasureType type) {
		switch(type) {
			case SCM:
				return ScmMeasure.getSingleton();
			case IL:
				return InfoLossMeasure.getSingleton();
			case UEM:
				return UemMeasure.getSingleton();
			case UEMNM:
				return UemnmMeasure.getSingleton();
			case JS:
				return JsMeasure.getSingleton();
			case ARCIL:
				return ArcLimboMeasure.getSingleton();
			case ARCUEM:
				return ArcUemMeasure.getSingleton();
			case ARCUEMNM:
				return ArcUemnmMeasure.getSingleton();
			default:
				throw new IllegalArgumentException(
					"Unknown similarity measure " + type);
		}
	}
}
