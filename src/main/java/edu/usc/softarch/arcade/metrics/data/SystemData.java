package edu.usc.softarch.arcade.metrics.data;

import edu.usc.softarch.arcade.util.McfpDriver;
import edu.usc.softarch.arcade.util.Version;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

public abstract class SystemData {
	//region ATTRIBUTES
	final Version[] versions;
	final double[][] metric;
	//endregion

	//region CONSTRUCTORS
	protected SystemData(Version[] versions, ExecutorService executor,
			McfpDriver[][] drivers, List<File>... files) throws IOException {
		this.versions = versions;
		this.metric = new double[this.versions.length - 1][];
		compute(executor, drivers, files);
	}

	protected SystemData(SystemData toCopy) {
		this.versions = Arrays.copyOf(toCopy.versions, toCopy.versions.length);
		this.metric = Arrays.stream(toCopy.metric).map(double[]::clone)
			.toArray(v -> toCopy.metric.clone());
	}

	protected SystemData(Version[] versions, double[][] metric) {
		this.versions = versions;
		this.metric = metric;
	}
	//endregion

	//region PROCESSING
	protected abstract void compute(ExecutorService executor,
		McfpDriver[][] drivers, List<File>... files) throws IOException;
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
						writer.write(Double.toString(this.metric[j][i - j - 1]));
					else if (i < j)
						writer.write(Double.toString(this.metric[i][j - i - 1]));
				}
				writer.write(System.lineSeparator());
			}
		}
	}

	void writeSubCsv(String path, Version.IncrementType incrementType)
		throws IOException {
		List<String> subVersions = new ArrayList<>();
		List<Double> versionValues = new ArrayList<>();

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
				versionValues.add(this.metric[currentIndex][i - currentIndex - 1]);
			}

			currentVersion = iVersion;
			currentIndex = i;
		}

		writeSubCsv(path, subVersions, versionValues);
	}

	void writeMinMajorCsv(String path) throws IOException {
		List<String> subVersions = new ArrayList<>();
		List<Double> versionValues = new ArrayList<>();

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
				versionValues.add(this.metric[currentIndex][i - currentIndex - 1]);
			}

			currentVersion = iVersion;
			currentIndex = i;
		}

		writeSubCsv(path, subVersions, versionValues);
	}

	private void writeSubCsv(String path, List<String> subVersions,
		List<Double> versionValues) throws IOException {
		try (FileWriter writer = new FileWriter(path)) {
			writer.write("versionPair,value");
			writer.write(System.lineSeparator());

			for (int i = 0; i < versionValues.size(); i++) {
				writer.write(subVersions.get(i) + "," + versionValues.get(i));
				writer.write(System.lineSeparator());
			}
		}
	}
	//endregion
}
