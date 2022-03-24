package edu.usc.softarch.arcade.clustering.techniques;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.usc.softarch.arcade.clustering.*;
import edu.usc.softarch.arcade.topics.DistributionSizeMismatchException;
import edu.usc.softarch.arcade.topics.DocTopics;
import edu.usc.softarch.arcade.topics.TopicUtil;
import edu.usc.softarch.arcade.topics.UnmatchingDocTopicItemsException;

public class ConcernClusteringRunner extends ClusteringAlgoRunner {
	// #region ATTRIBUTES --------------------------------------------------------
	private String language;
	// Initial fastClusters state before any clustering
	private Architecture initialFastClusters;
	// fastClusters state after initializing docTopics
	private Architecture fastClustersWithDocTopics;

	public static class PreSelectedStoppingCriterion
			implements StoppingCriterion {
		private int numClusters;

		public PreSelectedStoppingCriterion(int numClusters) {
			this.numClusters = numClusters; }

		public boolean notReadyToStop() {
			return ClusteringAlgoRunner.fastClusters.size() != 1
				&& ClusteringAlgoRunner.fastClusters.size() != numClusters;
		}
	}
	// #endregion ATTRIBUTES -----------------------------------------------------
	
	// #region CONSTRUCTORS ------------------------------------------------------
	/**
	 * @param vecs feature vectors (dependencies) of entities
	 * @param srcDir directories with java or c files
	 */
	public ConcernClusteringRunner(FastFeatureVectors vecs,
																 String srcDir, String artifactsDir, String language) {
		this.language = language;
		setFastFeatureVectors(vecs);

		// Initially, every node gets a cluster
		initializeClusters(srcDir, language);
		this.initialFastClusters = new Architecture(fastClusters);

		initializeDocTopicsForEachFastCluster(artifactsDir);
		this.fastClustersWithDocTopics = new Architecture(fastClusters);
	}
	// #endregion CONSTRUCTORS ---------------------------------------------------

	// #region ACCESSORS ---------------------------------------------------------
	public Architecture getInitialFastClusters() {
		return this.initialFastClusters; }

	public Architecture getFastClustersWithDocTopics() {
		return this.fastClustersWithDocTopics; }
	// #endregion ACCESSORS ------------------------------------------------------

	// #region INTERFACE ---------------------------------------------------------
	/**
	 * Entry point runner for ARC.
	 * 
	 * args[0]: Language of the subject system, java or c.
	 * args[1]: Path of the output directory to put the results in.
	 * args[2]: Path to the subject system's root directory.
	 * args[3]: Path to the FastFeatureVectors JSON file.
	 * args[4]: Path to the directory with the mallet artifacts.
	 * 
	 * @param args Arguments as per documentation above.
	 */
	public static void main(String[] args) throws IOException {
		String language = args[0];
		String outputDirPath = args[1];
		String sysDirPath = args[2];
		String ffVecsFilePath = args[3];
		String artifactsDirPath = args[4];

		runARC(language, outputDirPath, sysDirPath,
			ffVecsFilePath, artifactsDirPath);
	}

	/**
	 * Runs ARC.
	 * 
	 * @param language Language of the subject system, java or c.
	 * @param outputDirPath Path of the output directory to put the results in.
	 * @param sysDirPath Path to the subject system's root directory.
	 * @param ffVecsFilePath Path to the FastFeatureVectors JSON file.
	 * @param artifactsDirPath Path to the directory with the mallet artifacts.
	 */
	public static void runARC(String language, String outputDirPath,
			String sysDirPath, String ffVecsFilePath, String artifactsDirPath)
			throws IOException {
		String revisionNumber = (new File(sysDirPath)).getName();
		FastFeatureVectors ffVecs =
			FastFeatureVectors.deserializeFFVectors(ffVecsFilePath);
		int numTopics = (int) (ffVecs.getNumSourceEntities() * 0.18);

		ConcernClusteringRunner runner = new ConcernClusteringRunner(
			ffVecs, sysDirPath, artifactsDirPath, language);
		int numClusters = (int) (runner.getFastClusters().size() * .20);
		runner.computeClustersWithConcernsAndFastClusters(
			new ConcernClusteringRunner.PreSelectedStoppingCriterion(numClusters),
			"preselected", "js");
		
		String arcClustersFilename = outputDirPath + File.separator
			+ revisionNumber + "_" + numTopics + "_topics_"
			+ runner.getFastClusters().size()	+ "_arc_clusters.rsf";
		String docTopicsFilename = outputDirPath + File.separator
			+ revisionNumber + "_" + numTopics + "_topics_"
			+ runner.getFastClusters().size() + "_arc_docTopics.json";

		Map<String, Integer> clusterNameToNodeNumberMap =
			runner.getFastClusters().computeArchitectureIndex();
		runner.getFastClusters().writeToRsf(
			clusterNameToNodeNumberMap, arcClustersFilename);
		TopicUtil.docTopics.serializeDocTopics(docTopicsFilename);
	}
	// #endregion INTERFACE ------------------------------------------------------

	public void computeClustersWithConcernsAndFastClusters(
			StoppingCriterion stoppingCriterion, String stopCriterion,
			String simMeasure) {
		List<List<Double>> simMatrix =
			fastClusters.computeJSDivergenceSimMatrix(simMeasure);

		while (stoppingCriterion.notReadyToStop()) {
			if (stopCriterion.equalsIgnoreCase("clustergain")) {
				double clusterGain = fastClusters.computeTopicClusterGain();
				checkAndUpdateClusterGain(clusterGain);
			}

			MaxSimData data  = identifyMostSimClusters(simMatrix);
			Cluster newCluster = mergeFastClustersUsingTopics(data);
			updateFastClustersAndSimMatrixToReflectMergedCluster(data, newCluster, simMatrix, simMeasure);
		}
	}
	
	/**
	 * Looks for the smallest non-diagonal value in the matrix, which represents
	 * the pair of clusters with the lowest level of divergence (highest
	 * similarity).
	 * 
	 * @param simMatrix Similarity matrix to analyze.
	 * @return The maximum-similarity cell.
	 */
	private MaxSimData identifyMostSimClusters(List<List<Double>> simMatrix) {
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

		// Looks for the smallest value in the matrix
		for (int i = 0; i < length; i++) {
			for (int j = 0; j < length; j++) {
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

	private void initializeDocTopicsForEachFastCluster(String artifactsDir) {
		// Initialize DocTopics from files
		try {
			TopicUtil.docTopics = new DocTopics(artifactsDir);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Set the DocTopics of each Cluster
		for (Cluster c : fastClusters)
			TopicUtil.setDocTopicForFastClusterForMalletApi(c, this.language);
		
		// Map inner classes to their parents
		Map<String,String> parentClassMap = new HashMap<>();
		for (Cluster c : fastClusters) {
			if (c.getName().contains("$")) {
				String[] tokens = c.getName().split("\\$");
				String parentClassName = tokens[0];
				parentClassMap.put(c.getName(), parentClassName);
			}
		}
		
		removeClassesWithoutDTI(parentClassMap);

		Architecture updatedFastClusters =
			new Architecture(fastClusters);
		for (String key : parentClassMap.keySet()) {
			for (Cluster nestedCluster : fastClusters) {
				if (nestedCluster.getName().equals(key)) {
					for (Cluster parentCluster : fastClusters) {
						if (parentClassMap.get(key).equals(parentCluster.getName())) {
							Cluster mergedCluster = mergeFastClustersUsingTopics(nestedCluster,parentCluster);
							updatedFastClusters.remove(parentCluster);
							updatedFastClusters.remove(nestedCluster);
							updatedFastClusters.add(mergedCluster);
						}
					}
				}
			}
		}

		fastClusters = updatedFastClusters;
		
		Architecture clustersWithMissingDocTopics =
			new Architecture();
		for (Cluster c : fastClusters)
			if (c.docTopicItem == null)
				clustersWithMissingDocTopics.add(c);

		fastClusters.removeAll(clustersWithMissingDocTopics);

		boolean ignoreMissingDocTopics = true;
		if (ignoreMissingDocTopics)
			for (Cluster c : clustersWithMissingDocTopics)
				fastClusters.remove(c);
	}

	private void removeClassesWithoutDTI(Map<String, String> parentClassMap) {
		// Locate non-inner classes without DTI
		Architecture excessClusters = new Architecture();
		for (Cluster c : fastClusters)
			if (c.docTopicItem == null && !c.getName().contains("$"))
				excessClusters.add(c);
		
		// Locate inner classes of those non-inner classes without DTI
		Architecture excessInners = new Architecture();
		for (Cluster excessCluster : excessClusters) {
			for (Cluster cluster : fastClusters) {
				if (parentClassMap.containsKey(cluster.getName())) {
					String parentClass = parentClassMap.get(cluster.getName());
					if (parentClass.equals(excessCluster.getName()))
						excessInners.add(cluster);
				}
			}
		}

		// Remove them from the analysis
		fastClusters.removeAll(excessClusters);
		fastClusters.removeAll(excessInners);
	}
	
	private static Cluster mergeFastClustersUsingTopics(MaxSimData data) {
		Cluster cluster = fastClusters.get(data.rowIndex);
		Cluster otherCluster = fastClusters.get(data.colIndex);
		return mergeFastClustersUsingTopics(cluster, otherCluster);
	}

	private static Cluster mergeFastClustersUsingTopics(
					Cluster cluster, Cluster otherCluster) {
		Cluster newCluster =
			new Cluster(ClusteringAlgorithmType.LIMBO, cluster, otherCluster);
		
		try {
			newCluster.docTopicItem = TopicUtil.mergeDocTopicItems(
				cluster.docTopicItem, otherCluster.docTopicItem);
		} catch (UnmatchingDocTopicItemsException e) {
			e.printStackTrace(); //TODO handle it
		}

		return newCluster;
	}
	
	private static void updateFastClustersAndSimMatrixToReflectMergedCluster(
					MaxSimData data, Cluster newCluster, List<List<Double>> simMatrix,
					String simMeasure) {
		Cluster cluster = fastClusters.get(data.rowIndex);
		Cluster otherCluster = fastClusters.get(data.colIndex);
		
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
		for (int i = 0; i < fastClusters.size(); i++)
			newRow.add(Double.MAX_VALUE);
		
		simMatrix.add(newRow);
		
		// adding a new value to create new column for all but the last row, which
		// already has the column for the new cluster
		for (int i = 0; i < fastClusters.size() - 1; i++)
			simMatrix.get(i).add(Double.MAX_VALUE);
		
		if (simMatrix.size() != fastClusters.size())
			throw new RuntimeException("simMatrix.size(): " + simMatrix.size()
				+ " is not equal to fastClusters.size(): " + fastClusters.size());
		
		for (int i = 0; i < fastClusters.size(); i++)
			if (simMatrix.get(i).size() != fastClusters.size())
				throw new RuntimeException("simMatrix.get(" + i + ").size(): "
					+ simMatrix.get(i).size() + " is not equal to fastClusters.size(): "
					+ fastClusters.size());
	
		
		for (int i = 0; i < fastClusters.size(); i++) {
			Cluster currCluster = fastClusters.get(i);
			double currJSDivergence = 0;
			if (simMeasure.equalsIgnoreCase("js")) {
				try {
					currJSDivergence =
						newCluster.docTopicItem.getJsDivergence(currCluster.docTopicItem);
				} catch (DistributionSizeMismatchException e) {
					e.printStackTrace(); //TODO handle it
				}
			}
			else if (simMeasure.equalsIgnoreCase("scm"))
				currJSDivergence = FastSimCalcUtil.getStructAndConcernMeasure(newCluster, currCluster);
			else
				throw new IllegalArgumentException("Invalid similarity measure: " + simMeasure);

			simMatrix.get(fastClusters.size()-1).set(i, currJSDivergence);
			simMatrix.get(i).set(fastClusters.size()-1, currJSDivergence);
		}
	}
}
