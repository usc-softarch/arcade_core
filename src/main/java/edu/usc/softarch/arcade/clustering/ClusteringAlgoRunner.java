package edu.usc.softarch.arcade.clustering;

import java.io.FileNotFoundException;
import java.io.File;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import edu.usc.softarch.arcade.clustering.util.ClusterUtil;
import edu.usc.softarch.arcade.config.Config;
import edu.usc.softarch.arcade.config.Config.Granule;
import edu.usc.softarch.arcade.config.Config.Language;
import edu.usc.softarch.arcade.util.FileListing;

public class ClusteringAlgoRunner {
	// #region ATTRIBUTES --------------------------------------------------------
	private static Logger logger =
		LogManager.getLogger(ClusteringAlgoRunner.class);
	
	protected static List<FastCluster> fastClusters;
	protected static ArrayList<Cluster> clusters;
	protected static FastFeatureVectors fastFeatureVectors;
	protected static double maxClusterGain = 0;
	protected static int numClustersAtMaxClusterGain = 0;
	protected static int numberOfEntitiesToBeClustered = 0;
	// #endregion ATTRIBUTES -----------------------------------------------------

	// #region ACCESSORS ---------------------------------------------------------
	public static List<FastCluster> getFastClusters() { return fastClusters; }

	public static void setFastFeatureVectors(
			FastFeatureVectors inFastFeatureVectors) {
		fastFeatureVectors = inFastFeatureVectors;
	}
	// #endregion ACCESSORS ------------------------------------------------------
	
	protected static void initializeClusters(String srcDir) {
		fastClusters = new ArrayList<>();

		for (String name : fastFeatureVectors.getFeatureVectorNames()) {
			BitSet featureSet = fastFeatureVectors.getNameToFeatureSetMap().get(name);
			FastCluster fastCluster = new FastCluster(name, featureSet,
					fastFeatureVectors.getNamesInFeatureSet());
			
			addClusterConditionally(fastCluster);
		}
		
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

	private static void addClusterConditionally(FastCluster fastCluster) {
		if (Config.ignoreDependencyFilters) {
			fastClusters.add(fastCluster);
			return;
		}
		
		if (Config.getSelectedLanguage().equals(Language.c)) {
			Pattern p = Pattern.compile("\\.(c|cpp|cc|s|h|hpp|icc|ia|tbl|p)$");
			if (Config.getClusteringGranule().equals(Granule.file) && 
					isSingletonClusterNonexcluded(fastCluster) &&
					!fastCluster.getName().startsWith("/") &&
					p.matcher(fastCluster.getName()).find())
				fastClusters.add(fastCluster);
			else
				logger.debug("Excluding file: " + fastCluster.getName());
		}

		if (Config.getClusteringGranule().equals(Granule.func)) {
			if (fastCluster.getName().equals("\"##\""))
				return;
			fastClusters.add(fastCluster);
		}

		if (Config.getSelectedLanguage().equals(Language.java) && 
				Config.isClassInSelectedPackages(fastCluster.getName()))
			fastClusters.add(fastCluster);
	}

	public static boolean isSingletonClusterNonexcluded(FastCluster fastCluster) {
		if (Config.getExcludedEntities() == null) {
			return true;
		}
		return !Config.getExcludedEntities().contains(fastCluster.getName());
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
	
	protected static void performPostProcessingConditionally() {
		if (Config.getClustersToWriteList() == null) {
			logger.debug("Config.getClustersToWriteList() == null so skipping post processing");
			return;
		}
		if (Config.getClustersToWriteList().contains(fastClusters.size())) {
			String postProcMsg = "Performing post processing at " + fastClusters.size() + " number of clusters";
			logger.debug(postProcMsg);
			ClusterUtil.fastClusterPostProcessing(fastClusters,fastFeatureVectors);
		}
	}
}
