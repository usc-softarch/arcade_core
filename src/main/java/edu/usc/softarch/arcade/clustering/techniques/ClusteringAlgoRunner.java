package edu.usc.softarch.arcade.clustering.techniques;

import java.io.FileNotFoundException;
import java.util.BitSet;
import java.util.Map;
import java.util.regex.Pattern;

import edu.usc.softarch.arcade.clustering.Cluster;
import edu.usc.softarch.arcade.clustering.Architecture;
import edu.usc.softarch.arcade.clustering.FeatureVectors;
import edu.usc.softarch.arcade.clustering.SimData;
import edu.usc.softarch.arcade.clustering.SimilarityMatrix;
import edu.usc.softarch.arcade.clustering.criteria.SerializationCriterion;
import edu.usc.softarch.arcade.clustering.criteria.StoppingCriterion;
import edu.usc.softarch.arcade.config.Config;
import edu.usc.softarch.arcade.config.Config.Granule;
import edu.usc.softarch.arcade.topics.DistributionSizeMismatchException;

public abstract class ClusteringAlgoRunner {
	//region ATTRIBUTES
	public Architecture architecture;
	protected FeatureVectors featureVectors;
	protected static double maxClusterGain = 0;
	public static int numClustersAtMaxClusterGain = 0;
	public static int numberOfEntitiesToBeClustered = 0;
	protected final String language;
	private final String packagePrefix;
	protected final SerializationCriterion serializationCriterion;
	//endregion ATTRIBUTES

	//region CONTRUCTORS
	protected ClusteringAlgoRunner(String language, FeatureVectors vectors) {
		this(language, vectors, "", null); }

	protected ClusteringAlgoRunner(String language, FeatureVectors vectors,
			SerializationCriterion serializationCriterion) {
		this(language, vectors, "", serializationCriterion); }

	protected ClusteringAlgoRunner(String language, FeatureVectors vectors,
			String packagePrefix) {
		this(language, vectors, packagePrefix, null); }

	protected ClusteringAlgoRunner(String language, FeatureVectors vectors,
			String packagePrefix, SerializationCriterion serializationCriterion) {
		this.language = language;
		this.featureVectors = vectors;
		this.packagePrefix = packagePrefix;
		this.serializationCriterion = serializationCriterion;
	}

	protected ClusteringAlgoRunner(String language, FeatureVectors vectors,
			String packagePrefix, SerializationCriterion serializationCriterion,
			Architecture arch) {
		this(language, vectors, packagePrefix, serializationCriterion);
		this.architecture = arch;
	}
	//endregion

	// #region ACCESSORS ---------------------------------------------------------
	public Architecture getArchitecture() { return architecture; }

	public void setFeatureVectors(FeatureVectors featureVectors) {
		this.featureVectors = featureVectors;
	}
	protected void removeCluster(Cluster cluster) {
		architecture.remove(cluster.getName());	}
	protected void addCluster(Cluster cluster) {
		architecture.put(cluster.getName(), cluster); }
	// #endregion ACCESSORS ------------------------------------------------------
	
	protected void initializeClusters() {
		if (this.architecture == null)
			this.architecture = new Architecture();

		// For each cell in the adjacency matrix
		for (String name : featureVectors.getFeatureVectorNames()) {
			// Get the vector relative to that cell
			BitSet featureSet = featureVectors.getNameToFeatureSetMap().get(name);
			// Create a cluster containing only that cell
			Cluster cluster = new Cluster(name, featureSet,
				featureVectors.getNamesInFeatureSet());
			
			// Add the cluster except extraordinary circumstances (assume always)
			addClusterConditionally(cluster, language);
		}

		numberOfEntitiesToBeClustered = architecture.size();
	}

	/**
	 * For almost all situations, adds the cluster to the list.
	 */
	private void addClusterConditionally(Cluster cluster, String language) {
		// If the source language is C or C++, add the only C-based entities
		if (language.equalsIgnoreCase("c")) {
			Pattern p = Pattern.compile("\\.(c|cpp|cc|s|h|hpp|icc|ia|tbl|p)$");
			// First condition to be assumed true
			// Second condition to be assumed true
			// Third condition checks whether the cluster is based on a valid C entity
			if (Config.getClusteringGranule().equals(Granule.file) &&
					!cluster.getName().startsWith("/") &&
					p.matcher(cluster.getName()).find())
				this.architecture.put(cluster.getName(), cluster);
		}

		// This block is used only for certain older modules, disregard
		if (Config.getClusteringGranule().equals(Granule.func)) {
			if (cluster.getName().equals("\"##\""))
				return;
			this.architecture.put(cluster.getName(), cluster);
		}

		// If the source language is Java, add all clusters that match prefix
		if (language.equalsIgnoreCase("java")
				&& (this.packagePrefix.isEmpty()
					|| cluster.getName().startsWith(this.packagePrefix)))
			this.architecture.put(cluster.getName(), cluster);
	}
	
	protected void checkAndUpdateClusterGain(double clusterGain) {
		if (clusterGain > maxClusterGain) {
			maxClusterGain = clusterGain;
			numClustersAtMaxClusterGain = this.architecture.size();
		}
	}

	protected void updateFastClustersAndSimMatrixToReflectMergedCluster(
		SimData data, Cluster newCluster,	SimilarityMatrix simMatrix)
		throws DistributionSizeMismatchException {
		// Sanity check
		if (data.c1.getName().equals(data.c2.getName()))
			throw new IllegalArgumentException("data.c1: " + data.c1
				+ " should not be the same as data.c2: " + data.c2);

		// Initializing variables
		Cluster cluster = data.c1;
		Cluster otherCluster = data.c2;

		// Remove the merged row and column from the matrix
		simMatrix.removeCluster(cluster);
		simMatrix.removeCluster(otherCluster);
		simMatrix.addCluster(newCluster);

		// Remove merged clusters, add new cluster
		removeCluster(cluster);
		removeCluster(otherCluster);
		addCluster(newCluster);
	}

	/**
	 * Looks for the smallest non-diagonal value in the matrix, which represents
	 * the pair of clusters with the lowest level of divergence (highest
	 * similarity).
	 *
	 * @param simMatrix Similarity matrix to analyze.
	 * @return The maximum-similarity cell.
	 */
	protected SimData identifyMostSimClusters(SimilarityMatrix simMatrix) {
		if (simMatrix.size() != architecture.size())
			throw new IllegalArgumentException("expected simMatrix.size():"
				+ simMatrix.size() + " to be fastClusters.size(): "
				+ architecture.size());

		for (Map<Cluster, SimData> col : simMatrix.getColumns())
			if (col.size() != architecture.size())
				throw new IllegalArgumentException("expected col.size():" + col.size()
					+ " to be fastClusters.size(): " + architecture.size());

		SimData toReturn = null;

		try {
			toReturn = simMatrix.getMinCell();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return toReturn;
	}

	public abstract Architecture computeArchitecture(
		StoppingCriterion stoppingCriterion, String stopCriterion,
		SimilarityMatrix.SimMeasure simMeasure)
		throws DistributionSizeMismatchException, FileNotFoundException;
}
