package edu.usc.softarch.arcade.clustering.simmeasures;

import cc.mallet.util.Maths;
import edu.usc.softarch.arcade.clustering.Cluster;

public class InfoLossMeasure extends SimMeasure {
	private static SimMeasure singleton;

	public static SimMeasure getSingleton() {
		if (singleton == null) singleton = new InfoLossMeasure();
		return singleton;
	}

	@Override
	public double computeCellValue(int numEntitiesToCluster, Cluster row,
			Cluster col, int numFeatures) {
		double[] firstDist = normalizeFeatureVectorOfCluster(row, numFeatures);
		double[] secondDist = normalizeFeatureVectorOfCluster(col, numFeatures);

		double jsDivergence = Maths.jensenShannonDivergence(firstDist, secondDist);
		if (Double.isInfinite(jsDivergence))
			jsDivergence = Double.MAX_VALUE;

		double infoLossMeasure = ((double) row.getNumEntities() /
			numEntitiesToCluster	+ (double) col.getNumEntities() /
			numEntitiesToCluster) * jsDivergence;

		if (Double.isNaN(infoLossMeasure))
			throw new RuntimeException("infoLossMeasure is NaN");

		return infoLossMeasure;
	}
}
