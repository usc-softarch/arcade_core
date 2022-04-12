package edu.usc.softarch.arcade.clustering.techniques;

import java.util.ArrayList;
import java.util.List;

import edu.usc.softarch.arcade.clustering.Cluster;

import edu.usc.softarch.arcade.clustering.FastSimCalcUtil;
import edu.usc.softarch.arcade.clustering.MaxSimData;
import edu.usc.softarch.arcade.clustering.criteria.StoppingCriterion;

public class WcaRunner extends ClusteringAlgoRunner {
	public static void computeClustersWithPQAndWCA(
			StoppingCriterion stopCriterion, String language,
			String stoppingCriterion, String simMeasure) {

		initializeClusters(null, language);
		
		List<List<Double>> simMatrix = fastClusters.computeUEMSimMatrix(simMeasure);

		while (stopCriterion.notReadyToStop()) {
			if (stoppingCriterion.equalsIgnoreCase("clustergain")) {
				double clusterGain = 0;
				clusterGain = fastClusters.computeStructuralClusterGain();
				checkAndUpdateClusterGain(clusterGain);
			}

			MaxSimData data  = identifyMostSimClusters(simMatrix);

			Cluster cluster = fastClusters.get(data.rowIndex);
			Cluster otherCluster = fastClusters.get(data.colIndex);
			Cluster newCluster = new Cluster(cluster, otherCluster);
			
			updateFastClustersAndSimMatrixToReflectMergedCluster(data, newCluster, simMatrix, simMeasure);
		}
	}
	
	private static void updateFastClustersAndSimMatrixToReflectMergedCluster(MaxSimData data,
																																					 Cluster newCluster, List<List<Double>> simMatrix, String simMeasure) {
		Cluster cluster = fastClusters.get(data.rowIndex);
		Cluster otherCluster = fastClusters.get(data.colIndex);
		
		int greaterIndex = -1;
		int lesserIndex = -1;
		if (data.rowIndex == data.colIndex)
			throw new IllegalArgumentException("data.rowIndex: " + data.rowIndex + " should not be the same as data.colIndex: " + data.colIndex);
		if (data.rowIndex > data.colIndex) {
			greaterIndex = data.rowIndex;
			lesserIndex = data.colIndex;
		}
		else if (data.rowIndex < data.colIndex) {
			greaterIndex = data.colIndex;
			lesserIndex = data.rowIndex;
		}
		
		simMatrix.remove(greaterIndex);
		for (List<Double> col : simMatrix)
			col.remove(greaterIndex);
		
		simMatrix.remove(lesserIndex);
		for (List<Double> col : simMatrix)
			col.remove(lesserIndex);
		
		fastClusters.remove(cluster);
		fastClusters.remove(otherCluster);
		
		fastClusters.add(newCluster);
		
		List<Double> newRow = new ArrayList<>(fastClusters.size());
		
		for (int i = 0; i < fastClusters.size(); i++)
			newRow.add(Double.MAX_VALUE);
		
		simMatrix.add(newRow);
		
		// adding a new value to create new column for all but the last row, which already has the column for the new cluster
		for (int i = 0; i < fastClusters.size() - 1; i++)
			simMatrix.get(i).add(Double.MAX_VALUE);
		
		if (simMatrix.size()!=fastClusters.size())
			throw new RuntimeException("simMatrix.size(): " + simMatrix.size() + " is not equal to fastClusters.size(): " + fastClusters.size());
		
		for (int i = 0; i < fastClusters.size(); i++)
			if (simMatrix.get(i).size() != fastClusters.size())
				throw new RuntimeException("simMatrix.get(" + i + ").size(): " + simMatrix.get(i).size() + " is not equal to fastClusters.size(): " + fastClusters.size());
	
		
		for (int i = 0; i < fastClusters.size(); i++) {
			Cluster currCluster = fastClusters.get(i);
			double currSimMeasure = 0; 
			if (simMeasure.equalsIgnoreCase("uem"))
				currSimMeasure = FastSimCalcUtil.getUnbiasedEllenbergMeasure(newCluster, currCluster);
			else if (simMeasure.equalsIgnoreCase("uemnm"))
				currSimMeasure = FastSimCalcUtil.getUnbiasedEllenbergMeasureNM(newCluster, currCluster);
			else
				throw new IllegalArgumentException(simMeasure + " is not a valid similarity measure for WCA");
			simMatrix.get(fastClusters.size()-1).set(i, currSimMeasure);
			simMatrix.get(i).set(fastClusters.size()-1, currSimMeasure);
		}
	}

	private static MaxSimData identifyMostSimClusters(List<List<Double>> simMatrix) {
		if ( simMatrix.size() != fastClusters.size() )
			throw new IllegalArgumentException("expected simMatrix.size():" + simMatrix.size() + " to be fastClusters.size(): " + fastClusters.size());
		for (List<Double> col : simMatrix)
			if (col.size() != fastClusters.size())
				throw new IllegalArgumentException("expected col.size():" + col.size() + " to be fastClusters.size(): " + fastClusters.size());
		
		int length = simMatrix.size();
		MaxSimData msData = new MaxSimData();
		msData.rowIndex = 0;
		msData.colIndex = 1;
		double greatestUnbiasedEllenberg = 0;
		boolean foundMoreSimilarMeasure = false;
		for (int i = 0; i < length; i++) {
			for (int j = 0; j < length; j++) {
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
			msData.foundMoreSimilarMeasure = foundMoreSimilarMeasure;
		}
		msData.currentMaxSim = greatestUnbiasedEllenberg;
		return msData;
	}
}