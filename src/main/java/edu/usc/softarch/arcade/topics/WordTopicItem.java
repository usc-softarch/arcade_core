package edu.usc.softarch.arcade.topics;

import java.util.HashMap;

/**
 * @author joshua
 *
 */
public class WordTopicItem {
	public int id;
	public String name;
	public HashMap<Integer,Integer> topicIDWordCountMap;
	
	public double probabilityWordGivenTopic(Integer topicNum) {
		boolean debug = false;
		if (!topicIDWordCountMap.containsKey(topicNum)) {
			return 0;
		}
		int wordCountSum = 0;
		for (Integer wordCount : topicIDWordCountMap.values()) {
			wordCountSum += wordCount;
		}
		Integer wordCountTopic = (Integer) topicIDWordCountMap.get(topicNum);
		if (debug) {
			System.out.println("wordCountTopic: " + wordCountTopic);
			System.out.println("wordCountSum: " + wordCountSum);
		}
		return (double)((double)wordCountTopic/(double)wordCountSum);
	}
	
	public String toString() {
		return "[" + id + "," + name + "," + topicIDWordCountMap + "]";
	}
	
	public void add(Integer topicNum, Integer wordCount) {
		topicIDWordCountMap.put(topicNum, wordCount);
	}
	
	public boolean equals(Object o) {
		WordTopicItem wordTopicItem = (WordTopicItem)o;
		if (wordTopicItem.name.equals(this.name) &&
			 wordTopicItem.topicIDWordCountMap.equals(this.topicIDWordCountMap)
			) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public int hashCode() {
		int hash = 7;
		hash = 37 * hash + (this.name == null ? 0 : this.name.hashCode());
		hash = 37 * hash + (this.topicIDWordCountMap == null ? 0 : this.topicIDWordCountMap.hashCode());
		return hash;
	}
}
