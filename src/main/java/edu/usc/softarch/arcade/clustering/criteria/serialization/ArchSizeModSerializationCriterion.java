package edu.usc.softarch.arcade.clustering.criteria.serialization;

import edu.usc.softarch.arcade.clustering.data.Architecture;
import edu.usc.softarch.arcade.clustering.criteria.SerializationCriterion;

public class ArchSizeModSerializationCriterion
		extends SerializationCriterion {
	private final int modValue;
	private final Architecture arch;

	public ArchSizeModSerializationCriterion(Architecture arch, int modValue) {
		this.arch = arch;
		this.modValue = modValue;
	}

	@Override
	public boolean shouldSerialize() {
		return this.arch.size() % this.modValue == 0;
	}
}
