package edu.usc.softarch.arcade.metrics;

import edu.usc.softarch.arcade.util.CLI;
import edu.usc.softarch.arcade.util.FileUtil;
import edu.usc.softarch.util.json.EnhancedJsonGenerator;
import edu.usc.softarch.util.json.EnhancedJsonParser;
import edu.usc.softarch.util.json.JsonSerializable;
import mojo.MoJoCalculator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SystemMetrics implements JsonSerializable {
	//region PUBLIC INTERFACE
	/**
	 * systemDirPath = Path to where ARCADE put the version directories as output.
	 * depsDirPath   = Path to where all the deps.rsf files are located.
	 * outputPath    = Path to place the output file in.
	 */
	public static void main(String[] args) throws IOException {
		Map<String, String> parsedArgs = CLI.parseArguments(args);
		run(parsedArgs.get("systemdirpath"),
			parsedArgs.get("depsdirpath"), parsedArgs.get("outputpath"));
	}

	public static void run(String systemDirPath, String depsDirPath,
			String outputPath) throws IOException {
		SystemMetrics metrics = new SystemMetrics(systemDirPath, depsDirPath);
		metrics.serialize(outputPath);
	}
	//endregion

	//region ATTRIBUTES
	private final Collection<ArchitectureMetrics> versionMetrics;

	private final String[] versions;
	private final double[][] a2a;
	private final double[][] cvgForwards;
	private final double[][] cvgBackwards;
	private final double[][] mojoFm;
	//endregion

	//region CONSTRUCTORS
	public SystemMetrics(String systemDirPath, String depsDirPath)
			throws IOException {
		// Get and sort the architecture files
		List<File> archFiles = new ArrayList<>();
		List<File> archDirs = List.of(
			new File(FileUtil.tildeExpandPath(systemDirPath)).listFiles());
		for (File archDir : archDirs) {
			archFiles.add(Arrays.stream(archDir.listFiles())
				.filter(f -> f.getName().contains(".rsf"))
				.findFirst().get());
		}
		archFiles = FileUtil.sortFileListByVersion(archFiles);

		// Get and sort the dependency files
		List<File> depsFiles = List.of(
			new File(FileUtil.tildeExpandPath(depsDirPath)).listFiles());
		depsFiles = depsFiles.stream()
			.filter(f -> f.getName().contains(".rsf")).collect(Collectors.toList());
		depsFiles = FileUtil.sortFileListByVersion(depsFiles);

		// Initialize the version list from the version directories
		this.versions = new String[archDirs.size()];
		archDirs = FileUtil.sortFileListByVersion(archDirs);
		for (int i = 0; i < archDirs.size(); i++)
			this.versions[i] = archDirs.get(i).getName();

		// Run a2a
		this.a2a = new double[this.versions.length - 1][];
		for (int i = 0; i < this.versions.length - 1; i++) {
			this.a2a[i] = new double[this.versions.length - 2 - i];

			for (int j = i + 1; j < this.versions.length; j++)
				this.a2a[i][j - i - 1] =
					SystemEvo.run(archFiles.get(i), archFiles.get(j));
		}

		// Run cvg
		this.cvgForwards = new double[this.versions.length - 1][];
		this.cvgBackwards = new double[this.versions.length - 1][];
		for (int i = 0; i < this.versions.length - 1; i++) {
			this.cvgForwards[i] = new double[this.versions.length - 2 - i];
			this.cvgBackwards[i] = new double[this.versions.length - 2 - i];

			for (int j = i + 1; j < this.versions.length; j++) {
				this.cvgForwards[i][j - i - 1] =
					Cvg.run(archFiles.get(i), archFiles.get(j));
				this.cvgBackwards[i][j - i - 1] =
					Cvg.run(archFiles.get(j), archFiles.get(i));
			}
		}

		// Run MoJoFM
		this.mojoFm = new double[this.versions.length - 1][];
		for (int i = 0; i < this.versions.length - 1; i++) {
			this.mojoFm[i] = new double[this.versions.length - 2 - i];

			for (int j = i + 1; j < this.versions.length; j++) {
				MoJoCalculator mojoCalc = new MoJoCalculator(
					archFiles.get(i).getAbsolutePath(),
					archFiles.get(j).getAbsolutePath(), null);
				this.mojoFm[i][j - i - 1] = mojoCalc.mojofm();
			}
		}

		// Initialize ArchitectureMetrics
		this.versionMetrics = new ArrayList<>();
		for (int i = 0; i < this.versions.length; i++)
			this.versionMetrics.add(new ArchitectureMetrics(
				archFiles.get(i).getAbsolutePath(),
				depsFiles.get(i).getAbsolutePath()));
	}

	private SystemMetrics(Collection<ArchitectureMetrics> versionMetrics,
			String[] versions, double[][] a2a, double[][] cvgForwards,
			double[][] cvgBackwards, double[][] mojoFm) {
		this.versionMetrics = versionMetrics;
		this.versions = versions;
		this.a2a = a2a;
		this.cvgForwards = cvgForwards;
		this.cvgBackwards = cvgBackwards;
		this.mojoFm = mojoFm;
	}
	//endregion

	//region ACCESSORS
	public Collection<ArchitectureMetrics> getVersionMetrics() {
		return new ArrayList<>(this.versionMetrics); }
	public String[] getVersions() {
		return Arrays.copyOf(this.versions, this.versions.length); }
	public double[][] getA2a() {
		return Arrays.copyOf(this.a2a, this.a2a.length); }
	public double[][] getCvgForwards() {
		return Arrays.copyOf(this.cvgForwards, this.cvgForwards.length); }
	public double[][] getCvgBackwards() {
		return Arrays.copyOf(this.cvgBackwards, this.cvgBackwards.length); }
	public double[][] getMojoFm() {
		return Arrays.copyOf(this.mojoFm, this.mojoFm.length); }
	//endregion

	//region SERIALIZATION
	public void serialize(String path) throws IOException {
		try (EnhancedJsonGenerator generator = new EnhancedJsonGenerator(path)) {
			serialize(generator);
		}
	}

	@Override
	public void serialize(EnhancedJsonGenerator generator) throws IOException {
		generator.writeField("archs", versionMetrics);
		generator.writeField("versions", List.of(versions));
		generator.writeField("a2a", a2a);
		generator.writeField("cvgF", cvgForwards);
		generator.writeField("cvgB", cvgBackwards);
		generator.writeField("mojo", mojoFm);
	}

	public static SystemMetrics deserialize(String path) throws IOException {
		try (EnhancedJsonParser parser = new EnhancedJsonParser(path)) {
			return deserialize(parser);
		}
	}

	public static SystemMetrics deserialize(EnhancedJsonParser parser)
			throws IOException {
		Collection<ArchitectureMetrics> versionMetrics =
			parser.parseCollection(ArchitectureMetrics.class);
		String[] versions = parser.parseStringArray();
		double[][] a2a = parser.parseDoubleMatrix();
		double[][] cvgForwards = parser.parseDoubleMatrix();
		double[][] cvgBackwards = parser.parseDoubleMatrix();
		double[][] mojoFm = parser.parseDoubleMatrix();

		return new SystemMetrics(versionMetrics, versions, a2a, cvgForwards,
			cvgBackwards, mojoFm);
	}
	//endregion
}
