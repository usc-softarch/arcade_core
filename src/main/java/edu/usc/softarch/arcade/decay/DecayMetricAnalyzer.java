package edu.usc.softarch.arcade.decay;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import edu.usc.softarch.arcade.antipattern.detection.Smell;
import edu.usc.softarch.arcade.clustering.StringGraph;
import edu.usc.softarch.arcade.clustering.util.ClusterUtil;
import edu.usc.softarch.arcade.config.Config;
import edu.usc.softarch.arcade.facts.ConcernCluster;
import edu.usc.softarch.arcade.facts.driver.ConcernClusterRsf;
import edu.usc.softarch.arcade.facts.driver.RsfReader;
import edu.usc.softarch.arcade.util.FileUtil;
import edu.usc.softarch.arcade.util.LogUtil;

public class DecayMetricAnalyzer {
	static Logger logger = Logger.getLogger(DecayMetricAnalyzer.class);
	public static Double rciVal;
	public static double twoWayPairRatio;
	public static double avgStability;
	public static double mqRatio;

	public static void main(String[] args) {
		PropertyConfigurator.configure(Config.getLoggingConfigFilename());
		
		rciVal = null;
		twoWayPairRatio = -1;
		avgStability = -1;
		mqRatio = -1;
		
		String clustersFilename = FileUtil.tildeExpandPath(args[0]);
		String depsRsfFilename = FileUtil.tildeExpandPath(args[1]);
		String readingClustersFile = "Reading in clusters file: " + clustersFilename;
		System.out.println(readingClustersFile);
		logger.info(readingClustersFile);
		
		Set<ConcernCluster> clusters = ConcernClusterRsf.extractConcernClustersFromRsfFile(clustersFilename);
		
		boolean showBuiltClusters = true;
		if (showBuiltClusters) {
			logger.debug("Found and built clusters:");
			for (ConcernCluster cluster : clusters) {
				logger.debug(cluster.getName());
			}
		}
		
		Map<String,Set<String>> clusterSmellMap = new HashMap<String,Set<String>>();
		
		String readingDepsFile = "Reading in deps file: " + depsRsfFilename;
		System.out.println(readingDepsFile);
		logger.info(readingDepsFile);
		Map<String, Set<String>> depMap = ClusterUtil.buildDependenciesMap(depsRsfFilename);
		
		StringGraph clusterGraph = ClusterUtil.buildClusterGraphUsingDepMap(depMap,clusters);
		
		SimpleDirectedGraph<String, DefaultEdge> directedGraph = ClusterUtil.buildConcernClustersDiGraph(
				clusters, clusterGraph);
		
		Map<String, Double> decayMetrics = new LinkedHashMap<String,Double>();
		rciVal = detectRci(directedGraph);
		
		logger.info("rci: " + rciVal);
		
		
		Set<Set<String>> twoWayPairs = detectTwoWayDeps(directedGraph);
		twoWayPairRatio = (double)twoWayPairs.size()/(double)(combinations(directedGraph.vertexSet().size(),2));
		logger.info("no. of two-way pairs: " + twoWayPairs.size());
		logger.info("no. of two-way pairs / all possible pairs: " + twoWayPairRatio);
		
		avgStability = detectStability(directedGraph);
		
		logger.info("avg stability: " + avgStability);
		
		RsfReader.loadRsfDataFromFile(depsRsfFilename);
		List<List<String>> depFacts = RsfReader.unfilteredFacts;
		RsfReader.loadRsfDataFromFile(clustersFilename);
		List<List<String>> clusterFacts = RsfReader.unfilteredFacts;
		Map<String,Set<String>> clusterMap = ClusterUtil.buildClusterMap(clusterFacts);
		Map<String,Set<MutablePair<String,String>>> internalEdgeMap = ClusterUtil.buildInternalEdgesPerCluster(clusterMap, depFacts);
		Map<String,Set<MutablePair<String,String>>> externalEdgeMap = ClusterUtil.buildExternalEdgesPerCluster(clusterMap, depFacts);
		Map<String,Set<MutablePair<String,String>>> intoEdgeMap = ClusterUtil.buildEdgesIntoEachCluster(clusterMap, depFacts);
		
		Map<String,Double> clusterFactors = new LinkedHashMap<String,Double>();
		for (ConcernCluster cluster : clusters) {
			Set<MutablePair<String,String>> internalEdges = internalEdgeMap.get(cluster.getName());
			Set<MutablePair<String,String>> externalEdges = externalEdgeMap.get(cluster.getName());
			if (internalEdges.size() == 0) {
				clusterFactors.put(cluster.getName(),new Double(0));
			}
			else {
				double cf = (double)(2*internalEdges.size())/(2*internalEdges.size()+externalEdges.size()); 
				clusterFactors.put(cluster.getName(), cf);
			}		
		}
		
		double mq = 0;
		for (Double cf : clusterFactors.values()) {
			mq += cf;
		}
		mqRatio = mq/(double)clusters.size();
		
		logger.info("MQ: " + mq);
		logger.info("# of clusters: " + clusters.size());
		logger.info("MQ ratio: " + mqRatio);
		
		System.out.println("Wrote decay metrics to: ");
		LogUtil.printLogFiles();
		logger.info("");
		
		//computeMq(clusters,depsRsf)2*internalEdges.size()
		

	}
	
	private static double detectStability(
			SimpleDirectedGraph<String, DefaultEdge> directedGraph) {
		Set<String> vertices = directedGraph.vertexSet();
		Map<String,Double> stabilityMap = new LinkedHashMap<String,Double>();
		double stabilitySum = 0;
		for (String vertex : vertices) {
			Set<DefaultEdge> incomingEdges = directedGraph.incomingEdgesOf(vertex);
			Set<DefaultEdge> outgoingEdges = directedGraph.outgoingEdgesOf(vertex);
			int denom = incomingEdges.size()+outgoingEdges.size();
			double stability = 0;
			
			if (denom !=0) {
				stability = (double)incomingEdges.size()/(double)(denom);
			}
			stabilityMap.put(vertex, stability);
			
			stabilitySum += stability;
		}
		
		double avgStability = stabilitySum/vertices.size();
		return avgStability;

	}

	static long combinations(int n, int k) {
		long coeff = 1;
		for (int i = n - k + 1; i <= n; i++) {
			coeff *= i;
		}
		for (int i = 1; i <= k; i++) {
			coeff /= i;
		}
		return coeff;
	}
	
	private static Set<Set<String>> detectTwoWayDeps(
			SimpleDirectedGraph<String, DefaultEdge> directedGraph) {
		Set<Set<String>> twoWayPairs = new LinkedHashSet<Set<String>>();
		
		Set<DefaultEdge> actualEdges = directedGraph.edgeSet();		
		for (DefaultEdge edge : actualEdges) {
			String sourceCluster = directedGraph.getEdgeSource(edge);
			String targetCluster = directedGraph.getEdgeTarget(edge);
			if (directedGraph.containsEdge(targetCluster, sourceCluster)) {
				Set<String> twoWayPair = new HashSet<String>();
				twoWayPair.add(sourceCluster);
				twoWayPair.add(targetCluster);
				twoWayPairs.add(twoWayPair);
			}
		}
		
		return twoWayPairs;
	}

	private static double detectRci(SimpleDirectedGraph<String, DefaultEdge> directedGraph) {
		
		Set<DefaultEdge> actualEdges = directedGraph.edgeSet();
		Set<String> vertices = directedGraph.vertexSet();
		
		int potentialEdgeCount = vertices.size()*(vertices.size()-1);
		logger.debug("# actual edges: " + actualEdges.size());
		logger.debug("# potential edges: " + potentialEdgeCount);
		double rciVal = (double)actualEdges.size()/(double)potentialEdgeCount;
		
		return rciVal;
	}

}
