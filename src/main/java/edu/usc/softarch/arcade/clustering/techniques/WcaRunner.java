package edu.usc.softarch.arcade.clustering.techniques;

import edu.usc.softarch.arcade.clustering.Architecture;
import edu.usc.softarch.arcade.clustering.Cluster;
import edu.usc.softarch.arcade.clustering.FeatureVectors;
import edu.usc.softarch.arcade.clustering.SimData;
import edu.usc.softarch.arcade.clustering.SimilarityMatrix;
import edu.usc.softarch.arcade.clustering.criteria.SerializationCriterion;
import edu.usc.softarch.arcade.clustering.criteria.SerializationCriterionFactory;
import edu.usc.softarch.arcade.clustering.criteria.StoppingCriterion;
import edu.usc.softarch.arcade.clustering.criteria.StoppingCriterionFactory;
import edu.usc.softarch.arcade.topics.DistributionSizeMismatchException;

import java.io.FileNotFoundException;
import java.io.IOException;

public class WcaRunner extends ClusteringAlgoRunner {
	//region INTERFACE
	public static void main(String[] args)
			throws IOException, DistributionSizeMismatchException {
		String featureVectorsFilePath = args[0];
		String language = args[1];
		String stoppingCriterion = args[2];
		int numClusters = Integer.parseInt(args[3]);
		String simMeasure = args[4];
		String serializationCriterion = args[5];
		int serializationCriterionVal = Integer.parseInt(args[6]);
		String projectName = args[7];
		String projectPath = args[8];
		String packagePrefix = args[9];

		run(featureVectorsFilePath, language, stoppingCriterion, numClusters,
			simMeasure, serializationCriterion, serializationCriterionVal,
			projectName, projectPath, packagePrefix);
	}

	public static Architecture run(String featureVectorsFilePath,
			String language, String stoppingCriterionName, int numClusters,
			String simMeasure, String serializationCriterionName,
			int serializationCriterionVal, String projectName,
			String projectPath, String packagePrefix)
			throws IOException, DistributionSizeMismatchException {
		// Read in the Feature Vectors
		FeatureVectors featureVectors =
			FeatureVectors.deserializeFFVectors(featureVectorsFilePath);
		// Create architecture with assigned output values
		Architecture arch = new Architecture(projectName, projectPath);
		// Create serialization criterion
		SerializationCriterion serializationCriterion =
			SerializationCriterionFactory.makeSerializationCriterion(
				serializationCriterionName, serializationCriterionVal, arch);
		// Create the runner object
		ClusteringAlgoRunner runner = new WcaRunner(language, featureVectors,
			packagePrefix, serializationCriterion, arch);
		// Establish the stopping criterion
		StoppingCriterion stoppingCriterion =
			StoppingCriterionFactory.makeStoppingCriterion(stoppingCriterionName,
				runner, numClusters);
		// Compute the clustering algorithm and return the resulting architecture
		return runner.computeArchitecture(stoppingCriterion, stoppingCriterionName,
			SimilarityMatrix.SimMeasure.valueOf(simMeasure.toUpperCase()));
	}
	//endregion

	//region CONSTRUCTORS
	public WcaRunner(String language, FeatureVectors vectors) {
		super(language, vectors);	}

	public WcaRunner(String language, FeatureVectors vectors,
			String packagePrefix) {
		super(language, vectors, packagePrefix); }

	public WcaRunner(String language, FeatureVectors vectors,
			String packagePrefix, SerializationCriterion serializationCriterion) {
		super(language, vectors, packagePrefix, serializationCriterion); }

	public WcaRunner(String language, FeatureVectors vectors,
			String packagePrefix, SerializationCriterion serializationCriterion,
			Architecture arch) {
		super(language, vectors, packagePrefix,
			serializationCriterion, arch);
	}
	//endregion

	@Override
	public Architecture computeArchitecture(
			StoppingCriterion stopCriterion, String stoppingCriterion,
			SimilarityMatrix.SimMeasure simMeasure)
			throws DistributionSizeMismatchException, FileNotFoundException {
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

			if (super.serializationCriterion != null
					&& super.serializationCriterion.shouldSerialize()) {
				super.architecture.writeToRsf();
			}
		}

		return super.architecture;
	}
}
