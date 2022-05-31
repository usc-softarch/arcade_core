package edu.usc.softarch.arcade.clustering.techniques;

import java.io.FileNotFoundException;
import java.io.IOException;

import edu.usc.softarch.arcade.clustering.Architecture;
import edu.usc.softarch.arcade.clustering.Cluster;
import edu.usc.softarch.arcade.clustering.ConcernArchitecture;
import edu.usc.softarch.arcade.clustering.simmeasures.SimData;
import edu.usc.softarch.arcade.clustering.simmeasures.SimMeasure;
import edu.usc.softarch.arcade.clustering.simmeasures.SimilarityMatrix;
import edu.usc.softarch.arcade.clustering.criteria.SerializationCriterion;
import edu.usc.softarch.arcade.clustering.criteria.StoppingCriterion;
import edu.usc.softarch.arcade.topics.DistributionSizeMismatchException;

public class ConcernClusteringRunner extends ClusteringAlgoRunner {
	//region INTERFACE
	public static Architecture run(ClusteringAlgoArguments parsedArguments)
			throws IOException, DistributionSizeMismatchException {
		return run(
			parsedArguments.concernArch,
			parsedArguments.serialCrit,
			parsedArguments.stopCrit,
			parsedArguments.language,
			parsedArguments.stoppingCriterion,
			parsedArguments.simMeasure);
	}

	public static Architecture run(ConcernArchitecture arch,
			SerializationCriterion serialCrit, StoppingCriterion stopCrit,
			String language, String stoppingCriterionName,
			SimMeasure.SimMeasureType simMeasure)
			throws IOException, DistributionSizeMismatchException {
		// Create the runner object
		ConcernClusteringRunner runner = new ConcernClusteringRunner(
			language, serialCrit, arch);
		// Compute the clustering algorithm and return the resulting architecture
		return runner.computeArchitecture(stopCrit, stoppingCriterionName,
			simMeasure);
	}
	//endregion INTERFACE

	//region CONSTRUCTORS
	public ConcernClusteringRunner(String language,
			SerializationCriterion serializationCriterion, ConcernArchitecture arch) {
		super(language, serializationCriterion, arch);
	}
	//endregion CONSTRUCTORS

	@Override
	public Architecture computeArchitecture(StoppingCriterion stopCriterion,
			String stoppingCriterion, SimMeasure.SimMeasureType simMeasure)
			throws DistributionSizeMismatchException, FileNotFoundException {
		SimilarityMatrix simMatrix = initializeSimMatrix(simMeasure);

		while (stopCriterion.notReadyToStop(super.architecture)) {
			if (stoppingCriterion.equalsIgnoreCase("clustergain"))
				checkAndUpdateClusterGain(architecture.computeTopicClusterGain());

			SimData data = identifyMostSimClusters(simMatrix);
			Cluster newCluster = ConcernArchitecture.mergeClustersUsingTopics(
				data.c1, data.c2);

			updateFastClustersAndSimMatrixToReflectMergedCluster(
				data, newCluster, simMatrix);

			if (super.serializationCriterion != null
					&& super.serializationCriterion.shouldSerialize())
				super.architecture.writeToRsf();
		}

		return super.architecture;
	}

	protected SimilarityMatrix initializeSimMatrix(
			SimMeasure.SimMeasureType simMeasure)
			throws DistributionSizeMismatchException {
		return new SimilarityMatrix(simMeasure, this.architecture);
	}
}
