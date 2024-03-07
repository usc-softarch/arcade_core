package edu.usc.softarch.arcade.antipattern.detection.concern;

import edu.usc.softarch.arcade.antipattern.Smell;
import edu.usc.softarch.arcade.antipattern.SmellCollection;
import edu.usc.softarch.arcade.clustering.data.ReadOnlyArchitecture;
import edu.usc.softarch.arcade.clustering.data.ReadOnlyCluster;
import edu.usc.softarch.arcade.topics.TopicItem;
import edu.usc.softarch.arcade.util.CentralTendency;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ScatteredParasiticFunctionality {
	public static SmellCollection detect(ReadOnlyArchitecture arch) {
		return detect(arch, .10, .10);
	}

	public static SmellCollection detect(ReadOnlyArchitecture arch,
			double scatteredConcernThreshold, double parasiticConcernThreshold) {
		// Counts how many times a smelly topic appears
		Map<Integer, Integer> topicCounts = new HashMap<>();
		// Maps smelly topics to the clusters they appear in
		Map<Integer, Collection<ReadOnlyCluster>> topicClusters = new HashMap<>();

		// Count smelly topics and map them to relevant clusters
		for (ReadOnlyCluster cluster : arch.values()) {
			// Filter out non-smelly topic items
			Collection<TopicItem> smellyTopicItems =
				cluster.getDocTopicItem().getTopics();
			smellyTopicItems.removeIf(
				ti ->	ti.getProportion() < scatteredConcernThreshold);

			for (TopicItem ti : smellyTopicItems) {
				// Adds the topic to the count map if absent, increments otherwise
				topicCounts.compute(ti.topicNum, (k, v) -> (v == null) ? 1 : ++v);

				topicClusters.putIfAbsent(ti.topicNum, new ArrayList<>());
				topicClusters.get(ti.topicNum).add(cluster);
			}
		}

		// Calculate significance threshold
		double[] topicCountsArray = topicCounts.values().stream()
			.mapToDouble(Integer::doubleValue).toArray();
		CentralTendency ct = new CentralTendency(topicCountsArray);
		double significanceThreshold = ct.getMean() + ct.getStDev();

		// Select significant topics
		Set<Integer> topicNums = new HashSet<>(topicCounts.keySet());
		topicNums.removeIf(ti -> topicCounts.get(ti) <= significanceThreshold);

		// Evaluate topics
		SmellCollection result = new SmellCollection();
		for (Integer topicNum : topicNums) {
			Smell spf = buildSpf(parasiticConcernThreshold, topicClusters, topicNum);
			result.add(spf);
		}

		return result;
	}

	/**
	 * Builds a single SPF instance for the given topic.
	 *
	 * @param parasiticConcernThreshold The threshold at which a topic is
	 *                                  considered a parasite to this topic.
	 * @param topicClusters The map of clusters for each topic.
	 * @param topicNum The topic for which the SPF instance will be built.
	 */
	private static Smell buildSpf(double parasiticConcernThreshold,
			Map<Integer, Collection<ReadOnlyCluster>> topicClusters,
			Integer topicNum) {
		Collection<ReadOnlyCluster> clusters = topicClusters.get(topicNum);
		Collection<String> affectedClusters = new ArrayList<>();

		for (ReadOnlyCluster cluster : clusters) {
			Collection<TopicItem> parasiteTopics = new ArrayList<>(
				cluster.getDocTopicItem().getTopics());
			parasiteTopics.removeIf(ti ->
				ti.topicNum == topicNum // parasites are topics other than this one
				|| ti.getProportion() < parasiticConcernThreshold);

			if (!parasiteTopics.isEmpty()) affectedClusters.add(cluster.name);
		}

		Smell spf = new Smell(topicNum);
		spf.addClusterCollection(affectedClusters);
		return spf;
	}
}
