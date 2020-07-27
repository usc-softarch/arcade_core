package edu.usc.softarch.arcade.topics;

import java.util.ArrayList;

/**
 * @author joshua
 *
 */
public class TopicKey {
	public int topicNum;
	double alpha;
	ArrayList<String> words;
	public String type;
	
	public ArrayList<String> getWords() {
		return new ArrayList<String>(words);
	}


	public TopicKey() {
		super();
		words = new ArrayList<String>();
	}
	
	public boolean equals(Object o) {
		TopicKey topicKey = (TopicKey) o;
		if (this.topicNum == topicKey.topicNum) {
			return true;
		}
		else
			return false;
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
	
}