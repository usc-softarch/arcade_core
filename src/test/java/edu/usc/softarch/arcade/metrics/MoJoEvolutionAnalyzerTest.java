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
  @ParameterizedTest
  @CsvSource({
      // Struts2
      ".///src///test///resources///MoJoEvolutionAnalyzerTest_resources///Struts2///clusters,"
      + ".///src///test///resources///MoJoEvolutionAnalyzerTest_resources///oracles///struts2_mojo_oracle.txt",

      // httpd
      ".///src///test///resources///MoJoEvolutionAnalyzerTest_resources///httpd///clusters,"
      + ".///src///test///resources///MoJoEvolutionAnalyzerTest_resources///oracles///httpd_mojo_oracle.txt",
  })
  public void mainTest(String clusters, String oracleFile){
    String oraclePath = oracleFile.replace("///", File.separator);
    String clustersDir = clusters.replace("///", File.separator);

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
    Map<String, Double> mojoMap = new HashMap<>();
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
    mojoMap.put("n", (double) (stats.getN()));
    mojoMap.put("min", stats.getMin());
    mojoMap.put("max", stats.getMax());
    mojoMap.put("mean", stats.getMean());
    mojoMap.put("std dev", stats.getStandardDeviation());
    mojoMap.put("median", stats.getPercentile(50));
    mojoMap.put("skewness", stats.getSkewness());
    mojoMap.put("kurtosis", stats.getKurtosis());

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
    Map<String, Double> oracleMojoMap = new HashMap<>();
    for (int i = 1; i < records.get(0).size(); i += 3){
      oracleMojoMap.put(records.get(0).get(i) + " " + records.get(0).get(i + 1), Double.parseDouble(records.get(0).get(i + 2)));
    }
    // records.get(1) contains the metrics
    for (int i = 0; i < records.get(1).size(); i += 2){
      oracleMojoMap.put(records.get(1).get(i), Double.parseDouble(records.get(1).get(i + 1)));
    }

    // Compare mojoFmValues to oracle
    for (String file : mojoFiles){
      assertEquals(oracleMojoMap.get(file), mojoMap.get(file));
    }

    // Compare result metrics to oracle metrics
    assertAll(
      () -> assertEquals(oracleMojoMap.get("n"), mojoMap.get("n")),
      () -> assertEquals(oracleMojoMap.get("min"), mojoMap.get("min")),
      () -> assertEquals(oracleMojoMap.get("max"), mojoMap.get("max")),
      () -> assertEquals(oracleMojoMap.get("mean"), mojoMap.get("mean")),
      () -> assertEquals(oracleMojoMap.get("std dev"), mojoMap.get("std dev")),
      () -> assertEquals(oracleMojoMap.get("median"), mojoMap.get("median")),
      () -> assertEquals(oracleMojoMap.get("skewness"), mojoMap.get("skewness")),
      () -> assertEquals(oracleMojoMap.get("kurtosis"), mojoMap.get("kurtosis"))
    );
  }
}
