package edu.usc.softarch.arcade.clustering.techniques;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import edu.usc.softarch.arcade.clustering.Architecture;
import edu.usc.softarch.arcade.clustering.Cluster;
import edu.usc.softarch.arcade.clustering.ClusteringAlgorithmType;
import edu.usc.softarch.arcade.clustering.FeatureVectors;
import edu.usc.softarch.arcade.clustering.SimData;
import edu.usc.softarch.arcade.clustering.SimilarityMatrix;
import edu.usc.softarch.arcade.clustering.criteria.PreSelectedStoppingCriterion;
import edu.usc.softarch.arcade.clustering.criteria.StoppingCriterion;
import edu.usc.softarch.arcade.topics.DistributionSizeMismatchException;
import edu.usc.softarch.arcade.topics.DocTopicItem;
import edu.usc.softarch.arcade.topics.DocTopics;
import edu.usc.softarch.arcade.topics.UnmatchingDocTopicItemsException;

public class ConcernClusteringRunner extends ClusteringAlgoRunner {
	// #region ATTRIBUTES --------------------------------------------------------
	private final String language;
	private DocTopics docTopics;
	// #endregion ATTRIBUTES -----------------------------------------------------
	
	// #region CONSTRUCTORS ------------------------------------------------------
	/**
	 * @param vecs feature vectors (dependencies) of entities
	 */
	public ConcernClusteringRunner(FeatureVectors vecs, String artifactsDir,
			String language) {
		this.language = language;
		setFeatureVectors(vecs);
		initializeClusters(language);
		initializeClusterDocTopics(artifactsDir);
	}
	// #endregion CONSTRUCTORS ---------------------------------------------------

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
		FeatureVectors ffVecs =
			FeatureVectors.deserializeFFVectors(ffVecsFilePath);
		int numTopics = (int) (ffVecs.getNumSourceEntities() * 0.18);

		ConcernClusteringRunner runner = new ConcernClusteringRunner(
			ffVecs, artifactsDirPath, language);
		int numClusters = (int) (runner.getArchitecture().size() * .20);

		try {
			runner.computeArchitecture(
				new PreSelectedStoppingCriterion(numClusters, runner),
				"preselected", SimilarityMatrix.SimMeasure.JS);
		} catch (DistributionSizeMismatchException e) {
			e.printStackTrace(); //TODO Handle it
		}

		String prefix = outputDirPath + File.separator
			+ revisionNumber + "_" + numTopics + "_topics_"
			+ runner.getArchitecture().size();
		String arcClustersFilename = prefix	+ "_arc_clusters.rsf";
		String docTopicsFilename = prefix + "_arc_docTopics.json";

		Map<String, Integer> clusterNameToNodeNumberMap =
			runner.getArchitecture().computeArchitectureIndex();
		runner.getArchitecture().writeToRsf(
			clusterNameToNodeNumberMap, arcClustersFilename);
		runner.docTopics.serializeDocTopics(docTopicsFilename);
	}
	// #endregion INTERFACE ------------------------------------------------------

	public void computeArchitecture(
			StoppingCriterion stoppingCriterion, String stopCriterion,
			SimilarityMatrix.SimMeasure simMeasure) throws DistributionSizeMismatchException {
		SimilarityMatrix simMatrix = initializeSimMatrix(simMeasure);

		while (stoppingCriterion.notReadyToStop()) {
			if (stopCriterion.equalsIgnoreCase("clustergain")) {
				double clusterGain = architecture.computeTopicClusterGain();
				checkAndUpdateClusterGain(clusterGain);
			}

			SimData data = identifyMostSimClusters(simMatrix);
			Cluster newCluster = mergeClustersUsingTopics(data);
			updateFastClustersAndSimMatrixToReflectMergedCluster(
				data, newCluster, simMatrix);
		}
	}

	protected SimilarityMatrix initializeSimMatrix(SimilarityMatrix.SimMeasure simMeasure)
			throws DistributionSizeMismatchException {
		return new SimilarityMatrix(simMeasure, this.architecture);	}

	protected void initializeClusterDocTopics(String artifactsDir) {
		try	{
			this.docTopics = DocTopics.deserializeDocTopics(artifactsDir + File.separator + "docTopics.json");
		} catch (IOException e) {
			e.printStackTrace();
			// Initialize DocTopics from files
			try {
				this.docTopics = new DocTopics(artifactsDir);
			} catch (Exception f) {
				f.printStackTrace();
			}
		}

		// Set the DocTopics of each Cluster
		for (Cluster c : architecture.values())
			this.docTopics.setClusterDocTopic(c, this.language);

		// Map inner classes to their parents
		Map<String,String> oldParentClassMap = new HashMap<>();
		for (Cluster c : architecture.values()) {
			if (c.getName().contains("$")) {
				String[] tokens = c.getName().split("\\$");
				String parentClassName = tokens[0];
				oldParentClassMap.put(c.getName(), parentClassName);
			}
		}

		Map<Cluster, Cluster> parentClassMap = new LinkedHashMap<>();
		for (Cluster c : architecture.values()) {
			if (c.getName().contains("$")) {
				String[] tokens = c.getName().split("\\$");
				String parentClassName = tokens[0];
				parentClassMap.put(c, this.architecture.get(parentClassName));
			}
		}
		
		removeClassesWithoutDTI(parentClassMap);
		removeInnerClasses(oldParentClassMap);
		
		Architecture clustersWithMissingDocTopics =	new Architecture();
		for (Cluster c : architecture.values())
			if (c.docTopicItem == null)
				clustersWithMissingDocTopics.put(c.getName(), c);

		architecture.removeAll(clustersWithMissingDocTopics);

		for (Cluster c : clustersWithMissingDocTopics.values())
			architecture.remove(c.getName());
	}

	//TODO Change this to the correct format
	private void removeInnerClasses(Map<String, String> parentClassMap) {
		for (Map.Entry<String, String> entry : parentClassMap.entrySet()) {
			Cluster nestedCluster = this.architecture.get(entry.getKey());
			if (nestedCluster == null) continue; // was already removed by WithoutDTI
			Cluster parentCluster = this.architecture.get(entry.getValue());
			Cluster mergedCluster = mergeClustersUsingTopics(nestedCluster, parentCluster);
			architecture.remove(parentCluster.getName());
			architecture.remove(nestedCluster.getName());
			architecture.put(mergedCluster.getName(), mergedCluster);
		}
	}

	private void removeClassesWithoutDTI(Map<Cluster, Cluster> parentClassMap) {
		// Locate non-inner classes without DTI
		Architecture excessClusters = new Architecture();
		for (Cluster c : this.architecture.values())
			if (c.docTopicItem == null && !c.getName().contains("$"))
				excessClusters.put(c.getName(), c);

		Architecture excessInners = new Architecture();
		// For each Child/Parent pair, if the parent is marked, mark the child
		for (Map.Entry<Cluster, Cluster> entry : parentClassMap.entrySet()) {
			Cluster child = entry.getKey();
			Cluster parent = entry.getValue();
			if (excessClusters.containsKey(parent.getName()))
				excessInners.put(child.getName(), child);
		}

		// Remove them from the analysis
		architecture.removeAll(excessClusters);
		architecture.removeAll(excessInners);
	}
	
	private Cluster mergeClustersUsingTopics(SimData data) {
		Cluster cluster = data.c1;
		Cluster otherCluster = data.c2;
		return mergeClustersUsingTopics(cluster, otherCluster);
	}

	private Cluster mergeClustersUsingTopics(Cluster cluster, Cluster otherCluster) {
		Cluster newCluster =
			new Cluster(ClusteringAlgorithmType.LIMBO, cluster, otherCluster);
		
		try {
			newCluster.docTopicItem = new DocTopicItem(
				cluster.docTopicItem, otherCluster.docTopicItem);
		} catch (UnmatchingDocTopicItemsException e) {
			e.printStackTrace(); //TODO handle it
		}

		return newCluster;
	}
}
