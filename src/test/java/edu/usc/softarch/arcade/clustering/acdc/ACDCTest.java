package edu.usc.softarch.arcade.clustering.acdc;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import edu.usc.softarch.arcade.util.FileUtil;
import edu.usc.softarch.arcade.util.RsfCompare;

public class ACDCTest {
	@BeforeEach
	public void setUp(){
		// Create rsf file output path ./target/test_results/ACDCTest/ if it does not already exist
		char fs = File.separatorChar;
		String outputPath = "." + fs + "target" + fs + "test_results" + fs + "ACDCTest";
		File directory = new File(outputPath);
		directory.mkdirs();
	}

	@ParameterizedTest
	@CsvSource({
		// struts 2.3.30
		".///src///test///resources///JavaSourceToDepsBuilderTest_resources///struts-2.3.30_deps.rsf,"
		+ ".///target///test_results///ACDCTest///struts2.3.30_mainTestResult.rsf,"
		+ ".///src///test///resources///ACDCTest_resources///struts-2.3.30_acdc_clustered.rsf",
		// struts 2.5.2
		".///src///test///resources///JavaSourceToDepsBuilderTest_resources///struts-2.5.2_deps.rsf,"
		+ ".///target///test_results///ACDCTest///struts2.5.2_mainTestResult.rsf,"
		+ ".///src///test///resources///ACDCTest_resources///struts-2.5.2_acdc_clustered.rsf",
		// httpd 2.3.8
		".///src///test///resources///CSourceToDepsBuilderTest_resources///httpd-2.3.8_deps.rsf,"
		+ ".///target///test_results///ACDCTest///httpd-2.3.8_mainTestResult.rsf,"
		+ ".///src///test///resources///ACDCTest_resources///httpd-2.3.8_acdc_clustered.rsf",
		// httpd 2.4.26
		".///src///test///resources///CSourceToDepsBuilderTest_resources///httpd-2.4.26_deps.rsf,"
		+ ".///target///test_results///ACDCTest///httpd-2.4.26_mainTestResult.rsf,"
		+ ".///src///test///resources///ACDCTest_resources///httpd-2.4.26_acdc_clustered.rsf",
	})
	public void mainTest(String depsRsfFilename, String clusterRsfFilename, String oraclePath){
		/** Clusters system given dependencies rsf file **/
		// Format paths properly
		String deps = depsRsfFilename.replace("///", File.separator);
		String clusters = clusterRsfFilename.replace("///", File.separator);
		String oracle = oraclePath.replace("///", File.separator);
		
		// Run ACDC
		assertDoesNotThrow(() -> ACDC.run(deps, clusters));
		String result = assertDoesNotThrow(() -> {
			return FileUtil.readFile(clusters, StandardCharsets.UTF_8);
		});

		// Load oracle
		String oracleResult = assertDoesNotThrow(() -> {
			return FileUtil.readFile(oracle, StandardCharsets.UTF_8);
		});

		// Use RsfCompare.compareTo to compare file contents
		RsfCompare resultRsf = new RsfCompare(result);
		RsfCompare oracleRsf = new RsfCompare(oracleResult);
		assertEquals(oracleRsf, resultRsf);
	}
}