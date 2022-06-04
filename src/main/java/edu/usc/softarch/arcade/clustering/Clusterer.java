package edu.usc.softarch.arcade.clustering;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import edu.usc.softarch.arcade.clustering.simmeasures.SimData;
import edu.usc.softarch.arcade.clustering.simmeasures.SimMeasure;
import edu.usc.softarch.arcade.clustering.simmeasures.SimilarityMatrix;
import edu.usc.softarch.arcade.clustering.criteria.SerializationCriterion;
import edu.usc.softarch.arcade.clustering.criteria.StoppingCriterion;
import edu.usc.softarch.arcade.topics.DistributionSizeMismatchException;
import edu.usc.softarch.arcade.topics.UnmatchingDocTopicItemsException;

public class Clusterer {
	//region INTERFACE
	/**
	 * Primary entry point for all clustering algorithms. Arguments are:
	 *
	 * 0 : Clustering algorithm.
	 * 1 : Subject system language.
	 * 2 : Path to feature vectors file.
	 * 3 : Stopping criterion.
	 * 4 : Stopping criterion parameter.
	 * 5 : Similarity measure desired. Check compatibility in each algorithm.
	 * 6 : Serialization criterion.
	 * 7 : Parameter value of the serialization criterion.
	 * 8 : Name of the subject system.
	 * 9 : Path to place the output.
	 * 10: Package prefix to include in the analysis. If C, empty string.
	 * 11: Path to directory containing auxiliary artifacts.
	 */
	public static void main(String[] args)
			throws IOException, DistributionSizeMismatchException,
			UnmatchingDocTopicItemsException {
		ClusteringAlgoArguments parsedArguments =
			new ClusteringAlgoArguments(args);

		run(parsedArguments);
	}

	public static Architecture run(ClusteringAlgoArguments parsedArguments)
			throws IOException, DistributionSizeMismatchException,
			UnmatchingDocTopicItemsException {
		return run(
			parsedArguments.algorithm,
			parsedArguments.arch,
			parsedArguments.serialCrit,
			parsedArguments.stopCrit,
			parsedArguments.language,
			parsedArguments.stoppingCriterion,
			parsedArguments.simMeasure);
	}

	public static Architecture run(ClusteringAlgorithmType algorithm,
			Architecture arch, SerializationCriterion serialCrit,
			StoppingCriterion stopCrit, String language, String stoppingCriterionName,
			SimMeasure.SimMeasureType simMeasure)
			throws IOException, DistributionSizeMismatchException,
			UnmatchingDocTopicItemsException {
		// Create the runner object
		Clusterer runner = new Clusterer(language, serialCrit, arch, algorithm);
		// Compute the clustering algorithm and return the resulting architecture
		runner.computeArchitecture(stopCrit, stoppingCriterionName,	simMeasure);
		// Compute DTI word bags if concern-based technique is used
		if (runner.architecture instanceof ConcernArchitecture)
			((ConcernArchitecture) runner.architecture).computeConcernWordBags();

		return runner.architecture;
	}

	public static class ClusteringAlgoArguments {
		public final ClusteringAlgorithmType algorithm;
		public final String language;
		public final Architecture arch;
		public final SerializationCriterion serialCrit;
		public final StoppingCriterion stopCrit;
		public final String stoppingCriterion;
		public final SimMeasure.SimMeasureType simMeasure;

		public ClusteringAlgoArguments(String[] args)
				throws IOException, UnmatchingDocTopicItemsException {
			this.algorithm = ClusteringAlgorithmType.valueOf(args[0].toUpperCase());
			this.language = args[1];
			this.simMeasure =
				SimMeasure.SimMeasureType.valueOf(args[5].toUpperCase());
			if (args.length > 11)
				this.arch = new ConcernArchitecture(args[8], args[9], this.simMeasure,
					FeatureVectors.deserializeFFVectors(args[2]),	this.language, args[11],
					args[10]);
			else
				this.arch = new Architecture(args[8], args[9], this.simMeasure,
					FeatureVectors.deserializeFFVectors(args[2]),	this.language, args[10]);
			this.serialCrit = SerializationCriterion.makeSerializationCriterion(
				args[6], Double.parseDouble(args[7]), arch);
			this.stopCrit = StoppingCriterion.makeStoppingCriterion(
				args[3], Double.parseDouble(args[4]), arch);
			this.stoppingCriterion = args[3];
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
	protected final ClusteringAlgorithmType algorithm;
	//endregion ATTRIBUTES

	//region CONTRUCTORS
	protected Clusterer(String language,
			SerializationCriterion serializationCriterion, Architecture arch,
			ClusteringAlgorithmType algorithm) {
		this.language = language;
		this.serializationCriterion = serializationCriterion;
		this.architecture = arch;
		this.algorithm = algorithm;
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

	public Architecture computeArchitecture(StoppingCriterion stopCriterion,
		String stoppingCriterion, SimMeasure.SimMeasureType simMeasure)
		throws DistributionSizeMismatchException, FileNotFoundException,
		UnmatchingDocTopicItemsException {
		SimilarityMatrix simMatrix = new SimilarityMatrix(
			simMeasure, this.architecture);

		while (stopCriterion.notReadyToStop(this.architecture)) {
			if (stoppingCriterion.equalsIgnoreCase("clustergain"))
				checkAndUpdateClusterGain(architecture.computeClusterGain(algorithm));

			SimData data = identifyMostSimClusters(simMatrix);
			Cluster newCluster = new Cluster(this.algorithm,
				data.c1, data.c2);

			updateFastClustersAndSimMatrixToReflectMergedCluster(
				data, newCluster, simMatrix);

			if (this.serializationCriterion != null
					&& this.serializationCriterion.shouldSerialize())
				this.architecture.writeToRsf();
		}

		return this.architecture;
	}
}
