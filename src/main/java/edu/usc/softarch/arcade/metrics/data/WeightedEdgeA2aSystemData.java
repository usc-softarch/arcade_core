package edu.usc.softarch.arcade.metrics.data;

import edu.usc.softarch.arcade.metrics.evolution.WeightedEdgeA2a;
import edu.usc.softarch.arcade.util.McfpDriver;
import edu.usc.softarch.arcade.util.Version;
import edu.usc.softarch.util.Terminal;

import java.io.File;
import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.ExecutorService;

public class WeightedEdgeA2aSystemData extends SystemData {
	//region ATTRIBUTES
	private double simThreshold;
	//endregion

	//region CONSTRUCTORS
	public WeightedEdgeA2aSystemData(Version[] versions, Vector<File> archFiles,
			Vector<File> depsFiles, double simThreshold, ExecutorService executor,
			McfpDriver[][] drivers) throws IOException {
		super(versions, executor, drivers, archFiles, depsFiles);
		this.simThreshold = simThreshold;
	}

	public WeightedEdgeA2aSystemData(WeightedEdgeA2aSystemData toCopy) {
		super(toCopy);
		this.simThreshold = toCopy.simThreshold;
	}

	public WeightedEdgeA2aSystemData(Version[] versions, double[][] edgeA2a) {
		super(versions, edgeA2a); }
	//endregion

	//region PROCESSING
	@SafeVarargs
	@Override
	protected final void compute(ExecutorService executor,
			McfpDriver[][] drivers, Vector<File>... files) throws IOException {
		for (int i = 0; i < this.versions.length - 1; i++) {
			super.metric[i] = new double[this.versions.length - 1 - i];

			for (int j = i + 1; j < this.versions.length; j++) {
				int finalI = i;
				int finalJ = j;
				executor.submit(() -> {
					try {
						super.metric[finalI][finalJ - finalI - 1] =
							WeightedEdgeA2a.run(files[0].get(finalI),
							files[0].get(finalJ), files[1].get(finalI).getAbsolutePath(),
							files[1].get(finalJ).getAbsolutePath(), this.simThreshold,
							drivers[finalI][finalJ - finalI - 1]);
					} catch (IOException e) {
						e.printStackTrace();
					}
					Terminal.timePrint("Finished WEa2a: " + this.versions[finalI]
							+ "::" + this.versions[finalJ], Terminal.Level.DEBUG);
				});
			}
		}
	}
	//endregion
}
