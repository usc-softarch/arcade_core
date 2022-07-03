package edu.usc.softarch.arcade.clustering.simmeasures;

import edu.usc.softarch.arcade.clustering.Cluster;
import edu.usc.softarch.arcade.topics.exceptions.DistributionSizeMismatchException;

public class ArcLimboMeasure extends SimMeasure {
	private static SimMeasure singleton;

	public static SimMeasure getSingleton() {
		if (singleton == null) singleton = new ArcLimboMeasure();
		return singleton;
	}

	@Override
	public double computeCellValue(int numEntitiesToCluster, Cluster row,
		Cluster col, int numFeatures) throws DistributionSizeMismatchException {
		double arcVal = JsMeasure.getSingleton().computeCellValue(
			numEntitiesToCluster, row, col, numFeatures);
		double uemVal = InfoLossMeasure.getSingleton().computeCellValue(
			numEntitiesToCluster, row, col, numFeatures);

		return (arcVal + uemVal) / 2;
	}
}
