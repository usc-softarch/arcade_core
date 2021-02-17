package edu.usc.softarch.arcade.clustering.techniques;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import edu.usc.softarch.arcade.clustering.FastCluster;
import edu.usc.softarch.arcade.clustering.FastSimCalcUtil;
import edu.usc.softarch.arcade.clustering.MaxSimData;
import edu.usc.softarch.arcade.clustering.StoppingCriterion;
import edu.usc.softarch.arcade.config.Config;
import edu.usc.softarch.arcade.config.Config.SimMeasure;
import edu.usc.softarch.arcade.util.StopWatch;

public class WcaRunner extends ClusteringAlgoRunner {
	
	private static Logger logger = LogManager.getLogger(WcaRunner.class);

	public static void computeClustersWithPQAndWCA(
			StoppingCriterion stopCriterion, String language) {
		StopWatch loopSummaryStopwatch = new StopWatch();

		initializeClusters(null, language);

		loopSummaryStopwatch.start();
		
		StopWatch matrixCreateTimer = new StopWatch();
		matrixCreateTimer.start();
		List<List<Double>> simMatrix =
			fastClusters.createSimilarityMatrixUsingUEM();
		matrixCreateTimer.stop();
		logger.debug("time to create similarity matrix: "
				+ matrixCreateTimer.getElapsedTime());

		while (stopCriterion.notReadyToStop()) {
			if (Config.stoppingCriterion
					.equals(Config.StoppingCriterionConfig.clustergain)) {
				double clusterGain = 0;
				clusterGain = fastClusters.computeClusterGainUsingStructuralData();
				checkAndUpdateClusterGain(clusterGain);
			}

			StopWatch timer = new StopWatch();
			timer.start();
			MaxSimData data  = identifyMostSimClusters(simMatrix);
			
			timer.stop();
			logger.debug("time to identify two most similar clusters: "
					+ timer.getElapsedTime());
			
			printTwoMostSimilarClustersUsingStructuralData(data);
			
			FastCluster cluster = fastClusters.get(data.rowIndex);
			FastCluster otherCluster = fastClusters.get(data.colIndex);
			FastCluster newCluster = new FastCluster(cluster, otherCluster);
			
			updateFastClustersAndSimMatrixToReflectMergedCluster(data,newCluster,simMatrix);
		}

		loopSummaryStopwatch.stop();

		logger.debug("Time in milliseconds to compute clusters after priority queue initialization: "
				+ loopSummaryStopwatch.getElapsedTime());
		logger.debug("max cluster gain: " + maxClusterGain);
		logger.debug("num clusters at max cluster gain: "
				+ numClustersAtMaxClusterGain);

	}
	
	private static void updateFastClustersAndSimMatrixToReflectMergedCluster(MaxSimData data,
			FastCluster newCluster, List<List<Double>> simMatrix) {
		FastCluster cluster = fastClusters.get(data.rowIndex);
		FastCluster otherCluster = fastClusters.get(data.colIndex);
		
		int greaterIndex = -1, lesserIndex = -1;
		if (data.rowIndex == data.colIndex) {
			throw new IllegalArgumentException("data.rowIndex: " + data.rowIndex + " should not be the same as data.colIndex: " + data.colIndex);
		}
		if (data.rowIndex > data.colIndex) {
			greaterIndex = data.rowIndex;
			lesserIndex = data.colIndex;
		}
		else if (data.rowIndex < data.colIndex) {
			greaterIndex = data.colIndex;
			lesserIndex = data.rowIndex;
		}
		
		simMatrix.remove(greaterIndex);
		for (List<Double> col : simMatrix) {
			col.remove(greaterIndex);
		}
		
		simMatrix.remove(lesserIndex);
		for (List<Double> col : simMatrix) {
			col.remove(lesserIndex);
		}
		
		fastClusters.remove(cluster);
		fastClusters.remove(otherCluster);
		
		fastClusters.add(newCluster);
		
		List<Double> newRow = new ArrayList<>(fastClusters.size());
		
		for (int i=0;i<fastClusters.size();i++) {
			newRow.add(Double.MAX_VALUE);
		}
		
		simMatrix.add(newRow);
		
		for (int i=0;i<fastClusters.size()-1;i++) { // adding a new value to create new column for all but the last row, which already has the column for the new cluster
			simMatrix.get(i).add(Double.MAX_VALUE);
		}
		
		if (simMatrix.size()!=fastClusters.size()) {
			throw new RuntimeException("simMatrix.size(): " + simMatrix.size() + " is not equal to fastClusters.size(): " + fastClusters.size());
		}
		
		for (int i=0;i<fastClusters.size();i++) {
			if ( simMatrix.get(i).size() != fastClusters.size() ) {
				throw new RuntimeException("simMatrix.get(" + i + ").size(): " + simMatrix.get(i).size() + " is not equal to fastClusters.size(): " + fastClusters.size());
			}
		}
	
		
		for (int i=0;i<fastClusters.size();i++) {
			FastCluster currCluster = fastClusters.get(i);
			double currSimMeasure = 0; 
			if (Config.getCurrSimMeasure().equals(SimMeasure.uem)) {
				currSimMeasure = FastSimCalcUtil.getUnbiasedEllenbergMeasure(newCluster,currCluster);
			}
			else if (Config.getCurrSimMeasure().equals(SimMeasure.uemnm)) {
				currSimMeasure = FastSimCalcUtil.getUnbiasedEllenbergMeasureNM(newCluster, currCluster);
			}
			else {
				throw new IllegalArgumentException(Config.getCurrSimMeasure() + " is not a valid similarity measure for WCA");
			}
			simMatrix.get(fastClusters.size()-1).set(i, currSimMeasure);
			simMatrix.get(i).set(fastClusters.size()-1, currSimMeasure);
		}
	}
	
	protected static void printTwoMostSimilarClustersUsingStructuralData(
			MaxSimData maxSimData) {
		if (logger.isDebugEnabled()) {
			logger.debug("In, "
					+ Thread.currentThread().getStackTrace()[1].getMethodName()
					+ ", \nMax Similar Clusters: ");

			fastClusters.get(maxSimData.rowIndex).printSimilarFeatures(
				fastClusters.get(maxSimData.colIndex), fastFeatureVectors);

			logger.debug(maxSimData.currentMaxSim);
			logger.debug("\n");

			logger.debug("before merge, clusters size: " + fastClusters.size());
		}
	}
	
	private static MaxSimData identifyMostSimClusters(
			List<List<Double>> simMatrix) {
		if ( simMatrix.size() != fastClusters.size() ) {
			throw new IllegalArgumentException("expected simMatrix.size():" + simMatrix.size() + " to be fastClusters.size(): " + fastClusters.size());
		}
		for (List<Double> col : simMatrix) {
			if (col.size() != fastClusters.size()) {
				throw new IllegalArgumentException("expected col.size():" + col.size() + " to be fastClusters.size(): " + fastClusters.size());
			}
		}
		
		int length = simMatrix.size();
		MaxSimData msData = new MaxSimData();
		msData.rowIndex = 0;
		msData.colIndex = 1;
		double greatestUnbiasedEllenberg = 0;
		boolean foundMoreSimilarMeasure = false;
		for (int i=0;i<length;i++) {
			for (int j=0;j<length;j++) {
				double currUnbiasedEllenbergMeasure = simMatrix.get(i).get(j);
				if (currUnbiasedEllenbergMeasure > greatestUnbiasedEllenberg &&
					i != j) {
					greatestUnbiasedEllenberg = currUnbiasedEllenbergMeasure;
					msData.rowIndex = i;
					msData.colIndex = j;
					foundMoreSimilarMeasure = true;
				}
			}
		}
		if (!foundMoreSimilarMeasure) {
			String couldNotFindMoreSimilarMeasureMsg = "Cannot find any similar entities...making arbitrary decision at " + fastClusters.size() + " clusters...";
			logger.debug(couldNotFindMoreSimilarMeasureMsg);
			msData.foundMoreSimilarMeasure = foundMoreSimilarMeasure;
		}
		msData.currentMaxSim = greatestUnbiasedEllenberg;
		return msData;
	}
}