package edu.usc.softarch.arcade.clustering.techniques;

import edu.usc.softarch.arcade.clustering.Architecture;
import edu.usc.softarch.arcade.clustering.FeatureVectors;
import edu.usc.softarch.arcade.clustering.SimilarityMatrix;
import edu.usc.softarch.arcade.topics.DistributionSizeMismatchException;

/**
 * Enhanced version of ConcernClusteringRunner for testing.
 */
public class ConcernClusteringRunnerMock
		extends ConcernClusteringRunner {
	//region ATTRIBUTES
	private Architecture initialArchitecture;
	private Architecture architectureWithDocTopics;
	private SimilarityMatrix initialSimMatrix;
	//endregion

	//region CONSTRUCTORS
	ConcernClusteringRunnerMock(FeatureVectors vecs, String srcDir,
			String artifactsDir, String language) {
		super(vecs, srcDir, artifactsDir, language); }
	//endregion

	//region ACCESSORS
	public Architecture getInitialArchitecture() {
		return this.initialArchitecture; }

	public Architecture getArchitectureWithDocTopics() {
		return this.architectureWithDocTopics; }

	public SimilarityMatrix getInitialSimMatrix() {
		return this.initialSimMatrix; }
	//endregion

	//region OVERRIDES
	@Override
	protected void initializeClusters(String srcDir, String language) {
		super.initializeClusters(srcDir, language);
		this.initialArchitecture = new Architecture(super.architecture);
	}

	@Override
	protected void initializeClusterDocTopics(String artifactsDir) {
		super.initializeClusterDocTopics(artifactsDir);
		this.architectureWithDocTopics = new Architecture(super.architecture);
	}

	@Override
	protected SimilarityMatrix initializeSimMatrix(SimilarityMatrix.SimMeasure simMeasure)
			throws DistributionSizeMismatchException {
		this.initialSimMatrix = super.initializeSimMatrix(simMeasure);
		return new SimilarityMatrix(this.initialSimMatrix);
	}
	//endregion
}
