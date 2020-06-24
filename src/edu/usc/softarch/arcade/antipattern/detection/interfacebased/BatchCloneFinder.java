package edu.usc.softarch.arcade.antipattern.detection.interfacebased;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import org.apache.logging.log4j.Logger;

import com.google.common.base.Joiner;

import edu.usc.softarch.arcade.util.RecoveryParams;

public class BatchCloneFinder {
	static Logger logger = org.apache.logging.log4j.LogManager.getLogger(BatchCloneFinder.class);
	//static String DepExt = "F:\\DependencyFinder-1.2.1-beta4\\bin";
//	static String DepExt  		= System.getProperty("user.dir") + File.separator+ "DependencyFinder" + File.separator + "bin";
//	static String DepExt  		= "C:\\apache-ant-1.9.6\\bin";
	static String DepExt  		= "ext-tools";

//	Nutch
//	static String testInput 	= "O:\\Subject_systems\\ivy";
//	static String testOutput 	= "O:\\extras\\clones";
//	static String testClass		= "";
	
//	static String testInput 	= "E:\\openjpa\\src";
//	static String testOutput 	= "E:\\openjpa_data\\clones";
//	static String testClass		= "";

//	static String testInput 	= "E:\\android\\src";
//	static String testOutput 	= "E:\\android\\clones";
//	static String testClass		= "";

	
//cxf
//	static String testInput 	= "F:\\cxf_data\\cxf_src";
//	static String testOutput 	= "F:\\cxf_data\\clone";
//	static String testClass		= "";

//	Tika
//	static String testInput 	= "F:\\tika-data\\src";
//	static String testOutput 	= "F:\\tika-data\\extra_info\\clones";
//	static String testClass		= "";
	
//	Camel
//	static String testInput 	= "F:\\camel_data\\src_1";
//	static String testOutput 	= "F:\\camel_data\\extra_info\\clones_1";
//	static String testClass		= "";
	
//	Wicket
//	static String testInput 	= "F:\\wicket_data\\src";
//	static String testOutput 	= "F:\\wicket_data\\extra_info\\clones";
//	static String testClass		= "";
	
//	static String[] args	= new String[]{testInput, testOutput, testClass};
	
	public static void main(final String[] args1) throws IOException {
		if (args1.length != 2) {
			System.out.println("Usage: BatchCloneFinder <inputSrcDirName> <outputDirName>");
			System.exit(-1);
		}
		String[] args	= new String[]{args1[0], args1[1], ""};
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
		final String depsXMLFilename = outputDir.getAbsolutePath() + File.separatorChar + revisionNumber + "_clone.xml";
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

		// Run deps for a single folder
		// Run for windows
//		String command = "cmd /c ant -f C:\\pmd-bin-5.3.2\\cpd.xml cpd -Din=" 
//				+ versionFolder + File.separator + classesDirName + " -Dout="+depsXMLFilename;
		// Run for linux
		String command = "apache-ant-1.9.6/bin/ant -f pmd-bin-5.3.2/cpd.xml cpd -Din=" 
				+ versionFolder + File.separator + classesDirName + " -Dout="+depsXMLFilename;
		Process p =  Runtime.getRuntime().exec(command, 
				null, new File(DepExt));
	}
}
