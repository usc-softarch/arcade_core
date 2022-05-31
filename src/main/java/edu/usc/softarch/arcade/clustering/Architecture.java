package edu.usc.softarch.arcade.clustering;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import edu.usc.softarch.arcade.config.Config;
import edu.usc.softarch.arcade.topics.DistributionSizeMismatchException;
import edu.usc.softarch.arcade.topics.DocTopicItem;
import edu.usc.softarch.arcade.topics.DocTopics;

/**
 * Represents an architecture of clustered entities. An Architecture is a
 * specialization of a {@link TreeMap}&lt;{@link String}, {@link Cluster}&gt;,
 * meaning it is ordered by the names of its comprising {@link Cluster}s. This
 * ensures that all iterative operations done over an Architecture will follow
 * the same order, regardless of the order in which the Architecture was
 * constructed, i.e. the order of the clustering algorithm's inputs.
 */
public class Architecture extends TreeMap<String, Cluster> {
	//region ATTRIBUTES
	private static final long serialVersionUID = 1L;
	/**
	 * The name of the subject system represented by this architecture. Used in
	 * serialization.
	 */
	public final String projectName;
	/**
	 * The path to where this data structure should be serialized.
	 */
	private final String projectPath;
	/**
	 * The total number of features that can exist in any clusters of this
	 * architecture. For architectures constructed from structural data such as
	 * dependencies, this is the number of entities in the initial architecture,
	 * e.g. the number of files when using file-level granularity, or the number
	 * of functions when using function-level granularity.
	 */
	private final int numFeatures;

	protected final String language;
	//endregion

	//region CONSTRUCTORS
	/**
	 * Clone constructor.
	 */
  public Architecture(Architecture arch) {
		for (Cluster c : arch.values())
			this.add(new Cluster(c));

		this.projectName = arch.projectName;
		this.projectPath = arch.projectPath;
		this.numFeatures = arch.numFeatures;
		this.language = arch.language;
	}

	/**
	 * Default constructor for new Architecture objects.
	 *
	 * @param projectName The name of the subject system represented by this
	 *                    architecture. Used in serialization.
	 * @param projectPath The path to where this data structure should be
	 *                    serialized.
	 * @param vectors The {@link FeatureVectors} object to construct this
	 *                Architecture from.
	 * @param language The language of the subject system.
	 * @param packagePrefix The package prefix to be considered in the
	 *                      initialization of the Architecture. Only used in
	 *                      Java systems.
	 */
	public Architecture(String projectName, String projectPath,
			FeatureVectors vectors, String language, String packagePrefix) {
		this.projectName = projectName;
		this.projectPath = projectPath;
		this.numFeatures = vectors.getNamesInFeatureSet().size();
		this.language = language;
		initializeClusters(vectors, language, packagePrefix);
	}

	/**
	 * Convenience constructor for new Architecture objects in C-based systems.
	 * Package prefix is set to empty string.
	 *
	 * @param projectName The name of the subject system represented by this
	 *                    architecture. Used in serialization.
	 * @param projectPath The path to where this data structure should be
	 *                    serialized.
	 * @param vectors The {@link FeatureVectors} object to construct this
	 *                Architecture from.
	 * @param language The language of the subject system.
	 */
	public Architecture(String projectName, String projectPath,
			FeatureVectors vectors, String language) {
		this(projectName, projectPath, vectors, language, "");
	}

	/**
	 * Initializes the clusters of this architecture from a {@link FeatureVectors}
	 * object.
	 *
	 * @param vectors The {@link FeatureVectors} to use for initialization.
	 * @param language The language of the subject system.
	 * @param packagePrefix The package prefix to be considered in the
	 *                      initialization of the Architecture. Only used in
	 *                      Java systems.
	 */
	protected void initializeClusters(FeatureVectors vectors, String language,
			String packagePrefix) {
		// For each cell in the adjacency matrix
		for (String name : vectors.getFeatureVectorNames()) {
			// Get the vector relative to that cell
			BitSet featureSet = vectors.getNameToFeatureSetMap().get(name);
			// Create a cluster containing only that cell
			Cluster cluster = new Cluster(name, featureSet,
				vectors.getNamesInFeatureSet().size());

			// Add the cluster except extraordinary circumstances (assume always)
			addClusterConditionally(cluster, language, packagePrefix);
		}

		Clusterer.numberOfEntitiesToBeClustered = this.size();
	}

	/**
	 * Adds a cluster if and only if it satisfies a set of conditions.
	 *
	 * For systems in C-based languages, it checks that only entities from
	 * C-based files are accepted.
	 *
	 * For clustering analyses based on Function-level granularity, it ensures
	 * that no compiler-generated functions are accepted.
	 *
	 * For clustering of Java-based subject systems, it ensures that only entities
	 * within the given package prefix are accepted.
	 *
	 * @param cluster The {@link Cluster} to tentatively add to this architecture.
	 * @param language The language of the subject system.
	 * @param packagePrefix The package prefix to be considered. Only used in
	 *                      Java systems.
	 */
	private void addClusterConditionally(Cluster cluster, String language,
			String packagePrefix) {
		// If the source language is C or C++, add the only C-based entities
		if (language.equalsIgnoreCase("c")) {
			Pattern p = Pattern.compile("\\.(c|cpp|cc|s|h|hpp|icc|ia|tbl|p)$");
			// First condition to be assumed true
			// Second condition to be assumed true
			// Third condition checks whether the cluster is based on a valid C entity
			if (Config.getClusteringGranule().equals(Config.Granule.file) &&
				!cluster.getName().startsWith("/") &&
				p.matcher(cluster.getName()).find())
				this.put(cluster.getName(), cluster);
		}

		// This block is used only for certain older modules, disregard
		if (Config.getClusteringGranule().equals(Config.Granule.func)) {
			if (cluster.getName().equals("\"##\""))
				return;
			this.put(cluster.getName(), cluster);
		}

		// If the source language is Java, add all clusters that match prefix
		if (language.equalsIgnoreCase("java")
			&& (packagePrefix.isEmpty()
			|| cluster.getName().startsWith(packagePrefix)))
			this.put(cluster.getName(), cluster);
	}
	//endregion

	//region ACCESSORS
	/**
	 * Returns the total number of features present in this architecture.
	 */
	public int getNumFeatures() { return this.numFeatures; }

	/**
	 * Checks whether there exist any {@link Cluster} in this architecture with
	 * size == 1, that is, a {@link Cluster} with only 1 entity. Useful for
	 * some stopping criteria, as it indicates a {@link Cluster} which has not
	 * yet been considered by the clustering process.
	 *
	 * @return True if any {@link Cluster} has only 1 entity, False otherwise.
	 */
	public boolean hasOrphans() {
		for (Cluster c : this.values()) {
			if (c.getNumEntities() == 1)
				return true;
		}
		return false;
	}

	/**
	 * Adds the given {@link Cluster} to this architecture.
	 */
	public void add(Cluster c) {
		this.put(c.getName(), c);
	}

	/**
	 * Removes the given {@link Cluster}s from this architecture. Primarily used
	 * in pre-processing phases to remove unwanted entities from the analysis.
	 *
	 * @param clusters A map where the keys are the names of the {@link Cluster}s
	 *                 to be removed.
	 */
	public void removeAll(Map<String, Cluster> clusters) {
		for (String key : clusters.keySet())
			this.remove(key);
	}
	//endregion

	//region PROCESSING
	public double computeClusterGain(ClusteringAlgorithmType algorithm) {
		switch(algorithm) {
			case WCA:
			case LIMBO:
				return computeStructuralClusterGain();
			case ARC:
				return computeTopicClusterGain();
			default:
				throw new IllegalArgumentException();
		}
	}

	/**
	 * TODO
	 * @return
	 */
  public double computeStructuralClusterGain() {
		List<Double> clusterCentroids = new ArrayList<>();

		for (Cluster cluster : this.values()) {
			double centroid = cluster.computeStructuralCentroid(this.numFeatures);
			clusterCentroids.add(centroid);
		}

		double globalCentroid = computeStructuralGlobalCentroid(clusterCentroids);

		double clusterGain = 0;
		for (int i = 0; i < clusterCentroids.size(); i++)
			clusterGain += (((Cluster) this.values().toArray()[i]).getNumEntities() - 1)	* Math.pow(
        Math.abs(globalCentroid - clusterCentroids.get(i)), 2);

		return clusterGain;
	}

	/**
	 * TODO
	 * @param clusterCentroids
	 * @return
	 */
  private double computeStructuralGlobalCentroid(
					List<Double> clusterCentroids) {
		double centroidSum = 0;

		for (Double centroid : clusterCentroids)
			centroidSum += centroid;

		return centroidSum / clusterCentroids.size();
	}

	/**
	 * TODO
	 * @return
	 */
  public double computeTopicClusterGain() {
		List<DocTopicItem> docTopicItems = new ArrayList<>();
		for (Cluster c : this.values())
			docTopicItems.add(c.getDocTopicItem());
		DocTopicItem globalDocTopicItem =
      DocTopics.computeGlobalCentroidUsingTopics(docTopicItems);

		double clusterGain = 0;

		for (int i = 0; i < docTopicItems.size(); i++) {
			try {
				clusterGain += (((Cluster) this.values().toArray()[i]).getNumEntities() - 1)
					* docTopicItems.get(i).getJsDivergence(globalDocTopicItem);
			} catch (DistributionSizeMismatchException e) {
				e.printStackTrace(); //TODO handle it
			}
		}

		return clusterGain;
	}
	//endregion

	//region SERIALIZATION
	/**
	 * Writes an RSF file representing this architecture. Uses this object's
	 * {@link #projectPath} as the output location and its {@link #projectName}
	 * to determine the name of the output file.
	 */
	public void writeToRsf() throws FileNotFoundException {
		String fs = File.separator;
		String path = this.projectPath + fs + this.projectName + "_"
			+ this.size() + "_clusters.rsf";
		this.writeToRsf(path);
	}

	/**
	 * Writes an RSF file representing this architecture to the provided path.
	 *
	 * @param path A {@link String} pointing to the output file path, including
	 *             the desired output filename.
	 */
	public void writeToRsf(String path) throws FileNotFoundException {
		File rsfFile = new File(path);
		rsfFile.getParentFile().mkdirs();

		Map<Integer, String> architectureIndex = computeArchitectureIndex();

		try (PrintWriter out = new PrintWriter(
			new OutputStreamWriter(
				new FileOutputStream(rsfFile), StandardCharsets.UTF_8))) {
			for (Map.Entry<Integer, String> cluster : architectureIndex.entrySet()) {
				Integer clusterIndex = cluster.getKey();
				String[] entities = cluster.getValue().split(",");
				Set<String> entitiesSet = new HashSet<>(Arrays.asList(entities));
				for (String entity : entitiesSet) {
					out.println("contain " + clusterIndex + " " + entity);
				}
			}
		}
	}

	/**
	 * Returns a map indexing the {@link Cluster}s of this architecture. Used in
	 * serialization: as a {@link Cluster}'s name typically represents the names
	 * of its comprising entities, a number is assigned to the {@link Cluster} as
	 * a unique identifier. The names of the entities are broken down into
	 * separate entries by the serialization method.
	 */
	private Map<Integer, String> computeArchitectureIndex() {
		List<String> orderedClusterNames = this.values().stream()
			.map(Cluster::getName).sorted().collect(Collectors.toList());

		Map<Integer, String> architectureIndex = new TreeMap<>();
		for (int i = 0; i < this.size(); i++)
			architectureIndex.put(i, orderedClusterNames.get(i));

		return architectureIndex;
	}
	//endregion
}
