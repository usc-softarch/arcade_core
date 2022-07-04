package edu.usc.softarch.arcade.functiongraph;

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
	private static Logger logger = LogManager.getLogger(StringEdge.class);

	private String srcStr = "";
	private String tgtStr = "";
	private String type = "";
	// #endregion FIELDS ---------------------------------------------------------

	// #region CONSTRUCTORS ------------------------------------------------------
	public StringEdge() {
		initialize("", "", "");
	}

	public StringEdge(String srcStr, String tgtStr) {
		initialize(srcStr, tgtStr, "");
	}

	public StringEdge(String srcStr, String tgtStr, String type) {
		initialize(srcStr, tgtStr, type);
	}

	/**
	 * Clone constructor.
	 */
	public StringEdge(StringEdge e) {
		initialize(e.getSrcStr(), e.getTgtStr(), e.getType());
	}

	private void initialize(String srcStr, String tgtStr, String type) {
		setSrcStr(srcStr);
		setTgtStr(tgtStr);
		setType(type);
	}
	// #endregion CONSTRUCTORS ---------------------------------------------------

	// #region ACCESSORS ---------------------------------------------------------
	public String getSrcStr() { return this.srcStr; }
	public String getTgtStr() { return this.tgtStr; }
	public String getType() { return this.type; }

	public void setSrcStr(String srcStr) { this.srcStr = srcStr; }
	public void setTgtStr(String tgtStr) { this.tgtStr = tgtStr; }
	public void setType(String type) { this.type = type; }
	// #endregion ACCESSORS ------------------------------------------------------

	// #region MISC --------------------------------------------------------------
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof StringEdge))
			return false;
		
		StringEdge e = (StringEdge) o;
		
		logger.trace("e.srcStr: " + e.srcStr);
		logger.trace("this.srcStr: " + this.srcStr);
		logger.trace("e.tgtStr: " + e.tgtStr);
		logger.trace("this.tgtStr: " + this.tgtStr);

		return (e.srcStr.equals(this.srcStr) &&
			e.tgtStr.equals(this.tgtStr) &&
			e.type.equals(this.type));
	}

	@Override
	public int hashCode() {
		int result = srcStr.hashCode();
		result = 37 * result + tgtStr.hashCode();
		result = 37 * result + type.hashCode();
		return result;
	}
	
	@Override
	public String toString() {
		if(this.type.isEmpty())
			return "(" + srcStr + "," + tgtStr + ")";
		return "(" + type + "," + srcStr + "," + tgtStr + ")";
	}
	
	public String toDotString() {
		String result = "\t\"" + srcStr + "\" -> \"" + tgtStr + "\"";
		result += type.equals("extends") ? "[arrowhead=\"empty\"];" : ";";
		return result;
	}

	public String toNumberedNodeDotString(Map<String,Integer> map) {
		return "\t\"" + map.get(srcStr) + "\" -> \"" + map.get(tgtStr) + "\";";
	}
	// #endregion MISC -----------------------------------------------------------
}