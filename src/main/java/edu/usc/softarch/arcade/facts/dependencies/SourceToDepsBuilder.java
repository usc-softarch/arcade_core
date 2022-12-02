package edu.usc.softarch.arcade.facts.dependencies;

import java.io.IOException;

import edu.usc.softarch.arcade.clustering.data.FeatureVectors;
import edu.usc.softarch.arcade.facts.DependencyGraph;

public abstract class SourceToDepsBuilder {
	protected DependencyGraph dependencyGraph = new DependencyGraph();
	protected int numSourceEntities;
	protected FeatureVectors ffVecs;

	public abstract void build(String classesDirPath, String depsRsfFilename, String ffVecsFilename)
		throws IOException;
	public DependencyGraph getDependencyGraph() { return this.dependencyGraph; }
	public int getNumSourceEntities() { return numSourceEntities; }
	public FeatureVectors getFfVecs() { return this.ffVecs; }
}