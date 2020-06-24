package edu.usc.softarch.arcade.clustering;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
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
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.usc.softarch.arcade.classgraphs.ClassGraph;
import edu.usc.softarch.arcade.classgraphs.SootClassEdge;
import edu.usc.softarch.arcade.classgraphs.StringEdge;
import edu.usc.softarch.arcade.config.Config;
import edu.usc.softarch.arcade.facts.driver.RsfReader;
import edu.usc.softarch.arcade.functiongraph.TypedEdgeGraph;
import edu.usc.softarch.arcade.functiongraph.StringTypedEdge;
import edu.usc.softarch.arcade.util.ObjectSizer;

import soot.SootClass;

/**
 * @author joshua
 *
 */
public class FeatureVectorMap {
	
	public HashMap<SootClass, FeatureVector> sc_fv_map = new HashMap<SootClass, FeatureVector>();
	public HashMap<String, FeatureVector> featureVectorNameToFeatureVectorMap = new HashMap<String, FeatureVector>(1500);
	public HashMap<String, BitSet> nameToFeatureSetMap = new HashMap<String, BitSet>(1500);
	
	boolean DEBUG = false;
	Logger logger = Logger.getLogger(FeatureVectorMap.class);
	private ArrayList<String> endNodesListWithNoDupes;
	private HashSet<String> startNodesSet;
	private Set<String> allNodesSet;
	
	public void serializeAsFastFeatureVectors() {
		
		FastFeatureVectors ffv = convertToFastFeatureVectors();
		
		ObjectOutput out;
		try {
			out = new ObjectOutputStream(new FileOutputStream(
					Config.getFastFeatureVectorsFilename()));
			out.writeObject(ffv);
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public FastFeatureVectors convertToFastFeatureVectors() {
		return new FastFeatureVectors(new ArrayList<String>(allNodesSet),nameToFeatureSetMap,endNodesListWithNoDupes);
	}
	
	public void serializeNamesInFeatureSet() {

		// Serialize to a file
		ObjectOutput out;
		try {
			out = new ObjectOutputStream(new FileOutputStream(
					Config.getNamesInFeatureSetFilename()));
			out.writeObject(endNodesListWithNoDupes);
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	
	public void serializeNameToBitSetMap() {
		
	    // Serialize to a file
	    ObjectOutput out;
		try {
			out = new ObjectOutputStream(new FileOutputStream(Config.getNameToFeatureSetMapFilename()));
			out.writeObject(nameToFeatureSetMap);
		    out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	}

	public FeatureVectorMap(HashMap<SootClass, FeatureVector> vecMap) {
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
		
		HashSet<StringTypedEdge> edges = functionGraph.getEdges();
		
		List<String> arcTypesList = Lists.transform(
				new ArrayList<StringTypedEdge>(edges),
				new Function<StringTypedEdge, String>() {
					public String apply(StringTypedEdge edge) {
						return edge.arcTypeStr;
					}
				});
		
		HashSet<String> arcTypesSet = Sets.newHashSet(arcTypesList);

		List<String> startNodesList = Lists.transform(
				new ArrayList<StringTypedEdge>(edges),
				new Function<StringTypedEdge, String>() {
					public String apply(StringTypedEdge edge) {
						return edge.srcStr;
					}
				});

		startNodesSet = Sets.newHashSet(startNodesList);

		List<String> endNodesList = Lists.transform(
				new ArrayList<StringTypedEdge>(edges),
				new Function<StringTypedEdge, String>() {
					public String apply(StringTypedEdge edge) {
						return edge.tgtStr;
					}
				});
		TreeSet<String> endNodesSet = Sets.newTreeSet(endNodesList);
		endNodesListWithNoDupes = Lists.newArrayList(endNodesSet);
		
		List<String> allNodesList = new ArrayList<String>(startNodesList);
		allNodesList.addAll(endNodesListWithNoDupes);
		
		allNodesSet = new HashSet<String>(allNodesList);
		
		Set<String> nonStartNodes = new HashSet<String>(allNodesSet);
		nonStartNodes.removeAll(startNodesSet);
		
		int totalTrueBits = 0;
		for (String source : allNodesSet) {
			//FeatureVector vec = new FeatureVector();
			//vec.name = source.toString();
			BitSet featureSet = new BitSet(endNodesListWithNoDupes.size());
			for (String arcType : arcTypesSet) {
				int bitIndex =0;
				for (String target : endNodesListWithNoDupes) {
					if (functionGraph.containsEdge(arcType, source, target)) {
						//logger.debug(arcType  + ", " + source + ", " + target + ": true");
						//vec.add(new Feature(new StringEdge(source,target), 1));
						featureSet.set(bitIndex, true);
					} /*else {
						//logger.debug(arcType  + ", " + source + ", " + target + ": false");
						//vec.add(new Feature(new StringEdge(source,target), 0));
						featureSet.set(bitIndex,false);
					}*/
					bitIndex++;
				}
			}
			
			logger.debug(featureSet);
			totalTrueBits += featureSet.cardinality();
			
			/*if (featureSet.size() != endNodesListWithNoDupes.size()) {
				logger.error("feature set and nodes list are not equal in size: ");
				logger.error("feature set size: " + featureSet.size());
				logger.error("end nodes list without duplicates: " + endNodesListWithNoDupes.size());
				System.err.println("feature set and nodes list are not equal in size - see log for more details");
			}*/
			
			nameToFeatureSetMap.put(source, featureSet);
			
			/*if (measureMemoryUsage) {
				double vecSize = 0;
				try {
					vecSize = ObjectSizer.sizeOf(vec).length;
				}
				catch (IOException e) {
					e.printStackTrace();
				}
				double vecSizeInMB = vecSize/(1024*1024);
				logger.debug("vec size in KB: " + vecSize);
				logger.debug("vec size in KB: " + vecSizeInMB);
			}*/
			
			
			//featureVectorNameToFeatureVectorMap.put(source, vec);
			
		}
		
		logger.debug("total true bits among feature sets: " + totalTrueBits);
		
		HashSet<List<String>> featureSetEdges = new HashSet<List<String>>();
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
		
		/*double mapSize = 0;
		try {
			mapSize = ObjectSizer.sizeOf(featureVectorNameToFeatureVectorMap).length;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		double mapSizeInMB = mapSize/(1024*1024);
		
		logger.debug("map size in MB: " + mapSizeInMB);*/
		//logger.debug("Printing feature vector name to feature vector hash map...");
		//logger.debug("map size: " + featureVectorNameToFeatureVectorMap.size());
		//logger.debug(Joiner.on("\n").withKeyValueSeparator("->").join(featureVectorNameToFeatureVectorMap));
		
	}

	private void initializeMaps() {
		sc_fv_map = new HashMap<SootClass, FeatureVector>();
		featureVectorNameToFeatureVectorMap = new HashMap<String, FeatureVector>();
	}

	public void writeXMLFeatureVectorMapUsingFunctionDepEdges()
			throws TransformerException, ParserConfigurationException {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		// classgraph elements
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("FeatureVectorMap");
		doc.appendChild(rootElement);

		// classedge elements
		logger.trace("Printing out feature vector map...");
		for (String source : allNodesSet) {
			Element fvElem = doc.createElement("FeatureVector");
			rootElement.appendChild(fvElem);

			// set attribute to staff element
			Attr attr = doc.createAttribute("name");
			attr.setValue(source);
			fvElem.setAttributeNode(attr);

			rootElement.appendChild(fvElem);
			
			BitSet featureSet = nameToFeatureSetMap.get(source);
			for (int i=0;i<endNodesListWithNoDupes.size();i++) {
				String target = endNodesListWithNoDupes.get(i);
				
				Element fElem = doc.createElement("Feature");
				fvElem.appendChild(fElem);
				Element ce = doc.createElement("ClassEdge");
				fElem.appendChild(ce);

				Element src = doc.createElement("src");
				src.appendChild(doc.createTextNode(source));

				Element tgt = doc.createElement("tgt");
				tgt.appendChild(doc.createTextNode(target));

				ce.appendChild(src);
				ce.appendChild(tgt);

				Element valueElem = doc.createElement("value");
				fElem.appendChild(valueElem);
				if (featureSet.get(i))
					valueElem.appendChild(doc.createTextNode("1"));
				else
					valueElem.appendChild(doc.createTextNode("0"));
			}

		}

		// write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory
				.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(
				"{http://xml.apache.org/xslt}indent-amount", "2");
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

	public void writeXMLFeatureVectorMapUsingSootClassEdges() throws TransformerException,
			ParserConfigurationException {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory
				.newInstance();
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
			attr.setValue(fv.name);
			fvElem.setAttributeNode(attr);

			rootElement.appendChild(fvElem);
			for (Feature f : fv) {
				Element fElem = doc.createElement("Feature");
				fvElem.appendChild(fElem);
				Element ce = doc.createElement("ClassEdge");
				fElem.appendChild(ce);
				Element src = doc.createElement("src");
				
				SootClassEdge fSootEdge = null;
				
				if (f.edge instanceof SootClassEdge) {
					fSootEdge = (SootClassEdge) f.edge;
				}
				if (fSootEdge != null) 
					src.appendChild(doc.createTextNode(fSootEdge.src.toString()));
				else
					src.appendChild(doc.createTextNode(f.edge.srcStr));
				
				
				Element tgt = doc.createElement("tgt");
				
				if (fSootEdge !=null) 
					tgt.appendChild(doc.createTextNode(fSootEdge.tgt.toString()));
				else 
					tgt.appendChild(doc.createTextNode(f.edge.tgtStr));
				
				Element type = doc.createElement("type");
				type.appendChild(doc.createTextNode(fSootEdge.getType()));
				
				ce.appendChild(src);
				ce.appendChild(tgt);
				ce.appendChild(type);

				Element valueElem = doc.createElement("value");
				fElem.appendChild(valueElem);
				if (f.value == 1)
					valueElem.appendChild(doc.createTextNode("1"));
				else if (f.value == 0)
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
			vec.name = caller.toString();
			for (SootClass c : clg.getNodes()) {
				SootClassEdge currEdge = null;
				for (SootClassEdge edge : clg.getEdges()) {
					currEdge = edge;
					if (edge.getSrc().getName().trim().equals(c.getName().trim())) {
						vec.add(new Feature(new SootClassEdge(edge), 1));
					}
				}
				vec.add(new Feature(new SootClassEdge(currEdge), 0));
			}
			sc_fv_map.put(caller, vec);
		}
	}

	public void loadClassGraphBasedXMLFeatureVectorMap() throws ParserConfigurationException,
			SAXException, IOException {
		File fXmlFile = new File(Config.getXMLFeatureVectorMapFilename());
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);
		doc.getDocumentElement().normalize();
		
		if (DEBUG) {
			System.out.println("Root element :"
					+ doc.getDocumentElement().getNodeName());
		}
		NodeList fvList = doc.getElementsByTagName("FeatureVector");
		if (DEBUG) {
			System.out.println("----------------------- size: " + fvList.getLength());
		}

		for (int i = 0; i < fvList.getLength(); i++) {
			FeatureVector fv = new FeatureVector();
			Node fvNode = fvList.item(i);
			if (fvNode.getNodeType() == Node.ELEMENT_NODE) {
				Element fvElem = (Element) fvNode;	
				fv.name = fvElem.getAttribute("name");
				NodeList fList = fvElem.getElementsByTagName("Feature");
				if (DEBUG) {
					System.out.println("\t" + fvNode.getNodeName() + "");
					System.out.println("\t----------------------- size:"
							+ fList.getLength() + ", name: "
							+ fvElem.getAttribute("name"));
				}

				for (int j = 0; j < fList.getLength(); j++) {
					Feature f = new Feature();
					Node fNode = fList.item(j);
					//System.out.println("\t\t" + j);
					if (fNode.getNodeType() == Node.ELEMENT_NODE) {
						obtainFeatureData(f, fNode);						
						fv.add(f);
					}
				}
				featureVectorNameToFeatureVectorMap.put(fv.name,fv);
			} // end if
		} // end outer for loop on FeatureVectors
		if (DEBUG) {
			System.out.println("Pretty printing the name_fv_map:");
			prettyPrintHashMap(featureVectorNameToFeatureVectorMap);
			System.out.println("Printing name_fv_map: " + featureVectorNameToFeatureVectorMap);
		}
	}

	private void prettyPrintHashMap(HashMap<String, FeatureVector> map) {
		Iterator iter = map.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry pair = (Map.Entry) iter.next();
			logger.debug(pair);
		}
		
	}
	
	private void obtainFeatureData(Feature f, Node fNode) {
		if (DEBUG) {
			System.out.println("\t\t" + fNode.getNodeName());
			System.out.println("\t\t-----------------------");
		}
		
		Element fElement = (Element) fNode;
		
		//System.out.println("\t\t\tClassEdge : "  + getNode("ClassEdge",fElement).getNodeName());
		
		if (DEBUG) {
			System.out.println("\t\tvalue : " + getTagValue("value", fElement));
		}
		
		if(getTagValue("value",fElement).equals("0")) {
			f.value = 0;
		}
		else 
			f.value = 1;

		
		NodeList fChildren = fElement.getElementsByTagName("ClassEdge");
		for (int k = 0;k<fChildren.getLength();k++) {
			Node childNode = fChildren.item(k);
			Element childElem = (Element) childNode;
			if (DEBUG) {
				System.out.println("\t\t\tSource : "
						+ getTagValue("src", childElem));
				System.out.println("\t\t\tTarget : "
						+ getTagValue("tgt", childElem));
			}
			
			StringEdge edge = new StringEdge(getTagValue("src",childElem),getTagValue("tgt",childElem));
			f.edge = edge;
			
			
		}
	}
	
	private static Node getNode(String sTag, Element eElement) {
		NodeList nlList = eElement.getElementsByTagName(sTag).item(0)
				.getChildNodes();
		Node nValue = (Node) nlList.item(0);

		return nValue;
	}

	private static String getTagValue(String sTag, Element eElement) {
		NodeList nlList = eElement.getElementsByTagName(sTag).item(0)
				.getChildNodes();
		Node nValue = (Node) nlList.item(0);

		return nValue.getNodeValue();
	}

	public void loadFunctionDepGraphBasedXMLFeatureVectorMap() {
		// TODO Auto-generated method stub
		
	}

}