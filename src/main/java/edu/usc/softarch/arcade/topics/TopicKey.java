package edu.usc.softarch.arcade.topics;

import java.util.ArrayList;
import java.util.List;

/**
 * @author joshua
 *
 */
public class TopicKey {
	public int topicNum;
	double alpha;
	List<String> words;
	public String type;
	
	public List<String> getWords() {
		return new ArrayList<>(words);
	}


	public TopicKey() {
		super();
		words = new ArrayList<>();
	}
	
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
}