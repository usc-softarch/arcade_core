package edu.usc.softarch.arcade.clustering.techniques;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import edu.usc.softarch.arcade.clustering.FastFeatureVectors;


public class ConcernClusteringRunnerTest {
	// members to check: 
		// public static FastClusterArchitecture fastClusters;
		// protected static FastFeatureVectors fastFeatureVectors;
	@ParameterizedTest
	@CsvSource({
		/*** Test parameters: ***/ 
		// [directory with single system version's src files], 
		// [where the pipe and mallet files are (one above base/)], 
		// [location of serialized FastFeatureVectors], 
		// [file with serialized FastFeatureVectors],
		// [system language]
		// ... (TBD)

		// struts 2.3.30
		".///src///test///resources///PipeExtractorTest_resources///src///struts-2.3.30," // PLACE SRC FILES HERE
		+ ".///src///test///resources///mallet_resources///struts-2.3.30,"
		+ ".///src///test///resources///ConcernClusteringRunnerTest_resources///serialized,"
		+ "struts-2.3.30_output_ffVecs_before.txt,"
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
	public void initializeDocTopicsForEachFastClusterTest(String srcDir, String outDir, String resDir, String ffvName, String language){
		char fs = File.separatorChar;
		String outputPath = "." + fs + "target" + fs + "test_results" + fs + "ConcernClusteringRunnerTest";
		(new File(outputPath)).mkdirs();
		
		String fullSrcDir = srcDir.replace("///", File.separator);
		String outputDir = outDir.replace("///", File.separator);
		
		// Deserialize FastFeatureVectors object
		ObjectInputStream ois;
		FastFeatureVectors oisffVecs = null;
		try {
			ois = new ObjectInputStream(new FileInputStream(resDir + fs + ffvName));
			oisffVecs = (FastFeatureVectors)ois.readObject();
			ois.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		if (oisffVecs == null){
			fail("failed to deserialize FastFeatureVectors");
		}
		
		// Construct a ConcernClusteringRunner object
		ConcernClusteringRunner runner = new ConcernClusteringRunner(
			oisffVecs, fullSrcDir, outputDir + "/base", language); // calls initializeDocTopicsForEachFastCluster
		
	}

	// TEST METHODS
	// updateFastClustersAndSimMatrixToReflectMergedCluster - called by computeClustersWithConcernsAndFastClusters 
	// initializeDocTopicsForEachFastCluster


}
