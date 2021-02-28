package edu.usc.softarch.arcade.clustering.acdc;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import edu.usc.softarch.arcade.util.FileUtil;
import edu.usc.softarch.arcade.util.RsfCompare;

public class ACDCTest {
  @Test
  public void struts2MainTest1() {
    // Cluster struts2 version 2.3.30
    char fs = File.separatorChar;
    String depsRsfFilename = "." + fs + "src" + fs + "test" + fs + "resources"
      + fs + "JavaSourceToDepsBuilderTest_resources"
      + fs + "struts-2.3.30_deps.rsf";
    String clusterRsfFilename = "." + fs + "target" + fs + "test_results"
      + fs + "ACDCTest" + fs + "struts2MainTest1Result.rsf";

    // Run ACDC
    ACDC.run(depsRsfFilename, clusterRsfFilename);
    String result = assertDoesNotThrow(() ->
      { return FileUtil.readFile(clusterRsfFilename, StandardCharsets.UTF_8);});

    // Load oracle
    String oraclePath = "." + fs + "src" + fs + "test" + fs + "resources"
      + fs + "ACDCTest_resources" + fs + "struts-2.3.30_acdc_clustered.rsf";
    String oracle = assertDoesNotThrow(() ->
      { return FileUtil.readFile(oraclePath, StandardCharsets.UTF_8); });

    // Compare with oracle
    RsfCompare resultRsf = new RsfCompare(result);
    RsfCompare oracleRsf = new RsfCompare(oracle);
    // RsfCompare.compareTo returns 0 if files have the same contents
    assertEquals(resultRsf.compareTo(oracleRsf), 0);
  }

  @Test
  public void struts2MainTest2() {
    // Cluster struts2 version 2.5.2
    char fs = File.separatorChar;
    String depsRsfFilename = "." + fs + "src" + fs + "test" + fs + "resources"
      + fs + "JavaSourceToDepsBuilderTest_resources"
      + fs + "struts-2.5.2_deps.rsf";
    String clusterRsfFilename = "." + fs + "target" + fs + "test_results"
      + fs + "ACDCTest" + fs + "struts2MainTest2Result.rsf";

    // Run ACDC
    ACDC.run(depsRsfFilename, clusterRsfFilename);
    String result = assertDoesNotThrow(() ->
      { return FileUtil.readFile(clusterRsfFilename, StandardCharsets.UTF_8);});

    // Load oracle
    String oraclePath = "." + fs + "src" + fs + "test" + fs + "resources"
      + fs + "ACDCTest_resources" + fs + "struts-2.5.2_acdc_clustered.rsf";
    String oracle = assertDoesNotThrow(() ->
      { return FileUtil.readFile(oraclePath, StandardCharsets.UTF_8); });

    // Compare with oracle
    RsfCompare resultRsf = new RsfCompare(result);
    RsfCompare oracleRsf = new RsfCompare(oracle);
    // RsfCompare.compareTo returns 0 if files have the same contents
    assertEquals(resultRsf.compareTo(oracleRsf), 0);
  }

  @Test
  public void httpdMainTest1() {
    // Cluster httpd version 2.3.8
    char fs = File.separatorChar;
    String depsRsfFilename = "." + fs + "src" + fs + "test" + fs + "resources"
      + fs + "CSourceToDepsBuilderTest_resources"
      + fs + "httpd-2.3.8_deps.rsf";
    String clusterRsfFilename = "." + fs + "target" + fs + "test_results"
      + fs + "ACDCTest" + fs + "httpdMainTest1Result.rsf";

    // Run ACDC
    ACDC.run(depsRsfFilename, clusterRsfFilename);
    String result = assertDoesNotThrow(() ->
      { return FileUtil.readFile(clusterRsfFilename, StandardCharsets.UTF_8);});

    // Load oracle
    String oraclePath = "." + fs + "src" + fs + "test" + fs + "resources"
      + fs + "ACDCTest_resources" + fs + "httpd-2.3.8_acdc_clustered.rsf";
    String oracle = assertDoesNotThrow(() ->
      { return FileUtil.readFile(oraclePath, StandardCharsets.UTF_8); });

    // Compare with oracle
    RsfCompare resultRsf = new RsfCompare(result);
    RsfCompare oracleRsf = new RsfCompare(oracle);
    // RsfCompare.compareTo returns 0 if files have the same contents
    assertEquals(resultRsf.compareTo(oracleRsf), 0);
  }

  @Test
  public void httpdMainTest2() {
    // Cluster httpd version 2.4.26
    char fs = File.separatorChar;
    String depsRsfFilename = "." + fs + "src" + fs + "test" + fs + "resources"
      + fs + "CSourceToDepsBuilderTest_resources"
      + fs + "httpd-2.4.26_deps.rsf";
    String clusterRsfFilename = "." + fs + "target" + fs + "test_results"
      + fs + "ACDCTest" + fs + "httpdMainTest2Result.rsf";

    // Run ACDC
    ACDC.run(depsRsfFilename, clusterRsfFilename);
    String result = assertDoesNotThrow(() ->
      { return FileUtil.readFile(clusterRsfFilename, StandardCharsets.UTF_8);});

    // Load oracle
    String oraclePath = "." + fs + "src" + fs + "test" + fs + "resources"
      + fs + "ACDCTest_resources" + fs + "httpd-2.4.26_acdc_clustered.rsf";
    String oracle = assertDoesNotThrow(() ->
      { return FileUtil.readFile(oraclePath, StandardCharsets.UTF_8); });

    // Compare with oracle
    RsfCompare resultRsf = new RsfCompare(result);
    RsfCompare oracleRsf = new RsfCompare(oracle);
    // RsfCompare.compareTo returns 0 if files have the same contents
    assertEquals(resultRsf.compareTo(oracleRsf), 0);
  }

  @Test
  public void mainTest1() {
    // Cluster Josh's ARCADE version
    char fs = File.separatorChar;
    String depsRsfFilename = "." + fs + "src" + fs + "test" + fs + "resources"
      + fs + "JavaSourceToDepsBuilderTest_resources_old"
      + fs + "arcade_old_deps_oracle.rsf";
    String clusterRsfFilename = "." + fs + "target" + fs + "test_results"
      + fs + "ACDCTest" + fs + "mainTest1Result.rsf";

    // Run ACDC
    ACDC.run(depsRsfFilename, clusterRsfFilename);
    String result = assertDoesNotThrow(() ->
      { return FileUtil.readFile(clusterRsfFilename, StandardCharsets.UTF_8);});

    // Load oracle
    String oraclePath = "." + fs + "src" + fs + "test" + fs + "resources"
      + fs + "ACDCTest_resources_old" + fs + "arcade_old_cluster_oracle.rsf";
    String oracle = assertDoesNotThrow(() ->
      { return FileUtil.readFile(oraclePath, StandardCharsets.UTF_8); });

    // Sort results
    Set<String> resultSet =
      new HashSet<>(Arrays.asList(result.split(System.lineSeparator())));
    Set<String> oracleSet =
      new HashSet<>(Arrays.asList(oracle.split(System.lineSeparator())));

    // Check oracle
    assertEquals(resultSet, oracleSet);
  }

  @Test
  public void mainTest2() {
    // Cluster httpd version 2.3.8
    char fs = File.separatorChar;
    String depsRsfFilename = "." + fs + "src" + fs + "test" + fs + "resources"
      + fs + "ACDCTest_resources_old"
      + fs + "httpd-2.3.8_deps.rsf";
    String clusterRsfFilename = "." + fs + "target" + fs + "test_results"
      + fs + "ACDCTest" + fs + "mainTest2Result.rsf";

    // Run ACDC
    ACDC.run(depsRsfFilename, clusterRsfFilename);
    String result = assertDoesNotThrow(() ->
      { return FileUtil.readFile(clusterRsfFilename, StandardCharsets.UTF_8);});

    // Load oracle
    String oraclePath = "." + fs + "src" + fs + "test" + fs + "resources"
      + fs + "ACDCTest_resources_old" + fs + "httpd-2.3.8_cluster_oracle.rsf";
    String oracle = assertDoesNotThrow(() ->
      { return FileUtil.readFile(oraclePath, StandardCharsets.UTF_8); });

    // Sort results
    Set<String> resultSet =
      new HashSet<>(Arrays.asList(result.split(System.lineSeparator())));
    Set<String> oracleSet =
      new HashSet<>(Arrays.asList(oracle.split(System.lineSeparator())));

    // Check oracle
    assertEquals(resultSet, oracleSet);
  }
}