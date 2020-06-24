package edu.usc.softarch.arcade.callgraph;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;

import edu.usc.softarch.arcade.callgraph.MethodEdge;
import edu.usc.softarch.arcade.classgraphs.StringEdge;


/**
 * @author joshua
 *
 */
public class MyCallGraph implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7989577593008055517L;
	HashSet<MethodEdge> edges = new HashSet<MethodEdge>();
	
	public HashSet<MethodEdge> getEdges() {
		HashSet<MethodEdge> copyEdges = new HashSet<MethodEdge>(edges);
		return copyEdges;
	}
	
	public void addEdge(MyMethod src, MyMethod tgt) {
		edges.add(new MethodEdge(src,tgt));
	}
	
	public void addEdge(MethodEdge e) {
		edges.add(e);
	}
	
	public boolean containsEdge(MyMethod src, MyMethod tgt) {
		return edges.contains(new MethodEdge(src,tgt));
	}
	
	public boolean containsEdge(MethodEdge e) {
		return edges.contains(e);
	}
	
	public void removeEdge(MethodEdge e) {
		edges.remove(e);
	}
	
	public void removeEdge(MyMethod src, MyMethod tgt) {
		edges.remove(new MethodEdge(src,tgt));
	}
	
	public String toString() {
		Iterator<MethodEdge> iter = edges.iterator();
		String str = "";
		
		int edgeCount = 0;
		while(iter.hasNext()) {
			MethodEdge e = (MethodEdge) iter.next();
			str += edgeCount + ": " + e.toString();
			if(iter.hasNext()) {
				str+="\n";
			}
			edgeCount++;
		}
		
		return str;
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

	public HashSet<MyMethod> getTargetEdges(MyMethod src) {
		HashSet<MyMethod> targetMethods = new HashSet<MyMethod>();
		for (MethodEdge me : edges) {
			if ( me.src.equals(src) ) {
				targetMethods.add(me.tgt);
			}
		}
		return targetMethods;
	}
	
}
