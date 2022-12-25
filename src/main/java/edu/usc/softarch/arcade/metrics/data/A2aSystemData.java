package edu.usc.softarch.arcade.metrics.data;

import edu.usc.softarch.arcade.metrics.SystemEvo;
import edu.usc.softarch.arcade.util.Version;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class A2aSystemData {
	//region ATTRIBUTES
	final Version[] versions;
	final double[][] a2a;
	//endregion

	//region CONSTRUCTORS
	public A2aSystemData(Version[] versions, List<File> archFiles)
			throws IOException {
		this.versions = versions;
		this.a2a = new double[this.versions.length - 1][];
		for (int i = 0; i < this.versions.length - 1; i++) {
			this.a2a[i] = new double[this.versions.length - 1 - i];

			for (int j = i + 1; j < this.versions.length; j++)
				this.a2a[i][j - i - 1] =
					SystemEvo.run(archFiles.get(i), archFiles.get(j));
		}
	}

	public A2aSystemData(A2aSystemData toCopy) {
		this.versions = Arrays.copyOf(toCopy.versions, toCopy.versions.length);
		this.a2a = Arrays.stream(toCopy.a2a).map(double[]::clone)
			.toArray(v -> toCopy.a2a.clone());
	}

	public A2aSystemData(Version[] versions, double[][] a2a) {
		this.versions = versions;
		this.a2a = a2a;
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
						writer.write(Double.toString(this.a2a[j][i - j - 1]));
					else if (i < j)
						writer.write(Double.toString(this.a2a[i][j - i - 1]));
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
				versionValues.add(this.a2a[currentIndex][i - currentIndex - 1]);
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
				versionValues.add(this.a2a[currentIndex][i - currentIndex - 1]);
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
