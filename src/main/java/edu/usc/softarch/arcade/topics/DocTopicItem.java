package edu.usc.softarch.arcade.topics;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import cc.mallet.util.Maths;
import edu.usc.softarch.arcade.topics.exceptions.DistributionSizeMismatchException;
import edu.usc.softarch.arcade.topics.exceptions.UnmatchingDocTopicItemsException;
import edu.usc.softarch.util.json.EnhancedJsonGenerator;
import edu.usc.softarch.util.json.EnhancedJsonParser;
import edu.usc.softarch.util.json.JsonSerializable;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * The set of {@link TopicItem}s in a document. A document represents a cluster.
 */
public class DocTopicItem implements Serializable, JsonSerializable {
	//region ATTRIBUTES
	private static final long serialVersionUID = 5162975838519632395L;

	/**
	 * Source entities that make up this DocTopicItem.
	 */
	String source;
	/**
	 * Set of {@link TopicItem}s in this document.
	 */
	private Map<Integer, TopicItem> topics;
	/**
	 * Set of the most important words related to this "concern".
	 */
	private transient Concern concern; //TODO fix transient
	//endregion
	
	//region CONSTRUCTORS
	/**
	 * Default constructor for DocTopicItems.
	 *
	 * @param source The source entity of this DocTopicItem.
	 */
	DocTopicItem(String source) {
		if (source.isEmpty())
			throw new IllegalArgumentException("DocTopicItem source cannot be empty");

		this.source = source;
		this.topics = new TreeMap<>();
	}

	/**
	 * Merge constructor.
	 */
	DocTopicItem(DocTopicItem dti1, DocTopicItem dti2, String newName)
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

		this.source = newName;
		this.topics = new TreeMap<>();
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
	void initialize(DocTopicItem dti) {
		this.topics = new TreeMap<>();
		for (TopicItem topicItem : dti.getTopics())
			addTopic(new TopicItem(topicItem));
	}
	//endregion

	//region ACCESSORS
	/**
	 * Gets a copy of the {@link TopicItem}s in this DocTopicItem.
	 */
	public List<TopicItem> getTopics() {
		return new ArrayList<>(topics.values()); }

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
	public Set<Integer> getTopicNumbers() { return this.topics.keySet(); }

	/**
	 * Adds the given {@link TopicItem} to this DocTopicItem.
	 */
	public TopicItem addTopic(TopicItem topic) {
		return this.topics.put(topic.topicNum, topic); }

	/**
	 * Verifies whether this DocTopicItem is based on a C source entity.
	 */
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
	 * the {@link TopicItem#getProportion()}.
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
			sortedP[pTopicItem.topicNum] = pTopicItem.getProportion();
		
		for (TopicItem qTopicItem : toCompareTopics)
			sortedQ[qTopicItem.topicNum] = qTopicItem.getProportion();
		
		return Maths.jensenShannonDivergence(sortedP, sortedQ);
	}

	//TODO make this less horrible
	public Concern getConcern() {
		if (this.concern == null)
			throw new IllegalStateException("Attempted to get concern before " +
				"it was computed");
		return new Concern(this.concern);
	}
	//endregion

	//region PROCESSING
	public Concern computeConcern(Map<Integer, List<String>> wordBags) {
		if (this.concern == null)
			this.concern = new Concern(wordBags, this.topics);
		return new Concern(this.concern);
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

		return this.source.equals(that.source);
	}

	@Override
	public int hashCode() {
		return Objects.hash(source); }
	//endregion

	//region SERIALIZATION
	@Override
	public void serialize(EnhancedJsonGenerator generator) throws IOException {
		generator.writeField("source", this.source);
		generator.writeField("topics", this.topics.values());
	}

	public static DocTopicItem deserialize(EnhancedJsonParser parser)
			throws IOException {
		DocTopicItem toReturn = new DocTopicItem(parser.parseString());
		Collection<TopicItem> topicItems = parser.parseCollection(TopicItem.class);

		for (TopicItem topicItem : topicItems)
			toReturn.topics.put(topicItem.topicNum, topicItem);

		DocTopics.getSingleton().addDocTopicItem(toReturn);

		return toReturn;
	}
	//endregion
}
