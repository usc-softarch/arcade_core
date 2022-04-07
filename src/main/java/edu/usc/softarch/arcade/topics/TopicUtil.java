package edu.usc.softarch.arcade.topics;

import java.util.Set;

import edu.usc.softarch.arcade.clustering.Cluster;

/**
 * @author joshua
 */
public class TopicUtil {
	public static DocTopics docTopics;

	/**
	 * Merges the proportions of two DocTopicItems that contain the same topic
	 * numbers. Merging is done by taking the average of the proportions.
	 * 
	 * @param docTopicItem First DocTopicItem to merge.
	 * @param docTopicItem2 Second DocTopicItem to merge.
	 * @return Merged DocTopicItem.
	 * @throws UnmatchingDocTopicItemsException If the two DocTopicItems contain
	 * 	different topic numbers.
	 */
	public static DocTopicItem mergeDocTopicItems(DocTopicItem docTopicItem,
			DocTopicItem docTopicItem2) throws UnmatchingDocTopicItemsException {
		// If either argument is null, then return the non-null argument
		if (docTopicItem == null)
			return new DocTopicItem(docTopicItem2);
		if (docTopicItem2 == null)
			return new DocTopicItem(docTopicItem);

		// If arguments do not match, throw exception
		if (!docTopicItem.hasSameTopics(docTopicItem2))
			throw new UnmatchingDocTopicItemsException(
				"In mergeDocTopicItems, nonmatching docTopicItems");

		DocTopicItem mergedDocTopicItem = new DocTopicItem(docTopicItem);
		Set<Integer> topicNumbers = docTopicItem.getTopicNumbers();

		for (Integer i : topicNumbers) {
			TopicItem ti1 = docTopicItem.getTopic(i);
			TopicItem ti2 = docTopicItem2.getTopic(i);
			TopicItem mergedTopicItem = mergedDocTopicItem.getTopic(i);

			mergedTopicItem.setProportion(
				(ti1.getProportion() + ti2.getProportion()) / 2);
		}

		return mergedDocTopicItem;
	}

	/**
	 * Sets the DocTopicItem of a FastCluster.
	 */
	public static void setDocTopicForFastClusterForMalletApi(
					Cluster c, String language) {
		c.docTopicItem = docTopics.getDocTopicItem(c.getName(), language);
	}
}
