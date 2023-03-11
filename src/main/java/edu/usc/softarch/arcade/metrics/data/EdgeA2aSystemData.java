package edu.usc.softarch.arcade.metrics.data;

import edu.usc.softarch.arcade.clustering.data.ReadOnlyArchitecture;
import edu.usc.softarch.arcade.metrics.evolution.EdgeA2a;
import edu.usc.softarch.arcade.util.McfpDriver;
import edu.usc.softarch.arcade.util.Version;
import edu.usc.softarch.util.Terminal;

import java.io.File;
import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public final class EdgeA2aSystemData extends SystemData {
	//region ATTRIBUTES
	private double simThreshold;
	//endregion

	//region CONSTRUCTORS
	public EdgeA2aSystemData(Version[] versions, Vector<File> archFiles,
			Vector<File> depsFiles, double simThreshold, ExecutorService executor,
			McfpDriver[][] drivers) throws IOException {
		super(versions, executor, drivers, archFiles, depsFiles);
		this.simThreshold = simThreshold;
	}

	public EdgeA2aSystemData(Version[] versions, ArchPair[][] architectures,
			Vector<File> depsFiles, double simThreshold, ExecutorService executor,
			McfpDriver[][] drivers) throws IOException {
		super(versions, executor, drivers, architectures, depsFiles);
		this.simThreshold = simThreshold;
	}

	public EdgeA2aSystemData(Version[] versions, double simThreshold) {
		super(versions);
		this.simThreshold = simThreshold;
	}

	public EdgeA2aSystemData(EdgeA2aSystemData toCopy) {
		super(toCopy);
		this.simThreshold = toCopy.simThreshold;
	}

	public EdgeA2aSystemData(Version[] versions, double[][] edgeA2a) {
		super(versions, edgeA2a); }
	//endregion

	//region ACCESSORS
	public void addValue(ReadOnlyArchitecture ra1, ReadOnlyArchitecture ra2,
			File deps1, File deps2, McfpDriver driver, int i, int j)
			throws IOException {
		super.metric[i][j - i - 1] = EdgeA2a.run(ra1, ra2, deps1.getAbsolutePath(),
			deps2.getAbsolutePath(), this.simThreshold, driver);
	}
	//endregion

	//region PROCESSING
	@SafeVarargs
	@Override
	protected final void compute(ExecutorService executor,
			McfpDriver[][] drivers, Vector<File>... files) throws IOException {
		AtomicInteger edgeA2aCount = new AtomicInteger(1);
		int opCount = this.versions.length * (this.versions.length - 1) / 2;
		for (int i = 0; i < this.versions.length - 1; i++) {
			super.metric[i] = new double[this.versions.length - 1 - i];

			for (int j = i + 1; j < this.versions.length; j++) {
				int finalI = i;
				int finalJ = j;
				executor.submit(() -> {
					try {
						super.metric[finalI][finalJ - finalI - 1] =
							EdgeA2a.run(files[0].get(finalI),
							files[0].get(finalJ), files[1].get(finalI).getAbsolutePath(),
							files[1].get(finalJ).getAbsolutePath(), this.simThreshold,
							drivers[finalI][finalJ - finalI - 1]);
					} catch (IOException e) {
						e.printStackTrace();
					}
					Terminal.timePrint("Finished Edgea2a: " + this.versions[finalI]
						+ "::" + this.versions[finalJ], Terminal.Level.DEBUG);
					Terminal.timePrint(edgeA2aCount.getAndIncrement() + "/"
						+ opCount + " edgea2a " + this.simThreshold + " pairs computed.",
						Terminal.Level.INFO);
				});
			}
		}
	}

	@SafeVarargs
	@Override
	protected final void compute(ExecutorService executor,
			McfpDriver[][] drivers, ArchPair[][] architectures, Vector<File>... files)
			throws IOException {
		AtomicInteger edgeA2aCount = new AtomicInteger(1);
		int opCount = this.versions.length * (this.versions.length - 1) / 2;
		for (int i = 0; i < this.versions.length - 1; i++) {
			super.metric[i] = new double[this.versions.length - 1 - i];

			for (int j = i + 1; j < this.versions.length; j++) {
				int finalI = i;
				int finalJ = j;
				executor.submit(() -> {
					try {
						super.metric[finalI][finalJ - finalI - 1] =
							EdgeA2a.run(architectures[finalI][finalJ - finalI - 1].v1,
								architectures[finalI][finalJ - finalI - 1].v2,
								files[0].get(finalI).getAbsolutePath(),
								files[0].get(finalJ).getAbsolutePath(), this.simThreshold,
								drivers[finalI][finalJ - finalI - 1]);
					} catch (IOException e) {
						e.printStackTrace();
					}
					Terminal.timePrint("Finished Edgea2a: " + this.versions[finalI]
						+ "::" + this.versions[finalJ], Terminal.Level.DEBUG);
					Terminal.timePrint(edgeA2aCount.getAndIncrement() + "/"
						+ opCount + " edgea2a " + this.simThreshold + " pairs computed.",
						Terminal.Level.INFO);
				});
			}
		}
	}
	//endregion
}
