package edu.usc.softarch.arcade.clustering.techniques;

import edu.usc.softarch.arcade.clustering.Cluster;
import edu.usc.softarch.arcade.clustering.SimData;
import edu.usc.softarch.arcade.clustering.SimilarityMatrix;
import edu.usc.softarch.arcade.clustering.criteria.StoppingCriterion;
import edu.usc.softarch.arcade.topics.DistributionSizeMismatchException;

public class WcaRunner extends ClusteringAlgoRunner {
	public void computeClustersWithPQAndWCA(
			StoppingCriterion stopCriterion, String language,
			String stoppingCriterion, SimilarityMatrix.SimMeasure simMeasure)
			throws DistributionSizeMismatchException {
		initializeClusters(language);

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
