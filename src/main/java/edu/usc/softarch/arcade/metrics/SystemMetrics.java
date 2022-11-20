package edu.usc.softarch.arcade.metrics;

import edu.usc.softarch.arcade.util.FileUtil;
import mojo.MoJoCalculator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class SystemMetrics {
	//region ATTRIBUTES
	private final Collection<ArchitectureMetrics> versionMetrics;

	private final String[] versions;
	private final double[] a2a;
	private final double[] cvgForwards;
	private final double[] cvgBackwards;
	private final double[] mojoFm;
	//endregion

	//region CONSTRUCTORS
	public SystemMetrics(String systemDirPath, String depsDirPath)
			throws IOException {
		// Get and sort the architecture files
		List<File> archFiles = new ArrayList<>();
		List<File> archDirs = FileUtil.getFileListing(
			new File(FileUtil.tildeExpandPath(systemDirPath)));
		for (File archDir : archDirs) {
			archFiles.add(Arrays.stream(archDir.listFiles())
				.filter(f -> f.getName().contains(".rsf"))
				.findFirst().get());
		}
		archFiles = FileUtil.sortFileListByVersion(archFiles);

		// Get and sort the dependency files
		List<File> depsFiles = FileUtil.getFileListing(
			new File(FileUtil.tildeExpandPath(depsDirPath)));
		depsFiles = depsFiles.stream()
			.filter(f -> f.getName().contains(".rsf")).collect(Collectors.toList());
		depsFiles = FileUtil.sortFileListByVersion(depsFiles);

		// Initialize the version list from the version directories
		this.versions = new String[archDirs.size()];
		archDirs = FileUtil.sortFileListByVersion(archDirs);
		for (int i = 0; i < archDirs.size(); i++)
			this.versions[i] = archDirs.get(i).getName();

		// Run a2a
		this.a2a = new double[this.versions.length - 1];
		for (int i = 0; i < this.versions.length - 1; i++)
			this.a2a[i] = SystemEvo.run(archFiles.get(i), archFiles.get(i + 1));

		// Run cvg
		this.cvgForwards = new double[this.versions.length - 1];
		this.cvgBackwards = new double[this.versions.length - 1];
		for (int i = 0; i < this.versions.length - 1; i++) {
			this.cvgForwards[i] = Cvg.run(archFiles.get(i), archFiles.get(i + 1));
			this.cvgBackwards[i] = Cvg.run(archFiles.get(i + 1), archFiles.get(i));
		}

		// Run MoJoFM
		this.mojoFm = new double[this.versions.length - 1];
		for (int i = 0; i < this.versions.length - 1; i++) {
			MoJoCalculator mojoCalc = new MoJoCalculator(
				archFiles.get(i).getAbsolutePath(),
				archFiles.get(i + 1).getAbsolutePath(), null);
			this.mojoFm[i] = mojoCalc.mojofm();
		}

		// Initialize ArchitectureMetrics
		this.versionMetrics = new ArrayList<>();
		for (int i = 0; i < this.versions.length; i++)
			this.versionMetrics.add(new ArchitectureMetrics(
				archFiles.get(i).getAbsolutePath(),
				depsFiles.get(i).getAbsolutePath()));
	}
	//endregion

	//region ACCESSORS
	public Collection<ArchitectureMetrics> getVersionMetrics() {
		return new ArrayList<>(this.versionMetrics); }
	public String[] getVersions() {
		return Arrays.copyOf(this.versions, this.versions.length); }
	public double[] getA2a() {
		return Arrays.copyOf(this.a2a, this.a2a.length); }
	public double[] getCvgForwards() {
		return Arrays.copyOf(this.cvgForwards, this.cvgForwards.length); }
	public double[] getCvgBackwards() {
		return Arrays.copyOf(this.cvgBackwards, this.cvgBackwards.length); }
	public double[] getMojoFm() {
		return Arrays.copyOf(this.mojoFm, this.mojoFm.length); }
	//endregion
}
