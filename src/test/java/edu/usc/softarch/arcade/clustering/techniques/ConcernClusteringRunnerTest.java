package edu.usc.softarch.arcade.clustering.techniques;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import edu.usc.softarch.arcade.antipattern.SmellCollection;
import edu.usc.softarch.arcade.antipattern.detection.ArchSmellDetector;
import edu.usc.softarch.arcade.clustering.FeatureVectors;
import edu.usc.softarch.arcade.clustering.SimilarityMatrix;
import edu.usc.softarch.arcade.clustering.criteria.PreSelectedStoppingCriterion;
import edu.usc.softarch.arcade.topics.DocTopics;
import edu.usc.softarch.arcade.topics.TopicModelExtractionMethod;
import edu.usc.softarch.arcade.topics.TopicUtil;
import edu.usc.softarch.arcade.util.FileUtil;
import edu.usc.softarch.arcade.util.RsfCompare;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import edu.usc.softarch.arcade.clustering.Architecture;

public class ConcernClusteringRunnerTest {
	/**
	 * Tests ARC recovery for a single version of a system.
	 *
	 * @param sysVersionDir Dir with single system version.
	 * @param lang System language.
	 * @param artifactsDirPath Test file output dir name. IMPORTANT: should also contain base/output.pipe and base/infer.mallet.
	 * @param ffVecsFilepath Path to serialized FeatureVectors.
	 * @param oraclePath Path to oracle file.
	 * @param arcFilename Expected clusters file name.
	 * @param outputDirPath Path to place execution output.
	 */
	@ParameterizedTest
	@CsvSource({
		// struts 2.3.30
		".///src///test///resources///subject_systems_resources///Struts2///src///struts-2.3.30,"
			+ "java,"
			+ ".///src///test///resources///BatchClusteringEngineTest_resources///struts-2.3.30,"
			+ ".///src///test///resources///BatchClusteringEngineTest_resources///struts-2.3.30_ffVecs.json,"
			+ ".///src///test///resources///BatchClusteringEngineTest_resources///struts-2.3.30_239_topics_234_arc_clusters.rsf,"
			+ "struts-2.3.30_239_topics_234_arc_clusters.rsf,"
			+ ".///target///test_results///BatchClusteringEngineTest",

		// struts 2.5.2
		".///src///test///resources///subject_systems_resources///Struts2///src///struts-2.5.2,"
			+ "java,"
			+ ".///src///test///resources///BatchClusteringEngineTest_resources///struts-2.5.2,"
			+ ".///src///test///resources///BatchClusteringEngineTest_resources///struts-2.5.2_ffVecs.json,"
			+ ".///src///test///resources///BatchClusteringEngineTest_resources///struts-2.5.2_284_topics_275_arc_clusters.rsf,"
			+ "struts-2.5.2_284_topics_275_arc_clusters.rsf,"
			+ ".///target///test_results///BatchClusteringEngineTest",

		// httpd 2.3.8
		".///src///test///resources///subject_systems_resources///httpd///src///httpd-2.3.8,"
			+ "c,"
			+ ".///src///test///resources///BatchClusteringEngineTest_resources///httpd-2.3.8,"
			+ ".///src///test///resources///BatchClusteringEngineTest_resources///httpd-2.3.8_ffVecs.json,"
			+ ".///src///test///resources///BatchClusteringEngineTest_resources///httpd-2.3.8_46_topics_71_arc_clusters.rsf,"
			+ "httpd-2.3.8_46_topics_71_arc_clusters.rsf,"
			+ ".///target///test_results///BatchClusteringEngineTest",

		// httpd 2.4.26
		".///src///test///resources///subject_systems_resources///httpd///src///httpd-2.4.26,"
			+ "c,"
			+ ".///src///test///resources///BatchClusteringEngineTest_resources///httpd-2.4.26,"
			+ ".///src///test///resources///BatchClusteringEngineTest_resources///httpd-2.4.26_ffVecs.json,"
			+ ".///src///test///resources///BatchClusteringEngineTest_resources///httpd-2.4.26_50_topics_82_arc_clusters.rsf,"
			+ "httpd-2.4.26_50_topics_82_arc_clusters.rsf,"
			+ ".///target///test_results///BatchClusteringEngineTest"
	})
	public void ARCRecoveryTest(String sysVersionDir, String lang,
		String artifactsDirPath, String ffVecsFilepath, String oraclePath,
		String arcFilename, String outputDirPath) {
		// Format paths
		String sysDir = sysVersionDir.replace("///", File.separator);
		String artifactsDir = artifactsDirPath.replace("///", File.separator);
		String oracleFilePath = oraclePath.replace("///", File.separator);
		String ffVecs = ffVecsFilepath.replace("///", File.separator);
		String outputDirName = outputDirPath.replace("///", File.separator);

		assertDoesNotThrow(() ->
			ConcernClusteringRunner.runARC(lang, outputDirName, sysDir, ffVecs, artifactsDir));

		// Result file with clusters
		String resultClustersFile = outputDirName + File.separator + arcFilename;

		/* The expectation here is that this resulting clusters file has the same
		 * name as the oracle clusters file, meaning it has the same number of
		 * clusters and topics. */
		assertTrue(new File(resultClustersFile).exists(),
			"resulting clusters file name does not match oracle clusters file name:"
				+ resultClustersFile);

		String result = assertDoesNotThrow(() ->
			FileUtil.readFile(resultClustersFile, StandardCharsets.UTF_8));

		// Load oracle
		String oracle = assertDoesNotThrow(() ->
			FileUtil.readFile((oracleFilePath), StandardCharsets.UTF_8));

		// RsfCompare.equals() to compare contents of oracle and result files
		RsfCompare resultRsf = new RsfCompare(result);
		RsfCompare oracleRsf = new RsfCompare(oracle);
		assertEquals(oracleRsf, resultRsf);
	}

	/**
	 * ARC - smell analyzer integration test.
	 *
	 * @param depsRsfFilePath Path to dependencies RSF input file.
	 * @param lang System language.
	 * @param clusterFilePath Path to place the clusters RSF file output.
	 * @param docTopicsFilePath Path to DocTopics file input.
	 * @param oraclePath Path to oracle file.
	 * @param arcFilename Expected clusters file name.
	 */
	@ParameterizedTest
	@CsvSource({
		// struts 2.3.30
		".///src///test///resources///BatchClusteringEngineTest_resources///struts-2.3.30_deps.rsf,"
			+ "java,"
			+ ".///src///test///resources///BatchClusteringEngineTest_resources///struts-2.3.30_239_topics_234_arc_clusters.rsf,"
			+ ".///src///test///resources///BatchClusteringEngineTest_resources///struts-2.3.30_239_topics_234_arc_docTopics.json,"
			+ ".///src///test///resources///BatchClusteringEngineTest_resources///struts-2.3.30_arc_smells.ser,"
			+ ".///target///test_results///BatchClusteringEngineTest///struts-2.3.30_arc_smells.ser",

		// struts 2.5.2
		".///src///test///resources///BatchClusteringEngineTest_resources///struts-2.5.2_deps.rsf,"
			+ "java,"
			+ ".///src///test///resources///BatchClusteringEngineTest_resources///struts-2.5.2_284_topics_275_arc_clusters.rsf,"
			+ ".///src///test///resources///BatchClusteringEngineTest_resources///struts-2.5.2_284_topics_275_arc_docTopics.json,"
			+ ".///src///test///resources///BatchClusteringEngineTest_resources///struts-2.5.2_arc_smells.ser,"
			+ ".///target///test_results///BatchClusteringEngineTest///struts-2.5.2_arc_smells.ser",

		// httpd 2.3.8
		".///src///test///resources///BatchClusteringEngineTest_resources///httpd-2.3.8_deps.rsf,"
			+ "c,"
			+ ".///src///test///resources///BatchClusteringEngineTest_resources///httpd-2.3.8_46_topics_71_arc_clusters.rsf,"
			+ ".///src///test///resources///BatchClusteringEngineTest_resources///httpd-2.3.8_46_topics_71_arc_docTopics.json,"
			+ ".///src///test///resources///BatchClusteringEngineTest_resources///httpd-2.3.8_arc_smells.ser,"
			+ ".///target///test_results///BatchClusteringEngineTest///httpd-2.3.8_arc_smells.ser",

		// httpd 2.4.26
		".///src///test///resources///BatchClusteringEngineTest_resources///httpd-2.4.26_deps.rsf,"
			+ "c,"
			+ ".///src///test///resources///BatchClusteringEngineTest_resources///httpd-2.4.26_50_topics_82_arc_clusters.rsf,"
			+ ".///src///test///resources///BatchClusteringEngineTest_resources///httpd-2.4.26_50_topics_82_arc_docTopics.json,"
			+ ".///src///test///resources///BatchClusteringEngineTest_resources///httpd-2.4.26_arc_smells.ser,"
			+ ".///target///test_results///BatchClusteringEngineTest///httpd-2.4.26_arc_smells.ser",
	})
	public void asdWithConcernsTest(String depsRsfFilePath, String lang,
		String clusterFilePath, String docTopicsFilePath, String oraclePath,
		String arcFilename) {
		// Format paths
		String depsRsfFile = depsRsfFilePath.replace("///", File.separator);
		String docTopicsPath = docTopicsFilePath.replace("///", File.separator);
		String oracleFilePath = oraclePath.replace("///", File.separator);
		String clusterFile = clusterFilePath.replace("///", File.separator);
		String resultFile = arcFilename.replace("///", File.separator);

		assertDoesNotThrow(() -> {
			TopicUtil.docTopics = DocTopics.deserializeDocTopics(docTopicsPath);
			ArchSmellDetector asd = new ArchSmellDetector(
				depsRsfFile, clusterFile, resultFile, lang,
				TopicModelExtractionMethod.MALLET_API, TopicUtil.docTopics);
			asd.run(true, true, true);
		});

		// Construct SmellCollection objects out of the oracle and result files
		SmellCollection resultSmells = assertDoesNotThrow(() -> new SmellCollection(resultFile));
		SmellCollection oracleSmells = assertDoesNotThrow(() -> new SmellCollection(oracleFilePath));

		// SmellCollection extends HashSet, so we can use equals() to compare the result to the oracle
		assertEquals(oracleSmells, resultSmells);
	}

	/**
	 * This test primarily verifies that initializeClusterDocTopics is making
	 * modifications to the internal structures of ConcernClusteringRunner.
	 * Checks that ConcernClusteringRunner.featureVectors is not null after the
	 * ConcernClusteringRunner constructor call. Checks that
	 * ConcernClusteringRunner.architecture is modified in the
	 * ConcernClusteringRunner constructor.
	 *
	 * @param srcDir Directory with single system version's src files.
	 * @param outDir Path to the pipe and mallet files are (one directory above base/).
	 * @param resDir Path to serialized objects (resources directory).
	 * @param versionName System version.
	 * @param language System language.
	 */
	@ParameterizedTest
	@CsvSource({
		// struts 2.3.30
		".///src///test///resources///subject_systems_resources///Struts2///src///struts-2.3.30,"
		+ ".///src///test///resources///ConcernClusteringRunnerTest_resources///struts-2.3.30,"
		+ ".///src///test///resources///ConcernClusteringRunnerTest_resources,"
		+ "struts-2.3.30,"
		+ "java",

		// struts 2.5.2
		".///src///test///resources///subject_systems_resources///Struts2///src///struts-2.5.2,"
		+ ".///src///test///resources///ConcernClusteringRunnerTest_resources///struts-2.5.2,"
		+ ".///src///test///resources///ConcernClusteringRunnerTest_resources,"
		+ "struts-2.5.2,"
		+ "java",

		// httpd-2.3.8
		".///src///test///resources///subject_systems_resources///httpd///src///httpd-2.3.8,"
		+ ".///src///test///resources///ConcernClusteringRunnerTest_resources///httpd-2.3.8,"
		+ ".///src///test///resources///ConcernClusteringRunnerTest_resources,"
		+ "httpd-2.3.8,"
		+ "c",

		// httpd-2.4.26
		".///src///test///resources///subject_systems_resources///httpd///src///httpd-2.4.26,"
		+ ".///src///test///resources///ConcernClusteringRunnerTest_resources///httpd-2.4.26,"
		+ ".///src///test///resources///ConcernClusteringRunnerTest_resources,"
		+ "httpd-2.4.26,"
		+ "c",
	})
	public void initDataStructuresTest(String srcDir, String outDir,
			String resDir, String versionName, String language) {
		char fs = File.separatorChar;
		String outputPath = "." + fs + "target" + fs + "test_results" + fs
			+ "ConcernClusteringRunnerTest" + fs + "ds_serialized";
		(new File(outputPath)).mkdirs();
		
		String fullSrcDir = srcDir.replace("///", File.separator);
		String outputDir = outDir.replace("///", File.separator);
		
		// Deserialize FastFeatureVectors oracle
		String ffVecsFilePath = resDir + fs	+ "ffVecs_serialized" + fs
			+ versionName + "_ffVecs.json";
		FeatureVectors builderffVecs = null;

		try {
			builderffVecs = FeatureVectors.deserializeFFVectors(ffVecsFilePath);
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		if (builderffVecs == null)
			fail("failed to deserialize FastFeatureVectors from builder object");
		
		// Construct a ConcernClusteringRunner object
		ConcernClusteringRunner runner = new ConcernClusteringRunner(
			builderffVecs, fullSrcDir, outputDir + "/base", language);

		/* Tests whether fastClusters was altered by
		 * initializeDocTopicsForEachFastCluster */
		Architecture fastClustersBefore = runner.getInitialArchitecture();
		Architecture fastClustersAfterInit = runner.getArchitectureWithDocTopics();
		System.out.println("fastClusters size before: " + fastClustersBefore.size());
		System.out.println("fastClusters size after: " + fastClustersAfterInit.size());
		// every node should get a cluster, so there should be at least one cluster
		assertFalse(fastClustersBefore.isEmpty());
		assertAll(
			() -> assertFalse(fastClustersAfterInit.isEmpty(),
				"fastClusters empty after initializeDocTopicsForEachFastCluster"),
			() -> assertNotEquals(fastClustersAfterInit, fastClustersBefore)
		);

		/* Tests whether fastFeatureVectors was filled out by
		 * initializeDocTopicsForEachFastCluster */
		FeatureVectors ffvInit = runner.featureVectors;
		// Should not be null
		assertFalse(ffvInit.getFeatureVectorNames().isEmpty());
	}

	/**
	 * Checks whether fastClusters is modified by
	 * updateFastClustersAndSimMatrixToReflectMergedCluster.
	 * stopCriterion = clustergain is not currently in use.
	 *
	 * @param srcDir Directory with single system version's src files.
	 * @param outDir Path to the pipe and mallet files are (one directory above base/).
	 * @param resDir Path to serialized objects (resources directory).
	 * @param versionName System version.
	 * @param language System language.
	 */
	@ParameterizedTest
	@CsvSource({
		// struts 2.3.30
		".///src///test///resources///subject_systems_resources///Struts2///src///struts-2.3.30,"
		+ ".///src///test///resources///ConcernClusteringRunnerTest_resources///struts-2.3.30,"
		+ ".///src///test///resources///ConcernClusteringRunnerTest_resources,"
		+ "struts-2.3.30,"
		+ "java",

		// struts 2.5.2
		".///src///test///resources///subject_systems_resources///Struts2///src///struts-2.5.2,"
		+ ".///src///test///resources///ConcernClusteringRunnerTest_resources///struts-2.5.2,"
		+ ".///src///test///resources///ConcernClusteringRunnerTest_resources,"
		+ "struts-2.5.2,"
		+ "java",

		// httpd-2.3.8
		".///src///test///resources///subject_systems_resources///httpd///src///httpd-2.3.8,"
		+ ".///src///test///resources///ConcernClusteringRunnerTest_resources///httpd-2.3.8,"
		+ ".///src///test///resources///ConcernClusteringRunnerTest_resources,"
		+ "httpd-2.3.8,"
		+ "c",

		// httpd-2.4.26
		".///src///test///resources///subject_systems_resources///httpd///src///httpd-2.4.26,"
		+ ".///src///test///resources///ConcernClusteringRunnerTest_resources///httpd-2.4.26,"
		+ ".///src///test///resources///ConcernClusteringRunnerTest_resources,"
		+ "httpd-2.4.26,"
		+ "c",
	})
	public void computeClustersWithConcernsAndFastClustersTest(String srcDir,
			String outDir, String resDir, String versionName, String language) {
		char fs = File.separatorChar;
		String outputPath = "." + fs + "target" + fs + "test_results" + fs
			+ "ConcernClusteringRunnerTest";
		(new File(outputPath)).mkdirs();
		
		String fullSrcDir = srcDir.replace("///", File.separator);
		String outputDir = outDir.replace("///", File.separator);
		
		// Deserialize FastFeatureVectors oracle
		String ffVecsFilePath = resDir + fs	+ "ffVecs_serialized" + fs
			+ versionName + "_ffVecs.json";
		FeatureVectors builderffVecs = null;

		try {
			builderffVecs = FeatureVectors.deserializeFFVectors(ffVecsFilePath);
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		if (builderffVecs == null){
			fail("failed to deserialize FastFeatureVectors from builder object");
		}
		
		// Construct a ConcernClusteringRunner object
		ConcernClusteringRunner runner = new ConcernClusteringRunner(
			builderffVecs, fullSrcDir, outputDir + "/base", language);
		// call computeClustersWithConcernsAndFastClusters()
		assertDoesNotThrow(() -> {
			// copied from BatchClusteringEngine
			int numClusters = (int) ((double) runner.getArchitecture().size() * .20);
			// USING THE CLONE THAT TAKES IN THE VERSION NAME HERE
			runner.computeArchitecture(
				new PreSelectedStoppingCriterion(numClusters, runner),
				"preselected", SimilarityMatrix.SimMeasure.JS);
		});

		Architecture fastClustersAfterInit = runner.getArchitectureWithDocTopics();
		Architecture fastClustersCompute = runner.getArchitecture();
		// Check that fastClusters not empty after computeClustersWithConcernsAndFastClusters call 
		assertFalse(fastClustersCompute.isEmpty());
		// The size of the fastClusters should be smaller afterward
		assertTrue(fastClustersCompute.size() < fastClustersAfterInit.size());
	}
}