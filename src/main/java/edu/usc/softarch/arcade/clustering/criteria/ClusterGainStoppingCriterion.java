package edu.usc.softarch.arcade.clustering.criteria;

import edu.usc.softarch.arcade.clustering.techniques.ClusteringAlgoRunner;

public class ClusterGainStoppingCriterion
		extends StoppingCriterion {
	private final ClusteringAlgoRunner runner;

	public ClusterGainStoppingCriterion(ClusteringAlgoRunner runner) {
		this.runner = runner;	}

	public boolean notReadyToStop() {
		return runner.architecture.size() != 1
			&& runner.architecture.size() != ClusteringAlgoRunner.numClustersAtMaxClusterGain;
	}
}