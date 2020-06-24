package edu.usc.softarch.arcade.functiongraph;

import java.io.Serializable;
import java.util.HashMap;

import org.apache.log4j.Logger;

import edu.usc.softarch.arcade.classgraphs.StringEdge;



public class StringTypedEdge extends StringEdge implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2367755143057728102L;
	static Logger logger = Logger.getLogger(StringTypedEdge.class);

	public String arcTypeStr = "";
	public String srcStr = "";
	public String tgtStr = "";
	
	public StringTypedEdge() {
		super();
	}

	public StringTypedEdge(String arcTypeStr, String srcStr, String tgtStr) {
		super(srcStr,tgtStr);
		this.arcTypeStr = arcTypeStr;
		this.srcStr = srcStr;
		this.tgtStr = tgtStr;
	}
	
	public boolean equals(Object o) {
		boolean localDebug = false;
		
		StringTypedEdge e = (StringTypedEdge) o;
		
		if (localDebug) {
			logger.debug("e.arcTypeStr: " + e.arcTypeStr);
			logger.debug("this.arcTypeStr: " + this.arcTypeStr);
			
			logger.debug("e.srcStr: " + e.srcStr);
			logger.debug("this.srcStr: " + this.srcStr);
			
			logger.debug("e.tgtStr: " + e.tgtStr);
			logger.debug("this.tgtStr: " + this.tgtStr);
		}
		if (
			e.srcStr.equals(this.srcStr) &&
			e.tgtStr.equals(this.tgtStr) &&
			e.arcTypeStr.equals(this.arcTypeStr)
		   ) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * hashCode that combines two strings
	 * Source: http://www.javapractices.com/topic/TopicAction.do?Id=28
	 * @return a hash code value on the pair of strings.
	 */
	@Override
	public int hashCode() {
		int result = srcStr.hashCode();
		result = 37 * result + tgtStr.hashCode();
		result = 37 * result + arcTypeStr.hashCode();
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
		return "(" + arcTypeStr + "," + srcStr + "," + tgtStr + ")";
	}
	
	public String toDotString() {
		return "\t\"" + srcStr + "\" -> \"" + tgtStr + "\"[ label=\"" + arcTypeStr + "\" ];;";
	}

}
