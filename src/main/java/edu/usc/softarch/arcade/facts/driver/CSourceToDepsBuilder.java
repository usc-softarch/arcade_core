package edu.usc.softarch.arcade.facts.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.usc.softarch.arcade.clustering.FeatureVectors;
import edu.usc.softarch.arcade.functiongraph.TypedEdgeGraph;
import edu.usc.softarch.arcade.util.FileUtil;

public class CSourceToDepsBuilder extends SourceToDepsBuilder {
	public static FeatureVectors ffVecs = null;
	public static int numSourceEntities = 0;
	
	@Override
	public Set<Map.Entry<String,String>> getEdges() {
		return this.edges;
	}

	public static void main(String[] args) throws IOException {
		(new CSourceToDepsBuilder()).build(args[0], args[1], args[2]);
	}

	@Override
	public void build(String classesDirPath, String depsRsfFilename, String ffVecsFilename) throws IOException {
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
				
		numSourceEntities = RsfReader.unfilteredFaCtS.size();
		
		TypedEdgeGraph typedEdgeGraph = new TypedEdgeGraph();
		edges = new LinkedHashSet<Map.Entry<String,String>>();
		for (List<String> fact : RsfReader.unfilteredFaCtS) {
			String source = fact.get(1);
			String target = fact.get(2);
			
			typedEdgeGraph.addEdge("depends",source,target);
			
			Map.Entry<String,String> edge =
				new AbstractMap.SimpleEntry<>(source,target);
			edges.add(edge);
		}
		
		Set<String> sources = new HashSet<>();
		for (Map.Entry<String,String> edge : edges) {
			sources.add(edge.getKey());
		}
		numSourceEntities = sources.size();
		ffVecs = new FeatureVectors(edges);

		ffVecs.serializeFFVectors(ffVecsFilename);
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
	public FeatureVectors getFfVecs() {
		return ffVecs;
	}
}