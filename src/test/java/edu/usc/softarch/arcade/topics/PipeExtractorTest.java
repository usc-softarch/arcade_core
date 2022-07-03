package edu.usc.softarch.arcade.topics;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Set;

import edu.usc.softarch.arcade.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;

public class PipeExtractorTest extends BaseTest {
	@BeforeEach
	public void setUp() {
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
	public void mainTest(String versionDir, String outputDir, String oracleFile,
			String language) {
		String fs = File.separator;
		/** Integration test for PipeExtractor **/
		String classesDir = versionDir.replace("///", File.separator);
		String resultDir = outputDir.replace("///", File.separator);
		// Path to oracle pipe file
		String oraclePath = oracleFile.replace("///", File.separator);
		String stopWordsDir = "src" + fs + "main" + fs + "resources" + fs + "res";
		(new File(resultDir)).mkdirs();

		// Call PipeExtractor.main() 
		// (arguments: sys version dir, output dir, selected language)
		MalletRunner runner = new MalletRunner(classesDir, language, "",
			resultDir, stopWordsDir);
		assertDoesNotThrow(runner::copySource);
		assertDoesNotThrow(runner::runPipeExtractor);
		runner.cleanUp();

		// ------------------------- Generate Oracles ------------------------------

		if (super.generateOracles) {
			assertDoesNotThrow(() -> {
				Path result = Paths.get(resultDir + fs + "output.pipe");
				Path oracle = Paths.get(oraclePath);
				Files.copy(result, oracle, StandardCopyOption.REPLACE_EXISTING);
			});
		}

		// ------------------------- Generate Oracles ------------------------------

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