package edu.usc.softarch.arcade.clustering;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ReadOnlyCluster {
	//region ATTRIBUTES
	/**
	 * Name of the Cluster, typically given by the union of the names of its
	 * comprising entities. Can also be a related, representative name of all
	 * entities.
	 */
	public final String name;
	/**
	 * Set of code-level entities contained by this cluster.
	 */
	protected final Set<String> entities;
	//endregion

	//region CONSTRUCTORS
	protected ReadOnlyCluster(String name) {
		this.name = name;
		this.entities = new HashSet<>();
	}

	protected ReadOnlyCluster(String name, Collection<String> entities) {
		this.name = name;
		this.entities = new HashSet<>(entities);
	}

	public ReadOnlyCluster(Cluster c) {
		this.name = c.name;
		this.entities = new HashSet<>(c.entities);
	}

	protected ReadOnlyCluster(
			ClusteringAlgorithmType cat, Cluster c1, Cluster c2) {
		this.entities = new HashSet<>(c2.getEntities());

		if (cat.equals(ClusteringAlgorithmType.ARC) && c1.name.contains("$"))
			this.name = c2.name;
		else {
			this.name = c1.name + ',' + c2.name;
			this.entities.addAll(c1.getEntities());
		}
	}
	//endregion

	//region ACCESSORS
	public Collection<String> getEntities() { return new HashSet<>(entities); }
	void addEntity(String entity) { this.entities.add(entity); }
	//endregion
}
