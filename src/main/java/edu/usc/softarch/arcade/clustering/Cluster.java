package edu.usc.softarch.arcade.clustering;

import java.io.Serializable;
import java.util.BitSet;
import java.util.HashMap;
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
	public DocTopicItem docTopicItem;
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
		this.docTopicItem = c1.docTopicItem;
	}

	/**
	 * Merge constructor.
	 */
	public Cluster(Cluster c1, Cluster c2) {
		Set<Integer> c1Indices = c1.getFeatureMap().keySet();
		setWcaFeatureMap(c1, c2, c1Indices);
		
		Set<Integer> c2Indices = c2.getFeatureMap().keySet();
		setWcaFeatureMap(c1, c2, c2Indices);
		
		this.name = c1.getName() + ',' + c2.getName();

		this.numEntities = c1.getNumEntities() + c2.getNumEntities();
	}

	public Cluster(ClusteringAlgorithmType cat, Cluster c1, Cluster c2) {
		Set<Integer> c1Indices = c1.getFeatureMap().keySet();
		setLimboFeatureMap(c1, c2, c1Indices);

		Set<Integer> c2Indices = c2.getFeatureMap().keySet();
		setLimboFeatureMap(c1, c2, c2Indices);

		if (c1.getName().contains("$") && cat.equals(ClusteringAlgorithmType.ARC))
			this.name = c2.getName();
		else
			this.name = c1.getName() + ',' + c2.getName();

		this.numEntities = c1.getNumEntities() + c2.getNumEntities();
	}
	//endregion

	//region ACCESSORS
	public Map<Integer, Double> getFeatureMap() {
		return featureMap; }

	public String getName() {	return name; }

	public int getNumEntities() { return numEntities; }
	//endregion
	
	private void setLimboFeatureMap(
					Cluster c1,	Cluster c2, Set<Integer> c1Indices) {
		for (Integer index : c1Indices) {
			Double c1Value = c1.getFeatureMap().get(index);
			Double c2Value = c2.getFeatureMap().get(index);
			
			Double newFeatureValue = null;
			if (c1Value == null && c2Value != null)
				newFeatureValue = Double.valueOf( (c2Value*c2.getNumEntities()) /(c1.getNumEntities()+c2.getNumEntities()));
			else if (c2Value == null && c1Value != null)
				newFeatureValue = Double.valueOf((c1Value*c1.getNumEntities())/(c1.getNumEntities()+c2.getNumEntities()));
			else if (c1Value != null && c2Value != null)
				newFeatureValue = Double.valueOf((c1Value*c1.getNumEntities()+ c2Value*c2.getNumEntities())/(c1.getNumEntities()+c2.getNumEntities()));
			
			if (newFeatureValue != null)
				featureMap.put(index, newFeatureValue);
		}
	}

	private void setWcaFeatureMap(
					Cluster c1,	Cluster c2, Set<Integer> c1Indices) {
		for (Integer index : c1Indices) {
			Double c1Value = c1.getFeatureMap().get(index);
			Double c2Value = c2.getFeatureMap().get(index);
			
			Double newFeatureValue = null;
			if (c1Value == null && c2Value != null)
				newFeatureValue = Double.valueOf(c2Value/(c1.getNumEntities()+c2.getNumEntities()));
			else if (c2Value == null && c1Value != null)
				newFeatureValue = Double.valueOf(c1Value/(c1.getNumEntities()+c2.getNumEntities()));
			else if (c1Value != null && c2Value != null)
				newFeatureValue = Double.valueOf((c1Value + c2Value)/(c1.getNumEntities()+c2.getNumEntities()));
			
			if (newFeatureValue != null)
				featureMap.put(index, newFeatureValue);
		}
	}

	public double computeStructuralCentroid(int numFeatures) {
		double centroidSum = 0;
		Set<Integer> clusterKeys = getFeatureMap().keySet();

		for (Integer key : clusterKeys)
			centroidSum += getFeatureMap().get(key).doubleValue();

		double centroidAvg = centroidSum / numFeatures;

		// centroid
		return centroidAvg / getNumEntities();
	}
	
	public String toString() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o == this) return true;
		if (!(o instanceof Cluster)) return false;

		Cluster toCompare = (Cluster) o;

		boolean condition1 = Objects.equals(this.name, toCompare.name);
		boolean condition2 = this.numEntities == toCompare.numEntities;
		boolean condition3 = Objects.equals(this.docTopicItem, toCompare.docTopicItem);
		boolean condition4 = Objects.equals(this.featureMap, toCompare.featureMap);

		return condition1 && condition2	&& condition3	&& condition4;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, numEntities, featureMap, docTopicItem);
	}
}
