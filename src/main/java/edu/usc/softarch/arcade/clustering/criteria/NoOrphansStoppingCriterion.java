package edu.usc.softarch.arcade.clustering.criteria;

import edu.usc.softarch.arcade.clustering.Architecture;

public class NoOrphansStoppingCriterion
				extends StoppingCriterion {
	public boolean notReadyToStop(Architecture arch) {
		return arch.hasOrphans(); }
}
