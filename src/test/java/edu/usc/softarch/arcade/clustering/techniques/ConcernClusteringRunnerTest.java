package edu.usc.softarch.arcade.clustering.techniques;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import edu.usc.softarch.arcade.clustering.FastClusterArchitecture;
import edu.usc.softarch.arcade.clustering.FastFeatureVectors;

public class ConcernClusteringRunnerTest {
	ConcernClusteringRunner runner;
	@ParameterizedTest
	@CsvSource({
		/*** Test parameters: ***/ 
		// [directory with single system version's src files], 
		// [path to the pipe and mallet files are (one directory above base/)], 
		// [path to serialized objects (resources directory)],
		// [system version]
		// [system language]

		// struts 2.3.30
		".///src///test///resources///BatchClusteringEngineTest_resources///src///struts-2.3.30," // PLACE SRC FILES HERE
		+ ".///src///test///resources///mallet_resources///struts-2.3.30,"
		+ ".///src///test///resources///ConcernClusteringRunnerTest_resources,"
		+ "struts-2.3.30,"
		+ "java",

		// struts 2.5.2
		".///src///test///resources///BatchClusteringEngineTest_resources///src///struts-2.3.30," // PLACE SRC FILES HERE
		+ ".///src///test///resources///mallet_resources///struts-2.3.30,"
		+ ".///src///test///resources///ConcernClusteringRunnerTest_resources,"
		+ "struts-2.5.2,"
		+ "java",

		// httpd-2.3.8
		".///src///test///resources///CSourceToDepsBuilderTest_resources///src///httpd-2.3.8,"
		+ ".///src///test///resources///mallet_resources///httpd-2.3.8,"
		+ ".///src///test///resources///ConcernClusteringRunnerTest_resources,"
		+ "httpd-2.3.8,"
		+ "c",

		// httpd-2.4.26
		".///src///test///resources///CSourceToDepsBuilderTest_resources///src///httpd-2.4.26,"
		+ ".///src///test///resources///mallet_resources///httpd-2.4.26,"
		+ ".///src///test///resources///ConcernClusteringRunnerTest_resources,"
		+ "httpd-2.4.26,"
		+ "c",
	})
	public void initDataStructuresTest(String srcDir, String outDir, String resDir, String versionName, String language){
		/* Checks that ConcernClusteringRunner.fastFeatureVectors is not null after the ConcernClusteringRunner constructor call */
		/* Checks that ConcernClusteringRunner.fastClusters is modified in the ConcernClusteringRunner constructor */
		char fs = File.separatorChar;
		String outputPath = "." + fs + "target" + fs + "test_results" + fs + "ConcernClusteringRunnerTest";
		(new File(outputPath)).mkdirs();
		
		String fullSrcDir = srcDir.replace("///", File.separator);
		String outputDir = outDir.replace("///", File.separator);
		
		// Deserialize FastFeatureVectors object
		ObjectInputStream ois;
		FastFeatureVectors builderffVecs = null;
		try {
			ois = new ObjectInputStream(new FileInputStream(resDir + fs + "ffVecs_serialized" + fs + versionName + "_ffVecs_builder.txt"));
			builderffVecs = (FastFeatureVectors)ois.readObject();
			ois.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		if (builderffVecs == null){
			fail("failed to deserialize FastFeatureVectors from builder object");
		}
		
		// Construct a ConcernClusteringRunner object
		runner = new ConcernClusteringRunner(
			builderffVecs, fullSrcDir, outputDir + "/base", language);

		try {
			// Deserialize fastClusters from before initializeDocTopicsForEachFastCluster() call (wherein every node gets a cluster)
			ois = new ObjectInputStream(new FileInputStream(resDir + fs + "ds_serialized" + fs + versionName + "_fastClusters_before_init.txt"));
			FastClusterArchitecture fastClustersBefore = (FastClusterArchitecture) ois.readObject();
			ois.close();
			// Deserialize fastClusters from after initializeDocTopicsForEachFastCluster() call
			ois = new ObjectInputStream(new FileInputStream(resDir + fs + "ds_serialized" + fs + versionName + "_fastClusters_after_init.txt"));
			FastClusterArchitecture fastClustersAfterInit = (FastClusterArchitecture) ois.readObject();
			// System.out.println("fastClusters size before: " + fastClustersBefore.size());
			// System.out.println("fastClusters size after: " + fastClustersAfter.size());
			ois.close();


			assertFalse(fastClustersBefore.isEmpty()); // every node should get a cluster, so there should be at least one cluster?
			assertAll( // Only executed if the previous assertion passes
				() -> assertFalse(fastClustersAfterInit.isEmpty(), "fastClusters empty after initializeDocTopicsForEachFastCluster"),
				() -> assertNotEquals(fastClustersAfterInit, fastClustersBefore)
			);


			// Deserialize fastFeatureVectors from after setFastFeatureVectors() call
			ois = new ObjectInputStream(new FileInputStream(resDir + fs + "ds_serialized" + fs + versionName + "_fastfeatureVectors_init.txt"));
			FastFeatureVectors ffvInit = (FastFeatureVectors) ois.readObject();
			// Should not be null
			assertNotNull(ffvInit);
			ois.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

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
		".///src///test///resources///BatchClusteringEngineTest_resources///src///struts-2.3.30," // PLACE SRC FILES HERE
		+ ".///src///test///resources///mallet_resources///struts-2.3.30,"
		+ ".///src///test///resources///ConcernClusteringRunnerTest_resources,"
		+ "struts-2.3.30,"
		+ "java",

		// // struts 2.5.2
		// ".///src///test///resources///BatchClusteringEngineTest_resources///src///struts-2.3.30," // PLACE SRC FILES HERE
		// + ".///src///test///resources///mallet_resources///struts-2.3.30,"
		// + ".///src///test///resources///ConcernClusteringRunnerTest_resources,"
		// + "struts-2.5.2,"
		// + "java",

		// // httpd-2.3.8
		// ".///src///test///resources///BatchClusteringEngineTest_resources///src///httpd-2.3.8,"
		// + ".///src///test///resources///mallet_resources///httpd-2.3.8,"
		// + ".///src///test///resources///ConcernClusteringRunnerTest_resources,"
		// + "httpd-2.3.8,"
		// + "c",

		// // httpd-2.4.26
		// ".///src///test///resources///BatchClusteringEngineTest_resources///src///httpd-2.4.26,"
		// + ".///src///test///resources///mallet_resources///httpd-2.4.26,"
		// + ".///src///test///resources///ConcernClusteringRunnerTest_resources,"
		// + "httpd-2.4.26,"
		// + "c",
	})
	public void computeClustersWithConcernsAndFastClustersTest(String srcDir, String outDir, String resDir, String versionName, String language){
		// This function modifies fastClusters in its call to updateFastClustersAndSimMatrixToReflectMergedCluster()
		// is computeClustersWithConcernsAndFastClusters ever called with stopCriterion = "clustergain"? (I don't think this parameter affects the fastClusters anyway)

		char fs = File.separatorChar;
		String outputPath = "." + fs + "target" + fs + "test_results" + fs + "ConcernClusteringRunnerTest";
		(new File(outputPath)).mkdirs();
		
		String fullSrcDir = srcDir.replace("///", File.separator);
		String outputDir = outDir.replace("///", File.separator);
		
		// Deserialize FastFeatureVectors object
		ObjectInputStream ois;
		FastFeatureVectors builderffVecs = null;
		try {
			ois = new ObjectInputStream(new FileInputStream(resDir + fs + "ffVecs_serialized" + fs + versionName + "_ffVecs_builder.txt"));
			builderffVecs = (FastFeatureVectors)ois.readObject();
			ois.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		if (builderffVecs == null){
			fail("failed to deserialize FastFeatureVectors from builder object");
		}
		
		// Construct a ConcernClusteringRunner object
		runner = new ConcernClusteringRunner(
			builderffVecs, fullSrcDir, outputDir + "/base", language);
		// call computeClustersWithConcernsAndFastClusters()
		FastClusterArchitecture beforeCompute = ClusteringAlgoRunner.fastClusters;
		assertDoesNotThrow(() -> {
			int numClusters = (int) ((double) runner.getFastClusters().size() * .20); // copied from BatchClusteringEngine
			runner.computeClustersWithConcernsAndFastClusters(new ConcernClusteringRunner.PreSelectedStoppingCriterion(numClusters), "preselected", "js"); // copied from BatchClusteringEngine
		});

		// check fastClusters
		try {
			// Deserialize initial fastClusters
			ois = new ObjectInputStream(new FileInputStream(resDir + fs + "ds_serialized" + fs + versionName + "_fastClusters_after_init.txt"));
			FastClusterArchitecture fastClustersAfterInit = (FastClusterArchitecture) ois.readObject();
			ois.close();
			// Deserialize fastClusters from after computeClustersWithConcernsAndFastClusters() call
			ois = new ObjectInputStream(new FileInputStream(resDir + fs + "ds_serialized" + fs + versionName + "_fastClusters_after_compute.txt"));
			FastClusterArchitecture fastClustersCompute = (FastClusterArchitecture) ois.readObject();
			ois.close();

			FastClusterArchitecture afterCompute = ClusteringAlgoRunner.fastClusters;
			// Check that fastClusters not empty after computeClustersWithConcernsAndFastClusters call 
			assertFalse(fastClustersCompute.isEmpty());
			// The size of the fastClusters should be smaller afterward (I THINK)
			assertTrue(fastClustersCompute.size() < fastClustersAfterInit.size());
			// Check that the actual fastClusters before/after are different
			assertFalse(afterCompute.equals(beforeCompute));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
	}
}
