package edu.usc.softarch.arcade.clustering;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.usc.softarch.arcade.clustering.util.ClusterUtil;
import edu.usc.softarch.arcade.config.Config;
import edu.usc.softarch.arcade.config.Config.SimMeasure;
import edu.usc.softarch.arcade.util.DebugUtil;
import edu.usc.softarch.arcade.util.StopWatch;

public class LimboRunner extends ClusteringAlgoRunner {
	
	private static Logger logger = Logger.getLogger(LimboRunner.class);

	public static void computeClusters(StoppingCriterion stopCriterion) {
		StopWatch loopSummaryStopwatch = new StopWatch();

		initializeClusters(null);

		// SimCalcUtil.verifySymmetricClusterOrdering(clusters);

		loopSummaryStopwatch.start();
		
		StopWatch matrixCreateTimer = new StopWatch();
		matrixCreateTimer.start();
		List<List<Double>> simMatrix = createSimilarityMatrix(fastClusters);
		matrixCreateTimer.stop();
		logger.debug("time to create similarity matrix: "
				+ matrixCreateTimer.getElapsedTime());

		int clusterStepCount = 0;
		int stepCountToStop = 5;
		boolean stopAtClusterStep = false; // for debugging purposes
		while (stopCriterion.notReadyToStop()) {
			if (Config.stoppingCriterion
					.equals(Config.StoppingCriterionConfig.clustergain)) {
				double clusterGain = 0;
				clusterGain = ClusterUtil
						.computeClusterGainUsingStructuralDataFromFastFeatureVectors(fastClusters);
				checkAndUpdateClusterGain(clusterGain);
			}

			//runClusterStepForWCA();
			StopWatch timer = new StopWatch();
			timer.start();
			//identifyMostSimClustersForConcernsMultiThreaded(data);
			MaxSimData data  = identifyMostSimClusters(simMatrix);
			timer.stop();
			logger.debug("time to identify two most similar clusters: "
					+ timer.getElapsedTime());
			
			printTwoMostSimilarClustersUsingStructuralData(data);
			
			FastCluster cluster = fastClusters.get(data.rowIndex);
			FastCluster otherCluster = fastClusters.get(data.colIndex);
			FastCluster newCluster = new FastCluster(ClusteringAlgorithmType.LIMBO, cluster, otherCluster);
			
			updateFastClustersAndSimMatrixToReflectMergedCluster(data,newCluster,simMatrix);
			
			if (stopAtClusterStep)
				clusterStepCount++;

			if (stopAtClusterStep) {
				if (clusterStepCount == stepCountToStop) {
					loopSummaryStopwatch.stop();

					logger.debug("Time in milliseconds to compute clusters after priority queue initialization: "
							+ loopSummaryStopwatch.getElapsedTime());

					DebugUtil.earlyExit();
				}
			}
			
			performPostProcessingConditionally();
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
		
		List<Double> newRow = new ArrayList<Double>(fastClusters.size());
		
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
			
			currSimMeasure = FastSimCalcUtil.getInfoLossMeasure(numberOfEntitiesToBeClustered,newCluster,currCluster);
			
			simMatrix.get(fastClusters.size()-1).set(i, currSimMeasure);
			simMatrix.get(i).set(fastClusters.size()-1, currSimMeasure);
		}
		
		
		// SimCalcUtil.verifySymmetricClusterOrdering(clusters);
		// newCluster.addClustersToPriorityQueue(clusters);
	}
	
	protected static void printTwoMostSimilarClustersUsingStructuralData(
			MaxSimData maxSimData) {
		if (logger.isDebugEnabled()) {
			logger.debug("In, "
					+ Thread.currentThread().getStackTrace()[1].getMethodName()
					+ ", \nMax Similar Clusters: ");

			ClusterUtil.printSimilarFeatures(fastClusters.get(maxSimData.rowIndex), fastClusters.get(maxSimData.colIndex),
					fastFeatureVectors);

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
		msData.colIndex = 0;
		double leastInfoLossMeasure = Double.MAX_VALUE;
		for (int i=0;i<length;i++) {
			for (int j=0;j<length;j++) {
				double currInfoLossMeasure = simMatrix.get(i).get(j);
				if (currInfoLossMeasure < leastInfoLossMeasure &&
					i != j) {
					leastInfoLossMeasure = currInfoLossMeasure;
					msData.rowIndex = i;
					msData.colIndex = j;
				}
			}
		}
		msData.currentMaxSim = leastInfoLossMeasure;
		return msData;
	}

	private static List<List<Double>> createSimilarityMatrix(
			ArrayList<FastCluster> clusters) {

		//HashMap<HashSet<String>, Double> map = new HashMap<HashSet<String>, Double>();
		
		List<List<Double>> simMatrixObj = new ArrayList<List<Double>>(clusters.size());
		
		for (int i=0;i<clusters.size();i++) {
			simMatrixObj.add(new ArrayList<Double>(clusters.size()));
		}

		for (int i=0;i<clusters.size();i++) {
			FastCluster cluster = clusters.get(i);
			for (int j=0;j<clusters.size();j++) {
				FastCluster otherCluster = clusters.get(j);

				double currSimMeasure = 0;
				currSimMeasure = FastSimCalcUtil.getInfoLossMeasure(numberOfEntitiesToBeClustered,cluster,
							otherCluster);
				
				
				simMatrixObj.get(i).add(currSimMeasure);
			}

		}
		
		return simMatrixObj;
	}

	
}
