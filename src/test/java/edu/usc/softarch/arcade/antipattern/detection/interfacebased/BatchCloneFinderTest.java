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

public class BatchCloneFinderTest {
	char fs = File.separatorChar;
	private List<String> buildArguments(String version){
		List<String> command = new ArrayList<>();
		command.add("ext-tools" + fs + "apache-ant-1.9.6"  + fs + "bin" + fs + "ant.bat");
		command.add("-f");
		command.add("ext-tools" + fs + "pmd-bin-5.3.2"  + fs + "cpd.xml");
		command.add("cpd");
		command.add("-Din=" + System.getProperty("user.dir") + fs + "src" + fs + "test" + fs + "resources" 
			+ fs + "subject_systems_resources" + fs + "Struts2" + fs + "src" + fs + version);
		command.add("-Dout="+ System.getProperty("user.dir") + fs + "target" + fs + "test_results" 
			+ fs + "BatchCloneFinderTest" + fs + version + "_clone.xml");
		return command;
	}
	public void setUp(String version){
		(new File("target" + fs + "test_results" + fs + "BatchCloneFinderTest")).mkdirs();
		List<String> command = buildArguments(version);
		ProcessBuilder pb = new ProcessBuilder(command);
		pb.inheritIO();
		try{
			Process p = pb.start();
			p.waitFor();
		} catch(IOException ioe){
			fail("failed to start Process");
			ioe.printStackTrace();
		} catch (InterruptedException e) {
			fail("failed to wait for Process");
			e.printStackTrace();
		}

	}
	@ParameterizedTest
	@CsvSource({
		// struts-2.3.30
		"struts-2.3.30,"
		+ ".///src///test///resources///BatchCloneFinderTest_resources///struts-2.3.30_clone.xml",
		// struts-2.5.2
		"struts-2.5.2,"
		+ ".///src///test///resources///BatchCloneFinderTest_resources///struts-2.5.2_clone.xml",
	})
	public void singleTest(String version, String oracle){
		// Constructs ProcessBuilder
		setUp(version);

		String oraclePath = oracle.replace("///", File.separator);
		String resultPath = "target" + fs + "test_results" + fs + "BatchCloneFinderTest" + fs + version + "_clone.xml";

		// Read in xmls as Strings
		String resultString = assertDoesNotThrow( () -> {
			return FileUtil.readFile(resultPath, StandardCharsets.UTF_8);
		});
		String oracleString = assertDoesNotThrow( () -> {
			return FileUtil.readFile(oraclePath, StandardCharsets.UTF_8);
		});

		assertEquals(oracleString, resultString);
	}
}
