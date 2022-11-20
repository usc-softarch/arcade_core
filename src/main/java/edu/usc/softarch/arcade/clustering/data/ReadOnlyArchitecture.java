package edu.usc.softarch.arcade.clustering.data;

import edu.usc.softarch.arcade.clustering.Architecture;
import edu.usc.softarch.arcade.clustering.Cluster;
import edu.usc.softarch.arcade.clustering.Clusterer;
import edu.usc.softarch.arcade.facts.DependencyGraph;
import edu.usc.softarch.arcade.topics.DocTopics;
import edu.usc.softarch.arcade.util.FileUtil;
import edu.usc.softarch.util.EnhancedHashSet;
import edu.usc.softarch.util.EnhancedSet;
import edu.usc.softarch.util.LabeledEdge;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/*
 * TODO This should use composition instead of inheritance so it can track
 *  changes to the entities.
 */
/**
 * Architecture object which cannot be used for further clustering, i.e. is
 * already the result of a clustering technique. Used for post-processing
 * components.
 */
public class ReadOnlyArchitecture extends TreeMap<String, ReadOnlyCluster> {
	//region ATTRIBUTES
	/**
	 * Map of code-level entity names to the clusters in which they are
	 * contained. Used for building entity and cluster dependency graphs.
	 * Persisted in case multiple graphs are needed, but should only be accessed
	 * via {@link #getEntityLocationMap()}.
	 *
	 * @see #buildFullGraph(String)
	 * @see #buildGraph(String)
	 */
	private final Map<String, ReadOnlyCluster> entityLocationMap;
	//endregion

	//region CONSTRUCTORS
	/**
	 * Base constructor.
	 */
	private ReadOnlyArchitecture() {
		super();

		this.entityLocationMap = new HashMap<>();
	}

	/**
	 * Constructor that replicates an {@link Architecture} object. Used in
	 * serialization.
	 *
	 * @param arch The {@link Architecture} to replicate.
	 */
	public ReadOnlyArchitecture(Architecture arch) {
		this();

		for (Map.Entry<String, Cluster> entry : arch.entrySet())
			this.put(entry.getKey(), new ReadOnlyCluster(entry.getValue()));
	}

	/**
	 * Clone constructor.
	 *
	 * @param toClone Architecture to clone.
	 */
	private ReadOnlyArchitecture(ReadOnlyArchitecture toClone) {
		this();

		for (Map.Entry<String, ReadOnlyCluster> entry : toClone.entrySet())
			this.put(entry.getKey(), new ReadOnlyCluster(entry.getValue()));
		for (Map.Entry<String, ReadOnlyCluster> entity
				: toClone.getEntityLocationMap().entrySet()) {
			this.entityLocationMap.put(
				entity.getKey(), new ReadOnlyCluster(entity.getValue()));
		}
	}
	//endregion

	//region ACCESSORS
	/**
	 * Counts the number of code-level entities in this architecture.
	 *
	 * @return The count of code-level entities.
	 */
	public int countEntities() {
		int result = 0;

		for (ReadOnlyCluster cluster : this.values())
			result += cluster.getEntities().size();

		return result;
	}

	/**
	 * Gets the set of all code-level entities contained in this architecture.
	 *
	 * @return The set of all code-level entities.
	 */
	public EnhancedSet<String> getEntities() {
		EnhancedSet<String> result = new EnhancedHashSet<>();

		for (ReadOnlyCluster cluster : this.values())
			result.addAll(cluster.getEntities());

		return result;
	}

	/**
	 * Computes the difference between this architecture and the provided set of
	 * code-level entities by copying this architecture and removing those
	 * entities from the clusters in which they are contained. This object is
	 * not modified.
	 *
	 * @param entities The code-level entities to remove from the copy.
	 * @return The architecture copy without the provided code-level entities.
	 */
	public ReadOnlyArchitecture difference(Set<String> entities) {
		ReadOnlyArchitecture result = new ReadOnlyArchitecture(this);
		result.values().forEach(c -> c.removeEntities(entities));

		return result;
	}

	/**
	 * Creates and/or provides access to the {@link #entityLocationMap}.
	 *
	 * @return The built {@link #entityLocationMap}.
	 */
	public Map<String, ReadOnlyCluster> getEntityLocationMap() {
		if (this.entityLocationMap.isEmpty()) {
			for (ReadOnlyCluster cluster : this.values())
				for (String entity : cluster.getEntities())
					this.entityLocationMap.put(entity, cluster);
		}

		return this.entityLocationMap;
	}
	//endregion

	//region PROCESSING
	/**
	 * Builds the cluster-level dependency graph of this architecture.
	 *
	 * @param depsPath The path to the dependencies RSF file related to this
	 *                 architecture. This must be the same file that was used
	 *                 to originally create this architecture.
	 * @return The cluster-level dependency graph.
	 * @throws IOException Thrown if there are any errors in accessing the
	 * 				 dependencies file.
	 */
	public SimpleDirectedGraph<String, DefaultEdge> buildGraph(String depsPath)
			throws IOException {
		SimpleDirectedGraph<String, DefaultEdge> result =
			new SimpleDirectedGraph<>(DefaultEdge.class);

		for (ReadOnlyCluster cluster : this.values())
			result.addVertex(cluster.name);

		// Sanity check to ensure it's been built
		getEntityLocationMap();

		DependencyGraph depsGraph = DependencyGraph.readRsf(depsPath);
		for (Map.Entry<String, String> dependency : depsGraph) {
			ReadOnlyCluster source =
				this.entityLocationMap.get(dependency.getKey());
			ReadOnlyCluster target =
				this.entityLocationMap.get(dependency.getValue());

			// If either source or target entities are not in the model, skip.
			if (source == null || target == null) continue;

			if (!source.equals(target)
					&& !result.containsEdge(source.name, target.name))
				result.addEdge(source.name, target.name);
		}

		return result;
	}

	/**
	 * Builds the code-level entity dependency graph of this architecture.
	 *
	 * @param depsPath The path to the dependencies RSF file related to this
	 *                 architecture. This must be the same file that was used
	 *                 to originally create this architecture.
	 * @return The code-level entity dependency graph of this architecture.
	 * @throws IOException Thrown if there are any errors in accessing the
	 * 				 dependencies file.
	 */
	public SimpleDirectedGraph<String, LabeledEdge> buildFullGraph(
			String depsPath) throws IOException {
		SimpleDirectedGraph<String, LabeledEdge> result =
			new SimpleDirectedGraph<>(LabeledEdge.class);

		for (ReadOnlyCluster cluster : this.values())
			cluster.getEntities().forEach(result::addVertex);

		// Sanity check to ensure it's been built
		getEntityLocationMap();

		DependencyGraph depsGraph = DependencyGraph.readRsf(depsPath);
		for (Map.Entry<String, String> dependency : depsGraph) {
			ReadOnlyCluster source =
				this.entityLocationMap.get(dependency.getKey());
			ReadOnlyCluster target =
				this.entityLocationMap.get(dependency.getValue());

			// If the entity is not in the model, or the edge is to itself, skip
			if (source == null || target == null
					|| dependency.getKey().equals(dependency.getValue()))
				continue;

			if (source.equals(target))
				result.addEdge(dependency.getKey(), dependency.getValue(),
					new LabeledEdge("internal"));
			else
				result.addEdge(dependency.getKey(), dependency.getValue(),
					new LabeledEdge("external"));
		}

		return result;
	}

	/**
	 * Loads the {@link DocTopics} of this architecture from its serialized
	 * format, which should have been generated during clustering.
	 *
	 * @param path Path to the JSON-serialized {@link DocTopics}.
	 * @throws IOException Thrown if there are any errors in accessing the
	 * 				 specified {@link DocTopics} file.
	 */
	public void loadDocTopics(String path) throws IOException {
		DocTopics loader = DocTopics.deserialize(path);

		for (ReadOnlyCluster cluster : this.values())
			cluster.setDocTopicItem(loader.getDocTopicItem(cluster.name));
	}
	//endregion

	//region SERIALIZATION
	/**
	 * Builds an architecture object from an RSF file.
	 *
	 * @param path Path to the input RSF file.
	 * @return The reconstructed architecture.
	 * @throws IOException Thrown if there are any errors in accessing the
	 *         specified RSF file.
	 */
	public static ReadOnlyArchitecture readFromRsf(String path)
			throws IOException {
		return readFromRsf(new File(path));
	}

	/**
	 * Builds an architecture object from an RSF file.
	 *
	 * @param file The input RSF file.
	 * @return The reconstructed architecture.
	 * @throws IOException Thrown if there are any errors in accessing the
	 *         specified RSF file.
	 */
	public static ReadOnlyArchitecture readFromRsf(File file)
			throws IOException {
		ReadOnlyArchitecture result = new ReadOnlyArchitecture();

		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;

			while ((line = br.readLine()) != null) {
				if (line.trim().isEmpty()) continue;

				String[] entry = line.split(" ");

				ReadOnlyCluster cluster = new ReadOnlyCluster(entry[1]);
				result.putIfAbsent(entry[1], cluster);
				result.get(entry[1]).addEntity(entry[2]);
				result.entityLocationMap.put(entry[2], cluster);
			}
		}

		return result;
	}

	/**
	 * Serializes this architecture to RSF format.
	 *
	 * @param path Path to the file in which to serialize.
	 * @throws FileNotFoundException Thrown if the file path cannot be resolved.
	 */
	public void writeToRsf(String path) throws FileNotFoundException {
		File rsfFile = new File(path);
		rsfFile.getParentFile().mkdirs();

		writeToRsf(rsfFile);
	}

	/**
	 * Serializes this architecture to RSF format.
	 *
	 * @param file The file in which to serialize.
	 * @throws FileNotFoundException Thrown if the file path cannot be resolved.
	 */
	public void writeToRsf(File file) throws FileNotFoundException {
		try (PrintWriter out = new PrintWriter(new OutputStreamWriter(
			new FileOutputStream(file), StandardCharsets.UTF_8))) {
			for (ReadOnlyCluster cluster : this.values()) {
				Collection<String> entities = cluster.getEntities();
				for (String entity : entities)
					out.println("contain " + cluster.name + " " + entity);
			}
		}
	}

	/**
	 * Serializes this architecture into a Graphviz DOT format showing the
	 * dependencies between clusters.
	 *
	 * @param depsPath The path to the dependencies RSF file related to this
	 *                 architecture. This must be the same file that was used
	 *                 to originally create this architecture.
	 * @param outputPath Path to the file in which to serialize.
	 * @throws IOException Thrown if there are any errors in accessing the
	 * 				 dependencies file or the output file path.
	 */
	public void writeToDot(String depsPath, String outputPath)
			throws IOException {
		try (FileWriter writer = new FileWriter(outputPath)) {
			SimpleDirectedGraph<String, DefaultEdge> graph = buildGraph(depsPath);

			writer.write("digraph G {\n");

			for (ReadOnlyCluster cluster : this.values())
				writer.write("\t\"" + cluster.name + "\"" + ";\n");

			for (DefaultEdge edge : graph.edgeSet()) {
				String source = graph.getEdgeSource(edge);
				String target = graph.getEdgeTarget(edge);
				writer.write("\t\"" + source + "\" -> \"" + target + "\";\n");
			}

			writer.write("}\n");
		}
	}

	/**
	 * Serializes this architecture into a Graphviz DOT format showing the
	 * dependencies between all code-level entities, which are clustered into
	 * subgraphs. Warning: this is experimental and will probably result in
	 * very large files due to edges between clusters.
	 *
	 * @param depsPath The path to the dependencies RSF file related to this
	 *                 architecture. This must be the same file that was used
	 *                 to originally create this architecture.
	 * @param outputPath Path to the file in which to serialize.
	 * @throws IOException Thrown if there are any errors in accessing the
	 * 				 dependencies file or the output file path.
	 */
	public void writeToDotFull(String depsPath, String outputPath)
			throws IOException {
		try (FileWriter writer = new FileWriter(outputPath)) {
			SimpleDirectedGraph<String, LabeledEdge> graph = buildFullGraph(depsPath);

			writer.write("digraph G {\n");

			for (ReadOnlyCluster cluster : this.values())
				cluster.writeToDot(graph, this, writer);

			Set<LabeledEdge> externalEdges = graph.edgeSet().stream()
				.filter(e -> e.label.equals("external"))
				.collect(Collectors.toSet());

			for (LabeledEdge edge : externalEdges) {
				String source =
					graph.getEdgeSource(edge).replace("\\", ".");
				String target =
					graph.getEdgeTarget(edge).replace("\\", ".");
				writer.write("\t\"" + source + "\" -> \"" + target + "\";\n");
			}

			writer.write("}\n");
		}
	}

	/**
	 * Serializes this architecture into a Graphviz DOT format with multiple
	 * files representing each individual cluster and the dependencies between
	 * that cluster's entities.
	 *
	 * @param depsPath The path to the dependencies RSF file related to this
	 *                 architecture. This must be the same file that was used
	 *                 to originally create this architecture.
	 * @param outputPath Path to the directory in which to serialize.
	 * @throws IOException Thrown if there are any errors in accessing the
	 * 				 dependencies file or the output file path.
	 */
	public void writeToDotClusters(String depsPath, String outputPath)
			throws IOException {
		SimpleDirectedGraph<String, LabeledEdge> graph = buildFullGraph(depsPath);
		FileUtil.checkDir(outputPath, true, false);

		for (ReadOnlyCluster cluster : this.values())
			cluster.writeToDot(graph, this, outputPath + File.separator
				+ cluster.name.replace(File.separatorChar, '.') + ".dot");
	}

	/**
	 * Serializes this architecture into a Graphviz DOT format with multiple
	 * files representing each individual cluster and the dependencies between
	 * that cluster's entities. This variant uses a cluster index map
	 * generated by {@link Architecture#computeArchitectureIndex()}, and is
	 * meant to be used when serializing immediately after clustering.
	 *
	 * @param depsPath The path to the dependencies RSF file related to this
	 *                 architecture. This must be the same file that was used
	 *                 to originally create this architecture.
	 * @param outputPath Path to the directory in which to serialize.
	 * @param index The cluster index map.
	 * @throws IOException Thrown if there are any errors in accessing the
	 * 				 dependencies file or the output file path.
	 *
	 * @see Clusterer#printDots
	 * @see Clusterer#doSerializationStep()
	 */
	public void writeToDotClusters(String depsPath, String outputPath,
			Map<Integer, Cluster> index) throws IOException {
		SimpleDirectedGraph<String, LabeledEdge> graph = buildFullGraph(depsPath);

		for (Map.Entry<Integer, Cluster> cluster : index.entrySet())
			cluster.getValue().writeToDot(graph, this,
				outputPath + File.separator + cluster.getKey() + ".dot",
				cluster.getKey().toString());
	}
	//endregion
}
