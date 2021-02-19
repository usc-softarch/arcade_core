package edu.usc.softarch.arcade.clustering;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import edu.usc.softarch.arcade.classgraphs.StringEdge;
import edu.usc.softarch.arcade.facts.ConcernCluster;
import edu.usc.softarch.arcade.facts.driver.RsfReader;
import edu.usc.softarch.extractors.cda.odem.Dependencies;
import edu.usc.softarch.extractors.cda.odem.DependsOn;
import edu.usc.softarch.extractors.cda.odem.Type;

public class ConcernClusterArchitecture extends HashSet<ConcernCluster> {
	// #region ATTRIBUTES --------------------------------------------------------
  private static final long serialVersionUID = 1L;
	private static Logger logger =
		LogManager.getLogger(ConcernClusterArchitecture.class);
	// #endregion ATTRIBUTES -----------------------------------------------------

	// #region CONSTRUCTORS ------------------------------------------------------
	public ConcernClusterArchitecture() { super(); }

	public ConcernClusterArchitecture(ConcernClusterArchitecture toCopy) {
		super();
		this.addAll(toCopy);
	}

	public ConcernClusterArchitecture(Collection<ConcernCluster> initSet) {
		super();
		this.addAll(initSet);
	}
  
  public Set<String> getClassesInClusters() {
		Set<String> classes = new HashSet<>();
		for (ConcernCluster cluster : this)
			for (String entity : cluster.getEntities())
				classes.add(entity.trim());

		return classes;
	}
	// #endregion CONSTRUCTORS ---------------------------------------------------

	// #region IO ----------------------------------------------------------------
	public static ConcernClusterArchitecture loadFromRsf(String rsfFilename) {
		try {
			RsfReader.loadRsfDataFromFile(rsfFilename);
		} catch(IOException e) {
			e.printStackTrace();
		}

		Iterable<List<String>> clusterFacts = RsfReader.filteredRoutineFacts;
		ConcernClusterArchitecture clusters = new ConcernClusterArchitecture();
		for (List<String> fact : clusterFacts) {
			String clusterName = fact.get(1).trim();
			String element = fact.get(2).trim();
			if (clusters.containsClusterWithName(clusterName))
				for (ConcernCluster cluster : clusters)
					if (cluster.getName().equals(clusterName))
						cluster.addEntity(element);
			else {
				ConcernCluster newCluster = new ConcernCluster();
				newCluster.setName(clusterName);
				newCluster.addEntity(element);
				clusters.add(newCluster);
			}
		}
		return clusters;
	}
	// #endregion IO -------------------------------------------------------------

	public boolean containsClusterWithName(String clusterName) {
		for (ConcernCluster cluster : this)
			if (cluster.getName().equals(clusterName))
				return true;

		return false;
	}

  public Set<StringGraph> buildInternalGraphs(Map<String, Type> typeMap) {
		Set<StringGraph> graphs = new HashSet<>();
		for (ConcernCluster cluster : this) {
			StringGraph currGraph = new StringGraph(cluster.getName().trim());
			for (String entity : cluster.getEntities()) {
        if (typeMap.containsKey(entity.trim())) {
				  Type type = typeMap.get(entity.trim());
					Dependencies dependencies = type.getDependencies();
					for (DependsOn dependency : dependencies.getDependsOn()) {
						for (String otherEntity : cluster.getEntities()) {
							if (!entity.equals(otherEntity) &&
                  otherEntity.trim().equals(dependency.getName().trim())) {
                StringEdge newEdge =
                  new StringEdge(entity.trim(), otherEntity.trim());
                newEdge.setType(dependency.getClassification());
                currGraph.addEdge(newEdge);
							}
						}
					}
				}
			}

			graphs.add(currGraph);
		}

		return graphs;
	}

  public StringGraph buildClusterGraphUsingDepMap(
      Map<String, Set<String>> depMap) {
		StringGraph cg = new StringGraph();
		for (ConcernCluster cluster : this) {
			for (String entity : cluster.getEntities()) {
				if (depMap.containsKey(entity.trim())) {
					Set<String> dependencies = depMap.get(entity);
					for (String dependency : dependencies) {
						for (ConcernCluster otherCluster : this) {
							for (String otherEntity : otherCluster.getEntities()) {
								if (otherEntity.trim().equals(dependency.trim())) {
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

  public StringGraph buildClusterGraphUsingOdemClasses(
      Map<String, Type> typeMap) {
		StringGraph cg = new StringGraph();
		for (ConcernCluster cluster : this) {
			for (String entity : cluster.getEntities()) {
				if (typeMap.containsKey(entity.trim())) {
          Type type = typeMap.get(entity.trim());
					Dependencies dependencies = type.getDependencies();
					for (DependsOn dependency : dependencies.getDependsOn()) {
						for (ConcernCluster otherCluster : this) {
							for (String otherEntity : otherCluster.getEntities()) {
								if (otherEntity.trim().equals(dependency.getName().trim())) {
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

	public SimpleDirectedGraph<String, DefaultEdge> buildConcernClustersDiGraph(
			String depsRsfFilename) {
		SimpleDirectedGraph<String, DefaultEdge> directedGraph =
			new SimpleDirectedGraph<>(DefaultEdge.class);
		
		for (ConcernCluster cluster : this)
			directedGraph.addVertex(cluster.getName());
		logger.debug("No. of vertices: " + directedGraph.vertexSet().size());
		
		try {
			RsfReader.loadRsfDataFromFile(depsRsfFilename);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Iterable<List<String>> depFacts = RsfReader.filteredRoutineFacts;
		
		for (List<String> fact : depFacts) {
			String source = fact.get(1).trim();
			String target = fact.get(2).trim();
			directedGraph.addEdge(source, target);
    }
		logger.debug("No. of edges: " + directedGraph.edgeSet().size());
		
		return directedGraph;
	}

	public SimpleDirectedGraph<String, DefaultEdge>	buildSimpleDirectedGraph(
			String depsFilename) {
		String readingDepsFile = "Reading in deps file: " + depsFilename;
		logger.info(readingDepsFile);
		Map<String, Set<String>> depMap =
			ClusterUtil.buildDependenciesMap(depsFilename);
		
		StringGraph clusterGraph = this.buildClusterGraphUsingDepMap(depMap);
		
		return this.buildConcernClustersDiGraph(clusterGraph);
	}

	public SimpleDirectedGraph<String, DefaultEdge> buildConcernClustersDiGraph(
			StringGraph clusterGraph) {
		SimpleDirectedGraph<String, DefaultEdge> directedGraph =
			new SimpleDirectedGraph<>(DefaultEdge.class);
		
		for (ConcernCluster cluster : this)
			directedGraph.addVertex(cluster.getName());
		logger.debug("No. of vertices: " + directedGraph.vertexSet().size());
		
		for (StringEdge stringEdge : clusterGraph.edges)
			if (!stringEdge.getSrcStr().equals(stringEdge.getTgtStr()))
				directedGraph.addEdge(stringEdge.getSrcStr(), stringEdge.getTgtStr());
		logger.debug("No. of edges: " + directedGraph.edgeSet().size());
		
		return directedGraph;
	}

	public static ConcernClusterArchitecture buildGroundTruthClustersFromPackages(
			Set<String> topLevelPackagesOfUnclusteredClasses,
			Set<String> unClusteredClasses) {
		ConcernClusterArchitecture clusters = new ConcernClusterArchitecture();
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
}