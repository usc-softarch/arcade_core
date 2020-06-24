package edu.usc.softarch.arcade.clustering;

import edu.usc.softarch.arcade.config.Config;

public class SingleClusterStoppingCriterion implements StoppingCriterion {
	public boolean notReadyToStop() {
		return ClusteringAlgoRunner.fastClusters.size() != 1;
	}
}