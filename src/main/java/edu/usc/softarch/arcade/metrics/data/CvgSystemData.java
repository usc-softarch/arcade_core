package edu.usc.softarch.arcade.metrics.data;

import edu.usc.softarch.arcade.metrics.evolution.Cvg;
import edu.usc.softarch.arcade.util.Version;
import edu.usc.softarch.util.Terminal;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;

public class CvgSystemData {
	//region ATTRIBUTES
	final Version[] versions;
	final double[][] cvgForwards;
	final double[][] cvgBackwards;
	//endregion

	//region CONSTRUCTORS
	public CvgSystemData(Version[] versions, ExecutorService executor,
			Vector<File> archFiles) {
		this.versions = versions;
		this.cvgForwards = new double[this.versions.length - 1][];
		this.cvgBackwards = new double[this.versions.length - 1][];
		for (int i = 0; i < this.versions.length - 1; i++) {
			this.cvgForwards[i] = new double[this.versions.length - 1 - i];
			this.cvgBackwards[i] = new double[this.versions.length - 1 - i];

			for (int j = i + 1; j < this.versions.length; j++) {
				int finalI = i;
				int finalJ = j;
				executor.submit(() -> {
					try {
						this.cvgForwards[finalI][finalJ - finalI - 1] =
							Cvg.run(archFiles.get(finalI), archFiles.get(finalJ));
						this.cvgBackwards[finalI][finalJ - finalI - 1] =
							Cvg.run(archFiles.get(finalJ), archFiles.get(finalI));
					} catch (IOException e) {
						e.printStackTrace();
					}
					Terminal.timePrint("Finished cvg: " + this.versions[finalI]
						+ "::" + this.versions[finalJ], Terminal.Level.DEBUG);
				});
			}
		}
	}

	public CvgSystemData(CvgSystemData toCopy) {
		this.versions = Arrays.copyOf(toCopy.versions, toCopy.versions.length);
		this.cvgForwards = Arrays.stream(toCopy.cvgForwards).map(double[]::clone)
			.toArray(v -> toCopy.cvgForwards.clone());
		this.cvgBackwards = Arrays.stream(toCopy.cvgBackwards).map(double[]::clone)
			.toArray(v -> toCopy.cvgBackwards.clone());
	}

	public CvgSystemData(Version[] versions, double[][] cvgForwards,
			double[][] cvgBackwards) {
		this.versions = versions;
		this.cvgForwards = cvgForwards;
		this.cvgBackwards = cvgBackwards;
	}
	//endregion

	//region SERIALIZATION
	void writeFullCsv(String path) throws IOException {
		try (FileWriter writer = new FileWriter(path)) {
			writer.write("version");
			for (Version version : this.versions)
				writer.write("," + version);
			writer.write(System.lineSeparator());

			for (int i = 0; i < this.versions.length; i++) {
				writer.write(this.versions[i].toString());
				for (int j = 0; j < this.versions.length; j++) {
					writer.write(",");
					if (i > j)
						writer.write(Double.toString(this.cvgBackwards[j][i - j - 1]));
					else if (i < j)
						writer.write(Double.toString(this.cvgForwards[i][j - i - 1]));
				}
				writer.write(System.lineSeparator());
			}
		}
	}

	void writeSubCsv(String path, Version.IncrementType incrementType)
			throws IOException {
		List<String> subVersions = new ArrayList<>();
		List<Double> forwardsVersionValues = new ArrayList<>();
		List<Double> backwardsVersionValues = new ArrayList<>();

		Version currentVersion = this.versions[0];
		int currentIndex = 0;
		for (int i = 1; i < this.versions.length; i++) {
			Version.IncrementType increment =
				currentVersion.getIncrementType(this.versions[i]);
			if (increment.getValue() > incrementType.getValue())
				continue;

			Version iVersion = this.versions[i];

			if (increment == incrementType) {
				subVersions.add(currentVersion + " -> " + iVersion);
				forwardsVersionValues.add(
					this.cvgForwards[currentIndex][i - currentIndex - 1]);
				backwardsVersionValues.add(
					this.cvgBackwards[currentIndex][i - currentIndex - 1]);
			}

			currentVersion = iVersion;
			currentIndex = i;
		}

		writeSubCsv(path, subVersions,
			forwardsVersionValues, backwardsVersionValues);
	}

	void writeMinMajorCsv(String path) throws IOException {
		List<String> subVersions = new ArrayList<>();
		List<Double> forwardsVersionValues = new ArrayList<>();
		List<Double> backwardsVersionValues = new ArrayList<>();

		Version currentVersion = this.versions[0];
		int currentIndex = 0;
		for (int i = 1; i < this.versions.length; i++) {
			Version.IncrementType increment =
				currentVersion.getIncrementType(this.versions[i]);
			if (increment.getValue() > Version.IncrementType.MINOR.getValue())
				continue;

			Version iVersion = this.versions[i];

			if (increment == Version.IncrementType.MAJOR) {
				subVersions.add(currentVersion + " -> " + iVersion);
				forwardsVersionValues.add(
					this.cvgForwards[currentIndex][i - currentIndex - 1]);
				backwardsVersionValues.add(
					this.cvgBackwards[currentIndex][i - currentIndex - 1]);
			}

			currentVersion = iVersion;
			currentIndex = i;
		}

		writeSubCsv(path, subVersions,
			forwardsVersionValues, backwardsVersionValues);
	}

	private void writeSubCsv(String path, List<String> subVersions,
			List<Double> forwardsVersionValues, List<Double> backwardsVersionValues)
			throws IOException {
		try (FileWriter writer = new FileWriter(path)) {
			writer.write("versionPair,forwardsValue,backwardsValue");
			writer.write(System.lineSeparator());

			for (int i = 0; i < forwardsVersionValues.size(); i++) {
				writer.write(subVersions.get(i) + "," + forwardsVersionValues.get(i)
					+ "," + backwardsVersionValues.get(i));
				writer.write(System.lineSeparator());
			}
		}
	}
	//endregion
}
