package edu.usc.softarch.arcade.clustering.criteria;

import edu.usc.softarch.arcade.clustering.Architecture;
import edu.usc.softarch.arcade.clustering.criteria.serialization.ArchSizeFractionSerializationCriterion;
import edu.usc.softarch.arcade.clustering.criteria.serialization.ArchSizeModSerializationCriterion;
import edu.usc.softarch.arcade.clustering.criteria.serialization.ArchSizeSerializationCriterion;
import edu.usc.softarch.arcade.clustering.criteria.serialization.StepCountSerializationCriterion;

public abstract class SerializationCriterion {
	public abstract boolean shouldSerialize();

	public static SerializationCriterion makeSerializationCriterion(
		String serializationCriterion, double criterionValue,
		Architecture arch) {
		switch (serializationCriterion.toLowerCase()) {
			case "archsize":
				return new ArchSizeSerializationCriterion(arch, (int)criterionValue);
			case "archsizemod":
				return new ArchSizeModSerializationCriterion(arch, (int)criterionValue);
			case "stepcount":
				return new StepCountSerializationCriterion((int)criterionValue);
			case "archsizefraction":
				return new ArchSizeFractionSerializationCriterion(arch, criterionValue);
			default:
				throw new IllegalArgumentException(
					"Unknown serialization criterion " + serializationCriterion);
		}
	}
}
