package edu.usc.softarch.arcade.topics;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

/**
 * @author joshua
 *
 */
public class DocTopicItem implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5162975838519632395L;
	public int doc;
	public String source;
	public ArrayList<TopicItem> topics;
	
	public enum Sort {
		num, prop
	}
	
	Sort sort = Sort.prop;
	
	public DocTopicItem() {
		super();
	}
	
	public DocTopicItem(DocTopicItem docTopicItem) {
		this.doc = docTopicItem.doc;
		this.source = new String(docTopicItem.source);
		this.topics = new ArrayList<TopicItem>();
		for (TopicItem topicItem : docTopicItem.topics) {
			this.topics.add(new TopicItem(topicItem));
		}
		
	}
	
	public String toStringWithLeadingTabsAndLineBreaks(int numTabs) {
		String dtItemStr = "[" + doc + "," + source + ",\n";
		
		if (sort.equals(Sort.num))
			Collections.sort(this.topics,new TopicItemByTopicNumComparator());
		else 
			Collections.sort(this.topics,new TopicItemByTopicPropComparator());
		
		for (TopicItem t : topics) {
			for (int i=0;i<numTabs;i++) {
				dtItemStr += "\t";
			}
			dtItemStr += "[" + t.topicNum + "," + t.proportion + "]\n";
		}
		
		dtItemStr += "]";
		
		return dtItemStr;
	}
	
	public String toString() {
		String dtItemStr = "[" + doc + "," + source + ",";
		
		if (sort.equals(Sort.num))
			Collections.sort(this.topics,new TopicItemByTopicNumComparator());
		else 
			Collections.sort(this.topics,new TopicItemByTopicPropComparator());
		
		for (TopicItem t : topics) {
			dtItemStr += "[" + t.topicNum + "," + t.proportion + "]";
		}
		
		dtItemStr += "]";
		
		return dtItemStr;
	}
	
}
