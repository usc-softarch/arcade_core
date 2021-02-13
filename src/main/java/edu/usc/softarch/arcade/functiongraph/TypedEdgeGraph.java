package edu.usc.softarch.arcade.functiongraph;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import edu.usc.softarch.arcade.classgraphs.StringEdge;

public class TypedEdgeGraph implements Serializable {
	// #region FIELDS ------------------------------------------------------------
	private static final long serialVersionUID = 6318950604163450425L;
	private Set<StringEdge> edges = new HashSet<>();
	// #endregion FIELDS ---------------------------------------------------------

	// #region ACCESSORS ---------------------------------------------------------
	public Set<StringEdge> getEdges() { return new HashSet<>(edges); }

	public void addEdge(String type, String src, String tgt) {
		edges.add(new StringEdge(type, src, tgt)); }
	
	public void addEdge(StringEdge e) { edges.add(e); }
	
	public boolean containsEdge(String type, String src, String tgt) {
		return edges.contains(new StringEdge(type, src, tgt)); }
	
	public boolean containsEdge(StringEdge e) { return edges.contains(e); }
	
	public void removeEdge(StringEdge e) { edges.remove(e); }
	
	public void removeEdge(String type, String src, String tgt) {
		edges.remove(new StringEdge(type, src, tgt)); }
	// #endregion ACCESSORS ------------------------------------------------------
}