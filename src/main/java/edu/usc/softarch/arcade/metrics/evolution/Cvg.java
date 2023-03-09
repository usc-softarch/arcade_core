package edu.usc.softarch.arcade.metrics.evolution;

import edu.usc.softarch.arcade.clustering.data.ReadOnlyArchitecture;
import edu.usc.softarch.arcade.clustering.data.ReadOnlyCluster;
import edu.usc.softarch.arcade.metrics.RenameFixer;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class Cvg {
	//region PUBLIC INTERFACE
	public static void main(String[] args) throws IOException {
		System.out.println("Coverage from " + args[0] + " to " + args[1]
			+ " is " + run(args[0], args[1]));
		System.out.println("Coverage from " + args[1] + " to " + args[0]
			+ " is " + run(args[1], args[0]));
	}

	public static double run(String sourceArchPath, String targetArchPath)
			throws IOException {
		return run(ReadOnlyArchitecture.readFromRsf(sourceArchPath),
			ReadOnlyArchitecture.readFromRsf(targetArchPath));
	}

	public static double run(File sourceArchFile, File targetArchFile)
			throws IOException {
		return run(ReadOnlyArchitecture.readFromRsf(sourceArchFile),
			ReadOnlyArchitecture.readFromRsf(targetArchFile));
	}

	public static double run(ReadOnlyArchitecture sourceArch,
			ReadOnlyArchitecture targetArch) {
		Cvg runner = new Cvg(sourceArch, targetArch);
		return runner.getCvgSourceToTarget();
	}
	//endregion

	//region ATTRIBUTES
	private final ReadOnlyArchitecture sourceArch;
	private final ReadOnlyArchitecture targetArch;
	private final Set<ReadOnlyCluster> sourceMatches;
	private final Set<ReadOnlyCluster> targetMatches;
	private double cvgSourceToTarget;
	private double cvgTargetToSource;
	private final double lowerSimThreshold;
	private final double upperSimThreshold;
	//endregion

	//region CONSTRUCTORS
	public Cvg(ReadOnlyArchitecture sourceArch, ReadOnlyArchitecture targetArch) {
		this(sourceArch, targetArch, 0.66, 1.0);
	}

	public Cvg(ReadOnlyArchitecture sourceArch, ReadOnlyArchitecture targetArch,
			double lowerSimThreshold, double upperSimThreshold) {
		this.sourceArch = sourceArch;
		this.targetArch = targetArch;
		try {
			RenameFixer.fix(this.sourceArch, this.targetArch);
		} catch (ExecutionException | InterruptedException e) {
			throw new RuntimeException(e); //TODO handle it
		}
		this.sourceMatches = new HashSet<>();
		this.targetMatches = new HashSet<>();
		this.cvgSourceToTarget = 0.0;
		this.cvgTargetToSource = 0.0;
		this.lowerSimThreshold = lowerSimThreshold;
		this.upperSimThreshold = upperSimThreshold;
	}
	//endregion

	//region ACCESSORS
	public double getCvgSourceToTarget() {
		if (this.cvgSourceToTarget == 0.0) compute();
		return this.cvgSourceToTarget;
	}

	public double getCvgTargetToSource() {
		if (this.cvgTargetToSource == 0.0) compute();
		return this.cvgTargetToSource;
	}

	public Set<ReadOnlyCluster> getSourceMatches() {
		if (this.cvgTargetToSource == 0.0) compute();
		return new HashSet<>(this.sourceMatches);
	}

	public Set<ReadOnlyCluster> getTargetMatches() {
		if (this.cvgTargetToSource == 0.0) compute();
		return new HashSet<>(this.targetMatches);
	}
	//endregion

	//region PROCESSING
	private void compute() {
		for (ReadOnlyCluster sourceCluster : this.sourceArch.values()) {
			for (ReadOnlyCluster targetCluster : this.targetArch.values()) {
				int intersectionSize = sourceCluster.intersectionSize(targetCluster);
				double sim = (double) intersectionSize /
					Math.max(sourceCluster.size(), targetCluster.size());
				if (sim > this.lowerSimThreshold && sim <= this.upperSimThreshold) {
					this.sourceMatches.add(sourceCluster);
					this.targetMatches.add(targetCluster);
				}
			}
		}

		this.cvgSourceToTarget =
			(double) this.sourceMatches.size() / this.sourceArch.size();
		this.cvgTargetToSource =
			(double) this.targetMatches.size() / this.targetArch.size();
	}
	//endregion
}
