package edu.usc.softarch.arcade.clustering;

public class ClusterGainStoppingCriterion implements StoppingCriterion {
	public boolean notReadyToStop() {
		return ClusteringAlgoRunner.fastClusters.size() != 1
					&& ClusteringAlgoRunner.fastClusters.size() != ClusteringAlgoRunner.numClustersAtMaxClusterGain;
	}
}