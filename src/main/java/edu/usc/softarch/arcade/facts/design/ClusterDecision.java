package edu.usc.softarch.arcade.facts.design;

import edu.usc.softarch.arcade.clustering.ReadOnlyArchitecture;
import edu.usc.softarch.arcade.clustering.data.ReadOnlyCluster;
import edu.usc.softarch.arcade.facts.Decision;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ClusterDecision extends Decision {
	//region ATTRIBUTES
	private final Map<String, String> preToPostMap;
	private final Map<String, String> postToPreMap;
	//endregion

	//region CONSTRUCTORS
	public ClusterDecision(CodeElementDecision decision,
			ReadOnlyArchitecture arch1, ReadOnlyArchitecture arch2,
			Map<String, String> matchSet) {
		super(decision.description, decision.id, decision.version);

		this.preToPostMap = new HashMap<>();
		this.postToPreMap = new HashMap<>();

		Map<String, String> inverseMatchSet = matchSet.entrySet().stream()
			.collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

		for (ReadOnlyCluster cluster : arch1.values())
			for (String removedElement : decision.getRemovedElements())
				if (cluster.getEntities().contains(removedElement)) {
					this.preToPostMap.put(cluster.name, matchSet.get(cluster.name));
					this.postToPreMap.put(matchSet.get(cluster.name), cluster.name);
					break;
				}

		for (ReadOnlyCluster cluster : arch2.values())
			for (String addedElement : decision.getAddedElements())
				if (cluster.getEntities().contains(addedElement)) {
					this.preToPostMap.put(inverseMatchSet.get(cluster.name), cluster.name);
					this.postToPreMap.put(cluster.name, inverseMatchSet.get(cluster.name));
					break;
				}
	}
	//endregion

	//region ACCESSORS
	public Collection<String> getArch1Clusters() {
		return new ArrayList<>(this.preToPostMap.keySet());	}
	public Collection<String> getArch2Clusters() {
		return new ArrayList<>(this.postToPreMap.keySet()); }
	public boolean containsArch1Cluster(String clusterName) {
		return this.preToPostMap.containsKey(clusterName); }
	public boolean containsArch2Cluster(String clusterName) {
		return this.postToPreMap.containsKey(clusterName); }
	//endregion
}
