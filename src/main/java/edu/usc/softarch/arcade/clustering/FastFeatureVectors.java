package edu.usc.softarch.arcade.clustering;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.usc.softarch.arcade.facts.driver.RsfReader;
import edu.usc.softarch.arcade.functiongraph.StringTypedEdge;
import edu.usc.softarch.arcade.functiongraph.TypedEdgeGraph;

public class FastFeatureVectors implements Serializable {
	private static final long serialVersionUID = -8870834810415855677L;
	private static Logger logger = LogManager.getLogger(FastFeatureVectors.class);

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

	public FastFeatureVectors(TypedEdgeGraph functionGraph) {
		constructFastFeatureVectorsFromTypedEdgeGraph(functionGraph);
	}

	public List<String> getFeatureVectorNames() {
		return featureVectorNames;
	}

	public void setFeatureVectorNames(List<String> featureVectorNames) {
		this.featureVectorNames = featureVectorNames;
	}

	private void constructFastFeatureVectorsFromTypedEdgeGraph(
			TypedEdgeGraph functionGraph) {
		Set<StringTypedEdge> edges = functionGraph.getEdges();
		
		List<String> arcTypesList = Lists.transform(
				new ArrayList<StringTypedEdge>(edges),
				(StringTypedEdge edge) -> edge.arcTypeStr
				);
		
		HashSet<String> arcTypesSet = Sets.newHashSet(arcTypesList);

		List<String> startNodesList = Lists.transform(
				new ArrayList<StringTypedEdge>(edges),
				(StringTypedEdge edge) -> edge.getSrcStr()
				);

		Set<String> startNodesSet = Sets.newHashSet(startNodesList);

		List<String> endNodesList = Lists.transform(
				new ArrayList<StringTypedEdge>(edges),
				(StringTypedEdge edge) -> edge.getTgtStr()
				);
		TreeSet<String> endNodesSet = Sets.newTreeSet(endNodesList);
		List<String> endNodesListWithNoDupes = Lists.newArrayList(endNodesSet);
		
		List<String> allNodesList = new ArrayList<>(startNodesList);
		allNodesList.addAll(endNodesListWithNoDupes);
		
		Set<String> allNodesSet = new HashSet<>(allNodesList);
		
		Set<String> nonStartNodes = new HashSet<>(allNodesSet);
		nonStartNodes.removeAll(startNodesSet);
		
		int totalTrueBits = 0;
		for (String source : allNodesSet) {
			BitSet featureSet = new BitSet(endNodesListWithNoDupes.size());
			for (String arcType : arcTypesSet) {
				int bitIndex =0;
				for (String target : endNodesListWithNoDupes) {
					if (functionGraph.containsEdge(arcType, source, target)) {
						featureSet.set(bitIndex, true);
					}
					bitIndex++;
				}
			}
			
			logger.debug(featureSet);
			totalTrueBits += featureSet.cardinality();

			nameToFeatureSetMap.put(source, featureSet);
		}
		
		logger.debug("total true bits among feature sets: " + totalTrueBits);
		
		HashSet<List<String>> featureSetEdges = new HashSet<>();
		logger.debug("Printing edges represented by feature sets...");
		for (String source : startNodesSet) {
			BitSet featureSet = nameToFeatureSetMap.get(source);
			for (int i=0;i<featureSet.size();i++) {
				if (featureSet.get(i)) {
					String target = endNodesListWithNoDupes.get(i);
					logger.debug(source + " " + target);
					featureSetEdges.add(Lists.newArrayList(source,target));
				}
			}
		}
		
		if (RsfReader.untypedEdgesSet != null) {
			Set<List<String>> intersectionSet = Sets.intersection(
					featureSetEdges, RsfReader.untypedEdgesSet);
			logger.debug("Printing intersection of rsf reader untyped edges set and feature set edges...");
			logger.debug("intersection set size: " + intersectionSet.size());
			logger.debug(Joiner.on("\n").join(intersectionSet));

			Set<List<String>> differenceSet = Sets.difference(featureSetEdges,
					RsfReader.untypedEdgesSet);
			logger.debug("Printing difference of rsf reader untyped edges set and feature set edges...");
			logger.debug("difference set size: " + differenceSet.size());
			logger.debug(Joiner.on("\n").join(differenceSet));
		}

		this.setFeatureVectorNames(new ArrayList<>(allNodesSet));
		this.namesInFeatureSet = endNodesListWithNoDupes;
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
