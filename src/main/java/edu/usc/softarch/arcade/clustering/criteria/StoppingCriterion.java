package edu.usc.softarch.arcade.clustering.criteria;

import edu.usc.softarch.arcade.clustering.Architecture;

public abstract class StoppingCriterion {
	public abstract boolean notReadyToStop(Architecture arch);

	public static StoppingCriterion makeStoppingCriterion(
		String stoppingCriterion, int stoppingCriterionVal) {
		switch (stoppingCriterion) {
			case "clustergain":
				return new ClusterGainStoppingCriterion();
			case "preselected":
				return new PreSelectedStoppingCriterion(stoppingCriterionVal);
			default:
				throw new IllegalArgumentException(
					"Unknown stopping criterion " + stoppingCriterion);
		}
	}
}
