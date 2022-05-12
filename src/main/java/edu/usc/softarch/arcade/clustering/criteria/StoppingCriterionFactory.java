package edu.usc.softarch.arcade.clustering.criteria;

import edu.usc.softarch.arcade.clustering.techniques.ClusteringAlgoRunner;

public class StoppingCriterionFactory {
	public static StoppingCriterion makeStoppingCriterion(
			String stoppingCriterion, ClusteringAlgoRunner runner,
			int stoppingCriterionVal) {
		switch (stoppingCriterion) {
			case "clustergain":
				return new ClusterGainStoppingCriterion(runner);
			case "preselected":
				return new PreSelectedStoppingCriterion(stoppingCriterionVal, runner);
			default:
				throw new IllegalArgumentException(
					"Unknown stopping criterion " + stoppingCriterion);
		}
	}
}
