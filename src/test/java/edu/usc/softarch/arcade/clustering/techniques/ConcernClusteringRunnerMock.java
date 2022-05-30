package edu.usc.softarch.arcade.clustering.techniques;

import edu.usc.softarch.arcade.clustering.ConcernArchitecture;
import edu.usc.softarch.arcade.clustering.simmeasures.SimMeasure;
import edu.usc.softarch.arcade.clustering.simmeasures.SimilarityMatrix;
import edu.usc.softarch.arcade.clustering.criteria.SerializationCriterion;
import edu.usc.softarch.arcade.topics.DistributionSizeMismatchException;

/**
 * Enhanced version of ConcernClusteringRunner for testing.
 */
public class ConcernClusteringRunnerMock
		extends ConcernClusteringRunner {
	//region ATTRIBUTES
	private SimilarityMatrix initialSimMatrix;
	//endregion

	//region CONSTRUCTORS
	ConcernClusteringRunnerMock(String language,
			SerializationCriterion serializationCriterion, ConcernArchitecture concernArch) {
		super(language, serializationCriterion, concernArch);
	}
	//endregion

	//region ACCESSORS
	public SimilarityMatrix getInitialSimMatrix() {
		return this.initialSimMatrix; }
	//endregion

	//region OVERRIDES
	@Override
	protected SimilarityMatrix initializeSimMatrix(
			SimMeasure.SimMeasureType simMeasure)
			throws DistributionSizeMismatchException {
		this.initialSimMatrix = super.initializeSimMatrix(simMeasure);
		return new SimilarityMatrix(this.initialSimMatrix);
	}
	//endregion
}
