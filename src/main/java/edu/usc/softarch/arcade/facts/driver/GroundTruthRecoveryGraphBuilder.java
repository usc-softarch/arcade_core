package edu.usc.softarch.arcade.facts.driver;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import edu.usc.softarch.arcade.classgraphs.StringEdge;
import edu.usc.softarch.arcade.clustering.StringGraph;
import edu.usc.softarch.arcade.clustering.ClusterUtil;
import edu.usc.softarch.arcade.facts.ConcernCluster;
import edu.usc.softarch.arcade.facts.GroundTruthFileParser;
import edu.usc.softarch.extractors.cda.odem.Type;

public class GroundTruthRecoveryGraphBuilder {
	private static Logger logger =
		LogManager.getLogger(GroundTruthRecoveryGraphBuilder.class);
	
	/**
	 * Arguments are:
	 * ODEMFile GroundTruthFile ProjectName OutputDirectory
	 */
	public static void main(String[] args) {
		String odemFilePath = args[0];
		String groundTruthFilePath = args[1];
		String projectName = args[2];
		String outputDirectory = args[3];
		String nonPkgBasedGtcg = outputDirectory
			+ projectName + "_non_pkg_based_ground_truth_cluster_graph.dot";
		String pkgBasedGtcg = outputDirectory
			+ projectName + "_pkg_based_ground_truth_cluster_graph.dot";
		String fullGtcg = outputDirectory
			+ projectName + "_full_ground_truth_cluster_graph.dot";
		String groundTruthOutputPath =
			outputDirectory + projectName + "_ground_truth.rsf";
		
		System.out.println("Reading in odem file " + odemFilePath  + "...");
		
		ODEMReader.setTypesFromODEMFile(odemFilePath);
		List<Type> allTypes = ODEMReader.getAllTypes();
		Map<String,Type> typeMap = new HashMap<>();
		for (Type t : allTypes)
			typeMap.put(t.getName().trim(), t);
		
		System.out.println("Reading in ground truth file: "	+ groundTruthFilePath);
		
		if (groundTruthFilePath.endsWith(".rsf"))
			GroundTruthFileParser.parseRsf(groundTruthFilePath);
		else
			GroundTruthFileParser.parseHadoopStyle(groundTruthFilePath);
		Set<ConcernCluster> nonPkgBasedClusters = GroundTruthFileParser.getClusters();
		
		StringGraph nonPkgBasedClusterGraph =
			ClusterUtil.buildClusterGraphUsingOdemClasses(typeMap, nonPkgBasedClusters);
		logger.debug("Printing cluster graph of hdfs and mapred...");
		logger.debug(nonPkgBasedClusterGraph);
	
		Set<String> allClasses = new HashSet<>();
		for (Type type : allTypes)
			allClasses.add(type.getName().trim());
		Set<String> nodesInClusterGraph =
			ClusterUtil.getNodesInClusterGraph(nonPkgBasedClusterGraph);
		logger.debug("Number of nodes in cluster graph: "
			+ nodesInClusterGraph.size());
		
		Set<String> classesInClusterGraph =
			ClusterUtil.getClassesInClusters(nonPkgBasedClusters);
		logger.debug("Number of classes in all clusters: "
			+ classesInClusterGraph.size());
		
		Set<String> unClusteredClasses = new HashSet<>(allClasses);
		unClusteredClasses.removeAll(classesInClusterGraph);
		
		logger.debug("Unclustered classes...");
		int classCount = 0;
		for (String c : unClusteredClasses) {
			logger.debug(classCount + ": " + c);
			classCount++;
		}
		
		Set<String> packagesOfUnclusteredClasses = new HashSet<>();
		for (String c : unClusteredClasses)
			packagesOfUnclusteredClasses.add(
				c.substring(c.indexOf("org"), c.lastIndexOf(".")));
		
		logger.debug("Packages of unclustered classes");
		int pkgCount = 0;
		for (String pkg : packagesOfUnclusteredClasses) {
			logger.debug(pkgCount + ": " + pkg);
			pkgCount++;
		}
		
		Set<String> topLevelPackagesOfUnclusteredClasses = new HashSet<>();
		String topLevelPkgPatternStr = "org\\.apache\\.hadoop\\.\\w+";
		Pattern topLevelPkgPattern = Pattern.compile(topLevelPkgPatternStr);
		
		for (String pkg : packagesOfUnclusteredClasses) {
			Matcher m = topLevelPkgPattern.matcher(pkg);
			while(m.find())
				topLevelPackagesOfUnclusteredClasses.add(m.group(0));
		}
		
		logger.debug("Top-level packages of unclustered classes");
		pkgCount = 0;
		for (String pkg : topLevelPackagesOfUnclusteredClasses) {
			logger.debug(pkgCount + ": " + pkg);
			pkgCount++;
		}
		
		Set<ConcernCluster> pkgBasedClusters = ClusterUtil.buildGroundTruthClustersFromPackages(topLevelPackagesOfUnclusteredClasses,unClusteredClasses);
		StringGraph pkgBasedClusterGraph = ClusterUtil.buildClusterGraphUsingOdemClasses(typeMap, pkgBasedClusters);
		
		Set<ConcernCluster> allClusters = new HashSet<>(nonPkgBasedClusters);
		allClusters.addAll(pkgBasedClusters);
		
		StringGraph fullClusterGraph =
			ClusterUtil.buildClusterGraphUsingOdemClasses(typeMap, allClusters);
		
		Set<String> twoWayClusters = new HashSet<>();
		logger.debug("Clusters that would be merged together...");
		int mergeCount = 0;
		for (StringEdge edge : fullClusterGraph.edges) {
			StringEdge reversedEdge = new StringEdge(edge.getTgtStr(),edge.getSrcStr());
			if (fullClusterGraph.containsEdge(reversedEdge)) {
				logger.debug("\t Would be merged: " + edge.getSrcStr() + ", " + edge.getTgtStr());
				twoWayClusters.add(edge.getSrcStr().trim());
				twoWayClusters.add(edge.getTgtStr().trim());
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
			nonPkgBasedClusterGraph.writeDotFile(nonPkgBasedGtcg);
			pkgBasedClusterGraph.writeDotFile(pkgBasedGtcg);
			fullClusterGraph.writeDotFile(fullGtcg);
			
			for (StringGraph graph : internalGraphs)
				graph.writeDotFile(
					getInternalGraphDotFilename(graph.getName(), outputDirectory));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		String rsfFileWritingMsg = "Writing out ground truth RSF file " +
			groundTruthOutputPath + "...";
		System.out.println(rsfFileWritingMsg);
		logger.debug(rsfFileWritingMsg);
		
		try (BufferedWriter out = new BufferedWriter(
				new FileWriter(groundTruthOutputPath))) {
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
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String getInternalGraphDotFilename(
			String clusterName, String outputDirectory) {
		String cleanClusterName = clusterName.replaceAll("[\\/:*?\"<>|\\s]","_");
		return outputDirectory + cleanClusterName + "_internal_cluster_graph.dot";
	}
}