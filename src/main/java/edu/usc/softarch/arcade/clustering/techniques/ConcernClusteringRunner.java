package edu.usc.softarch.arcade.clustering.techniques;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import edu.usc.softarch.arcade.clustering.Architecture;
import edu.usc.softarch.arcade.clustering.Cluster;
import edu.usc.softarch.arcade.clustering.ClusteringAlgorithmType;
import edu.usc.softarch.arcade.clustering.ConcernArchitecture;
import edu.usc.softarch.arcade.clustering.simmeasures.SimData;
import edu.usc.softarch.arcade.clustering.simmeasures.SimMeasure;
import edu.usc.softarch.arcade.clustering.simmeasures.SimilarityMatrix;
import edu.usc.softarch.arcade.clustering.criteria.PreSelectedStoppingCriterion;
import edu.usc.softarch.arcade.clustering.criteria.SerializationCriterion;
import edu.usc.softarch.arcade.clustering.criteria.StoppingCriterion;
import edu.usc.softarch.arcade.topics.DistributionSizeMismatchException;
import edu.usc.softarch.arcade.topics.DocTopicItem;
import edu.usc.softarch.arcade.topics.UnmatchingDocTopicItemsException;

public class ConcernClusteringRunner extends ClusteringAlgoRunner {
	// #region INTERFACE ---------------------------------------------------------
	public static Architecture run(ClusteringAlgoArguments parsedArguments,
			String outputDirPath)
			throws IOException {
		return run(
			parsedArguments.concernArch,
			parsedArguments.serialCrit,
			parsedArguments.stopCrit,
			parsedArguments.language,
			parsedArguments.stoppingCriterion,
			parsedArguments.simMeasure,
			outputDirPath);
	}

	public static Architecture run(ConcernArchitecture arch,
			SerializationCriterion serialCrit, StoppingCriterion stopCrit,
			String language, String stoppingCriterionName,
			SimMeasure.SimMeasureType simMeasure, String outputDirPath)
			throws IOException {
		ConcernClusteringRunner runner = new ConcernClusteringRunner(
			language, serialCrit, arch);

		try {
			runner.computeArchitecture(stopCrit, stoppingCriterionName,	simMeasure);
		} catch (DistributionSizeMismatchException e) {
			e.printStackTrace(); //TODO Handle it
		}

		String prefix = outputDirPath + File.separator
			+ runner.getArchitecture().projectName;
		String arcClustersFilename = prefix	+ "_arc_clusters.rsf";

		runner.getArchitecture().writeToRsf(arcClustersFilename);

		return runner.getArchitecture();
	}
	// #endregion INTERFACE ------------------------------------------------------

	// #region CONSTRUCTORS ------------------------------------------------------
	public ConcernClusteringRunner(String language,
			SerializationCriterion serializationCriterion, ConcernArchitecture arch) {
		super(language, serializationCriterion, arch);
	}
	// #endregion CONSTRUCTORS ---------------------------------------------------

	@Override
	public Architecture computeArchitecture(
			StoppingCriterion stoppingCriterion, String stopCriterion,
			SimMeasure.SimMeasureType simMeasure)
			throws DistributionSizeMismatchException, FileNotFoundException {
		SimilarityMatrix simMatrix = initializeSimMatrix(simMeasure);

		while (stoppingCriterion.notReadyToStop(super.architecture)) {
			if (stopCriterion.equalsIgnoreCase("clustergain")) {
				double clusterGain = architecture.computeTopicClusterGain();
				checkAndUpdateClusterGain(clusterGain);
			}

			SimData data = identifyMostSimClusters(simMatrix);
			Cluster newCluster = mergeClustersUsingTopics(data);
			updateFastClustersAndSimMatrixToReflectMergedCluster(
				data, newCluster, simMatrix);

			if (super.serializationCriterion != null
				&& super.serializationCriterion.shouldSerialize()) {
				super.architecture.writeToRsf();
			}
		}

		return super.architecture;
	}

	protected SimilarityMatrix initializeSimMatrix(
			SimMeasure.SimMeasureType simMeasure)
			throws DistributionSizeMismatchException {
		return new SimilarityMatrix(simMeasure, this.architecture);	}

	private Cluster mergeClustersUsingTopics(SimData data) {
		Cluster cluster = data.c1;
		Cluster otherCluster = data.c2;
		return mergeClustersUsingTopics(cluster, otherCluster);
	}

	private Cluster mergeClustersUsingTopics(Cluster cluster, Cluster otherCluster) {
		Cluster newCluster =
			new Cluster(ClusteringAlgorithmType.ARC, cluster, otherCluster);
		
		try {
			newCluster.setDocTopicItem(new DocTopicItem(cluster, otherCluster));
		} catch (UnmatchingDocTopicItemsException e) {
			e.printStackTrace(); //TODO handle it
		}

		return newCluster;
	}
}
