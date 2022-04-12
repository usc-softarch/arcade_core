package edu.usc.softarch.arcade.clustering.criteria;

import edu.usc.softarch.arcade.clustering.techniques.ClusteringAlgoRunner;

public class PreSelectedStoppingCriterion
				implements StoppingCriterion {
	private int numClusters;

	public PreSelectedStoppingCriterion(int numClusters) {
		this.numClusters = numClusters; }

	public boolean notReadyToStop() {
		return ClusteringAlgoRunner.fastClusters.size() != 1
						&& ClusteringAlgoRunner.fastClusters.size() != numClusters;
	}
}
