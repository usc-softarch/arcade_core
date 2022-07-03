package edu.usc.softarch.arcade.facts.design;

import edu.usc.softarch.util.json.EnhancedJsonGenerator;
import edu.usc.softarch.util.json.EnhancedJsonParser;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class DecisionRankingEngine {
	//region PUBLIC INTERFACE
	public static void main(String[] args) throws IOException {
		DecisionRankingEngine engine = new DecisionRankingEngine(
			args[0], args[1], Arrays.copyOfRange(args, 2, args.length));
		Map<Integer, Collection<Integer>> ranking = engine.rankDecisions();

		try (PrintWriter writer =
				new PrintWriter(engine.rankingPath, StandardCharsets.UTF_8)) {
			for (Map.Entry<Integer, Collection<Integer>> entry : ranking.entrySet()) {
				writer.println(entry.getKey() + ":");
				writer.println("Count: " + entry.getValue().size());
				writer.println();
				writer.println(entry.getValue());
				writer.println();
			}
		}

		try (EnhancedJsonGenerator generator =
				new EnhancedJsonGenerator(engine.topRankPath)) {
			generator.writeField("decisions", engine.buildTopRankList());
		}
	}
	//endregion

	//region ATTRIBUTES
	public final String rankingPath;
	public final String topRankPath;
	private final Collection<Map<Integer, Decision>> decisionSets;
	private final Map<Integer, Collection<Integer>> ranking;
	//endregion

	//region CONSTRUCTORS
	public DecisionRankingEngine(String rankingPath, String topRankPath,
			String... inputPath) throws IOException {
		this.rankingPath = rankingPath;
		this.topRankPath = topRankPath;
		decisionSets = new ArrayList<>();
		ranking = new HashMap<>();

		for (String path : inputPath) {
			try (EnhancedJsonParser parser = new EnhancedJsonParser(path)) {
				Collection<Decision> result = parser.parseCollection(Decision.class);
				Map<Integer, Decision> resultMap = new HashMap<>();
				for (Decision decision : result)
					resultMap.put(Integer.parseInt(decision.id), decision);

				this.decisionSets.add(resultMap);
			}
		}
	}
	//endregion

	//region PROCESSING
	public Map<Integer, Collection<Integer>> rankDecisions() {
		Map<Integer, Integer> appearanceCounts = new HashMap<>();

		for (Map<Integer, Decision> decisionSet : decisionSets) {
			for (Integer id : decisionSet.keySet()) {
				Integer count = appearanceCounts.get(id);
				if (count == null)
					appearanceCounts.put(id, 1);
				else
					appearanceCounts.put(id, ++count);
			}
		}

		for (int i = 1; i < 6; i++)
			ranking.put(i, new ArrayList<>());

		for (Map.Entry<Integer, Integer> entry : appearanceCounts.entrySet())
			ranking.get(entry.getValue()).add(entry.getKey());

		return ranking;
	}

	public Collection<Decision> buildTopRankList() {
		Collection<Decision> result = new ArrayList<>();

		Collection<Integer> topRankIds = ranking.get(5);

		for (Integer id : topRankIds) {
			Decision hit = null;
			Collection<String> addedElements = new HashSet<>();
			Collection<String> removedElements = new HashSet<>();

			for (Map<Integer, Decision> decisionSet : decisionSets) {
				hit = decisionSet.get(id);
				addedElements.addAll(hit.getAddedElements());
				removedElements.addAll(hit.getRemovedElements());
			}

			result.add(new Decision(hit.description, hit.id, hit.version,
				addedElements, removedElements));
		}

		return result;
	}
	//endregion
}
