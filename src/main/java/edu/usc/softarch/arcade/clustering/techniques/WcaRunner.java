package edu.usc.softarch.arcade.clustering.techniques;

import edu.usc.softarch.arcade.clustering.Cluster;
import edu.usc.softarch.arcade.clustering.FeatureVectors;
import edu.usc.softarch.arcade.clustering.SimData;
import edu.usc.softarch.arcade.clustering.SimilarityMatrix;
import edu.usc.softarch.arcade.clustering.criteria.StoppingCriterion;
import edu.usc.softarch.arcade.topics.DistributionSizeMismatchException;

public class WcaRunner extends ClusteringAlgoRunner {
	//region CONSTRUCTORS
	public WcaRunner(String language, FeatureVectors vectors) {
		super(language, vectors);	}

	public WcaRunner(String language, FeatureVectors vectors, String packagePrefix) {
		super(language, vectors, packagePrefix); }
	//endregion

	@Override
	public void computeArchitecture(
			StoppingCriterion stopCriterion, String stoppingCriterion,
			SimilarityMatrix.SimMeasure simMeasure)
			throws DistributionSizeMismatchException {
		initializeClusters();

		SimilarityMatrix simMatrix = new SimilarityMatrix(simMeasure, this.architecture);

		while (stopCriterion.notReadyToStop()) {
			if (stoppingCriterion.equalsIgnoreCase("clustergain")) {
				double clusterGain = 0;
				clusterGain = super.architecture.computeStructuralClusterGain();
				checkAndUpdateClusterGain(clusterGain);
			}

			SimData data = identifyMostSimClusters(simMatrix);

			Cluster cluster = data.c1;
			Cluster otherCluster = data.c2;
			Cluster newCluster = new Cluster(cluster, otherCluster);

			updateFastClustersAndSimMatrixToReflectMergedCluster(data, newCluster, simMatrix);
		}
	}
}
