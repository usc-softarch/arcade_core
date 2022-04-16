package edu.usc.softarch.arcade.clustering.techniques;

import java.io.FileNotFoundException;
import java.io.File;
import java.util.BitSet;
import java.util.List;
import java.util.regex.Pattern;

import edu.usc.softarch.arcade.clustering.Cluster;
import edu.usc.softarch.arcade.clustering.Architecture;
import edu.usc.softarch.arcade.clustering.FastFeatureVectors;
import edu.usc.softarch.arcade.config.Config;
import edu.usc.softarch.arcade.config.Config.Granule;
import edu.usc.softarch.arcade.util.FileListing;

public class ClusteringAlgoRunner {
	// #region ATTRIBUTES --------------------------------------------------------
	public static Architecture architecture;
	protected static FastFeatureVectors fastFeatureVectors;
	protected static double maxClusterGain = 0;
	public static int numClustersAtMaxClusterGain = 0;
	protected static int numberOfEntitiesToBeClustered = 0;
	// #endregion ATTRIBUTES -----------------------------------------------------

	// #region ACCESSORS ---------------------------------------------------------
	public Architecture getFastClusters() { return architecture; }

	public static void setFastFeatureVectors(
			FastFeatureVectors inFastFeatureVectors) {
		fastFeatureVectors = inFastFeatureVectors;
	}
	protected void removeCluster(Cluster cluster) {
		architecture.remove(cluster.getName());	}
	protected void addCluster(Cluster cluster) {
		architecture.put(cluster.getName(), cluster); }
	// #endregion ACCESSORS ------------------------------------------------------
	
	protected static void initializeClusters(String srcDir, String language) {
		architecture = new Architecture();

		// For each cell in the adjacency matrix
		for (String name : fastFeatureVectors.getFeatureVectorNames()) {
			// Get the vector relative to that cell
			BitSet featureSet = fastFeatureVectors.getNameToFeatureSetMap().get(name);
			// Create a cluster containing only that cell
			Cluster cluster = new Cluster(name, featureSet,
				fastFeatureVectors.getNamesInFeatureSet());
			
			// Add the cluster except extraordinary circumstances (assume always)
			addClusterConditionally(cluster, language);
		}
		
		// Unknown whether this block ever executes
		try {
			if (architecture.isEmpty()) {
				List<File> javaFiles =
					FileListing.getFileListing(new File(srcDir), ".java");
				
				for (File javaFile : javaFiles) {
					Cluster cluster = new Cluster(javaFile.getPath());
					architecture.put(cluster.getName(), cluster);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * For almost all situations, adds the cluster to the list.
	 */
	private static void addClusterConditionally(Cluster cluster, String language) {
		// If the source language is C or C++, add the only C-based entities
		if (language.equalsIgnoreCase("c")) {
			Pattern p = Pattern.compile("\\.(c|cpp|cc|s|h|hpp|icc|ia|tbl|p)$");
			// First condition to be assumed true
			// Second condition to be assumed true
			// Third condition checks whether the cluster is based on a valid C entity
			if (Config.getClusteringGranule().equals(Granule.file) &&
					!cluster.getName().startsWith("/") &&
					p.matcher(cluster.getName()).find())
				architecture.put(cluster.getName(), cluster);
		}

		// This block is used only for certain older modules, disregard
		if (Config.getClusteringGranule().equals(Granule.func)) {
			if (cluster.getName().equals("\"##\""))
				return;
			architecture.put(cluster.getName(), cluster);
		}

		// If the source language is Java, add all clusters
		// Second condition to be assumed true
		if (language.equalsIgnoreCase("java"))
			architecture.put(cluster.getName(), cluster);
	}
	
	protected static void checkAndUpdateClusterGain(double clusterGain) {
		if (clusterGain > maxClusterGain) {
			maxClusterGain = clusterGain;
			numClustersAtMaxClusterGain = architecture.size();
		}
	}
}
