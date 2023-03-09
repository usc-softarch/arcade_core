package edu.usc.softarch.arcade.metrics.data;

import edu.usc.softarch.arcade.clustering.data.ReadOnlyArchitecture;

class ArchPair {
	public final ReadOnlyArchitecture v1;
	public final ReadOnlyArchitecture v2;

	public ArchPair(ReadOnlyArchitecture v1, ReadOnlyArchitecture v2) {
		this.v1 = v1;
		this.v2 = v2;
	}
}
