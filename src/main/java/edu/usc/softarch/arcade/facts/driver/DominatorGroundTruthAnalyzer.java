package edu.usc.softarch.arcade.facts.driver;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.collections4.Factory;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.google.common.collect.Iterables;

import edu.uci.ics.jung.algorithms.shortestpath.MinimumSpanningForest;
import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.graph.Tree;
import edu.uci.ics.jung.graph.util.Pair;
import edu.usc.softarch.arcade.clustering.util.ClusterUtil;

public class DominatorGroundTruthAnalyzer {
	private static Logger logger = LogManager.getLogger(DominatorGroundTruthAnalyzer.class);
	
	static Factory<Integer> edgeFactory = new Factory<Integer>() {
		int i = 0;

		public Integer create() {
			return i++;
		}
	};

	static Factory<String> vertexFactory = new Factory<String>() {
		int i = 0;

		public String create() {
			return "V" + i++;
		}
	};
	
	public static void main(String[] args) {
		String depsFilename = args[0];
		String clustersFilename = args[1];
		String outFilename = args[2];
		
		RsfReader.loadRsfDataFromFile(depsFilename);
		List<List<String>> depFacts = RsfReader.unfilteredFacts;
		
		RsfReader.loadRsfDataFromFile(clustersFilename);
		List<List<String>> clusterFacts = RsfReader.unfilteredFacts;
		
		Map<String,Set<String>> clusterMap = ClusterUtil.buildClusterMap(clusterFacts);
		
		Map<String,Set<MutablePair<String,String>>> internalEdgeMap = ClusterUtil.buildInternalEdgesPerCluster(clusterMap, depFacts);
		
		Map<String, Double> ratioMap = computeDominatorCriteriaIndicatorValues(
				clusterMap, internalEdgeMap);
		
		try (FileWriter out = new FileWriter(outFilename)) {
			for (Entry entry : ratioMap.entrySet()) {
				out.write(entry.getKey() + "," + entry.getValue() + "\n");
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Map<String, Double> computeDominatorCriteriaIndicatorValues(
			Map<String, Set<String>> clusterMap,
			Map<String, Set<MutablePair<String, String>>> internalEdgeMap) {
		Map<String,String> topDomMap = new HashMap<>();
		Map<String,Integer> topDomCountMap = new HashMap<>();
		Map<String,Integer> numForestMap = new HashMap<>();
		Map<String,Forest<String,Integer>> forestMap = new HashMap<>();
		
		String start = "ST";
		for (String clusterName : internalEdgeMap.keySet()) {
			Set<MutablePair<String,String>> edges = internalEdgeMap.get(clusterName);
			DirectedGraph<String,Integer> graph = new DirectedSparseGraph<>();
			for (MutablePair<String,String> edge : edges) {
				String source = edge.getLeft();
				String target = edge.getRight();
				graph.addEdge(edgeFactory.create(), source, target);
			}
			
			logger.debug("Printing graph...");
			for (Integer edge : graph.getEdges()) {
				Pair<String> pair = graph.getEndpoints(edge);
				logger.debug(pair.getFirst() + ", " + pair.getSecond());
			}
			
			Set<String> verticesWithNoPreds = new HashSet<>();
			
			for (String vertex : graph.getVertices()) {
				if (graph.getPredecessorCount(vertex) == 0) {
					System.out.println(vertex + " has no predecessors");
					verticesWithNoPreds.add(vertex);
				}
			}			
			
			for (String target : verticesWithNoPreds) {
				graph.addEdge(edgeFactory.create(), start, target);
			}
			
			logger.debug("Graph with new start:");
			logger.debug(graph);
			
			MinimumSpanningForest<String,Integer> minSpanForest = new MinimumSpanningForest<>(graph,new DelegateForest<>(), start);
			
			Forest<String,Integer> forest = minSpanForest.getForest();
			
			numForestMap.put(clusterName, forest.getTrees().size());
			forestMap.put(clusterName, forest);

			Map<String, Integer> domCountMap = computeDominatorInfo(graph,
					start);
			
			String topDom = null;
			int topCount = 0;
			for (Entry<String,Integer> entry : domCountMap.entrySet()) {
				if (topDom == null) {
					topDom = entry.getKey();
					topCount = entry.getValue();
				}
				String dom = entry.getKey();
				int count = entry.getValue();
				if (count > topCount && dom.trim() != "ST") {
					topDom = dom;
					topCount = count;
				}
			}
			
			topDomMap.put(clusterName, topDom);
			topDomCountMap.put(clusterName, topCount);
			logger.debug("Top dominator other than ST is " + topDom + " : " + topCount );
			logger.debug("No. of entities of " + clusterName + ": " + clusterMap.get(clusterName).size());
		}
		
		Map<String,Integer> properDomMap = new HashMap<>();
		for (String clusterName : topDomMap.keySet()) {
			if (topDomMap.get(clusterName) != null && !topDomMap.get(clusterName).equals("ST")) {
				int currTopCount = topDomCountMap.get(clusterName);
				int clusterSize = clusterMap.get(clusterName).size();
				if ((double)currTopCount > (double)(clusterSize/2)) {
					properDomMap.put(clusterName, currTopCount);
				}
			}
		}
		
		logger.debug("Cluster with proper dominators: ");
		for (String clusterName : properDomMap.keySet()) {
			logger.debug(clusterName + ", " + topDomCountMap.get(clusterName) + ", " + clusterMap.get(clusterName).size() + ", " + topDomMap.get(clusterName)); 
		}
		
		Set<String> clustersNoPropDoms = new HashSet<>(clusterMap.keySet());
		clustersNoPropDoms.removeAll(properDomMap.keySet());
		
		logger.debug("");
		logger.debug("Clusters withOUT proper dominators: ");
		for (String clusterName : clustersNoPropDoms) {
			logger.debug(clusterName);
		}
		
		logger.debug("No. of clusters with proper dominators: " + properDomMap.keySet().size());
		logger.debug("No. of total clusters: " + clusterMap.keySet().size());
		logger.debug("Percentage of proper dominators: " + (double)(properDomMap.keySet().size())/(double)(clusterMap.keySet().size()));
		
		logger.debug("Number of trees in minimum spanning tree forest for each cluster:");
		for (Entry entry : numForestMap.entrySet()) {
			logger.debug(entry);
		}
		
		logger.debug("Number of trees in minimum spanning tree forest for each cluster:");
		for (Entry entry : numForestMap.entrySet()) {
			logger.debug(entry);
		}
		
		Map<String,Double> ratioMap = new TreeMap<>();
		logger.debug("Comparing largest tree of cluster to entities of cluster:");
		for (Entry entry : forestMap.entrySet()) {
			String clusterName = (String) entry.getKey();
			Set<String> entities = clusterMap.get(clusterName);
			Forest<String,Integer> forest = (Forest<String, Integer>) entry.getValue();
			if (forest.getTrees().size() == 0) {
				logger.debug(clusterName + " has an empty forest");
				ratioMap.put(clusterName, (double) 0);
				continue;
			}
			Tree<String,Integer> largestTree = Iterables.get(forest.getTrees(), 0);
			for (Tree<String,Integer> tree : forest.getTrees()) { // identify the largest tree
				if (tree.getVertexCount() > largestTree.getVertexCount()) {
					largestTree = tree;
				}
			}
			int largestTreeTrueSize = largestTree.containsVertex(start) ? largestTree.getVertexCount() - 1 : largestTree.getVertexCount();
			double ratio = (double) largestTreeTrueSize/(double) entities.size();
			if (ratio > 1) {
				ratio = 1;
			}
			logger.debug(clusterName + ", numEntites: " + entities.size()
					+ ", size of largest tree: " + largestTreeTrueSize
					+ ", ratio: " + ratio);
			ratioMap.put(clusterName, ratio);
		}

		int numClustersWithOneTreeInForest = 0;
		for (Entry entry : numForestMap.entrySet()) {
			if (entry.getValue().equals(1))
				numClustersWithOneTreeInForest++;
		}
		
		logger.debug("Number of clusters with a forest with only one tree: " + numClustersWithOneTreeInForest);
		return ratioMap;
	}

	private static Map<String, Integer> computeDominatorInfo(
			DirectedGraph<String, Integer> graph, String start) {	
		Map<String,Set<String>> domMap = new HashMap<>();
		Set<String> startDominators = new HashSet<>();
		startDominators.add(start);
		domMap.put(start, startDominators);
		
		Set<String> verticesMinusStart = new HashSet<>(graph.getVertices());
		verticesMinusStart.remove(start);
		
		for (String vertex : verticesMinusStart) {
			domMap.put(vertex, new HashSet<>(graph.getVertices()));
		}
		
		boolean changedDom = true;
		while (changedDom) {
			changedDom = false;
			for (String vertex : verticesMinusStart) {
				Set<String> predDomIntersection = new HashSet<>();
				if (graph.getPredecessorCount(vertex) > 0) {
					predDomIntersection.addAll(graph.getVertices());
				}
				for (String pred : graph.getPredecessors(vertex)) {
					Set<String> domOfPred = domMap.get(pred);
					predDomIntersection.retainAll(domOfPred);
				}
				Set<String> oldDomOfVertex = domMap.get(vertex); 
				Set<String> newDomOfVertex = new HashSet<>(predDomIntersection);
				newDomOfVertex.add(vertex);
				if (!newDomOfVertex.equals(oldDomOfVertex)) {
					changedDom = true;
				}
				domMap.put(vertex, newDomOfVertex);
			}
		}
		
		for (String vertex : graph.getVertices()) {
			Set<String> dominators = domMap.get(vertex);
			logger.debug("dom of " + vertex + ": " + dominators);
		}
		
		Map<String,Integer> domCountMap = new HashMap<>();
		for (String vertex : graph.getVertices()) {
			Set<String> dominators = domMap.get(vertex);
			for (String dom : dominators) {
				if (domCountMap.get(dom) == null) {
					domCountMap.put(dom, 1);
				}
				else {
					int count = domCountMap.get(dom);
					count++;
					domCountMap.put(dom,count);
				}
			}
		}
		
		for (Entry<String,Integer> entry : domCountMap.entrySet()) {
			logger.debug(entry.getKey() + " : " + entry.getValue());
		}
		return domCountMap;
	}
}
