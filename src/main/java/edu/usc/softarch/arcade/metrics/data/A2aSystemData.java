package edu.usc.softarch.arcade.metrics.data;

import edu.usc.softarch.arcade.clustering.data.ReadOnlyArchitecture;
import edu.usc.softarch.arcade.metrics.evolution.A2a;
import edu.usc.softarch.arcade.util.McfpDriver;
import edu.usc.softarch.arcade.util.Version;
import edu.usc.softarch.util.Terminal;

import java.io.File;
import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public final class A2aSystemData extends SystemData {
	//region CONSTRUCTORS
	public A2aSystemData(Version[] versions, ExecutorService executor,
			McfpDriver[][] drivers, Vector<File> archFiles) throws IOException {
		super(versions, executor, drivers, archFiles);
	}

	public A2aSystemData(Version[] versions, ExecutorService executor,
			McfpDriver[][] drivers, ArchPair[][] architectures)
			throws IOException {
		super(versions, executor, drivers, architectures);
	}

	public A2aSystemData(A2aSystemData toCopy) {
		super(toCopy); }

	public A2aSystemData(Version[] versions, double[][] a2a) {
		super(versions, a2a); }

	public A2aSystemData(Version[] versions) {
		super(versions); }
	//endregion

	//region ACCESSORS
	public void addValue(ReadOnlyArchitecture ra1, ReadOnlyArchitecture ra2,
			int i, int j) {
		super.metric[i][j - i - 1] = A2a.run(ra1, ra2);
	}
	//endregion

	//region PROCESSING
	@SafeVarargs
	@Override
	protected final void compute(ExecutorService executor, McfpDriver[][] drivers,
			Vector<File>... files) throws IOException {
		AtomicInteger a2aCount = new AtomicInteger(1);
		int opCount = this.versions.length * (this.versions.length - 1) / 2;
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
					Terminal.timePrint(a2aCount.getAndIncrement() + "/"
						+ opCount + " a2a pairs computed.", Terminal.Level.INFO);
				});
			}
		}
	}

	@SafeVarargs
	@Override
	protected final void compute(ExecutorService executor, McfpDriver[][] drivers,
			ArchPair[][] architectures, Vector<File>... files) throws IOException {
		AtomicInteger a2aCount = new AtomicInteger(1);
		int opCount = this.versions.length * (this.versions.length - 1) / 2;
		for (int i = 0; i < this.versions.length - 1; i++) {
			super.metric[i] = new double[this.versions.length - 1 - i];

			for (int j = i + 1; j < this.versions.length; j++) {
				int finalI = i;
				int finalJ = j;
				executor.submit(() -> {
					super.metric[finalI][finalJ - finalI - 1] =
						A2a.run(architectures[finalI][finalJ - finalI - 1].v1,
							architectures[finalI][finalJ - finalI - 1].v2);
					Terminal.timePrint("Finished a2a: " + this.versions[finalI]
						+ "::" + this.versions[finalJ], Terminal.Level.DEBUG);
					Terminal.timePrint(a2aCount.getAndIncrement() + "/"
						+ opCount + " a2a pairs computed.", Terminal.Level.INFO);
				});
			}
		}
	}
	//endregion
}
