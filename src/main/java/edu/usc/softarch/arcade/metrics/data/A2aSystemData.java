package edu.usc.softarch.arcade.metrics.data;

import edu.usc.softarch.arcade.metrics.evolution.A2a;
import edu.usc.softarch.arcade.util.Version;

import java.io.File;
import java.io.IOException;
import java.util.List;

public final class A2aSystemData extends SystemData {
	//region CONSTRUCTORS
	public A2aSystemData(Version[] versions, List<File> archFiles)
			throws IOException {
		super(versions, archFiles);
	}

	public A2aSystemData(A2aSystemData toCopy) {
		super(toCopy); }

	public A2aSystemData(Version[] versions, double[][] a2a) {
		super(versions, a2a); }
	//endregion

	//region PROCESSING
	@SafeVarargs
	@Override
	protected final void compute(List<File>... files) throws IOException {
		for (int i = 0; i < this.versions.length - 1; i++) {
			super.metric[i] = new double[this.versions.length - 1 - i];

			for (int j = i + 1; j < this.versions.length; j++)
				super.metric[i][j - i - 1] =
					A2a.run(files[0].get(i), files[0].get(j));
		}
	}
	//endregion
}
