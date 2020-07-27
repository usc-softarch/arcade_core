package edu.usc.softarch.arcade.clustering;

public class MaxSimData {
	public Cluster fv1 = new Cluster();
	public Cluster fv2 = new Cluster();
	public FastCluster c1 = new FastCluster();
	public FastCluster c2 = new FastCluster();
	public double currentMaxSim = 0f;
	public int rowIndex;
	public int colIndex;
	public boolean foundMoreSimilarMeasure = true;
}