package edu.usc.softarch.arcade.facts.driver;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.usc.softarch.arcade.clustering.FastFeatureVectors;

public abstract class SourceToDepsBuilder {
	protected Logger logger = LogManager.getLogger(SourceToDepsBuilder.class);
	protected Set<Pair<String,String>> edges = new LinkedHashSet<>();
	protected int numSourceEntities;
	protected FastFeatureVectors ffVecs;

	public abstract void build(String classesDirPath, String depsRsfFilename)
		throws IOException;
	public Set<Pair<String,String>> getEdges() { return this.edges; }
	public int getNumSourceEntities() { return numSourceEntities; }
	public FastFeatureVectors getFfVecs() { return this.ffVecs; }
}