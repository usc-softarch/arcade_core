package edu.usc.softarch.arcade.functiongraph;

import java.io.Serializable;

import org.apache.log4j.Logger;

import edu.usc.softarch.arcade.classgraphs.StringEdge;

//TODO Replace all occurrences of this with the superclass
public class StringTypedEdge extends StringEdge implements Serializable {
	private static final long serialVersionUID = 2367755143057728102L;
	private static Logger logger = Logger.getLogger(StringTypedEdge.class);

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
		
		logger.debug("e.arcTypeStr: " + e.arcTypeStr);
		logger.debug("this.arcTypeStr: " + this.arcTypeStr);
		
		logger.debug("e.srcStr: " + e.getSrcStr());
		logger.debug("this.srcStr: " + this.getSrcStr());
		
		logger.debug("e.tgtStr: " + e.getTgtStr());
		logger.debug("this.tgtStr: " + this.getTgtStr());

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
