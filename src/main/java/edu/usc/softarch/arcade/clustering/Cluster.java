package edu.usc.softarch.arcade.clustering;

import java.io.Serializable;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.usc.softarch.arcade.topics.DocTopicItem;

public class Cluster implements Serializable {
	// #region ATTRIBUTES --------------------------------------------------------
	private static Logger logger = LogManager.getLogger(Cluster.class);

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

	public Cluster() { super(); }

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
		if (cat.equals(ClusteringAlgorithmType.LIMBO)) {
			Set<Integer> c1Indices = c1.getFeatureMap().keySet();
			setLimboFeatureMap(c1, c2, c1Indices);
			
			Set<Integer> c2Indices = c2.getFeatureMap().keySet();
			setLimboFeatureMap(c1, c2, c2Indices);
			
			this.name = c1.getName() + ',' + c2.getName();

			this.numEntities = c1.getNumEntities() + c2.getNumEntities();
			this.numFeatures = c1.getNumFeatures();
		}
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

	public void printSimilarFeatures(Cluster toCompare,
																	 FastFeatureVectors fastFeatureVectors) {
		List<String> names = fastFeatureVectors.getNamesInFeatureSet();

		logger.debug("Features shared between " + this.getName() + " and "
			+ toCompare.getName());

		Set<Integer> c1Keys = this.getFeatureMap().keySet();

		for (Integer key : c1Keys)
			if (this.getFeatureMap().get(key) != null
					&& toCompare.getFeatureMap().get(key) != null)
				logger.debug(names.get(key));
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

		boolean condition1 = this.name != null
			? this.name.equals(toCompare.name)
			: toCompare.name == null;

		boolean condition2 = this.numEntities == toCompare.numEntities;
		boolean condition3 = this.numFeatures == toCompare.numFeatures;

		boolean condition4 = this.docTopicItem != null
			? this.docTopicItem.equals(toCompare.docTopicItem)
			: toCompare.docTopicItem == null;

		boolean condition5 = this.featureMap != null
			? this.featureMap.equals(toCompare.featureMap)
			: toCompare.featureMap == null;

		return condition1 && condition2	&& condition3 && condition4	&& condition5;
	}
}
