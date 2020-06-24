package edu.usc.softarch.arcade.functiongraph;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.log4j.Logger;

import edu.usc.softarch.arcade.functiongraph.StringTypedEdge;

public class TypedEdgeGraph implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6318950604163450425L;
	public HashSet<StringTypedEdge> edges = new HashSet<StringTypedEdge>();
	private Logger logger = Logger.getLogger(TypedEdgeGraph.class);
	
	public HashSet<StringTypedEdge> getEdges() {
		return new HashSet<StringTypedEdge>(edges);
	}
	
	public TypedEdgeGraph() {
		
	}
	
	public void addEdge(String type, String src, String tgt) {
		edges.add(new StringTypedEdge(type, src,tgt));
	}
	
	public void addEdge(StringTypedEdge e) {
		edges.add(e);
	}
	
	public boolean containsEdge(String type, String src, String tgt) {
		return edges.contains(new StringTypedEdge(type, src,tgt));
	}
	
	public boolean containsEdge(StringTypedEdge e) {
		return edges.contains(e);
	}
	
	public void removeEdge(StringTypedEdge e) {
		edges.remove(e);
	}
	
	public void removeEdge(String type, String src, String tgt) {
		edges.remove(new StringTypedEdge(type, src,tgt));
	}
	
	public String toString() {
		Iterator<StringTypedEdge> iter = edges.iterator();
		String str = "";
		
		int edgeCount = 0;
		while(iter.hasNext()) {
			StringTypedEdge e = (StringTypedEdge) iter.next();
			str += edgeCount + ": " + e.toDotString();
			if(iter.hasNext()) {
				str+="\n";
			}
			edgeCount++;
		}
		
		return str;
	}

}
