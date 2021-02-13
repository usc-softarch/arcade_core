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

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.usc.softarch.arcade.classgraphs.ClassGraph;
import edu.usc.softarch.arcade.classgraphs.SootClassEdge;
import edu.usc.softarch.arcade.classgraphs.StringEdge;
import edu.usc.softarch.arcade.config.Config;
import edu.usc.softarch.arcade.facts.driver.RsfReader;
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

	// #region IO ----------------------------------------------------------------
	public void serializeAsFastFeatureVectors() {
		FastFeatureVectors ffv = convertToFastFeatureVectors();
		
		try(ObjectOutput out = new ObjectOutputStream(
				new FileOutputStream(Config.getFastFeatureVectorsFilename()))) {
			out.writeObject(ffv);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	// #endregion IO -------------------------------------------------------------

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

	private void initializeNodeSets(TypedEdgeGraph functionGraph) {
		// Get edges of the graph
		Set<StringEdge> edges = functionGraph.getEdges();
		
		// Get a set of the types of edges
		List<String> arcTypesList = Lists.transform(
			new ArrayList<StringEdge>(edges),	StringEdge::getType);
		this.arcTypesSet = Sets.newHashSet(arcTypesList);

		// Get a set of the start nodes
		List<String> startNodesList = Lists.transform(
				new ArrayList<StringEdge>(edges),	StringEdge::getSrcStr);
		this.startNodesSet = Sets.newHashSet(startNodesList);

		// Get a set of the target nodes
		List<String> endNodesList = Lists.transform(
				new ArrayList<StringEdge>(edges), StringEdge::getTgtStr);
		TreeSet<String> endNodesSet = Sets.newTreeSet(endNodesList);
		this.endNodesListWithNoDupes = Lists.newArrayList(endNodesSet);
		
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

	public void writeXMLFeatureVectorMapUsingSootClassEdges() throws TransformerException,
			ParserConfigurationException {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		// classgraph elements
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("FeatureVectorMap");
		doc.appendChild(rootElement);

		// classedge elements
		logger.trace("Printing out feature vector map...");
		for (FeatureVector fv : sc_fv_map.values()) {
			logger.trace(fv);
			Element fvElem = doc.createElement("FeatureVector");
			rootElement.appendChild(fvElem);

			// set attribute to staff element
			Attr attr = doc.createAttribute("name");
			attr.setValue(fv.getName());
			fvElem.setAttributeNode(attr);

			rootElement.appendChild(fvElem);
			for (Feature f : fv) {
				Element fElem = doc.createElement("Feature");
				fvElem.appendChild(fElem);
				Element ce = doc.createElement("ClassEdge");
				fElem.appendChild(ce);
				Element src = doc.createElement("src");
				
				SootClassEdge fSootEdge = null;
				
				if (f.getEdge() instanceof SootClassEdge) {
					fSootEdge = (SootClassEdge) f.getEdge();
				}
				if (fSootEdge != null) 
					src.appendChild(doc.createTextNode(fSootEdge.getSrc().toString()));
				else
					src.appendChild(doc.createTextNode(f.getEdge().getSrcStr()));
				
				
				Element tgt = doc.createElement("tgt");
				
				if (fSootEdge !=null) 
					tgt.appendChild(doc.createTextNode(fSootEdge.getTgt().toString()));
				else 
					tgt.appendChild(doc.createTextNode(f.getEdge().getTgtStr()));
				
				Element type = doc.createElement("type");
				type.appendChild(doc.createTextNode(fSootEdge.getType()));
				
				ce.appendChild(src);
				ce.appendChild(tgt);
				ce.appendChild(type);

				Element valueElem = doc.createElement("value");
				fElem.appendChild(valueElem);
				if (f.getValue() == 1)
					valueElem.appendChild(doc.createTextNode("1"));
				else if (f.getValue() == 0)
					valueElem.appendChild(doc.createTextNode("0"));
			}
		}

		// write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory
				.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new File(
				Config.getXMLFeatureVectorMapFilename()));
		transformer.transform(source, result);

		System.out.println("In "
				+ Thread.currentThread().getStackTrace()[1].getClassName()
				+ ". "
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ ", Wrote " + Config.getXMLFeatureVectorMapFilename());

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