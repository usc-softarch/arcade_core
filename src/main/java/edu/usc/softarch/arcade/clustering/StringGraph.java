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
import java.util.Set;

import edu.usc.softarch.arcade.classgraphs.StringEdge;

/**
 * @author joshua
 */
public class StringGraph implements Serializable {
	// #region FIELDS ------------------------------------------------------------
	private static final long serialVersionUID = -4879127696671797183L;
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
	public Set<StringEdge> getEdges() { return new HashSet<>(edges); }

	public Set<String> getNodesInClusterGraph() {
		Set<String> nodes = new HashSet<>();
		for (StringEdge edge : getEdges()) {
			nodes.add(edge.getSrcStr().trim());
			nodes.add(edge.getTgtStr().trim());
		}
		return nodes;
	}
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
	// #endregion IO -------------------------------------------------------------
}