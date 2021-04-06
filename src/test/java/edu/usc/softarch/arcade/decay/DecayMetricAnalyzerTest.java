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
        // httpd
        "///httpd///acdc_cluster,"
        + "///httpd///acdc_dep,"
        + "///httpd///oracles///decay_metrics_oracle_httpd_acdc.txt",
        
        // struts
        "///struts///acdc_cluster,"
        + "///struts///acdc_dep,"
        + "///struts///oracles///decay_metrics_oracle_struts_acdc.txt",
    })
    public void mainTest(String clusters, String deps, String oracle){
        String resDir = FileUtil.tildeExpandPath(resourcesDir.replace("///", File.separator));
        String depsDir = FileUtil.tildeExpandPath(resDir + deps.replace("///", File.separator));
        String clustersDir = FileUtil.tildeExpandPath((resDir + clusters.replace("///", File.separator)));
        

        List<File> clusterFiles = assertDoesNotThrow( () -> {
            return FileListing.getFileListing(new File(clustersDir));
        });
		List<File> depsFiles = assertDoesNotThrow( () -> {
            return FileListing.getFileListing(new File(depsDir));
        });
		
		clusterFiles = FileUtil.sortFileListByVersion(clusterFiles);
		
		Map<String,List<Double>> decayMetrics = new LinkedHashMap<>();

		String versionSchemeExpr = "[0-9]+\\.[0-9]+(\\.[0-9]+)*";
		for (File clusterFile : clusterFiles) {
			if (clusterFile.getName().endsWith(".rsf")) {
				String clusterVersion = FileUtil.extractVersionFromFilename(versionSchemeExpr, clusterFile.getName());
				
				// Identify appropriate deps file version
				for (File depsFile : depsFiles) {
					if (depsFile.getName().endsWith(".rsf")) {
						String depsVersion = FileUtil.extractVersionFromFilename(versionSchemeExpr, depsFile.getName());
						if (clusterVersion.equals(depsVersion)) {
							String[] dmaArgs = {clusterFile.getAbsolutePath(),depsFile.getAbsolutePath()};
							DecayMetricAnalyzer.main(dmaArgs);
							
							List<Double> rciVals = null; 
							if (decayMetrics.get("rci") != null) {
								rciVals = decayMetrics.get("rci");
							}
							else {
								rciVals = new ArrayList<>();
							}
							rciVals.add(DecayMetricAnalyzer.rciVal);
							decayMetrics.put("rci", rciVals);
							
							List<Double> twoWayRatios = null;
							if (decayMetrics.get("twoway") != null) {
								twoWayRatios = decayMetrics.get("twoway");
							}
							else {
								twoWayRatios = new ArrayList<>();
							}
							twoWayRatios.add(DecayMetricAnalyzer.twoWayPairRatio);
							decayMetrics.put("twoway", twoWayRatios);
							
							List<Double> stabilityVals = null;
							if (decayMetrics.get("stability") != null) {
								stabilityVals = decayMetrics.get("stability");
							}
							else {
								stabilityVals = new ArrayList<>();
							}
							stabilityVals.add(DecayMetricAnalyzer.avgStability);
							decayMetrics.put("stability", stabilityVals);
							
							List<Double> mqRatios = null;
							if (decayMetrics.get("mq") != null) {
								mqRatios = decayMetrics.get("mq");
							}
							else {
								mqRatios = new ArrayList<>();
							}
							mqRatios.add(DecayMetricAnalyzer.mqRatio);
							decayMetrics.put("mq", mqRatios);
							
							break;
						}
					}
				}
			}
		}

        // construct the DescriptiveStatistics since we can't get kurtosis or skewness otherwise
        Map<String,Map<String,Double>> allStats = new LinkedHashMap<>();
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
        try{
            BufferedReader br = assertDoesNotThrow( () -> {
                return new BufferedReader(new FileReader(oraclePath));
            });
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                records.add(Arrays.asList(values));
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            fail("Exception caught in DecayMetricAnalyzerTest mainTest");
        }

		Map<String,Map<String, Double>> oracleStats = new LinkedHashMap<>();
        for(int i = 0; i < records.size(); i++){
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
			() -> assertEquals(rciVals.get("n"), rciOracles.get("n")),
			() -> assertEquals(rciVals.get("min"), rciOracles.get("min")),
			() -> assertEquals(rciVals.get("max"), rciOracles.get("max")),
			() -> assertEquals(rciVals.get("mean"), rciOracles.get("mean")),
			() -> assertEquals(rciVals.get("std dev"), rciOracles.get("std dev")),
			() -> assertEquals(rciVals.get("median"), rciOracles.get("median")),
			() -> assertEquals(rciVals.get("skewness"), rciOracles.get("skewness")),
			() -> assertEquals(rciVals.get("kurtosis"), rciOracles.get("kurtosis"))
     	);

	  	// check twoway values
		Map<String, Double> twowayVals = allStats.get("twoway");
		Map<String, Double> twowayOracles = oracleStats.get("twoway");
		assertAll(
			() -> assertEquals(twowayVals.get("n"), twowayOracles.get("n")),
			() -> assertEquals(twowayVals.get("min"), twowayOracles.get("min")),
			() -> assertEquals(twowayVals.get("max"), twowayOracles.get("max")),
			() -> assertEquals(twowayVals.get("mean"), twowayOracles.get("mean")),
			() -> assertEquals(twowayVals.get("std dev"), twowayOracles.get("std dev")),
			() -> assertEquals(twowayVals.get("median"), twowayOracles.get("median")),
			() -> assertEquals(twowayVals.get("skewness"), twowayOracles.get("skewness")),
			() -> assertEquals(twowayVals.get("kurtosis"), twowayOracles.get("kurtosis"))
      	);

		// check stability values
		Map<String, Double> stabilityVals = allStats.get("stability");
		Map<String, Double> stabilityOracles = oracleStats.get("stability");
		assertAll(
			() -> assertEquals(stabilityVals.get("n"), stabilityOracles.get("n")),
			() -> assertEquals(stabilityVals.get("min"), stabilityOracles.get("min")),
			() -> assertEquals(stabilityVals.get("max"), stabilityOracles.get("max")),
			() -> assertEquals(stabilityVals.get("mean"), stabilityOracles.get("mean")),
			() -> assertEquals(stabilityVals.get("std dev"), stabilityOracles.get("std dev")),
			() -> assertEquals(stabilityVals.get("median"), stabilityOracles.get("median")),
			() -> assertEquals(stabilityVals.get("skewness"), stabilityOracles.get("skewness")),
			() -> assertEquals(stabilityVals.get("kurtosis"), stabilityOracles.get("kurtosis"))
      	);

		// check mq values
		Map<String, Double> mqVals = allStats.get("mq");
		Map<String, Double> mqOracles = oracleStats.get("mq");
		assertAll(
			() -> assertEquals(mqVals.get("n"), mqOracles.get("n")),
			() -> assertEquals(mqVals.get("min"), mqOracles.get("min")),
			() -> assertEquals(mqVals.get("max"), mqOracles.get("max")),
			() -> assertEquals(mqVals.get("mean"), mqOracles.get("mean")),
			() -> assertEquals(mqVals.get("std dev"), mqOracles.get("std dev")),
			() -> assertEquals(mqVals.get("median"), mqOracles.get("median")),
			() -> assertEquals(mqVals.get("skewness"), mqOracles.get("skewness")),
			() -> assertEquals(mqVals.get("kurtosis"), mqOracles.get("kurtosis"))
      	);
    }
}
