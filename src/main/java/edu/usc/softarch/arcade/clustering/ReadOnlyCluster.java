package edu.usc.softarch.arcade.clustering;

import edu.usc.softarch.util.EnhancedHashSet;
import edu.usc.softarch.util.EnhancedSet;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Cluster object which cannot be used for further clustering, i.e. is already
 * the result of a clustering technique. Used for post-processing components.
 */
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
	protected final EnhancedSet<String> entities;
	//endregion

	//region CONSTRUCTORS
	public ReadOnlyCluster(String name) {
		this.name = name;
		this.entities = new EnhancedHashSet<>();
	}

	public ReadOnlyCluster(String name, Collection<String> entities) {
		this.name = name;
		this.entities = new EnhancedHashSet<>(entities);
	}

	public ReadOnlyCluster(ReadOnlyCluster c) {
		this.name = c.name;
		this.entities = new EnhancedHashSet<>(c.entities);
	}

	protected ReadOnlyCluster(
			ClusteringAlgorithmType cat, Cluster c1, Cluster c2) {
		this.entities = new EnhancedHashSet<>(c2.getEntities());

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
	public void removeEntities(Set<String> entities) {
		this.entities.removeAll(entities); }

	public Set<String> union(ReadOnlyCluster c) {
		return this.entities.union(c.entities); }
	public Set<String> intersection(ReadOnlyCluster c) {
		return this.entities.intersection(c.entities); }
	public Set<String> difference(ReadOnlyCluster c) {
		return this.entities.difference(c.entities); }
	public Set<String> symmetricDifference(ReadOnlyCluster c) {
		return this.entities.symmetricDifference(c.entities); }
	//endregion
}
