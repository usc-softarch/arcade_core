package edu.usc.softarch.arcade.facts.driver;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.PropertyConfigurator;

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
	private static String loggingConfigFilename = "cfg" + File.separator + 
		"extractor_logging.cfg"; //TODO Make this come from outside
	// #endregion FIELDS ---------------------------------------------------------
	
	// #region ACCESSORS ---------------------------------------------------------
	@Override
	public Set<Pair<String,String>> getEdges() { return this.edges; }
	
	@Override
	public int getNumSourceEntities() { return this.numSourceEntities; }

	@Override
	public FastFeatureVectors getFfVecs() {	return this.ffVecs; }
	// #endregion ACCESSORS ------------------------------------------------------

	public static void main(String[] args) throws IOException {
		String classesDirPath = args[0];
		String depsRsfFilename = args[1];
		(new JavaSourceToDepsBuilder()).build(classesDirPath, depsRsfFilename);
	}

	public void build(String classesDirPath, String depsRsfFilename)
			throws IOException {
		PropertyConfigurator.configure(loggingConfigFilename);
		
		String[] inputClasses = { FileUtil.tildeExpandPath(classesDirPath) };
		String depsRsfFilepath = FileUtil.tildeExpandPath(depsRsfFilename);
		
		// Classycle gets a list of directories and/or files and runs an analysis
		// on everything it received.
		Analyser analyzer = new Analyser(inputClasses);
		// "false" means it will do a full analysis rather than a package-only
		// analysis.
		analyzer.readAndAnalyse(false);

		AtomicVertex[] graph = analyzer.getClassGraph();
		
		// Building the dependency graph as a set of edges between classes
		edges = new LinkedHashSet<>();
		for (int i = 0; i < graph.length; i++) {
			AtomicVertex vertex = graph[i];
			ClassAttributes sourceAttributes = 
				(ClassAttributes)vertex.getAttributes();
			for (int j = 0, n = vertex.getNumberOfOutgoingArcs(); j < n; j++) {
				ClassAttributes targetAttributes = 
					(ClassAttributes)vertex.getHeadVertex(j).getAttributes();
				Pair<String,String> edge =
					new ImmutablePair<>(
						sourceAttributes.getName(), targetAttributes.getName());
				edges.add(edge);
			}
		}
		
		// Prints the dependencies to a file
		PrintStream out = new PrintStream(depsRsfFilepath);
		PrintWriter writer = new PrintWriter(out);
		for (Pair<String,String> edge : edges) {
			writer.println("depends " + edge.getLeft() + " " + edge.getRight());
		}
		writer.close();
		
		// Calculating the number of source entities in dependency graph
		Set<String> sources = new HashSet<>();
		for (Pair<String,String> edge : edges) {
			sources.add(edge.getLeft());
		}
		numSourceEntities = sources.size();
		
		// Creates a proper graph object to hold the edges set.
		TypedEdgeGraph typedEdgeGraph = new TypedEdgeGraph();
		for (Pair<String,String> edge : edges) {
			typedEdgeGraph.addEdge("depends",edge.getLeft(),edge.getRight());
		}
		
		FeatureVectorMap fvMap = new FeatureVectorMap(typedEdgeGraph);
		ffVecs = fvMap.convertToFastFeatureVectors();
	}
}
