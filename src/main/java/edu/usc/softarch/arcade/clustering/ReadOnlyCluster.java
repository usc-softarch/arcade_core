package edu.usc.softarch.arcade.clustering;

import edu.usc.softarch.arcade.facts.ConcernCluster;
import edu.usc.softarch.arcade.topics.Concern;
import edu.usc.softarch.arcade.topics.DocTopicItem;
import edu.usc.softarch.arcade.topics.DocTopics;
import edu.usc.softarch.arcade.topics.exceptions.UnmatchingDocTopicItemsException;
import edu.usc.softarch.util.EnhancedHashSet;
import edu.usc.softarch.util.EnhancedSet;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
	/**
	 * {@link DocTopicItem} related to this Cluster, if one exists.
	 */
	private DocTopicItem dti;
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

	protected ReadOnlyCluster(String name, Collection<String> entities,
			DocTopicItem dti) {
		this(name, entities);
		this.dti = dti;
	}

	public ReadOnlyCluster(ReadOnlyCluster c) {
		this.name = c.name;
		this.entities = new EnhancedHashSet<>(c.entities);
		this.dti = c.dti;
	}

	protected ReadOnlyCluster(ClusteringAlgorithmType cat, Cluster c1, Cluster c2)
			throws UnmatchingDocTopicItemsException {
		this.entities = new EnhancedHashSet<>(c2.getEntities());

		if (cat.equals(ClusteringAlgorithmType.ARC) && c1.name.contains("$"))
			this.name = c2.name;
		else {
			this.name = c1.name + ',' + c2.name;
			this.entities.addAll(c1.getEntities());
		}

		if (cat.equals(ClusteringAlgorithmType.ARC))
			this.dti = DocTopics.getSingleton().mergeDocTopicItems(c1, c2, name);
	}

	//TODO kill it
	public ReadOnlyCluster(ConcernCluster cluster) {
		this.name = cluster.getName();
		this.dti = cluster.getDocTopicItem();
		this.entities = new EnhancedHashSet<>(cluster.getEntities());
	}
	//endregion

	//region ACCESSORS
	public Collection<String> getEntities() { return new HashSet<>(entities); }
	void addEntity(String entity) { this.entities.add(entity); }
	public void removeEntities(Set<String> entities) {
		this.entities.removeAll(entities); }

	/**
	 * Returns a copy of this Cluster's {@link DocTopicItem}.
	 */
	public DocTopicItem getDocTopicItem() {
		if (hasDocTopicItem())
			return this.dti;
		return null;
	}

	/**
	 * Checks whether this Cluster's {@link DocTopicItem} is null.
	 *
	 * @return False if {@link DocTopicItem} is null, true otherwise.
	 */
	public boolean hasDocTopicItem() { return this.dti != null; }

	/**
	 * Sets this Cluster's {@link DocTopicItem}.
	 */
	public void setDocTopicItem(DocTopicItem dti) { this.dti = dti; }

	public Set<String> union(ReadOnlyCluster c) {
		return this.entities.union(c.entities); }
	public Set<String> intersection(ReadOnlyCluster c) {
		return this.entities.intersection(c.entities); }
	public Set<String> difference(ReadOnlyCluster c) {
		return this.entities.difference(c.entities); }
	public Set<String> symmetricDifference(ReadOnlyCluster c) {
		return this.entities.symmetricDifference(c.entities); }
	//endregion

	//region PROCESSING
	public Concern computeConcern(Map<Integer, List<String>> wordBags) {
		return this.dti.computeConcern(wordBags); }
	//endregion

	//region OBJECT METHODS
	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o == this) return true;
		if (!(o instanceof ReadOnlyCluster)) return false;

		ReadOnlyCluster toCompare = (ReadOnlyCluster) o;

		return this.name.equals(toCompare.name);
	}

	@Override
	public int hashCode() { return this.name.hashCode(); }
	//endregion
}
