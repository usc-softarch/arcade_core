package edu.usc.softarch.arcade.clustering.criteria.stopping;

import edu.usc.softarch.arcade.clustering.Architecture;
import edu.usc.softarch.arcade.clustering.criteria.StoppingCriterion;

public class NoOrphansStoppingCriterion
				extends StoppingCriterion {
	public boolean notReadyToStop(Architecture arch) {
		return arch.hasOrphans(); }
}
