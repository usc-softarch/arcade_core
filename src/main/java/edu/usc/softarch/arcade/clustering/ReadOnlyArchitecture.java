package edu.usc.softarch.arcade.clustering;

import edu.usc.softarch.arcade.topics.DocTopics;
import edu.usc.softarch.util.EnhancedHashSet;
import edu.usc.softarch.util.EnhancedSet;
import edu.usc.softarch.util.LabeledEdge;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class ReadOnlyArchitecture extends TreeMap<String, ReadOnlyCluster> {
	//region ATTRIBUTES
	private final Map<String, ReadOnlyCluster> entityLocationMap;
	//endregion

	//region CONSTRUCTORS
	public ReadOnlyArchitecture(Architecture arch) {
		super();

		this.entityLocationMap = new HashMap<>();

		for (Map.Entry<String, Cluster> entry : arch.entrySet())
			this.put(entry.getKey(), new ReadOnlyCluster(entry.getValue()));
	}

	private ReadOnlyArchitecture() {
		super();

		this.entityLocationMap = new HashMap<>();
	}

	private ReadOnlyArchitecture(ReadOnlyArchitecture toClone) {
		this.entityLocationMap = new HashMap<>();

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
	public int countEntities() {
		int result = 0;

		for (ReadOnlyCluster cluster : this.values())
			result += cluster.getEntities().size();

		return result;
	}

	public EnhancedSet<String> getEntities() {
		EnhancedSet<String> result = new EnhancedHashSet<>();

		for (ReadOnlyCluster cluster : this.values())
			result.addAll(cluster.getEntities());

		return result;
	}

	public ReadOnlyArchitecture removeEntities(Set<String> entities) {
		ReadOnlyArchitecture result = new ReadOnlyArchitecture(this);
		result.values().forEach(c -> c.removeEntities(entities));

		return result;
	}

	public Map<String, ReadOnlyCluster> getEntityLocationMap() {
		if (this.entityLocationMap.isEmpty()) buildEntityLocationMap();
		return this.entityLocationMap;
	}
	//endregion

	//region PROCESSING
	public SimpleDirectedGraph<String, DefaultEdge> buildGraph(String depsPath)
			throws IOException {
		SimpleDirectedGraph<String, DefaultEdge> result =
			new SimpleDirectedGraph<>(DefaultEdge.class);

		for (ReadOnlyCluster cluster : this.values())
			result.addVertex(cluster.name);

		// Sanity check to ensure it's been built
		getEntityLocationMap();

		try (BufferedReader br = new BufferedReader(new FileReader(depsPath))) {
			String line;

			while ((line = br.readLine()) != null) {
				if (line.trim().isEmpty()) continue;

				String[] entry = line.split(" ");

				ReadOnlyCluster source = this.entityLocationMap.get(entry[1]);
				ReadOnlyCluster target = this.entityLocationMap.get(entry[2]);

				if (source == null || target == null) continue;

				if (!source.equals(target)
						&& !result.containsEdge(source.name, target.name))
					result.addEdge(source.name, target.name);
			}
		}

		return result;
	}

	public SimpleDirectedGraph<String, LabeledEdge> buildFullGraph(
			String depsPath) throws IOException {
		SimpleDirectedGraph<String, LabeledEdge> result =
			new SimpleDirectedGraph<>(LabeledEdge.class);

		for (ReadOnlyCluster cluster : this.values())
			cluster.getEntities().forEach(result::addVertex);

		// Sanity check to ensure it's been built
		getEntityLocationMap();

		try (BufferedReader br = new BufferedReader(new FileReader(depsPath))) {
			String line;

			while ((line = br.readLine()) != null) {
				if (line.trim().isEmpty()) continue;

				String[] entry = line.split(" ");

				ReadOnlyCluster source = this.entityLocationMap.get(entry[1]);
				ReadOnlyCluster target = this.entityLocationMap.get(entry[2]);

				// If the entity is not in the model, or the edge is to itself, skip
				if (source == null || target == null || entry[1].equals(entry[2]))
					continue;

				if (source.equals(target))
					result.addEdge(entry[1], entry[2], new LabeledEdge("internal"));
				else
					result.addEdge(entry[1], entry[2], new LabeledEdge("external"));
			}
		}

		return result;
	}

	private void buildEntityLocationMap() {
		for (ReadOnlyCluster cluster : this.values())
			for (String entity : cluster.getEntities())
				this.entityLocationMap.put(entity, cluster);
	}

	public void loadDocTopics(String path) throws IOException {
		DocTopics loader = DocTopics.deserialize(path);

		for (ReadOnlyCluster cluster : this.values())
			cluster.setDocTopicItem(loader.getDocTopicItem(cluster.name));
	}
	//endregion

	//region SERIALIZATION
	public static ReadOnlyArchitecture readFromRsf(String path)
			throws IOException {
		return readFromRsf(new File(path));
	}

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

	public void writeToDotClusters(String depsPath, String outputPath)
			throws IOException {
		SimpleDirectedGraph<String, LabeledEdge> graph = buildFullGraph(depsPath);

		for (ReadOnlyCluster cluster : this.values())
			cluster.writeToDot(graph, this,
				outputPath + File.separator + cluster.name + ".dot");
	}

	public void writeToDotClusters(String depsPath, String outputPath,
			Map<Integer, Cluster> index) throws IOException {
		SimpleDirectedGraph<String, LabeledEdge> graph = buildFullGraph(depsPath);

		for (Map.Entry<Integer, Cluster> cluster : index.entrySet())
			cluster.getValue().writeToDot(graph, this,
				outputPath + File.separator + cluster.getKey() + ".dot",
				cluster.getKey().toString());
	}

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
	//endregion
}
