package edu.usc.softarch.arcade.clustering.techniques;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import edu.usc.softarch.arcade.clustering.FastClusterArchitecture;
import edu.usc.softarch.arcade.clustering.FastFeatureVectors;

// NOTE: run tests sequentially (need to figure out BeforeEach methods here later)
public class ConcernClusteringRunnerTest {
	ConcernClusteringRunner runner;
	// members to check: 
		// public static FastClusterArchitecture fastClusters;
		// protected static FastFeatureVectors fastFeatureVectors;
	@ParameterizedTest
	@CsvSource({
		/*** Test parameters: ***/ 
		// [directory with single system version's src files], 
		// [path to the pipe and mallet files are (one directory above base/)], 
		// [path to serialized objects (resources directory)],
		// [system version]
		// [system language]
		// ... (TBD)

		// struts 2.3.30
		".///src///test///resources///PipeExtractorTest_resources///src///struts-2.3.30," // PLACE SRC FILES HERE
		+ ".///src///test///resources///mallet_resources///struts-2.3.30,"
		+ ".///src///test///resources///ConcernClusteringRunnerTest_resources,"
		+ "struts-2.3.30,"
		+ "java",

		// // struts 2.5.2
		// ".///src///test///resources///PipeExtractorTest_resources///src///struts-2.3.30," // PLACE SRC FILES HERE
		// + ".///src///test///resources///mallet_resources///struts-2.3.30,"
		// + ".///src///test///resources///ConcernClusteringRunnerTest_resources///serialized,"
		// + "struts-2.5.2_output_ffVecs_before.txt,"
		// + "java",

		// // httpd-2.3.8
		// ".///src///test///resources///CSourceToDepsBuilderTest_resources///binaries///httpd-2.3.8,"
		// + ".///src///test///resources///mallet_resources///httpd-2.3.8,"
		// + ".///src///test///resources///ConcernClusteringRunnerTest_resources///serialized,"
		// + "httpd-2.3.8_output_ffVecs_before.txt,"
		// + "c",

		// // httpd-2.4.26
		// ".///src///test///resources///CSourceToDepsBuilderTest_resources///binaries///httpd-2.4.26,"
		// + ".///src///test///resources///mallet_resources///httpd-2.4.26,"
		// + ".///src///test///resources///ConcernClusteringRunnerTest_resources///serialized,"
		// + "httpd-2.4.26_output_ffVecs_before.txt,"
		// + "c",
	})
	public void initFastClustersTest(String srcDir, String outDir, String resDir, String versionName, String language){
		/* Checks that ConcernClusteringRunner.fastFeatureVectors is not null after the ConcernClusteringRunner constructor call */
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
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
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
			FastClusterArchitecture fastClustersAfter= (FastClusterArchitecture) ois.readObject();
			System.out.println("clusters size before: " + fastClustersBefore.size());
			System.out.println("clusters size after: " + fastClustersAfter.size());
			assertNotEquals(fastClustersAfter, fastClustersBefore);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	

	// TEST METHODS
	// updateFastClustersAndSimMatrixToReflectMergedCluster - called by computeClustersWithConcernsAndFastClusters 
	// mergeFastClustersUsingTopics

	// updateFastClustersAndSimMatrixToReflectMergedCluster
	// identifyMostSimClusters
	// computeClustersWithConcernsAndFastClusters -> computeClusters

}
