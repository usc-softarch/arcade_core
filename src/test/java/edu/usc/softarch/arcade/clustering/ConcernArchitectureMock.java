package edu.usc.softarch.arcade.clustering;

public class ConcernArchitectureMock extends ConcernArchitecture {
	//region ATTRIBUTES
	public Architecture initialArchitecture;
	public Architecture architectureWithDocTopics;
	//endregion

	//region CONSTRUCTORS
	ConcernArchitectureMock(String projectName, String projectPath,
			FeatureVectors vectors, String language, String artifactsDir,
			String packagePrefix) {
		super(projectName, projectPath, vectors, language,
			artifactsDir, packagePrefix);
	}

	@Override
	protected void initializeClusters(FeatureVectors vectors, String language,
			String packagePrefix) {
		super.initializeClusters(vectors, language, packagePrefix);
		this.initialArchitecture = new Architecture(this);
	}

	@Override
	protected void initializeClusterDocTopics() {
		super.initializeClusterDocTopics();
		this.architectureWithDocTopics = new Architecture(this);
	}
	//endregion
}
