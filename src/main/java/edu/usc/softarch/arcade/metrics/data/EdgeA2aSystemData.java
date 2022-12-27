package edu.usc.softarch.arcade.metrics.data;

import edu.usc.softarch.arcade.metrics.evolution.EdgeA2a;
import edu.usc.softarch.arcade.util.Version;

import java.io.File;
import java.io.IOException;
import java.util.List;

public final class EdgeA2aSystemData extends SystemData {
	//region CONSTRUCTORS
	public EdgeA2aSystemData(Version[] versions, List<File> archFiles,
			List<File> depsFiles) throws IOException {
		super(versions, archFiles, depsFiles);
	}

	public EdgeA2aSystemData(EdgeA2aSystemData toCopy) {
		super(toCopy); }

	public EdgeA2aSystemData(Version[] versions, double[][] edgeA2a) {
		super(versions, edgeA2a); }
	//endregion

	//region PROCESSING
	@SafeVarargs
	@Override
	protected final void compute(List<File>... files) throws IOException {
		for (int i = 0; i < this.versions.length - 1; i++) {
			super.metric[i] = new double[this.versions.length - 1 - i];

			for (int j = i + 1; j < this.versions.length; j++)
				super.metric[i][j - i - 1] = EdgeA2a.run(files[0].get(i),
					files[0].get(j), files[1].get(i).getAbsolutePath(),
					files[1].get(j).getAbsolutePath());
		}
	}
	//endregion
}
