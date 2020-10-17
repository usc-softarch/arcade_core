package edu.usc.softarch.arcade.facts.driver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.File;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import edu.usc.softarch.arcade.util.FileUtil;

public class JavaSourceToDepsBuilderTest {
  // #region TESTS build -------------------------------------------------------
  @Test
  public void buildTest1() {
    // Builds the dependencies RSF file for Josh's ARCADE version
    char fs = File.separatorChar;
    String classesDirPath = "." + fs + "src" + fs + "test" + fs + "resources"
      + fs + "JavaSourceToDepsBuilderTest_resources"
      + fs + "arcade_old_binaries";
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
      + fs + "arcade_old_deps_oracle.rsf";
    String oracle = assertDoesNotThrow(() ->
      { return FileUtil.readFile(oraclePath, StandardCharsets.UTF_8); });
    
    // Check oracle
    assertEquals(result, oracle);
  }
  // #endregion TESTS build ----------------------------------------------------
}
