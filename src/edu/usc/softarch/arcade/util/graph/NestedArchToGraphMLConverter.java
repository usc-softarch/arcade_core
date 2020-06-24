package edu.usc.softarch.arcade.util.graph;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.usc.softarch.arcade.clustering.util.ClusterUtil;
import edu.usc.softarch.arcade.facts.driver.RsfReader;

public class NestedArchToGraphMLConverter {
	public static void main(String[] args) {
		String depsFilename = args[0];
		String clustersFilename = args[1];
		String graphMLFilename = args[2];
		
		RsfReader.loadRsfDataFromFile(depsFilename);
		List<List<String>> depFacts = RsfReader.unfilteredFacts;
		
		RsfReader.loadRsfDataFromFile(clustersFilename);
		List<List<String>> clusterFacts = RsfReader.unfilteredFacts;
		
		Map<String,Set<String>> clusterMap = ClusterUtil.buildClusterMap(clusterFacts);

		try {

			DocumentBuilderFactory docFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("company");
			doc.appendChild(rootElement);
			
			// topmost graph element
			Element graphElem = doc.createElement("graph");
			rootElement.appendChild(graphElem);
	 
			// set attribute id and edgedefault
			Attr IdAttr = doc.createAttribute("id");
			IdAttr.setValue("G");
			graphElem.setAttributeNode(IdAttr);
			
			Attr edgeDefaultAttr = doc.createAttribute("edgedefault");
			edgeDefaultAttr.setValue("directed");
			graphElem.setAttributeNode(edgeDefaultAttr);
			
			for (String clusterName : clusterMap.keySet()) {
				Element clusterNodeElem = doc.createElement("node");
				graphElem.appendChild(clusterNodeElem);
				
				Attr clusterIdAttr = doc.createAttribute("id");
				clusterIdAttr.setValue(clusterName);
				clusterNodeElem.setAttributeNode(clusterIdAttr);
				
				Set<String> entities = clusterMap.get(clusterName);
				
				// nested graph element
				Element nestedGraphElem = doc.createElement("graph");
				clusterNodeElem.appendChild(nestedGraphElem);
				
				// set attribute id and edgedefault
				Attr nestedGraphIdAttr = doc.createAttribute("id");
				nestedGraphIdAttr.setValue(clusterName + ":");
				nestedGraphElem.setAttributeNode(nestedGraphIdAttr);
				
				Attr nestedEdgeDefaultAttr = doc.createAttribute("edgedefault");
				nestedEdgeDefaultAttr.setValue("directed");
				nestedGraphElem.setAttributeNode(nestedEdgeDefaultAttr);
				
				for (String entity : entities) {
					Element nestedNodeElem = doc.createElement("node");
					nestedGraphElem.appendChild(nestedNodeElem);
					
					Attr nostedNodeId = doc.createAttribute("id");
					nostedNodeId.setValue(entity);
					nestedNodeElem.setAttributeNode(nostedNodeId);
				}
				
				int edgeCounter = 0;
				for (List<String> fact : depFacts) {
					String source = fact.get(1);
					String target = fact.get(2);
					
					Element edgeElem = doc.createElement("edge");
					graphElem.appendChild(edgeElem);
					
					Attr edgeIdAttr = doc.createAttribute("id");
					edgeIdAttr.setValue(Integer.toString(edgeCounter));
					edgeElem.setAttributeNode(edgeIdAttr);
					
					Attr sourceAttr = doc.createAttribute("source");
					sourceAttr.setValue(source);
					edgeElem.setAttributeNode(sourceAttr);
					
					Attr targetAttr = doc.createAttribute("target");
					targetAttr.setValue(target);
					edgeElem.setAttributeNode(targetAttr);
					
					edgeCounter++;
				}
			}
			
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(graphMLFilename));
	 
			// Output to console for testing
			// StreamResult result = new StreamResult(System.out);
	 
			transformer.transform(source, result);
	 
			System.out.println(graphMLFilename + " saved");
			
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
		}
	}
}
