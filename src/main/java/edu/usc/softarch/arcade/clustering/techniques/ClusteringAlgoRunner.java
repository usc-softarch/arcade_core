package edu.usc.softarch.arcade.clustering.techniques;

import java.io.FileNotFoundException;
import java.io.File;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import edu.usc.softarch.arcade.clustering.Cluster;
import edu.usc.softarch.arcade.clustering.FastCluster;
import edu.usc.softarch.arcade.clustering.FastClusterArchitecture;
import edu.usc.softarch.arcade.clustering.FastFeatureVectors;
import edu.usc.softarch.arcade.config.Config;
import edu.usc.softarch.arcade.config.Config.Granule;
import edu.usc.softarch.arcade.config.Config.Language;
import edu.usc.softarch.arcade.util.FileListing;

public class ClusteringAlgoRunner {
	// #region ATTRIBUTES --------------------------------------------------------
	private static Logger logger =
		LogManager.getLogger(ClusteringAlgoRunner.class);
	
	public static FastClusterArchitecture fastClusters;
	protected static ArrayList<Cluster> clusters;
	protected static FastFeatureVectors fastFeatureVectors;
	protected static double maxClusterGain = 0;
	public static int numClustersAtMaxClusterGain = 0;
	protected static int numberOfEntitiesToBeClustered = 0;
	// #endregion ATTRIBUTES -----------------------------------------------------

	// #region ACCESSORS ---------------------------------------------------------
	public FastClusterArchitecture getFastClusters() { return fastClusters; }

	public static void setFastFeatureVectors(
			FastFeatureVectors inFastFeatureVectors) {
		fastFeatureVectors = inFastFeatureVectors;
	}
	// #endregion ACCESSORS ------------------------------------------------------
	
	protected static void initializeClusters(String srcDir) {
		fastClusters = new FastClusterArchitecture();

		// For each node in the adjacency matrix
		for (String name : fastFeatureVectors.getFeatureVectorNames()) {
			// Get the vector relative to that node
			BitSet featureSet = fastFeatureVectors.getNameToFeatureSetMap().get(name);
			// Create a cluster containing only that node
			FastCluster fastCluster = new FastCluster(name, featureSet,
				fastFeatureVectors.getNamesInFeatureSet());
			
			// Add the cluster except extraordinary circumstances (assume always)
			addClusterConditionally(fastCluster);
		}
		
		// Unknown whether this block ever executes
		try {
			if (fastClusters.isEmpty()) {
				List<File> javaFiles =
					FileListing.getFileListing(new File(srcDir), ".java");
				
				for (File javaFile : javaFiles) {
					FastCluster cluster = new FastCluster(javaFile.getPath());
					fastClusters.add(cluster);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		// Logging
		logger.debug("Listing initial cluster names using for-each...");
		for (FastCluster cluster : fastClusters)
			logger.debug(cluster.getName());
		logger.debug("Listing initial cluster names using indexed loop...");
		for (int i = 0; i < fastClusters.size(); i++) {
			FastCluster cluster = fastClusters.get(i);
			logger.debug(cluster.getName());
		}
		numberOfEntitiesToBeClustered = fastClusters.size();
		logger.debug("number of initial clusters: " + numberOfEntitiesToBeClustered);
	}

	/**
	 * For almost all situations, adds the cluster to the list.
	 */
	private static void addClusterConditionally(FastCluster fastCluster) {
		// If the source language is C or C++, add the only C-based entities
		if (Config.getSelectedLanguage().equals(Language.c)) {
			Pattern p = Pattern.compile("\\.(c|cpp|cc|s|h|hpp|icc|ia|tbl|p)$");
			// First condition to be assumed true
			// Second condition to be assumed true
			// Third condition checks whether the cluster is based on a valid C entity
			if (Config.getClusteringGranule().equals(Granule.file) &&
					!fastCluster.getName().startsWith("/") &&
					p.matcher(fastCluster.getName()).find())
				fastClusters.add(fastCluster);
			else
				logger.debug("Excluding file: " + fastCluster.getName());
		}

		// This block is used only for certain older modules, disregard
		if (Config.getClusteringGranule().equals(Granule.func)) {
			if (fastCluster.getName().equals("\"##\""))
				return;
			fastClusters.add(fastCluster);
		}

		// If the source language is Java, add all clusters
		// Second condition to be assumed true
		if (Config.getSelectedLanguage().equals(Language.java))
			fastClusters.add(fastCluster);
	}
	
	protected static void checkAndUpdateClusterGain(double clusterGain) {
		if (logger.isDebugEnabled()) {
			logger.debug("Current cluster gain: " + clusterGain);
			logger.debug("Current max cluster gain: " + maxClusterGain);
		}

		if (clusterGain > maxClusterGain) {
			if (logger.isDebugEnabled()) {
				logger.debug("Updating max cluster gain and num clusters at it...");
			}
			maxClusterGain = clusterGain;
			numClustersAtMaxClusterGain = fastClusters.size();
		}
	}
}