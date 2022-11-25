package edu.usc.softarch.arcade.clustering.simmeasures;

import edu.usc.softarch.arcade.clustering.Cluster;

public class UemnmMeasure extends SimMeasure {
	private static SimMeasure singleton;

	public static SimMeasure getSingleton() {
		if (singleton == null) singleton = new UemnmMeasure();
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
		int num10Features = getNumOf10Features(row, col);
		int num01Features = getNumOf01Features(row, col);
		int num00Features = getNumOf00Features(row, col, numFeatures);
		int numSharedFeatures = getNumOfFeaturesInBothEntities(row, col);

		//TODO Need to get rid of NaN somehow
		return 1 - (0.5 * sumOfFeaturesInBothEntities /
			(0.5 * sumOfFeaturesInBothEntities + 2
				* ((double) num10Features + (double) num01Features)
				+ num00Features + numSharedFeatures));
	}
}
