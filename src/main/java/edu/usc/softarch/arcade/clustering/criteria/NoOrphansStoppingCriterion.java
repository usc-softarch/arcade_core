package edu.usc.softarch.arcade.clustering.criteria;

import edu.usc.softarch.arcade.clustering.techniques.ConcernClusteringRunner;

public class NoOrphansStoppingCriterion
				extends StoppingCriterion {
	private final ConcernClusteringRunner parent;

	public NoOrphansStoppingCriterion(ConcernClusteringRunner parent) {
		this.parent = parent;	}

	public boolean notReadyToStop() {
		return parent.getArchitecture().hasOrphans(); }
}
