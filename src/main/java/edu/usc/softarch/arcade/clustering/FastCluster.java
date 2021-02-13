package edu.usc.softarch.arcade.clustering;

import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.usc.softarch.arcade.topics.DocTopicItem;

public class FastCluster {
	// #region ATTRIBUTES --------------------------------------------------------
	private String name;
	private int numEntities;
	private Map<Integer,Double> nonZeroFeatureMap = new HashMap<>();
	private int featuresLength = 0;
	public DocTopicItem docTopicItem;
	// #endregion ATTRIBUTES -----------------------------------------------------

	// #region CONSTRUCTORS ------------------------------------------------------
	public FastCluster(String name, BitSet featureSet,
			List<String> namesInFeatureSet) {
		// Name of a node
		this.name = name;
		// Number of target nodes in the graph
		this.featuresLength = namesInFeatureSet.size();
		
		// Map of target nodes to which this node has an edge
		this.nonZeroFeatureMap = new HashMap<>();
		for (int i=0; i < featuresLength; i++)
			if (featureSet.get(i)) // put if not 0
				nonZeroFeatureMap.put(i,1.0);

		// Cluster currently only has one entity, the node itself
		this.numEntities = 1;
	}
	
	public FastCluster(String name) {
		this.name = name;
		this.numEntities = 1;
	}

	public FastCluster() { super(); }

	public FastCluster(FastCluster c1, FastCluster c2) {
		Set<Integer> c1Indices = c1.getNonZeroFeatureMap().keySet();
		setNonZeroFeatureMapForWcaUsingIndices(c1, c2, c1Indices);
		
		Set<Integer> c2Indices = c2.getNonZeroFeatureMap().keySet();
		setNonZeroFeatureMapForWcaUsingIndices(c1, c2, c2Indices);
		
		this.name = c1.getName() + ',' + c2.getName();

		this.numEntities = c1.getNumEntities() + c2.getNumEntities();
		this.featuresLength = c1.getFeaturesLength();
	}

	public FastCluster(ClusteringAlgorithmType cat, FastCluster c1,
			FastCluster c2) {
		if (cat.equals(ClusteringAlgorithmType.LIMBO)) {
			Set<Integer> c1Indices = c1.getNonZeroFeatureMap().keySet();
			setNonZeroFeatureMapForLibmoUsingIndices(c1, c2, c1Indices);
			
			Set<Integer> c2Indices = c2.getNonZeroFeatureMap().keySet();
			setNonZeroFeatureMapForLibmoUsingIndices(c1, c2, c2Indices);
			
			this.name = c1.getName() + ',' + c2.getName();

			this.numEntities = c1.getNumEntities() + c2.getNumEntities();
			this.featuresLength = c1.getFeaturesLength();
		}
	}
	// #endregion CONSTRUCTORS ---------------------------------------------------

	// #region ACCESSORS ---------------------------------------------------------
	public int getFeaturesLength() { return featuresLength; }

	public Map<Integer, Double> getNonZeroFeatureMap() {
		return nonZeroFeatureMap; }

	public String getName() {	return name; }

	public int getNumEntities() { return numEntities; }
	// #endregion ACCESSORS ------------------------------------------------------
	
	private void setNonZeroFeatureMapForLibmoUsingIndices(FastCluster c1,
			FastCluster c2, Set<Integer> c1Indices) {
		for (Integer index : c1Indices) {
			Double c1Value = c1.getNonZeroFeatureMap().get(index);
			Double c2Value = c2.getNonZeroFeatureMap().get(index);
			
			Double newFeatureValue = null;
			if (c1Value == null && c2Value != null) {
				newFeatureValue = Double.valueOf( (c2Value*c2.getNumEntities()) /(c1.getNumEntities()+c2.getNumEntities()));
				
			}
			else if (c2Value == null && c1Value != null) {
				newFeatureValue = Double.valueOf((c1Value*c1.getNumEntities())/(c1.getNumEntities()+c2.getNumEntities()));
			}
			else if (c1Value != null && c2Value != null) {
				newFeatureValue = Double.valueOf((c1Value*c1.getNumEntities()+ c2Value*c2.getNumEntities())/(c1.getNumEntities()+c2.getNumEntities()));
			}
			
			if (newFeatureValue != null)
				nonZeroFeatureMap.put(index, newFeatureValue);
		}
	}

	private void setNonZeroFeatureMapForWcaUsingIndices(FastCluster c1,
			FastCluster c2, Set<Integer> c1Indices) {
		for (Integer index : c1Indices) {
			Double c1Value = c1.getNonZeroFeatureMap().get(index);
			Double c2Value = c2.getNonZeroFeatureMap().get(index);
			
			Double newFeatureValue = null;
			if (c1Value == null && c2Value != null) {
				newFeatureValue = Double.valueOf(c2Value/(c1.getNumEntities()+c2.getNumEntities()));
				
			}
			else if (c2Value == null && c1Value != null) {
				newFeatureValue = Double.valueOf(c1Value/(c1.getNumEntities()+c2.getNumEntities()));
			}
			else if (c1Value != null && c2Value != null) {
				newFeatureValue = Double.valueOf((c1Value + c2Value)/(c1.getNumEntities()+c2.getNumEntities()));
			}
			
			if (newFeatureValue != null)
				nonZeroFeatureMap.put(index, newFeatureValue);
			
		}
	}
	
	public String toString() {
		return name;
	}
}