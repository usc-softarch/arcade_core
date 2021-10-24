package edu.usc.softarch.arcade.antipattern.detection.interfacebased;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import edu.usc.softarch.arcade.util.FileUtil;

/**
 * Tests whether the results of PMD are correct for what ARCADE expects.
 */
public class BatchCloneFinderTest {
	private char fs = File.separatorChar;

	/**
	 * Sets up the CLI command to run PMD.
	 * 
	 * @param system The name of the sample system.
	 * @param version The version of the sample system.
	 * @return The command and arguments needed to run PMD.
	 */
	private List<String> buildArguments(String system, String version) {
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
			+ "test" + fs + "resources" + fs + "subject_systems_resources" + fs
			+ system + fs + "src" + fs + version);
		// This is the path for an output file.
		command.add("-Dout="+ System.getProperty("user.dir") + fs + "target" + fs
			+ "test_results" + fs + "BatchCloneFinderTest" + fs + version 
			+ "_clone.xml");

		return command;
	}

	/**
	 * Sets up and runs the Process object responsible for PMD.
	 * 
	 * @param system The name of the sample system.
	 * @param version The version of the sample system.
	 */
	private void setUp(String system, String version) {
		(new File("target" + fs + "test_results" + fs + "BatchCloneFinderTest"))
			.mkdirs();
		List<String> command = buildArguments(system, version);
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
			.replaceAll("path=\\\".*\\\\subject_systems(_resources)?", " ");
	}
	
	/**
	 * Test for PMD's CPD module, used as a clone detector in ARCADE. The goal of
	 * this test is to ensure that the output of PMD is what is expected by
	 * ARCADE, both as a sanity check and as a gatekeeping mechanism if we ever
	 * opt to use a newer version of PMD, or a different clone detector.
	 * 
	 * @param system The name of the sample system.
	 * @param version The version of the sample system.
	 * @param oracle The path to the test oracle file.
	 */
	@ParameterizedTest
	@CsvSource({
		// struts-2.3.30
		"Struts2,"
		+ "struts-2.3.30,"
		+ ".///src///test///resources///BatchCloneFinderTest_resources"
			+ "///struts-2.3.30_clone.xml",
		// struts-2.5.2
		"Struts2,"
		+ "struts-2.5.2,"
		+ ".///src///test///resources///BatchCloneFinderTest_resources"
			+ "///struts-2.5.2_clone.xml",
		// nutch-1.7
		"nutch,"
		+ "nutch1.7,"
		+ ".///src///test///resources///BatchCloneFinderTest_resources"
			+ "///nutch-1.7_clone.xml",
		// nutch-1.8
		"nutch,"
		+ "nutch1.8,"
		+ ".///src///test///resources///BatchCloneFinderTest_resources"
			+ "///nutch-1.8_clone.xml",
		// nutch-1.8
		"nutch,"
		+ "nutch1.9,"
		+ ".///src///test///resources///BatchCloneFinderTest_resources"
			+ "///nutch-1.9_clone.xml",
	})
	public void singleTest(String system, String version, String oracle) {
		// Constructs ProcessBuilder
		setUp(system, version);

		String oraclePath = oracle.replace("///", File.separator);
		String resultPath = "target" + fs + "test_results" + fs
			+ "BatchCloneFinderTest" + fs + version + "_clone.xml";

		// Read in xmls as Strings
		String resultString = assertDoesNotThrow(() -> {
		  return FileUtil.readFile(resultPath, StandardCharsets.UTF_8);
		});
		String oracleString = assertDoesNotThrow(() -> {
		  return FileUtil.readFile(oraclePath, StandardCharsets.UTF_8);
		});

		assertEquals(cleanAbsolutePaths(oracleString),
			cleanAbsolutePaths(resultString));
	}
}
