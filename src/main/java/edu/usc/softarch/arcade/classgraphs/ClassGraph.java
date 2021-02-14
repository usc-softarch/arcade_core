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
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import soot.SootClass;

/**
 * @author joshua
 */
public class ClassGraph implements Serializable {
	// #region FIELDS ------------------------------------------------------------
	private static final long serialVersionUID = -4718039019698373111L;
	private static final Logger logger = LogManager.getLogger(ClassGraph.class);

	private Set<SootClassEdge> edges;
	// #endregion FIELDS ---------------------------------------------------------
	
	// #region CONSTRUCTORS ------------------------------------------------------
	public ClassGraph() {
		super();
		this.edges = new HashSet<>();
	}
	// #endregion CONSTRUCTORS ---------------------------------------------------

	// #region ACCESSORS ---------------------------------------------------------
	public Set<SootClassEdge> getEdges() { return edges; }

	/**
	 * @return All SootClasses in this ClassGraph.
	 */
	public List<SootClass> getNodes() {
		Set<SootClass> cgNodes = new HashSet<>();

		for (SootClassEdge e : edges) {
			cgNodes.add(e.getTgt());
			cgNodes.add(e.getSrc());
		}

		return new ArrayList<>(cgNodes);
	}

	/**
	 * Gets all SootClasses that serve as the source node for edges where the
	 * target is the provided SootClass argument.
	 * 
	 * @param c Target SootClass.
	 * @return All sources of c.
	 */
	public List<SootClass> getCallerClasses(SootClass c) {
		List<SootClass> callerClasses = new ArrayList<>();

		for (SootClassEdge e : edges)
			//TODO make this a proper object comparison rather than String
			if (e.getTgt().toString().equals(c.toString()))
				callerClasses.add(e.getSrc());
		
		return callerClasses;
	}
	
	/**
	 * Gets all SootClasses that serve as the target node for edges where the
	 * source is the provided SootClass argument.
	 * 
	 * @param c Source SootClass.
	 * @return All targets of c.
	 */
	public List<SootClass> getCalleeClasses(SootClass c) {
		List<SootClass> calleeClasses = new ArrayList<>();

		for (SootClassEdge e : edges)
			//TODO make this a proper object comparison rather than String
			if (e.getSrc().toString().equals(c.toString()))
				calleeClasses.add(e.getTgt());
		
		return calleeClasses;
	}

	public boolean containsEdge(SootClass src, SootClass tgt, String type) {
		return edges.contains(new SootClassEdge(src,tgt,type)); }
	public boolean containsEdge(SootClassEdge e) { return edges.contains(e); }
	public int size() { return edges.size(); }
	
	public void addEdge(SootClass src, SootClass tgt, String type) {
		edges.add(new SootClassEdge(src,tgt,type)); }
	public void addEdge(SootClassEdge e) { edges.add(e); }
	public void removeEdge(SootClass src, SootClass tgt,String type) {
		edges.remove(new SootClassEdge(src,tgt,type)); }
	public void removeEdge(SootClassEdge e) { edges.remove(e); }

	public void addElementTypes(Map<String,String> map) {
		logger.debug("Current Map: " + map);
		logger.debug("Printing class edges with arch element type...");

		for (SootClassEdge e : edges) {
			String srcStr = e.getSrcStr();
			String tgtStr = e.getTgtStr();
			String srcType = map.get(srcStr);
			String tgtType = map.get(tgtStr);
			
			//TODO do this with logger instead
			System.out.print("(" + srcStr + ":" + srcType +  ",");
			System.out.print(tgtStr + ":" + tgtType + ")");
		}

		System.out.println();
	}
	// #endregion ACCESSORS ------------------------------------------------------
	
	// #region MISC --------------------------------------------------------------
	public String toString() {
		String str = "";
		
		for (SootClassEdge e : edges)
			str += e.toString() + ",";
		
		// Remove the last comma before returning
		return str.substring(0, str.length() - 1);
	}
	// #endregion MISC -----------------------------------------------------------

	// #region IO ----------------------------------------------------------------
	public void generateRsf(String rsfFilePath) {
		try (Writer out = new BufferedWriter(new FileWriter(rsfFilePath))) {
			for (SootClassEdge edge : edges)
				out.write(edge.toRsf() + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void serialize(String filename) throws IOException {
		try (ObjectOutputStream obj_out =
				new ObjectOutputStream(
				new FileOutputStream(filename))) {
			obj_out.writeObject(this);
		}
	}

	public void writeDotFile(String filename) throws FileNotFoundException {
		File f = new File(filename);
		if (f.getParentFile() != null && !f.getParentFile().exists())
			f.getParentFile().mkdirs();

		FileOutputStream fos = new FileOutputStream(f);
		OutputStreamWriter osw =
			new OutputStreamWriter(fos, StandardCharsets.UTF_8);

		try(PrintWriter out = new PrintWriter(osw)) {
			out.println("digraph G {");
			
			for(SootClassEdge e : edges)
				out.println(e.toDotString());
			
			out.println("}");
		}
	}
	
	public void writeXMLClassGraph(String xmlFilePath) 
			throws ParserConfigurationException, TransformerException {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		docFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		docFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
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
			src.appendChild(doc.createTextNode(e.getSrcStr()));
			Element tgt = doc.createElement("tgt");
			tgt.appendChild(doc.createTextNode(e.getTgtStr()));
			ce.appendChild(src);
			ce.appendChild(tgt);
		}
	
		//write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET,"");
		Transformer transformer = transformerFactory.newTransformer();

		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new File(xmlFilePath));
		transformer.transform(source, result);
	
		System.out.println("In "
			+ Thread.currentThread().getStackTrace()[1].getClassName() 
			+ ". " + Thread.currentThread().getStackTrace()[1].getMethodName() 
			+ ", Wrote " + xmlFilePath);
	}
	// #endregion IO -------------------------------------------------------------
}