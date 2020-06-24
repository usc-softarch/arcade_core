package edu.usc.softarch.arcade.facts.driver;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.functors.StringValueTransformer;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.xml.sax.SAXException;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;

import edu.uci.ics.jung.algorithms.shortestpath.MinimumSpanningForest;
import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Tree;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.io.GraphMLReader;
import edu.uci.ics.jung.io.GraphMLWriter;
import edu.usc.softarch.arcade.clustering.util.ClusterUtil;
import edu.usc.softarch.arcade.config.Config;

public class DominatorGroundTruthAnalyzer {

	static Logger logger = Logger.getLogger(DominatorGroundTruthAnalyzer.class);
	
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
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		PropertyConfigurator.configure(Config.getLoggingConfigFilename());
		
		String depsFilename = args[0];
		String clustersFilename = args[1];
		String outFilename = args[2];
		
		RsfReader.loadRsfDataFromFile(depsFilename);
		List<List<String>> depFacts = RsfReader.unfilteredFacts;
		
		RsfReader.loadRsfDataFromFile(clustersFilename);
		List<List<String>> clusterFacts = RsfReader.unfilteredFacts;
		
		Map<String,Set<String>> clusterMap = ClusterUtil.buildClusterMap(clusterFacts);
		
		Map<String,Set<MutablePair<String,String>>> internalEdgeMap = ClusterUtil.buildInternalEdgesPerCluster(clusterMap, depFacts);
		
		/*logger.debug("Printing internal edges of clusters: ");
		for (String clusterName : internalEdgeMap.keySet()) {
			Set<MutablePair<String,String>> edges = internalEdgeMap.get(clusterName);
			logger.debug(clusterName);
			for (MutablePair<String,String> edge : edges) { 
				logger.debug("\t" + edge);
			}
		}*/
		
		Map<String,Set<MutablePair<String,String>>> externalEdgeMap = ClusterUtil.buildExternalEdgesPerCluster(clusterMap, depFacts);
		
		/*logger.debug("Printing external edges of clusters: ");
		for (String clusterName : externalEdgeMap.keySet()) {
			Set<MutablePair<String,String>> edges = externalEdgeMap.get(clusterName);
			logger.debug(clusterName);
			for (MutablePair<String,String> edge : edges) { 
				logger.debug("\t" + edge);
			}
		}*/
		
		Map<String,Set<MutablePair<String,String>>> intoEdgeMap = ClusterUtil.buildEdgesIntoEachCluster(clusterMap, depFacts);
		
		/*logger.debug("Printing edges into of clusters: ");
		for (String clusterName : intoEdgeMap.keySet()) {
			Set<MutablePair<String,String>> edges = intoEdgeMap.get(clusterName);
			logger.debug(clusterName);
			for (MutablePair<String,String> edge : edges) { 
				logger.debug("\t" + edge);
			}
		}*/
		
		Map<String, Double> ratioMap = computeDominatorCriteriaIndicatorValues(
				clusterMap, internalEdgeMap);
		
		try {
			FileWriter out = new FileWriter(outFilename);
			for (Entry entry : ratioMap.entrySet()) {
				out.write(entry.getKey() + "," + entry.getValue() + "\n");
			}
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		//runGeneratedGraphTest(args);

	}

	public static Map<String, Double> computeDominatorCriteriaIndicatorValues(
			Map<String, Set<String>> clusterMap,
			Map<String, Set<MutablePair<String, String>>> internalEdgeMap) {
		Map<String,String> topDomMap = new HashMap<String,String>();
		Map<String,Integer> topDomCountMap = new HashMap<String,Integer>();
		Map<String,Integer> numForestMap = new HashMap<String,Integer>();
		Map<String,Forest<String,Integer>> forestMap = new HashMap<String,Forest<String,Integer>>();
		
		String start = "ST";
		for (String clusterName : internalEdgeMap.keySet()) {
			Set<MutablePair<String,String>> edges = internalEdgeMap.get(clusterName);
			DirectedGraph<String,Integer> graph = new DirectedSparseGraph<String,Integer>();
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
			
			//DirectedGraph<String,Integer> origGraph = dupeDirectedSparseGraph(graph);
			
			Set<String> verticesWithNoPreds = new HashSet<String>();
			
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
			
			MinimumSpanningForest<String,Integer> minSpanForest = new MinimumSpanningForest<String,Integer>(graph,new DelegateForest<String,Integer>(), start);
			
			Forest<String,Integer> forest = minSpanForest.getForest();
			
			numForestMap.put(clusterName, forest.getTrees().size());
			forestMap.put(clusterName, forest);
			 
			//start = setStartVertexToFirstWithNoPredecessors(graph, vertices, start);

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
		
		int properDomCount = 0;
		Map<String,Integer> properDomMap = new HashMap<String,Integer>();
		for (String clusterName : topDomMap.keySet()) {
			if (topDomMap.get(clusterName) != null && !topDomMap.get(clusterName).equals("ST")) {
				int currTopCount = topDomCountMap.get(clusterName);
				int clusterSize = clusterMap.get(clusterName).size();
				if ((double)currTopCount > (double)(clusterSize/2)) {
					properDomCount++;
					properDomMap.put(clusterName, currTopCount);
				}
			}
		}
		
		logger.debug("Cluster with proper dominators: ");
		for (String clusterName : properDomMap.keySet()) {
			logger.debug(clusterName + ", " + topDomCountMap.get(clusterName) + ", " + clusterMap.get(clusterName).size() + ", " + topDomMap.get(clusterName)); 
		}
		
		Set<String> clustersNoPropDoms = new HashSet<String>(clusterMap.keySet());
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
		
		Map<String,Double> ratioMap = new TreeMap<String,Double>();
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
		Map<String,Set<String>> domMap = new HashMap<String,Set<String>>();
		Set<String> startDominators = new HashSet<String>();
		startDominators.add(start);
		domMap.put(start, startDominators);
		
		Set<String> verticesMinusStart = new HashSet<String>(graph.getVertices());
		verticesMinusStart.remove(start);
		
		for (String vertex : verticesMinusStart) {
			domMap.put(vertex, new HashSet<String>(graph.getVertices()));
		}
		
		boolean changedDom = true;
		while (changedDom) {
			changedDom = false;
			for (String vertex : verticesMinusStart) {
				Set<String> predDomIntersection = new HashSet<String>();
				if (graph.getPredecessorCount(vertex) > 0) {
					predDomIntersection.addAll(graph.getVertices());
				}
				for (String pred : graph.getPredecessors(vertex)) {
					Set<String> domOfPred = domMap.get(pred);
					predDomIntersection.retainAll(domOfPred);
				}
				Set<String> oldDomOfVertex = domMap.get(vertex); 
				Set<String> newDomOfVertex = new HashSet<String>(predDomIntersection);
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
		
		Map<String,Integer> domCountMap = new HashMap<String,Integer>();
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
	
	private static <V, E> DirectedGraph<V, E> dupeDirectedSparseGraph(DirectedGraph<V, E> src) {
		DirectedGraph<V, E> dst = new DirectedSparseGraph<V,E>();

		for (V v : src.getVertices())
			dst.addVertex(v);

		for (E e : src.getEdges())
			dst.addEdge(e, src.getIncidentVertices(e));
		
		return dst;
	}

	private static void runGeneratedGraphTest(String[] args) {
		String filename = "/home/joshua/workspace/MyExtractors/data/generated/s2v1e5.graphml";
		File graphMLFile = new File(filename);
		System.out.println(graphMLFile.getParentFile());
		System.out.println(graphMLFile.getName());
		
		try {
			GraphMLReader gmlReader = new GraphMLReader();
			DirectedGraph<String,Integer> graph = new DirectedSparseMultigraph<String,Integer>();
			gmlReader.load(filename,graph);
			System.out.println(graph);
			
			String start = "ST";
			 
			//start = setStartVertexToFirstWithNoPredecessors(graph, vertices, start);
			
			Set<String> verticesWithNoPreds = new HashSet<String>();
			for (String vertex : graph.getVertices()) {
				if (graph.getPredecessorCount(vertex) == 0) {
					System.out.println(vertex + " has no predecessors");
					verticesWithNoPreds.add(vertex);
				}
			}
			
			
			for (String target : verticesWithNoPreds) {
				graph.addEdge(graph.getEdgeCount(), start, target);
			}
			
			System.out.println("Graph with new start:");
			System.out.println(graph);
		
			
			Map<String,Set<String>> domMap = new HashMap<String,Set<String>>();
			Set<String> startDominators = new HashSet<String>();
			startDominators.add(start);
			domMap.put(start, startDominators);
			
			Set<String> verticesMinusStart = new HashSet<String>(graph.getVertices());
			verticesMinusStart.remove(start);
			
			for (String vertex : verticesMinusStart) {
				domMap.put(vertex, new HashSet<String>(graph.getVertices()));
			}
			
			boolean changedDom = true;
			while (changedDom) {
				changedDom = false;
				for (String vertex : verticesMinusStart) {
					Set<String> predDomIntersection = new HashSet<String>();
					if (graph.getPredecessorCount(vertex) > 0) {
						predDomIntersection.addAll(graph.getVertices());
					}
					for (String pred : graph.getPredecessors(vertex)) {
						Set<String> domOfPred = domMap.get(pred);
						predDomIntersection.retainAll(domOfPred);
					}
					Set<String> oldDomOfVertex = domMap.get(vertex); 
					Set<String> newDomOfVertex = new HashSet<String>(predDomIntersection);
					newDomOfVertex.add(vertex);
					if (!newDomOfVertex.equals(oldDomOfVertex)) {
						changedDom = true;
					}
					domMap.put(vertex, newDomOfVertex);
				}
			}
			
			for (String vertex : graph.getVertices()) {
				Set<String> dominators = domMap.get(vertex);
				System.out.println("dom of " + vertex + ": " + dominators);
			}
			
			Map<String,Integer> domCountMap = new HashMap<String,Integer>();
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
				System.out.println(entry.getKey() + " : " + entry.getValue());
			}
			
			String topDom = null;
			int topCount = 0;
			for (Entry<String,Integer> entry : domCountMap.entrySet()) {
				if (topDom == null) {
					topDom = entry.getKey();
					topCount = entry.getValue();
				}
				String dom = entry.getKey();
				int count = entry.getValue();
				if (count > topCount && dom != "ST") {
					topDom = dom;
					topCount = count;
				}
			}
			
			System.out.println("Top dominator other than ST is " + topDom + " : " + topCount );
			
			
			String[] tokens = graphMLFile.getName().toString().split("\\.");
			String outFilename = graphMLFile.getParentFile() + File.separator + tokens[0] + "_special_start." + tokens[1];
			System.out.println("output graphml filename: " + outFilename);
			FileWriter fileWriter = new FileWriter(outFilename);
			GraphMLWriter gmlWriter = new GraphMLWriter();
			gmlWriter.setEdgeIDs(StringValueTransformer.getInstance());
			gmlWriter.save(graph,fileWriter);
			
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static String setStartVertexToFirstWithNoPredecessors(
			DirectedGraph<String, Integer> graph, Set<String> vertices,
			String start) {
		for (String vertex : vertices) {
			System.out.println(vertex + " predecessor count: " + graph.getPredecessorCount(vertex));
			if (graph.getPredecessorCount(vertex) == 0) {
				start = vertex;
				System.out.println("Start node is " + start);
			}
			
		}
		return start;
	}

}
