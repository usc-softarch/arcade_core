package edu.usc.softarch.arcade.clustering.drivers;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import edu.usc.softarch.arcade.antipattern.detection.ArchSmellDetector;
import edu.usc.softarch.arcade.clustering.techniques.ConcernClusteringRunner;
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
	private static Logger logger =
		LogManager.getLogger(BatchClusteringEngine.class);

	public static void main(String[] args) throws Exception {
		// directory where each subdirectory is a different version or revision of
		// the system you want to analyze
		String inputDirName = args[0];
		File inputDir = new File(FileUtil.tildeExpandPath(inputDirName));
		
		// directory where all the output will go for every version or revision
		String outputDirName = args[1];
		
		File[] files = inputDir.listFiles();
		Set<File> fileSet = new TreeSet<>(Arrays.asList(files));
		List<String> fileSetNames =
			fileSet.stream().map(File::toString).collect(Collectors.toList());

		// Logging
		logger.debug("All files in " + inputDir + ":");
		logger.debug(String.join("\n", fileSetNames));
		for (File file : fileSet)
			if (file.isDirectory())
				logger.debug("Identified directory: " + file.getName());
		
		// location of classes file, jar, or zip
		String inClassesDir = args[2];
		String language = "java";
		if (args.length == 4 && args[3].equals("c"))
			language = "c";

		for (File file : fileSet)
			single(file, language, outputDirName, inClassesDir);
	}
	
	/**
	 * Runs a recovery for a single version of a system.
	 * 
	 * @param folder Directory containing that system version.
	 * @param language Source language of the system.
	 * @param outputDirName Path to the directory that will contain the output.
	 * @param inClassesDir Name of the directory containing the binaries.
	 */
	public static void single (File folder, String language, String outputDirName, 
			String inClassesDir) throws IOException {
		// Check that this is a valid version directory.
		if (folder.isFile()) return;

		// Set up variables
		String fs = File.separator;
		logger.debug("Processing directory: " + folder.getName());
		String revisionNumber = folder.getName();
		String fullClassesDir =	folder.getAbsolutePath() + fs + inClassesDir;
		String fullSrcDir = folder.getAbsolutePath() + fs;
		
		// Ensure binaries directory exists
		File classesDirFile = new File(fullClassesDir);
		if (!classesDirFile.exists())
			throw new IOException("Could not find classDir at given path.");

		// Ensure output directory exists
		String depsRsfFilename = outputDirName + fs + revisionNumber + "_deps.rsf";
		File depsRsfFile = new File(depsRsfFilename);
		depsRsfFile.getParentFile().mkdirs();

		// Fact extraction
		logger.debug("Get deps for revision " + revisionNumber);
		SourceToDepsBuilder builder = new JavaSourceToDepsBuilder();
		if (language.equals("c"))	builder = new CSourceToDepsBuilder();
		builder.build(fullClassesDir, depsRsfFilename);
		if (builder.getEdges().isEmpty()) return;

		// Set the number of topics to be used in clustering
		int numTopics = (int) ((double) builder.getNumSourceEntities() * 0.18);
		
		ConcernClusteringRunner runner = new ConcernClusteringRunner(
			builder.getFfVecs(), fullSrcDir, outputDirName + "/base", language);

		// have to set some Config settings before executing the runner
		// number of clusters to obtain is based on the number of entities
		int numClusters = (int) ((double) runner.getFastClusters().size() * .20);
		Config.stoppingCriterion = StoppingCriterionConfig.preselected;
		Config.setCurrSimMeasure(SimMeasure.js);
		runner.computeClustersWithConcernsAndFastClusters(
			new ConcernClusteringRunner.PreSelectedStoppingCriterion(numClusters));

		String arcClustersFilename = outputDirName + fs	+ revisionNumber + "_"
			+ numTopics + "_topics_" + runner.getFastClusters().size()
			+ "_arc_clusters.rsf";
		// need to build the map before writing the file
		Map<String, Integer> clusterNameToNodeNumberMap =
			runner.getFastClusters().createFastClusterNameToNodeNumberMap();
		runner.getFastClusters().writeFastClustersRsfFile(
			clusterNameToNodeNumberMap, arcClustersFilename);

		String detectedSmellsFilename = outputDirName + fs + revisionNumber
			+ "_arc_smells.ser";

		// Need to provide docTopics first
		logger.debug("Running smell detecion for revision "	+ revisionNumber);
		ArchSmellDetector asd = new ArchSmellDetector(
			depsRsfFile.getAbsolutePath(), arcClustersFilename,
			detectedSmellsFilename, language,
			TopicModelExtractionMethod.MALLET_API, TopicUtil.docTopics);
		asd.run(true, true, true);
	}
}