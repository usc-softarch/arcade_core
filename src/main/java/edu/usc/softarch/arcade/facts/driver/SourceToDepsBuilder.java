package edu.usc.softarch.arcade.facts.driver;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import edu.usc.softarch.arcade.clustering.FastFeatureVectors;

public abstract class SourceToDepsBuilder {
	protected Set<Pair<String,String>> edges = new LinkedHashSet<>();

	public abstract void build(String classesDirPath, String depsRsfFilename)
		throws IOException;
	public abstract Set<Pair<String,String>> getEdges();
	public abstract int getNumSourceEntities();
	public abstract FastFeatureVectors getFfVecs();
}
