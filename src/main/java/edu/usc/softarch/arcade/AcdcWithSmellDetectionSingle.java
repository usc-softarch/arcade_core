package edu.usc.softarch.arcade;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


import edu.usc.softarch.arcade.config.Config;
import edu.usc.softarch.arcade.util.FileUtil;

public class AcdcWithSmellDetectionSingle {

	static Logger logger = Logger.getLogger(AcdcWithSmellDetectionSingle.class);

	public static void main(String[] args) throws IOException {

		PropertyConfigurator.configure(Config.getLoggingConfigFilename());

		// inputDirName is a directory where each subdirectory contains a
		// revision or version of the system to be analyzed
		String inputDirName = args[0];
		File inputDir = new File(FileUtil.tildeExpandPath(inputDirName));

		// outputDirName is the directory where dependencies rsf files, cluster
		// rsf files, and detected smells ser files are generated
		String outputDirName = args[1];
		File outputDir = new File(FileUtil.tildeExpandPath(outputDirName));
		
		AcdcWithSmellDetection.single(inputDir, args, outputDir);
	}
}
