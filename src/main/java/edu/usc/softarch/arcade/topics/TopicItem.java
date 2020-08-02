package edu.usc.softarch.arcade.topics;

import java.io.Serializable;

/**
 * @author joshua
 */
public class TopicItem implements Serializable {
	// #region FIELDS ------------------------------------------------------------
	private static final long serialVersionUID = 4599518018739063447L;
	public int topicNum;
	public double proportion;
	public String type;
	// #endregion FIELDS ---------------------------------------------------------
	
	// #region CONTRUCTORS -------------------------------------------------------
	public TopicItem() { super(); }
	
	public TopicItem(TopicItem topicItem) {
		this.topicNum = topicItem.topicNum;
		this.proportion = topicItem.proportion;
	}
	// #endregion CONSTRUCTORS ---------------------------------------------------
	
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
}