package edu.usc.softarch.arcade.antipattern.detection.interfacebased;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jeantessier.dependencyfinder.cli.DependencyExtractor;

import edu.usc.softarch.arcade.util.RecoveryParams;

/**
 * @author nikola
 *
 * Running {@code DependencyExtractor} for each version of system under test.
 *
 * Outputting _deps.xml files.
 */
public class DepFindRunner {
	private static Logger logger = LogManager.getLogger(DepFindRunner.class);

	public static void main(final String[] args) {
		if (args.length != 3) {
			System.out.println("Usage: BatchDepFinder <inputSrcDirName> <outputDirName> <classDirName>");
			System.exit(-1);
		}
		String inputDir = args[0];
		String outputDir = args[1];
		String classesDir = args[2];
		logger.info(System.getProperty("user.dir"));

		RecoveryParams recParams = null;
		try {
			recParams =	new RecoveryParams(inputDir, outputDir, classesDir);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}

		File[] files = recParams.getInputDir().listFiles();
		Set<File> fileSet = new TreeSet<>(Arrays.asList(files));

		// Makes a list by calling "toString" on every File in fileSet
		List<String> fileSetNames =
			fileSet.stream().map(File::toString).collect(Collectors.toList());

		logger.debug("All files in " + recParams.getInputDir() + ":");
		logger.debug(String.join("\n", fileSetNames));

		boolean nothingtodo = true;
		for (File versionFolder : fileSet) {
			if (versionFolder.isDirectory()) {
				logger.debug("Identified directory: " + versionFolder.getName());
				nothingtodo = false;
				single(versionFolder, recParams.getOutputDir(),
					recParams.getClassesDirName());
			} else {
				logger.debug("Not a directory: " + versionFolder.getName());
			}
		}
		if (nothingtodo) {
			System.out.println("Nothing to do!");
		}
	}

	public static void single(File versionFolder, File outputDir,
			String classesDirName) {
		logger.debug("Processing directory: " + versionFolder.getName());

		String fs = File.separator;
		// the revision number is really just the name of the subdirectory
		String revisionNumber = versionFolder.getName();
		// depsRsfFilename is the file name of the dependencies rsf file (one is
		// created per subdirectory of dir)
		String depsXMLFilename = outputDir.getAbsolutePath() + fs
			+ revisionNumber + "_deps.xml";
		File depsXMLFile = new File(depsXMLFilename);
		if (!depsXMLFile.getParentFile().exists())
			depsXMLFile.getParentFile().mkdirs();

		/* classesDir is the directory in each subdirectory of the dir directory
		   that contains the compiled classes of the subdirectory */
		String absoluteClassesDir = versionFolder.getAbsolutePath()
			+ fs + classesDirName;
		File classesDirFile = new File(absoluteClassesDir);
		if (!classesDirFile.exists())
			return;

		logger.debug("Get deps for revision " + revisionNumber);

		String[] args =
			new String[] {"-xml", "-out", depsXMLFilename, absoluteClassesDir};
		try {
			DependencyExtractor.main(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}