package edu.usc.softarch.arcade.metrics;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import edu.usc.softarch.arcade.BaseTest;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class SystemEvoTest extends BaseTest {
	private final String resourcesDir = resourcesBase + fs + "SystemEvo";
  private final String outputDirPath = outputBase + fs + "SystemEvoTest";

  @BeforeEach
  public void setUp(){
    File directory = new File(outputDirPath);
    directory.mkdirs();
  }

	/**
	 * Batch test of SystemEvo.
	 *
	 * @param clusterAlgo Clustering algorithm used to generate inputs.
	 * @param projectName Name of the subject system.
	 */
  @ParameterizedTest
  @CsvSource({
    // ACDC Struts2
    "acdc,"
		+ "Struts2",

    // ACDC httpd
    "acdc,"
		+ "httpd",

    // ARC Struts2
    "arc,"
		+ "Struts2",

    // ARC httpd
    "arc,"
		+ "httpd",
  })
  public void systemEvoTest(String clusterAlgo, String projectName) {
		String oraclePath =
			resourcesDir + fs + clusterAlgo + fs + projectName + "_oracle.csv";
		String clustersDir = resourcesDir + fs + clusterAlgo + fs + projectName;

		DescriptiveStatistics outputStats =
			assertDoesNotThrow(() -> SystemEvo.runBatch(clustersDir));
    
    try (BufferedReader br = new BufferedReader(new FileReader(oraclePath))) {
      Map<String, Double> oracleMap = new HashMap<>();
      String line;
      while ((line = br.readLine()) != null) {
        String[] arr = line.split(":");
        oracleMap.put(arr[0], Double.parseDouble(arr[1]));
      }

      assertAll(
        () -> assertEquals(oracleMap.get("n"),
					(double)(outputStats.getN()),"N does not match the oracle"),
        () -> assertEquals(oracleMap.get("min"),
					outputStats.getMin(),"Min does not match the oracle"),
        () -> assertEquals(oracleMap.get("max"),
					outputStats.getMax(),"Max does not match the oracle"),
        () -> assertEquals(oracleMap.get("mean"),
					outputStats.getMean(),"Mean does not match the oracle"),
        () -> assertEquals(oracleMap.get("std dev"),
					outputStats.getStandardDeviation(),"StandardDeviation does not match the oracle"),
        () -> assertEquals(oracleMap.get("median"),
					outputStats.getPercentile(50),"Median does not match the oracle"),
        () -> assertEquals(oracleMap.get("skewness"),
					outputStats.getSkewness(),"Skewness does not match the oracle"),
        () -> assertEquals(oracleMap.get("kurtosis"),
					outputStats.getKurtosis(),"Kurtosis does not match the oracle")
      );
    } catch(IOException e) {
      e.printStackTrace();
      fail("failed to read in oracle file");
    }
  }
}
