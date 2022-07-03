package edu.usc.softarch.arcade.clustering.criteria;

import edu.usc.softarch.arcade.clustering.Architecture;

public abstract class StoppingCriterion {
	public abstract boolean notReadyToStop(Architecture arch);

	public static StoppingCriterion makeStoppingCriterion(
			String stoppingCriterion, double stoppingCriterionVal, Architecture arch) {
		switch (stoppingCriterion) {
			case "preselected":
				return new PreSelectedStoppingCriterion((int)stoppingCriterionVal);
			case "archsizefraction":
				if (arch == null) throw new IllegalArgumentException(
					"ArchSizeFraction stopping criterion requires non-null Architecture");
				return new ArchSizeFractionStoppingCriterion(arch, stoppingCriterionVal);
			default:
				throw new IllegalArgumentException(
					"Unknown stopping criterion " + stoppingCriterion);
		}
	}

	public static StoppingCriterion makeStoppingCriterion(
			String stoppingCriterion, double stoppingCriterionVal) {
		return makeStoppingCriterion(
			stoppingCriterion, stoppingCriterionVal, null);
	}
}
