package edu.usc.softarch.arcade.classgraphs;

import soot.SootClass;

/**
 * @author joshua
 */
public class SootClassEdge extends StringEdge {
	// #region FIELDS ------------------------------------------------------------
	private static final long serialVersionUID = 1783211540062135043L;

	private transient SootClass src;
	private transient SootClass tgt;
	private String type;
	// #endregion FIELDS ---------------------------------------------------------

	// #region CONSTRUCTORS ------------------------------------------------------
	public SootClassEdge(String srcStr, String tgtStr) {
		super(srcStr, tgtStr);
		initialize(null, null, "depends");
	}
	
	public SootClassEdge(SootClassEdge edge) {
		super();
		initialize(edge.src, edge.tgt, edge.type);
	}
	
	public SootClassEdge(SootClass src, SootClass tgt, String type) {
		super();
		initialize(src, tgt, type);
	}

	private void initialize(SootClass src, SootClass tgt, String type) {
		setSrc(src);
		setTgt(tgt);
		setType(type);
	}
	// #endregion CONSTRUCTORS ---------------------------------------------------

	// #region ACCESSORS ---------------------------------------------------------
	public SootClass getSrc() { return src; }
	public SootClass getTgt() { return tgt; }

	public void setSrc(SootClass src) {
		this.src = src;
		if (src != null)
			setSrcStr(src.getName());
	}
	public void setTgt(SootClass tgt) {
		this.tgt = tgt;
		if (tgt != null)
			setSrcStr(tgt.getName());
	}
	// #endregion ACCESSORS ------------------------------------------------------
	
	// #region MISC --------------------------------------------------------------
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof SootClassEdge))
			return false;
		
		SootClassEdge e = (SootClassEdge) o;
		
		// If any parts are missing, return false
		if (e.src == null || e.tgt == null || this.src == null || this.tgt == null)
			return false;
		
		return e.src.getName().equals(this.src.getName()) &&
			e.tgt.getName().equals(this.tgt.getName()) && 
			e.getType().equals(this.getType());
	}

	@Override
	public int hashCode() {
		int result = src.getName().hashCode();
		result = 37 * result + tgt.getName().hashCode();
		result = 37 * result + type.hashCode();
		return result;
	}
	
	@Override
	public String toString() {
		return "(" + src + "," + tgt + "," + type + ")";
	}
	
	@Override
	public String toDotString() {
		return "\t\"" + src + "\" -> \"" + tgt + "\";";
	}
	
	public String toRsf() {
		return type + " " + src + " " + " " + tgt;
	}
	// #endregion MISC -----------------------------------------------------------
}