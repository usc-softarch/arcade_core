package edu.usc.softarch.arcade.clustering;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

public class FastFeatureVectors implements Serializable {
	private static final long serialVersionUID = -8870834810415855677L;
	private List<String> featureVectorNames = new ArrayList<>();
	private List<String> namesInFeatureSet = new ArrayList<>();
	private Map<String, BitSet> nameToFeatureSetMap = new HashMap<>();
	public Map<String, BitSet> getNameToFeatureSetMap() {
		return nameToFeatureSetMap;	}

	public void setNameToFeatureSetMap(Map<String, BitSet> nameToFeatureSetMap){
		this.nameToFeatureSetMap = nameToFeatureSetMap; }

	public List<String> getNamesInFeatureSet() {
		return namesInFeatureSet;	}

	public void setNamesInFeatureSet(List<String> namesInFeatureSet) {
		this.namesInFeatureSet = namesInFeatureSet;
	}

	FastFeatureVectors(List<String> featureNames, 
			Map<String, BitSet> nameToFeatureSetMap, 
			List<String> namesInFeatureSet) {
		this.setFeatureVectorNames(featureNames);
		this.nameToFeatureSetMap = nameToFeatureSetMap;
		this.namesInFeatureSet = namesInFeatureSet;
	}

	public List<String> getFeatureVectorNames() {
		return featureVectorNames;
	}

	public void setFeatureVectorNames(List<String> featureVectorNames) {
		this.featureVectorNames = featureVectorNames;
	}

	// #region SERIALIZATION ---------------------------------------------------
	public void serializeFFVectors(String filePath)
			throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(new File(filePath), this);
	}

	public static FastFeatureVectors deserializeFFVectors(String filePath)
			throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(new File(filePath), FastFeatureVectors.class);
	}
	// #endregion --------------------------------------------------------------
}
