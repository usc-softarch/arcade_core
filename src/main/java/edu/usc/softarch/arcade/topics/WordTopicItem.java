package edu.usc.softarch.arcade.topics;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author joshua
 */
public class WordTopicItem {
	private static final Logger logger = LogManager.getLogger(WordTopicItem.class);
	public int id;
	public String name;
	public Map<Integer,Integer> topicIDWordCountMap;
	
	public double probabilityWordGivenTopic(Integer topicNum) {
		if (!topicIDWordCountMap.containsKey(topicNum)) {
			return 0;
		}
		int wordCountSum = 0;
		for (Integer wordCount : topicIDWordCountMap.values()) {
			wordCountSum += wordCount;
		}
		Integer wordCountTopic = topicIDWordCountMap.get(topicNum);
		logger.debug("wordCountTopic: " + wordCountTopic);
		logger.debug("wordCountSum: " + wordCountSum);
		return ((double)wordCountTopic/(double)wordCountSum);
	}
	
	public String toString() {
		return "[" + id + "," + name + "," + topicIDWordCountMap + "]";
	}
	
	public void add(Integer topicNum, Integer wordCount) {
		topicIDWordCountMap.put(topicNum, wordCount);
	}
	
	public boolean equals(Object o) {
		if(!(o instanceof WordTopicItem))
			return false;

		WordTopicItem wordTopicItem = (WordTopicItem)o;

		return wordTopicItem.name.equals(this.name) &&
			 wordTopicItem.topicIDWordCountMap.equals(this.topicIDWordCountMap);
	}
	
	public int hashCode() {
		int hash = 7;
		hash = 37 * hash + (this.name == null ? 0 : this.name.hashCode());
		hash = 37 * hash + (this.topicIDWordCountMap == null ? 0 : this.topicIDWordCountMap.hashCode());
		return hash;
	}
}