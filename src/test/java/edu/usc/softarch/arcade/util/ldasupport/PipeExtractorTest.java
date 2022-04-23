package edu.usc.softarch.arcade.util.ldasupport;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;

public class PipeExtractorTest {
	// // Save the present working directory (i.e. the root of the repository)
	// String pwd = System.getProperty("user.dir");

	@BeforeEach
	public void setUp(){
		char fs = File.separatorChar;
		// Make sure directory for test output exists (and create if it doesn't)
		String outputPath = "." + fs + "target" + fs + "test_results" + fs + "BatchClusteringEngineTest";
		(new File(outputPath)).mkdirs();

		// Change the present working directory to location of stoplists/keywords
		// System.setProperty("user.dir", pwd + fs + "src" + fs + "main" + fs + "resources" + fs);
	}

	@ParameterizedTest
	@CsvSource({
		// Test parameters: 
		// [path to system version src files], 
		// [desired output path], 
		// [path to oracle pipe file],
		// [system language]

		// struts 2.3.30
		".///src///test///resources///subject_systems///struts-2.3.30,"
		+ ".///target///test_results///PipeExtractorTest///struts-2.3.30///base,"
		+ ".///src///test///resources///PipeExtractorTest_resources///Struts2///arc///base///struts-2.3.30///output.pipe,"
		+ "java",
		// struts 2.5.2
		".///src///test///resources///subject_systems///struts-2.5.2,"
		+ ".///target///test_results///PipeExtractorTest///struts-2.5.2///base,"
		+ ".///src///test///resources///PipeExtractorTest_resources///Struts2///arc///base///struts-2.5.2///output.pipe,"
		+ "java",
		// httpd 2.3.8
		".///src///test///resources///subject_systems///httpd-2.3.8,"
		+ ".///target///test_results///PipeExtractorTest///httpd-2.3.8///base,"
		+ ".///src///test///resources///PipeExtractorTest_resources///httpd///arc///base///httpd-2.3.8///output.pipe,"
		+ "c",
		// httpd 2.4.26
		".///src///test///resources///subject_systems///httpd-2.4.26,"
		+ ".///target///test_results///PipeExtractorTest///httpd-2.4.26///base,"
		+ ".///src///test///resources///PipeExtractorTest_resources///httpd///arc///base///httpd-2.4.26///output.pipe,"
		+ "c",
	})
	public void mainTest(String versionDir, String outputDir, String oracleFile, String language){
		/** Integration test for PipeExtractor **/
		String classesDir = versionDir.replace("///", File.separator);
		String resultDir = outputDir.replace("///", File.separator);
		// Path to oracle pipe file
		String oraclePath = oracleFile.replace("///", File.separator);
		(new File(resultDir)).mkdirs();
		
		// Call PipeExtractor.main() 
		// (arguments: sys version dir, output dir, selected language)
		assertDoesNotThrow( () -> {
			PipeExtractor.main(new String[] {classesDir, resultDir, language});
		});

		// Read result instances into a set
		InstanceList resultInstances = InstanceList.load(new File(resultDir + File.separatorChar + "output.pipe"));
		Set<InstanceComparator> result = new HashSet<>();
		for (Instance i : resultInstances) {
			result.add(new InstanceComparator(i));
		}
		
		// Read oracle instances into a set
		InstanceList oracleInstances = InstanceList.load(new File(oraclePath));
		Set<InstanceComparator> oracle = new HashSet<>();
		for (Instance i : oracleInstances) {
			oracle.add(new InstanceComparator(i));
		}

		// Compare sets of instances
		assertEquals(oracle.size(), result.size()); // passes - same size
		assertEquals(oracle, result, "Failed argument is: " + versionDir); // fails - comparing sets of instances doesn't work?
	}
}