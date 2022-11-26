package edu.usc.softarch.arcade.clustering.criteria;

import edu.usc.softarch.arcade.clustering.data.Architecture;
import edu.usc.softarch.arcade.clustering.criteria.stopping.ArchSizeFractionStoppingCriterion;
import edu.usc.softarch.arcade.clustering.criteria.stopping.PreSelectedStoppingCriterion;

public abstract class StoppingCriterion {
	public enum Criterion {
		PRESELECTED, ARCHSIZEFRACTION
	}

	public abstract boolean notReadyToStop(Architecture arch);

	public static StoppingCriterion makeStoppingCriterion(
		String stoppingCriterion, double stoppingCriterionVal) {
		return makeStoppingCriterion(
			Criterion.valueOf(stoppingCriterion.toUpperCase()),
			stoppingCriterionVal, null);
	}

	public static StoppingCriterion makeStoppingCriterion(
			String stoppingCriterion, double stoppingCriterionVal,
			Architecture arch) {
		return makeStoppingCriterion(
			Criterion.valueOf(stoppingCriterion.toUpperCase()),
			stoppingCriterionVal, arch);
	}

	public static StoppingCriterion makeStoppingCriterion(
			Criterion stoppingCriterion, double stoppingCriterionVal, Architecture arch) {
		switch (stoppingCriterion) {
			case PRESELECTED:
				return new PreSelectedStoppingCriterion((int)stoppingCriterionVal);
			case ARCHSIZEFRACTION:
				if (arch == null) throw new IllegalArgumentException(
					"ArchSizeFraction stopping criterion requires non-null Architecture");
				return new ArchSizeFractionStoppingCriterion(arch, stoppingCriterionVal);
			default:
				throw new IllegalArgumentException(
					"Unknown stopping criterion " + stoppingCriterion);
		}
	}
}
