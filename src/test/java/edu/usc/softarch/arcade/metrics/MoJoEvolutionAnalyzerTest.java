package edu.usc.softarch.arcade.metrics;

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

			// // httpd
			// "///httpd///clusters,"
			// + "///oracles///httpd_mojo_oracle.txt",
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
		// Map for storing mojoFmValues and associated cluster files
		HashMap<List<String>, Double> mojoMap = new HashMap<>();
		// System.out.println("Comparison distance is: " + comparisonDistance);
		for (int i = 0; i < clusterFiles.size(); i += comparisonDistance) {
			File currFile = clusterFiles.get(i);
			// exclude annoying .ds_store files from OSX
			if (!currFile.getName().equals(".DS_Store")) {
				if (prevFile != null && currFile != null) {
					double mojoFmValue = MoJoEvolutionAnalyzer.doMoJoFMComparison(currFile, prevFile);
					mojoFmValues.add(mojoFmValue);
					mojoMap.put(Arrays.asList(currFile.getName(), prevFile.getName()), mojoFmValue);
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
		// System.out.println(stats);

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


	}

}
