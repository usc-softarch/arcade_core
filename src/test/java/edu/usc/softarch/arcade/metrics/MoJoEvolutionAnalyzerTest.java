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

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import edu.usc.softarch.arcade.util.FileListing;
import edu.usc.softarch.arcade.util.FileUtil;

public class MoJoEvolutionAnalyzerTest {
  String resourcesDir = ".///src///test///resources///MoJoEvolutionAnalyzerTest_resources";
  @ParameterizedTest
  @CsvSource({
      // Struts2
      "///Struts2///clusters,"
      + "///oracles///struts2_mojo_oracle.txt",

      // httpd
      "///httpd///clusters,"
      + "///oracles///httpd_mojo_oracle.txt",
  })
  public void mainTest(String clusters, String oracleFile){
    String resDir = resourcesDir.replace("///", File.separator);
    String oraclePath = resDir + oracleFile.replace("///", File.separator);
    String clustersDir = resDir + clusters.replace("///", File.separator);

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
    // Map to mojoFmValues and associated cluster files
    HashMap<String, Double> mojoMap = new HashMap<>();
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
    Double[] mojoFmArr = new Double[mojoFmValues.size()];
    mojoFmValues.toArray(mojoFmArr);
    DescriptiveStatistics stats = new DescriptiveStatistics(
      Arrays.stream(mojoFmArr).mapToDouble(Double::valueOf).toArray());


    // Place metrics in map
    HashMap<String, Double> statsMap = new HashMap<>();
    statsMap.put("n", (double) (stats.getN()));
    statsMap.put("min", stats.getMin());
    statsMap.put("max", stats.getMax());
    statsMap.put("mean", stats.getMean());
    statsMap.put("std dev", stats.getStandardDeviation());
    statsMap.put("median", stats.getPercentile(50));
    statsMap.put("skewness", stats.getSkewness());
    statsMap.put("kurtosis", stats.getKurtosis());

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
    HashMap<String, Double> oracleMojoMap = new HashMap<>();
    for (int i = 1; i < records.get(0).size(); i += 3){
      oracleMojoMap.put(records.get(0).get(i) + " " + records.get(0).get(i + 1), Double.parseDouble(records.get(0).get(i + 2)));
    }
    // records.get(1) contains the metrics
    HashMap<String, Double> oracleMetricsMap = new HashMap<>();
    for (int i = 0; i < records.get(1).size(); i += 2){
      oracleMetricsMap.put(records.get(1).get(i), Double.parseDouble(records.get(1).get(i + 1)));
    }

    // Compare mojoFmValues to oracle
    for (String file : mojoFiles){
      assertEquals(oracleMojoMap.get(file), mojoMap.get(file));
    }

    // Compare result metrics to oracle metrics
    assertAll(
      () -> assertEquals(oracleMetricsMap.get("n"), statsMap.get("n")),
      () -> assertEquals(oracleMetricsMap.get("min"), statsMap.get("min")),
      () -> assertEquals(oracleMetricsMap.get("max"), statsMap.get("max")),
      () -> assertEquals(oracleMetricsMap.get("mean"), statsMap.get("mean")),
      () -> assertEquals(oracleMetricsMap.get("std dev"), statsMap.get("std dev")),
      () -> assertEquals(oracleMetricsMap.get("median"), statsMap.get("median")),
      () -> assertEquals(oracleMetricsMap.get("skewness"), statsMap.get("skewness")),
      () -> assertEquals(oracleMetricsMap.get("kurtosis"), statsMap.get("kurtosis"))
    );
  }
}
