package edu.usc.softarch.arcade.util.graph;

import java.awt.Container;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.collections15.Factory;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.google.common.base.Joiner;

import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Tree;
import edu.usc.softarch.arcade.MetricsDriver;
import edu.usc.softarch.arcade.config.Config;
import edu.usc.softarch.arcade.facts.driver.RsfReader;



public class FlatDecompositionBuilder {
	
	public enum FlatDecompType {
		compact, detailed
	}
	
	static Logger logger = Logger.getLogger(FlatDecompositionBuilder.class);
	
	static Factory<Integer> edgeFactory = new Factory<Integer>() {
		int i=0;
		public Integer create() {
			return i++;
		}};
		
	public static void buildViewer(Tree<String,Integer> tree) {
		JFrame frame = new JFrame();
        Container content = frame.getContentPane();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        content.add(new TreeGraphGenerator(tree));
        frame.pack();
        frame.setVisible(true);
	}

	public static void main(String[] args) {
		PropertyConfigurator.configure(Config.getLoggingConfigFilename());

		// String filename = "data/generated/generated_tree_5mc_5mh.rsf";
		// String filename =
		// "/home/joshua/recovery/Expert Decompositions/linux.Nested.Autho#254C003D.rsf";
		String nestedFilename = "";
		String flatFilename = "";
		FlatDecompType fdt = FlatDecompType.compact;
		boolean isVisualizingTree = false;
		
		Options options = new Options();
		
		
		
		Option help = new Option( "help", "print this message" );
		Option visualizeOption = new Option( "visualize", "show visualization of tree" );
		Option nestedFilenameOption = OptionBuilder.withArgName( "file" )
                .hasArg()
                .withDescription(  "nested file to flatten" )
                .create( "nestedFile" );
		Option flatFilenameOption = OptionBuilder.withArgName( "file" )
                .hasArg()
                .withDescription(  "flattened file name" )
                .create( "flatFile" );
		Option flattenTypeOption = OptionBuilder.withArgName("c|d").hasArg().withDescription("flatten as compact (c) or detailed (d)").create("type");
		
		options.addOption(help);
		options.addOption(nestedFilenameOption);
		options.addOption(flatFilenameOption);
		options.addOption(flattenTypeOption);
		
		 // create the parser
		CommandLineParser parser = new GnuParser();
		try {
			// parse the command line arguments
			CommandLine line = parser.parse(options, args);

			if (line.hasOption("help")) {
				generateHelpStatement(options);
			}
			if (line.hasOption("visualize")) {
				isVisualizingTree = true;
			}
			if (line.hasOption("nestedFile")) {
				nestedFilename = line.getOptionValue("nestedFile");
			}
			if (line.hasOption("flatFile")) {
				flatFilename = line.getOptionValue("flatFile");
			}
			if (line.hasOption("type")) {
				String typeStr = line.getOptionValue("type");
				if (typeStr.equals("c")) {
					fdt = FlatDecompType.compact;
				}
				else if (typeStr.equals("d")) {
					fdt = FlatDecompType.detailed;
				}
			}
		} catch (ParseException exp) {
			// oops, something went wrong
			System.err.println("Parsing failed.  Reason: " + exp.getMessage());
			generateHelpStatement(options);
		}
		
		
		
		RsfReader.loadRsfDataFromFile(nestedFilename);
		logger.debug("Printing filtered routine facts...");
		logger.debug(Joiner.on("\n").join(RsfReader.filteredRoutineFacts));
		
		DirectedGraph<String,Integer> dGraph = new DirectedSparseGraph<String,Integer>();
		for (List<String> fact : RsfReader.filteredRoutineFacts) {
			dGraph.addEdge(edgeFactory.create(), fact.get(1), fact.get(2));
		}
		
		logger.debug("Printing graph...");
		logger.debug(dGraph);
		
		DelegateTree<String,Integer> tree = new DelegateTree<String,Integer>(dGraph);
		for (String vertex : tree.getVertices()) {
			if (tree.getParent(vertex) == null) {
				tree.setRoot(vertex);
			}
		}
		if (tree.getRoot() == null) {
			throw new RuntimeException("tree has no root...");
		}
		logger.debug("Printing tree...");
		logger.debug(tree);
		if (isVisualizingTree)
			buildViewer(tree);
		
		Map<String, List<String>> clustersMap = null;
		if (fdt.equals(FlatDecompType.compact))
			clustersMap = buildCompactFlatClusters(tree);
		else if (fdt.equals(FlatDecompType.detailed))
			clustersMap = buildDetailedFlatClusters(tree);
		else 
			throw new IllegalArgumentException("selected FlatDecompType is invalid: " + fdt);
		
		logger.debug("number of flat clusters: " + clustersMap.keySet().size());
		for (String parent : clustersMap.keySet()) {
			List<String> members = clustersMap.get(parent);
			logger.debug("parent: " + parent);
			logger.debug("members: " + Joiner.on(",").join(members));
		}
		
		FileWriter fw;
		try {
			fw = new FileWriter(flatFilename);
			BufferedWriter out = new BufferedWriter(fw);
			for (String parent : clustersMap.keySet()) {
				List<String> members = clustersMap.get(parent);
				for (String member : members) {
					String rsfLine = "contain " + parent + " " + member;
					logger.debug(rsfLine);
					out.write(rsfLine + "\n");
				}
			}
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}

	private static void generateHelpStatement(Options options) {
		// automatically generate the help statement
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(FlatDecompositionBuilder.class.getName(), options);
		System.exit(0);
	}

	private static Map<String, List<String>> buildDetailedFlatClusters(
			DelegateTree<String, Integer> tree) {
		Map<String,List<String>> clustersMap = new HashMap<String,List<String>>();
		for (String vertex : tree.getVertices()) {
			if (tree.getRoot().equals(vertex)) {
				continue;
			}
			if (tree.isLeaf(vertex)) {
				List<String> clusterMembers = null;
				String parent = tree.getParent(vertex);
				if (clustersMap.containsKey(parent)) {
					clusterMembers = clustersMap.get(parent);
					clusterMembers.add(vertex);
				}
				else {
					clusterMembers = new ArrayList<String>();
					clusterMembers.add(vertex);
					clustersMap.put(parent, clusterMembers);
				}
			}
		}
		return clustersMap;
	}
	
	private static Map<String, List<String>> buildCompactFlatClusters(
			DelegateTree<String, Integer> tree) {
		Map<String,List<String>> clustersMap = new HashMap<String,List<String>>();
		Collection<String> topLevelClusters = tree.getChildren(tree.getRoot());
		for (String vertex : topLevelClusters) {
			List<String> leaves = new ArrayList<String>();
			getLeavesOfBranch(vertex,leaves,tree);
			clustersMap.put(vertex, leaves);
		}
		return clustersMap;
	}
	
	private static void getLeavesOfBranch(String vertex, Collection<String> leaves, DelegateTree<String,Integer> tree) {
		for (String child : tree.getChildren(vertex)) {
			if (tree.isLeaf(child)) {
				leaves.add(child);
			}
			else {
				getLeavesOfBranch(child, leaves, tree);
			}
		}
	}
}
