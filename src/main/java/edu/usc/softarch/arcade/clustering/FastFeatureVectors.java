package edu.usc.softarch.arcade.clustering;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.usc.softarch.arcade.classgraphs.StringEdge;
import edu.usc.softarch.arcade.functiongraph.TypedEdgeGraph;

/**
 * This class represents a graph as an adjacency matrix.
 */
public class FastFeatureVectors implements Serializable {
	// #region ATTRIBUTES --------------------------------------------------------
	private static final long serialVersionUID = -8870834810415855677L;
	private Set<String> featureVectorNames;
	private Set<String> namesInFeatureSet;
	private Map<String, BitSet> nameToFeatureSetMap;
	// #endregion ATTRIBUTES -----------------------------------------------------

	// #region CONSTRUCTORS ------------------------------------------------------
	public FastFeatureVectors(TypedEdgeGraph typedEdgeGraph) {
		this.featureVectorNames = new HashSet<>();
		this.namesInFeatureSet = new HashSet<>();
		this.nameToFeatureSetMap = new HashMap<>();

		initialize(typedEdgeGraph);
	}

	private void initialize(TypedEdgeGraph typedEdgeGraph) {
		Set<String> arcTypesSet = new HashSet<>();

		for (StringEdge edge : typedEdgeGraph.getEdges()) {
			arcTypesSet.add(edge.getType());
			featureVectorNames.add(edge.getSrcStr());
			namesInFeatureSet.add(edge.getTgtStr());
		}

		featureVectorNames.addAll(namesInFeatureSet);

		for (String source : featureVectorNames) {
			BitSet featureSet = new BitSet(namesInFeatureSet.size());
			
			for (String arcType : arcTypesSet) {
				int index = 0;
				for (String target : namesInFeatureSet) {
					if (typedEdgeGraph.containsEdge(arcType, source, target))
						featureSet.set(index, true);
					index++;
				}
			}

			this.nameToFeatureSetMap.put(source, featureSet);
		}
	}
	// #endregion CONSTRUCTORS ---------------------------------------------------

	// #region ACCESSORS ---------------------------------------------------------
	public List<String> getFeatureVectorNames() {
		return new ArrayList<>(featureVectorNames); }
	public List<String> getNamesInFeatureSet() {
		return new ArrayList<>(namesInFeatureSet); }
	public Map<String, BitSet> getNameToFeatureSetMap() {
		return new HashMap<>(nameToFeatureSetMap); }
	// #endregion ACCESSORS ------------------------------------------------------
}