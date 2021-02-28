package edu.usc.softarch.arcade.facts.driver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.File;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import edu.usc.softarch.arcade.util.FileUtil;
import edu.usc.softarch.arcade.util.RsfCompare;

public class JavaSourceToDepsBuilderTest {
  // #region TESTS build -------------------------------------------------------
  @Test
  public void buildTestOld() {
    // Builds the dependencies RSF file for Josh's ARCADE version
    char fs = File.separatorChar;
    String classesDirPath = "." + fs + "src" + fs + "test" + fs + "resources"
      + fs + "JavaSourceToDepsBuilderTest_resources_old"
      + fs + "arcade_old_binaries";
    String depsRsfFilename = "." + fs + "target" + fs + "test_results" + fs 
      + "JavaSourceToDepsBuilderTest" + fs + "buildTestOldResult.rsf";

    // Run JavaSourceToDepsBuilder.build()
    assertDoesNotThrow(() -> (
      new JavaSourceToDepsBuilder()).build(classesDirPath, depsRsfFilename));
    String result = assertDoesNotThrow(() -> 
      { return FileUtil.readFile(depsRsfFilename, StandardCharsets.UTF_8); });
    
    // Load oracle
    String oraclePath = "." + fs + "src" + fs + "test" + fs + "resources"
      + fs + "JavaSourceToDepsBuilderTest_resources_old"
      + fs + "arcade_old_deps_oracle.rsf";
    String oracle = assertDoesNotThrow(() ->
      { return FileUtil.readFile(oraclePath, StandardCharsets.UTF_8); });
    
    // Check oracle
    RsfCompare resultRsf = new RsfCompare(result);
    RsfCompare oracleRsf = new RsfCompare(oracle);
    // RsfCompare.compareTo returns 0 if files have the same contents
    assertEquals(resultRsf.compareTo(oracleRsf), 0);
  }
  @Test
  public void buildTest1() {
    // Builds the dependencies RSF file for Josh's ARCADE version
    char fs = File.separatorChar;
    String classesDirPath = "." + fs + "src" + fs + "test" + fs + "resources"
      + fs + "JavaSourceToDepsBuilderTest_resources"
      + fs + "binaries" + fs + "struts-2.3.30" + fs + "lib_struts";
    String depsRsfFilename = "." + fs + "target" + fs + "test_results" + fs 
      + "JavaSourceToDepsBuilderTest" + fs + "buildTest1Result.rsf";

    // Run JavaSourceToDepsBuilder.build()
    assertDoesNotThrow(() -> (
      new JavaSourceToDepsBuilder()).build(classesDirPath, depsRsfFilename));
    String result = assertDoesNotThrow(() -> 
      { return FileUtil.readFile(depsRsfFilename, StandardCharsets.UTF_8); });
    
    // Load oracle
    String oraclePath = "." + fs + "src" + fs + "test" + fs + "resources"
      + fs + "JavaSourceToDepsBuilderTest_resources"
      + fs + "struts-2.3.30_deps.rsf";
    String oracle = assertDoesNotThrow(() ->
      { return FileUtil.readFile(oraclePath, StandardCharsets.UTF_8); });
    
    // Check oracle
    RsfCompare resultRsf = new RsfCompare(result);
    RsfCompare oracleRsf = new RsfCompare(oracle);
    // RsfCompare.compareTo returns 0 if files have the same contents
    assertEquals(resultRsf.compareTo(oracleRsf), 0);
  }

  @Test
  public void buildTest2() {
    // Builds the dependencies RSF file for Josh's ARCADE version
    char fs = File.separatorChar;
    String classesDirPath = "." + fs + "src" + fs + "test" + fs + "resources"
      + fs + "JavaSourceToDepsBuilderTest_resources"
      + fs + "binaries" + fs + "struts-2.5.2" + fs + "lib_struts";
    String depsRsfFilename = "." + fs + "target" + fs + "test_results" + fs 
      + "JavaSourceToDepsBuilderTest" + fs + "buildTest2Result.rsf";

    // Run JavaSourceToDepsBuilder.build()
    assertDoesNotThrow(() -> (
      new JavaSourceToDepsBuilder()).build(classesDirPath, depsRsfFilename));
    String result = assertDoesNotThrow(() -> 
      { return FileUtil.readFile(depsRsfFilename, StandardCharsets.UTF_8); });
    
    // Load oracle
    String oraclePath = "." + fs + "src" + fs + "test" + fs + "resources"
      + fs + "JavaSourceToDepsBuilderTest_resources"
      + fs + "struts-2.5.2_deps.rsf";
    String oracle = assertDoesNotThrow(() ->
      { return FileUtil.readFile(oraclePath, StandardCharsets.UTF_8); });
    
    // Check oracle
    RsfCompare resultRsf = new RsfCompare(result);
    RsfCompare oracleRsf = new RsfCompare(oracle);
    // RsfCompare.compareTo returns 0 if files have the same contents
    assertEquals(resultRsf.compareTo(oracleRsf), 0);
  }
  // #endregion TESTS build ----------------------------------------------------
}
