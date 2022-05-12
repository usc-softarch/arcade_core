package edu.usc.softarch.arcade.clustering.criteria;

import edu.usc.softarch.arcade.clustering.Architecture;

public class ArchSizeModSerializationCriterion
		implements SerializationCriterion {
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
