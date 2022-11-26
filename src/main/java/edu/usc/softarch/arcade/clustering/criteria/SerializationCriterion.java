package edu.usc.softarch.arcade.clustering.criteria;

import edu.usc.softarch.arcade.clustering.data.Architecture;
import edu.usc.softarch.arcade.clustering.criteria.serialization.ArchSizeFractionSerializationCriterion;
import edu.usc.softarch.arcade.clustering.criteria.serialization.ArchSizeModSerializationCriterion;
import edu.usc.softarch.arcade.clustering.criteria.serialization.ArchSizeSerializationCriterion;
import edu.usc.softarch.arcade.clustering.criteria.serialization.StepCountSerializationCriterion;

public abstract class SerializationCriterion {
	public enum Criterion {
		ARCHSIZE, ARCHSIZEMOD, STEPCOUNT, ARCHSIZEFRACTION
	}

	public abstract boolean shouldSerialize();

	public static SerializationCriterion makeSerializationCriterion(
			String serializationCriterion, double criterionValue,
			Architecture arch) {
		return makeSerializationCriterion(
			Criterion.valueOf(serializationCriterion.toUpperCase()),
			criterionValue, arch);
	}

	public static SerializationCriterion makeSerializationCriterion(
			Criterion serializationCriterion, double criterionValue,
			Architecture arch) {
		switch (serializationCriterion) {
			case ARCHSIZE:
				return new ArchSizeSerializationCriterion(arch, (int)criterionValue);
			case ARCHSIZEMOD:
				return new ArchSizeModSerializationCriterion(arch, (int)criterionValue);
			case STEPCOUNT:
				return new StepCountSerializationCriterion((int)criterionValue);
			case ARCHSIZEFRACTION:
				return new ArchSizeFractionSerializationCriterion(arch, criterionValue);
			default:
				throw new IllegalArgumentException(
					"Unknown serialization criterion " + serializationCriterion);
		}
	}
}
