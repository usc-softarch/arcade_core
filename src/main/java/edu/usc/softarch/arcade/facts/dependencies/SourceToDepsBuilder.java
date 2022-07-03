package edu.usc.softarch.arcade.facts.dependencies;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import edu.usc.softarch.arcade.clustering.FeatureVectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class SourceToDepsBuilder {
	protected Logger logger = LogManager.getLogger(SourceToDepsBuilder.class);
	protected Set<Map.Entry<String,String>> edges = new LinkedHashSet<>();
	protected int numSourceEntities;
	protected FeatureVectors ffVecs;

	public abstract void build(String classesDirPath, String depsRsfFilename, String ffVecsFilename)
		throws IOException;
	public Set<Map.Entry<String,String>> getEdges() { return this.edges; }
	public int getNumSourceEntities() { return numSourceEntities; }
	public FeatureVectors getFfVecs() { return this.ffVecs; }
}