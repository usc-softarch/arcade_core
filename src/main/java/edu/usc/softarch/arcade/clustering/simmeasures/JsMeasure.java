package edu.usc.softarch.arcade.clustering.simmeasures;

import edu.usc.softarch.arcade.clustering.Cluster;
import edu.usc.softarch.arcade.topics.DistributionSizeMismatchException;

public class JsMeasure extends SimMeasure {
	private static SimMeasure singleton;

	public static SimMeasure getSingleton() {
		if (singleton == null) singleton = new JsMeasure();
		return singleton;
	}

	@Override
	public double computeCellValue(int numEntitiesToCluster, Cluster row,
			Cluster col, int numFeatures) throws DistributionSizeMismatchException {
		return row.getDocTopicItem().getJsDivergence(col.getDocTopicItem());
	}
}
