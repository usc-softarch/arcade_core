package edu.usc.softarch.arcade.util;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Recovery Parameters container class
 *
 * @author daniellink
 */
public class RecoveryParams {
	// #region FIELDS ------------------------------------------------------------
	private static Logger logger = LogManager.getLogger(RecoveryParams.class);
	/**
	 * Directory where each subdirectory contains a revision or version of system
	 */
	private File inputDir;
	/**
	 * Directory where dependencies rsf files, cluster rsf files and detected
	 * smells ser files are generated
	 */
	private File outputDir;
	/**
	 * Directory in each subdirectory of inputDir that contains compiled classes
	 */
	private String classesDirName;
	private String language;
	// #endregion FIELDS ---------------------------------------------------------

	// #region CONSTRUCTORS ------------------------------------------------------
	@Deprecated
	public RecoveryParams(String[] commandLineParams) throws Exception {
		initialize(
			commandLineParams[0],
			commandLineParams[1],
			commandLineParams[2],
			commandLineParams.length > 3 ? commandLineParams[3] : "java");
	}

	public RecoveryParams(String inputDir, String outputDir, 
			String classesDirName, String language) throws Exception {
		initialize(inputDir, outputDir, classesDirName, language);
	}

	public RecoveryParams(String inputDir, String outputDir, 
			String classesDirName) throws Exception {
		initialize(inputDir, outputDir, classesDirName, "java");
	}

	private void initialize(String inputDir, String outputDir,
			String classesDirName, String language) throws Exception {
		setInputDir(FileUtil.checkDir(inputDir, false, false));
		setOutputDir(FileUtil.checkDir(outputDir, true, false));
		setClassesDirName(classesDirName);
		if (!checkClassesDirs()) {
			logger.error("Could not find binaries directory " + getClassesDirName()
				+ " in " + getInputDir().toString());
			throw new Exception();
		}
		setLanguage(language);
	}
	// #endregion CONSTRUCTORS ---------------------------------------------------

	// #region ACCESSORS ---------------------------------------------------------
	public File getInputDir() { return inputDir; }
	public String getInputDirAbsolutePath() { return inputDir.getAbsolutePath(); }
	public File getOutputDir() { return outputDir; }
	public String getOutputDirAbsolutePath() {
		return outputDir.getAbsolutePath(); }
	public String getClassesDirName() { return classesDirName; }
	public String getLanguage() { return language; }

	public void setInputDir(File inputDir) { this.inputDir = inputDir; }
	public void setOutputDir(File outputDir) { this.outputDir = outputDir; }
	public void setClassesDirName(String classesDirName) {
		this.classesDirName = classesDirName; }
	public void setLanguage(String language) { this.language = language; }
	// #endregion ACCESSORS ------------------------------------------------------

	public boolean checkClassesDirs() {
		return checkClassesDirs(inputDir, classesDirName);
	}

	public static boolean checkClassesDirs(File inputDir, String classesDirs) {
		String fs = File.separator;
		// List all files in inputDir
		List<File> versionDirectories = Arrays.asList(inputDir.listFiles());
		// Remove if not a directory
		versionDirectories.removeIf((File file) -> !file.isDirectory());

		for (File d : versionDirectories) {
			File currentClassesDir = FileUtil.checkDir(d.getAbsolutePath() + fs
				+ classesDirs, false, false);
			if (!currentClassesDir.isDirectory()) {
				logger.debug("Classes directory " + d.getAbsolutePath()
					+ " does not exist!");
				return false;
			}
		}
		return true;
	}
}