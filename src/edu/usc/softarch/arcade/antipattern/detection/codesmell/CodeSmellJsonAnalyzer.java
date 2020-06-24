package edu.usc.softarch.arcade.antipattern.detection.codesmell;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import org.apache.logging.log4j.Logger;

import com.google.common.base.Joiner;

import edu.usc.softarch.arcade.util.RecoveryParams;

public class CodeSmellJsonAnalyzer {
	static Logger logger = org.apache.logging.log4j.LogManager.getLogger(CodeSmellJsonAnalyzer.class);
	//static String DepExt = "F:\\DependencyFinder-1.2.1-beta4\\bin";
	static String DepExt  		= System.getProperty("user.dir") + File.separator+ "DependencyFinder" + File.separator + "bin";
	static String testInput 	= "F:\\code_smell\\output-apache-activemq";
	static String testOutput 	= "F:\\ICSE_2016_data";
	static String testClass		= "";
	
	static String[] args	= new String[]{testInput, testOutput, testClass};
	
	public static void main(final String[] args1) throws IOException {
//		if (args.length < 3 || args.length > 4) {
//			System.out.println("Usage: InterfaceBasedSmellDetection <inputDirName> <outputDirName> <classesDir> [language]");
//			System.exit(-1);
//		}
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
		final String depsXMLFilename = outputDir.getAbsolutePath() + File.separatorChar + revisionNumber + "_count.csv";
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

		logger.debug("Get couting for revision " + revisionNumber);

		// Run deps for a single folder
	}
}
