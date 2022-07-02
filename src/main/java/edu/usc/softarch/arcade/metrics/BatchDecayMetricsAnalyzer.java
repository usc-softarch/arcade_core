package edu.usc.softarch.arcade.metrics;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import edu.usc.softarch.arcade.util.FileUtil;

public class BatchDecayMetricsAnalyzer {
	private static Logger logger =
		LogManager.getLogger(BatchDecayMetricsAnalyzer.class);

	public static void main(String[] args) throws FileNotFoundException {
		// Directory containing all clustered rsf files
		String clustersDir = FileUtil.tildeExpandPath(args[0]);
		
		// Directory containing all deps rsf files
		String depsDir = FileUtil.tildeExpandPath(args[1]);
		
		List<File> clusterFiles = FileUtil.getFileListing(new File(clustersDir));
		List<File> depsFiles = FileUtil.getFileListing(new File(depsDir));
		
		clusterFiles = FileUtil.sortFileListByVersion(clusterFiles);
		
		Map<String,List<Double>> decayMetrics = new LinkedHashMap<>();

		String versionSchemeExpr = "[0-9]+\\.[0-9]+(\\.[0-9]+)*";
		for (File clusterFile : clusterFiles) {
			if (clusterFile.getName().endsWith(".rsf")) {
				String clusterVersion = FileUtil.extractVersion(versionSchemeExpr, clusterFile.getName());
				
				// Identify appropriate deps file version
				for (File depsFile : depsFiles) {
					if (depsFile.getName().endsWith(".rsf")) {
						String depsVersion = FileUtil.extractVersion(versionSchemeExpr, depsFile.getName());
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
		
		for (String key : decayMetrics.keySet()) {
			List<Double> vals = decayMetrics.get(key);
			double[] valArr = ArrayUtils.toPrimitive(vals.toArray(new Double[vals.size()]));
			DescriptiveStatistics stats = new DescriptiveStatistics(valArr);
			String header = "stats for " + key;
			System.out.println(header);
			logger.info(header);
			logger.info(stats);
			System.out.println(stats);
		}
	}
}