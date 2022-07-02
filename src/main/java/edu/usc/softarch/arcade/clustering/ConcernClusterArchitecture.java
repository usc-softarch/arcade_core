package edu.usc.softarch.arcade.clustering;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import edu.usc.softarch.arcade.classgraphs.StringEdge;
import edu.usc.softarch.arcade.facts.ConcernCluster;
import edu.usc.softarch.arcade.facts.driver.RsfReader;

public class ConcernClusterArchitecture extends HashSet<ConcernCluster> {
	//region ATTRIBUTES
  private static final long serialVersionUID = 1L;
	//endregion

	//region CONSTRUCTORS
	public ConcernClusterArchitecture() { super(); }

	public ConcernClusterArchitecture(ConcernClusterArchitecture toCopy) {
		super();
		this.addAll(toCopy);
	}
	//endregion

	//region SERIALIZATION
	public static ConcernClusterArchitecture loadFromRsf(String rsfFilename) {
		RsfReader.loadRsfDataFromFile(rsfFilename);
		Iterable<List<String>> clusterFacts = RsfReader.filteredRoutineFacts;

		ConcernClusterArchitecture clusters = new ConcernClusterArchitecture();
		for (List<String> fact : clusterFacts) {
			String clusterName = fact.get(1).trim();
			String element = fact.get(2).trim();
			if (clusters.containsClusterWithName(clusterName)){
				for (ConcernCluster cluster : clusters)
					if (cluster.getName().equals(clusterName))
						cluster.addEntity(element);
			}
			else {
				ConcernCluster newCluster = new ConcernCluster();
				newCluster.setName(clusterName);
				newCluster.addEntity(element);
				clusters.add(newCluster);
			}
		}
		return clusters;
	}
	//endregion

	public boolean containsClusterWithName(String clusterName) {
		for (ConcernCluster cluster : this)
			if (cluster.getName().equals(clusterName))
				return true;

		return false;
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

	public SimpleDirectedGraph<String, DefaultEdge> buildConcernClustersDiGraph(
			StringGraph clusterGraph) {
		SimpleDirectedGraph<String, DefaultEdge> directedGraph =
			new SimpleDirectedGraph<>(DefaultEdge.class);
		
		for (ConcernCluster cluster : this)
			directedGraph.addVertex(cluster.getName());

		for (StringEdge stringEdge : clusterGraph.edges)
			if (!stringEdge.getSrcStr().equals(stringEdge.getTgtStr()))
				directedGraph.addEdge(stringEdge.getSrcStr(), stringEdge.getTgtStr());

		return directedGraph;
	}
}
