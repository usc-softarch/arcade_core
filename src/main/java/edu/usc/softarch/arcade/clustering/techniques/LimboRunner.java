package edu.usc.softarch.arcade.clustering.techniques;

import edu.usc.softarch.arcade.clustering.ClusteringAlgorithmType;
import edu.usc.softarch.arcade.clustering.Cluster;
import edu.usc.softarch.arcade.clustering.SimData;
import edu.usc.softarch.arcade.clustering.SimilarityMatrix;
import edu.usc.softarch.arcade.clustering.criteria.StoppingCriterion;
import edu.usc.softarch.arcade.topics.DistributionSizeMismatchException;

public class LimboRunner extends ClusteringAlgoRunner {
	public void computeClusters(StoppingCriterion stopCriterion,
			String language, String stoppingCriterion)
			throws DistributionSizeMismatchException {
		initializeClusters(language);

		SimilarityMatrix simMatrix = new SimilarityMatrix(
			SimilarityMatrix.SimMeasure.IL, this.architecture);

		while (stopCriterion.notReadyToStop()) {
			if (stoppingCriterion.equalsIgnoreCase("clustergain"))
				checkAndUpdateClusterGain(
					super.architecture.computeStructuralClusterGain());

			SimData data = identifyMostSimClusters(simMatrix);

			Cluster cluster = data.c1;
			Cluster otherCluster = data.c2;
			Cluster newCluster = new Cluster(ClusteringAlgorithmType.LIMBO, cluster, otherCluster);

			updateFastClustersAndSimMatrixToReflectMergedCluster(data,newCluster,simMatrix);
		}
	}
}
