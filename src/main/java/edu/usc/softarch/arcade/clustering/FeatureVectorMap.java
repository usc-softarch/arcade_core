package edu.usc.softarch.arcade.clustering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import edu.usc.softarch.arcade.classgraphs.ClassGraph;
import edu.usc.softarch.arcade.classgraphs.SootClassEdge;
import edu.usc.softarch.arcade.classgraphs.StringEdge;
import edu.usc.softarch.arcade.functiongraph.TypedEdgeGraph;

import soot.SootClass;

/**
 * Representation of a graph as an adjacency matrix. Each bit vector in the
 * matrix represents all possible edges from a given source and each valid
 * target.
 * 
 * @author joshua
 */
public class FeatureVectorMap {
	// #region FIELDS ------------------------------------------------------------
	private static Logger logger =
		LogManager.getLogger(FeatureVectorMap.class);

	private Map<SootClass, FeatureVector> sc_fv_map = new HashMap<>();
	private Map<String, BitSet> nameToFeatureSetMap = new HashMap<>(1500);
	private List<String> endNodesListWithNoDupes;
	private Set<String> allNodesSet;
	private Set<String> arcTypesSet;
	private Set<String> startNodesSet;
	// #endregion FIELDS ---------------------------------------------------------

	// #region CONSTRUCTORS ------------------------------------------------------
	public FeatureVectorMap(Map<SootClass, FeatureVector> vecMap) {
		this.sc_fv_map = vecMap; }
	
	public FeatureVectorMap(ClassGraph clg) {
		constructFeatureVectorMapFromClassGraph(clg); }

	public FeatureVectorMap(TypedEdgeGraph typedEdgeGraph) {
		constructFeatureVectorMapFromTypedEdgeGraph(typedEdgeGraph); }
	// #endregion CONSTRUCTORS ---------------------------------------------------

	// #region VERIFIED METHODS --------------------------------------------------
	public FastFeatureVectors convertToFastFeatureVectors() {
		return new FastFeatureVectors(
			new ArrayList<>(allNodesSet),
			nameToFeatureSetMap,
			endNodesListWithNoDupes);
	}

	private void constructFeatureVectorMapFromTypedEdgeGraph(
			TypedEdgeGraph functionGraph) {
		initializeNodeSets(functionGraph);
		buildAdjacencyMatrix(functionGraph);
		
		// -------------------------------------------------------------------------

		//TODO This entire block of code below seems to be used only by C fact
		// fact extraction. I need to review this when I get to that. Either way,
		// it doesn't seem to serve any purpose other than debugging.

		HashSet<List<String>> featureSetEdges = new HashSet<>();
		logger.debug("Printing edges represented by feature sets...");
		// For each source in the graph
		for (String source : startNodesSet) {
			// Get the vector representing that node's edges
			BitSet featureSet = nameToFeatureSetMap.get(source);
			// And then for each potential edge in the vector
			for (int i=0; i < featureSet.size(); i++) {
				// If that edge exists in the graph
				if (featureSet.get(i)) {
					// Add it to the featureSetEdges and print it
					String target = this.endNodesListWithNoDupes.get(i);
					logger.debug(source + " " + target);
					featureSetEdges.add(Arrays.asList(source,target));
				}
			}
		}
	}

	private void initializeNodeSets(TypedEdgeGraph functionGraph) {
		// Get edges of the graph
		Set<StringEdge> edges = functionGraph.getEdges();
		
		// Get a set of the types of edges
		List<String> arcTypesList = edges.stream()
			.map(StringEdge::getType).collect(Collectors.toList());
		this.arcTypesSet = new HashSet<>(arcTypesList);

		// Get a set of the start nodes
		List<String> startNodesList = edges.stream()
			.map(StringEdge::getSrcStr).collect(Collectors.toList());
		this.startNodesSet = new HashSet<>(startNodesList);

		// Get a set of the target nodes
		List<String> endNodesList = edges.stream()
			.map(StringEdge::getTgtStr).collect(Collectors.toList());
		TreeSet<String> endNodesSet = new TreeSet<>(endNodesList);
		this.endNodesListWithNoDupes = new ArrayList<>(endNodesSet);
		
		// Get the set of all nodes in the graph
		List<String> allNodesList = new ArrayList<>(startNodesList);
		allNodesList.addAll(endNodesListWithNoDupes);
		this.allNodesSet = new HashSet<>(allNodesList);
	}

	private void buildAdjacencyMatrix(TypedEdgeGraph functionGraph) {
		// Represents the number of edges in the graph
		int totalTrueBits = 0;

		// For each node in the graph
		for (String source : this.allNodesSet) {
			// Create a vector representing all possible edges from this node to
			// each potential target node
			BitSet featureSet = new BitSet(endNodesListWithNoDupes.size());
			// And then for each node type
			for (String arcType : arcTypesSet) {
				int bitIndex = 0;
				// and for each potential target node
				for (String target : endNodesListWithNoDupes) {
					// check if an edge exists between source and target
					if (functionGraph.containsEdge(arcType, source, target))
						// If so, set the equivalent target node's bit to 1
						featureSet.set(bitIndex, true);
					// Finally, increment the featureSet's index
					bitIndex++;
				}
			}
			
			logger.debug(featureSet);
			// Increment totalTrueBits by the number of outgoing edges from source
			totalTrueBits += featureSet.cardinality();

			// Add the edges vector to the edge matrix of this graph
			this.nameToFeatureSetMap.put(source, featureSet);
		}
		
		logger.debug("total true bits among feature sets: " + totalTrueBits);
	}
	// #endregion VERIFIED METHODS -----------------------------------------------

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