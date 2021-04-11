package edu.usc.softarch.arcade.metrics;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import edu.usc.softarch.arcade.util.FileListing;
import edu.usc.softarch.arcade.util.FileUtil;

public class MoJoEvolutionAnalyzerTest {
  private static Map<String, Map<String, Double>> results = new HashMap<>();
  private static Map<String, Map<String, Double>> oracles = new HashMap<>();
  private static Map<String, List<String>> files = new HashMap<>();

  public static Map<String, Double> setUp(String clustersDir) {
    Map<String, Double> mojoMap = results.get(clustersDir);
    if (mojoMap != null) return mojoMap;

    // Map to mojoFmValues and associated cluster files
    mojoMap = new HashMap<>();
    results.put(clustersDir, mojoMap);

    // Copied from MoJoEvolutionAnalyzer.main()
    List<File> clusterFiles = null;
    try {
      clusterFiles = FileListing.getFileListing(new File(FileUtil.tildeExpandPath(clustersDir)));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      fail("cluster files directory does not exist");
    }
    // FileUtil.sortFileListByVersion sorts the list by the versioning scheme found in the filename
    clusterFiles = FileUtil.sortFileListByVersion(clusterFiles);
    // Only testing comparison distance of 1
    int comparisonDistance = 1;
    File prevFile = null;
    List<Double> mojoFmValues = new ArrayList<>();
    // List to store the compared versions
    List<String> mojoFiles = new ArrayList<>();
    for (int i = 0; i < clusterFiles.size(); i += comparisonDistance) {
      File currFile = clusterFiles.get(i);
      // exclude annoying .ds_store files from OSX
      if (!currFile.getName().equals(".DS_Store")) {
        if (prevFile != null && currFile != null) {
          double mojoFmValue = MoJoEvolutionAnalyzer.doMoJoFMComparison(currFile, prevFile);
          mojoFmValues.add(mojoFmValue);
          mojoMap.put(currFile.getName() + " " + prevFile.getName(), mojoFmValue);
          mojoFiles.add(currFile.getName() + " " + prevFile.getName());
        }
        prevFile = currFile;
      }
    }
    files.put(clustersDir, mojoFiles);
    Double[] mojoFmArr = new Double[mojoFmValues.size()];
    mojoFmValues.toArray(mojoFmArr);
    DescriptiveStatistics stats = new DescriptiveStatistics(
      Arrays.stream(mojoFmArr).mapToDouble(Double::valueOf).toArray());

    // Place metrics in map
    mojoMap.put("n", (double) (stats.getN()));
    mojoMap.put("min", stats.getMin());
    mojoMap.put("max", stats.getMax());
    mojoMap.put("mean", stats.getMean());
    mojoMap.put("std dev", stats.getStandardDeviation());
    mojoMap.put("median", stats.getPercentile(50));
    mojoMap.put("skewness", stats.getSkewness());
    mojoMap.put("kurtosis", stats.getKurtosis());

    return mojoMap;
  }

  public static Map<String, Double> readOracle(String oraclePath) {
    Map<String, Double> oracleMojoMap = oracles.get(oraclePath);
    if (oracleMojoMap != null) return oracleMojoMap;

    // Map to mojoFmValues and associated cluster files
    oracleMojoMap = new HashMap<>();
    oracles.put(oraclePath, oracleMojoMap);

    // Read in oracle file
    List<List<String>> records = new ArrayList<>();
    oraclePath.replace("///", File.separator);
    try (BufferedReader br = new BufferedReader(new FileReader(oraclePath))) {
      String line;
      while ((line = br.readLine()) != null) {
        String[] values = line.split(",");
        records.add(Arrays.asList(values));
      }
    } catch (IOException e) {
      e.printStackTrace();
      fail("failed to read in oracle metrics file");
    }

    // records.get(0) contains the mojoFmValues
    for (int i = 1; i < records.get(0).size(); i += 3){
      oracleMojoMap.put(records.get(0).get(i) + " " + records.get(0).get(i + 1), Double.parseDouble(records.get(0).get(i + 2)));
    }
    // records.get(1) contains the metrics
    for (int i = 0; i < records.get(1).size(); i += 2){
      oracleMojoMap.put(records.get(1).get(i), Double.parseDouble(records.get(1).get(i + 1)));
    }

    return oracleMojoMap;
  }

  @ParameterizedTest
  @CsvSource({
    /** Test parameters **/
    // [directory with clusters rsf files]
    // [oracle file path]
    
    // Struts2 (acdc)
    ".///src///test///resources///MoJoEvolutionAnalyzerTest_resources///Struts2///acdc_clusters,"
    + ".///src///test///resources///MoJoEvolutionAnalyzerTest_resources///oracles///struts2_acdc_mojo_oracle.txt",

    // httpd (acdc)
    ".///src///test///resources///MoJoEvolutionAnalyzerTest_resources///httpd///acdc_clusters,"
    + ".///src///test///resources///MoJoEvolutionAnalyzerTest_resources///oracles///httpd_acdc_mojo_oracle.txt",

    // Struts2 (arc)
    ".///src///test///resources///MoJoEvolutionAnalyzerTest_resources///Struts2///arc_clusters,"
    + ".///src///test///resources///MoJoEvolutionAnalyzerTest_resources///oracles///struts2_arc_mojo_oracle.txt",

    // httpd (arc)
    ".///src///test///resources///MoJoEvolutionAnalyzerTest_resources///httpd///arc_clusters,"
    + ".///src///test///resources///MoJoEvolutionAnalyzerTest_resources///oracles///httpd_arc_mojo_oracle.txt",
  })
  public void mainTest(String clusters, String oracleFile){
    String oraclePath = oracleFile.replace("///", File.separator);
    String clustersDir = clusters.replace("///", File.separator);

    Map<String, Double> mojoMap = setUp(clustersDir);
    List<String> mojoFiles = files.get(clustersDir);
    Map<String, Double> oracleMojoMap = readOracle(oraclePath);

    // Compare mojoFmValues to oracle
    for (String file : mojoFiles){
      assertEquals(oracleMojoMap.get(file), mojoMap.get(file), "mojoFmValue from comparison between " + file + " does not match oracle");
    }

    // Compare result metrics to oracle metrics
    assertAll(
      () -> assertEquals(oracleMojoMap.get("n"), mojoMap.get("n"), "n value does not match the oracle"),
      () -> assertEquals(oracleMojoMap.get("min"), mojoMap.get("min"), "min value does not match the oracle"),
      () -> assertEquals(oracleMojoMap.get("max"), mojoMap.get("max"), "max value does not match the oracle"),
      () -> assertEquals(oracleMojoMap.get("mean"), mojoMap.get("mean"), "mean value does not match the oracle"),
      () -> assertEquals(oracleMojoMap.get("std dev"), mojoMap.get("std dev"), "std dev, value does not match the oracle"),
      () -> assertEquals(oracleMojoMap.get("median"), mojoMap.get("median"), "median value does not match the oracle"),
      () -> assertEquals(oracleMojoMap.get("skewness"), mojoMap.get("skewness"), "skewness value does not match the oracle"),
      () -> assertEquals(oracleMojoMap.get("kurtosis"), mojoMap.get("kurtosis"), "kurtosis value does not match the oracle")
    );
  }
}
