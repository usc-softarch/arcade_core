package edu.usc.softarch.arcade.clustering.criteria;

import edu.usc.softarch.arcade.clustering.Architecture;

public abstract class SerializationCriterion {
	public abstract boolean shouldSerialize();

	public static SerializationCriterion makeSerializationCriterion(
		String serializationCriterion, int criterionValue,
		Architecture arch) {
		switch (serializationCriterion.toLowerCase()) {
			case "archsize":
				return new ArchSizeSerializationCriterion(arch, criterionValue);
			case "archsizemod":
				return new ArchSizeModSerializationCriterion(arch, criterionValue);
			case "stepcount":
				return new StepCountSerializationCriterion(criterionValue);
			default:
				throw new IllegalArgumentException(
					"Unknown serialization criterion " + serializationCriterion);
		}
	}
}
