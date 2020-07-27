package edu.usc.softarch.arcade;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import acdc.ACDC;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.google.common.base.Joiner;

import edu.usc.softarch.arcade.antipattern.detection.ArchSmellDetector;
import edu.usc.softarch.arcade.config.Config;
import edu.usc.softarch.arcade.facts.driver.CSourceToDepsBuilder;
import edu.usc.softarch.arcade.facts.driver.JavaSourceToDepsBuilder;
import edu.usc.softarch.arcade.facts.driver.SourceToDepsBuilder;
import edu.usc.softarch.arcade.util.FileUtil;

public class PkgsWithSmellDetection {
	
	static Logger logger = Logger.getLogger(PkgsWithSmellDetection.class);
	
	public static void main(String[] args) throws IOException  {
		PropertyConfigurator.configure(Config.getLoggingConfigFilename());
		
		PkgsWithSmellDetectionOptions psdOptions = new PkgsWithSmellDetectionOptions();
		JCommander jcmd = new JCommander(psdOptions);;
		
		try {
			jcmd.parse(args); 
		}
		catch (ParameterException e) {
			System.out.println(e.getMessage());
			jcmd.usage();
			System.exit(1);
		}
		
		File clustersDir = new File(FileUtil.tildeExpandPath(psdOptions.clustersDir));
		File smellsDir = new File(FileUtil.tildeExpandPath(psdOptions.smellsDir));
		File depsDir = new File(FileUtil.tildeExpandPath(psdOptions.depsDir));
		
		File[] files = clustersDir.listFiles();
		Set<File> fileSet = new TreeSet<File>(Arrays.asList(files));
		logger.debug("All files in " + clustersDir + ":");
		logger.debug(Joiner.on("\n").join(fileSet));
		for (File file : fileSet) {
			if (file.isDirectory()) {
				logger.debug("Identified directory: " + file.getName());
			}
		}
		
		for (File file : fileSet) {
			if (file.getName().endsWith("_pkgs.rsf")) {
				File clustersPkgFile = file;
				String pkgFilePrefix = file.getName().replace(
						"_pkgs.rsf", "");
				String expectedDepsFilename = pkgFilePrefix + ".rsf";
				File identifiedDepsFile = null;
				File[] potentialDepsFiles = depsDir.listFiles();
				for (File potentialDepsFile : potentialDepsFiles) {
					if (potentialDepsFile.getName()
							.equals(expectedDepsFilename)) {
						identifiedDepsFile = potentialDepsFile;
					}
				}
				
				if (identifiedDepsFile == null) {
					System.err.println("Could not find deps file: " + expectedDepsFilename);
					System.exit(2);
				}

				// the last element of the smellArgs array is the location of
				// the file containing the detected smells (one is created per
				// subdirectory of dir)
				String[] smellArgs = {
						identifiedDepsFile.getAbsolutePath(),
						clustersPkgFile.getAbsolutePath(),
						smellsDir.getAbsolutePath() + File.separatorChar
								+ pkgFilePrefix + "_pkg_smells.ser" };
				logger.debug("Running smell detecion for release "
						+ pkgFilePrefix);
				ArchSmellDetector.setupAndRunStructuralDetectionAlgs(smellArgs);
			}

		}
	}
}
