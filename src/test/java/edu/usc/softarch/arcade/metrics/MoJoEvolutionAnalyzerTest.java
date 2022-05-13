package edu.usc.softarch.arcade.metrics;

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

import edu.usc.softarch.arcade.BaseTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import edu.usc.softarch.arcade.util.FileUtil;

public class MoJoEvolutionAnalyzerTest extends BaseTest {
	private final String resourcesDir = resourcesBase + fs + "MoJoFM";
	private final String oraclesDir = resourcesDir + fs + "oracles";

	/**
	 * Maps a system's name to a map of MoJo results.
	 */
  private static final Map<String, Map<String, Double>> results = new HashMap<>();
	/**
	 * Maps a system's name to a map of MoJo oracles.
	 */
  private static final Map<String, Map<String, Double>> oracles = new HashMap<>();
	/**
	 * Maps a system's name to a list of analyzed pairs of files.
	 */
  private static final Map<String, List<String>> files = new HashMap<>();

	/**
	 * Calculates the test results for a given system.
	 *
	 * @param clustersDir The path to the system's _clusters.rsf directory.
	 * @return A map of version pairs to MoJoFM values.
	 */
  public static Map<String, Double> setUp(String clustersDir) {
		// Short-circuit if the results already exist
    Map<String, Double> mojoMap = results.get(clustersDir);
    if (mojoMap != null) return mojoMap;

    // Create map to MoJoFmValues and associated cluster files
    mojoMap = new HashMap<>();
    results.put(clustersDir, mojoMap);

    // Get the list of _clusters.rsf files for input.
    List<File> clusterFiles = null;
    try {
      clusterFiles = FileUtil.getFileListing(
				new File(FileUtil.tildeExpandPath(clustersDir)));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      fail("cluster files directory does not exist");
    }

    // FileUtil.sortFileListByVersion sorts the list by version
    clusterFiles = FileUtil.sortFileListByVersion(clusterFiles);

    File prevFile = null;

		// List to store the actual MoJoFM values
    List<Double> mojoFmValues = new ArrayList<>();

    // List to store the compared versions
    List<String> mojoFiles = new ArrayList<>();

		// For each adjacent pair of files, calculate their MoJoFM values and
		// mark that pair of files as having been analyzed.
		for (File currFile : clusterFiles) {
			if (prevFile != null && currFile != null) {
				double mojoFmValue =
					MoJoEvolutionAnalyzer.doMoJoFMComparison(currFile, prevFile);
				mojoFmValues.add(mojoFmValue);
				mojoMap.put(currFile.getName() + " " + prevFile.getName(), mojoFmValue);
				mojoFiles.add(currFile.getName() + " " + prevFile.getName());
			}
			prevFile = currFile;
    }

		// Add the list of analyzed pairs of files to the files map.
    files.put(clustersDir, mojoFiles);

    return mojoMap;
  }

	/**
	 * Reads in the test oracles for a given system.
	 *
	 * @param oraclePath The path to the system's oracle directory.
	 * @return A map of version pairs to MoJoFM oracle values.
	 */
  public static Map<String, Double> readOracle(String oraclePath) {
		// Short-circuit if the oracles have already been read in
    Map<String, Double> oracleMojoMap = oracles.get(oraclePath);
    if (oracleMojoMap != null) return oracleMojoMap;

    // Create map to MoJoFMValues and associated cluster files
    oracleMojoMap = new HashMap<>();
    oracles.put(oraclePath, oracleMojoMap);

    // Read in oracle file
    List<List<String>> records = new ArrayList<>();
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

    return oracleMojoMap;
  }

	/**
	 * Tests MoJoFM's calculation against a given system and recovery technique.
	 *
	 * @param systemName Name of the system being tested with.
	 * @param recoveryTechnique Name of the recovery technique being tested with.
	 */
  @ParameterizedTest
  @CsvSource({
    // Struts2 (acdc)
		"Struts2,"
			+ "acdc",

    // httpd (acdc)
		"httpd,"
			+ "acdc",

    // Struts2 (arc)
		"Struts2,"
			+ "arc",

    // httpd (arc)
		"httpd,"
			+ "arc"
  })
  public void mainTest(String systemName, String recoveryTechnique) {
		String clustersDir = resourcesDir + fs + systemName
			+ fs + recoveryTechnique;
		String oraclePath = oraclesDir + fs + systemName + "_"
			+ recoveryTechnique + ".txt";

    Map<String, Double> mojoMap = setUp(clustersDir);
    List<String> mojoFiles = files.get(clustersDir);
    Map<String, Double> oracleMojoMap = readOracle(oraclePath);

    // Compare mojoFmValues to oracle
    for (String file : mojoFiles) {
      assertEquals(oracleMojoMap.get(file), mojoMap.get(file),
				"mojoFmValue from comparison between " + file
					+ " does not match oracle");
    }
  }
}
