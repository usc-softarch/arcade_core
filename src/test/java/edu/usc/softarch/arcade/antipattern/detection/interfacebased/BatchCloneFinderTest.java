package edu.usc.softarch.arcade.antipattern.detection.interfacebased;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import edu.usc.softarch.arcade.BaseTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import edu.usc.softarch.arcade.util.FileUtil;

/**
 * Tests whether the results of PMD are correct for what ARCADE expects.
 */
public class BatchCloneFinderTest extends BaseTest {
	private final String resourcesDir = resourcesBase + fs + "CloneFinder";
	private final String outputDir = outputBase + fs + "CloneFinder";

	/**
	 * Sets up the CLI command to run PMD.
	 * 
	 * @param version The version of the sample system.
	 * @return The command and arguments needed to run PMD.
	 */
	private List<String> buildArguments(String version) {
		List<String> command = new ArrayList<>();

		// The actual system being run is ANT. PMD is run through it.
		command.add("ext-tools" + fs + "apache-ant-1.9.6"  + fs + "bin"
			+ fs + "ant.bat");
		// -f is to input a file
		command.add("-f");
		// The file being input is to run PMD
		command.add("ext-tools" + fs + "pmd-bin-5.3.2"  + fs + "cpd.xml");
		// CPD is the copy-paste detector of PMD
		command.add("cpd");
		// This is the input for PMD
		command.add("-Din=" + System.getProperty("user.dir") + fs + "src" + fs
			+ "test" + fs + "resources" + fs + "subject_systems" + fs
			+ version);
		// This is the path for an output file.
		command.add("-Dout="+ System.getProperty("user.dir") + fs + "target" + fs
			+ "test_results" + fs + "CloneFinder" + fs + version
			+ "_clone.xml");

		return command;
	}

	/**
	 * Sets up and runs the Process object responsible for PMD.
	 * 
	 * @param version The version of the sample system.
	 */
	private void setUp(String version) {
		(new File("target" + fs + "test_results"
			+ fs + "CloneFinder")).mkdirs();
		List<String> command = buildArguments(version);
		ProcessBuilder pb = new ProcessBuilder(command);
		pb.inheritIO();

		try {
			Process p = pb.start();
			p.waitFor();
		} catch(IOException ioe) {
			ioe.printStackTrace();
			fail("Failed to start Process.");
		} catch (InterruptedException e) {
			e.printStackTrace();
			fail("Failed to wait for Process.");
		}
	}

	/**
	 * Cleans up any absolute paths in the result of PMD, so the test will be able
	 * to compare the strings.
	 * 
	 * @param toClean The String to clean.
	 * @return A new String with relative paths.
	 */
	private String cleanAbsolutePaths(String toClean) {
		return toClean.replaceAll("\\r\\n?", "\n")
			.replaceAll("path=\\\".*\\\\subject_systems(_resources)?(\\\\.*\\\\src)?", " ")
			.replaceAll("<\\?xml version=\".*\" encoding=\".*\"\\?>", "");
	}
	
	/**
	 * Test for PMD's CPD module, used as a clone detector in ARCADE. The goal of
	 * this test is to ensure that the output of PMD is what is expected by
	 * ARCADE, both as a sanity check and as a gatekeeping mechanism if we ever
	 * opt to use a newer version of PMD, or a different clone detector.
	 * 
	 * @param version The version of the sample system.
	 */
	@ParameterizedTest
	@CsvSource({
		"struts-2.3.30",

		"struts-2.5.2",

		"nutch-1.7",

		"nutch-1.8",

		"nutch-1.9"
	})
	public void singleTest(String version) {
		// Constructs ProcessBuilder
		setUp(version);

		String oracleClonesPath = resourcesDir + fs + version + "_clone.xml";
		String resultClonesPath = outputDir + fs + version + "_clone.xml";

		// Read in xmls as Strings
		String resultString = assertDoesNotThrow(
			() -> FileUtil.readFile(resultClonesPath, StandardCharsets.UTF_8));

		// ------------------------- Generate Oracles ------------------------------

		if (generateOracles) {
			assertDoesNotThrow(() -> {
				Path resultPath = Paths.get(resultClonesPath);
				Path oraclePath = Paths.get(oracleClonesPath);
				Files.copy(resultPath, oraclePath, StandardCopyOption.REPLACE_EXISTING);
			});
		}

		// ------------------------- Generate Oracles ------------------------------

		String oracleString = assertDoesNotThrow(
			() -> FileUtil.readFile(oracleClonesPath, StandardCharsets.UTF_8));

		assertEquals(cleanAbsolutePaths(oracleString),
			cleanAbsolutePaths(resultString));
	}
}
