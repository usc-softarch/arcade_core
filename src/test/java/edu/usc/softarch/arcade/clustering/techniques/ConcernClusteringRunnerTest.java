package edu.usc.softarch.arcade.clustering.techniques;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;

import edu.usc.softarch.arcade.clustering.FeatureVectors;
import edu.usc.softarch.arcade.clustering.SimilarityMatrix;
import edu.usc.softarch.arcade.clustering.criteria.PreSelectedStoppingCriterion;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import edu.usc.softarch.arcade.clustering.Architecture;

public class ConcernClusteringRunnerTest {
	@ParameterizedTest
	@CsvSource({
		/*** Test parameters: ***/ 
		// [directory with single system version's src files], 
		// [path to the pipe and mallet files are (one directory above base/)], 
		// [path to serialized objects (resources directory)],
		// [system version]
		// [system language]

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
		/* This test primarily verifies that initializeDocTopicsForEachFastCluster
		 * is making modifications to the internal structures of
		 * ConcernClusteringRunner. */
		/* Checks that ConcernClusteringRunner.fastFeatureVectors is not null after
		 * the ConcernClusteringRunner constructor call */
		/* Checks that ConcernClusteringRunner.fastClusters is modified in the
		 * ConcernClusteringRunner constructor */
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

		if (builderffVecs == null) {
			fail("failed to deserialize FastFeatureVectors from builder object");
		}
		
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

	@ParameterizedTest
	@CsvSource({
		/*** Test parameters: ***/ 
		// [directory with single system version's src files], 
		// [path to the pipe and mallet files are (one directory above base/)], 
		// [path to serialized objects (resources directory)],
		// [system version]
		// [system language]

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
		/* Checks whether fastClusters is modified by
		 * updateFastClustersAndSimMatrixToReflectMergedCluster */
		/* stopCriterion = clustergain is not currently in use */
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