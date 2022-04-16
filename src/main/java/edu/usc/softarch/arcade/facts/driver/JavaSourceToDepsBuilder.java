package edu.usc.softarch.arcade.facts.driver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import classycle.Analyser;
import classycle.ClassAttributes;
import classycle.graph.AtomicVertex;
import edu.usc.softarch.arcade.clustering.FeatureVectors;
import edu.usc.softarch.arcade.util.FileUtil;

public class JavaSourceToDepsBuilder extends SourceToDepsBuilder {
	// #region INTERFACE ---------------------------------------------------------
	public static void main(String[] args) throws IOException {
		String classesDirPath = args[0];
		String depsRsfFilename = args[1];
		String ffVecsFilename = args[2];
		String packagePrefix = args[3];

		(new JavaSourceToDepsBuilder()).build(classesDirPath, depsRsfFilename, ffVecsFilename, packagePrefix);
	}
	// #endregion INTERFACE ------------------------------------------------------

	// #region PROCESSING --------------------------------------------------------
	@Override
	public void build(String classesDirPath, String depsRsfFilename, String ffVecsFilename)
			throws IOException {
		build(classesDirPath, depsRsfFilename, ffVecsFilename, "");
	}

	public void build(String classesDirPath, String depsRsfFilename,
			String ffVecsFilename, String packagePrefix) throws IOException {
		String[] inputClasses = { FileUtil.tildeExpandPath(classesDirPath) };
		String depsRsfFilepath = FileUtil.tildeExpandPath(depsRsfFilename);
		(new File(depsRsfFilepath)).getParentFile().mkdirs();
		
		// Classycle gets a list of directories and/or files and runs an analysis
		// on everything it received.
		Analyser analyzer = new Analyser(inputClasses);
		// "false" means it will do a full analysis rather than package-only.
		analyzer.readAndAnalyse(false);

		// Building the dependency graph as a set of edges between classes
		AtomicVertex[] graph = analyzer.getClassGraph();
		this.edges = buildEdges(graph, packagePrefix);
		
		// Prints the dependencies to a file
		serializeEdges(this.edges, depsRsfFilepath);
		
		// Calculating the number of source entities in dependency graph
		Set<String> sources = new HashSet<>();
		for (Map.Entry<String,String> edge : edges)
			sources.add(edge.getKey());
		this.numSourceEntities = sources.size();

		this.ffVecs = new FeatureVectors(edges);

		this.ffVecs.serializeFFVectors(ffVecsFilename);
	}

	/**
	 * Converts the format of a graph from Classycle's to ARCADE's.
	 * 
	 * @param graph A graph drawn from Classycle.
	 */
	private Set<Map.Entry<String, String>> buildEdges(
			AtomicVertex[] graph, String packagePrefix) {
		Set<Map.Entry<String, String>> edges = new LinkedHashSet<>();

		// For each Vertex in the graph
		for (AtomicVertex vertex : graph) {
			// Get the attributes of the vertex
			ClassAttributes sourceAttributes =
				(ClassAttributes)vertex.getAttributes();

			// Ignore edges where source is irrelevant
			if (!packagePrefix.equals("")
					&& !sourceAttributes.getName().startsWith(packagePrefix))
				continue;

			// And then for each edge of that vertex
			for (int j = 0; j < vertex.getNumberOfOutgoingArcs(); j++) {
				// Get the attributes of the related vertex
				ClassAttributes targetAttributes =
					(ClassAttributes)vertex.getHeadVertex(j).getAttributes();
				// Create a Pair to represent the edge
				Map.Entry<String,String> edge = new AbstractMap.SimpleEntry<>(
					sourceAttributes.getName(), targetAttributes.getName());

				// And add it to the set of edges
				edges.add(edge);
			}
		}

		return edges;
	}
	// #endregion PROCESSING -----------------------------------------------------

	// #region IO ----------------------------------------------------------------
	private void serializeEdges(Set<Map.Entry<String, String>> edges,
			String depsRsfFilepath) throws FileNotFoundException{
		PrintStream out = new PrintStream(depsRsfFilepath);
		PrintWriter writer = new PrintWriter(out);
		for (Map.Entry<String,String> edge : edges) {
			writer.println("depends " + edge.getKey() + " " + edge.getValue());
		}
		writer.close();
	}
	// #endregion IO -------------------------------------------------------------
}