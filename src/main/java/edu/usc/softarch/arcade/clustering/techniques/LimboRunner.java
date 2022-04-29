package edu.usc.softarch.arcade.clustering.techniques;

import edu.usc.softarch.arcade.clustering.ClusteringAlgorithmType;
import edu.usc.softarch.arcade.clustering.Cluster;
import edu.usc.softarch.arcade.clustering.FeatureVectors;
import edu.usc.softarch.arcade.clustering.SimData;
import edu.usc.softarch.arcade.clustering.SimilarityMatrix;
import edu.usc.softarch.arcade.clustering.criteria.StoppingCriterion;
import edu.usc.softarch.arcade.topics.DistributionSizeMismatchException;

public class LimboRunner extends ClusteringAlgoRunner {
	//region CONSTRUCTORS
	public LimboRunner(String language, FeatureVectors vectors) {
		super(language, vectors);	}
	//endregion

	@Override
	public void computeArchitecture(StoppingCriterion stoppingCriterion,
			String stopCriterion, SimilarityMatrix.SimMeasure simMeasure)
			throws DistributionSizeMismatchException {
		initializeClusters();

		SimilarityMatrix simMatrix = new SimilarityMatrix(
			simMeasure, this.architecture);

		while (stoppingCriterion.notReadyToStop()) {
			if (stopCriterion.equalsIgnoreCase("clustergain"))
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
