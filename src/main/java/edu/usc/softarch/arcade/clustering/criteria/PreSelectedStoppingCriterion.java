package edu.usc.softarch.arcade.clustering.criteria;

import edu.usc.softarch.arcade.clustering.techniques.ClusteringAlgoRunner;

public class PreSelectedStoppingCriterion
		implements StoppingCriterion {
	private int numClusters;
	private ClusteringAlgoRunner runner;

	public PreSelectedStoppingCriterion(int numClusters, ClusteringAlgoRunner runner) {
		this.numClusters = numClusters;
		this.runner = runner;
	}

	public boolean notReadyToStop() {
		return runner.architecture.size() != 1
						&& runner.architecture.size() != numClusters;
	}
}