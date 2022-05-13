package edu.usc.softarch.arcade.clustering.techniques;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import edu.usc.softarch.arcade.clustering.Architecture;
import edu.usc.softarch.arcade.clustering.Cluster;
import edu.usc.softarch.arcade.clustering.ClusteringAlgorithmType;
import edu.usc.softarch.arcade.clustering.SimData;
import edu.usc.softarch.arcade.clustering.SimilarityMatrix;
import edu.usc.softarch.arcade.clustering.criteria.PreSelectedStoppingCriterion;
import edu.usc.softarch.arcade.clustering.criteria.SerializationCriterion;
import edu.usc.softarch.arcade.clustering.criteria.StoppingCriterion;
import edu.usc.softarch.arcade.topics.DistributionSizeMismatchException;
import edu.usc.softarch.arcade.topics.DocTopicItem;
import edu.usc.softarch.arcade.topics.DocTopics;
import edu.usc.softarch.arcade.topics.UnmatchingDocTopicItemsException;

public class ConcernClusteringRunner extends ClusteringAlgoRunner {
	// #region INTERFACE ---------------------------------------------------------
	public static Architecture run(Architecture arch,
			SerializationCriterion serialCrit, StoppingCriterion stopCrit,
			String language, String stoppingCriterionName,
			SimilarityMatrix.SimMeasure simMeasure, String outputDirPath,
			String sysDirPath, String artifactsDirPath)
			throws IOException {
		String revisionNumber = (new File(sysDirPath)).getName();

		ConcernClusteringRunner runner = new ConcernClusteringRunner(
			language, serialCrit, arch, artifactsDirPath);

		// Overwrite numClusters with docTopicfied architecture
		int numClusters = (int) (runner.getArchitecture().size() * .20);
		if (stopCrit instanceof PreSelectedStoppingCriterion)
			((PreSelectedStoppingCriterion) stopCrit).setNumClusters(numClusters);

		try {
			runner.computeArchitecture(stopCrit, stoppingCriterionName,
				simMeasure);
		} catch (DistributionSizeMismatchException e) {
			e.printStackTrace(); //TODO Handle it
		}

		String prefix = outputDirPath + File.separator
			+ revisionNumber + "_" + runner.getArchitecture().size();
		String arcClustersFilename = prefix	+ "_arc_clusters.rsf";
		String docTopicsFilename = prefix + "_arc_docTopics.json";

		runner.getArchitecture().writeToRsf(arcClustersFilename);
		runner.docTopics.serializeDocTopics(docTopicsFilename);

		return runner.getArchitecture();
	}
	// #endregion INTERFACE ------------------------------------------------------

	// #region ATTRIBUTES --------------------------------------------------------
	private DocTopics docTopics;
	// #endregion ATTRIBUTES -----------------------------------------------------
	
	// #region CONSTRUCTORS ------------------------------------------------------
	public ConcernClusteringRunner(String language,
			SerializationCriterion serializationCriterion, Architecture arch,
			String artifactsDir) {
		super(language, serializationCriterion, arch);
		initializeClusterDocTopics(artifactsDir);
	}
	// #endregion CONSTRUCTORS ---------------------------------------------------

	@Override
	public Architecture computeArchitecture(
			StoppingCriterion stoppingCriterion, String stopCriterion,
			SimilarityMatrix.SimMeasure simMeasure)
			throws DistributionSizeMismatchException, FileNotFoundException {
		SimilarityMatrix simMatrix = initializeSimMatrix(simMeasure);

		while (stoppingCriterion.notReadyToStop(super.architecture)) {
			if (stopCriterion.equalsIgnoreCase("clustergain")) {
				double clusterGain = architecture.computeTopicClusterGain();
				checkAndUpdateClusterGain(clusterGain);
			}

			SimData data = identifyMostSimClusters(simMatrix);
			Cluster newCluster = mergeClustersUsingTopics(data);
			updateFastClustersAndSimMatrixToReflectMergedCluster(
				data, newCluster, simMatrix);

			if (super.serializationCriterion != null
				&& super.serializationCriterion.shouldSerialize()) {
				super.architecture.writeToRsf();
			}
		}

		return super.architecture;
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
			if (!c.hasDocTopicItem())
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
			if (!c.hasDocTopicItem() && !c.getName().contains("$"))
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
			new Cluster(ClusteringAlgorithmType.ARC, cluster, otherCluster);
		
		try {
			newCluster.setDocTopicItem(new DocTopicItem(cluster, otherCluster));
		} catch (UnmatchingDocTopicItemsException e) {
			e.printStackTrace(); //TODO handle it
		}

		return newCluster;
	}
}
