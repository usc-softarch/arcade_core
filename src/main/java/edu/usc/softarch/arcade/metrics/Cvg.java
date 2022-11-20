package edu.usc.softarch.arcade.metrics;

import edu.usc.softarch.arcade.clustering.ReadOnlyArchitecture;
import edu.usc.softarch.arcade.clustering.data.ReadOnlyCluster;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

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

	public static double run(ReadOnlyArchitecture sourceArch,
			ReadOnlyArchitecture targetArch) {
		Cvg runner = new Cvg(sourceArch, targetArch);
		return runner.getCvgSourceToTarget();
	}
	//endregion

	//region ATTRIBUTES
	private final ReadOnlyArchitecture sourceArch;
	private final ReadOnlyArchitecture targetArch;
	private double cvgSourceToTarget;
	private double cvgTargetToSource;
	private final double lowerSimThreshold;
	private final double upperSimThreshold;
	//endregion

	//region CONSTRUCTORS
	public Cvg(ReadOnlyArchitecture sourceArch, ReadOnlyArchitecture targetArch) {
		this.sourceArch = sourceArch;
		this.targetArch = targetArch;
		this.cvgSourceToTarget = 0.0;
		this.cvgTargetToSource = 0.0;
		//TODO these two can be parameterized if necessary
		this.lowerSimThreshold = 0.66;
		this.upperSimThreshold = 1.0;
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
	//endregion

	//region PROCESSING
	private void compute() {
		Set<ReadOnlyCluster> sourceMatches = new HashSet<>();
		Set<ReadOnlyCluster> targetMatches = new HashSet<>();

		for (ReadOnlyCluster sourceCluster : this.sourceArch.values()) {
			for (ReadOnlyCluster targetCluster : this.targetArch.values()) {
				Set<String> intersection = sourceCluster.intersection(targetCluster);
				double sim = (double) intersection.size() /
					Math.max(sourceCluster.size(), targetCluster.size());
				if (sim > this.lowerSimThreshold && sim <= this.upperSimThreshold) {
					sourceMatches.add(sourceCluster);
					targetMatches.add(targetCluster);
				}
			}
		}

		this.cvgSourceToTarget =
			(double) sourceMatches.size() / this.sourceArch.size();
		this.cvgTargetToSource =
			(double) targetMatches.size() / this.targetArch.size();
	}
	//endregion
}
