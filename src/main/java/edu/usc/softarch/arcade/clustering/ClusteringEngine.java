package edu.usc.softarch.arcade.clustering;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import edu.usc.softarch.arcade.classgraphs.ClassGraph;
import edu.usc.softarch.arcade.config.Config;
import edu.usc.softarch.arcade.util.StopWatch;

/**
 * @author joshua
 */
public class ClusteringEngine {
	private static Logger logger = Logger.getLogger(ClusteringEngine.class);

	public ClusteringEngine() { }

	public ClusteringEngine(ClassGraph clg) throws TransformerException,
			ParserConfigurationException, SAXException, IOException {
	}

	public void run() throws Exception {
		FastFeatureVectors fastFeatureVectors = null;

		ArrayList<FastCluster> fastClusters = null;
		
		File fastFeatureVectorsFile = new File(
				Config.getFastFeatureVectorsFilename());

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

		StopWatch stopwatch = new StopWatch();

		stopwatch.start();
		if (Config.getCurrentClusteringAlgorithm()
				.equals(ClusteringAlgorithmType.WCA)) {
			WcaRunner.setFastFeatureVectors(fastFeatureVectors);
			if (Config.stoppingCriterion
					.equals(Config.StoppingCriterionConfig.preselected)) {
				StoppingCriterion stopCriterion = new PreSelectedStoppingCriterion();
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
			LimboRunner.computeClusters(new PreSelectedStoppingCriterion());
			if (Config.stoppingCriterion.equals(Config.StoppingCriterionConfig.clustergain)) {
				LimboRunner.computeClusters(new ClusterGainStoppingCriterion());
			}
		}
		stopwatch.stop();

		String timeInSecsToComputeClusters = "Time in seconds to compute clusters: "
				+ stopwatch.getElapsedTimeSecs();
		String timeInMilliSecondsToComputeClusters = "Time in milliseconds to compute clusters: "
				+ stopwatch.getElapsedTime();
		logger.debug(timeInSecsToComputeClusters);
		System.out.println(timeInSecsToComputeClusters);
		logger.debug(timeInMilliSecondsToComputeClusters);
		System.out.println(timeInMilliSecondsToComputeClusters);
		logger.debug("Final clusters: " + fastClusters);
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