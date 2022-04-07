package edu.usc.softarch.arcade.topics;

import java.io.Serializable;

/**
 * @author joshua
 */
public class TopicItem implements Serializable {
	// #region FIELDS ------------------------------------------------------------
	private static final long serialVersionUID = 4599518018739063447L;
	private int topicNum;
	private int weight;
	private double proportion;
	// #endregion FIELDS ---------------------------------------------------------
	
	// #region CONTRUCTORS -------------------------------------------------------
	public TopicItem() { setWeight(1); }
	
	public TopicItem(TopicItem topicItem) {
		setTopicNum(topicItem.getTopicNum());
		setWeight(topicItem.getWeight());
		setProportion(topicItem.getProportion());
	}
	// #endregion CONSTRUCTORS ---------------------------------------------------

	// #region ACCESSORS ---------------------------------------------------------
	public int getTopicNum() { return this.topicNum; }
	public int getWeight() { return weight; }
	public double getProportion() { return this.proportion; }

	public void setTopicNum(int topicNum) { this.topicNum = topicNum; }
	public void setWeight(int weight) {	this.weight = weight; }
	public void setProportion(double proportion) { this.proportion = proportion; }
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
		return "[" + topicNum + "," + proportion + "]"; }
	// #endregion MISC -----------------------------------------------------------
}
