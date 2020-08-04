package edu.usc.softarch.arcade.antipattern.detection.codesmell;

import java.io.File;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Joiner;

import edu.usc.softarch.arcade.util.RecoveryParams;

public class CodeSmellJsonAnalyzer {
	private static Logger logger =
		LogManager.getLogger(CodeSmellJsonAnalyzer.class);
	static String testInput 	= "F:\\code_smell\\output-apache-activemq";
	static String testOutput 	= "F:\\ICSE_2016_data";
	static String testClass		= "";
	
	public static void main(String[] args1) {
		logger.info(System.getProperty("user.dir"));
		RecoveryParams recParams = null;
		try {
			recParams =	new RecoveryParams(testInput, testOutput, testClass);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		File[] files = recParams.getInputDir().listFiles();
		Set<File> fileSet = new TreeSet<>(Arrays.asList(files));
		logger.debug("All files in " + recParams.getInputDir() + ":");
		logger.debug(Joiner.on("\n").join(fileSet));
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
		String fs = File.separator;
		logger.debug("Processing directory: " + versionFolder.getName());
		// the revision number is really just the name of the subdirectory
		String revisionNumber = versionFolder.getName();
		// depsRsfFilename is the file name of the dependencies rsf file (one is
		// created per subdirectory of dir)
		String depsXMLFilename = outputDir.getAbsolutePath() + fs
			+ revisionNumber + "_count.csv";
		File depsXMLFile = new File(depsXMLFilename);
		if (!depsXMLFile.getParentFile().exists())
			depsXMLFile.getParentFile().mkdirs();

		// classesDir is the directory in each subdirectory of the dir directory
		// that contains the compiled classes of the subdirectory
		String absoluteClassesDir = versionFolder.getAbsolutePath() + fs
			+ classesDirName;
		File classesDirFile = new File(absoluteClassesDir);
		if (!classesDirFile.exists())
			return;

		logger.debug("Get couting for revision " + revisionNumber);
	}
}