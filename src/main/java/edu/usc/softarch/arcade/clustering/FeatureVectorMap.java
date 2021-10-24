package edu.usc.softarch.arcade.clustering;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.usc.softarch.arcade.classgraphs.ClassGraph;
import edu.usc.softarch.arcade.classgraphs.SootClassEdge;
import edu.usc.softarch.arcade.config.Config;
import edu.usc.softarch.arcade.facts.driver.RsfReader;
import edu.usc.softarch.arcade.functiongraph.TypedEdgeGraph;
import edu.usc.softarch.arcade.functiongraph.StringTypedEdge;

import soot.SootClass;

/**
 * @author joshua
 */
public class FeatureVectorMap {
	// #region FIELDS ------------------------------------------------------------
	public Map<SootClass, FeatureVector> sc_fv_map = new HashMap<>();
	public Map<String, FeatureVector> featureVectorNameToFeatureVectorMap =
		new HashMap<>(1500);
	public Map<String, BitSet> nameToFeatureSetMap = new HashMap<>(1500);
	
	private Logger logger = Logger.getLogger(FeatureVectorMap.class);
	private List<String> endNodesListWithNoDupes;
	private Set<String> startNodesSet;
	private Set<String> allNodesSet;
	// #endregion FIELDS ---------------------------------------------------------

	public FastFeatureVectors convertToFastFeatureVectors() {
		return new FastFeatureVectors(
			new ArrayList<>(allNodesSet),
			nameToFeatureSetMap,
			endNodesListWithNoDupes);
	}

	public FeatureVectorMap(Map<SootClass, FeatureVector> vecMap) {
		this.sc_fv_map = vecMap;
	}
	
	public FeatureVectorMap() {
		initializeMaps();
	}
	
	public FeatureVectorMap(ClassGraph clg) {
		constructFeatureVectorMapFromClassGraph(clg);
	}

	public FeatureVectorMap(TypedEdgeGraph typedEdgeGraph) {
		constructFeatureVectorMapFromTypedEdgeGraph(typedEdgeGraph);
	}

	private void constructFeatureVectorMapFromTypedEdgeGraph(
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

		startNodesSet = Sets.newHashSet(startNodesList);

		List<String> endNodesList = Lists.transform(
				new ArrayList<StringTypedEdge>(edges),
				(StringTypedEdge edge) -> edge.getTgtStr()
				);
		TreeSet<String> endNodesSet = Sets.newTreeSet(endNodesList);
		endNodesListWithNoDupes = Lists.newArrayList(endNodesSet);
		
		List<String> allNodesList = new ArrayList<>(startNodesList);
		allNodesList.addAll(endNodesListWithNoDupes);
		
		allNodesSet = new HashSet<>(allNodesList);
		
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
	}

	private void initializeMaps() {
		sc_fv_map = new HashMap<>();
		featureVectorNameToFeatureVectorMap = new HashMap<>();
	}

	public void constructFeatureVectorMapFromClassGraph(ClassGraph clg) {
		for (SootClass caller : clg.getNodes()) {
			FeatureVector vec = new FeatureVector();
			vec.setName(caller.toString());
			for (SootClass c : clg.getNodes()) {
				SootClassEdge currEdge = null;
				for (SootClassEdge edge : clg.getEdges()) {
					currEdge = edge;
					if (edge.getSrc().getName().trim().equals(c.getName().trim())) {
						vec.add(new Feature(new SootClassEdge(edge), 1.0));
					}
				}
				vec.add(new Feature(new SootClassEdge(currEdge), 0.0));
			}
			sc_fv_map.put(caller, vec);
		}
	}
}