package edu.usc.softarch.arcade.clustering;

import java.io.Serializable;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import edu.usc.softarch.arcade.topics.DocTopicItem;

public class Cluster implements Serializable {
	// #region ATTRIBUTES --------------------------------------------------------
	// This attribute helps guarantee the order of clustering, and makes ARC deterministic.
	private String name;
	private int numEntities;
	private int numFeatures = 0;
	private Map<Integer, Double> featureMap = new HashMap<>();
	public DocTopicItem docTopicItem;

	private static final long serialVersionUID = 1L;
	// #endregion ATTRIBUTES -----------------------------------------------------

	// #region CONSTRUCTORS ------------------------------------------------------
	public Cluster(String name, BitSet featureSet,
								 List<String> namesInFeatureSet) {
		// Name of a node
		this.name = name;
		// Number of target nodes in the graph
		this.numFeatures = namesInFeatureSet.size();
		
		// Map of target nodes to which this node has an edge
		this.featureMap = new HashMap<>();
		for (int i = 0; i < numFeatures; i++)
			if (featureSet.get(i)) // put if not 0
				featureMap.put(i, 1.0);

		// Cluster currently only has one entity, the node itself
		this.numEntities = 1;
	}
	
	public Cluster(String name) {
		this.name = name;
		this.numEntities = 1;
	}

	public Cluster() {
		super();
	}

	/**
	 * Clone constructor. Does NOT increment cluster ages.
	 */
	public Cluster(Cluster c1) {
		this.name = c1.getName();
		this.numEntities = c1.getNumEntities();
		this.featureMap = c1.getFeatureMap();
		this.numFeatures = c1.getNumFeatures();
		this.docTopicItem = c1.docTopicItem;
	}

	public Cluster(Cluster c1, Cluster c2) {
		Set<Integer> c1Indices = c1.getFeatureMap().keySet();
		setWcaFeatureMap(c1, c2, c1Indices);
		
		Set<Integer> c2Indices = c2.getFeatureMap().keySet();
		setWcaFeatureMap(c1, c2, c2Indices);
		
		this.name = c1.getName() + ',' + c2.getName();

		this.numEntities = c1.getNumEntities() + c2.getNumEntities();
		this.numFeatures = c1.getNumFeatures();
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
		this.numFeatures = c1.getNumFeatures();
	}
	// #endregion CONSTRUCTORS ---------------------------------------------------

	// #region ACCESSORS ---------------------------------------------------------
	public int getNumFeatures() { return numFeatures; }

	public Map<Integer, Double> getFeatureMap() {
		return featureMap; }

	public String getName() {	return name; }

	public int getNumEntities() { return numEntities; }
	// #endregion ACCESSORS ------------------------------------------------------
	
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

	public double computeStructuralCentroid() {
		double centroidSum = 0;
		Set<Integer> clusterKeys = getFeatureMap().keySet();

		for (Integer key : clusterKeys)
			centroidSum += getFeatureMap().get(key).doubleValue();

		double centroidAvg = centroidSum / getNumFeatures();

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
		boolean condition3 = this.numFeatures == toCompare.numFeatures;
		boolean condition4 = Objects.equals(this.docTopicItem, toCompare.docTopicItem);
		boolean condition5 = Objects.equals(this.featureMap, toCompare.featureMap);

		return condition1 && condition2	&& condition3
			&& condition4	&& condition5;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, numEntities, numFeatures, featureMap, docTopicItem);
	}
}
