package edu.usc.softarch.arcade.topics;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.usc.softarch.arcade.clustering.Cluster;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a topic's prevalence in a document.
 */
public class TopicItem implements Serializable, Comparable<TopicItem> {
	//region ATTRIBUTES
	private static final long serialVersionUID = 4599518018739063447L;

	/**
	 * Topic number that this TopicItem is related to.
	 */
	public final int topicNum;
	/**
	 * Weight of this TopicItem, i.e. how many entities it represents. Used as a
	 * multiplier to proportion when calculating the average proportion of two
	 * TopicItems, primarily when merging two {@link Cluster} objects.
	 */
	public final int weight;
	/**
	 * Proportion of this TopicItem within the context of a {@link DocTopicItem}
	 * object.
	 */
	public final double proportion;
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
		this.weight = 1;
		this.topicNum = topicNum;
		this.proportion = proportion;
	}

	/**
	 * Constructor for TopicItems with pre-defined weight. Primarily used for
	 * deserialization.
	 *
	 * @param topicNum Topic number of the new TopicItem.
	 * @param proportion Proportion of the new TopicItem within its
	 * 									 {@link DocTopicItem}.
	 * @param weight Weight of (number of entities represented by) this TopicItem.
	 */
	@JsonCreator
	public TopicItem(@JsonProperty("topicNum") int topicNum,
									 @JsonProperty("proportion") double proportion,
									 @JsonProperty("weight") int weight) {
		this.weight = weight;
		this.topicNum = topicNum;
		this.proportion = proportion;
	}

	/**
	 * Constructor for merging TopicItems.
	 *
	 * @param ti1 TopicItem from first entity being merged.
	 * @param ti2 TopicItem from second entity being merged.
	 */
	public TopicItem(TopicItem ti1, TopicItem ti2) {
		this.weight = ti1.weight + ti2.weight;
		this.topicNum = ti1.topicNum;
		this.proportion = (ti1.proportion * ti1.weight
			+ ti2.proportion * ti2.weight) / this.weight;
	}

	/**
	 * Clone constructor for TopicItems.
	 *
	 * @param topicItem TopicItem to clone.
	 */
	public TopicItem(TopicItem topicItem) {
		this.weight = topicItem.weight;
		this.topicNum = topicItem.topicNum;
		this.proportion = topicItem.proportion;
	}
	//endregion
	
	//region OBJECT METHODS
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof TopicItem)) return false;

		TopicItem topicItem = (TopicItem) o;

		return this.topicNum == topicItem.topicNum
			&& this.weight == topicItem.weight
			&& Double.compare(topicItem.proportion, this.proportion) == 0;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.topicNum, this.weight, this.proportion);	}

	public String toString() {
		return "[" + this.topicNum + "," + this.proportion + "]"; }

	@Override
	public int compareTo(TopicItem o) {
		return Double.compare(this.proportion, o.proportion);	}
	//endregion
}
