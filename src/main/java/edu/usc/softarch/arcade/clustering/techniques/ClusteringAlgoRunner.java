package edu.usc.softarch.arcade.clustering.techniques;

import java.io.FileNotFoundException;
import java.util.Map;

import edu.usc.softarch.arcade.clustering.Cluster;
import edu.usc.softarch.arcade.clustering.Architecture;
import edu.usc.softarch.arcade.clustering.SimData;
import edu.usc.softarch.arcade.clustering.SimilarityMatrix;
import edu.usc.softarch.arcade.clustering.criteria.SerializationCriterion;
import edu.usc.softarch.arcade.clustering.criteria.StoppingCriterion;
import edu.usc.softarch.arcade.topics.DistributionSizeMismatchException;

public abstract class ClusteringAlgoRunner {
	//region ATTRIBUTES
	public Architecture architecture;
	protected static double maxClusterGain = 0;
	public static int numClustersAtMaxClusterGain = 0;
	public static int numberOfEntitiesToBeClustered = 0;
	protected final String language;
	protected final SerializationCriterion serializationCriterion;
	//endregion ATTRIBUTES

	//region CONTRUCTORS
	protected ClusteringAlgoRunner(String language,
			SerializationCriterion serializationCriterion) {
		this.language = language;
		this.serializationCriterion = serializationCriterion;
	}

	protected ClusteringAlgoRunner(String language,
			SerializationCriterion serializationCriterion, Architecture arch) {
		this(language, serializationCriterion);
		this.architecture = arch;
	}
	//endregion

	// #region ACCESSORS ---------------------------------------------------------
	public Architecture getArchitecture() { return architecture; }

	protected void removeCluster(Cluster cluster) {
		architecture.remove(cluster.getName());	}
	protected void addCluster(Cluster cluster) {
		architecture.put(cluster.getName(), cluster); }
	// #endregion ACCESSORS ------------------------------------------------------
	
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
