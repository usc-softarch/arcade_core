package edu.usc.softarch.arcade.topics;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import cc.mallet.util.Maths;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * @author joshua
 */
public class DocTopicItem implements Serializable {
	// #region FIELDS ------------------------------------------------------------
	public enum Sort { num, prop }

	private static final long serialVersionUID = 5162975838519632395L;
	
	private int doc;
	private String source;
	private Map<Integer, TopicItem> topics;
	private Sort sortMethod;
	private transient Comparator<TopicItem> sorter;
	// #endregion FIELDS ---------------------------------------------------------
	
	// #region CONSTRUCTORS ------------------------------------------------------
	public DocTopicItem() {
		super();
		setSortMethod(Sort.prop);
		this.topics = new HashMap<>();
	}
	
	/**
	 * Clone contructor
	 */
	public DocTopicItem(DocTopicItem dti) {
		initialize(dti);
	}

	/**
	 * Merge constructor
	 */
	public DocTopicItem(DocTopicItem dti1, DocTopicItem dti2)
			throws UnmatchingDocTopicItemsException {
		// If either argument is null, then return the non-null argument
		if (dti1 == null) {
			initialize(dti2);
			return;
		}
		if (dti2 == null) {
			initialize(dti1);
			return;
		}

		// If arguments do not match, throw exception
		if (!dti1.hasSameTopics(dti2))
			throw new UnmatchingDocTopicItemsException(
				"In mergeDocTopicItems, nonmatching docTopicItems");

		initialize(dti1);
		Set<Integer> topicNumbers = dti1.getTopicNumbers();

		for (Integer i : topicNumbers) {
			TopicItem ti1 = dti1.getTopic(i);
			TopicItem ti2 = dti2.getTopic(i);
			TopicItem mergedTopicItem = this.getTopic(i);

			mergedTopicItem.setWeight(ti1.getWeight() + ti2.getWeight());

			mergedTopicItem.setProportion(
				(ti1.getProportion() * ti1.getWeight()
					+ ti2.getProportion() * ti2.getWeight())
					/ mergedTopicItem.getWeight());
		}
	}

	/**
	 * Initialize clone
	 */
	public void initialize(DocTopicItem dti) {
		this.doc = dti.doc;
		this.source = dti.source;
		this.topics = new HashMap<>();
		setSortMethod(Sort.prop); //TODO Clone from argument
		for (TopicItem topicItem : dti.getTopics())
			addTopic(new TopicItem(topicItem));
	}
	// #endregion CONSTRUCTORS ---------------------------------------------------

	// #region ACCESSORS ---------------------------------------------------------
	public int getDoc() { return this.doc; }
	public String getSource() { return this.source; }
	@JsonIgnore
	public List<TopicItem> getTopics() {
		return new ArrayList<>(topics.values()); }
	public Map<Integer, TopicItem> getTopicsForJackson() {
		return this.topics;
	}
	public Sort getSortMethod() { return this.sortMethod; }
	public int size() { return this.topics.size(); }
	public TopicItem getTopic(int topicNum) { return this.topics.get(topicNum); }
	public boolean hasTopic(int topicNum) { return topics.containsKey(topicNum); }
	@JsonIgnore
	public Set<Integer> getTopicNumbers() { return this.topics.keySet(); }

	public void setDoc(int doc) { this.doc = doc; }
	public void setSource(String source) { this.source = source; }
	public TopicItem addTopic(TopicItem topic) {
		return this.topics.put(topic.getTopicNum(), topic); }
	public TopicItem removeTopic(TopicItem topic) {
		return this.topics.remove(topic.getTopicNum()); }
	public TopicItem removeTopic(int topicNum) {
		return this.topics.remove(topicNum); }

	public void setSortMethod(Sort sortMethod) {
		this.sortMethod = sortMethod;
		if(sortMethod == Sort.num) {
			this.sorter = (TopicItem ti0, TopicItem ti1) -> {
				Integer int0 = ti0.getTopicNum();
				Integer int1 = ti1.getTopicNum();
				return int0.compareTo(int1); };
		} else if(sortMethod == Sort.prop) {
			this.sorter = (TopicItem ti0, TopicItem ti1) -> {
				Double double0 = Double.valueOf(ti0.getProportion());
				Double double1 = Double.valueOf(ti1.getProportion());
				return double0.compareTo(double1); };
		}
	}

	@JsonIgnore
	public boolean isCSourced() {
		return source.endsWith(".c") || source.endsWith(".h")
			|| source.endsWith(".tbl") || source.endsWith(".p")
			|| source.endsWith(".cpp") || source.endsWith(".s")
			|| source.endsWith(".hpp") || source.endsWith(".icc")
			|| source.endsWith(".ia");
	}

	public boolean hasSameTopics(DocTopicItem dti) {
		if (dti.size() != this.size())
			return false;

		for (Integer key : this.topics.keySet())
			if (!dti.hasTopic(key))
				return false;

		return true;
	}

	public double getJsDivergence(DocTopicItem toCompare)
			throws DistributionSizeMismatchException {
		double divergence = 0;

		// Error due to size mismatch
		if (this.size() != toCompare.size())
			throw new DistributionSizeMismatchException(
				"P and Q for Jensen Shannon Divergence not the same size");
		
		double[] sortedP = new double[this.size()];
		double[] sortedQ = new double[this.size()];

		Collection<TopicItem> currTopics = this.topics.values();
		Collection<TopicItem> toCompareTopics = toCompare.topics.values();

		for (TopicItem pTopicItem : currTopics)
			sortedP[pTopicItem.getTopicNum()] = pTopicItem.getProportion();
		
		for (TopicItem qTopicItem : toCompareTopics)
			sortedQ[qTopicItem.getTopicNum()] = qTopicItem.getProportion();

		divergence = Maths.jensenShannonDivergence(sortedP, sortedQ);
		
		return divergence;
	}
	// #endregion ACCESSORS ------------------------------------------------------

	// #region PROCESSING --------------------------------------------------------
	public List<TopicItem> sort(Sort sortMethod) {
		Sort oldMethod = this.sortMethod;
		setSortMethod(sortMethod);
		List<TopicItem> sortedTopics = sort();
		setSortMethod(oldMethod);
		return sortedTopics;
	}

	public List<TopicItem> sort() {
		List<TopicItem> topicsToSort = getTopics();
		// This ensures sortMethod is properly initialized when deserializing DTIs
		if (sorter == null) setSortMethod(this.sortMethod);
		Collections.sort(topicsToSort, sorter);
		return topicsToSort;
	}

	public List<Integer> intersection(DocTopicItem dti) {
		List<Integer> result = new ArrayList<>();

		for (Integer key : this.topics.keySet())
			if (dti.hasTopic(key)) result.add(key);

		return result;
	}
	// #endregion PROCESSING -----------------------------------------------------
	
	// #region MISC --------------------------------------------------------------
	public String toStringWithLeadingTabsAndLineBreaks(int numTabs) {
		List<TopicItem> values = sort();
		String dtItemStr = "[" + doc + "," + source + ",\n";
		
		for (TopicItem t : values) {
			dtItemStr += "\t".repeat(numTabs);
			dtItemStr += "[" + t.getTopicNum() + "," + t.getProportion() + "]\n";
		}
		
		dtItemStr += "]";
		return dtItemStr;
	}
	
	public String toString() {
		List<TopicItem> values = sort();
		String dtItemStr = "[" + doc + "," + source + ",";
	
		for (TopicItem t : values)
			dtItemStr += "[" + t.getTopicNum() + "," + t.getProportion() + "]";
		
		dtItemStr += "]";
		return dtItemStr;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o == this) return true;
		if (!(o instanceof DocTopicItem)) return false;

		DocTopicItem toCompare = (DocTopicItem) o;

		boolean condition1 = this.doc == toCompare.doc;
		boolean condition2 = this.source.equals(toCompare.source);
		boolean condition3 = this.topics.equals(toCompare.topics);

		return condition1 && condition2 && condition3;
	}

	// #endregion MISC -----------------------------------------------------------
}