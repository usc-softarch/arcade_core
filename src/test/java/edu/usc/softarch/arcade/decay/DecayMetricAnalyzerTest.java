package edu.usc.softarch.arcade.decay;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import edu.usc.softarch.arcade.util.FileListing;
import edu.usc.softarch.arcade.util.FileUtil;

public class DecayMetricAnalyzerTest {
	String resourcesDir = ".///src///test///resources///decay_metrics_resources";

	@ParameterizedTest
	@CsvSource ({
			// httpd acdc
			"///httpd///acdc_cluster,"
			+ "///httpd///acdc_dep,"
			+ "///httpd///oracles///decay_metrics_oracle_httpd_acdc.txt",
			
			// struts acdc
			"///struts///acdc_cluster,"
			+ "///struts///acdc_dep,"
			+ "///struts///oracles///decay_metrics_oracle_struts_acdc.txt",
	})
	public void mainTest(String clusters, String deps, String oracle) {
		// Setup
		String resDir = FileUtil.tildeExpandPath(resourcesDir.replace("///", File.separator));
		String depsDir = FileUtil.tildeExpandPath(resDir + deps.replace("///", File.separator));
		String clustersDir = FileUtil.tildeExpandPath((resDir + clusters.replace("///", File.separator)));
			
		List<File> clusterFiles = assertDoesNotThrow(() -> {
			return FileListing.getFileListing(new File(clustersDir));
		});
		List<File> depsFiles = assertDoesNotThrow(() -> {
			return FileListing.getFileListing(new File(depsDir));
		});
	
		clusterFiles = FileUtil.sortFileListByVersion(clusterFiles);
		Map<String,List<Double>> decayMetrics = new LinkedHashMap<>();
		String versionSchemeExpr = "[0-9]+\\.[0-9]+(\\.[0-9]+)*";

		for (File clusterFile : clusterFiles) {
			if (!clusterFile.getName().endsWith(".rsf")) continue;
			String clusterVersion = FileUtil.extractVersionFromFilename(versionSchemeExpr, clusterFile.getName());
			
			for (File depsFile : depsFiles) {
				if (!depsFile.getName().endsWith(".rsf"))	continue;
				String depsVersion = FileUtil.extractVersionFromFilename(versionSchemeExpr, depsFile.getName());

				// Identify appropriate deps file version
				if (clusterVersion.equals(depsVersion)) {
					String[] dmaArgs = { clusterFile.getAbsolutePath(), depsFile.getAbsolutePath() };
					DecayMetricAnalyzer.main(dmaArgs);
					
					List<Double> rciVals = null;
					if (decayMetrics.get("rci") != null) {
						rciVals = decayMetrics.get("rci");
					}	else {
						rciVals = new ArrayList<>();
					}
					rciVals.add(DecayMetricAnalyzer.rciVal);
					decayMetrics.put("rci", rciVals);
					
					List<Double> twoWayRatios = null;
					if (decayMetrics.get("twoway") != null) {
						twoWayRatios = decayMetrics.get("twoway");
					}	else {
						twoWayRatios = new ArrayList<>();
					}
					twoWayRatios.add(DecayMetricAnalyzer.twoWayPairRatio);
					decayMetrics.put("twoway", twoWayRatios);
					
					List<Double> stabilityVals = null;
					if (decayMetrics.get("stability") != null) {
						stabilityVals = decayMetrics.get("stability");
					}	else {
						stabilityVals = new ArrayList<>();
					}
					stabilityVals.add(DecayMetricAnalyzer.avgStability);
					decayMetrics.put("stability", stabilityVals);
					
					List<Double> mqRatios = null;
					if (decayMetrics.get("mq") != null) {
						mqRatios = decayMetrics.get("mq");
					}	else {
						mqRatios = new ArrayList<>();
					}
					mqRatios.add(DecayMetricAnalyzer.mqRatio);
					decayMetrics.put("mq", mqRatios);
					
					break;
				}
			}
		}

		// construct the DescriptiveStatistics since we can't get kurtosis or skewness otherwise
		Map<String, Map<String, Double>> allStats = new LinkedHashMap<>();
		for (String key : decayMetrics.keySet()) {
			List<Double> vals = decayMetrics.get(key);
			double[] valArr = ArrayUtils.toPrimitive(vals.toArray(new Double[vals.size()]));
			Map<String, Double> rawStats = new LinkedHashMap<>();
			DescriptiveStatistics stats = new DescriptiveStatistics(valArr);

			rawStats.put("n", (double) (stats.getN()));
			rawStats.put("min", stats.getMin());
			rawStats.put("max", stats.getMax());
			rawStats.put("mean", stats.getMean());
			rawStats.put("std dev", stats.getStandardDeviation());
			rawStats.put("median", stats.getPercentile(50));
			rawStats.put("skewness",stats.getSkewness());
			rawStats.put("kurtosis",stats.getKurtosis());

			allStats.put(key, rawStats);
		}

		// parse oracle
		List<List<String>> records = new ArrayList<>();
		String oraclePath = resourcesDir + oracle;
		oraclePath.replace("///", File.separator);
		try (BufferedReader br = new BufferedReader(new FileReader(oraclePath))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] values = line.split(",");
				records.add(Arrays.asList(values));
			}
		} catch (IOException e) {
			e.printStackTrace();
			fail("Exception caught in DecayMetricAnalyzerTest mainTest");
		}

		Map<String, Map<String, Double>> oracleStats = new LinkedHashMap<>();
		for (int i = 0; i < records.size(); i++) {
			System.out.println(records.get(i).size());
			
			// first entry in each line is the key
			String oracleKey = records.get(i).get(0);
			Map<String,Double> oracleRawStats = new LinkedHashMap<>();

			for(int j = 1; j < records.get(i).size(); j+=2){
				oracleRawStats.put(records.get(i).get(j), Double.parseDouble(records.get(i).get(j+1)));
			}
			oracleStats.put(oracleKey, oracleRawStats);
		}

		// check rci values
		Map<String, Double> rciVals = allStats.get("rci");
		Map<String, Double> rciOracles = oracleStats.get("rci");
		assertAll(
			() -> assertEquals(rciOracles.get("n"), rciVals.get("n"), "RCI n value does not match the oracle"),
			() -> assertEquals(rciOracles.get("min"), rciVals.get("min"), "RCI min value does not match the oracle"),
			() -> assertEquals(rciOracles.get("max"), rciVals.get("max"), "RCI max value does not match the oracle"),
			() -> assertEquals(rciOracles.get("mean"), rciVals.get("mean"), "RCI mean value does not match the oracle"),
			() -> assertEquals(rciOracles.get("std dev"), rciVals.get("std dev"), "RCI std dev value does not match the oracle"),
			() -> assertEquals(rciOracles.get("median"), rciVals.get("median"), "RCI median value does not match the oracle"),
			() -> assertEquals(rciOracles.get("skewness"), rciVals.get("skewness"), "RCI skewness value does not match the oracle"),
			() -> assertEquals(rciOracles.get("kurtosis"), rciVals.get("kurtosis"), "RCI kurtosis value does not match the oracle")
		);

			// check twoway values
		Map<String, Double> twowayVals = allStats.get("twoway");
		Map<String, Double> twowayOracles = oracleStats.get("twoway");
		assertAll(
			() -> assertEquals(twowayOracles.get("n"), twowayVals.get("n"), "Two-way n value does not match the oracle"),
			() -> assertEquals(twowayOracles.get("min"), twowayVals.get("min"), "Two-way min value does not match the oracle"),
			() -> assertEquals(twowayOracles.get("max"), twowayVals.get("max"), "Two-way max value does not match the oracle"),
			() -> assertEquals(twowayOracles.get("mean"), twowayVals.get("mean"), "Two-way mean value does not match the oracle"),
			() -> assertEquals(twowayOracles.get("std dev"), twowayVals.get("std dev"), "Two-way std dev value does not match the oracle"),
			() -> assertEquals(twowayOracles.get("median"), twowayVals.get("median"), "Two-way median value does not match the oracle"),
			() -> assertEquals(twowayOracles.get("skewness"), twowayVals.get("skewness"), "Two-way skewness value does not match the oracle"),
			() -> assertEquals(twowayOracles.get("kurtosis"), twowayVals.get("kurtosis"), "Two-way kurtosis value does not match the oracle")
		);

		// check stability values
		Map<String, Double> stabilityVals = allStats.get("stability");
		Map<String, Double> stabilityOracles = oracleStats.get("stability");
		assertAll(
			() -> assertEquals(stabilityOracles.get("n"), stabilityVals.get("n"), "Stability n value does not match the oracle"),
			() -> assertEquals(stabilityOracles.get("min"), stabilityVals.get("min"), "Stability min value does not match the oracle"),
			() -> assertEquals(stabilityOracles.get("max"), stabilityVals.get("max"), "Stability max value does not match the oracle"),
			() -> assertEquals(stabilityOracles.get("mean"), stabilityVals.get("mean"), "Stability mean value does not match the oracle"),
			() -> assertEquals(stabilityOracles.get("std dev"), stabilityVals.get("std dev"), "Stability std dev value does not match the oracle"),
			() -> assertEquals(stabilityOracles.get("median"), stabilityVals.get("median"), "Stability median value does not match the oracle"),
			() -> assertEquals(stabilityOracles.get("skewness"), stabilityVals.get("skewness"), "Stability skewness value does not match the oracle"),
			() -> assertEquals(stabilityOracles.get("kurtosis"), stabilityVals.get("kurtosis"), "Stability kurtosis value does not match the oracle")
		);

		// check mq values
		Map<String, Double> mqVals = allStats.get("mq");
		Map<String, Double> mqOracles = oracleStats.get("mq");
		assertAll(
			() -> assertEquals(mqOracles.get("n"), mqVals.get("n"), "MQ n value does not match the oracle"),
			() -> assertEquals(mqOracles.get("min"), mqVals.get("min"), "MQ min value does not match the oracle"),
			() -> assertEquals(mqOracles.get("max"), mqVals.get("max"), "MQ max value does not match the oracle"),
			() -> assertEquals(mqOracles.get("mean"), mqVals.get("mean"), "MQ mean value does not match the oracle"),
			() -> assertEquals(mqOracles.get("std dev"), mqVals.get("std dev"), "MQ std dev value does not match the oracle"),
			() -> assertEquals(mqOracles.get("median"), mqVals.get("median"), "MQ median value does not match the oracle"),
			() -> assertEquals(mqOracles.get("skewness"), mqVals.get("skewness"), "MQ skewness value does not match the oracle"),
			() -> assertEquals(mqOracles.get("kurtosis"), mqVals.get("kurtosis"), "MQ kurtosis value does not match the oracle")
		);
	}
}
