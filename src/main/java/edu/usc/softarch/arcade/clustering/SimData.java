package edu.usc.softarch.arcade.clustering;

import java.util.Objects;

public class SimData implements Comparable<SimData> {
	public final Cluster c1;
	public final Cluster c2;
	public final Double cellValue;
	public final Integer clusterSize;
	public final String cellName;

	public SimData(Cluster c1, Cluster c2, Double cellValue,
			Integer clusterSize, String cellName) {
		this.c1 = c1;
		this.c2 = c2;
		this.cellValue = cellValue;
		this.clusterSize = clusterSize;
		this.cellName = cellName;
	}

	@Override
	public int compareTo(SimData o) {
		if (!this.cellValue.equals(o.cellValue))
			return this.cellValue.compareTo(o.cellValue);
		if (!this.clusterSize.equals(o.clusterSize))
			return this.clusterSize.compareTo(o.clusterSize);
		return this.cellName.compareTo(o.cellName);
	}

	//region OBJECT METHODS
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		SimData simData = (SimData) o;
		return Objects.equals(c1, simData.c1)
			&& Objects.equals(c2, simData.c2)
			&& Objects.equals(cellValue, simData.cellValue)
			&& Objects.equals(clusterSize, simData.clusterSize)
			&& Objects.equals(cellName, simData.cellName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(c1, c2, cellValue, clusterSize, cellName); }
	//endregion
}
