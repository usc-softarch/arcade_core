package edu.usc.softarch.arcade.antipattern.detection.interfacebased;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.SystemUtils;

import org.apache.logging.log4j.Logger;

import com.google.common.base.Joiner;

import edu.usc.softarch.arcade.util.RecoveryParams;

public class BatchCloneFinder {
	private static Logger logger = org.apache.logging.log4j.LogManager.getLogger(BatchCloneFinder.class);
	static String DepExt  		= System.getProperty("user.dir")+ File.separator +"ext-tools";
	static String cpd_file_path  = "pmd-bin-5.3.2"+ File.separator +"cpd.xml";
	static String ant_path       = "apache-ant-1.9.6"+ File.separator +"bin"+ File.separator +"ant";

	public static void main(final String[] args1) throws IOException {
		if (args1.length != 2) {
			System.out.println("Usage: BatchCloneFinder <inputSrcDirName> <outputDirName>");
			System.exit(-1);
		}
		String[] args	= new String[]{args1[0], args1[1], ""};
		logger.info(System.getProperty("user.dir"));
		final RecoveryParams recParms = new RecoveryParams(args);
		final File[] files = recParms.getInputDir().listFiles();
		final Set<File> fileSet = new TreeSet<>(Arrays.asList(files));
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

		String[] command = {};

		// Run for windows
		if(SystemUtils.IS_OS_WINDOWS)
		{
			String[] windows_command = {"cmd","/c",ant_path,"-f",cpd_file_path, "cpd","-Din=" + versionFolder + File.separator + classesDirName,"-Dout="+depsXMLFilename};
			command = ArrayUtils.addAll(command, windows_command);
		}
		// Run for linux
		else
		{
			String[] linux_command   = {ant_path,"-f",cpd_file_path, "cpd","-Din=" + versionFolder + File.separator + classesDirName,"-Dout="+depsXMLFilename};
			command = ArrayUtils.addAll(command, linux_command);
		}

		ProcessBuilder pb = new ProcessBuilder(command);
		pb.directory(new File(DepExt));
		pb.inheritIO();
		pb.redirectErrorStream(true);
		pb.start();
	}
}
