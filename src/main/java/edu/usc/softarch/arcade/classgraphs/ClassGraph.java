package edu.usc.softarch.arcade.classgraphs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.usc.softarch.arcade.config.Config;

import soot.SootClass;

/**
 * @author joshua
 */
public class ClassGraph implements Serializable {
	private static final long serialVersionUID = -4718039019698373111L;
	private Set<SootClassEdge> edges = new HashSet<>();
	
	public ClassGraph() { super(); }
	
	public void generateRsf() {
		try (Writer out = new BufferedWriter(new FileWriter(Config.getClassGraphRsfFilename()))) {
			for (SootClassEdge edge : edges) {
				out.write(edge.toRsf() + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void serialize(String filename) throws IOException {
		// Write to disk with FileOutputStream
		FileOutputStream f_out = new 
			FileOutputStream(filename);

		// Write object with ObjectOutputStream
		ObjectOutputStream obj_out = new
			ObjectOutputStream (f_out);

		// Write object out to disk
		obj_out.writeObject ( this );
	}
	
	public List<SootClass> getNodes() {
		List<SootClass> cgNodes = new ArrayList<>();
		for (SootClassEdge e : edges) {
			if (!cgNodes.contains(e.getTgt())) {
				cgNodes.add(e.getTgt());
			}
			if (!cgNodes.contains(e.getSrc())) {
				cgNodes.add(e.getSrc());
			}
		}
		return cgNodes;
		
	}
	
	protected boolean hasClass(List<SootClass> cgNodes, SootClass inClass) {
		boolean hasClass = false;
		for (SootClass c : cgNodes) {
			if (inClass.toString().equals(c.toString())) {
				hasClass = true;
			}
		}
		return hasClass;
	}
	
	public List<SootClass> getCallerClasses(SootClass c) {
		List<SootClass> callerClasses = new ArrayList<>();
		for (SootClassEdge e : edges) {
			if (e.getTgt().toString().equals(c.toString())) {
				callerClasses.add(e.getSrc());
			}
		}
		
		return callerClasses;
	}
	
	public List<SootClass> getCalleeClasses(SootClass c) {
		List<SootClass> calleeClasses = new ArrayList<>();
		for (SootClassEdge e : edges) {
			if (e.getSrc().toString().equals(c.toString())) {
				calleeClasses.add(e.getTgt());
			}
		}
		
		return calleeClasses;
	}
	
	public void addEdge(SootClass src, SootClass tgt, String type) {
		edges.add(new SootClassEdge(src,tgt,type));
	}
	
	public void addEdge(SootClassEdge e) {
		edges.add(e);
	}
	
	public void removeEdge(SootClassEdge e) {
		edges.remove(e);
	}
	
	public void removeEdge(SootClass src, SootClass tgt,String type) {
		edges.remove(new SootClassEdge(src,tgt,type));
	}
	
	public boolean containsEdge(SootClass src, SootClass tgt, String type) {
		return edges.contains(new SootClassEdge(src,tgt,type));
	}
	
	public boolean containsEdge(SootClassEdge e) {
		return edges.contains(e);
	}
	
	public String toString() {
		Iterator<SootClassEdge> iter = edges.iterator();
		String str = "";
		
		while(iter.hasNext()) {
			SootClassEdge e = iter.next();
			str += e.toString();
			if(iter.hasNext()) {
				str+=",";
			}
		}
		
		return str;
	}
	
	public String toStringWithArchElemType() {
		Iterator<SootClassEdge> iter = edges.iterator();
		String str = "";
		
		while(iter.hasNext()) {
			SootClassEdge e = iter.next();
			str += e.toStringWithArchElemType();
			if(iter.hasNext()) {
				str+=",";
			}
		}
		
		return str;
	}

	public int size() {
		return edges.size();
	}
	
	public void writeDotFile(String filename) throws FileNotFoundException {
		File f = new File(filename);
		if (f.getParentFile() != null) {
			if (!f.getParentFile().exists()) {
				f.getParentFile().mkdirs();
			}
		}
		FileOutputStream fos = new FileOutputStream(f);
		OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8); 
		PrintWriter out = new PrintWriter(osw);
		
		Iterator<SootClassEdge> iter = edges.iterator();
		
		out.println("digraph G {");
		
		while(iter.hasNext()) {
			SootClassEdge e = iter.next();
			out.println(e.toDotString());
		}
		
		out.println("}");
		
		out.close();
	}
	
	public void writeDotFileWithArchElementType(String filename) throws FileNotFoundException, UnsupportedEncodingException {
		File f = new File(filename);
		if (!f.getParentFile().exists()) {
			f.getParentFile().mkdirs();
		}
		FileOutputStream fos = new FileOutputStream(f);
		OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8); 
		PrintWriter out = new PrintWriter(osw);
		
		Iterator<SootClassEdge> iter = edges.iterator();
		
		out.println("digraph G {");
		
		while(iter.hasNext()) {
			SootClassEdge e = iter.next();
			out.println(e.toDotStringWithArchElemType());
		}
		
		out.println("}");
		
		out.close();
	}
	
	public void addElementTypes(Map<String,String> map) {
		Iterator<SootClassEdge> iter = edges.iterator();
		
		System.out.println("Current Map: " + map);
		System.out.println("Printing class edges with arch element type...");
		while(iter.hasNext()) {
			
			SootClassEdge e = iter.next();
			
			String srcStr = e.getSrc().getName();
			String tgtStr = e.getTgt().getName();
			String srcType = map.get(srcStr);
			String tgtType = map.get(tgtStr);
			
			if (srcType.equals("p")) {
				e.srcType = ArchElemType.proc;
			}
			else if (srcType.equals("d")) {
				e.srcType = ArchElemType.data;
			}
			else if (srcType.equals("c")) {
				e.srcType = ArchElemType.conn;
			}
			
			if (tgtType.equals("p")) {
				e.tgtType = ArchElemType.proc;
			}
			else if (tgtType.equals("d")) {
				e.tgtType = ArchElemType.data;
			}
			else if (tgtType.equals("c")) {
				e.tgtType = ArchElemType.conn;
			}
			
			System.out.print("(" + srcStr + ":" + srcType +  ",");
			System.out.print(tgtStr + ":" + tgtType + ")");
		}
		System.out.println();
	}

	public void writeXMLClassGraph() throws ParserConfigurationException, TransformerException {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		  DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
	 
		  //classgraph elements
		  Document doc = docBuilder.newDocument();
		  Element rootElement = doc.createElement("ClassGraph");
		  doc.appendChild(rootElement);
	 
		  //classedge elements
		  for (SootClassEdge e : edges) {
			  Element ce = doc.createElement("ClassEdge");
			  rootElement.appendChild(ce);
			  Element src = doc.createElement("src");
			  src.appendChild(doc.createTextNode(e.src.toString()));
			  Element tgt = doc.createElement("tgt");
			  tgt.appendChild(doc.createTextNode(e.tgt.toString()));
			  ce.appendChild(src);
			  ce.appendChild(tgt);
		  }
	 
		  //write the content into xml file
		  TransformerFactory transformerFactory = TransformerFactory.newInstance();
		  Transformer transformer = transformerFactory.newTransformer();
		  DOMSource source = new DOMSource(doc);
		  StreamResult result =  new StreamResult(new File(Config.getXMLClassGraphFilename()));
		  transformer.transform(source, result);
	 
		  System.out.println("In " + Thread.currentThread().getStackTrace()[1].getClassName() 
				  + ". " + Thread.currentThread().getStackTrace()[1].getMethodName() 
				  + ", Wrote " + Config.getXMLClassGraphFilename());
	}

	public Set<SootClassEdge> getEdges() {
		return edges;
	}
}
