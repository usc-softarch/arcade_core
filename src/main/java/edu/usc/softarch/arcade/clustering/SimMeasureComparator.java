package edu.usc.softarch.arcade.clustering;

import java.util.Comparator;

/**
 * @author joshua
 */
public abstract class SimMeasureComparator implements Comparator<Cluster> {
	private Cluster refCluster = null;

	public Cluster getRefCluster() { return refCluster; }

	public void setRefCluster(Cluster refCluster) {
		this.refCluster = refCluster; }
}
