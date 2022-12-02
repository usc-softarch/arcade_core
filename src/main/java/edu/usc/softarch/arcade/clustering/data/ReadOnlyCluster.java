package edu.usc.softarch.arcade.clustering.data;

import edu.usc.softarch.arcade.clustering.ClusteringAlgorithmType;
import edu.usc.softarch.arcade.metrics.ClusterMetrics;
import edu.usc.softarch.arcade.topics.Concern;
import edu.usc.softarch.arcade.topics.DocTopicItem;
import edu.usc.softarch.arcade.topics.DocTopics;
import edu.usc.softarch.arcade.topics.exceptions.UnmatchingDocTopicItemsException;
import edu.usc.softarch.util.EnhancedSet;
import edu.usc.softarch.util.EnhancedTreeSet;
import edu.usc.softarch.util.LabeledEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Cluster object which cannot be used for further clustering, i.e. is already
 * the result of a clustering technique. Used for post-processing components.
 */
public class ReadOnlyCluster {
	//region ATTRIBUTES
	/**
	 * Name of the Cluster, typically given by the union of the names of its
	 * comprising entities. Can also be a related, representative name of all
	 * entities, or an integer index.
	 */
	public final String name;
	/**
	 * Set of code-level entities contained by this cluster, typically classes
	 * or files.
	 */
	private final EnhancedSet<String> entities;
	/**
	 * {@link DocTopicItem} related to this Cluster, if one exists.
	 */
	private DocTopicItem dti;
	//endregion

	//region CONSTRUCTORS
	/**
	 * Base constructor, used to initialize a new {@link Cluster} or read a
	 * ReadOnlyCluster in iteratively.
	 *
	 * @param name Name of the new ReadOnlyCluster.
	 */
	public ReadOnlyCluster(String name) {
		this.name = name;
		this.entities = new EnhancedTreeSet<>();
	}

	/**
	 * Full constructor for structural recovery techniques, used in cloning
	 * operations.
	 *
	 * @param name Name of the new ReadOnlyCluster.
	 * @param entities Code-level entities to be added.
	 */
	public ReadOnlyCluster(String name, Collection<String> entities) {
		this(name);
		this.entities.addAll(entities);
	}

	/**
	 * Full constructor for concern-based recovery techniques, used in cloning
	 * operations.
	 *
	 * @param name Name of the new ReadOnlyCluster.
	 * @param entities Code-level entities to be added.
	 * @param dti DocTopicItem relative to the new ReadOnlyCluster.
	 */
	public ReadOnlyCluster(String name, Collection<String> entities,
			DocTopicItem dti) {
		this(name, entities);
		this.dti = dti;
	}

	/**
	 * Clone constructor.
	 *
	 * @param c ReadOnlyCluster to clone.
	 */
	public ReadOnlyCluster(ReadOnlyCluster c) {
		this(c.name, c.entities, c.dti); }

	/**
	 * Merge constructor.
	 *
	 * @param cat Clustering algorithm used in the recovery.
	 * @param c1 First cluster of the merge.
	 * @param c2 Second cluster of the merge.
	 * @param projectName Name of the project being recovered.
	 * @param projectVersion Version of the project being recovered.
	 * @throws UnmatchingDocTopicItemsException Thrown if the
	 * 				 {@link DocTopicItem}s of the merged {@link Cluster}s are of
	 * 				 different size. This should never happen during normal operation.
	 */
	protected ReadOnlyCluster(ClusteringAlgorithmType cat, Cluster c1, Cluster c2,
			String projectName, String projectVersion)
			throws UnmatchingDocTopicItemsException {
		this.entities = new EnhancedTreeSet<>(c2.getEntities());

		if (cat.equals(ClusteringAlgorithmType.ARC) && c1.name.contains("$"))
			this.name = c2.name;
		else {
			this.name = c1.name + ',' + c2.name;
			this.entities.addAll(c1.getEntities());
		}

		if (cat.equals(ClusteringAlgorithmType.ARC))
			this.dti = DocTopics.getSingleton(projectName, projectVersion)
				.mergeDocTopicItems(c1, c2, name);
	}
	//endregion

	//region ACCESSORS
	/**
	 * Creates an immutable clone of this cluster's entities.
	 *
	 * @return The immutable clone.
	 */
	public Collection<String> getEntities() {
		return Collections.unmodifiableCollection(entities); }

	/**
	 * Adds an entity to this cluster.
	 *
	 * @param entity The entity to be added.
	 */
	public void addEntity(String entity) { this.entities.add(entity); }

	/**
	 * Removes a set of entities from this cluster.
	 *
	 * @param entities The entities to remove.
	 */
	public void removeEntities(Set<String> entities) {
		this.entities.removeAll(entities); }

	/**
	 * Counts the number of entities in this cluster.
	 *
	 * @return The count of entities.
	 */
	public int size() { return this.entities.size(); }

	//TODO Should return a copy, but DTIs are a mess.
	/**
	 * Returns this cluster's {@link DocTopicItem}.
	 */
	public DocTopicItem getDocTopicItem() {
		if (hasDocTopicItem())
			return this.dti;
		return null;
	}

	/**
	 * Checks whether this cluster's {@link DocTopicItem} is null.
	 *
	 * @return False if {@link DocTopicItem} is null, true otherwise.
	 */
	public boolean hasDocTopicItem() { return this.dti != null; }

	/**
	 * Sets this cluster's {@link DocTopicItem}.
	 */
	public void setDocTopicItem(DocTopicItem dti) { this.dti = dti; }

	/**
	 * Returns the union of the entities of two clusters.
	 *
	 * @param c The cluster with which to perform union.
	 * @return The union of the entities.
	 */
	public Set<String> union(ReadOnlyCluster c) {
		return this.entities.union(c.entities); }

	/**
	 * Returns the intersection of the entities of two clusters.
	 *
	 * @param c The cluster with which to perform intersection.
	 * @return The intersection of the entities.
	 */
	public Set<String> intersection(ReadOnlyCluster c) {
		return this.entities.intersection(c.entities); }

	public String[] intersectionArray(ReadOnlyCluster c) {
		Object[] objArray = this.entities.intersectionArray(c.entities);
		String[] result = new String[objArray.length];
		for (int i = 0; i < objArray.length; i++)
			result[i] = (String) objArray[i];

		return result;
	}

	public int intersectionSize(ReadOnlyCluster c) {
		return this.entities.intersectionSize(c.entities); }

	/**
	 * Returns the difference of the entities of two clusters.
	 *
	 * @param c The cluster with which to perform difference.
	 * @return The difference of the entities.
	 */
	public Set<String> difference(ReadOnlyCluster c) {
		return this.entities.difference(c.entities); }

	/**
	 * Returns the symmetric difference of the entities of two clusters.
	 *
	 * @param c The cluster with which to perform symmetric difference.
	 * @return The symmetric difference of the entities.
	 */
	public Set<String> symmetricDifference(ReadOnlyCluster c) {
		return this.entities.symmetricDifference(c.entities); }

	public String[] symmetricDifferenceArray(ReadOnlyCluster c) {
		Object[] objArray = this.entities.symmetricDifferenceArray(c.entities);
		String[] result = new String[objArray.length];
		for (int i = 0; i < objArray.length; i++)
			result[i] = (String) objArray[i];

		return result;
	}

	public int symmetricDifferenceSize(ReadOnlyCluster c) {
		return this.entities.symmetricDifferenceSize(c.entities); }
	//endregion

	//region PROCESSING
	/**
	 * Computes the concern of this cluster based on the provided bag of words.
	 * This concern will include up to 100 words, these being the top N words of
	 * each topic where N is the proportion of that topic in this cluster's topic
	 * distribution.
	 *
	 * @param wordBags Words relating to each topic.
	 * @return The {@link Concern} of this cluster.
	 */
	public Concern computeConcern(Map<Integer, List<String>> wordBags) {
		return this.dti.computeConcern(wordBags); }
	//endregion

	//region OBJECT METHODS
	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o == this) return true;
		if (!(o instanceof ReadOnlyCluster)) return false;

		ReadOnlyCluster toCompare = (ReadOnlyCluster) o;

		return this.name.equals(toCompare.name);
	}

	@Override
	public int hashCode() { return this.name.hashCode(); }
	//endregion

	//region SERIALIZATION
	/**
	 * Serializes this cluster into a Graphviz DOT format.
	 *
	 * @param graph Graph of all the code entity-level dependencies in the
	 *              architecture to which this cluster belongs.
	 * @param arch The architecture to which this cluster belongs.
	 * @param outputPath File path in which to serialize.
	 * @throws IOException Thrown if there are any errors in accessing the
	 * 				 specified output file.
	 */
	public void writeToDot(SimpleDirectedGraph<String, LabeledEdge> graph,
			ReadOnlyArchitecture arch, String outputPath) throws IOException {
		writeToDot(graph, arch, outputPath, this.name);
	}

	/**
	 * Serializes this cluster into a Graphviz DOT format.
	 *
	 * @param graph Graph of all the code entity-level dependencies in the
	 *              architecture to which this cluster belongs.
	 * @param arch The architecture to which this cluster belongs.
	 * @param outputPath File path in which to serialize.
	 * @param name Name to be given to this cluster, if different from the
	 *             cluster's {@link #name}.
	 * @throws IOException Thrown if there are any errors in accessing the
	 * 				 specified output file.
	 */
	public void writeToDot(SimpleDirectedGraph<String, LabeledEdge> graph,
			ReadOnlyArchitecture arch, String outputPath, String name)
			throws IOException {
		try (FileWriter writer = new FileWriter(outputPath)) {
			writer.write("digraph \"" + name + "\" {\n");

			this.writeToDot(graph, arch, writer, name);

			writer.write("}\n");
		}
	}

	/**
	 * Inputs this cluster as a subgraph to an open DOT {@link FileWriter}.
	 *
	 * @param graph Graph of all the code entity-level dependencies in the
	 *              architecture to which this cluster belongs.
	 * @param arch The architecture to which this cluster belongs.
	 * @param writer {@link FileWriter} into which to write the cluster subgraph.
	 * @throws IOException Thrown if there are any errors in accessing the
	 * 				 {@link FileWriter}'s output file.
	 */
	public void writeToDot(SimpleDirectedGraph<String, LabeledEdge> graph,
			ReadOnlyArchitecture arch, FileWriter writer) throws IOException {
		writeToDot(graph, arch, writer, this.name);
	}

	/**
	 * Inputs this cluster as a subgraph to an open DOT {@link FileWriter}.
	 *
	 * @param graph Graph of all the code entity-level dependencies in the
	 *              architecture to which this cluster belongs.
	 * @param arch The architecture to which this cluster belongs.
	 * @param writer {@link FileWriter} into which to write the cluster subgraph.
	 * @param name Name to be given to this cluster, if different from the
	 *             cluster's {@link #name}.
	 * @throws IOException Thrown if there are any errors in accessing the
	 * 				 {@link FileWriter}'s output file.
	 */
	private void writeToDot(SimpleDirectedGraph<String, LabeledEdge> graph,
			ReadOnlyArchitecture arch, FileWriter writer, String name)
			throws IOException {
		Set<LabeledEdge> clusterEdges = new HashSet<>();

		writer.write("\tsubgraph \"cluster_" + name + "\" {\n");
		writer.write("\t\tnode [style=filled];\n");

		for (String entity : this.getEntities()) {
			writer.write(
				"\t\t\"" + entity.replace("\\", ".") + "\";\n");
			clusterEdges.addAll(graph.edgesOf(entity).stream()
				.filter(e -> e.label.equals("internal"))
				.collect(Collectors.toSet()));
		}

		for (LabeledEdge edge : clusterEdges) {
			String source =
				graph.getEdgeSource(edge).replace("\\", ".");
			String target =
				graph.getEdgeTarget(edge).replace("\\", ".");
			writer.write("\t\t\"" + source + "\" -> \"" + target + "\";\n");
		}

		ClusterMetrics metrics = new ClusterMetrics(this, arch, graph);
		DecimalFormat formatter = new DecimalFormat("#.####");

		writer.write("\t\tlabel = \"Cluster: " + name
			+ "\\nIntra-connectivity: " + formatter.format(metrics.intraConnectivity)
			+ "\\nInter-connectivity: "
				+ formatter.format(metrics.interConnectivity.getMean()) + ", "
				+ formatter.format(metrics.interConnectivity.getMedian())
				+ ", " + formatter.format(metrics.interConnectivity.getMin()) + ", "
				+ formatter.format(metrics.interConnectivity.getMax()) + ", "
				+ formatter.format(metrics.interConnectivity.getStDev())
			+ "\\nBasicMQ: " + formatter.format(metrics.basicMq)
			+ "\\nCluster Factor: " + formatter.format(metrics.clusterFactor)
			+ "\\nFan-in: " + formatter.format(metrics.fanIn)
			+ "\\nFan-out: " + formatter.format(metrics.fanOut)
			+ "\\nInstability: " + formatter.format(metrics.instability)
			+ "\";\n");
		writer.write("\t}\n");
	}
	//endregion
}
