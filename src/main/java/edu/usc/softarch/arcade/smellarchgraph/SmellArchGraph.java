package edu.usc.softarch.arcade.smellarchgraph;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Iterator;
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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.usc.softarch.arcade.clustering.Cluster;


/**
 * @author joshua
 */
public class SmellArchGraph implements Serializable {
	private static final long serialVersionUID = 8029563985930798840L;
	Set<ClusterEdge> edges;
	
	public SmellArchGraph() {
		super();
		edges = new HashSet<>();
	}
	
	public void addEdge(Cluster src, Cluster tgt) {
		edges.add(new ClusterEdge(src,tgt));
	}
	
	public void addEdge(ClusterEdge e) {
		edges.add(e);
	}
	
	public boolean containsEdge(Cluster src, Cluster tgt) {
		return edges.contains(new ClusterEdge(src,tgt));
	}
	
	public boolean containsEdge(ClusterEdge e) {
		return edges.contains(e);
	}
	
	public void removeEdge(ClusterEdge e) {
		edges.remove(e);
	}
	
	public void removeEdge(Cluster src, Cluster tgt) {
		edges.remove(new ClusterEdge(src,tgt));
	}
	
	public String toString() {
		Iterator<ClusterEdge> iter = edges.iterator();
		String str = "";
		
		int edgeCount = 0;
		while(iter.hasNext()) {
			ClusterEdge e = iter.next();
			str += edgeCount + ": " + e.toDotString();
			if(iter.hasNext()) {
				str+="\n";
			}
			edgeCount++;
		}
		
		return str;
	}
	
	public void writeDotFile(String filename) throws FileNotFoundException {
		File f = new File(filename);
		if (f.getParentFile() != null && !f.getParentFile().exists()) {
			f.getParentFile().mkdirs();
		}
		FileOutputStream fos = new FileOutputStream(f);
		OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8); 
		PrintWriter out = new PrintWriter(osw);
		
		Iterator<ClusterEdge> iter = edges.iterator();
		
		out.println("digraph G {");
		
		while(iter.hasNext()) {
			ClusterEdge e = iter.next();
			out.println(e.toDotString());
		}
		
		out.println("}");
		
		out.close();
	}
	
	public void writeXMLSmellArchGraph(String filename) throws ParserConfigurationException, TransformerException {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		  DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
	 
		  //classgraph elements
		  Document doc = docBuilder.newDocument();
		  Element rootElement = doc.createElement("SmellArchGraph");
		  doc.appendChild(rootElement);
	 
		  //classedge elements
		  for (ClusterEdge e : edges) {
			  Element ce = doc.createElement("ClusterEdge");
			  rootElement.appendChild(ce);
			  Element src = doc.createElement("src");
			  src.appendChild(doc.createTextNode(e.getSrc().toString()));
			  Element tgt = doc.createElement("tgt");
			  tgt.appendChild(doc.createTextNode(e.getTgt().toString()));
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
	 
		  System.out.println("In " + Thread.currentThread().getStackTrace()[1].getClassName() 
				  + ". " + Thread.currentThread().getStackTrace()[1].getMethodName() 
				  + ", Wrote " + filename);
	}
}
