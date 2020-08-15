package edu.usc.softarch.arcade.topics;

import java.util.ArrayList;
import java.util.List;

/**
 * @author joshua
 */
public class TopicKey {
	// #region FIELDS ------------------------------------------------------------
	private int topicNum;
	private double alpha;
	private List<String> words;
	private String type;
	// #endregion FIELDS ---------------------------------------------------------

	// #region CONSTRUCTORS ------------------------------------------------------
	public TopicKey() { words = new ArrayList<>(); }
	// #endregion CONSTRUCTORS ---------------------------------------------------

	// #region ACCESSORS ---------------------------------------------------------
	public int getTopicNum() { return this.topicNum; }
	public double getAlpha() { return this.alpha; }
	public List<String> getWords() { return new ArrayList<>(this.words); }
	public String getType() { return this.type; }

	public void setTopicNum(int topicNum) { this.topicNum = topicNum; }
	public void setAlpha(double alpha) { this.alpha = alpha; }
	public boolean addWord(String word) { return this.words.add(word); }
	public boolean removeWord(String word) { return this.words.remove(word); }
	public void setType(String type) { this.type = type; }
	// #endregion ACCESSORS ------------------------------------------------------
	
	// #region MISC --------------------------------------------------------------
	public boolean equals(Object o) {
		if(!(o instanceof TopicKey))
			return false;

		TopicKey topicKey = (TopicKey) o;

		return this.topicNum == topicKey.topicNum;
	}
	
	public int hashCode() {
		int hash = 7;
		hash = 37 * hash + this.topicNum;
		return hash;
	}
	
	public String toString() {
		String tkStr = "[" + topicNum + "," + alpha + "," + type + "[";
		for (String word : words) {
			tkStr += word + ",";
		}
		tkStr += "]]";
		return tkStr;
	}
	// #endregion MISC -----------------------------------------------------------
}