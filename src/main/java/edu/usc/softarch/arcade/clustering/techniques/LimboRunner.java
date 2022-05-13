package edu.usc.softarch.arcade.clustering.techniques;

import edu.usc.softarch.arcade.clustering.Architecture;
import edu.usc.softarch.arcade.clustering.ClusteringAlgorithmType;
import edu.usc.softarch.arcade.clustering.Cluster;
import edu.usc.softarch.arcade.clustering.SimData;
import edu.usc.softarch.arcade.clustering.SimilarityMatrix;
import edu.usc.softarch.arcade.clustering.criteria.SerializationCriterion;
import edu.usc.softarch.arcade.clustering.criteria.StoppingCriterion;
import edu.usc.softarch.arcade.topics.DistributionSizeMismatchException;

import java.io.FileNotFoundException;
import java.io.IOException;

public class LimboRunner extends ClusteringAlgoRunner {
	//region INTERFACE
	public static Architecture run(Architecture arch,
			SerializationCriterion serialCrit, StoppingCriterion stopCrit,
			String language, String stoppingCriterionName,
			SimilarityMatrix.SimMeasure simMeasure)
			throws IOException, DistributionSizeMismatchException {
		// Create the runner object
		ClusteringAlgoRunner runner = new LimboRunner(language,
			serialCrit, arch);
		// Compute the clustering algorithm and return the resulting architecture
		return runner.computeArchitecture(stopCrit, stoppingCriterionName,
			simMeasure);
	}
	//endregion

	//region CONSTRUCTORS
	public LimboRunner(String language,
			SerializationCriterion serializationCriterion, Architecture arch) {
		super(language, serializationCriterion, arch);
	}
	//endregion

	@Override
	public Architecture computeArchitecture(StoppingCriterion stoppingCriterion,
			String stopCriterion, SimilarityMatrix.SimMeasure simMeasure)
			throws DistributionSizeMismatchException, FileNotFoundException {
		SimilarityMatrix simMatrix = new SimilarityMatrix(
			simMeasure, this.architecture);

		while (stoppingCriterion.notReadyToStop(super.architecture)) {
			if (stopCriterion.equalsIgnoreCase("clustergain"))
				checkAndUpdateClusterGain(
					super.architecture.computeStructuralClusterGain());

			SimData data = identifyMostSimClusters(simMatrix);

			Cluster cluster = data.c1;
			Cluster otherCluster = data.c2;
			Cluster newCluster = new Cluster(ClusteringAlgorithmType.LIMBO, cluster, otherCluster);

			updateFastClustersAndSimMatrixToReflectMergedCluster(data,
				newCluster, simMatrix);

			if (super.serializationCriterion != null
					&& super.serializationCriterion.shouldSerialize()) {
				super.architecture.writeToRsf();
			}
		}

		return super.architecture;
	}
}
