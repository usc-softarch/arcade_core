package edu.usc.softarch.arcade.facts.smells;

import edu.usc.softarch.arcade.antipattern.Smell;
import edu.usc.softarch.arcade.facts.Change;
import edu.usc.softarch.arcade.facts.Decision;
import edu.usc.softarch.arcade.facts.design.ClusterDecision;
import edu.usc.softarch.util.json.EnhancedJsonGenerator;
import edu.usc.softarch.util.json.EnhancedJsonParser;
import edu.usc.softarch.util.json.JsonSerializable;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SmellDecision extends Decision implements JsonSerializable {
	//region ATTRIBUTES
	private final Map<Smell, String> addedSmells;
	private final Map<Smell, String> removedSmells;
	//endregion

	//region CONSTRUCTORS
	public SmellDecision(ClusterDecision decision,
			Collection<Change<Smell>> smellChanges) {
		super(decision.description, decision.id, decision.version);

		this.addedSmells = new HashMap<>();
		this.removedSmells = new HashMap<>();

		for (Change<Smell> change : smellChanges) {
			if (decision.containsArch1Cluster(change.priorClusterName)
					|| decision.containsArch2Cluster(change.newClusterName)) {
				for (Smell addedSmell : change.getAddedElements())
					this.addedSmells.put(addedSmell,
						change.priorClusterName + " : " + change.newClusterName);
				for (Smell removedSmell : change.getRemovedElements())
					this.removedSmells.put(removedSmell,
						change.priorClusterName + " : " + change.newClusterName);
			}
		}
	}

	private SmellDecision(String description, String id, String version,
			Map<Smell, String> addedSmells, Map<Smell, String> removedSmells) {
		super(description, id, version);

		this.addedSmells = addedSmells;
		this.removedSmells = removedSmells;
	}
	//endregion

	//region ACCESSORS
	public boolean isEmpty() {
		return this.addedSmells.isEmpty() && this.removedSmells.isEmpty(); }
	//endregion

	//region SERIALIZATION
	@Override
	public void serialize(EnhancedJsonGenerator generator) throws IOException {
		generator.writeField("description", super.description);
		generator.writeField("id", super.id);
		generator.writeField("version", super.version);
		generator.writeField("addedSmells", this.addedSmells,
			true, "smell", "clusters");
		generator.writeField("removedSmells", this.removedSmells,
			true, "smell", "clusters");
	}

	public static SmellDecision deserialize(EnhancedJsonParser parser)
		throws IOException {
		String description = parser.parseString();
		String id = parser.parseString();
		String version = parser.parseString();
		Map<Smell, String> addedElements =
			parser.parseMap(Smell.class, String.class);
		Map<Smell, String> removedElements =
			parser.parseMap(Smell.class, String.class);

		return new SmellDecision(
			description, id, version, addedElements, removedElements);
	}
	//endregion
}
