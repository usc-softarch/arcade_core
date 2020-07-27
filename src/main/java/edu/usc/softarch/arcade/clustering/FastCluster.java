package edu.usc.softarch.arcade.clustering;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Set;

import edu.usc.softarch.arcade.topics.DocTopicItem;

public class FastCluster {
	
	private String name;
	private int numEntities;
	private HashMap<Integer,Double> nonZeroFeatureMap = new HashMap<Integer,Double>();
	private int featuresLength = 0;
	public DocTopicItem docTopicItem;
	
	public int getFeaturesLength() {
		return featuresLength;
	}

	public void setFeaturesLength(int featuresLength) {
		this.featuresLength = featuresLength;
	}

	public HashMap<Integer, Double> getNonZeroFeatureMap() {
		return nonZeroFeatureMap;
	}

	public void setNonZeroFeatureMap(HashMap<Integer, Double> nonZeroFeatureMap) {
		this.nonZeroFeatureMap = nonZeroFeatureMap;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public FastCluster(String name, BitSet featureSet, ArrayList<String> namesInFeatureSet) {
		this.name = name;
		featuresLength = namesInFeatureSet.size();
		/*features = new double[featureLength];
		for (int i=0;i<featureLength;i++) {
			if (featureSet.get(i)) {
				features[i] = 1;
			}
			else {
				features[i] = 0;
			}
		}*/
		
		nonZeroFeatureMap = new HashMap<Integer,Double>();
		for (int i=0;i<featuresLength;i++) {
			if (featureSet.get(i)) {
				double one = 1;
				nonZeroFeatureMap.put(i,one);
			}
			else {
				/*Don't put anythigng*/
				
			}
		}
		this.numEntities = 1;
	}
	
	public FastCluster(String name) {
		this.name = name;
		this.numEntities = 1;
	}

	public FastCluster() {
		// TODO Auto-generated constructor stub
	}

	public FastCluster(FastCluster c1, FastCluster c2) {
		/*double[] features1 = c1.getFeatures();
		double[] features2 = c2.getFeatures();
		double[] newFeatures = new double[features1.length];
		
		for (int i=0;i<features1.length;i++) {
			double featureSum = features1[i] + features2[i];
			double newFeatureVal = featureSum/(c1.getNumEntities()+c2.getNumEntities());
			newFeatures[i] = newFeatureVal;
		}*/
		
		Set<Integer> c1Indices = c1.getNonZeroFeatureMap().keySet();
		setNonZeroFeatureMapForWcaUsingIndices(c1, c2, c1Indices);
		
		Set<Integer> c2Indices = c2.getNonZeroFeatureMap().keySet();
		setNonZeroFeatureMapForWcaUsingIndices(c1, c2, c2Indices);
		
		this.name = c1.getName() + ',' + c2.getName();
		
		/*this.features = new double[features1.length];
		System.arraycopy(newFeatures, 0, this.features, 0, this.features.length);*/

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
	
	private void setNonZeroFeatureMapForLibmoUsingIndices(FastCluster c1,
			FastCluster c2, Set<Integer> c1Indices) {
		for (Integer index : c1Indices) {
			Double c1Value = c1.getNonZeroFeatureMap().get(index);
			Double c2Value = c2.getNonZeroFeatureMap().get(index);
			
			Double newFeatureValue = null;
			if (c1Value == null && c2Value != null) {
				newFeatureValue = new Double( (c2Value*c2.getNumEntities()) /(c1.getNumEntities()+c2.getNumEntities()));
				
			}
			else if (c2Value == null && c1Value != null) {
				newFeatureValue = new Double((c1Value*c1.getNumEntities())/(c1.getNumEntities()+c2.getNumEntities()));
			}
			else if (c1Value != null && c2Value != null) {
				newFeatureValue = new Double((c1Value*c1.getNumEntities()+ c2Value*c2.getNumEntities())/(c1.getNumEntities()+c2.getNumEntities()));
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
				newFeatureValue = new Double(c2Value/(c1.getNumEntities()+c2.getNumEntities()));
				
			}
			else if (c2Value == null && c1Value != null) {
				newFeatureValue = new Double(c1Value/(c1.getNumEntities()+c2.getNumEntities()));
			}
			else if (c1Value != null && c2Value != null) {
				newFeatureValue = new Double((c1Value + c2Value)/(c1.getNumEntities()+c2.getNumEntities()));
			}
			
			if (newFeatureValue != null)
				nonZeroFeatureMap.put(index, newFeatureValue);
			
		}
	}

	public int getNumEntities() {
		return numEntities;
	}

	public void setNumEntities(int numEntities) {
		this.numEntities = numEntities;
	}
	
	public String toString() {
		return name;
	}
}
