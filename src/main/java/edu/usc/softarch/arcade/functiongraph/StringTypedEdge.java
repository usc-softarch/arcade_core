package edu.usc.softarch.arcade.functiongraph;

import java.io.Serializable;

//TODO Replace all occurrences of this with the superclass
public class StringTypedEdge extends StringEdge implements Serializable {
	private static final long serialVersionUID = 2367755143057728102L;

	public String arcTypeStr = "";
	
	public StringTypedEdge() { super(); }

	public StringTypedEdge(String arcTypeStr, String srcStr, String tgtStr) {
		super (srcStr, tgtStr);
		this.arcTypeStr = arcTypeStr;
	}
	
	public boolean equals(Object o) {
		if(!(o instanceof StringTypedEdge))
			return false;
		
		StringTypedEdge e = (StringTypedEdge) o;

		return (e.getSrcStr().equals(this.getSrcStr()) &&
			e.getTgtStr().equals(this.getTgtStr()) &&
			e.arcTypeStr.equals(this.arcTypeStr));
	}
	
	/**
	 * hashCode that combines two strings
	 * Source: http://www.javapractices.com/topic/TopicAction.do?Id=28
	 * @return a hash code value on the pair of strings.
	 */
	@Override
	public int hashCode() {
		int result = getSrcStr().hashCode();
		result = 37 * result + getTgtStr().hashCode();
		result = 37 * result + arcTypeStr.hashCode();
		return result;
	}
	
	@Override
	public String toString() {
		return "(" + arcTypeStr + "," + getSrcStr() + "," + getTgtStr() + ")";
	}
	
	@Override
	public String toDotString() {
		return "\t\"" + getSrcStr() + "\" -> \"" + getTgtStr() + "\"[ label=\""
			+ arcTypeStr + "\" ];;";
	}
}
