package edu.usc.softarch.arcade.facts.driver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import classycle.Analyser;
import classycle.ClassAttributes;
import classycle.graph.AtomicVertex;
import edu.usc.softarch.arcade.clustering.FastFeatureVectors;
import edu.usc.softarch.arcade.clustering.FeatureVectorMap;
import edu.usc.softarch.arcade.functiongraph.TypedEdgeGraph;
import edu.usc.softarch.arcade.util.FileUtil;

public class JavaSourceToDepsBuilder extends SourceToDepsBuilder {
	// #region FIELDS ------------------------------------------------------------
	private FastFeatureVectors ffVecs = null;
	private int numSourceEntities = 0;
	// #endregion FIELDS ---------------------------------------------------------
	
	// #region ACCESSORS ---------------------------------------------------------
	@Override
	public Set<Pair<String,String>> getEdges() { return this.edges; }
	
	@Override
	public int getNumSourceEntities() { return this.numSourceEntities; }

	@Override
	public FastFeatureVectors getFfVecs() {	return this.ffVecs; }
	// #endregion ACCESSORS ------------------------------------------------------

	// #region PROCESSING --------------------------------------------------------
	public void build(String classesDirPath, String depsRsfFilename)
			throws IOException {
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
		this.edges = buildEdges(graph);
		
		// Prints the dependencies to a file
		serializeEdges(this.edges, depsRsfFilepath);
		
		// Calculating the number of source entities in dependency graph
		Set<String> sources = new HashSet<>();
		for (Pair<String,String> edge : edges)
			sources.add(edge.getLeft());
		this.numSourceEntities = sources.size();
		
		// Creates a proper graph object to hold the edges set.
		TypedEdgeGraph typedEdgeGraph = new TypedEdgeGraph();
		for (Pair<String,String> edge : edges)
			typedEdgeGraph.addEdge("depends", edge.getLeft(), edge.getRight());
		FeatureVectorMap fvMap = new FeatureVectorMap(typedEdgeGraph);
		this.ffVecs = fvMap.convertToFastFeatureVectors();
	}

	/**
	 * Converts the format of a graph from Classycle's to ARCADE's.
	 * 
	 * @param graph A graph drawn from Classycle.
	 */
	private Set<Pair<String, String>> buildEdges(AtomicVertex[] graph) {
		Set<Pair<String, String>> edges = new LinkedHashSet<>();

		// For each Vertex in the graph
		for (AtomicVertex vertex : graph) {
			// Get the attributes of the vertex
			ClassAttributes sourceAttributes =
				(ClassAttributes)vertex.getAttributes();
			// And then for each edge of that vertex
			for (int j = 0; j < vertex.getNumberOfOutgoingArcs(); j++) {
				// Get the attributes of the related vertex
				ClassAttributes targetAttributes =
					(ClassAttributes)vertex.getHeadVertex(j).getAttributes();
				// Create a Pair to represent the edge
				Pair<String,String> edge = new ImmutablePair<>(
					sourceAttributes.getName(), targetAttributes.getName());
				// And add it to the set of edges
				edges.add(edge);
			}
		}

		return edges;
	}
	// #endregion PROCESSING -----------------------------------------------------

	// #region IO ----------------------------------------------------------------
	public static void main(String[] args) throws IOException {
		String classesDirPath = args[0];
		String depsRsfFilename = args[1];
		(new JavaSourceToDepsBuilder()).build(classesDirPath, depsRsfFilename);
	}

	private void serializeEdges(Set<Pair<String, String>> edges,
			String depsRsfFilepath) throws FileNotFoundException{
		PrintStream out = new PrintStream(depsRsfFilepath);
		PrintWriter writer = new PrintWriter(out);
		for (Pair<String,String> edge : edges) {
			writer.println("depends " + edge.getLeft() + " " + edge.getRight());
		}
		writer.close();
	}
	// #endregion IO -------------------------------------------------------------
}