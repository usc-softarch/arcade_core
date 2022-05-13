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
import edu.usc.softarch.arcade.clustering.Cluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * The set of {@link TopicItem}s in a document. A document represents a cluster.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DocTopicItem implements Serializable {
	//region ATTRIBUTES
	private static final long serialVersionUID = 5162975838519632395L;

	/**
	 * Source entities that make up this DocTopicItem.
	 */
	public final String source;
	/**
	 * Set of {@link TopicItem}s in this document.
	 */
	private Map<Integer, TopicItem> topics;
	//endregion
	
	//region CONSTRUCTORS
	/**
	 * Default constructor for DocTopicItems.
	 *
	 * @param source The source entity of this DocTopicItem.
	 */
	@JsonCreator
	public DocTopicItem(@JsonProperty("source") String source) {
		this.source = source;
		this.topics = new HashMap<>();
	}
	
	/**
	 * Clone contructor.
	 */
	public DocTopicItem(DocTopicItem dti) {
		this.source = dti.source;
		initialize(dti);
	}

	/**
	 * Merge constructor.
	 */
	public DocTopicItem(Cluster c1, Cluster c2)
			throws UnmatchingDocTopicItemsException {
		this(c1.getDocTopicItem(), c2.getDocTopicItem()); }

	/**
	 * Merge constructor.
	 */
	public DocTopicItem(DocTopicItem dti1, DocTopicItem dti2)
			throws UnmatchingDocTopicItemsException {
		// If either argument is null, then return the non-null argument
		if (dti1 == null) {
			this.source = dti2.source;
			initialize(dti2);
			return;
		}
		if (dti2 == null) {
			this.source = dti1.source;
			initialize(dti1);
			return;
		}

		// If arguments do not match, throw exception
		if (!dti1.hasSameTopics(dti2))
			throw new UnmatchingDocTopicItemsException(
				"In mergeDocTopicItems, nonmatching docTopicItems");

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
	 * Initializes a clone.
	 */
	public void initialize(DocTopicItem dti) {
		this.topics = new HashMap<>();
		for (TopicItem topicItem : dti.getTopics())
			addTopic(new TopicItem(topicItem));
	}
	//endregion

	//region ACCESSORS

	/**
	 * Gets a copy of the {@link TopicItem}s in this DocTopicItem.
	 */
	@JsonIgnore
	public List<TopicItem> getTopics() {
		return new ArrayList<>(topics.values()); }

	/**
	 * Gets the original map of {@link TopicItem}s in this DocTopicItem. Meant
	 * ONLY for use with Jackson JSON serialization.
	 */
	public Map<Integer, TopicItem> getTopicsForJackson() {
		//TODO Find a better way to do this
		return this.topics;
	}

	/**
	 * Get the quantity of {@link TopicItem}s in this DocTopicItem.
	 */
	public int size() { return this.topics.size(); }

	/**
	 * Gets a {@link TopicItem} by its {@link TopicItem#topicNum}.
	 */
	public TopicItem getTopic(int topicNum) { return this.topics.get(topicNum); }

	/**
	 * Verifies whether this DocTopicItem contains the given {@link TopicItem}.
	 */
	public boolean hasTopic(int topicNum) { return topics.containsKey(topicNum); }

	/**
	 * Gets the {@link TopicItem#topicNum}s of all {@link TopicItem}s in this
	 * DocTopicItem.
	 */
	@JsonIgnore
	public Set<Integer> getTopicNumbers() { return this.topics.keySet(); }

	/**
	 * Adds the given {@link TopicItem} to this DocTopicItem.
	 */
	public TopicItem addTopic(TopicItem topic) {
		return this.topics.put(topic.topicNum, topic); }

	/**
	 * Verifies whether this DocTopicItem is based on a C source entity.
	 */
	@JsonIgnore
	public boolean isCSourced() {
		return source.endsWith(".c") || source.endsWith(".h")
			|| source.endsWith(".tbl") || source.endsWith(".p")
			|| source.endsWith(".cpp") || source.endsWith(".s")
			|| source.endsWith(".hpp") || source.endsWith(".icc")
			|| source.endsWith(".ia");
	}

	/**
	 * Verifies whether this DocTopicItem has the same {@link TopicItem}s as the
	 * provided DocTopicItem. Only checks their {@link TopicItem#topicNum}, not
	 * the {@link TopicItem#proportion}.
	 */
	public boolean hasSameTopics(DocTopicItem dti) {
		if (dti.size() != this.size())
			return false;

		for (Integer key : this.topics.keySet())
			if (!dti.hasTopic(key))
				return false;

		return true;
	}

	/**
	 * Returns the Jensen-Shannon divergence between the distributions of this
	 * DocTopicItem and the provided DocTopicItem.
	 *
	 * @throws DistributionSizeMismatchException If the two DocTopicItems have
	 * 		different sets of {@link TopicItem}s.
	 */
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
		StringBuilder dtItemStr = new StringBuilder("[" + source + ",");
	
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

		return Objects.equals(this.source, that.source)
			&& Objects.equals(getTopics(), that.getTopics());
	}

	@Override
	public int hashCode() {
		return Objects.hash(source, topics); }
	//endregion
}
