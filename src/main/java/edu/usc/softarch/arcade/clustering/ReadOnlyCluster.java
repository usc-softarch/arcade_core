package edu.usc.softarch.arcade.clustering;

import edu.usc.softarch.arcade.metrics.decay.ArchitecturalStability;
import edu.usc.softarch.arcade.metrics.decay.InterConnectivity;
import edu.usc.softarch.arcade.metrics.decay.IntraConnectivity;
import edu.usc.softarch.arcade.metrics.decay.TurboMQ;
import edu.usc.softarch.arcade.topics.Concern;
import edu.usc.softarch.arcade.topics.DocTopicItem;
import edu.usc.softarch.arcade.topics.DocTopics;
import edu.usc.softarch.arcade.topics.exceptions.UnmatchingDocTopicItemsException;
import edu.usc.softarch.util.EnhancedHashSet;
import edu.usc.softarch.util.EnhancedSet;
import edu.usc.softarch.util.LabeledEdge;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.jgrapht.graph.SimpleDirectedGraph;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Collection;
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
	 * entities.
	 */
	public final String name;
	/**
	 * Set of code-level entities contained by this cluster.
	 */
	protected final EnhancedSet<String> entities;
	/**
	 * {@link DocTopicItem} related to this Cluster, if one exists.
	 */
	private DocTopicItem dti;
	//endregion

	//region CONSTRUCTORS
	public ReadOnlyCluster(String name) {
		this.name = name;
		this.entities = new EnhancedHashSet<>();
	}

	public ReadOnlyCluster(String name, Collection<String> entities) {
		this.name = name;
		this.entities = new EnhancedHashSet<>(entities);
	}

	protected ReadOnlyCluster(String name, Collection<String> entities,
			DocTopicItem dti) {
		this(name, entities);
		this.dti = dti;
	}

	public ReadOnlyCluster(ReadOnlyCluster c) {
		this.name = c.name;
		this.entities = new EnhancedHashSet<>(c.entities);
		this.dti = c.dti;
	}

	protected ReadOnlyCluster(ClusteringAlgorithmType cat, Cluster c1, Cluster c2,
			String projectName, String projectVersion)
			throws UnmatchingDocTopicItemsException {
		this.entities = new EnhancedHashSet<>(c2.getEntities());

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
	public Collection<String> getEntities() { return new HashSet<>(entities); }
	void addEntity(String entity) { this.entities.add(entity); }
	public void removeEntities(Set<String> entities) {
		this.entities.removeAll(entities); }
	public int size() { return this.entities.size(); }

	/**
	 * Returns a copy of this Cluster's {@link DocTopicItem}.
	 */
	public DocTopicItem getDocTopicItem() {
		if (hasDocTopicItem())
			return this.dti;
		return null;
	}

	/**
	 * Checks whether this Cluster's {@link DocTopicItem} is null.
	 *
	 * @return False if {@link DocTopicItem} is null, true otherwise.
	 */
	public boolean hasDocTopicItem() { return this.dti != null; }

	/**
	 * Sets this Cluster's {@link DocTopicItem}.
	 */
	public void setDocTopicItem(DocTopicItem dti) { this.dti = dti; }

	public Set<String> union(ReadOnlyCluster c) {
		return this.entities.union(c.entities); }
	public Set<String> intersection(ReadOnlyCluster c) {
		return this.entities.intersection(c.entities); }
	public Set<String> difference(ReadOnlyCluster c) {
		return this.entities.difference(c.entities); }
	public Set<String> symmetricDifference(ReadOnlyCluster c) {
		return this.entities.symmetricDifference(c.entities); }
	//endregion

	//region PROCESSING
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
	public void writeToDot(SimpleDirectedGraph<String, LabeledEdge> graph,
			ReadOnlyArchitecture arch, String outputPath) throws IOException {
		writeToDot(graph, arch, outputPath, this.name);
	}

	public void writeToDot(SimpleDirectedGraph<String, LabeledEdge> graph,
			ReadOnlyArchitecture arch, String outputPath, String name)
			throws IOException {
		try (FileWriter writer = new FileWriter(outputPath)) {
			writer.write("digraph \"" + name + "\" {\n");

			this.writeToDot(graph, arch, writer, name);

			writer.write("}\n");
		}
	}

	public void writeToDot(SimpleDirectedGraph<String, LabeledEdge> graph,
			ReadOnlyArchitecture arch, FileWriter writer) throws IOException {
		writeToDot(graph, arch, writer, this.name);
	}

	public void writeToDot(SimpleDirectedGraph<String, LabeledEdge> graph,
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

		double intraconnectivity =
			IntraConnectivity.computeIntraConnectivity(this, graph);
		DescriptiveStatistics interconnectivity =
			InterConnectivity.computeInterConnectivity(this, arch, graph);
		double basicMq = intraconnectivity - interconnectivity.getMean();
		double clusterFactor = TurboMQ.computeClusterFactor(this, graph);
		double fanIn = ArchitecturalStability.computeFanIn(this, graph);
		double fanOut = ArchitecturalStability.computeFanOut(this, graph);
		double instability =
			ArchitecturalStability.computeStability(this, graph);

		DecimalFormat formatter = new DecimalFormat("#.####");

		writer.write("\t\tlabel = \"Cluster: " + name
			+ "\\nIntra-connectivity: " + formatter.format(intraconnectivity)
			+ "\\nInter-connectivity: "
				+ formatter.format(interconnectivity.getMean()) + ", "
				+ formatter.format(interconnectivity.getPercentile(50.0)) + ", "
				+ formatter.format(interconnectivity.getMin()) + ", "
				+ formatter.format(interconnectivity.getMax()) + ", "
				+ formatter.format(interconnectivity.getStandardDeviation())
			+ "\\nBasicMQ: " + formatter.format(basicMq)
			+ "\\nCluster Factor: " + formatter.format(clusterFactor)
			+ "\\nFan-in: " + formatter.format(fanIn)
			+ "\\nFan-out: " + formatter.format(fanOut)
			+ "\\nInstability: " + formatter.format(instability)
			+ "\";\n");
		writer.write("\t}\n");
	}
	//endregion
}
