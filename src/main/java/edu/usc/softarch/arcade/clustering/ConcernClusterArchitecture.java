package edu.usc.softarch.arcade.clustering;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.usc.softarch.arcade.classgraphs.StringEdge;
import edu.usc.softarch.arcade.facts.ConcernCluster;
import edu.usc.softarch.extractors.cda.odem.Dependencies;
import edu.usc.softarch.extractors.cda.odem.DependsOn;
import edu.usc.softarch.extractors.cda.odem.Type;

public class ConcernClusterArchitecture extends HashSet<ConcernCluster> {
  private static final long serialVersionUID = 1L;

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
}