package edu.usc.softarch.arcade;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import edu.usc.softarch.arcade.clustering.acdc.ACDC;

import com.google.common.base.Joiner;

import edu.usc.softarch.arcade.antipattern.detection.ArchSmellDetector;
import edu.usc.softarch.arcade.facts.driver.CSourceToDepsBuilder;
import edu.usc.softarch.arcade.facts.driver.JavaSourceToDepsBuilder;
import edu.usc.softarch.arcade.facts.driver.SourceToDepsBuilder;
import edu.usc.softarch.arcade.util.FileUtil;

public class AcdcWithSmellDetection {
	private static Logger logger =
		LogManager.getLogger(AcdcWithSmellDetection.class);

	/**
	 * inputDirName is a directory where each subdirectory contains a revision
	 * or version of the system to be analyzed
	 */
	private static File inputDir;
	/**
	 * outputDirName is the directory where dependencies rsf files, cluster rsf
	 * files, and detected smells ser files are generated
	 */
	private static File outputDir;
	/**
	 * classesDir is the directory in each subdirectory of the dir directory
	 * that contains the compiled classes of the subdirectory
	 */
	private static String classesDir;
	//TODO Replace with the SourceBuilder itself once those have been refactored
	private static String srcLanguage;
	
	public static void main(String[] args) throws IOException  {
		// Setup
		loadArguments(args);
		File[] files = inputDir.listFiles();
		Set<File> fileSet = new TreeSet<>(Arrays.asList(files));

		// Console Output
		logger.debug("All files in " + inputDir + ":");
		logger.debug(Joiner.on("\n").join(fileSet));

		for (File file : fileSet) {
			if (file.isDirectory()) {
				logger.debug("Identified directory: " + file.getName());
			}
		}

		// Processing
		for (File vFolder : fileSet) {
			if (vFolder.isDirectory()) {
				single (vFolder, args, outputDir);
			}
		}		
	}

	public static void single (File versionFolder, String[] args, File outputDir)
			throws IOException {
		loadSingleArguments(args);
		String fs = File.separator;
		//TODO Permit project name and version to be input as argument
		String versionFolderName = versionFolder.getName();
		logger.debug("Processing directory: " + versionFolderName);

		String classesDirPath = versionFolder.getAbsolutePath() + fs + classesDir;
		File classesDirFile = new File(classesDirPath);
		if (!classesDirFile.exists())
		{
			String errorMessage = "Classes directory " + classesDir + " not found "
			 + "for version " + versionFolderName;
			throw new IllegalArgumentException(errorMessage);
		}
		
		// depsRsfFilename is the file name of the dependencies rsf file (one is 
		// created per subdirectory of dir)
		String depsRsfFilename = outputDir.getAbsolutePath() + fs 
			+ versionFolderName + "_deps.rsf";
		File depsRsfFile = new File(depsRsfFilename);
		if (!depsRsfFile.getParentFile().exists())
			depsRsfFile.getParentFile().mkdirs();
		
		logger.debug("Get deps for revision " + versionFolderName);
		
		SourceToDepsBuilder builder;
		switch(srcLanguage) {
			case "java":
				builder = new JavaSourceToDepsBuilder();
				break;
			case "c":
				builder = new CSourceToDepsBuilder();
				break;
			default:
				throw new IllegalArgumentException("Error parsing arguments.");
		}
		
		builder.build(classesDirPath, depsRsfFilename);
		if (builder.getEdges().isEmpty()) return; //TODO Throw appropriate error
		
		// acdcClusteredfile is the recovered architecture for acdc, one per
		// subdirectory of dir
		String acdcClusteredFile = outputDir.getAbsolutePath() + fs
			+ versionFolderName + "_acdc_clustered.rsf";
		String[] acdcArgs = { depsRsfFile.getAbsolutePath(), acdcClusteredFile };
		
		logger.debug("Running acdc for revision " + versionFolderName);
		ACDC.main(acdcArgs);
		
		logger.debug("Running smell detecion for revision " + versionFolderName);

		ArchSmellDetector asd = new ArchSmellDetector(depsRsfFile.getAbsolutePath(),
			acdcClusteredFile, outputDir.getAbsolutePath() + fs + versionFolderName
			+ "_acdc_smells.ser");

		asd.runStructuralDetectionAlgs();
	}

	// #region AUXILIARY METHODS -------------------------------------------------
	private static void loadArguments(String[] args) {
		String inputDirName = args[0];
		inputDir = new File(FileUtil.tildeExpandPath(inputDirName));

		String outputDirName = args[1];
		outputDir = new File(FileUtil.tildeExpandPath(outputDirName));
	}

	private static void loadSingleArguments(String[] args) {
		classesDir = args[2];

		if (args.length == 4)
		{
			switch(args[3].toLowerCase()) {
				case "c":
					srcLanguage = "c";
					break;
				case "java":
					srcLanguage = "java";
					break;
				default:
					String errorMessage = "Unknown source language selected: " + args[3];
					throw new IllegalArgumentException(errorMessage);
			}
		} else srcLanguage = "java";
	}
	// #endregion AUXILIARY METHODS ----------------------------------------------
}
