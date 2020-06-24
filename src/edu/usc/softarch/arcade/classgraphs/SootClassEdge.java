package edu.usc.softarch.arcade.classgraphs;

import java.io.Serializable;

import soot.SootClass;

/**
 * @author joshua
 *
 */
public class SootClassEdge extends StringEdge implements Serializable {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1783211540062135043L;
	public SootClass src = null;
	public SootClass tgt = null;
	public String type = "depends";
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public SootClassEdge(String srcStr, String tgtStr) {
		super(srcStr,tgtStr);
	}
	
	public SootClassEdge(SootClassEdge edge) {
		this.src = edge.src;
		this.tgt = edge.tgt;
		this.type = edge.type;
	}
	
	public SootClassEdge(SootClass src, SootClass tgt, String type) {
		this.src = src;
		this.tgt = tgt;
		this.type = type;
	}

	public SootClass getSrc() {
		return src;
	}

	public void setSrc(SootClass src) {
		this.src = src;
	}

	public SootClass getTgt() {
		return tgt;
	}

	public void setTgt(SootClass tgt) {
		this.tgt = tgt;
	}
	
	@Override
	public boolean equals(Object o) {
		SootClassEdge e = (SootClassEdge)o;
		
		
		
		if (e.src!=null && e.tgt!=null && this.src !=null && this.tgt != null ) {
			if (e.src.getName().equals(this.src.getName()) &&
				e.tgt.getName().equals(this.tgt.getName()) && 
				e.getType().equals(this.getType()) ) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * hashCode that combines two strings
	 * Source: http://www.javapractices.com/topic/TopicAction.do?Id=28
	 * @return a hash code value on the pair of strings.
	 */
	@Override
	public int hashCode() {
		int result = src.getName().hashCode();
		result = 37 * result + tgt.getName().hashCode();
		result = 37 * result + type.hashCode();
		// etc for all fields in the object
		// This will only work for 5 fields before you overflow and lose input
		// from the first field.
		// The optimal prime multiplier is near Math.exp( Math.log(
		// Integer.MAX_VALUE ) / numberOfFields) )
		// This technique samples output from all the component Objects but
		// mushes it together with information form the other fields.
		return result;
	}
	
	public String toString() {
		return "(" + src + "," + tgt + "," + type + ")";
	}
	
	public String toDotString() {
		return "\t\"" + src + "\" -> \"" + tgt + "\";";
	}
	
	public String toStringWithArchElemType() {
		return "(" + src + ":" + ArchElemType.typeToString(srcType) + "," + tgt
				+ ":" + ArchElemType.typeToString(srcType) + ")";
	}
	public String toDotStringWithArchElemType() {
		
		String srcDef = "\"" + src + "\" ";
		String tgtDef = "\"" + tgt + "\" ";
		
		srcDef += ArchElemType.typeToStyleString(srcType) + ";";
		tgtDef += ArchElemType.typeToStyleString(tgtType) + ";";
		
		String edgeStr = "\t\"" + src + "\" -> \"" + tgt + "\";";
		
		return srcDef + "\n" + tgtDef + "\n" + edgeStr;
	}
	
	public String toRsf() {
		return type + " " + src + " " + " " + tgt;
	}
	
}
