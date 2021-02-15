package edu.usc.softarch.arcade.clustering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import edu.usc.softarch.arcade.clustering.util.ClusterUtil;
import edu.usc.softarch.arcade.config.Config;
import edu.usc.softarch.arcade.config.Config.SimMeasure;
import edu.usc.softarch.arcade.topics.DistributionSizeMismatchException;
import edu.usc.softarch.arcade.topics.DocTopics;
import edu.usc.softarch.arcade.topics.TopicUtil;
import edu.usc.softarch.arcade.topics.UnmatchingDocTopicItemsException;
import edu.usc.softarch.arcade.util.StopWatch;

public class ConcernClusteringRunner extends ClusteringAlgoRunner {
	private static Logger logger =
		LogManager.getLogger(ConcernClusteringRunner.class);
	private String language;
	
	/**
	 * @param vecs feature vectors (dependencies) of entities
	 * @param srcDir directories with java or c files
	 * @param numTopics number of topics to extract
	 */
	ConcernClusteringRunner(FastFeatureVectors vecs,
			String srcDir, String artifactsDir, String language) {
		setFastFeatureVectors(vecs);
		initializeClusters(srcDir); // Initially, every node gets a cluster
		initializeDocTopicsForEachFastCluster(srcDir, artifactsDir);
		this.language = language;
	}
	
	public void computeClustersWithConcernsAndFastClusters(
			StoppingCriterion stoppingCriterion) {
		StopWatch loopSummaryStopwatch = new StopWatch();
		loopSummaryStopwatch.start();

		List<List<Double>> simMatrix = createSimilarityMatrix(fastClusters);

		while (stoppingCriterion.notReadyToStop()) {
			if (Config.stoppingCriterion
					.equals(Config.StoppingCriterionConfig.clustergain)) {
				double clusterGain = ClusterUtil.computeClusterGainUsingTopics(fastClusters);
				checkAndUpdateClusterGain(clusterGain);
			}

			MaxSimData data  = identifyMostSimClusters(simMatrix);
			printDataForTwoMostSimilarClustersWithTopicsForConcerns(data);
			FastCluster newCluster = mergeFastClustersUsingTopics(data);
			updateFastClustersAndSimMatrixToReflectMergedCluster(data, newCluster, simMatrix);
			performPostProcessingConditionally();

			logger.debug("after merge, clusters size: " + fastClusters.size());
		}

		loopSummaryStopwatch.stop();
		logger.debug("Time in milliseconds to compute clusters: "
				+ loopSummaryStopwatch.getElapsedTime());
		logger.debug("max cluster gain: " + maxClusterGain);
		logger.debug("num clusters at max cluster gain: "
				+ numClustersAtMaxClusterGain);
	}
	
	private static MaxSimData identifyMostSimClusters(List<List<Double>> simMatrix) {
		if (simMatrix.size() != fastClusters.size())
			throw new IllegalArgumentException("expected simMatrix.size():"
				+ simMatrix.size() + " to be fastClusters.size(): "
				+ fastClusters.size());
		for (List<Double> col : simMatrix)
			if (col.size() != fastClusters.size())
				throw new IllegalArgumentException("expected col.size():" + col.size()
					+ " to be fastClusters.size(): " + fastClusters.size());
		
		int length = simMatrix.size();
		MaxSimData msData = new MaxSimData();
		msData.rowIndex = 0;
		msData.colIndex = 0;
		double smallestJsDiv = Double.MAX_VALUE;

		for (int i=0; i < length; i++) {
			for (int j=0; j < length; j++) {
				double currJsDiv = simMatrix.get(i).get(j);
				if (currJsDiv < smallestJsDiv && i != j) {
					smallestJsDiv = currJsDiv;
					msData.rowIndex = i;
					msData.colIndex = j;
				}
			}
		}

		msData.currentMaxSim = smallestJsDiv;
		return msData;
	}

	private void initializeDocTopicsForEachFastCluster(String srcDir,
			String artifactsDir) {
		logger.debug("Initializing doc-topics for each cluster...");

		try {
			TopicUtil.docTopics = new DocTopics(srcDir, artifactsDir, this.language);
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (FastCluster c : fastClusters)
			TopicUtil.setDocTopicForFastClusterForMalletApi(c, this.language);
		
		List<FastCluster> jspRemoveList = new ArrayList<>();
		for (FastCluster c : fastClusters) {
			if (c.getName().endsWith("_jsp")) {
				logger.debug("Adding " + c.getName() + " to jspRemoveList...");
				jspRemoveList.add(c);
			}
		}

		logger.debug("Removing jspRemoveList from fastCluters");
		for (FastCluster c : jspRemoveList)
			fastClusters.remove(c);
		
		Map<String,String> parentClassMap = new HashMap<>();
		for (FastCluster c : fastClusters) {
			if (c.getName().contains("$")) {
				logger.debug("Nested class singleton cluster with missing doc topic: " + c.getName());
				String[] tokens = c.getName().split("\\$");
				String parentClassName = tokens[0];
				parentClassMap.put(c.getName(), parentClassName);
			}
		}
		
		logger.debug("Removing singleton clusters with no doc-topic and are non-inner classes...");
		List<FastCluster> excessClusters = new ArrayList<>();
		for (FastCluster c : fastClusters) {
			if (c.docTopicItem == null && !c.getName().contains("$")) {
				logger.error("Could not find doc-topic for non-inner class: " + c.getName());
				excessClusters.add(c);
			}
		}
		
		List<FastCluster> excessInners = new ArrayList<>();
		for (FastCluster excessCluster : excessClusters) {
			for (FastCluster cluster : fastClusters) {
				if (parentClassMap.containsKey(cluster)) {
					String parentClass = parentClassMap.get(cluster);
					if (parentClass.equals(excessCluster.getName()))
						excessInners.add(cluster);
				}
			}
		}
		
		fastClusters.removeAll(excessClusters);
		fastClusters.removeAll(excessInners);

		ArrayList<FastCluster> updatedFastClusters = new ArrayList<>(fastClusters);
		for (String key : parentClassMap.keySet()) {
			for (FastCluster nestedCluster : fastClusters) {
				if (nestedCluster.getName().equals(key)) {
					for (FastCluster parentCluster : fastClusters) {
						if (parentClassMap.get(key).equals(parentCluster.getName())) {
							FastCluster mergedCluster = mergeFastClustersUsingTopics(nestedCluster,parentCluster);
							updatedFastClusters.remove(parentCluster);
							updatedFastClusters.remove(nestedCluster);
							updatedFastClusters.add(mergedCluster);
						}
					}
				}
			}
		}

		fastClusters = updatedFastClusters;
		
		List<FastCluster> clustersWithMissingDocTopics = new ArrayList<>();
		for (FastCluster c : fastClusters) {
			if (c.docTopicItem == null) {
				logger.error("Could not find doc-topic for: " + c.getName());
				clustersWithMissingDocTopics.add(c);
			}
		}
		
		logger.debug("Removing clusters with missing doc topics...");
		fastClusters.removeAll(clustersWithMissingDocTopics);

		boolean ignoreMissingDocTopics = true;
		if (ignoreMissingDocTopics) {
			logger.debug("Removing clusters with missing doc topics...");
			for (FastCluster c : clustersWithMissingDocTopics) {
				logger.debug("Removing cluster: " + c.getName());
				fastClusters.remove(c);
			}
			logger.debug("New initial clusters size: " + fastClusters.size());
		}
		
		logger.debug("New initial fast clusters:");
		logger.debug(String.join("\n", fastClusters.stream()
			.map(FastCluster::toString).collect(Collectors.toList())));
	}
	
	private static void printDataForTwoMostSimilarClustersWithTopicsForConcerns(
			MaxSimData data) {
		if (logger.isDebugEnabled()) {
			logger.debug("In, "
					+ Thread.currentThread().getStackTrace()[1].getMethodName()
					+ ", \nMax Similar Clusters: ");
			logger.debug("sim value(" + data.rowIndex + "," + data.colIndex + "): " + data.currentMaxSim);
			logger.debug("\n");
			logger.debug("most sim clusters: " + fastClusters.get(data.rowIndex).getName() + ", " + fastClusters.get(data.colIndex).getName());
			TopicUtil.printTwoDocTopics(fastClusters.get(data.rowIndex).docTopicItem,
					fastClusters.get(data.colIndex).docTopicItem);

			logger.debug("before merge, fast clusters size: "
					+ fastClusters.size());
		}
	}
	
	private static FastCluster mergeFastClustersUsingTopics(MaxSimData data) {
		FastCluster cluster = fastClusters.get(data.rowIndex);
		FastCluster otherCluster = fastClusters.get(data.colIndex);
		return mergeFastClustersUsingTopics(cluster, otherCluster);
	}

	private static FastCluster mergeFastClustersUsingTopics(
			FastCluster cluster, FastCluster otherCluster) {
		FastCluster newCluster =
			new FastCluster(ClusteringAlgorithmType.LIMBO, cluster, otherCluster);
		
		try {
			newCluster.docTopicItem = TopicUtil.mergeDocTopicItems(
				cluster.docTopicItem, otherCluster.docTopicItem);
		} catch (UnmatchingDocTopicItemsException e) {
			e.printStackTrace(); //TODO handle it
		}

		return newCluster;
	}
	
	private static void updateFastClustersAndSimMatrixToReflectMergedCluster(
			MaxSimData data, FastCluster newCluster, List<List<Double>> simMatrix) {
		FastCluster cluster = fastClusters.get(data.rowIndex);
		FastCluster otherCluster = fastClusters.get(data.colIndex);
		
		int greaterIndex = -1;
		int lesserIndex = -1;
		if (data.rowIndex == data.colIndex)
			throw new IllegalArgumentException("data.rowIndex: " + data.rowIndex
				+ " should not be the same as data.colIndex: " + data.colIndex);
		
		if (data.rowIndex > data.colIndex) {
			greaterIndex = data.rowIndex;
			lesserIndex = data.colIndex;
		}
		if (data.rowIndex < data.colIndex) {
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
		for (int i=0; i < fastClusters.size(); i++)
			newRow.add(Double.MAX_VALUE);
		
		simMatrix.add(newRow);
		
		// adding a new value to create new column for all but the last row, which
		// already has the column for the new cluster
		for (int i=0; i < fastClusters.size()-1; i++)
			simMatrix.get(i).add(Double.MAX_VALUE);
		
		if (simMatrix.size() != fastClusters.size())
			throw new RuntimeException("simMatrix.size(): " + simMatrix.size()
				+ " is not equal to fastClusters.size(): " + fastClusters.size());
		
		for (int i=0; i < fastClusters.size(); i++)
			if ( simMatrix.get(i).size() != fastClusters.size() )
				throw new RuntimeException("simMatrix.get(" + i + ").size(): "
					+ simMatrix.get(i).size() + " is not equal to fastClusters.size(): "
					+ fastClusters.size());
	
		
		for (int i=0; i < fastClusters.size(); i++) {
			FastCluster currCluster = fastClusters.get(i);
			double currJSDivergence = 0;
			if (Config.getCurrSimMeasure().equals(SimMeasure.js)) {
				try {
					currJSDivergence = TopicUtil.jsDivergence(newCluster.docTopicItem, currCluster.docTopicItem);
				} catch (DistributionSizeMismatchException e) {
					e.printStackTrace(); //TODO handle it
				}
			}
			else if (Config.getCurrSimMeasure().equals(SimMeasure.scm)) {
				currJSDivergence = FastSimCalcUtil.getStructAndConcernMeasure(newCluster, currCluster);
			}
			else {
				throw new IllegalArgumentException("Invalid similarity measure: " + Config.getCurrSimMeasure());
			}
			simMatrix.get(fastClusters.size()-1).set(i, currJSDivergence);
			simMatrix.get(i).set(fastClusters.size()-1, currJSDivergence);
		}
	}
	
	public static List<List<Double>> createSimilarityMatrix(
			List<FastCluster> clusters) {
		List<List<Double>> simMatrixObj = new ArrayList<>(clusters.size());
		
		for (int i=0;i<clusters.size();i++) {
			simMatrixObj.add(new ArrayList<>(clusters.size()));
		}

		for (int i=0;i<clusters.size();i++) {
			FastCluster cluster = clusters.get(i);
			for (int j=0;j<clusters.size();j++) {
				FastCluster otherCluster = clusters.get(j);
				boolean isShowingEachSimilarityComparison = false;
				if (isShowingEachSimilarityComparison) {
					if (logger.isDebugEnabled()) {
						logger.debug("Comparing " + cluster.getName() + " to "
								+ otherCluster.getName());
						TopicUtil.printTwoDocTopics(cluster.docTopicItem,
								otherCluster.docTopicItem);
					}
				}

				double currJSDivergence = 0;

				if (Config.getCurrSimMeasure().equals(SimMeasure.js)) {
					try {
						currJSDivergence = TopicUtil.jsDivergence(cluster.docTopicItem, otherCluster.docTopicItem);
					} catch (DistributionSizeMismatchException e) {
						e.printStackTrace(); //TODO handle it
					}
				}
				else if (Config.getCurrSimMeasure().equals(SimMeasure.scm)) {
					currJSDivergence = FastSimCalcUtil.getStructAndConcernMeasure(cluster, otherCluster);
				}
				else {
					throw new IllegalArgumentException("Invalid similarity measure: " + Config.getCurrSimMeasure());
				}
				
				simMatrixObj.get(i).add(currJSDivergence);
			}
		}
		return simMatrixObj;
	}
}