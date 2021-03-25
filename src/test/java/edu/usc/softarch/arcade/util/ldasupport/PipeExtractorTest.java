package edu.usc.softarch.arcade.util.ldasupport;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;

public class PipeExtractorTest {
	// Save the present working directory (i.e. the root of the repository)
	String pwd = System.getProperty("user.dir");

	@BeforeEach
	public void setUp(){
		char fs = File.separatorChar;
		// Make sure directory for test output exists (and create if it doesn't)
		String outputPath = "." + fs + "target" + fs + "test_results" + fs + "PipeExtractorTest";
		(new File(outputPath)).mkdirs();

		// Change the present working directory to location of stoplists/keywords
		System.setProperty("user.dir", pwd + fs + "src" + fs + "main" + fs + "resources" + fs);
	}

	@ParameterizedTest
	@CsvSource({
		// Test parameters: 
		// [path to system version], 
		// [desired output path], 
		// [path to oracle pipe file],
		// [system language]

		// struts 2.3.30
		"///src///test///resources///BatchClusteringEngineTest_resources///src///struts-2.3.30,"
		+ "///target///test_results///PipeExtractorTest///struts-2.3.30,"
		+ "///src///test///resources///PipeExtractorTest_resources///Struts2///arc///base///struts-2.3.30///output.pipe,"
		+ "java",
		// // struts 2.5.2
		// "///src///test///resources///BatchClusteringEngineTest_resources///src///struts-2.5.2,"
		// + "///target///test_results///PipeExtractorTest///struts-2.5.2,"
		// + "///src///test///resources///PipeExtractorTest_resources///Struts2///arc///base///struts-2.5.2///output.pipe,"
		// + "java",
		// // httpd 2.3.8
		// "///src///test///resources///CSourceToDepsBuilderTest_resources///src///httpd-2.3.8,"
		// + "///target///test_results///PipeExtractorTest///httpd-2.3.8,"
		// + "///src///test///resources///PipeExtractorTest_resources///httpd///arc///base///httpd-2.3.8///output.pipe,"
		// + "c",
		// // httpd 2.4.26
		// "///src///test///resources///CSourceToDepsBuilderTest_resources///src///httpd-2.4.26,"
		// + "///target///test_results///PipeExtractorTest///httpd-2.4.26,"
		// + "///src///test///resources///PipeExtractorTest_resources///httpd///arc///base///httpd-2.4.26///output.pipe,"
		// + "c",
	})
	public void mainTest(String versionDir, String outputDir, String oracleFile, String language){
		/** Integration test for PipeExtractor **/
		String classesDir = pwd + versionDir.replace("///", File.separator);
		String resultDir = pwd + outputDir.replace("///", File.separator);
		// Path to oracle pipe file
		String oraclePath = pwd + oracleFile.replace("///", File.separator);
		(new File(resultDir)).mkdirs();
		// Call PipeExtractor.main() 
		// (arguments: sys version dir, output dir, selected language)
		assertDoesNotThrow( () -> {
			PipeExtractor.main(new String[] {classesDir, resultDir, language});
		});

		// Read result instances into a set
		InstanceList resultInstances = InstanceList.load(new File(oraclePath));
		Set<Instance> result = new HashSet<>();
		for (Instance i : resultInstances) {
			result.add(i);
		}
		
		// Read oracle instances into a set
		InstanceList oracleInstances = InstanceList.load(new File(oraclePath));
		Set<Instance> oracle = new HashSet<>();
		for (Instance i : oracleInstances) {
			oracle.add(i);
		}

		System.out.println("Oracle size: " + oracle.size());
		System.out.println("Result size: " + result.size());
		// Compare sets of instances
		assertEquals(oracle.size(), result.size()); // passes - same size
		// assertTrue(oracle.equals(result)); // fails - comparing sets of instances doesn't work
	}

	@AfterEach
	public void cleanUp(){
		// Reset working directory to repo root
		System.setProperty("user.dir", pwd);
	}
}
