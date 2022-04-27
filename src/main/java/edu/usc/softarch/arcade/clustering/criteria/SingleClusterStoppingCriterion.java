package edu.usc.softarch.arcade.clustering.criteria;

import edu.usc.softarch.arcade.clustering.techniques.ClusteringAlgoRunner;

public class SingleClusterStoppingCriterion
		implements StoppingCriterion {
	private ClusteringAlgoRunner runner;

	public SingleClusterStoppingCriterion(ClusteringAlgoRunner runner) {
		this.runner = runner;	}

	public boolean notReadyToStop() {
		return runner.architecture.size() != 1;	}
}
