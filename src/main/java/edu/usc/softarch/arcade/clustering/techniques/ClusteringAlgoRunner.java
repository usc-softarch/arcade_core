package edu.usc.softarch.arcade.clustering.techniques;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import edu.usc.softarch.arcade.clustering.Cluster;
import edu.usc.softarch.arcade.clustering.Architecture;
import edu.usc.softarch.arcade.clustering.FeatureVectors;
import edu.usc.softarch.arcade.clustering.simmeasures.SimData;
import edu.usc.softarch.arcade.clustering.simmeasures.SimMeasure;
import edu.usc.softarch.arcade.clustering.simmeasures.SimilarityMatrix;
import edu.usc.softarch.arcade.clustering.criteria.SerializationCriterion;
import edu.usc.softarch.arcade.clustering.criteria.StoppingCriterion;
import edu.usc.softarch.arcade.topics.DistributionSizeMismatchException;

public abstract class ClusteringAlgoRunner {
	//region INTERFACE
	/**
	 * Primary entry point for all clustering algorithms. Arguments are:
	 *
	 * 0 : Clustering algorithm.
	 * 1 : Subject system language.
	 * 2 : Path to feature vectors file.
	 * 3 : Stopping criterion.
	 * 4 : Number of clusters to stop at, if using preselected. 0 otherwise.
	 * 5 : Similarity measure desired. Check compatibility in each algorithm.
	 * 6 : Serialization criterion.
	 * 7 : Parameter value of the serialization criterion.
	 * 8 : Name of the subject system.
	 * 9 : Path to place the output.
	 * 10: Package prefix to include in the analysis. If C, empty string.
	 * 11: Path to the subject system root.
	 * 12: Path to directory containing auxiliary artifacts.
	 */
	public static void main(String[] args)
			throws IOException, DistributionSizeMismatchException {
		ClusteringAlgoArguments parsedArguments =
			new ClusteringAlgoArguments(args);

		switch (args[0].toLowerCase()) {
			case "arc":
				runArc(parsedArguments, args);
				break;
			case "limbo":
				runLimbo(parsedArguments);
				break;
			case "wca":
				runWca(parsedArguments);
				break;
			default:
				throw new IllegalArgumentException(
					"Unknown clustering algorithm " + args[0]);
		}
	}

	private static void runArc(ClusteringAlgoArguments parsedArguments,
			String[] args) throws IOException {
		String outputDirPath = args[9];
		String sysDirPath = args[11];
		String artifactsDirPath = args[12];

		ConcernClusteringRunner.run(
			parsedArguments.arch,
			parsedArguments.serialCrit,
			parsedArguments.stopCrit,
			parsedArguments.language,
			parsedArguments.stoppingCriterion,
			parsedArguments.simMeasure,
			outputDirPath,
			sysDirPath,
			artifactsDirPath);
	}

	private static void runLimbo(ClusteringAlgoArguments parsedArguments)
			throws IOException, DistributionSizeMismatchException {
		LimboRunner.run(
			parsedArguments.arch,
			parsedArguments.serialCrit,
			parsedArguments.stopCrit,
			parsedArguments.language,
			parsedArguments.stoppingCriterion,
			parsedArguments.simMeasure);
	}

	private static void runWca(ClusteringAlgoArguments parsedArguments)
			throws IOException, DistributionSizeMismatchException {
		WcaRunner.run(
			parsedArguments.arch,
			parsedArguments.serialCrit,
			parsedArguments.stopCrit,
			parsedArguments.language,
			parsedArguments.stoppingCriterion,
			parsedArguments.simMeasure);
	}

	public static class ClusteringAlgoArguments {
		public final String language;
		public final Architecture arch;
		public final SerializationCriterion serialCrit;
		public final StoppingCriterion stopCrit;
		public final String stoppingCriterion;
		public final SimMeasure.SimMeasureType simMeasure;

		public ClusteringAlgoArguments(String[] args) throws IOException {
			this.language = args[1];
			this.arch = new Architecture(args[8], args[9],
				FeatureVectors.deserializeFFVectors(args[2]),	this.language, args[10]);
			this.serialCrit = SerializationCriterion.makeSerializationCriterion(
				args[6], Integer.parseInt(args[7]), arch);
			this.stopCrit = StoppingCriterion.makeStoppingCriterion(
				args[3], Integer.parseInt(args[4]));
			this.stoppingCriterion = args[3];
			this.simMeasure =
				SimMeasure.SimMeasureType.valueOf(args[5].toUpperCase());
		}
	}
	//endregion

	//region ATTRIBUTES
	protected final Architecture architecture;
	protected static double maxClusterGain = 0;
	public static int numClustersAtMaxClusterGain = 0;
	public static int numberOfEntitiesToBeClustered = 0;
	protected final String language;
	protected final SerializationCriterion serializationCriterion;
	//endregion ATTRIBUTES

	//region CONTRUCTORS
	protected ClusteringAlgoRunner(String language,
			SerializationCriterion serializationCriterion, Architecture arch) {
		this.language = language;
		this.serializationCriterion = serializationCriterion;
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
		SimMeasure.SimMeasureType simMeasure)
		throws DistributionSizeMismatchException, FileNotFoundException;
}
