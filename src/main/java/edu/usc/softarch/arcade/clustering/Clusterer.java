package edu.usc.softarch.arcade.clustering;

import java.io.IOException;
import java.util.Map;

import edu.usc.softarch.arcade.clustering.simmeasures.SimData;
import edu.usc.softarch.arcade.clustering.simmeasures.SimMeasure;
import edu.usc.softarch.arcade.clustering.simmeasures.SimilarityMatrix;
import edu.usc.softarch.arcade.clustering.criteria.SerializationCriterion;
import edu.usc.softarch.arcade.clustering.criteria.StoppingCriterion;
import edu.usc.softarch.arcade.facts.DependencyGraph;
import edu.usc.softarch.arcade.topics.DocTopics;
import edu.usc.softarch.arcade.topics.exceptions.UnmatchingDocTopicItemsException;
import edu.usc.softarch.arcade.util.CLI;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;

public class Clusterer {
	//region PUBLIC INTERFACE
	/**
	 * Primary entry point for all clustering algorithms. Arguments are:
	 *
	 * algo : Clustering algorithm.
	 * language : Subject system language.
	 * deps : Path to feature vectors file.
	 * stop : Stopping criterion.
	 * stopthreshold : Stopping criterion parameter.
	 * measure : Similarity measure desired. Check compatibility in each algorithm.
	 * serial : Serialization criterion.
	 * serialthreshold : Parameter value of the serialization criterion.
	 * projname : Name of the subject system.
	 * projversion : Version of the subject system.
	 * projpath: Path to place the output.
	 * packageprefix: Package prefix to include in the analysis. If C, empty string.
	 * artifacts: Path to directory containing auxiliary artifacts.
	 * reassignversion: Reassign DocTopics
	 */
	public static void main(String[] args)
			throws IOException, UnmatchingDocTopicItemsException,
			SAXException, ParserConfigurationException {
		ClusteringAlgoArguments parsedArguments =
			new ClusteringAlgoArguments(CLI.parseArguments(args));
		run(parsedArguments);
	}

	public static class ClusteringAlgoArguments {
		public final ClusteringAlgorithmType algorithm;
		public final Architecture arch;
		public final SerializationCriterion serialCrit;
		public final StoppingCriterion stopCrit;
		public final String stoppingCriterion;
		public final SimMeasure.SimMeasureType simMeasure;

		public ClusteringAlgoArguments(Map<String, String> args)
				throws IOException, UnmatchingDocTopicItemsException,
				ParserConfigurationException, SAXException {
			this.algorithm =
				ClusteringAlgorithmType.valueOf(args.get("algo").toUpperCase());
			this.simMeasure =
				SimMeasure.SimMeasureType.valueOf(args.get("measure").toUpperCase());

			FeatureVectors vectors;
			String depsPath = args.get("deps");
			if (depsPath.contains(".rsf"))
				vectors = new FeatureVectors(DependencyGraph.readRsf(depsPath));
			else if (depsPath.contains(".odem"))
				vectors = new FeatureVectors(DependencyGraph.readOdem(depsPath));
			else if (depsPath.contains(".json"))
				vectors = FeatureVectors.deserializeFFVectors(depsPath);
			else
				throw new IOException("Unrecognized dependency file type: " + depsPath);

			String projname = args.get("projname");
			String projversion = args.get("projversion");
			String projpath = args.get("projpath");
			String language = args.get("language");
			String packagePrefix = "";
			if (language.equalsIgnoreCase("java"))
				packagePrefix = args.get("packageprefix");

			if (this.algorithm == ClusteringAlgorithmType.ARC) {
				String artifacts = args.get("artifacts");
				boolean reassignVersion = false;
				if (args.containsKey("reassignversion"))
					reassignVersion = Boolean.parseBoolean(args.get("reassignversion"));

				this.arch = new Architecture(projname, projversion, projpath,
					this.simMeasure, vectors, language, artifacts, packagePrefix,
					reassignVersion);
			} else {
				this.arch = new Architecture(projname, projversion, projpath,
					this.simMeasure, vectors, language, packagePrefix);
			}

			String serial = "archsize";
			double serialThreshold = 50.0;

			if (args.containsKey("serial")) {
				serial = args.get("serial");
				serialThreshold = Double.parseDouble("serialthreshold");
			}

			this.serialCrit = SerializationCriterion.makeSerializationCriterion(
				serial, serialThreshold, arch);

			String stop = "preselected";
			double stopThreshold = 50.0;

			if (args.containsKey("stop")) {
				stop = args.get("stop");
				stopThreshold = Double.parseDouble(args.get("stopthreshold"));
			}

			this.stopCrit = StoppingCriterion.makeStoppingCriterion(
				stop, stopThreshold, arch);
			this.stoppingCriterion = stop;
		}
	}

	public static Architecture run(ClusteringAlgoArguments parsedArguments)
			throws IOException, UnmatchingDocTopicItemsException {
		return run(
			parsedArguments.algorithm,
			parsedArguments.arch,
			parsedArguments.serialCrit,
			parsedArguments.stopCrit,
			parsedArguments.simMeasure);
	}

	public static Architecture run(ClusteringAlgorithmType algorithm,
			Architecture arch, SerializationCriterion serialCrit,
			StoppingCriterion stopCrit, SimMeasure.SimMeasureType simMeasure)
			throws IOException, UnmatchingDocTopicItemsException {
		Clusterer runner = new Clusterer(serialCrit, arch, algorithm,
			simMeasure, stopCrit);
		runner.computeArchitecture(stopCrit);

		return runner.architecture;
	}
	//endregion

	//region ATTRIBUTES
	private final Architecture architecture;
	public static int numberOfEntitiesToBeClustered = 0;
	private final SerializationCriterion serializationCriterion;
	public final ClusteringAlgorithmType algorithm;
	private final SimilarityMatrix simMatrix;
	public final StoppingCriterion stopCrit;
	//endregion ATTRIBUTES

	//region CONTRUCTORS
	public Clusterer(SerializationCriterion serializationCriterion,
			Architecture arch, ClusteringAlgorithmType algorithm,
			SimMeasure.SimMeasureType simMeasure, StoppingCriterion stopCrit) {
		this.serializationCriterion = serializationCriterion;
		this.architecture = arch;
		this.algorithm = algorithm;
		this.simMatrix = new SimilarityMatrix(simMeasure, this.architecture);
		this.stopCrit = stopCrit;
	}
	//endregion

	//region ACCESSORS
	public Architecture getArchitecture() { return architecture; }

	private void addCluster(Cluster cluster) {
		architecture.put(cluster.name, cluster); }
	private void removeCluster(Cluster cluster) {
		architecture.remove(cluster.name);	}
	//endregion

	//region PROCESSING
	public Architecture computeArchitecture(StoppingCriterion stopCriterion)
			throws IOException, UnmatchingDocTopicItemsException {
		while (stopCriterion.notReadyToStop(this.architecture)) {
			doClusteringStep();
			doSerializationStep();
		}

		return this.architecture;
	}

	public void doClusteringStep() throws UnmatchingDocTopicItemsException {
		SimData data = identifyMostSimClusters();
		Cluster newCluster = new Cluster(this.algorithm, data.c1, data.c2,
			this.architecture.projectName, this.architecture.projectVersion);

		updateFastClustersAndSimMatrixToReflectMergedCluster(
			data, newCluster, simMatrix);
	}

	public void doSerializationStep() throws IOException {
		if (this.serializationCriterion != null
				&& this.serializationCriterion.shouldSerialize()) {
			serialize();
		}
	}

	/**
	 * Looks for the smallest non-diagonal value in the matrix, which represents
	 * the pair of clusters with the lowest level of divergence (highest
	 * similarity).
	 *
	 * @return The maximum-similarity cell.
	 */
	public SimData identifyMostSimClusters() {
		if (this.simMatrix.size() != this.architecture.size())
			throw new IllegalArgumentException("expected simMatrix.size():"
				+ this.simMatrix.size() + " to be fastClusters.size(): "
				+ this.architecture.size());

		for (Map<Cluster, SimData> col : this.simMatrix.getColumns())
			if (col.size() != this.architecture.size())
				throw new IllegalArgumentException("expected col.size():" + col.size()
					+ " to be fastClusters.size(): " + this.architecture.size());

		return this.simMatrix.getMinCell();
	}

	private void updateFastClustersAndSimMatrixToReflectMergedCluster(
			SimData data, Cluster newCluster,	SimilarityMatrix simMatrix) {
		// Sanity check
		if (data.c1.name.equals(data.c2.name))
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
	//endregion

	//region SERIALIZATION
	public void serialize() throws IOException {
		this.architecture.writeToRsf();
		// Compute DTI word bags if concern-based technique is used
		if (DocTopics.isReady(
				this.architecture.projectName, this.architecture.projectVersion)) {
			this.architecture.serializeBagOfWords();
			this.architecture.serializeDocTopics();
		}
	}
	//endregion
}
