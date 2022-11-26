package edu.usc.softarch.arcade.clustering.criteria.serialization;

import edu.usc.softarch.arcade.clustering.data.Architecture;

public class ArchSizeFractionSerializationCriterion
		extends ArchSizeSerializationCriterion {
	public ArchSizeFractionSerializationCriterion(Architecture arch, double fraction) {
		super(arch, (int)(arch.size() * fraction));
	}
}
