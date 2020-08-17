package edu.usc.softarch.arcade.callgraph;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author joshua
 */
public class MyCallGraph implements Serializable {
	// #region FIELDS ------------------------------------------------------------
	private static final long serialVersionUID = 7989577593008055517L;

	private Set<MethodEdge> edges;
	// #endregion FIELDS ---------------------------------------------------------

	// #region CONSTRUCTORS ------------------------------------------------------
	public MyCallGraph() { this.edges = new HashSet<>(); }
	// #endregion CONSTRUCTORS ---------------------------------------------------
	
	// #region ACCESSORS ---------------------------------------------------------
	public Set<MethodEdge> getEdges() { return new HashSet<>(edges); }
	/**
	 * Returns all Methods that are target of the given src MyMethod.
	 * 
	 * @param src Source MyMethod to get targets from.
	 * @return All target MyMethods of src.
	 */
	public Set<MyMethod> getTargetEdges(MyMethod src) {
		Set<MyMethod> targetMethods = new HashSet<>();

		for (MethodEdge me : edges)
			if (me.getSrc().equals(src))
				targetMethods.add(me.getTgt());

		return targetMethods;
	}
	public boolean containsEdge(MyMethod src, MyMethod tgt) {
		return edges.contains(new MethodEdge(src,tgt)); }
	public boolean containsEdge(MethodEdge e) { return edges.contains(e); }

	public void addEdge(MyMethod src, MyMethod tgt) {
		edges.add(new MethodEdge(src,tgt)); }
	public void addEdge(MethodEdge e) { edges.add(e); }
	public void removeEdge(MethodEdge e) { edges.remove(e); }
	public void removeEdge(MyMethod src, MyMethod tgt) {
		edges.remove(new MethodEdge(src,tgt)); }
	// #endregion ACCESSORS ------------------------------------------------------
	
	// #region MISC --------------------------------------------------------------
	@Override
	public String toString() {
		String str = "";
		int edgeCount = 0;

		for (MethodEdge e : edges) {
			str += edgeCount + ": " + e.toString() + "\n";
			edgeCount++;
		}
		
		// remove last line break before return
		return str.substring(0, str.length() - 1);
	}
	// #endregion MISC -----------------------------------------------------------
	
	// #region IO ----------------------------------------------------------------
	public void serialize(String filename) throws IOException {
		try (ObjectOutputStream obj_out =
				new ObjectOutputStream(
				new FileOutputStream(filename))) {
			obj_out.writeObject(this);
		}
	}
	// #endregion IO -------------------------------------------------------------
}