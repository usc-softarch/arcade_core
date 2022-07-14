package edu.usc.softarch.arcade.facts.smells;

import edu.usc.softarch.arcade.antipattern.Smell;
import edu.usc.softarch.arcade.antipattern.SmellCollection;
import edu.usc.softarch.arcade.clustering.ReadOnlyArchitecture;
import edu.usc.softarch.arcade.facts.Change;
import edu.usc.softarch.arcade.facts.VersionMap;
import edu.usc.softarch.arcade.facts.VersionTree;
import edu.usc.softarch.arcade.facts.design.ClusterDecision;
import edu.usc.softarch.arcade.facts.design.CodeElementDecision;
import edu.usc.softarch.arcade.util.McfpDriver;
import edu.usc.softarch.util.json.EnhancedJsonGenerator;
import edu.usc.softarch.util.json.EnhancedJsonParser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class SmellDecisionAnalyzer {
	//region PUBLIC INTERFACE
	public static void main(String[] args) throws IOException {
		String clusters = args[0];
		String smells = args[1];
		String versionTree = args[2];
		String decisions = args[3];

		Collection<SmellDecision> result =
			run(clusters, smells, versionTree, decisions);

		File outputFile = new File(args[4]);
		outputFile.getParentFile().mkdirs();

		try (EnhancedJsonGenerator generator = new EnhancedJsonGenerator(outputFile)) {
			generator.writeField("smellDecisions", result);
		}
	}

	public static Collection<SmellDecision> run(String clusters, String smells,
			String versionTree, String decisions)	throws IOException {
		SmellDecisionAnalyzer runner =
			new SmellDecisionAnalyzer(clusters, smells, versionTree, decisions);

		return runner.correlate();
	}
	//endregion

	//region ATTRIBUTES
	private final VersionTree versionTree;
	private final Map<String, ReadOnlyArchitecture> architectures;
	private final Map<String, SmellCollection> smellCollections;
	private final Collection<ClusterDecision> decisions;
	//endregion

	//region CONSTRUCTORS
	public SmellDecisionAnalyzer(String clusters, String smells,
			String versionTree, String decisions) throws IOException {
		this.versionTree = VersionTree.deserialize(versionTree);
		this.architectures = loadArchitectures(clusters);
		this.smellCollections = loadSmells(smells);

		try (EnhancedJsonParser parser = new EnhancedJsonParser(decisions)) {
			this.decisions = loadDecisions(
				this.versionTree, parser.parseCollection(CodeElementDecision.class));
		}
	}

	private Map<String, ReadOnlyArchitecture> loadArchitectures(String clusters)
			throws IOException {
		Map<String, ReadOnlyArchitecture> result = new HashMap<>();

		VersionMap versionMap = new VersionMap(clusters);
		for (Map.Entry<String, File> clustersFile : versionMap.entrySet())
			result.put(clustersFile.getKey(),
				ReadOnlyArchitecture.readFromRsf(clustersFile.getValue()));

		return result;
	}

	private Map<String, SmellCollection> loadSmells(String smells)
			throws IOException {
		Map<String, SmellCollection> result = new HashMap<>();

		VersionMap versionMap = new VersionMap(smells, "[0-9]+\\.[0-9]+(\\.[0-9]+)*", ".json");
		for (Map.Entry<String, File> smellsFile : versionMap.entrySet())
			result.put(smellsFile.getKey(),
				SmellCollection.deserialize(smellsFile.getValue()));

		return result;
	}

	private Collection<ClusterDecision> loadDecisions(VersionTree currentVersion,
			Collection<CodeElementDecision> elementDecisions) {
		Collection<ClusterDecision> result = new ArrayList<>();

		for (VersionTree nextVersion : currentVersion.getChildren()) {
			ReadOnlyArchitecture currentVersionArch =
				this.architectures.get(currentVersion.node);
			ReadOnlyArchitecture nextVersionArch =
				this.architectures.get(nextVersion.node);

			Collection<CodeElementDecision> relevantDecisions = elementDecisions
				.stream().filter(d -> d.version.equals(nextVersion.node))
				.collect(Collectors.toList());

			McfpDriver mcfpDriver =
				new McfpDriver(currentVersionArch, nextVersionArch);

			for (CodeElementDecision relevantDecision : relevantDecisions)
				result.add(new ClusterDecision(relevantDecision, currentVersionArch,
					nextVersionArch, mcfpDriver.getMatchSet()));

			result.addAll(this.loadDecisions(nextVersion, elementDecisions));
		}

		return result;
	}
	//endregion

	//region PROCESSING
	public Collection<SmellDecision> correlate() {
		return correlate(this.versionTree); }

	private Collection<SmellDecision> correlate(VersionTree currentVersion) {
		Collection<SmellDecision> result = new ArrayList<>();

		for (VersionTree nextVersion : currentVersion.getChildren()) {
			ReadOnlyArchitecture currentVersionArch =
				this.architectures.get(currentVersion.node);
			ReadOnlyArchitecture nextVersionArch =
				this.architectures.get(nextVersion.node);
			SmellCollection currentVersionSmells =
				this.smellCollections.get(currentVersion.node);
			SmellCollection nextVersionSmells =
				this.smellCollections.get(nextVersion.node);

			SmellChangeAnalyzer changeAnalyzer = new SmellChangeAnalyzer(
				currentVersionArch, nextVersionArch,
				currentVersionSmells, nextVersionSmells);

			Collection<Change<Smell>> changes = changeAnalyzer.getChangeList();

			Collection<ClusterDecision> relevantDecisions = this.decisions
				.stream().filter(d -> d.version.equals(nextVersion.node))
				.collect(Collectors.toList());

			for (ClusterDecision decision : relevantDecisions) {
				SmellDecision smellDecision = new SmellDecision(decision, changes);
				if (!smellDecision.isEmpty()) result.add(smellDecision);
			}

			result.addAll(this.correlate(nextVersion));
		}

		return result;
	}
	//endregion
}
