package edu.usc.softarch.arcade.facts.driver;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import edu.usc.softarch.arcade.clustering.FastFeatureVectors;

public interface SourceToDepsBuilder {
	public Set<Pair<String,String>> edges = new LinkedHashSet<Pair<String,String>>();
	public static FastFeatureVectors ffVecs = null;
	public static int numSourceEntities = 0;
	public abstract void build(String[] args) throws IOException, FileNotFoundException;
	
	public Set<Pair<String,String>> getEdges();

	public int getNumSourceEntities();

	public abstract FastFeatureVectors getFfVecs();
}
