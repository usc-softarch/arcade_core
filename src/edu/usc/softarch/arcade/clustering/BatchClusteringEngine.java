
package edu.usc.softarch.arcade.clustering;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.google.common.base.Joiner;

import edu.usc.softarch.arcade.DriverEngine;
import edu.usc.softarch.arcade.antipattern.detection.ArchSmellDetector;
import edu.usc.softarch.arcade.clustering.util.ClusterUtil;
import edu.usc.softarch.arcade.config.Config;
import edu.usc.softarch.arcade.config.Config.SimMeasure;
import edu.usc.softarch.arcade.config.Config.StoppingCriterionConfig;
import edu.usc.softarch.arcade.facts.driver.CSourceToDepsBuilder;
import edu.usc.softarch.arcade.facts.driver.JavaSourceToDepsBuilder;
import edu.usc.softarch.arcade.facts.driver.SourceToDepsBuilder;
import edu.usc.softarch.arcade.topics.TopicModelExtractionMethod;
import edu.usc.softarch.arcade.topics.TopicUtil;
import edu.usc.softarch.arcade.util.FileUtil;

public class BatchClusteringEngine {
	static Logger logger = Logger.getLogger(BatchClusteringEngine.class);

	public static void main(String[] args) throws Exception {
		PropertyConfigurator.configure(Config.getLoggingConfigFilename());
		
		// directory where each subdirectory is a different version or revision of the system you want to analyze
		String inputDirName = args[0];
		File inputDir = new File(FileUtil.tildeExpandPath(inputDirName));
		
		// directory where all the output will go for every version or revision
		String outputDirName = args[1];
		
		File[] files = inputDir.listFiles();
		Set<File> fileSet = new TreeSet<File>(Arrays.asList(files));
		logger.debug("All files in " + inputDir + ":");
		logger.debug(Joiner.on("\n").join(fileSet));
		for (File file : fileSet) {
			if (file.isDirectory()) {
				logger.debug("Identified directory: " + file.getName());
			}
		}
		
		// location of classes file, jar, or zip
		String inClassesDir = args[2];
		for (File file : fileSet) {
			single(file, args, outputDirName, inClassesDir);
		}

	}
	public static void single (File folder,String[] args,String outputDirName,String inClassesDir) throws Exception{
		if (folder.isDirectory()) {
			logger.debug("Processing directory: " + folder.getName());
			String revisionNumber = folder.getName();
			String fullClassesDir = folder.getAbsolutePath() + File.separatorChar + inClassesDir;
			//			Handle classes for cxf
//			String fullClassesDir = folder.getAbsolutePath().substring(0, folder.getAbsolutePath().length()-4).replace("src", "binary") + File.separatorChar + inClassesDir;
			//			Handle classes for camel
//			String fullClassesDir = folder.getAbsolutePath().replace("src", "bin") + File.separatorChar + inClassesDir;
			// Handle calasses for  wicket 6.0
//			String fullClassesDir = folder.getAbsolutePath().replace("src_2", "bin") + "-bin" + File.separatorChar + inClassesDir;		
			// Handle calasses for  nutch 
//			String fullClassesDir = folder.getAbsolutePath().replace("src", "bin") + "-bin" + File.separatorChar + inClassesDir;
			// Handle calasses for  openjpa 
//			String fullClassesDir = folder.getAbsolutePath().replace("src", "bin").replace("-source", "") + File.separatorChar + inClassesDir;
			
			File classesDirFile = new File(fullClassesDir);
			if (!classesDirFile.exists()) {
				throw new Exception ("classDir is not exist");
			}

			String depsRsfFilename = outputDirName + File.separatorChar + revisionNumber + "_deps.rsf";
			String[] builderArgs = { fullClassesDir, depsRsfFilename };
			File depsRsfFile = new File(depsRsfFilename);
			if (!depsRsfFile.getParentFile().exists())
				depsRsfFile.getParentFile().mkdirs();

			logger.debug("Get deps for revision " + revisionNumber);
			SourceToDepsBuilder builder = new JavaSourceToDepsBuilder();
			if (args.length == 4) {
				if (args[3].equals("c")) {
					builder = new CSourceToDepsBuilder();
				}
			}
			
			builder.build(builderArgs);
			if (builder.getEdges().size() == 0) {
				return;
			}
			 

			int numTopics = (int) ((double) builder.getNumSourceEntities() * 0.18);
			String fullSrcDir = folder.getAbsolutePath() + File.separatorChar;
			String topicModelFilename = outputDirName + File.separatorChar
					+ revisionNumber + "_" + numTopics + "_topics.mallet";
			String docTopicsFilename = outputDirName + File.separatorChar
					+ revisionNumber + "_" + numTopics + "_doc_topics.txt";
			String topWordsFilename = outputDirName + File.separatorChar
					+ revisionNumber + "_" + numTopics + "_top_words_per_topic.txt";
			
			if (args.length == 4) {
				if (args[3].equals("c")) {
					Config.selectedLanguage = Config.Language.c;
				}
			}
			
			ConcernClusteringRunner runner = new ConcernClusteringRunner(
					builder.getFfVecs(),
					TopicModelExtractionMethod.MALLET_API, fullSrcDir,
					outputDirName+"/base", numTopics, topicModelFilename, docTopicsFilename, topWordsFilename);

			// have to set some Config settings before executing the runner
			int numClusters = (int) ((double) runner.getFastClusters()
					.size() * .20); // number of clusters to obtain is based
									// on the number of entities
			Config.setNumClusters(numClusters);
			Config.stoppingCriterion = StoppingCriterionConfig.preselected;
			Config.setCurrSimMeasure(SimMeasure.js);
			runner.computeClustersWithConcernsAndFastClusters(new PreSelectedStoppingCriterion());

			String arcClustersFilename = outputDirName + File.separatorChar
					+ revisionNumber + "_" + numTopics + "_topics_"
					+ runner.getFastClusters().size() + "_arc_clusters.rsf";
			// need to build the map before writing the file
			HashMap<String, Integer> clusterNameToNodeNumberMap = ClusterUtil
					.createFastClusterNameToNodeNumberMap(runner
							.getFastClusters());
			ClusterUtil.writeFastClustersRsfFile(
					clusterNameToNodeNumberMap, runner.getFastClusters(),
					arcClustersFilename);

			Config.setSmellClustersFile(arcClustersFilename);
			Config.setDepsRsfFilename(depsRsfFile.getAbsolutePath());
			String detectedSmellsFilename = outputDirName + File.separatorChar
					+ revisionNumber + "_arc_smells.ser";
			String[] smellArgs = { depsRsfFile.getAbsolutePath(),
					arcClustersFilename, };

			// Need to provide docTopics first
			ArchSmellDetector.docTopics = TopicUtil.docTopics;
			ArchSmellDetector.tmeMethod = TopicModelExtractionMethod.MALLET_API;
			logger.debug("Running smell detecion for revision "
					+ revisionNumber);
			ArchSmellDetector.runAllDetectionAlgs(detectedSmellsFilename);
		}
	}

}

