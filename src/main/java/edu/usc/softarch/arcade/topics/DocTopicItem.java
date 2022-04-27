package edu.usc.softarch.arcade.topics;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;

import cc.mallet.util.Maths;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DocTopicItem implements Serializable {
	//region FIELDS
	private static final long serialVersionUID = 5162975838519632395L;
	
	public final int doc;
	public final String source;
	private Map<Integer, TopicItem> topics;
	//endregion FIELDS
	
	//region CONSTRUCTORS
	@JsonCreator
	public DocTopicItem(@JsonProperty("doc") int doc,
											@JsonProperty("source") String source) {
		this.doc = doc;
		this.source = source;
		this.topics = new HashMap<>();
	}
	
	/**
	 * Clone contructor
	 */
	public DocTopicItem(DocTopicItem dti) {
		this.doc = dti.doc;
		this.source = dti.source;
		initialize(dti);
	}

	/**
	 * Merge constructor
	 */
	public DocTopicItem(DocTopicItem dti1, DocTopicItem dti2)
			throws UnmatchingDocTopicItemsException {
		// If either argument is null, then return the non-null argument
		if (dti1 == null) {
			this.doc = dti2.doc;
			this.source = dti2.source;
			initialize(dti2);
			return;
		}
		if (dti2 == null) {
			this.doc = dti1.doc;
			this.source = dti1.source;
			initialize(dti1);
			return;
		}

		// If arguments do not match, throw exception
		if (!dti1.hasSameTopics(dti2))
			throw new UnmatchingDocTopicItemsException(
				"In mergeDocTopicItems, nonmatching docTopicItems");

		this.doc = dti1.doc;
		this.source = dti1.source;
		this.topics = new HashMap<>();
		Set<Integer> topicNumbers = dti1.getTopicNumbers();

		for (Integer i : topicNumbers) {
			TopicItem ti1 = dti1.getTopic(i);
			TopicItem ti2 = dti2.getTopic(i);
			this.addTopic(new TopicItem(ti1, ti2));
		}
	}

	/**
	 * Initialize clone
	 */
	public void initialize(DocTopicItem dti) {
		this.topics = new HashMap<>();
		for (TopicItem topicItem : dti.getTopics())
			addTopic(new TopicItem(topicItem));
	}
	//endregion

	//region ACCESSORS
	@JsonIgnore
	public List<TopicItem> getTopics() {
		return new ArrayList<>(topics.values()); }
	public Map<Integer, TopicItem> getTopicsForJackson() {
		return this.topics;
	}
	public int size() { return this.topics.size(); }
	public TopicItem getTopic(int topicNum) { return this.topics.get(topicNum); }
	public boolean hasTopic(int topicNum) { return topics.containsKey(topicNum); }
	@JsonIgnore
	public Set<Integer> getTopicNumbers() { return this.topics.keySet(); }

	public TopicItem addTopic(TopicItem topic) {
		return this.topics.put(topic.topicNum, topic); }

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
		// Error due to size mismatch
		if (this.size() != toCompare.size())
			throw new DistributionSizeMismatchException(
				"P and Q for Jensen Shannon Divergence not the same size");
		
		double[] sortedP = new double[this.size()];
		double[] sortedQ = new double[this.size()];

		Collection<TopicItem> currTopics = this.topics.values();
		Collection<TopicItem> toCompareTopics = toCompare.topics.values();

		for (TopicItem pTopicItem : currTopics)
			sortedP[pTopicItem.topicNum] = pTopicItem.proportion;
		
		for (TopicItem qTopicItem : toCompareTopics)
			sortedQ[qTopicItem.topicNum] = qTopicItem.proportion;
		
		return Maths.jensenShannonDivergence(sortedP, sortedQ);
	}
	//endregion

	//region OBJECT METHODS
	public String toString() {
		List<TopicItem> values =
			getTopics().stream().sorted().collect(Collectors.toList());
		StringBuilder dtItemStr = new StringBuilder("[" + doc + "," + source + ",");
	
		for (TopicItem t : values)
			dtItemStr.append(t);
		
		dtItemStr.append("]");
		return dtItemStr.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof DocTopicItem)) return false;

		DocTopicItem that = (DocTopicItem) o;

		return this.doc == that.doc
			&& Objects.equals(this.source, that.source)
			&& Objects.equals(getTopics(), that.getTopics());
	}

	@Override
	public int hashCode() {
		return Objects.hash(doc, source, topics);	}
	//endregion
}
