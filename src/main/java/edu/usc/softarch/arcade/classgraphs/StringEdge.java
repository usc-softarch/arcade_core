package edu.usc.softarch.arcade.classgraphs;

import java.io.Serializable;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author joshua
 */
public class StringEdge implements Serializable {
	// #region FIELDS ------------------------------------------------------------
	private static final long serialVersionUID = -4787247799445316839L;
	public ArchElemType srcType = null;
	public ArchElemType tgtType = null;
	public String srcStr = "";
	public String tgtStr = "";
	public String type = "";
	private static Logger logger = LogManager.getLogger(StringEdge.class);
	// #endregion FIELDS ---------------------------------------------------------

	// #region CONSTRUCTORS ------------------------------------------------------
	public StringEdge() { super(); }

	public StringEdge(String srcStr, String tgtStr) {
		this.srcStr = srcStr;
		this.tgtStr = tgtStr;
	}
	// #endregion CONSTRUCTORS ---------------------------------------------------

	// #region ACCESSORS ---------------------------------------------------------
	public String getType() { return type; }

	public void setType(String type) { this.type = type; }
	// #endregion ACCESSORS ------------------------------------------------------

	// #region PROCESSING --------------------------------------------------------
	public boolean equals(Object o) {
		if(!(o instanceof StringEdge))
			return false;
		
		StringEdge e = (StringEdge) o;
		
		logger.info("e.srcStr: " + e.srcStr);
		logger.info("this.srcStr: " + this.srcStr);
		logger.info("e.tgtStr: " + e.tgtStr);
		logger.info("this.tgtStr: " + this.tgtStr);

		return (e.srcStr.equals(this.srcStr) && e.tgtStr.equals(this.tgtStr));
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
		return result;
	}
	
	@Override
	public String toString() {
		return "(" + srcStr + "," + tgtStr + ")";
	}
	
	public String toDotString() {
		String result = "\t\"" + srcStr + "\" -> \"" + tgtStr + "\"";
		result += type.equals("extends") ? "[arrowhead=\"empty\"];" : ";";
		return result;
	}

	public String toNumberedNodeDotString(Map<String,Integer> map) {
		return "\t\"" + map.get(srcStr) + "\" -> \"" + map.get(tgtStr) + "\";";
	}
	// #endregion PROCESSING -----------------------------------------------------
}