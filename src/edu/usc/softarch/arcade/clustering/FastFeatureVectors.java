package edu.usc.softarch.arcade.clustering;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;

public class FastFeatureVectors implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8870834810415855677L;
	private ArrayList<String> featureVectorNames = new ArrayList<String>();
	private ArrayList<String> namesInFeatureSet = new ArrayList<String>();
	private HashMap<String, BitSet> nameToFeatureSetMap = new HashMap<String,BitSet>();
	public HashMap<String, BitSet> getNameToFeatureSetMap() {
		return nameToFeatureSetMap;
	}

	public void setNameToFeatureSetMap(HashMap<String, BitSet> nameToFeatureSetMap) {
		this.nameToFeatureSetMap = nameToFeatureSetMap;
	}

	public ArrayList<String> getNamesInFeatureSet() {
		return namesInFeatureSet;
	}

	public void setNamesInFeatureSet(ArrayList<String> namesInFeatureSet) {
		this.namesInFeatureSet = namesInFeatureSet;
	}

	
	
	FastFeatureVectors(ArrayList<String> featureNames, HashMap<String, BitSet> nameToFeatureSetMap, ArrayList<String> namesInFeatureSet ) {
		this.setFeatureVectorNames(featureNames);
		this.nameToFeatureSetMap = nameToFeatureSetMap;
		this.namesInFeatureSet = namesInFeatureSet;
	}

	public ArrayList<String> getFeatureVectorNames() {
		return featureVectorNames;
	}

	public void setFeatureVectorNames(ArrayList<String> featureVectorNames) {
		this.featureVectorNames = featureVectorNames;
	}
}
