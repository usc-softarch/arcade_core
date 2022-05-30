package edu.usc.softarch.arcade.clustering.criteria;

import edu.usc.softarch.arcade.clustering.Architecture;

public class ArchSizeFractionStoppingCriterion
		extends PreSelectedStoppingCriterion {
	public ArchSizeFractionStoppingCriterion(Architecture arch, double stoppingCriterionVal) {
		super((int)(arch.size() * stoppingCriterionVal)); }
}
