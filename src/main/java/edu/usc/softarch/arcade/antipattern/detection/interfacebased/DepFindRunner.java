package edu.usc.softarch.arcade.antipattern.detection.interfacebased;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import org.apache.logging.log4j.Logger;

import com.google.common.base.Joiner;
import com.jeantessier.dependencyfinder.cli.DependencyExtractor;

import edu.usc.softarch.arcade.util.RecoveryParams;

/**
 *
 * @author nikola
 *
 * Running {@code DependencyExtractor} for each version of system under test.
 *
 * Outputting _deps.xml files.
 */
public class DepFindRunner {

	static Logger logger = org.apache.logging.log4j.LogManager.getLogger(DepFindRunner.class);

//	static String testInput 	= "F:\\continuum\\continuum_binary_2";
//	static String testOutput 	= "F:\\continuum\\DepFinder";
//	static String testClass		= "apps\\continuum\\WEB-INF\\classes";

//	static String[] args	= new String[]{testInput, testOutput, testClass};

	public static void main(final String[] args1) throws IOException {
		if (args1.length != 3) {
			System.out.println("Usage: BatchDepFinder <inputSrcDirName> <outputDirName> <classDirName>");
			System.exit(-1);
		}
		String[] args	= new String[]{args1[0], args1[1], args1[2]};
		logger.info(System.getProperty("user.dir"));
		final RecoveryParams recParms = new RecoveryParams(args);
		final File[] files = recParms.getInputDir().listFiles();
		final Set<File> fileSet = new TreeSet<File>(Arrays.asList(files));
		logger.debug("All files in " + recParms.getInputDir() + ":");
		logger.debug(Joiner.on("\n").join(fileSet));
		boolean nothingtodo = true;
		for (final File versionFolder : fileSet) {
			if (versionFolder.isDirectory()) {
				logger.debug("Identified directory: " + versionFolder.getName());
				nothingtodo = false;
				single(versionFolder, recParms.getOutputDir(), recParms.getClassesDirName(), recParms.getLanguage());
			} else {
				logger.debug("Not a directory: " + versionFolder.getName());
			}
		}
		if (nothingtodo) {
			System.out.println("Nothing to do!");
		}
	}

	public static void single(final File versionFolder, final File outputDir, final String classesDirName, final String language) throws IOException {
		logger.debug("Processing directory: " + versionFolder.getName());
		// the revision number is really just the name of the subdirectory
		final String revisionNumber = versionFolder.getName();
		// depsRsfFilename is the file name of the dependencies rsf file (one is
		// created per subdirectory of dir)
		final String depsXMLFilename = outputDir.getAbsolutePath() + File.separatorChar + revisionNumber + "_deps.xml";
		final File depsXMLFile = new File(depsXMLFilename);
		if (!depsXMLFile.getParentFile().exists()) {
			depsXMLFile.getParentFile().mkdirs();
		}
		// classesDir is the directory in each subdirectory of the dir directory
		// that contains the compiled classes of the subdirectory

		final String absoluteClassesDir = versionFolder.getAbsolutePath() + File.separatorChar + classesDirName;
		final File classesDirFile = new File(absoluteClassesDir);
		if (!classesDirFile.exists()) {
			return;
		}

		logger.debug("Get deps for revision " + revisionNumber);

		String[] args = new String[] {"-xml", "-out", depsXMLFilename, absoluteClassesDir};
		try {
			DependencyExtractor.main(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
