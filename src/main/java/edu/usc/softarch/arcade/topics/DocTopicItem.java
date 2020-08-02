package edu.usc.softarch.arcade.topics;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * @author joshua
 */
public class DocTopicItem implements Serializable {
	// #region FIELDS ------------------------------------------------------------
	public enum Sort { num, prop }

	private static final long serialVersionUID = 5162975838519632395L;
	private Sort sortMethod;
	private transient Comparator<TopicItem> sorter;
	public int doc;
	public String source;
	public List<TopicItem> topics;
	// #endregion FIELDS ---------------------------------------------------------
	
	// #region CONSTRUCTORS ------------------------------------------------------
	public DocTopicItem() {
		super();
		setSortMethod(Sort.prop);
	}
	
	/**
	 * Clone contructor
	 */
	public DocTopicItem(DocTopicItem docTopicItem) {
		this.doc = docTopicItem.doc;
		this.source = docTopicItem.source;
		this.topics = new ArrayList<>();
		setSortMethod(Sort.prop); //TODO Clone from argument
		for (TopicItem topicItem : docTopicItem.topics)
			this.topics.add(new TopicItem(topicItem));
	}
	// #endregion CONSTRUCTORS ---------------------------------------------------

	// #region ACCESSORS ---------------------------------------------------------
	public Sort getSortMethod() { return this.sortMethod; }

	public void setSortMethod(Sort sortMethod) {
		this.sortMethod = sortMethod;
		if(sortMethod == Sort.num) {
			this.sorter = (TopicItem ti0, TopicItem ti1) -> {
				Integer int0 = ti0.topicNum;
				Integer int1 = ti1.topicNum;
				return int0.compareTo(int1); };
		} else if(sortMethod == Sort.prop) {
			this.sorter = (TopicItem ti0, TopicItem ti1) -> {
				Double double0 = Double.valueOf(ti0.proportion);
				Double double1 = Double.valueOf(ti1.proportion);
				return double0.compareTo(double1); };
		}
	}
	// #endregion ACCESSORS ------------------------------------------------------

	public List<TopicItem> sort(Sort sortMethod) {
		Sort oldMethod = this.sortMethod;
		setSortMethod(sortMethod);
		sort();
		setSortMethod(oldMethod);
		return this.topics;
	}

	public List<TopicItem> sort() {
		Collections.sort(this.topics, sorter);
		return this.topics;
	}
	
	public String toStringWithLeadingTabsAndLineBreaks(int numTabs) {
		sort();
		String dtItemStr = "[" + doc + "," + source + ",\n";
		
		for (TopicItem t : topics) {
			dtItemStr += "\t".repeat(numTabs);
			dtItemStr += "[" + t.topicNum + "," + t.proportion + "]\n";
		}
		
		dtItemStr += "]";
		return dtItemStr;
	}
	
	public String toString() {
		sort();
		String dtItemStr = "[" + doc + "," + source + ",";
	
		for (TopicItem t : topics)
			dtItemStr += "[" + t.topicNum + "," + t.proportion + "]";
		
		dtItemStr += "]";
		return dtItemStr;
	}
}