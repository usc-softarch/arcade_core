package edu.usc.softarch.arcade.clustering;

import java.io.Serializable;
import java.util.Comparator;

/**
 * @author joshua
 */
public abstract class SimMeasureComparator
		implements Comparator<Cluster>, Serializable {
	private static final long serialVersionUID = 1L;

	private Cluster refCluster = null;

	public Cluster getRefCluster() { return refCluster; }

	public void setRefCluster(Cluster refCluster) {
		this.refCluster = refCluster; }
}
