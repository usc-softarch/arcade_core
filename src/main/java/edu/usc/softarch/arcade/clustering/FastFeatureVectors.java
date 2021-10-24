package edu.usc.softarch.arcade.clustering;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

public class FastFeatureVectors implements Serializable {
	// #region ATTRIBUTES --------------------------------------------------------
	private static final long serialVersionUID = -8870834810415855677L;

	private List<String> featureVectorNames = new ArrayList<>();
	private List<String> namesInFeatureSet = new ArrayList<>();
	private Map<String, BitSet> nameToFeatureSetMap = new HashMap<>();
	private int numSourceEntities;
	// #endregion ATTRIBUTES -----------------------------------------------------

	// #region CONSTRUCTORS ------------------------------------------------------
	public FastFeatureVectors(Set<Map.Entry<String,String>> edges) {
		// Make a List with the sources of all edges
		List<String> startNodesList =
			edges.stream().map(Map.Entry::getKey).collect(Collectors.toList());

		this.numSourceEntities = new HashSet<>(startNodesList).size();

		// Make a List with the targets of all edges
		List<String> endNodesList =
		edges.stream().map(Map.Entry::getValue).collect(Collectors.toList());

		// Make a List after removing the duplicate targets
		Set<String> endNodesSet = new HashSet<>(endNodesList);
		List<String> endNodesListWithNoDupes = new ArrayList<>(endNodesSet);
		
		// Make a List with all sources and non-duplicate targets
		List<String> allNodesList = new ArrayList<>(startNodesList);
		allNodesList.addAll(endNodesListWithNoDupes);
		
		// Remove duplicates
		Set<String> allNodesSet = new HashSet<>(allNodesList);

		for (String source : allNodesSet) {
			BitSet featureSet = new BitSet(endNodesListWithNoDupes.size());
			int bitIndex = 0;
			for (String target : endNodesListWithNoDupes) {
				if (edges.contains(new
						AbstractMap.SimpleEntry<String,String>(source, target))) {
					featureSet.set(bitIndex, true);
				}
				bitIndex++;
			}

			this.nameToFeatureSetMap.put(source, featureSet);
		}

		this.setFeatureVectorNames(new ArrayList<>(allNodesSet));
		this.namesInFeatureSet = endNodesListWithNoDupes;
	}
	// #endregion CONSTRUCTORS ---------------------------------------------------

	public Map<String, BitSet> getNameToFeatureSetMap() {
		return nameToFeatureSetMap;	}

	public void setNameToFeatureSetMap(Map<String, BitSet> nameToFeatureSetMap){
		this.nameToFeatureSetMap = nameToFeatureSetMap; }

	public List<String> getNamesInFeatureSet() {
		return namesInFeatureSet;	}

	public void setNamesInFeatureSet(List<String> namesInFeatureSet) {
		this.namesInFeatureSet = namesInFeatureSet;
	}

	public int getNumSourceEntities() { return this.numSourceEntities; }

	public List<String> getFeatureVectorNames() {
		return featureVectorNames;
	}

	public void setFeatureVectorNames(List<String> featureVectorNames) {
		this.featureVectorNames = featureVectorNames;
	}

	// #region SERIALIZATION -----------------------------------------------------
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
	// #endregion SERIALIZATION --------------------------------------------------
}
