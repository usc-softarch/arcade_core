package edu.usc.softarch.arcade.clustering;

public class SingleClusterStoppingCriterion implements StoppingCriterion {
	public boolean notReadyToStop() {
		return ClusteringAlgoRunner.fastClusters.size() != 1;
	}
}