package edu.usc.softarch.arcade.clustering;

import java.io.Serializable;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import edu.usc.softarch.arcade.topics.DocTopicItem;

/**
 * Represents a cluster of entities in the subject system.
 */
public class Cluster implements Serializable {
	//region ATTRIBUTES
	private static final long serialVersionUID = 1L;

	/**
	 * Name of the Cluster, typically given by the union of the names of its
	 * comprising entities. Can also be a related, representative name of all
	 * entities.
	 */
	private final String name;
	/**
	 * Count of how many entities are represented by this Cluster.
	 */
	private final int numEntities;
	/**
	 * Map of the values of each non-zero feature in this Cluster.
	 */
	private Map<Integer, Double> featureMap = new HashMap<>();
	/**
	 * {@link DocTopicItem} related to this Cluster, if one exists.
	 */
	private DocTopicItem dti;
	//endregion

	//region CONSTRUCTORS
	/**
	 * Default constructor for new Clusters.
	 *
	 * @param name Name of the new Cluster, typically its originating entity.
	 * @param featureSet Set of the new Cluster's entities.
	 */
	public Cluster(String name, BitSet featureSet, int numFeatures) {
		this.name = name;

		this.featureMap = new HashMap<>();
		for (int i = 0; i < numFeatures; i++)
			if (featureSet.get(i)) // put if not 0
				featureMap.put(i, 1.0);

		// Cluster currently only has one entity, the node itself
		this.numEntities = 1;
	}

	/**
	 * Clone constructor.
	 */
	public Cluster(Cluster c1) {
		this.name = c1.getName();
		this.numEntities = c1.getNumEntities();
		this.featureMap = c1.getFeatureMap();
		this.dti = c1.dti;
	}

	/**
	 * Merge constructor.
	 */
	public Cluster(ClusteringAlgorithmType cat, Cluster c1, Cluster c2) {
		Set<Integer> indices = new HashSet<>(c1.getFeatureMap().keySet());
		indices.addAll(c2.getFeatureMap().keySet());

		switch (cat) {
			case LIMBO:
			case ARC:
				setLimboFeatureMap(c1, c2, indices);
				break;

			case WCA:
				setWcaFeatureMap(c1, c2, indices);
		}

		if (cat.equals(ClusteringAlgorithmType.ARC) && c1.getName().contains("$"))
			this.name = c2.getName();
		else
			this.name = c1.getName() + ',' + c2.getName();

		this.numEntities = c1.getNumEntities() + c2.getNumEntities();
	}
	//endregion

	//region ACCESSORS
	/**
	 * Returns the {@link #featureMap} of this Cluster itself, NOT a copy.
	 */
	public Map<Integer, Double> getFeatureMap() { return featureMap; }

	/**
	 * Returns the name of this Cluster.
	 */
	public String getName() {	return name; }

	/**
	 * Returns the number of entities represented by this Cluster.
	 */
	public int getNumEntities() { return numEntities; }

	/**
	 * Returns a copy of this Cluster's {@link DocTopicItem}.
	 */
	public DocTopicItem getDocTopicItem() {
		if (hasDocTopicItem())
			return new DocTopicItem(this.dti);
		return null;
	}

	/**
	 * Sets this Cluster's {@link DocTopicItem}.
	 */
	public void setDocTopicItem(DocTopicItem dti) { this.dti = dti; }

	/**
	 * Checks whether this Cluster's {@link DocTopicItem} is null.
	 *
	 * @return False if {@link DocTopicItem} is null, true otherwise.
	 */
	public boolean hasDocTopicItem() { return this.dti != null; }
	//endregion

	//region PROCESSING
	/**
	 * Merges the feature maps of two Clusters for Limbo or ARC.
	 *
	 * @param c1 First Cluster being merged.
	 * @param c2 Second Cluster being merged.
	 * @param indices The indices all features present in EITHER Cluster.
	 */
	private void setLimboFeatureMap(Cluster c1,	Cluster c2,
			Set<Integer> indices) {
		for (Integer index : indices) {
			Double c1Value = c1.getFeatureMap().get(index);
			Double c2Value = c2.getFeatureMap().get(index);

			if (c1Value == null && c2Value == null) continue;

			double newFeatureValue;
			if (c1Value == null)
				newFeatureValue = (c2Value * c2.getNumEntities()) /
					(c1.getNumEntities() + c2.getNumEntities());
			else if (c2Value == null)
				newFeatureValue = (c1Value * c1.getNumEntities()) /
					(c1.getNumEntities() + c2.getNumEntities());
			else
				newFeatureValue = (c1Value * c1.getNumEntities()
					+ c2Value * c2.getNumEntities()) /
					(c1.getNumEntities() + c2.getNumEntities());

			featureMap.put(index, newFeatureValue);
		}
	}

	/**
	 * Merges the feature maps of two Clusters for WCA.
	 *
	 * @param c1 First Cluster being merged.
	 * @param c2 Second Cluster being merged.
	 * @param indices The indices all features present in EITHER Cluster.
	 */
	private void setWcaFeatureMap(Cluster c1,	Cluster c2,
			Set<Integer> indices) {
		for (Integer index : indices) {
			Double c1Value = c1.getFeatureMap().get(index);
			Double c2Value = c2.getFeatureMap().get(index);

			if (c1Value == null && c2Value == null) continue;
			
			double newFeatureValue;
			if (c1Value == null)
				newFeatureValue = c2Value / (c1.getNumEntities() + c2.getNumEntities());
			else if (c2Value == null)
				newFeatureValue = c1Value / (c1.getNumEntities() + c2.getNumEntities());
			else
				newFeatureValue = (c1Value + c2Value) /
					(c1.getNumEntities() + c2.getNumEntities());

			featureMap.put(index, newFeatureValue);
		}
	}

	/**
	 * TODO
	 * @param numFeatures
	 * @return
	 */
	public double computeStructuralCentroid(int numFeatures) {
		double centroidSum = 0;
		Set<Integer> clusterKeys = getFeatureMap().keySet();

		for (Integer key : clusterKeys)
			centroidSum += getFeatureMap().get(key);

		double centroidAvg = centroidSum / numFeatures;

		return centroidAvg / getNumEntities();
	}
	//endregion

	//region OBJECT METHODS
	public String toString() { return name; }

	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o == this) return true;
		if (!(o instanceof Cluster)) return false;

		Cluster toCompare = (Cluster) o;

		boolean condition1 = Objects.equals(this.name, toCompare.name);
		boolean condition2 = this.numEntities == toCompare.numEntities;
		boolean condition3 = Objects.equals(this.dti, toCompare.dti);
		boolean condition4 = Objects.equals(this.featureMap, toCompare.featureMap);

		return condition1 && condition2	&& condition3	&& condition4;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, numEntities, featureMap, dti);
	}
	//endregion
}
