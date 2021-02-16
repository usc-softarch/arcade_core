package edu.usc.softarch.arcade.clustering.techniques;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.xml.sax.SAXException;

import edu.usc.softarch.arcade.classgraphs.ClassGraph;
import edu.usc.softarch.arcade.clustering.ClusteringAlgorithmType;
import edu.usc.softarch.arcade.clustering.FastFeatureVectors;
import edu.usc.softarch.arcade.clustering.Feature;
import edu.usc.softarch.arcade.clustering.StoppingCriterion;
import edu.usc.softarch.arcade.config.Config;

/**
 * @author joshua
 */
public class ClusteringEngine {
	private static Logger logger = LogManager.getLogger(ClusteringEngine.class);

	class ClusterGainStoppingCriterion implements StoppingCriterion {
		public boolean notReadyToStop() {
			return ClusteringAlgoRunner.fastClusters.size() != 1
				&& ClusteringAlgoRunner.fastClusters.size() != ClusteringAlgoRunner.numClustersAtMaxClusterGain;
		}
	}

	class SingleClusterStoppingCriterion implements StoppingCriterion {
		public boolean notReadyToStop() {
			return ClusteringAlgoRunner.fastClusters.size() != 1;
		}
	}

	public ClusteringEngine() { }

	public ClusteringEngine(ClassGraph clg) throws TransformerException,
			ParserConfigurationException, SAXException, IOException {
	}

	public void run(String fastFeatureVectorsFilePath) throws Exception {
		FastFeatureVectors fastFeatureVectors = null;
		
		File fastFeatureVectorsFile = new File(fastFeatureVectorsFilePath);

		ObjectInputStream objInStream = new ObjectInputStream(
				new FileInputStream(fastFeatureVectorsFile));

		// Deserialize the object
		try {
			fastFeatureVectors = (FastFeatureVectors) objInStream.readObject();
			logger.debug("feature set size: "+ fastFeatureVectors.getNamesInFeatureSet().size());
			logger.debug("Names in Feature Set:");
			logger.debug(fastFeatureVectors.getNamesInFeatureSet());
			objInStream.close();

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		logger.debug("Read in serialized feature vectors...");

		if (Config.getCurrentClusteringAlgorithm()
				.equals(ClusteringAlgorithmType.WCA)) {
			WcaRunner.setFastFeatureVectors(fastFeatureVectors);
			if (Config.stoppingCriterion
					.equals(Config.StoppingCriterionConfig.preselected)) {
				StoppingCriterion stopCriterion = new ConcernClusteringRunner.PreSelectedStoppingCriterion();
				WcaRunner.computeClustersWithPQAndWCA(stopCriterion);
			}
			if (Config.stoppingCriterion
					.equals(Config.StoppingCriterionConfig.clustergain)) {
				StoppingCriterion singleClusterStopCriterion = new SingleClusterStoppingCriterion();
				WcaRunner.computeClustersWithPQAndWCA(singleClusterStopCriterion);
				StoppingCriterion clusterGainStopCriterion = new ClusterGainStoppingCriterion();
				WcaRunner.computeClustersWithPQAndWCA(clusterGainStopCriterion);
			}
		}
		
		for (int numTopics : Config.getNumTopicsList()) {
			Config.setNumTopics(numTopics);
			if (Config.getCurrentClusteringAlgorithm().equals(
					ClusteringAlgorithmType.ARC)) {
				throw new Exception("Pooyan-> there is a null instead of outputDir/base"); 
			}
		}
		
		if (Config.getCurrentClusteringAlgorithm().equals(ClusteringAlgorithmType.LIMBO)) {
			LimboRunner.setFastFeatureVectors(fastFeatureVectors);
			LimboRunner.computeClusters(new ConcernClusteringRunner.PreSelectedStoppingCriterion());
			if (Config.stoppingCriterion.equals(Config.StoppingCriterionConfig.clustergain)) {
				LimboRunner.computeClusters(new ClusterGainStoppingCriterion());
			}
		}
	}

	public class SharedFeature {
		Feature f1;
		Feature f2;

		SharedFeature(Feature f1, Feature f2) {
			this.f1 = f1;
			this.f2 = f2;
		}

		public String toString() {
			return this.f1 + "," + this.f2;
		}
	}
}