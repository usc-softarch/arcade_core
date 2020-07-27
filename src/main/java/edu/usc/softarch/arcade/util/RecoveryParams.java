/**
 *
 */
package edu.usc.softarch.arcade.util;

import java.io.File;

/**
 * Recovery Parameters container class
 *
 * @author daniellink
 *
 */
public class RecoveryParams {
	File inputDir;
	File outputDir;
	String classesDirName;
	String language;

	public File getInputDir() {
		return inputDir;
	}

	public void setInputDir(File inputDir) {
		this.inputDir = inputDir;
	}

	public String getInputDirAbsolutePath() {
		return inputDir.getAbsolutePath();
	}

	public File getOutputDir() {
		return outputDir;
	}

	public void setOutputDir(File outputDir) {
		this.outputDir = outputDir;
	}

	public String getOutputDirAbsolutePath() {
		return outputDir.getAbsolutePath();
	}

	public String getClassesDirName() {
		return classesDirName;
	}

	public void setClassesDirName(String classesDirName) {
		this.classesDirName = classesDirName;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public RecoveryParams(String[] commandLineParams) {
		// inputDirName is a directory where each subdirectory contains a
		// revision or version of the system to be analyzed
		inputDir = FileUtil.checkDir(commandLineParams[0], false, false);
		// outputDirName is the directory where dependencies rsf files, cluster
		// rsf files, and detected smells ser files are generated
		outputDir = FileUtil.checkDir(commandLineParams[1], true, false);
		// classesDir is the directory in each subdirectory of the dir directory
		// that contains the compiled classes of the subdirectory
		classesDirName = commandLineParams[2];
		if (!checkClassesDirs()) {
			System.out.println("Exiting");
			System.exit(-1);
		}
		language = commandLineParams.length > 3 ? commandLineParams[3] : "java";
	}

	public boolean checkClassesDirs() {
		return checkClassesDirs(inputDir, classesDirName);
	}

	public static boolean checkClassesDirs(File inputDir_, String classesDirs) {
		final File[] versionDirectories = inputDir_.listFiles();
		for (final File d : versionDirectories) {
			if (d.isDirectory()) {
				final File currentClassesDir = FileUtil.checkDir(d.getAbsolutePath() + File.separatorChar + classesDirs, false, false);
				if (!currentClassesDir.isDirectory()) {
					System.out.println("Classes directory " + d.getAbsolutePath() + " does not exist!");
					return false;
				}
			}
		}
		return true;
	}
}
