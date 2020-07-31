package edu.usc.softarch.arcade.functiongraph;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class TypedEdgeGraph implements Serializable {
	// #region FIELDS ------------------------------------------------------------
	private static final long serialVersionUID = 6318950604163450425L;
	public Set<StringTypedEdge> edges = new HashSet<>();
	// #endregion FIELDS ---------------------------------------------------------
	
	// #region CONSTRUCTORS ------------------------------------------------------
	public TypedEdgeGraph() { super(); }
	// #endregion CONSTRUCTORS ---------------------------------------------------

	// #region ACCESSORS ---------------------------------------------------------
	public Set<StringTypedEdge> getEdges() {
		return new HashSet<>(edges); }

	public void addEdge(String type, String src, String tgt) {
		edges.add(new StringTypedEdge(type, src,tgt)); }
	
	public void addEdge(StringTypedEdge e) { edges.add(e); }
	
	public boolean containsEdge(String type, String src, String tgt) {
		return edges.contains(new StringTypedEdge(type, src,tgt)); }
	
	public boolean containsEdge(StringTypedEdge e) {
		return edges.contains(e);	}
	
	public void removeEdge(StringTypedEdge e) {
		edges.remove(e); }
	
	public void removeEdge(String type, String src, String tgt) {
		edges.remove(new StringTypedEdge(type, src,tgt)); }
	// #endregion ACCESSORS ------------------------------------------------------
	
	public String toString() {
		Iterator<StringTypedEdge> iter = edges.iterator();
		String str = "";
		
		int edgeCount = 0;
		while(iter.hasNext()) {
			StringTypedEdge e = iter.next();
			str += edgeCount + ": " + e.toDotString();
			if(iter.hasNext()) {
				str+="\n";
			}
			edgeCount++;
		}
		
		return str;
	}
}
