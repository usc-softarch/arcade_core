package edu.usc.softarch.arcade.clustering.simmeasures;

import edu.usc.softarch.arcade.clustering.data.Cluster;
import edu.usc.softarch.arcade.topics.exceptions.DistributionSizeMismatchException;

public class PkgMeasure extends SimMeasure {
	private static SimMeasure singleton;

	public static SimMeasure getSingleton() {
		if (singleton == null) singleton = new PkgMeasure();
		return singleton;
	}

	@Override
	public double computeCellValue(int numEntitiesToCluster, Cluster row,
			Cluster col, int numFeatures) throws DistributionSizeMismatchException {
		throw new IllegalStateException("Pkg should not be used with Clusterer.");
	}
}
