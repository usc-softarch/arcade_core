package edu.usc.softarch.arcade.topics;

import edu.usc.softarch.util.json.EnhancedJsonGenerator;
import edu.usc.softarch.util.json.EnhancedJsonParser;
import edu.usc.softarch.util.json.JsonSerializable;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

/**
 * Represents a topic's prevalence in a document.
 */
public class TopicItem
		implements JsonSerializable, Serializable, Comparable<TopicItem> {
	//region ATTRIBUTES
	private static final long serialVersionUID = 4599518018739063447L;

	/**
	 * Topic number that this TopicItem is related to.
	 */
	public final int topicNum;
	/**
	 * This stops rounding errors from propagating by retaining the original
	 * component proportions and re-calculating a TopicItem's proportion each
	 * time that is necessary.
	 */
	private final double[] proportionComponents;
	//endregion
	
	//region CONTRUCTORS
	/**
	 * Constructor for new TopicItems.
	 *
	 * @param topicNum Topic number of the new TopicItem.
	 * @param proportion Proportion of the new TopicItem within its
	 * 									 {@link DocTopicItem}.
	 */
	public TopicItem(int topicNum, double proportion) {
		this.topicNum = topicNum;
		this.proportionComponents = new double[1];
		this.proportionComponents[0] = proportion;
	}

	/**
	 * Constructor for merging TopicItems.
	 *
	 * @param ti1 TopicItem from first entity being merged.
	 * @param ti2 TopicItem from second entity being merged.
	 */
	public TopicItem(TopicItem ti1, TopicItem ti2) {
		this.topicNum = ti1.topicNum;
		this.proportionComponents = new double[
			ti1.proportionComponents.length + ti2.proportionComponents.length];
		int index = 0;
		for (double value : ti1.proportionComponents)
			this.proportionComponents[index++] = value;
		for (double value : ti2.proportionComponents)
			this.proportionComponents[index++] = value;
	}

	/**
	 * Clone constructor for TopicItems.
	 *
	 * @param topicItem TopicItem to clone.
	 */
	public TopicItem(TopicItem topicItem) {
		this.topicNum = topicItem.topicNum;
		this.proportionComponents = topicItem.proportionComponents;
	}
	//endregion

	//region ACCESSORS
	public double getProportion() {
		return Arrays.stream(this.proportionComponents).sum()
			/ this.proportionComponents.length;
	}
	//endregion
	
	//region OBJECT METHODS
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof TopicItem)) return false;

		TopicItem topicItem = (TopicItem) o;

		return this.topicNum == topicItem.topicNum
			&& Arrays.equals(this.proportionComponents, topicItem.proportionComponents);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.topicNum, Arrays.hashCode(this.proportionComponents));	}

	public String toString() {
		return "[" + this.topicNum + "," + this.getProportion() + "]"; }

	@Override
	public int compareTo(TopicItem o) {
		return Double.compare(this.getProportion(), o.getProportion());	}
	//endregion

	//region SERIALIZATION
	@Override
	public void serialize(EnhancedJsonGenerator generator) throws IOException {
		generator.writeField("topicNum", this.topicNum);
		generator.writeField("proportion", this.getProportion());
	}

	public static TopicItem deserialize(EnhancedJsonParser parser)
			throws IOException {
		int topicNum = parser.parseInt();
		double proportion = parser.parseDouble();
		return new TopicItem(topicNum, proportion);
	}
	//endregion
}
