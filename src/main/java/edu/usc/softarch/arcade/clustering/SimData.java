package edu.usc.softarch.arcade.clustering;

public class SimData implements Comparable<SimData> {
	public final Cluster c1;
	public final Cluster c2;
	public final Double cellValue;
	public final Integer clusterSize;
	public final Integer clusterAge;

	public SimData(Cluster c1, Cluster c2, Double cellValue,
			Integer clusterSize, Integer clusterAge) {
		this.c1 = c1;
		this.c2 = c2;
		this.cellValue = cellValue;
		this.clusterSize = clusterSize;
		this.clusterAge = clusterAge;
	}

	@Override
	public int compareTo(SimData o) {
		if (!this.cellValue.equals(o.cellValue))
			return this.cellValue.compareTo(o.cellValue);
		if (!this.clusterSize.equals(o.clusterSize))
			return this.clusterSize.compareTo(o.clusterSize);
		return this.clusterAge.compareTo(o.clusterAge);
	}
}
