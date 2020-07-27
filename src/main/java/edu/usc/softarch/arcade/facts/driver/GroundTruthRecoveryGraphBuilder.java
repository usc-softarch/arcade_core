package edu.usc.softarch.arcade.facts.driver;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import edu.usc.softarch.arcade.classgraphs.StringEdge;
import edu.usc.softarch.arcade.clustering.StringGraph;
import edu.usc.softarch.arcade.clustering.util.ClusterUtil;
import edu.usc.softarch.arcade.config.Config;
import edu.usc.softarch.arcade.facts.ConcernCluster;
import edu.usc.softarch.arcade.facts.GroundTruthFileParser;
import edu.usc.softarch.extractors.cda.odem.Dependencies;
import edu.usc.softarch.extractors.cda.odem.DependsOn;
import edu.usc.softarch.extractors.cda.odem.Type;

public class GroundTruthRecoveryGraphBuilder {
	static Logger logger = Logger.getLogger(GroundTruthRecoveryGraphBuilder.class);
	
	public static void main(String[] args) {
		Options options = new Options();

		Option help = new Option("help", "print this message");

		Option projFile = OptionBuilder.withArgName("file").hasArg()
				.withDescription("project configuration file")
				.create("projfile");

		options.addOption(help);
		options.addOption(projFile);

		// create the parser
		CommandLineParser parser = new GnuParser();
		try {
			// parse the command line arguments
			CommandLine line = parser.parse(options, args);

			if (line.hasOption("projfile")) {
				Config.setProjConfigFilename(line.getOptionValue("projfile"));
			}
			if (line.hasOption("help")) {
				// automatically generate the help statement
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("GroundTruthRecoveryGraphBuilder", options);
				System.exit(0);
			}
		} catch (ParseException exp) {
			// oops, something went wrong
			System.err.println("Parsing failed.  Reason: " + exp.getMessage());
		}
		
		PropertyConfigurator.configure(Config.getLoggingConfigFilename());
		
		Config.initConfigFromFile(Config.getProjConfigFilename());
		
		System.out.println("Reading in odem file " + Config.getOdemFile()  + "...");
		
		ODEMReader.setTypesFromODEMFile(Config.getOdemFile());
		List<Type> allTypes = ODEMReader.getAllTypes();
		HashMap<String,Type> typeMap = new HashMap<String,Type>();
		for (Type t : allTypes) {
			typeMap.put(t.getName().trim(), t);
		}
		
		System.out.println("Reading in ground truth file: " + Config.getGroundTruthFile());
		
		
		
		if (Config.getGroundTruthFile().endsWith(".rsf")) {
			GroundTruthFileParser.parseRsf(Config.getGroundTruthFile());
		}
		else {
			GroundTruthFileParser.parseHadoopStyle(Config.getGroundTruthFile());
		}
		Set<ConcernCluster> nonPkgBasedClusters = GroundTruthFileParser.getClusters();
		
		
		StringGraph nonPkgBasedClusterGraph = ClusterUtil.buildClusterGraphUsingOdemClasses(typeMap, nonPkgBasedClusters);
		logger.debug("Printing cluster graph of hdfs and mapred...");
		logger.debug(nonPkgBasedClusterGraph);
	
		Set<String> allClasses = new HashSet<String>();
		for (Type type : allTypes) {
			allClasses.add(type.getName().trim());
		}
		Set<String> nodesInClusterGraph = ClusterUtil.getNodesInClusterGraph(nonPkgBasedClusterGraph);
		logger.debug("Number of nodes in cluster graph: " + nodesInClusterGraph.size());
		
		Set<String> classesInClusterGraph = ClusterUtil.getClassesInClusters(nonPkgBasedClusters);
		logger.debug("Number of classes in all clusters: " + classesInClusterGraph.size());
		
		Set<String> unClusteredClasses = new HashSet<String>(allClasses);
		unClusteredClasses.removeAll(classesInClusterGraph);
		
		logger.debug("Unclustered classes...");
		int classCount = 0;
		for (String c : unClusteredClasses) {
			logger.debug(classCount + ": " + c);
			classCount++;
		}
		
		Set<String> packagesOfUnclusteredClasses = new HashSet<String>();
		for (String c : unClusteredClasses) {
			packagesOfUnclusteredClasses.add(c.substring(c.indexOf("org"), c.lastIndexOf(".")));
		}
		
		logger.debug("Packages of unclustered classes");
		int pkgCount = 0;
		for (String pkg : packagesOfUnclusteredClasses) {
			logger.debug(pkgCount + ": " + pkg);
			pkgCount++;
		}
		
		Set<String> topLevelPackagesOfUnclusteredClasses = new HashSet<String>();
		String topLevelPkgPatternStr = "org\\.apache\\.hadoop\\.\\w+";
		Pattern topLevelPkgPattern = Pattern.compile(topLevelPkgPatternStr);
		
		for (String pkg : packagesOfUnclusteredClasses) {
			Matcher m = topLevelPkgPattern.matcher(pkg);
			while(m.find()) {
				topLevelPackagesOfUnclusteredClasses.add(m.group(0));
			}
		}
		
		logger.debug("Top-level packages of unclustered classes");
		pkgCount = 0;
		for (String pkg : topLevelPackagesOfUnclusteredClasses) {
			logger.debug(pkgCount + ": " + pkg);
			pkgCount++;
		}
		
		Set<ConcernCluster> pkgBasedClusters = ClusterUtil.buildGroundTruthClustersFromPackages(topLevelPackagesOfUnclusteredClasses,unClusteredClasses);
		StringGraph pkgBasedClusterGraph = ClusterUtil.buildClusterGraphUsingOdemClasses(typeMap, pkgBasedClusters);
		
		Set<ConcernCluster> allClusters = new HashSet<ConcernCluster>(nonPkgBasedClusters);
		allClusters.addAll(pkgBasedClusters);
		
		StringGraph fullClusterGraph = ClusterUtil.buildClusterGraphUsingOdemClasses(typeMap, allClusters);
		
		
		
		Set<String> twoWayClusters = new HashSet<String>();
		logger.debug("Clusters that would be merged together...");
		int mergeCount = 0;
		for (StringEdge edge : fullClusterGraph.edges) {
			StringEdge reversedEdge = new StringEdge(edge.tgtStr,edge.srcStr);
			if (fullClusterGraph.containsEdge(reversedEdge)) {
				logger.debug("\t Would be merged: " + edge.srcStr + ", " + edge.tgtStr);
				twoWayClusters.add(edge.srcStr.trim());
				twoWayClusters.add(edge.tgtStr.trim());
				mergeCount++;
			}
		}
		logger.debug("Total clusters that would be merged: " + mergeCount);
		
		logger.debug("Clusters involved in two-way associations...");
		int clusterCount = 0;
		for (String cluster : twoWayClusters) {
			logger.debug(clusterCount + ": " + cluster);
			clusterCount++;
		}
		
		Set<StringGraph> internalGraphs = ClusterUtil.buildInternalGraphs(typeMap, allClusters);
	
		String dotFileWritingMsg = "Writing out dot files for cluster graphs...";
		System.out.println(dotFileWritingMsg);
		logger.debug(dotFileWritingMsg);
		try {
			nonPkgBasedClusterGraph.writeDotFile(Config.getNonPkgBasedGroundTruthClusterGraphDotFilename());
			pkgBasedClusterGraph.writeDotFile(Config.getPkgBasedGroundTruthClusterGraphDotFilename());
			fullClusterGraph.writeDotFile(Config.getFullGroundTruthClusterGraphDotFilename());
			
			for (StringGraph graph : internalGraphs) {
				graph.writeDotFile(Config.getInternalGraphDotFilename(graph.getName()));
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String rsfFileWritingMsg = "Writing out ground truth RSF file " + Config.getGroundTruthRsfFilename() + "...";
		System.out.println(rsfFileWritingMsg);
		logger.debug(rsfFileWritingMsg);
		
		try {
			FileWriter fw = new FileWriter(Config.getGroundTruthRsfFilename());
			BufferedWriter out = new BufferedWriter(fw);
			clusterCount = 0;
			for (ConcernCluster cluster : nonPkgBasedClusters) {
				for (String entity : cluster.getEntities()) {
					String rsfLine = "contain "
							+ cluster.getName().replaceAll("[:\\s]", "_") + " "
							+ entity;
					logger.debug(rsfLine);
					out.write(rsfLine + "\n");
				}
				clusterCount++;
			}

			for (ConcernCluster cluster : pkgBasedClusters) {
				for (String entity : cluster.getEntities()) {
					String rsfLine = "contain "
							+ cluster.getName().replaceAll("[:\\s]", "_") + " "
							+ entity;
					logger.debug(rsfLine);
					out.write(rsfLine + "\n");
				}
				clusterCount++;
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
}
