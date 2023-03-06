package edu.usc.softarch.arcade.metrics.data;

import edu.usc.softarch.arcade.metrics.evolution.A2a;
import edu.usc.softarch.arcade.util.McfpDriver;
import edu.usc.softarch.arcade.util.Version;
import edu.usc.softarch.util.Terminal;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;

public final class A2aSystemData extends SystemData {
	//region CONSTRUCTORS
	public A2aSystemData(Version[] versions, ExecutorService executor,
			McfpDriver[][] drivers, List<File> archFiles) throws IOException {
		super(versions, executor, drivers, archFiles);
	}

	public A2aSystemData(A2aSystemData toCopy) {
		super(toCopy); }

	public A2aSystemData(Version[] versions, double[][] a2a) {
		super(versions, a2a); }
	//endregion

	//region PROCESSING
	@SafeVarargs
	@Override
	protected final void compute(ExecutorService executor, McfpDriver[][] drivers,
			List<File>... files) throws IOException {
		for (int i = 0; i < this.versions.length - 1; i++) {
			super.metric[i] = new double[this.versions.length - 1 - i];

			for (int j = i + 1; j < this.versions.length; j++) {
				int finalI = i;
				int finalJ = j;
				executor.submit(() -> {
					try {
						super.metric[finalI][finalJ - finalI - 1] =
							A2a.run(files[0].get(finalI), files[0].get(finalJ));
					} catch (IOException e) {
						e.printStackTrace();
					}
					Terminal.timePrint("Finished a2a: " + this.versions[finalI]
						+ "::" + this.versions[finalJ], Terminal.Level.DEBUG);
				});
			}
		}
	}
	//endregion
}
