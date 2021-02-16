package edu.usc.softarch.arcade.clustering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import edu.usc.softarch.arcade.classgraphs.StringEdge;
import edu.usc.softarch.arcade.facts.ConcernCluster;
import edu.usc.softarch.arcade.facts.driver.RsfReader;
import edu.usc.softarch.extractors.cda.odem.Dependencies;
import edu.usc.softarch.extractors.cda.odem.DependsOn;
import edu.usc.softarch.extractors.cda.odem.Type;

/**
 * @author joshua
 */
public class ClusterUtil {
	private static Logger logger = LogManager.getLogger(ClusterUtil.class);

	public static void printSimilarFeatures(FastCluster c1, FastCluster c2,
			FastFeatureVectors fastFeatureVectors) {
		List<String> names = fastFeatureVectors.getNamesInFeatureSet();

		String c1LimitedName = c1.getName();
		String c2LimitedName = c2.getName();

		logger.debug("Features shared between " + c1LimitedName + " and " + c2LimitedName);

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
							for (String otherEntity : otherCluster.getEntities()) {
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