package edu.usc.softarch.arcade.clustering.techniques;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import edu.usc.softarch.arcade.classgraphs.ClassGraph;
import edu.usc.softarch.arcade.clustering.FastFeatureVectors;
import edu.usc.softarch.arcade.clustering.Feature;
import edu.usc.softarch.arcade.clustering.StoppingCriterion;
import edu.usc.softarch.arcade.config.Config;

/**
 * @author joshua
 */
public class ClusteringEngine {
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

	public void run(String fastFeatureVectorsFilePath, String language,
			String clusteringAlgorithm, String stoppingCriterion,
			int numClusters, String simMeasure) throws Exception {
		FastFeatureVectors fastFeatureVectors = null;
		
		File fastFeatureVectorsFile = new File(fastFeatureVectorsFilePath);

		// Deserialize the object
		try (ObjectInputStream objInStream = new ObjectInputStream(
				new FileInputStream(fastFeatureVectorsFile))) {
			fastFeatureVectors = (FastFeatureVectors) objInStream.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		if (clusteringAlgorithm.equalsIgnoreCase("wca")) {
			WcaRunner.setFastFeatureVectors(fastFeatureVectors);
			if (stoppingCriterion.equalsIgnoreCase("preselected")) {
				StoppingCriterion stopCriterion = new ConcernClusteringRunner.PreSelectedStoppingCriterion(numClusters);
				WcaRunner.computeClustersWithPQAndWCA(stopCriterion, language, stoppingCriterion, simMeasure);
			}
			if (stoppingCriterion.equalsIgnoreCase("clustergain")) {
				StoppingCriterion singleClusterStopCriterion = new SingleClusterStoppingCriterion();
				WcaRunner.computeClustersWithPQAndWCA(singleClusterStopCriterion, language, stoppingCriterion, simMeasure);
				StoppingCriterion clusterGainStopCriterion = new ClusterGainStoppingCriterion();
				WcaRunner.computeClustersWithPQAndWCA(clusterGainStopCriterion, language, stoppingCriterion, simMeasure);
			}
		}
		
		for (int numTopics : Config.getNumTopicsList()) {
			Config.setNumTopics(numTopics);
			if (clusteringAlgorithm.equalsIgnoreCase("arc"))
				throw new Exception("there is a null instead of outputDir/base"); 
		}
		
		if (clusteringAlgorithm.equalsIgnoreCase("limbo")) {
			LimboRunner.setFastFeatureVectors(fastFeatureVectors);
			LimboRunner.computeClusters(new ConcernClusteringRunner.PreSelectedStoppingCriterion(numClusters), language, stoppingCriterion);
			if (stoppingCriterion.equalsIgnoreCase("clustergain"))
				LimboRunner.computeClusters(new ClusterGainStoppingCriterion(), language, stoppingCriterion);
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
