package edu.usc.softarch.arcade.config;

//TODO This class is an abomination and must be destroyed.
/**
 * @author joshua
 */
public class Config {
	public enum Granule { func, file, clazz	}

	private static Granule clusteringGranule = Granule.file;
	public static Granule getClusteringGranule() { return clusteringGranule; }
}