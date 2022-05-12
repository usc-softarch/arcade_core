package edu.usc.softarch.arcade.clustering.criteria;

import edu.usc.softarch.arcade.clustering.Architecture;

public class ArchSizeSerializationCriterion implements SerializationCriterion {
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
