package edu.usc.softarch.arcade.antipattern.detection.concern;

import edu.usc.softarch.arcade.antipattern.Smell;
import edu.usc.softarch.arcade.antipattern.SmellCollection;
import edu.usc.softarch.arcade.clustering.ReadOnlyArchitecture;
import edu.usc.softarch.arcade.clustering.data.ReadOnlyCluster;
import edu.usc.softarch.arcade.topics.TopicItem;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ConcernOverload {
	public static SmellCollection detect(ReadOnlyArchitecture arch) {
		return detect(arch, .10);
	}

	public static SmellCollection detect(ReadOnlyArchitecture arch,
			double overloadThreshold) {
		SmellCollection result = new SmellCollection();

		Map<String, Integer> concernCounts = countConcerns(arch, overloadThreshold);

		double[] doubleConcernCountValues =
			concernCounts.values().stream().mapToDouble(Double::valueOf).toArray();

		double concernCountMean = Arrays.stream(doubleConcernCountValues).sum()
			/ doubleConcernCountValues.length;
		double concernCountStdev =
			(new StandardDeviation()).evaluate(doubleConcernCountValues);

		for (Map.Entry<String, Integer> entry : concernCounts.entrySet()) {
			if (entry.getValue() > concernCountMean + concernCountStdev) {
				Smell bco = new Smell(Smell.SmellType.bco);
				bco.addCluster(entry.getKey());
				result.add(bco);
			}
		}

		return result;
	}

	private static Map<String, Integer> countConcerns(ReadOnlyArchitecture arch,
			double overloadThreshold) {
		Map<String, Integer> result = new HashMap<>();

		for (ReadOnlyCluster cluster : arch.values()) {
			int count = 0;
			for (TopicItem topic : cluster.getDocTopicItem().getTopics())
				if (topic.getProportion() > overloadThreshold) count++;
			result.put(cluster.name, count);
		}

		return result;
	}
}
