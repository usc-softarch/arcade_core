package edu.usc.softarch.arcade.clustering.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import edu.usc.softarch.arcade.classgraphs.StringEdge;
import edu.usc.softarch.arcade.clustering.FastCluster;
import edu.usc.softarch.arcade.clustering.FastFeatureVectors;
import edu.usc.softarch.arcade.clustering.StringGraph;
import edu.usc.softarch.arcade.config.Config;
import edu.usc.softarch.arcade.facts.ConcernCluster;
import edu.usc.softarch.arcade.facts.driver.RsfReader;
import edu.usc.softarch.arcade.topics.DistributionSizeMismatchException;
import edu.usc.softarch.arcade.topics.DocTopicItem;
import edu.usc.softarch.arcade.topics.TopicUtil;
import edu.usc.softarch.arcade.topics.UnmatchingDocTopicItemsException;
import edu.usc.softarch.extractors.cda.odem.Dependencies;
import edu.usc.softarch.extractors.cda.odem.DependsOn;
import edu.usc.softarch.extractors.cda.odem.Type;

/**
 * @author joshua
 */
public class ClusterUtil {
	private static Logger logger = LogManager.getLogger(ClusterUtil.class);

	public static StringGraph generateFastClusterGraph(
			Collection<FastCluster> splitClusters,
			List<String> namesInFeatureSet) {
		boolean debugMethod = false;
		StringGraph clusterGraph = new StringGraph();
		for (FastCluster c1 : splitClusters) {
			for (FastCluster c2 : splitClusters) {
				Set<Integer> c1Keys = c1.getNonZeroFeatureMap().keySet();
				for (Integer key : c1Keys) {
					Double c1FeatureValue = c1.getNonZeroFeatureMap().get(key);
					String c1FeatureName = namesInFeatureSet.get(key);
					String[] c2Entities = c2.getName().split(",");
					for (String c2EntityName : c2Entities) {
						if (debugMethod) {
							logger.debug("c1FeatureName: " + c1FeatureName
									+ ", c1FeatureValue: " + c1FeatureValue);
							logger.debug("c2EntityName: " + c2EntityName);
						}

						if (c1FeatureName.equals(c2EntityName)) {
							logger.trace("Adding edge (" + c1.getName() + ","
									+ c2.getName() + ")");
							clusterGraph.addEdge(new StringEdge(c1.getName(),
									c2.getName()));
						}
					}
				}
			}
		}
		return clusterGraph;
	}

	public static double computeCentroidUsingStructuralData(FastCluster cluster) {

		double centroidSum = 0;
		Set<Integer> clusterKeys = cluster.getNonZeroFeatureMap().keySet();

		for (Integer key : clusterKeys) {
			centroidSum += cluster.getNonZeroFeatureMap().get(key)
					.doubleValue();
		}

		double centroidAvg = centroidSum / cluster.getFeaturesLength();

		// centroid
		return centroidAvg / cluster.getNumEntities();
	}

	public static double computeGlobalCentroidForStructuralData(
			List<Double> clusterCentroids) {

		double centroidSum = 0;

		for (Double centroid : clusterCentroids) {
			centroidSum += centroid.doubleValue();
		}

		return centroidSum / clusterCentroids.size();
	}

	public static double computeClusterGainUsingStructuralDataFromFastFeatureVectors(
			List<FastCluster> fastClusters) {
		ArrayList<Double> clusterCentroids = new ArrayList<>();

		for (FastCluster cluster : fastClusters) {
			double centroid = computeCentroidUsingStructuralData(cluster);
			clusterCentroids.add(centroid);
		}

		double globalCentroid = computeGlobalCentroidForStructuralData(clusterCentroids);

		double clusterGain = 0;
		for (int i = 0; i < clusterCentroids.size(); i++) {
			clusterGain += (fastClusters.get(i).getNumEntities() - 1)
					* Math.pow(
							Math.abs(globalCentroid
									- clusterCentroids.get(i).doubleValue()), 2);
		}

		return clusterGain;
	}

	public static DocTopicItem computeGlobalCentroidUsingTopics(
			List<DocTopicItem> docTopicItems) {
		int firstNonNullDocTopicItemIndex = 0;
		for (; docTopicItems.get(firstNonNullDocTopicItemIndex) == null
				&& firstNonNullDocTopicItemIndex < docTopicItems.size(); firstNonNullDocTopicItemIndex++) {
		}
		DocTopicItem mergedDocTopicItem = new DocTopicItem(
				docTopicItems.get(firstNonNullDocTopicItemIndex));
		for (int i = firstNonNullDocTopicItemIndex; i < docTopicItems.size(); i++) {
			if (docTopicItems.get(i) == null)
				continue;
			DocTopicItem currDocTopicItem = docTopicItems.get(i);
			try {
				mergedDocTopicItem = TopicUtil.mergeDocTopicItems(
					mergedDocTopicItem, currDocTopicItem);
			} catch (UnmatchingDocTopicItemsException e) {
				e.printStackTrace(); //TODO handle it
			}
		}
		return mergedDocTopicItem;
	}

	public static double computeClusterGainUsingTopics(
			List<FastCluster> clusters) {
		ArrayList<DocTopicItem> docTopicItems = new ArrayList<>();
		for (FastCluster c : clusters) {
			docTopicItems.add(c.docTopicItem);
		}
		DocTopicItem globalDocTopicItem = computeGlobalCentroidUsingTopics(docTopicItems);
		logger.debug("Global Centroid Using Topics: "
				+ globalDocTopicItem.toStringWithLeadingTabsAndLineBreaks(0));

		double clusterGain = 0;

		for (int i = 0; i < docTopicItems.size(); i++) {
			try {
				clusterGain += (clusters.get(i).getNumEntities() - 1)
					* TopicUtil.jsDivergence(docTopicItems.get(i), globalDocTopicItem);
			} catch (DistributionSizeMismatchException e) {
				e.printStackTrace(); //TODO handle it
			}
		}

		return clusterGain;
	}

	public static Map<String, Integer> createFastClusterNameToNodeNumberMap(
			List<FastCluster> clusters) {
		HashMap<String, Integer> clusterNameToNodeNumberMap = new HashMap<>();
		for (int i = 0; i < clusters.size(); i++) {
			FastCluster cluster = clusters.get(i);
			clusterNameToNodeNumberMap.put(cluster.getName(), Integer.valueOf(i));
		}
		return clusterNameToNodeNumberMap;
	}

	public static Map<Integer, String> createNodeNumberToFastClusterNameMap(
			List<FastCluster> clusters,
			Map<String, Integer> clusterNameToNodeNumberMap) {
		Map<Integer, String> nodeNumberToClusterNameMap = new TreeMap<>();

		for (FastCluster cluster : clusters) {
			nodeNumberToClusterNameMap.put(
					clusterNameToNodeNumberMap.get(cluster.getName()),
					cluster.getName());
		}

		return nodeNumberToClusterNameMap;
	}

	public static void writeFastClusterRSFFileUsingConfigName(
			Map<String, Integer> clusterNameToNodeNumberMap,
			List<FastCluster> clusters) throws FileNotFoundException {
		String currentClustersDetailedRsfFilename = Config.getClustersRSFFilename(clusters.size());
		writeFastClustersRsfFile(clusterNameToNodeNumberMap, clusters,
				currentClustersDetailedRsfFilename);
	}

	public static void writeFastClustersRsfFile(
			Map<String, Integer> clusterNameToNodeNumberMap,
			List<FastCluster> clusters,
			String currentClustersDetailedRsfFilename)
			throws FileNotFoundException {
		File rsfFile = new File(currentClustersDetailedRsfFilename);

		FileOutputStream fos = new FileOutputStream(rsfFile);
		OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
		PrintWriter out = new PrintWriter(osw);

		logger.trace("Printing each cluster and its leaves...");
		for (FastCluster cluster : clusters) {
			Integer currentNodeNumber = clusterNameToNodeNumberMap.get(cluster
					.getName());
			logger.trace("Cluster name: " + currentNodeNumber);
			logger.trace("Cluster node number: " + cluster);
			String[] entities = cluster.getName().split(",");
			Set<String> entitiesSet = new HashSet<>( Arrays.asList(entities) );
			int entityCount = 0;
			for (String entity : entitiesSet) {
				logger.trace(entityCount + ":\t" + entity);
				out.println("contain " + currentNodeNumber + " " + entity);
				entityCount++;
			}
		}

		out.close();
	}

	public static void printSimilarFeatures(FastCluster c1, FastCluster c2,
			FastFeatureVectors fastFeatureVectors) {
		List<String> names = fastFeatureVectors.getNamesInFeatureSet();

		String c1LimitedName = c1.getName();
		String c2LimitedName = c2.getName();

		logger.debug("Features shared between " + c1LimitedName + " and "
				+ c2LimitedName);

		Set<Integer> c1Keys = c1.getNonZeroFeatureMap().keySet();

		for (Integer key : c1Keys) {
			if (c1.getNonZeroFeatureMap().get(key) != null
					&& c2.getNonZeroFeatureMap().get(key) != null) {
				logger.debug(names.get(key));
			}
		}

	}

	public static Set<String> getNodesInClusterGraph(StringGraph cg) {
		Set<String> nodes = new HashSet<>();
		for (StringEdge edge : cg.edges) {
			nodes.add(edge.getSrcStr().trim());
			nodes.add(edge.getTgtStr().trim());
		}
		return nodes;
	}

	public static Set<String> getClassesInClusters(
			Set<ConcernCluster> clusters) {
		Set<String> classes = new HashSet<>();
		for (ConcernCluster cluster : clusters) {
			for (String entity : cluster.getEntities()) {
				classes.add(entity.trim());
			}
		}
		return classes;
	}
	
	public static Set<StringGraph> buildInternalGraphs(Map<String, Type> typeMap,
			Set<ConcernCluster> clusters) {
		Set<StringGraph> graphs = new HashSet<>();
		for (ConcernCluster cluster : clusters) {
			StringGraph currGraph = new StringGraph(cluster.getName().trim());
			for (String entity : cluster.getEntities()) {
				Type type = typeMap.get(entity.trim());
				if (type != null) {
					Dependencies dependencies = type.getDependencies();
					for (DependsOn dependency : dependencies.getDependsOn()) {
						for (String otherEntity : cluster.getEntities()) {
							if (!entity.equals(otherEntity)) {
								if (otherEntity.trim().equals(
										dependency.getName().trim())) {
									StringEdge newEdge = new StringEdge(entity.trim(), otherEntity.trim());
									newEdge.setType(dependency.getClassification());
									currGraph.addEdge(newEdge);
								}
							}
						}
					}
				}
					
				
			}
			graphs.add(currGraph);
		}
		return graphs;
	}
	
	public static StringGraph buildClusterGraphUsingDepMap(Map<String,Set<String>> depMap,
			Set<ConcernCluster> clusters) {
		StringGraph cg = new StringGraph();
		for (ConcernCluster cluster : clusters) {
			for (String entity : cluster.getEntities()) {
				if (depMap.containsKey(entity.trim())) {
					Set<String> dependencies = depMap.get(entity);
					for (String dependency : dependencies) {
						for (ConcernCluster otherCluster : clusters) {
							for (String otherEntity : otherCluster
									.getEntities()) {
								if (otherEntity.trim()
										.equals(dependency.trim())) {
									cg.addEdge(cluster.getName().trim(),
											otherCluster.getName().trim());
								}
							}
						}
					}
				}
			}
		}
		
		return cg;
	}
	
	public static StringGraph buildClusterGraphUsingOdemClasses(Map<String, Type> typeMap,
			Set<ConcernCluster> clusters) {
		StringGraph cg = new StringGraph();
		for (ConcernCluster cluster : clusters) {
			for (String entity : cluster.getEntities()) {
				Type type = typeMap.get(entity.trim());
				if (type != null) {
					Dependencies dependencies = type.getDependencies();
					for (DependsOn dependency : dependencies.getDependsOn()) {
						for (ConcernCluster otherCluster : clusters) {
							for (String otherEntity : otherCluster
									.getEntities()) {
								if (otherEntity.trim().equals(
										dependency.getName().trim())) {
									cg.addEdge(cluster.getName().trim(),
											otherCluster.getName().trim());
								}
							}
						}
					}
				}
			}
		}
		
		
		
		return cg;
	}

	public static Set<ConcernCluster> buildGroundTruthClustersFromPackages(
			Set<String> topLevelPackagesOfUnclusteredClasses,
			Set<String> unClusteredClasses) {
		Set<ConcernCluster> clusters = new HashSet<>();
		for (String pkg : topLevelPackagesOfUnclusteredClasses) {
			ConcernCluster cluster = new ConcernCluster();
			cluster.setName(pkg.trim());
			for (String clazz : unClusteredClasses) {
				if (clazz.trim().startsWith(pkg.trim())) {
					cluster.addEntity(clazz);
				}
			}
			clusters.add(cluster);
		}
		return clusters;
	}
	
	/**
	 * Creates a map of a cluster name to its entities
	 * 
	 * @param clusterFacts
	 * @return
	 */
	public static Map<String,Set<String>> buildClusterMap(List<List<String>> clusterFacts ) {
		
		Map<String,Set<String>> clusterMap = new HashMap<>();
		
		for (List<String> fact : clusterFacts) {
			String clusterName = fact.get(1);
			String entity = fact.get(2);
			if (clusterMap.get(clusterName) == null) {
				Set<String> entities = new HashSet<>();
				entities.add(entity);
				clusterMap.put(clusterName,entities);
			}
			else {
				Set<String> entities = clusterMap.get(clusterName);
				entities.add(entity);
				clusterMap.put(clusterName,entities);
			}
		}
		
		logger.trace("Resulting clusterMap:");
		for (Entry<String,Set<String>> entry : clusterMap.entrySet()) {
			logger.trace(entry.getKey());
			for (String entity : entry.getValue()) {
				logger.trace("\t" + entity);
			}
		}
		
		return clusterMap;
		
	}
	
	public static Map<String,Set<MutablePair<String,String>>> buildInternalEdgesPerCluster(Map<String,Set<String>> clusterMap, List<List<String>> depFacts) {
		Map<String,Set<MutablePair<String,String>>> map = new HashMap<>();

		for (String clusterName : clusterMap.keySet()) { // for each cluster name
			Set<MutablePair<String,String>> edges = new HashSet<>();
			for (List<String> depFact : depFacts) {
				String source = depFact.get(1);
				String target = depFact.get(2);
				if (clusterMap.get(clusterName).contains(source) && clusterMap.get(clusterName).contains(target)) { // check if the source and target is in the cluster
					// Add internal edge 
					MutablePair<String, String> edge = new MutablePair<>();
					edge.setLeft(source);
					edge.setRight(target);
					edges.add(edge);
				}
			}
			map.put(clusterName, edges);
		}


		return map;
	}
	
	public static Map<String,Set<MutablePair<String,String>>> buildExternalEdgesPerCluster(Map<String,Set<String>> clusterMap, List<List<String>> depFacts) {
		Map<String,Set<MutablePair<String,String>>> map = new HashMap<>();

		for (String clusterName : clusterMap.keySet()) { // for each cluster name
			Set<MutablePair<String,String>> edges = new HashSet<>();
			for (List<String> depFact : depFacts) {
				String source = depFact.get(1);
				String target = depFact.get(2);
				if (clusterMap.get(clusterName).contains(source) && !(clusterMap.get(clusterName).contains(target)) ) { // source is in cluster, but target is not
					// Add external edge 
					MutablePair<String, String> edge = new MutablePair<>();
					edge.setLeft(source);
					edge.setRight(target);
					edges.add(edge);
				}
				if (!(clusterMap.get(clusterName).contains(source)) && clusterMap.get(clusterName).contains(target)) { // target is in cluster, but source is not
					// Add external edge 
					MutablePair<String, String> edge = new MutablePair<>();
					edge.setLeft(source);
					edge.setRight(target);
					edges.add(edge);
				}
			}
			map.put(clusterName, edges);
		}


		return map;
	}
	
	public static Set<List<String>> buildClusterEdges(Map<String,Set<String>> clusterMap, List<List<String>> depFacts) {
		Set<List<String>> edges = new HashSet<>();

		for (List<String> depFact : depFacts) {
			String source = depFact.get(1);
			String target = depFact.get(2);

			for (String clusterNameSource : clusterMap.keySet()) {
				if (clusterMap.get(clusterNameSource).contains(source)) {
					for (String clusterNameTarget : clusterMap.keySet()) {
						if (clusterMap.get(clusterNameTarget).contains(target)) {
							if (!clusterNameSource.equals(clusterNameTarget)) {
								List<String> edge = new ArrayList<>();
								edge.add(clusterNameSource);
								edge.add(clusterNameTarget);
								edges.add(edge);
							}
						}
					}
				}
			}
		}

		return edges;
	}
	
	public static void fastClusterPostProcessing(List<FastCluster> fastClusters, FastFeatureVectors fastFeatureVectors) {

		boolean outputResultingClusterGraph = false;
		StringGraph clusterGraph = ClusterUtil.generateFastClusterGraph(
				fastClusters, fastFeatureVectors.getNamesInFeatureSet());
		if (outputResultingClusterGraph) {
			logger.debug("Resulting ClusterGraph...");
			logger.debug(clusterGraph);
		}

		Map<String, Integer> clusterNameToNodeNumberMap = ClusterUtil
				.createFastClusterNameToNodeNumberMap(fastClusters);
		Map<Integer, String> nodeNumberToClusterNameMap = ClusterUtil
				.createNodeNumberToFastClusterNameMap(fastClusters,
						clusterNameToNodeNumberMap);

		try {
			clusterGraph.writeNumberedNodeDotFileWithTextMappingFile(
					Config.getClusterGraphDotFilename(),
					clusterNameToNodeNumberMap, nodeNumberToClusterNameMap);
			ClusterUtil.writeFastClusterRSFFileUsingConfigName(clusterNameToNodeNumberMap,
					fastClusters);
			clusterGraph.writeXMLClusterGraph(Config.getClusterGraphXMLFilename());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}
	
	public static Map<String, Set<String>> buildDependenciesMap(String depsRsfFilename) {
		RsfReader.loadRsfDataFromFile(depsRsfFilename);
		Iterable<List<String>> depFacts = RsfReader.filteredRoutineFacts;
		
		Map<String,Set<String>> depMap = new HashMap<>();
		
		for (List<String> fact : depFacts) {
			String source = fact.get(1).trim();
			String target = fact.get(2).trim();
			Set<String> dependencies = null;
			if (depMap.containsKey(source)) {
				dependencies = depMap.get(source);
			}
			else {
				dependencies = new HashSet<>();
			}
			dependencies.add(target);
			depMap.put(source, dependencies);
		}
		return depMap;
	}
	
	public static SimpleDirectedGraph<String, DefaultEdge> buildConcernClustersDiGraph(
			Set<ConcernCluster> clusters, String depsRsfFilename) {
		SimpleDirectedGraph<String, DefaultEdge>  directedGraph = new SimpleDirectedGraph<>(DefaultEdge.class);
		
		for (ConcernCluster cluster : clusters) {
			directedGraph.addVertex(cluster.getName());
		}
		logger.debug("No. of vertices: " + directedGraph.vertexSet().size());
		
		RsfReader.loadRsfDataFromFile(depsRsfFilename);
		Iterable<List<String>> depFacts = RsfReader.filteredRoutineFacts;
		
		for (List<String> fact : depFacts) {
			String source = fact.get(1).trim();
			String target = fact.get(2).trim();
			directedGraph.addEdge(source, target);
        }
		logger.debug("No. of edges: " + directedGraph.edgeSet().size());
		
		return directedGraph;
	}
	
	public static SimpleDirectedGraph<String, DefaultEdge> buildConcernClustersDiGraph(
			Set<ConcernCluster> clusters, StringGraph clusterGraph) {
		SimpleDirectedGraph<String, DefaultEdge>  directedGraph = new SimpleDirectedGraph<>(DefaultEdge.class);
		
		for (ConcernCluster cluster : clusters) {
			directedGraph.addVertex(cluster.getName());
		}
		logger.debug("No. of vertices: " + directedGraph.vertexSet().size());
		
		
		for (StringEdge stringEdge : clusterGraph.edges) {
        	if (!stringEdge.getSrcStr().equals(stringEdge.getTgtStr()))
        		directedGraph.addEdge(stringEdge.getSrcStr(), stringEdge.getTgtStr());
        }
		logger.debug("No. of edges: " + directedGraph.edgeSet().size());
		
		return directedGraph;
	}

	public static SimpleDirectedGraph<String, DefaultEdge> buildSimpleDirectedGraph(
			String depsFilename, Set<ConcernCluster> clusters) {
		String readingDepsFile = "Reading in deps file: " + depsFilename;
		System.out.println(readingDepsFile);
		logger.info(readingDepsFile);
		Map<String, Set<String>> depMap = ClusterUtil.buildDependenciesMap(depsFilename);
		
		StringGraph clusterGraph = ClusterUtil.buildClusterGraphUsingDepMap(depMap,clusters);
		
		return ClusterUtil.buildConcernClustersDiGraph(clusters, clusterGraph);
	}
}