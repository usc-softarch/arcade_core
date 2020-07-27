package edu.usc.softarch.arcade.topics;

import java.util.Comparator;

/**
 * @author joshua
 *
 */
public class TopicItemByTopicPropComparator implements Comparator<TopicItem> {

	public int compare(TopicItem ti0, TopicItem ti1) {
		Double double0 = new Double(ti0.proportion);
		Double double1 = new Double(ti1.proportion);
		
		return double0.compareTo(double1);
	}

}
