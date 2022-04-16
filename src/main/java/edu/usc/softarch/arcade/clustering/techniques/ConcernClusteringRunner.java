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
import edu.usc.softarch.arcade.clustering.FastSimCalcUtil;
import edu.usc.softarch.arcade.clustering.MaxSimData;
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

	// Initial fastClusters state before any clustering
	private final Architecture initialArchitecture;
	// fastClusters state after initializing docTopics
	private final Architecture architectureWithDocTopics;
	// #endregion ATTRIBUTES -----------------------------------------------------
	
	// #region CONSTRUCTORS ------------------------------------------------------
	/**
	 * @param vecs feature vectors (dependencies) of entities
	 * @param srcDir directories with java or c files
	 */
	public ConcernClusteringRunner(FeatureVectors vecs, String srcDir,
			String artifactsDir, String language) {
		this.language = language;
		setFeatureVectors(vecs);

		// Initially, every node gets a cluster
		initializeClusters(srcDir, language);
		this.initialArchitecture = new Architecture(architecture);

		initializeDocTopicsForEachFastCluster(artifactsDir);
		this.architectureWithDocTopics = new Architecture(architecture);
	}
	// #endregion CONSTRUCTORS ---------------------------------------------------

	// #region ACCESSORS ---------------------------------------------------------
	public Architecture getInitialArchitecture() {
		return this.initialArchitecture; }

	public Architecture getArchitectureWithDocTopics() {
		return this.architectureWithDocTopics; }
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
		FeatureVectors ffVecs =
			FeatureVectors.deserializeFFVectors(ffVecsFilePath);
		int numTopics = (int) (ffVecs.getNumSourceEntities() * 0.18);

		ConcernClusteringRunner runner = new ConcernClusteringRunner(
			ffVecs, sysDirPath, artifactsDirPath, language);
		int numClusters = (int) (runner.getFastClusters().size() * .20);

		try {
			runner.computeClustersWithConcernsAndFastClusters(
				new PreSelectedStoppingCriterion(numClusters, runner),
				"preselected", "js");
		} catch (DistributionSizeMismatchException e) {
			e.printStackTrace(); //TODO Handle it
		}

		String prefix = outputDirPath + File.separator
			+ revisionNumber + "_" + numTopics + "_topics_"
			+ runner.getFastClusters().size();
		String arcClustersFilename = prefix	+ "_arc_clusters.rsf";
		String docTopicsFilename = prefix + "_arc_docTopics.json";

		Map<String, Integer> clusterNameToNodeNumberMap =
			runner.getFastClusters().computeArchitectureIndex();
		runner.getFastClusters().writeToRsf(
			clusterNameToNodeNumberMap, arcClustersFilename);
		runner.docTopics.serializeDocTopics(docTopicsFilename);
	}
	// #endregion INTERFACE ------------------------------------------------------

	public void computeClustersWithConcernsAndFastClusters(
			StoppingCriterion stoppingCriterion, String stopCriterion,
			String simMeasure) throws DistributionSizeMismatchException {
		SimilarityMatrix simMatrix = architecture.computeJSDivergenceSimMatrix(simMeasure);

		while (stoppingCriterion.notReadyToStop()) {
			if (stopCriterion.equalsIgnoreCase("clustergain")) {
				double clusterGain = architecture.computeTopicClusterGain();
				checkAndUpdateClusterGain(clusterGain);
			}

			MaxSimData data = identifyMostSimClusters(simMatrix);
			Cluster newCluster = mergeFastClustersUsingTopics(data);
			updateFastClustersAndSimMatrixToReflectMergedCluster(
				data, newCluster, simMatrix, simMeasure);
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
	private MaxSimData identifyMostSimClusters(SimilarityMatrix simMatrix) {
		if (simMatrix.size() != architecture.size())
			throw new IllegalArgumentException("expected simMatrix.size():"
				+ simMatrix.size() + " to be fastClusters.size(): "
				+ architecture.size());

		for (HashMap<Cluster, Double> col : simMatrix.values())
			if (col.size() != architecture.size())
				throw new IllegalArgumentException("expected col.size():" + col.size()
					+ " to be fastClusters.size(): " + architecture.size());
		
		MaxSimData msData = new MaxSimData();
		Map.Entry<Cluster, Cluster> minCell = null;
		try {
			minCell = simMatrix.getMinCell();
		} catch (Exception e) {
			e.printStackTrace(); //TODO Handle it
		}
		msData.c1 = minCell.getKey();
		msData.c2 = minCell.getValue();

		return msData;
	}

	private void initializeDocTopicsForEachFastCluster(String artifactsDir) {
		try	{
			this.docTopics = DocTopics.deserializeDocTopics(artifactsDir + File.separator + "docTopics.json");
		} catch (IOException e) {
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
		Map<String,String> parentClassMap = new HashMap<>();
		for (Cluster c : architecture.values()) {
			if (c.getName().contains("$")) {
				String[] tokens = c.getName().split("\\$");
				String parentClassName = tokens[0];
				parentClassMap.put(c.getName(), parentClassName);
			}
		}
		
		removeClassesWithoutDTI(parentClassMap);

		Architecture updatedArchitecture = new Architecture(architecture);
		for (String key : parentClassMap.keySet()) {
			for (Cluster nestedCluster : architecture.values()) {
				if (nestedCluster.getName().equals(key)) {
					for (Cluster parentCluster : architecture.values()) {
						if (parentClassMap.get(key).equals(parentCluster.getName())) {
							Cluster mergedCluster = mergeFastClustersUsingTopics(nestedCluster,parentCluster);
							updatedArchitecture.remove(parentCluster.getName());
							updatedArchitecture.remove(nestedCluster.getName());
							updatedArchitecture.put(mergedCluster.getName(), mergedCluster);
						}
					}
				}
			}
		}

		architecture = updatedArchitecture;
		
		Architecture clustersWithMissingDocTopics =	new Architecture();
		for (Cluster c : architecture.values())
			if (c.docTopicItem == null)
				clustersWithMissingDocTopics.put(c.getName(), c);

		architecture.removeAll(clustersWithMissingDocTopics);

		for (Cluster c : clustersWithMissingDocTopics.values())
			architecture.remove(c.getName());
	}

	private void removeClassesWithoutDTI(Map<String, String> parentClassMap) {
		// Locate non-inner classes without DTI
		Architecture excessClusters = new Architecture();
		for (Cluster c : architecture.values())
			if (c.docTopicItem == null && !c.getName().contains("$"))
				excessClusters.put(c.getName(), c);
		
		// Locate inner classes of those non-inner classes without DTI
		Architecture excessInners = new Architecture();
		for (Cluster excessCluster : excessClusters.values()) {
			for (Cluster cluster : architecture.values()) {
				if (parentClassMap.containsKey(cluster.getName())) {
					String parentClass = parentClassMap.get(cluster.getName());
					if (parentClass.equals(excessCluster.getName()))
						excessInners.put(cluster.getName(), cluster);
				}
			}
		}

		// Remove them from the analysis
		architecture.removeAll(excessClusters);
		architecture.removeAll(excessInners);
	}
	
	private Cluster mergeFastClustersUsingTopics(MaxSimData data) {
		Cluster cluster = data.c1;
		Cluster otherCluster = data.c2;
		return mergeFastClustersUsingTopics(cluster, otherCluster);
	}

	private Cluster mergeFastClustersUsingTopics(
			Cluster cluster, Cluster otherCluster) {
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
	
	private void updateFastClustersAndSimMatrixToReflectMergedCluster(
			MaxSimData data, Cluster newCluster, SimilarityMatrix simMatrix,
			String simMeasure) {
		// Sanity check
		if (data.c1.getName().equals(data.c2.getName()))
			throw new IllegalArgumentException("data.rowIndex: " + data.rowIndex
				+ " should not be the same as data.colIndex: " + data.colIndex);

		// Initializing variables
		Cluster cluster = data.c1;
		Cluster otherCluster = data.c2;

		// Remove the merged row and column from the matrix
		simMatrix.remove(cluster);
		simMatrix.remove(otherCluster);
		simMatrix.put(newCluster, new HashMap<>());

		// Remove merged clusters, add new cluster
		super.removeCluster(cluster);
		super.removeCluster(otherCluster);
		super.addCluster(newCluster);

		// Calculate new cluster divergence measure against all others
		for (Cluster currCluster : super.architecture.values()) {
			double currDivergence = 0;

			// Calculate it based on the selected similarity measure
			switch (simMeasure.toLowerCase()) {
				case "js":
					try {
						currDivergence = getJsDivergence(newCluster, currCluster);
					} catch (DistributionSizeMismatchException e) {
						e.printStackTrace(); //TODO handle it
					}
					break;
				case "scm":
					currDivergence = getScmDivergence(newCluster, currCluster);
					break;
				default:
					throw new IllegalArgumentException("Invalid similarity measure: " + simMeasure);
			}

			simMatrix.get(currCluster).put(newCluster, currDivergence);
			simMatrix.get(newCluster).put(currCluster, currDivergence);
		}
	}

	private double getScmDivergence(Cluster newCluster, Cluster currCluster) {
		return FastSimCalcUtil.getStructAndConcernMeasure(newCluster, currCluster);
	}

	private double getJsDivergence(Cluster newCluster, Cluster currCluster)
			throws DistributionSizeMismatchException {
		return newCluster.docTopicItem.getJsDivergence(currCluster.docTopicItem);
	}
}
