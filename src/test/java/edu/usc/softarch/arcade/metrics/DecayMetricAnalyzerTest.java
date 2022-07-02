package edu.usc.softarch.arcade.metrics;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.usc.softarch.arcade.BaseTest;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import edu.usc.softarch.arcade.util.FileUtil;

public class DecayMetricAnalyzerTest extends BaseTest {
	private final String resourcesDir =
		resourcesBase + fs + "DecayMetricAnalyzer";
	private String projectDir;

  private Map<String, double[]> getResults(String clusteringAlgo) {
		String clusterDir = this.projectDir + fs + clusteringAlgo + "_cluster";
		String depDir = this.projectDir + fs + clusteringAlgo + "_dep";

    Map<String, double[]> resultStats = new LinkedHashMap<>();

		// Load in the input files lists
    List<File> clusterFiles = assertDoesNotThrow(
			() -> FileUtil.getFileListing(new File(clusterDir)));
    List<File> depsFiles = assertDoesNotThrow(
			() -> FileUtil.getFileListing(new File(depDir)));

		// Map files to their versions
		Map<String, File> clusterFilesMap = new HashMap<>();
		Map<String, File> depFilesMap = new HashMap<>();

		for (File clusterFile : clusterFiles)
			clusterFilesMap.put(clusterFile.getName(), clusterFile);
		for (File depsFile : depsFiles)
			depFilesMap.put(depsFile.getName(), depsFile);

		// Run DMA
		for (String version : clusterFilesMap.keySet()) {
			File clusterFile = clusterFilesMap.get(version);
			File depsFile = depFilesMap.get(version);

			double[] values = assertDoesNotThrow(() -> DecayMetricAnalyzer.run(
					clusterFile.getAbsolutePath(), depsFile.getAbsolutePath()));

			resultStats.put(version.replace(".rsf", ""), values);
		}

    return resultStats;
  }

  private static Map<String, double[]> readOracle(String oraclePath){
    Map<String, double[]> oracleStats = new LinkedHashMap<>();

    try (BufferedReader br = new BufferedReader(new FileReader(oraclePath))) {
      String line;
      while ((line = br.readLine()) != null) {
        String[] records = line.split(",");
				double[] values =
					{ Double.parseDouble(records[1]), Double.parseDouble(records[2]),
						Double.parseDouble(records[3]), Double.parseDouble(records[4]) };
				oracleStats.put(records[0], values);
      }
    } catch (IOException e) {
      e.printStackTrace();
      fail("Exception caught in DecayMetricAnalyzerTest readOracle");
    }

    return oracleStats;
  }

  @ParameterizedTest
  @CsvSource ({
    "httpd,"
		+ "acdc",

    "httpd,"
		+ "arc",

    "struts,"
		+ "acdc",

    "struts,"
		+ "arc",
  })
  public void mainTest(String projectName, String clusteringAlgo) {
		this.projectDir = resourcesDir + fs + projectName;
		String oraclesPath = this.projectDir + fs + "oracles" + fs
			+ "decay_metrics_oracle_" + projectName + "_" + clusteringAlgo + ".txt";

    Map<String, double[]> resultStats = getResults(clusteringAlgo);

		// ------------------------- Generate Oracles ------------------------------

		if (generateOracles) {
			assertDoesNotThrow(() -> {
				try (PrintWriter writer =
						new PrintWriter(oraclesPath, StandardCharsets.UTF_8)) {
					for (Map.Entry<String, double[]> entry : resultStats.entrySet()) {
						double[] values = entry.getValue();
						writer.println(entry.getKey() + ", " + values[0] + ", "
							+ values[1] + ", " + values[2] + ", " + values[3]);
					}
				}
			});
		}

		// ------------------------- Generate Oracles ------------------------------

    Map<String, double[]> oracleStats = readOracle(oraclesPath);

		for (String version : oracleStats.keySet())
			assertArrayEquals(oracleStats.get(version),
				resultStats.get(version), "Unmatching decay metrics.");
  }
}
