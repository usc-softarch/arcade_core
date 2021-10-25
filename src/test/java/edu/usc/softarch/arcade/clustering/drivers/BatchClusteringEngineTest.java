package edu.usc.softarch.arcade.clustering.drivers;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import edu.usc.softarch.arcade.antipattern.SmellCollection;
import edu.usc.softarch.arcade.antipattern.detection.ArchSmellDetector;
import edu.usc.softarch.arcade.clustering.techniques.ConcernClusteringRunner;
import edu.usc.softarch.arcade.topics.DocTopics;
import edu.usc.softarch.arcade.topics.TopicModelExtractionMethod;
import edu.usc.softarch.arcade.topics.TopicUtil;
import edu.usc.softarch.arcade.util.FileUtil;
import edu.usc.softarch.arcade.util.RsfCompare;

public class BatchClusteringEngineTest {
	@ParameterizedTest
	@CsvSource({
		/*** Test parameters: ***/
		// [dir with single system version], 
		// [system language], 
		// [test file output dir name], (IMPORTANT: should also contain base/output.pipe and base/infer.mallet)
		// [subject system binaries location], 
		// [path to oracle file], 
		// [expected clusters file name]

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
		/** Tests ARC recovery for a single version of a system **/
		// Format paths
		String sysDir = sysVersionDir.replace("///", File.separator);
		String artifactsDir = artifactsDirPath.replace("///", File.separator);
		String oracleFilePath = oraclePath.replace("///", File.separator);
		String ffVecs = ffVecsFilepath.replace("///", File.separator);
		String outputDirName = outputDirPath.replace("///", File.separator);

		assertDoesNotThrow(() -> {
			ConcernClusteringRunner.runARC(lang, outputDirName, sysDir,
				ffVecs, artifactsDir);
		});

		// Result file with clusters
		String resultClustersFile = outputDirName + File.separator + arcFilename;

		/* The expectation here is that this resulting clusters file has the same
		 * name as the oracle clusters file, meaning it has the same number of
		 * clusters and topics. */
		assertTrue(new File(resultClustersFile).exists(),
			"resulting clusters file name does not match oracle clusters file name:"
			+ resultClustersFile);

		String result = assertDoesNotThrow(() -> {
			return FileUtil.readFile(resultClustersFile, StandardCharsets.UTF_8);
		});

		// Load oracle
		String oracle = assertDoesNotThrow(() -> {
			return FileUtil.readFile((oracleFilePath), StandardCharsets.UTF_8); 
		});

		// RsfCompare.equals() to compare contents of oracle and result files
		RsfCompare resultRsf = new RsfCompare(result);
		RsfCompare oracleRsf = new RsfCompare(oracle);
		assertEquals(oracleRsf, resultRsf);
	}

	@ParameterizedTest
	@CsvSource({
		/*** Test parameters: ***/
		// [dir with single system version], 
		// [system language], 
		// [test file output dir name], (IMPORTANT: must also contain base/output.pipe and base/infer.mallet)
		// [system binaries location], 
		// [path to oracle file], 
		// [expected clusters file name]

		// struts 2.3.30
		".///src///test///resources///BatchClusteringEngineTest_resources///struts-2.3.30_deps.rsf,"
		+ "java,"
		+ ".///src///test///resources///BatchClusteringEngineTest_resources///struts-2.3.30_239_topics_234_arc_clusters.rsf,"
		+ ".///src///test///resources///BatchClusteringEngineTest_resources///struts-2.3.30_239_topics_234_arc_docTopics.json,"
		+ "lib_struts,"
		+ ".///src///test///resources///BatchClusteringEngineTest_resources///struts-2.3.30_arc_smells.ser,"
		+ ".///target///test_results///BatchClusteringEngineTest///struts-2.3.30_arc_smells.ser",

		// struts 2.5.2
		".///src///test///resources///BatchClusteringEngineTest_resources///struts-2.5.2_deps.rsf,"
		+ "java,"
		+ ".///src///test///resources///BatchClusteringEngineTest_resources///struts-2.5.2_284_topics_275_arc_clusters.rsf,"
		+ ".///src///test///resources///BatchClusteringEngineTest_resources///struts-2.5.2_284_topics_275_arc_docTopics.json,"
		+ "lib_struts,"
		+ ".///src///test///resources///BatchClusteringEngineTest_resources///struts-2.5.2_arc_smells.ser,"
		+ ".///target///test_results///BatchClusteringEngineTest///struts-2.5.2_arc_smells.ser",

		// httpd 2.3.8
		".///src///test///resources///BatchClusteringEngineTest_resources///httpd-2.3.8_deps.rsf,"
		+ "c,"
		+ ".///src///test///resources///BatchClusteringEngineTest_resources///httpd-2.3.8_46_topics_71_arc_clusters.rsf,"
		+ ".///src///test///resources///BatchClusteringEngineTest_resources///httpd-2.3.8_46_topics_71_arc_docTopics.json,"
		+ "/,"
		+ ".///src///test///resources///BatchClusteringEngineTest_resources///httpd-2.3.8_arc_smells.ser,"
		+ ".///target///test_results///BatchClusteringEngineTest///httpd-2.3.8_arc_smells.ser",

		// httpd 2.4.26
		".///src///test///resources///BatchClusteringEngineTest_resources///httpd-2.4.26_deps.rsf,"
		+ "c,"
		+ ".///src///test///resources///BatchClusteringEngineTest_resources///httpd-2.4.26_50_topics_82_arc_clusters.rsf,"
		+ ".///src///test///resources///BatchClusteringEngineTest_resources///httpd-2.4.26_50_topics_82_arc_docTopics.json,"
		+ "/,"
		+ ".///src///test///resources///BatchClusteringEngineTest_resources///httpd-2.4.26_arc_smells.ser,"
		+ ".///target///test_results///BatchClusteringEngineTest///httpd-2.4.26_arc_smells.ser",
	})
	public void asdWithConcernsTest(String depsRsfFilePath, String lang,
			String clusterFilePath, String docTopicsFilePath, String classesDir,
			String oraclePath, String arcFilename) {
		/** ARC - smell analyzer integration test **/
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
		SmellCollection resultSmells = assertDoesNotThrow(() -> { return new SmellCollection(resultFile); });
		SmellCollection oracleSmells = assertDoesNotThrow(() -> { return new SmellCollection(oracleFilePath); });

		// SmellCollection extends HashSet, so we can use equals() to compare the result to the oracle
		assertEquals(oracleSmells, resultSmells);
	}
}