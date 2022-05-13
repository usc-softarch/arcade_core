package edu.usc.softarch.arcade.clustering.criteria;

import edu.usc.softarch.arcade.clustering.techniques.ClusteringAlgoRunner;

public class PreSelectedStoppingCriterion
		extends StoppingCriterion {
	private final int numClusters;
	private final ClusteringAlgoRunner runner;

	public PreSelectedStoppingCriterion(int numClusters, ClusteringAlgoRunner runner) {
		this.numClusters = numClusters;
		this.runner = runner;
	}

	public boolean notReadyToStop() {
		return runner.getArchitecture().size() != 1
						&& runner.getArchitecture().size() != numClusters;
	}
}
