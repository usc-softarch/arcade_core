package edu.usc.softarch.arcade.config;

import java.util.ArrayList;
import java.util.List;

import edu.usc.softarch.arcade.clustering.ClusteringAlgorithmType;

//TODO This class is an abomination and must be destroyed.
/**
 * @author joshua
 */
public class Config {
	public enum Granule { func, file, clazz	}
	
	/* Clustering configuration data */
	//TODO Pretty sure this is also dead
	private static ClusteringAlgorithmType currentClusteringAlgorithm = 
		ClusteringAlgorithmType.WCA;
	public static ClusteringAlgorithmType getCurrentClusteringAlgorithm() {
		return currentClusteringAlgorithm; }
	
	/* Concern properties data */
	private static int numTopics = 10;
	private static List<Integer> numTopicsList = new ArrayList<>();

	public static List<Integer> getNumTopicsList() { return numTopicsList; }

	public static int getNumTopics() { return numTopics; }
	public static void setNumTopics(int numTopics) {
		Config.numTopics = numTopics;	}

	/* DriverEngine options */
	private static int startNumClustersRange;
	private static int endNumClustersRange;
	private static int rangeNumClustersStep;

	private static int startNumTopicsRange;
	public static int getStartNumTopicsRange() { return startNumTopicsRange; }
	public static int getEndNumTopicsRange() { return endNumTopicsRange; }
	public static int getRangeNumTopicsStep() {	return rangeNumTopicsStep; }

	private static int endNumTopicsRange;
	private static int rangeNumTopicsStep;
	private static Granule clusteringGranule = Granule.file;
	public static Granule getClusteringGranule() { return clusteringGranule; }
	public static int getStartNumClustersRange() { return startNumClustersRange; }
	public static int getEndNumClustersRange() { return endNumClustersRange; }
	public static int getRangeNumClustersStep() { return rangeNumClustersStep; }
}