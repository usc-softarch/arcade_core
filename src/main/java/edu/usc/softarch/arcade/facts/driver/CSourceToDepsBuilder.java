package edu.usc.softarch.arcade.facts.driver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import edu.usc.softarch.arcade.clustering.FastFeatureVectors;
import edu.usc.softarch.arcade.functiongraph.TypedEdgeGraph;
import edu.usc.softarch.arcade.util.FileUtil;

public class CSourceToDepsBuilder extends SourceToDepsBuilder {
	@Override
	public void build(String classesDirPath, String depsRsfFilename)
			throws IOException {
		String inputDir = FileUtil.tildeExpandPath(classesDirPath);
		String depsRsfFilepath = FileUtil.tildeExpandPath(depsRsfFilename);
		
		String pwd = System.getProperty("user.dir");
		String mkFilesCmd = "perl " + pwd + File.separator + "mkfiles.pl";
		String mkDepCmd = "perl " + pwd + File.separator + "mkdep.pl";
		
		String[] cmds = { mkFilesCmd, mkDepCmd };
		for (String cmd : cmds)	execCmd(cmd, inputDir);
		
		String makeDepFileLocation = inputDir + File.separator + "make.dep";
		Map<String, List<String>> depMap = buildDeps(makeDepFileLocation);
		serializeResults(depsRsfFilepath, depMap);
		
		List<List<String>> unfilteredFacts =
			RsfReader.loadRsfDataFromFile(depsRsfFilepath);
		
		this.numSourceEntities = unfilteredFacts.size();
		
		TypedEdgeGraph typedEdgeGraph = new TypedEdgeGraph();
		this.edges = new LinkedHashSet<Pair<String, String>>();
		for (List<String> fact : unfilteredFacts) {
			String source = fact.get(1);
			String target = fact.get(2);
			
			typedEdgeGraph.addEdge("depends", source, target);
			
			Pair<String,String> edge = new ImmutablePair<>(source, target);
			this.edges.add(edge);
		}
		
		Set<String> sources = new HashSet<>();
		for (Pair<String,String> edge : this.edges)
			sources.add(edge.getLeft());
		this.numSourceEntities = sources.size();

		this.ffVecs = new FastFeatureVectors(typedEdgeGraph);
	}

	private Map<String, List<String>> buildDeps(String filename)
			throws IOException {
		Map<String, List<String>> depMap = new HashMap<>();

		try (Scanner scanner = new Scanner(new FileInputStream(filename))) {
			String currDotCFile = "";
			
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				String[] tokens = line.split("\\s");
				
				for (String token : tokens) {
					String trimmedToken = token.trim();
					
					if (trimmedToken.endsWith(".c") || trimmedToken.endsWith(".h")) {
						List<String> deps = null;
						deps = depMap.containsKey(currDotCFile)
							? depMap.get(currDotCFile)
							: new ArrayList<>();
						deps.add(trimmedToken);
						depMap.put(currDotCFile, deps);
					}
					if (trimmedToken.endsWith(".o:")) {
						trimmedToken = trimmedToken.substring(0, trimmedToken.length() - 1);
						currDotCFile = trimmedToken.replace(".o", ".c");
					}
				}
			}
		}

		return depMap;
	}

	private void serializeResults(String outRsfFile,
			Map<String, List<String>> depMap) throws IOException {
		Set<String> cFiles = depMap.keySet();
		try (BufferedWriter out = new BufferedWriter(new FileWriter(outRsfFile))) {
			for (String cFile : cFiles) {
				List<String> deps = depMap.get(cFile);
				for (String dep : deps)
					out.write("depends " + cFile + " " + dep + "\n");
			}
		}
	}

	private void execCmd(String cmd, String inputDir) throws IOException {
		System.out.println("Executing command: " + cmd);
		Process process = Runtime.getRuntime().exec(cmd,null,new File(inputDir));

		String line;
		BufferedReader in = new BufferedReader(new InputStreamReader(
			process.getInputStream()));
		while ((line = in.readLine()) != null)
			System.out.println(line);
		in.close();		
	}
}