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
//	static String testClass		= "apps";

//	nutch
//	static String testInput 	= "O:\\Subject_systems\\ivy";
//	static String testOutput 	= "O:\\extras\\depfinders";
//	static String testClass		= "";
	
//	static String testInput 	= "E:\\openjpa\\bin";
//	static String testOutput 	= "E:\\openjpa_data\\extra_info\\depfinders";
//	static String testClass		= "";

	
//	static String testInput 	= "E:\\android\\binary";
//	static String testOutput 	= "E:\\android\\depfinders";
//	static String testClass		= "";
	
	
//	cxf
//	static String testInput 	= "F:\\cxf_data\\cxf_binary";
//	static String testOutput 	= "F:\\cxf_data\\DepFinder";
//	static String testClass		= "lib";

//	static String testInput 	= "F:\\wicket_data\\src_3";
//	static String testOutput 	= "F:\\wicket_data\\extra_info\\DepFinders";
//	static String testClass		= "lib";

//	Camel
//	static String testInput 	= "F:\\camel_data\\New Folder";
//	static String testOutput 	= "F:\\camel_data\\extra_info\\";
//	static String testClass		= "lib";
	
//	Wicket
//	static String testInput 	= "F:\\wicket_data\\src_2";
//	static String testOutput 	= "F:\\wicket_data\\extra_info\\DepFinders";
//	static String testClass		= "";
	
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
