package edu.usc.softarch.arcade.clustering.criteria.serialization;

import edu.usc.softarch.arcade.clustering.data.Architecture;
import edu.usc.softarch.arcade.clustering.criteria.SerializationCriterion;

public class ArchSizeSerializationCriterion extends SerializationCriterion {
	private final Architecture arch;
	private final int archSize;

	public ArchSizeSerializationCriterion(Architecture arch, int archSize) {
		this.arch = arch;
		this.archSize = archSize;
	}

	@Override
	public boolean shouldSerialize() {
		return this.arch.size() == this.archSize;
	}
}
