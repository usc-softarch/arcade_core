package edu.usc.softarch.arcade.antipattern.detection;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.File;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import edu.usc.softarch.arcade.antipattern.SmellCollection;
import edu.usc.softarch.arcade.clustering.acdc.ACDC;

public class SmellsCompareTest{
	@BeforeEach
	public void setUp(){
		char fs = File.separatorChar;
		String outputPath = "." + fs + "target" + fs + "test_results" + fs + "SmellsCompareTest";
		File directory = new File(outputPath);
		directory.mkdirs();
	}

	@ParameterizedTest
	@CsvSource({
		/** Test parameters **/
		// [path to oracle]
		// [path to deps rsf]
		// [output directory]
		// [output path for clusters file]
		// [desired result ser filename]

		// struts 2.3.30
		".///src///test///resources///SmellsCompareTest_resources///edited_struts-2.3.30_acdc_smells.ser,"
		+ ".///src///test///resources///JavaSourceToDepsBuilderTest_resources///struts-2.3.30_deps.rsf,"
		+ ".///target///test_results///SmellsCompareTest,"
		+ ".///target///test_results///SmellsCompareTest///struts-2.3.30_acdc_clusters.rsf,"
		+ "struts-2.3.30_acdc_smells.ser",

		// // struts-2.5.2
		// ".///src///test///resources///SmellsCompareTest_resources///struts-2.5.2_acdc_smells.ser,"
		// + ".///src///test///resources///JavaSourceToDepsBuilderTest_resources///struts-2.5.2_deps.rsf,"
		// + ".///target///test_results///SmellsCompareTest,"
		// + ".///target///test_results///SmellsCompareTest///struts-2.5.2_acdc_clusters.rsf,"
		// + "struts-2.5.2_acdc_smells.ser",

		// // httpd 2.3.8
		// ".///src///test///resources///SmellsCompareTest_resources///edited_httpd-2.3.8_acdc_smells.ser,"
		// + ".///src///test///resources///CSourceToDepsBuilderTest_resources///httpd-2.3.8_deps.rsf,"
		// + ".///target///test_results///SmellsCompareTest,"
		// + ".///target///test_results///SmellsCompareTest///httpd-2.3.8_acdc_clusters.rsf,"
		// + "httpd-2.3.8_acdc_smells.ser",

		// // httpd-2.4.26
		// ".///src///test///resources///SmellsCompareTest_resources///httpd-2.4.26_acdc_smells.ser,"
		// + ".///src///test///resources///CSourceToDepsBuilderTest_resources///httpd-2.4.26_deps.rsf,"
		// + ".///target///test_results///SmellsCompareTest,"
		// + ".///target///test_results///SmellsCompareTest///httpd-2.4.26_acdc_clusters.rsf,"
		// + "httpd-2.4.26_acdc_smells.ser",
	})
	public void withoutConcernsTest(String oracle, String deps, String output, String clusters, String ser){    
		/** Run smell analyzer without concerns (ACDC) **/
		String oraclePath = oracle.replace("///", File.separator);  
		String depsPath = deps.replace("///", File.separator);
		String outputPath = output.replace("///", File.separator);
		String outputClustersPath = clusters.replace ("///", File.separator);
		
		// Get clusters
		ACDC.run(depsPath, outputClustersPath);
		String resultSerFilename = outputPath + File.separator + ser;
		ArchSmellDetector asd = new ArchSmellDetector(depsPath, outputClustersPath, resultSerFilename);
		
		// Call ArchSmellDetector.run() (with runConcern=false)
		assertDoesNotThrow(() -> asd.run(true, false, true)); 

		// Construct SmellCollection objects out of the result and oracle files
		SmellCollection resultSmells = assertDoesNotThrow(() -> {
			return new SmellCollection(resultSerFilename);
		});
		SmellCollection oracleSmells = assertDoesNotThrow(() -> {
			return new SmellCollection(oraclePath);
		});

		// SmellCollection extends HashSet, so we can use equals() to compare the result to the oracle
		assertTrue(oracleSmells.equals(resultSmells));
	}

	@ParameterizedTest
	@CsvSource({
		/** Test parameters **/
		// [path to oracle ser file (must be reformatted for compatibility)]
		// [path to deps rsf]
		// [output directory]
		// [output path for clusters file]
		// [desired result ser filename]

		// TODO: convert the oracle files to serialized SmellCollection
		// // struts 2.3.30
		// ".///src///test///resources///SmellsCompareTest///struts-2.3.30_arc_smells.ser,"
		// + ".///src///test///resources///JavaSourceToDepsBuilderTest_resources///struts-2.3.30_deps.rsf,"
		// + ".///target///test_results///SmellsCompareTest,"
		// + ".///target///test_results///SmellsCompareTest///struts-2.3.30_arc_clusters.rsf,"
		// + "struts-2.3.30_arc_smells.ser",

		// // struts-2.5.2
		// ".///src///test///resources///SmellsCompareTest_resources///struts-2.5.2_arc_smells.ser,"
		// + ".///src///test///resources///JavaSourceToDepsBuilderTest_resources///struts-2.5.2_deps.rsf,"
		// + ".///target///test_results///SmellsCompareTest,"
		// + ".///target///test_results///SmellsCompareTest///struts-2.5.2_arc_clusters.rsf,"
		// + "struts-2.5.2_arc_smells.ser",

		// TODO: No ARC oracles available for httpd yet (empty smells file due to recovery bug)
		// // httpd 2.3.8
		// ".///src///test///resources///SmellsCompareTest///httpd-2.3.8_arc_smells.ser,"
		// + ".///src///test///resources///CSourceToDepsBuilderTest_resources///httpd-2.3.8_deps.rsf,"
		// + ".///target///test_results///SmellsCompareTest,"
		// + ".///target///test_results///SmellsCompareTest///httpd-2.3.8_arc_clusters.rsf,"
		// + "httpd-2.3.8_arc_smells.ser",

		// // httpd-2.4.26
		// ".///src///test///resources///SmellsCompareTest///httpd-2.4.26_arc_smells.ser,"
		// + ".///src///test///resources///CSourceToDepsBuilderTest_resources///httpd-2.4.26_deps.rsf,"
		// + ".///target///test_results///SmellsCompareTest,"
		// + ".///target///test_results///SmellsCompareTest///httpd-2.4.26_arc_clusters.rsf,"
		// + "httpd-2.4.26_arc_smells.ser",
	})
	public void withConcernsTest(String oracle, String deps, String output, String clusters, String ser){    
		/** Run smell analyzer WITH concerns (ARC) **/
		String oraclePath = oracle.replace("///", File.separator);  
		String depsPath = deps.replace("///", File.separator);
		String outputPath = output.replace("///", File.separator);
		String outputClustersPath = clusters.replace ("///", File.separator);
		
		// Get clusters
		ACDC.run(depsPath, outputClustersPath);
		String resultSerFilename = outputPath + File.separator + ser;
		ArchSmellDetector asd = new ArchSmellDetector(depsPath, outputClustersPath, resultSerFilename);
		
		// Call ArchSmellDetector.run() (with runConcern=true)
		assertDoesNotThrow(() -> asd.run(true, false, true)); 

		// Construct SmellCollection objects out of the result and oracle files
		SmellCollection resultSmells = assertDoesNotThrow(() -> {
			return new SmellCollection(resultSerFilename);
		});
		SmellCollection oracleSmells = assertDoesNotThrow(() -> {
			return new SmellCollection(oraclePath);
		});

		// SmellCollection extends HashSet, so we can use equals() to compare the result to the oracle
		assertTrue(oracleSmells.equals(resultSmells));
	}
}