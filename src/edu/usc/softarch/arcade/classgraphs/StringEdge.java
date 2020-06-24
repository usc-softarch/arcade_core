package edu.usc.softarch.arcade.classgraphs;

import java.io.Serializable;
import java.util.HashMap;

/**
 * @author joshua
 *
 */
public class StringEdge implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4787247799445316839L;
	public ArchElemType srcType = null;
	public ArchElemType tgtType = null;
	public String srcStr = "";
	public String tgtStr = "";
	public String type = "";

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public StringEdge() {
		super();
	}

	public StringEdge(String srcStr, String tgtStr) {
		this.srcStr = srcStr;
		this.tgtStr = tgtStr;
	}
	
	public boolean equals(Object o) {
		boolean localDebug = false;
		
		StringEdge e = (StringEdge) o;
		
		if (localDebug) {
			System.out.println("e.srcStr: " + e.srcStr);
			System.out.println("this.srcStr: " + this.srcStr);
			System.out.println("e.tgtStr: " + e.tgtStr);
			System.out.println("this.tgtStr: " + this.tgtStr);
		}
		if (e.srcStr.equals(this.srcStr) &&
				    e.tgtStr.equals(this.tgtStr)) {
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
		return "(" + srcStr + "," + tgtStr + ")";
	}
	
	public String toDotString() {
		if (type.equals("extends")) {
			return "\t\"" + srcStr + "\" -> \"" + tgtStr + "\"[arrowhead=\"empty\"];";
		}
		
			return "\t\"" + srcStr + "\" -> \"" + tgtStr + "\";";
	}

	public String toNumberedNodeDotString(HashMap<String,Integer> map) {
		return "\t\"" + map.get(srcStr) + "\" -> \"" + map.get(tgtStr) + "\";";
	}

}