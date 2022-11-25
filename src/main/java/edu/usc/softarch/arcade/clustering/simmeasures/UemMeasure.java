package edu.usc.softarch.arcade.clustering.simmeasures;

import edu.usc.softarch.arcade.clustering.Cluster;

public class UemMeasure extends SimMeasure {
	private static SimMeasure singleton;

	public static SimMeasure getSingleton() {
		if (singleton == null) singleton = new UemMeasure();
		return singleton;
	}

	@Override
	public double computeCellValue(int numEntitiesToCluster, Cluster row,
			Cluster col, int numFeatures) {
		// Gets rid of rounding error arbitrariness
		double sumOfFeaturesInBothEntities1 =
			getSumOfFeaturesInBothEntities(row, col);
		double sumOfFeaturesInBothEntities2 =
			getSumOfFeaturesInBothEntities(col, row);
		double sumOfFeaturesInBothEntities = (sumOfFeaturesInBothEntities1
			+ sumOfFeaturesInBothEntities2) / 2;
		int numberOf10Features = getNumOf10Features(row, col);
		int numberOf01Features = getNumOf01Features(row, col);

		//TODO Need to get rid of NaN somehow
		return 1 - (0.5 * sumOfFeaturesInBothEntities /
			(0.5 * sumOfFeaturesInBothEntities + numberOf10Features
				+ numberOf01Features));
	}
}
