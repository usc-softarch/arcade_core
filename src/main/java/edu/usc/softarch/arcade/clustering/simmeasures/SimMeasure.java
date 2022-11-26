package edu.usc.softarch.arcade.clustering.simmeasures;

import edu.usc.softarch.arcade.clustering.data.Cluster;
import edu.usc.softarch.arcade.topics.exceptions.DistributionSizeMismatchException;

import java.util.BitSet;

public abstract class SimMeasure {
	public enum SimMeasureType { JS, SCM, UEM, UEMNM, IL, ARCIL, ARCUEM, ARCUEMNM, WJS }

	public abstract double computeCellValue(int numEntitiesToCluster, Cluster row,
		Cluster col, int numFeatures) throws DistributionSizeMismatchException;

	protected double[] normalizeFeatureVectorOfCluster(Cluster cluster,
			int featuresLength) {
		double[] result = new double[featuresLength];

		for (Integer index : cluster.getFeatureIndices()) {
			if (cluster.getFeatureMap()[index] != 0.0) {
				double featureValue = cluster.getFeatureMap()[index];
				result[index] = featureValue / cluster.getFeatureCount();
			}
		}

		return result;
	}

	protected int getNumOf01Features(Cluster currCluster,
			Cluster otherCluster) {
		int num01Features = 0;
		for (Integer otherIndex : otherCluster.getFeatureIndices())
			if (currCluster.getFeatureMap()[otherIndex] == 0.0)
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
		for (Integer integer : currCluster.getFeatureIndices())
			currIndices.set(integer);

		BitSet otherIndices = new BitSet(numFeatures);
		for (Integer integer : otherCluster.getFeatureIndices())
			otherIndices.set(integer);

		currIndices.or(otherIndices);
		currIndices.flip(0, numFeatures);

		return currIndices.cardinality();
	}

	protected int getNumOfFeaturesInBothEntities(Cluster currCluster,
			Cluster otherCluster) {
		int numSharedFeatures = 0;
		for (Integer currIndex : currCluster.getFeatureIndices())
			if (currCluster.getFeatureMap()[currIndex] != 0.0
					&& otherCluster.getFeatureMap()[currIndex] != 0.0)
				numSharedFeatures++;

		return numSharedFeatures;
	}

	protected double getSumOfFeaturesInBothEntities(Cluster currCluster,
			Cluster otherCluster) {
		double sumSharedFeatures = 0;
		for (Integer currIndex : currCluster.getFeatureIndices()) {
			if (currCluster.getFeatureMap()[currIndex] != 0.0
					&& otherCluster.getFeatureMap()[currIndex] != 0.0) {
				double currFeatureValue = currCluster.getFeatureMap()[currIndex];
				double otherFeatureValue = otherCluster.getFeatureMap()[currIndex];
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
			case WJS:
				return WeightedJsMeasure.getSingleton();
			default:
				throw new IllegalArgumentException(
					"Unknown similarity measure " + type);
		}
	}
}
