package edu.usc.softarch.arcade.facts.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.PropertyConfigurator;

import edu.usc.softarch.arcade.clustering.FastFeatureVectors;
import edu.usc.softarch.arcade.clustering.FeatureVectorMap;
import edu.usc.softarch.arcade.functiongraph.TypedEdgeGraph;
import edu.usc.softarch.arcade.util.FileUtil;

public class CSourceToDepsBuilder extends SourceToDepsBuilder {
	public static FastFeatureVectors ffVecs = null;
	public static int numSourceEntities = 0;
	
	@Override
	public Set<Pair<String,String>> getEdges() {
		return this.edges;
	}

	public static void main(String[] args) throws IOException {
		(new CSourceToDepsBuilder()).build(args[0], args[1]);
	}

	@Override
	public void build(String classesDirPath, String depsRsfFilename) throws IOException {
		PropertyConfigurator.configure("cfg" + File.separator + "extractor_logging.cfg");
		
		String inputDir = FileUtil.tildeExpandPath(classesDirPath);
		String depsRsfFilepath = FileUtil.tildeExpandPath(depsRsfFilename);
		
		String pwd = System.getProperty("user.dir");
		String mkFilesCmd = "perl " + pwd + File.separator + "mkfiles.pl";
		String mkDepCmd = "perl " + pwd + File.separator + "mkdep.pl";
		
		
		String[] cmds = { mkFilesCmd, mkDepCmd };
		for (String cmd : cmds) {
			execCmd(cmd,inputDir);
		}
		
		String makeDepFileLocation = inputDir + File.separator + "make.dep";
		String[] makeDepReaderArgs = {makeDepFileLocation,depsRsfFilepath};
		
		MakeDepReader.main(makeDepReaderArgs);
		
		RsfReader.loadRsfDataFromFile(depsRsfFilepath);
				
		numSourceEntities = RsfReader.unfilteredFacts.size();
		
		TypedEdgeGraph typedEdgeGraph = new TypedEdgeGraph();
		edges = new LinkedHashSet<Pair<String,String>>();
		for (List<String> fact : RsfReader.unfilteredFacts) {
			String source = fact.get(1);
			String target = fact.get(2);
			
			typedEdgeGraph.addEdge("depends",source,target);
			
			Pair<String,String> edge = new ImmutablePair<>(source,target);
			edges.add(edge);
		}
		
		Set<String> sources = new HashSet<>();
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