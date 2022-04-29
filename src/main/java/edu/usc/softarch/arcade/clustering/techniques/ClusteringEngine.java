package edu.usc.softarch.arcade.clustering.techniques;

import edu.usc.softarch.arcade.clustering.FeatureVectors;
import edu.usc.softarch.arcade.clustering.SimilarityMatrix;
import edu.usc.softarch.arcade.clustering.criteria.ClusterGainStoppingCriterion;
import edu.usc.softarch.arcade.clustering.criteria.PreSelectedStoppingCriterion;

import edu.usc.softarch.arcade.clustering.criteria.SingleClusterStoppingCriterion;
import edu.usc.softarch.arcade.clustering.criteria.StoppingCriterion;

/**
 * @author joshua
 */
public class ClusteringEngine {
	public static void main(String[] args) throws Exception {
		String featureVectorsFilePath = args[0];
		String language = args[1];
		String clusteringAlgorithm = args[2];
		String stoppingCriterion = args[3];
		int numClusters = Integer.parseInt(args[4]);
		String simMeasure = args[5];
		String outputPath = args[6];

		run(featureVectorsFilePath, language, clusteringAlgorithm,
			stoppingCriterion, numClusters, simMeasure, outputPath);
	}

	public static void run(String featureVectorsFilePath, String language,
			String clusteringAlgorithm, String stoppingCriterion,
			int numClusters, String simMeasure, String outputPath) throws Exception {
		FeatureVectors featureVectors = FeatureVectors.deserializeFFVectors(featureVectorsFilePath);;
		ClusteringAlgoRunner runner = null;

		if (clusteringAlgorithm.equalsIgnoreCase("wca")) {
			runner = new WcaRunner(language, featureVectors);
			if (stoppingCriterion.equalsIgnoreCase("preselected")) {
				StoppingCriterion stopCriterion = new PreSelectedStoppingCriterion(numClusters, runner);
				runner.computeArchitecture(stopCriterion, stoppingCriterion,
					SimilarityMatrix.SimMeasure.valueOf(simMeasure.toUpperCase()));
			}
			if (stoppingCriterion.equalsIgnoreCase("clustergain")) {
				StoppingCriterion singleClusterStopCriterion = new SingleClusterStoppingCriterion(runner);
				runner.computeArchitecture(singleClusterStopCriterion, stoppingCriterion,
					SimilarityMatrix.SimMeasure.valueOf(simMeasure.toUpperCase()));
				StoppingCriterion clusterGainStopCriterion = new ClusterGainStoppingCriterion(runner);
				runner.computeArchitecture(clusterGainStopCriterion, stoppingCriterion,
					SimilarityMatrix.SimMeasure.valueOf(simMeasure.toUpperCase()));
			}
		}

		if (clusteringAlgorithm.equalsIgnoreCase("limbo")) {
			runner = new LimboRunner(language, featureVectors);
			runner.setFeatureVectors(featureVectors);
			runner.computeArchitecture(new PreSelectedStoppingCriterion(numClusters, runner), stoppingCriterion,
				SimilarityMatrix.SimMeasure.valueOf(simMeasure.toUpperCase()));
			if (stoppingCriterion.equalsIgnoreCase("clustergain"))
				runner.computeArchitecture(new ClusterGainStoppingCriterion(runner), stoppingCriterion,
					SimilarityMatrix.SimMeasure.valueOf(simMeasure.toUpperCase()));
		}

		runner.architecture.writeToRsf(
			runner.architecture.computeArchitectureIndex(), outputPath);
	}
}
