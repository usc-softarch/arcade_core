package edu.usc.softarch.arcade.clustering.techniques;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

import edu.usc.softarch.arcade.clustering.FeatureVectors;
import edu.usc.softarch.arcade.clustering.SimilarityMatrix;
import edu.usc.softarch.arcade.clustering.criteria.ClusterGainStoppingCriterion;
import edu.usc.softarch.arcade.clustering.criteria.PreSelectedStoppingCriterion;

import edu.usc.softarch.arcade.clustering.criteria.SingleClusterStoppingCriterion;
import edu.usc.softarch.arcade.clustering.criteria.StoppingCriterion;
import edu.usc.softarch.arcade.config.Config;

/**
 * @author joshua
 */
public class ClusteringEngine {
	public ClusteringEngine() { }

	public void run(String fastFeatureVectorsFilePath, String language,
			String clusteringAlgorithm, String stoppingCriterion,
			int numClusters, String simMeasure) throws Exception {
		FeatureVectors fastFeatureVectors = null;

		File fastFeatureVectorsFile = new File(fastFeatureVectorsFilePath);

		// Deserialize the object
		try (ObjectInputStream objInStream = new ObjectInputStream(
				new FileInputStream(fastFeatureVectorsFile))) {
			fastFeatureVectors = (FeatureVectors) objInStream.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		if (clusteringAlgorithm.equalsIgnoreCase("wca")) {
			WcaRunner runner = new WcaRunner();
			runner.setFeatureVectors(fastFeatureVectors);
			if (stoppingCriterion.equalsIgnoreCase("preselected")) {
				StoppingCriterion stopCriterion = new PreSelectedStoppingCriterion(numClusters, runner);
				runner.computeClustersWithPQAndWCA(stopCriterion, language, stoppingCriterion,
					SimilarityMatrix.SimMeasure.valueOf(simMeasure.toUpperCase()));
			}
			if (stoppingCriterion.equalsIgnoreCase("clustergain")) {
				StoppingCriterion singleClusterStopCriterion = new SingleClusterStoppingCriterion(runner);
				runner.computeClustersWithPQAndWCA(singleClusterStopCriterion, language, stoppingCriterion,
					SimilarityMatrix.SimMeasure.valueOf(simMeasure.toUpperCase()));
				StoppingCriterion clusterGainStopCriterion = new ClusterGainStoppingCriterion(runner);
				runner.computeClustersWithPQAndWCA(clusterGainStopCriterion, language, stoppingCriterion,
					SimilarityMatrix.SimMeasure.valueOf(simMeasure.toUpperCase()));
			}
		}

		for (int numTopics : Config.getNumTopicsList()) {
			Config.setNumTopics(numTopics);
			if (clusteringAlgorithm.equalsIgnoreCase("arc"))
				throw new Exception("there is a null instead of outputDir/base");
		}

		if (clusteringAlgorithm.equalsIgnoreCase("limbo")) {
			LimboRunner runner = new LimboRunner();
			runner.setFeatureVectors(fastFeatureVectors);
			runner.computeClusters(new PreSelectedStoppingCriterion(numClusters, runner), language, stoppingCriterion);
			if (stoppingCriterion.equalsIgnoreCase("clustergain"))
				runner.computeClusters(new ClusterGainStoppingCriterion(runner), language, stoppingCriterion);
		}
	}
}
