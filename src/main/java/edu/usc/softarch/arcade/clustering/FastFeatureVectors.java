package edu.usc.softarch.arcade.clustering;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents a graph as an adjacency matrix.
 */
public class FastFeatureVectors implements Serializable {
	// #region ATTRIBUTES --------------------------------------------------------
	private static final long serialVersionUID = -8870834810415855677L;
	private List<String> featureVectorNames = new ArrayList<>();
	private List<String> namesInFeatureSet = new ArrayList<>();
	private Map<String, BitSet> nameToFeatureSetMap = new HashMap<>();
	// #endregion ATTRIBUTES -----------------------------------------------------

	// #region CONSTRUCTORS ------------------------------------------------------
	FastFeatureVectors(List<String> featureNames, 
			Map<String, BitSet> nameToFeatureSetMap, 
			List<String> namesInFeatureSet) {
		this.setFeatureVectorNames(featureNames);
		this.nameToFeatureSetMap = nameToFeatureSetMap;
		this.namesInFeatureSet = namesInFeatureSet;
	}
	// #endregion CONSTRUCTORS ---------------------------------------------------

	// #region ACCESSORS ---------------------------------------------------------
	public List<String> getFeatureVectorNames() { return featureVectorNames; }
	public List<String> getNamesInFeatureSet() { return namesInFeatureSet; }
	public Map<String, BitSet> getNameToFeatureSetMap() {
		return nameToFeatureSetMap; }

	public void setFeatureVectorNames(List<String> featureVectorNames) {
		this.featureVectorNames = featureVectorNames; }
	// #endregion ACCESSORS ------------------------------------------------------
}