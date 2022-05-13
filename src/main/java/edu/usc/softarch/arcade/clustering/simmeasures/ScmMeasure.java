package edu.usc.softarch.arcade.clustering.simmeasures;

import cc.mallet.util.Maths;
import edu.usc.softarch.arcade.clustering.Cluster;
import edu.usc.softarch.arcade.topics.DistributionSizeMismatchException;

public class ScmMeasure extends SimMeasure {
	private static SimMeasure singleton;

	public static SimMeasure getSingleton() {
		if (singleton == null) singleton = new ScmMeasure();
		return singleton;
	}

	@Override
	public double computeCellValue(int numEntitiesToCluster, Cluster row,
			Cluster col, int numFeatures) {
		double[] firstDist = new double[numFeatures];
		double[] secondDist = new double[numFeatures];

		normalizeFeatureVectorOfCluster(row, numFeatures, firstDist);
		normalizeFeatureVectorOfCluster(col, numFeatures, secondDist);

		double jsDivergenceStruct = Maths.jensenShannonDivergence(firstDist, secondDist);
		if (Double.isInfinite(jsDivergenceStruct))
			jsDivergenceStruct = Double.MAX_VALUE;


		double jsDivergenceConcern = 0;
		try {
			jsDivergenceConcern =
				row.getDocTopicItem().getJsDivergence(
					col.getDocTopicItem());
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
}