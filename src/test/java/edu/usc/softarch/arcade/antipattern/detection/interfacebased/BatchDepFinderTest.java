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

public class BatchDepFinderTest {
  char fs = File.separatorChar;
  private List<String> buildArguments(String version){
    List<String> command = new ArrayList<>();
    command.add("ext-tools" + fs + "DependencyFinder" + fs + "bin" + fs + "DependencyExtractor.bat");
    command.add("-xml");
    command.add("-out");
    command.add(System.getProperty("user.dir") + fs + "target" + fs + "test_results" 
      + fs + "BatchDepFinderTest" + fs + version + "_deps.xml");
    command.add(System.getProperty("user.dir") + fs + "src" + fs + "test" + fs + "resources" 
      + fs + "subject_systems_resources" + fs + "Struts2" + fs + "src" + fs + version + fs + "lib_struts");
    return command;
  }
  public void setUp(String version){
    (new File("target" + fs + "test_results" + fs + "BatchDepFinderTest")).mkdirs();
    List<String> command = buildArguments(version);
    ProcessBuilder pb = new ProcessBuilder(command);
    pb.inheritIO();
    try{
      Process p = pb.start();
      p.waitFor();
    } catch(IOException ioe){
      ioe.printStackTrace();
      fail("failed to start Process");
    } catch (InterruptedException e) {
      e.printStackTrace();
      fail("failed to wait for Process");
    }

  }
  @ParameterizedTest
  @CsvSource({
    // struts-2.3.30
    "struts-2.3.30,"
    + ".///src///test///resources///BatchDepFinderTest_resources///struts-2.3.30_deps.xml",
    // struts-2.5.2
    "struts-2.5.2,"
    + ".///src///test///resources///BatchDepFinderTest_resources///struts-2.5.2_deps.xml",
  })
  public void singleTest(String version, String oracle){
    // Constructs ProcessBuilder
    setUp(version);

    String oraclePath = oracle.replace("///", File.separator);
    String resultPath = "target" + fs + "test_results" + fs + "BatchDepFinderTest" + fs + version + "_deps.xml";

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