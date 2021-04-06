package edu.usc.softarch.arcade.decay;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    String resourcesDir = ".///src///test///resources////decay_metrics_resources";

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
			() -> assertTrue(rciVals.get("n").equals(rciOracles.get("n"))),
			() -> assertTrue(rciVals.get("min").equals(rciOracles.get("min"))),
			() -> assertTrue(rciVals.get("max").equals(rciOracles.get("max"))),
			() -> assertTrue(rciVals.get("mean").equals(rciOracles.get("mean"))),
			() -> assertTrue(rciVals.get("std dev").equals(rciOracles.get("std dev"))),
			() -> assertTrue(rciVals.get("median").equals(rciOracles.get("median"))),
			() -> assertTrue(rciVals.get("skewness").equals(rciOracles.get("skewness"))),
			() -> assertTrue(rciVals.get("kurtosis").equals(rciOracles.get("kurtosis")))
     	);

	  	// check twoway values
		Map<String, Double> twowayVals = allStats.get("twoway");
		Map<String, Double> twowayOracles = oracleStats.get("twoway");
		assertAll(
			() -> assertTrue(twowayVals.get("n").equals(twowayOracles.get("n"))),
			() -> assertTrue(twowayVals.get("min").equals(twowayOracles.get("min"))),
			() -> assertTrue(twowayVals.get("max").equals(twowayOracles.get("max"))),
			() -> assertTrue(twowayVals.get("mean").equals(twowayOracles.get("mean"))),
			() -> assertTrue(twowayVals.get("std dev").equals(twowayOracles.get("std dev"))),
			() -> assertTrue(twowayVals.get("median").equals(twowayOracles.get("median"))),
			() -> assertTrue(twowayVals.get("skewness").equals(twowayOracles.get("skewness"))),
			() -> assertTrue(twowayVals.get("kurtosis").equals(twowayOracles.get("kurtosis")))
      	);

		// check stability values
		Map<String, Double> stabilityVals = allStats.get("stability");
		Map<String, Double> stabilityOracles = oracleStats.get("stability");
		assertAll(
			() -> assertTrue(stabilityVals.get("n").equals(stabilityOracles.get("n"))),
			() -> assertTrue(stabilityVals.get("min").equals(stabilityOracles.get("min"))),
			() -> assertTrue(stabilityVals.get("max").equals(stabilityOracles.get("max"))),
			() -> assertTrue(stabilityVals.get("mean").equals(stabilityOracles.get("mean"))),
			() -> assertTrue(stabilityVals.get("std dev").equals(stabilityOracles.get("std dev"))),
			() -> assertTrue(stabilityVals.get("median").equals(stabilityOracles.get("median"))),
			() -> assertTrue(stabilityVals.get("skewness").equals(stabilityOracles.get("skewness"))),
			() -> assertTrue(stabilityVals.get("kurtosis").equals(stabilityOracles.get("kurtosis")))
      	);

		// check mq values
		Map<String, Double> mqVals = allStats.get("mq");
		Map<String, Double> mqOracles = oracleStats.get("mq");
		assertAll(
			() -> assertTrue(mqVals.get("n").equals(mqOracles.get("n"))),
			() -> assertTrue(mqVals.get("min").equals(mqOracles.get("min"))),
			() -> assertTrue(mqVals.get("max").equals(mqOracles.get("max"))),
			() -> assertTrue(mqVals.get("mean").equals(mqOracles.get("mean"))),
			() -> assertTrue(mqVals.get("std dev").equals(mqOracles.get("std dev"))),
			() -> assertTrue(mqVals.get("median").equals(mqOracles.get("median"))),
			() -> assertTrue(mqVals.get("skewness").equals(mqOracles.get("skewness"))),
			() -> assertTrue(mqVals.get("kurtosis").equals(mqOracles.get("kurtosis")))
      	);
    }
}
