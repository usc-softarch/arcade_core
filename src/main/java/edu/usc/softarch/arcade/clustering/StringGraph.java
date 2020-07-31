package edu.usc.softarch.arcade.clustering;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
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

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.usc.softarch.arcade.classgraphs.StringEdge;
import edu.usc.softarch.arcade.config.Config;

/**
 * @author joshua
 */
public class StringGraph implements Serializable {
	// #region FIELDS ------------------------------------------------------------
	private static final long serialVersionUID = -4879127696671797183L;
	private transient Logger logger = Logger.getLogger(StringGraph.class);
	public Set<StringEdge> edges = new HashSet<>();
	private String name = "";
	// #endregion FIELDS ---------------------------------------------------------
	
	// #region CONSTRUCTORS ------------------------------------------------------
	public StringGraph() { super();	}
	public StringGraph(String name) {
		super();
		this.name = name;
	}
	// #endregion CONSTRUCTORS ---------------------------------------------------
	
	// #region ACCESSORS ---------------------------------------------------------
	public void addEdge(String src, String tgt) {
		edges.add(new StringEdge(src,tgt)); }
	public void addEdge(StringEdge e) {	edges.add(e);	}
	public boolean containsEdge(String src, String tgt) {
		return edges.contains(new StringEdge(src,tgt));	}
	public boolean containsEdge(StringEdge e) {
		return edges.contains(e);	}
	public void removeEdge(StringEdge e) { edges.remove(e); }
	public void removeEdge(String src, String tgt) {
		edges.remove(new StringEdge(src,tgt)); }
	public String getName() {	return name; }
	public void setName(String name) { this.name = name; }
	// #endregion ACCESSORS ------------------------------------------------------

	// #region PROCESSING --------------------------------------------------------
	public String toString() {
		Iterator<StringEdge> iter = edges.iterator();
		String str = "";
		
		int edgeCount = 0;
		while(iter.hasNext()) {
			StringEdge e = iter.next();
			str += edgeCount + ": " + e.toDotString();
			if(iter.hasNext())
				str+="\n";
			edgeCount++;
		}
		
		return str;
	}
	// #endregion PROCESSING -----------------------------------------------------
	
	// #region IO ----------------------------------------------------------------
	public void writeNumberedNodeDotFileWithTextMappingFile(
			String clusterGraphDotFilename, 
			Map<String, Integer> clusterNameToNodeNumberMap,
			Map<Integer, String> nodeNumberToClusterNameMap
			) throws FileNotFoundException {
		File clusterGraphDotFile = new File(clusterGraphDotFilename);
		if ((clusterGraphDotFile.getParentFile() != null)
				&& (!clusterGraphDotFile.getParentFile().exists()))
			clusterGraphDotFile.getParentFile().mkdirs();
		
		FileOutputStream fos = new FileOutputStream(clusterGraphDotFile);
		OutputStreamWriter osw =
			new OutputStreamWriter(fos, StandardCharsets.UTF_8); 
		PrintWriter clusterGraphDotOut = new PrintWriter(osw);
		
		logger.debug("Writing mapping of clusters to integers to " + 
			"CurrProj.getNumbereNodeMappingTextFilename()...");
		
		File clusterGraphNumberNodeMappingFile = 
			new File(Config.getNumbereNodeMappingTextFilename());
		FileOutputStream numberNodeMappingFileOutputStream = 
			new FileOutputStream(clusterGraphNumberNodeMappingFile);
		OutputStreamWriter numberedNodeMappingOutputStreamWriter = 
			new OutputStreamWriter(numberNodeMappingFileOutputStream,
			StandardCharsets.UTF_8);
		PrintWriter clusterGraphNumberedNodeOut =
			new PrintWriter(numberedNodeMappingOutputStreamWriter);
		
		for (Map.Entry<Integer, String> e : nodeNumberToClusterNameMap.entrySet()) {  
			Integer key = e.getKey();  
			String value = e.getValue();  
			clusterGraphNumberedNodeOut.println(key + ": " + value);  
		}
	    
	  clusterGraphNumberedNodeOut.close();
		Iterator<StringEdge> iter = edges.iterator();
		clusterGraphDotOut.println("digraph G {");
		
		while(iter.hasNext()) {
			StringEdge e = iter.next();
			clusterGraphDotOut.println(
				e.toNumberedNodeDotString(clusterNameToNodeNumberMap));
		}
		
		clusterGraphDotOut.println("}");
		clusterGraphDotOut.close();
	}
	
	public void writeDotFile(String filename) throws FileNotFoundException {
		File f = new File(filename);
		if ((f.getParentFile() != null) && !f.getParentFile().exists()) {
			f.getParentFile().mkdirs();
		}
		FileOutputStream fos = new FileOutputStream(f);
		OutputStreamWriter osw = 
			new OutputStreamWriter(fos, StandardCharsets.UTF_8);
		PrintWriter out = new PrintWriter(osw);
		
		Iterator<StringEdge> iter = edges.iterator();
		
		out.println("digraph G {");
		
		while(iter.hasNext()) {
			StringEdge e = iter.next();
			out.println(e.toDotString());
		}
		
		out.println("}");
		
		out.close();
	}
	
	public void writeXMLClusterGraph(String filename)
			throws ParserConfigurationException, TransformerException {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
	
		//classgraph elements
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("ClusterGraph");
		doc.appendChild(rootElement);
	
		//classedge elements
		for (StringEdge e : edges) {
			Element ce = doc.createElement("StringEdge");
			rootElement.appendChild(ce);
			Element src = doc.createElement("src");
			src.appendChild(doc.createTextNode(e.srcStr));
			Element tgt = doc.createElement("tgt");
			tgt.appendChild(doc.createTextNode(e.tgtStr));
			ce.appendChild(src);
			ce.appendChild(tgt);
		}
	
		//write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		DOMSource source = new DOMSource(doc);
		StreamResult result =  new StreamResult(new File(filename));
		transformer.transform(source, result);
	
		logger.debug(
			"In "	+ Thread.currentThread().getStackTrace()[1].getClassName() 
			+ ". " + Thread.currentThread().getStackTrace()[1].getMethodName() 
			+ ", Wrote " + filename);
	}
	// #endregion IO -------------------------------------------------------------
}
