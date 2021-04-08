package edu.usc.softarch.arcade.topics;

import java.io.Serializable;

/**
 * @author joshua
 */
public class TopicItem implements Serializable {
	// #region FIELDS ------------------------------------------------------------
	private static final long serialVersionUID = 4599518018739063447L;
	private int topicNum;
	private double proportion;
	private String type;
	// #endregion FIELDS ---------------------------------------------------------
	
	// #region CONTRUCTORS -------------------------------------------------------
	public TopicItem() { super(); }
	
	public TopicItem(TopicItem topicItem) {
		setTopicNum(topicItem.getTopicNum());
		setProportion(topicItem.getProportion());
	}
	// #endregion CONSTRUCTORS ---------------------------------------------------

	// #region ACCESSORS ---------------------------------------------------------
	public int getTopicNum() { return this.topicNum; }
	public double getProportion() { return this.proportion; }
	public String getType() { return this.type; }

	public void setTopicNum(int topicNum) { this.topicNum = topicNum; }
	public void setProportion(double proportion) { this.proportion = proportion; }
	public void setType(String type) { this.type = type; }
	// #endregion ACCESSORS ------------------------------------------------------
	
	// #region MISC --------------------------------------------------------------
	public boolean equals(Object o) {
		if(!(o instanceof TopicItem))
			return false;
		
		TopicItem topicItem = (TopicItem)o;
		return topicItem.topicNum == this.topicNum
			&& topicItem.proportion == this.proportion;
	}
	
	public int hashCode() {
		int hash = 7;
		hash = 37 * hash + this.topicNum;
		hash = 37 * hash + (int) this.proportion;
		return hash;
	}

	public String toString() {
		return "[" + topicNum + "," + proportion + "," + type + "]"; }
	// #endregion MISC -----------------------------------------------------------
}