package edu.usc.softarch.arcade.antipattern.detection.interfacebased;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.usc.softarch.arcade.util.FileUtil;

public class BatchCloneFinder {
	// #region FIELDS ------------------------------------------------------------
	private static Logger logger = LogManager.getLogger(BatchCloneFinder.class);
	private String depExt;
	private String cpdFilePath;
	private String antPath;
	// #endregion FIELDS ---------------------------------------------------------

	// #region CONSTRUCTORS ------------------------------------------------------
	public BatchCloneFinder(String depExt, String cpdFilePath, String antPath) {
		initialize(depExt, cpdFilePath, antPath);
	}

	public BatchCloneFinder() {
		String fs = File.separator;
		String newDepExt = System.getProperty("user.dir")+ fs + "ext-tools";
		String newCpdFilePath = "pmd-bin-5.3.2"+ fs + "cpd.xml";
		String newAntPath = "apache-ant-1.9.6"+ fs + "bin"+ fs + "ant";
		initialize(newDepExt, newCpdFilePath, newAntPath);
	}

	private void initialize(String depExt, String cpdFilePath, String antPath) {
		setDepExt(depExt);
		setCpdFilePath(cpdFilePath);
		setAntPath(antPath);
	}
	// #endregion CONSTRUCTORS ---------------------------------------------------

	// #region ACCESSORS ---------------------------------------------------------
	public String getDepExt() { return this.depExt; }
	public String getCpdFilePath() { return this.cpdFilePath; }
	public String getAntPath() { return this.antPath; }

	public void setDepExt(String depExt) { this.depExt = depExt; }
	public void setCpdFilePath(String cpdFilePath) {
		this.cpdFilePath = cpdFilePath; }
	public void setAntPath(String antPath) { this.antPath = antPath; }
	// #endregion ACCESSORS ------------------------------------------------------

	// #region PROCESSING --------------------------------------------------------
	public void run(File versionFolder, File outputDir, 
			String classesDirName) throws IOException {
		logger.debug("Processing directory: " + versionFolder.getName());
		
		String fs = File.separator;
		// the revision number is really just the name of the subdirectory
		String revisionNumber = versionFolder.getName();
		/* depsRsfFilename is the file name of the dependencies rsf file (one is
		   created per subdirectory of dir) */
		String depsXMLFilename = outputDir.getAbsolutePath() + fs
			+ revisionNumber + "_clone.xml";
		File depsXMLFile = new File(depsXMLFilename);
		if (!depsXMLFile.getParentFile().exists())
			depsXMLFile.getParentFile().mkdirs();
		
		/* classesDir is the directory in each subdirectory of the dir directory
		   that contains the compiled classes of the subdirectory */
		String absoluteClassesDir = versionFolder.getAbsolutePath()
			+ fs + classesDirName;
		File classesDirFile = new File(absoluteClassesDir);
		if (!classesDirFile.exists())	return;

		logger.debug("Get deps for revision " + revisionNumber);

		// Run deps for a single folder
		List<String> command;
		List<String> prepend = new ArrayList<>();

		// Windows
		if(SystemUtils.IS_OS_WINDOWS)
			prepend = List.of("cmd", "/c");

		command = List.of(getAntPath(), "-f",	getCpdFilePath(),	"cpd-Din="
			+ versionFolder + fs + classesDirName, "-Dout=" + depsXMLFilename);
		command.addAll(0, prepend);

		ProcessBuilder pb = new ProcessBuilder(command);
		pb.directory(new File(getDepExt()));
		pb.inheritIO();
		pb.redirectErrorStream(true);
		pb.start();
	}
	// #endregion PROCESSING -----------------------------------------------------

	// #region IO ----------------------------------------------------------------
	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			System.out.println(
				"Usage: BatchCloneFinder <inputSrcDirName> <outputDirName>");
			System.exit(-1);
		}

		BatchCloneFinder batchCloneFinder = new BatchCloneFinder();
		String inputDirPath = args[0];
		File inputDir = FileUtil.checkDir(inputDirPath, false, false);
		String outputDirPath = args[1];
		File outputDir = FileUtil.checkDir(outputDirPath, true, false);
		String classesDir = "";
		logger.info(System.getProperty("user.dir"));

		if (FileUtil.checkClassesDirs(inputDir, classesDir))
			throw new IOException("Classes directory not found or empty");

		File[] files = inputDir.listFiles();
		Set<File> fileSet = new TreeSet<>(Arrays.asList(files));

		// Makes a list by calling "toString" on every File in fileSet
		List<String> fileSetNames =
			fileSet.stream().map(File::toString).collect(Collectors.toList());

		logger.debug("All files in " + inputDir + ":");
		logger.debug(String.join("\n", fileSetNames));

		boolean nothingtodo = true;
		for (File versionFolder : fileSet) {
			if (versionFolder.isDirectory()) {
				logger.debug("Identified directory: " + versionFolder.getName());
				nothingtodo = false;
				batchCloneFinder.run(versionFolder, outputDir, classesDir);
			}
			else logger.debug("Not a directory: " + versionFolder.getName());
		}
		if (nothingtodo) System.out.println("Nothing to do!");
	}
	// #endregion IO -------------------------------------------------------------
}