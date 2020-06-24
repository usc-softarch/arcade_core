package edu.usc.softarch.arcade.facts.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
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

public class CSourceToDepsBuilder implements SourceToDepsBuilder {
	
	static Logger logger = Logger.getLogger(CSourceToDepsBuilder.class);

	public Set<Pair<String,String>> edges;
	public static FastFeatureVectors ffVecs = null;
	public static int numSourceEntities = 0;
	
	@Override
	public Set<Pair<String,String>> getEdges() {
		return this.edges;
	}

	public static void main(String[] args) throws IOException {
		(new CSourceToDepsBuilder()).build(args);
	}

	public void build(String[] args) throws IOException,
			FileNotFoundException {
		PropertyConfigurator.configure(Config.getLoggingConfigFilename());
		
		String inputDir = FileUtil.tildeExpandPath(args[0]);
		String depsRsfFilename = FileUtil.tildeExpandPath(args[1]);
		
		String pwd = System.getProperty("user.dir");
		String mkFilesCmd = "perl " + pwd + File.separator + "mkfiles.pl";
		String mkDepCmd = "perl " + pwd + File.separator + "mkdep.pl";
		
		
		String[] cmds = { mkFilesCmd, mkDepCmd };
		for (String cmd : cmds) {
			execCmd(cmd,inputDir);
		}
		
		String makeDepFileLocation = inputDir + File.separator + "make.dep";
		String[] makeDepReaderArgs = {makeDepFileLocation,depsRsfFilename};
		
		MakeDepReader.main(makeDepReaderArgs);
		
		RsfReader.loadRsfDataFromFile(depsRsfFilename);
				
		numSourceEntities = RsfReader.unfilteredFacts.size();
		
		TypedEdgeGraph typedEdgeGraph = new TypedEdgeGraph();
		edges = new LinkedHashSet<Pair<String,String>>();
		for (List<String> fact : RsfReader.unfilteredFacts) {
			String source = fact.get(1);
			String target = fact.get(2);
			
			typedEdgeGraph.addEdge("depends",source,target);
			
			Pair<String,String> edge = new ImmutablePair<String,String>(source,target);
			edges.add(edge);
		}
		
		Set<String> sources = new HashSet<String>();
		for (Pair<String,String> edge : edges) {
			sources.add(edge.getLeft());
		}
		numSourceEntities = sources.size();
		
		FeatureVectorMap fvMap = new FeatureVectorMap(typedEdgeGraph);
		ffVecs = fvMap.convertToFastFeatureVectors();
	}

	private static void execCmd(String cmd, String inputDir) throws IOException {
		System.out.println("Executing command: " + cmd);
		Process process = Runtime.getRuntime().exec(cmd,null,new File(inputDir));

		String line;
		BufferedReader in = new BufferedReader(new InputStreamReader(
				process.getInputStream()));
		while ((line = in.readLine()) != null) {
			System.out.println(line);
		}
		in.close();		
	}

	@Override
	public int getNumSourceEntities() {
		return numSourceEntities;
	}

	@Override
	public FastFeatureVectors getFfVecs() {
		return ffVecs;
	}

}
