package edu.usc.softarch.arcade.facts.driver;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.google.common.base.Joiner;

import classycle.Analyser;
import classycle.ClassAttributes;
import classycle.graph.AtomicVertex;
import edu.usc.softarch.arcade.clustering.FastFeatureVectors;
import edu.usc.softarch.arcade.clustering.FeatureVectorMap;
import edu.usc.softarch.arcade.config.Config;
import edu.usc.softarch.arcade.functiongraph.TypedEdgeGraph;
import edu.usc.softarch.arcade.util.FileUtil;

public class JavaSourceToDepsBuilder implements SourceToDepsBuilder {
	
	static Logger logger = Logger.getLogger(JavaSourceToDepsBuilder.class);

	public Set<Pair<String,String>> edges;
	public static FastFeatureVectors ffVecs = null;
	public int numSourceEntities = 0;
	
	@Override
	public Set<Pair<String,String>> getEdges() {
		return this.edges;
	}
	
	@Override
	public int getNumSourceEntities() {
		return this.numSourceEntities;
	}

	public static void main(String[] args) throws IOException {
		(new JavaSourceToDepsBuilder()).build(args);
	}

	public void build(String[] args) throws IOException,
			FileNotFoundException {
		PropertyConfigurator.configure(Config.getLoggingConfigFilename());
		
		String[] inputClasses = { FileUtil.tildeExpandPath(args[0]) };
		String depsRsfFilename = FileUtil.tildeExpandPath(args[1]);
		
		Analyser analyzer = new Analyser(inputClasses);
		analyzer.readAndAnalyse(false);
		//analyzer.printRaw(new PrintWriter(System.out));

		PrintStream out = new PrintStream(depsRsfFilename);
		PrintWriter writer = new PrintWriter(out);
		AtomicVertex[] graph = analyzer.getClassGraph();
		
		edges = new LinkedHashSet<Pair<String,String>>();
		for (int i = 0; i < graph.length; i++) {
			AtomicVertex vertex = graph[i];
			ClassAttributes sourceAttributes = (ClassAttributes)vertex.getAttributes();
			//writer.println(sourceAttributes.getType() +  " " + sourceAttributes.getName());
			for (int j = 0, n = vertex.getNumberOfOutgoingArcs(); j < n; j++) {
				ClassAttributes targetAttributes = (ClassAttributes)vertex.getHeadVertex(j).getAttributes();
				//writer.println("    " + targetAttributes.getType() + " " + targetAttributes.getName());
				Pair<String,String> edge = new ImmutablePair<String,String>(sourceAttributes.getName(),targetAttributes.getName());
				edges.add(edge);
			}
		}
		
		for (Pair<String,String> edge : edges) {
			writer.println("depends " + edge.getLeft() + " " + edge.getRight());
		}
		writer.close();
		
		Set<String> sources = new HashSet<String>();
		for (Pair<String,String> edge : edges) {
			sources.add(edge.getLeft());
		}
		numSourceEntities = sources.size();
		
		TypedEdgeGraph typedEdgeGraph = new TypedEdgeGraph();
		for (Pair<String,String> edge : edges) {
			typedEdgeGraph.addEdge("depends",edge.getLeft(),edge.getRight());
		}
		
		FeatureVectorMap fvMap = new FeatureVectorMap(typedEdgeGraph);
		ffVecs = fvMap.convertToFastFeatureVectors();
	}

	@Override
	public FastFeatureVectors getFfVecs() {
		return this.ffVecs;
	}

}
