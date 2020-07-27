package edu.usc.softarch.arcade.clustering;

import java.io.File;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import edu.usc.softarch.arcade.config.Config;

import edu.usc.softarch.arcade.util.FileUtil;

public class BatchClusteringEngineSingle {
	static Logger logger = Logger.getLogger(BatchClusteringEngineSingle.class);

	public static void main(String[] args) throws Exception {
		PropertyConfigurator.configure(Config.getLoggingConfigFilename());
		// directory where each subdirectory is a different version or revision of the system you want to analyze
		String inputDirName = args[0];
		File inputDir = new File(FileUtil.tildeExpandPath(inputDirName));
		// directory where all the output will go for every version or revision
		String outputDirName = args[1];
		// location of classes file, jar, or zip
		String inClassesDir = args[2];
		BatchClusteringEngine.single(inputDir, args, outputDirName, inClassesDir);
	}
}

