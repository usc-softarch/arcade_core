package edu.usc.softarch.arcade.clustering;

import edu.usc.softarch.arcade.topics.DocTopicItem;
import edu.usc.softarch.arcade.topics.DocTopics;
import edu.usc.softarch.arcade.topics.UnmatchingDocTopicItemsException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ConcernArchitecture extends Architecture {
	//region ATTRIBUTES
	private transient DocTopics docTopics;
	//endregion

	//region CONSTRUCTORS
	public ConcernArchitecture(String projectName, String projectPath,
			FeatureVectors vectors, String language, String artifactsDir,
			String packagePrefix) {
		super(projectName, projectPath, vectors, language, packagePrefix);

		try	{
			this.docTopics = DocTopics.deserializeDocTopics(artifactsDir
				+ File.separator + "docTopics.json");
		} catch (IOException e) {
			System.out.println("No DocTopics file found, generating new one.");
			// Initialize DocTopics from files
			try {
				this.docTopics = new DocTopics(artifactsDir);
				this.docTopics.serializeDocTopics(artifactsDir
					+ File.separator + "docTopics.json");
			} catch (Exception f) {
				f.printStackTrace();
			}
		}

		initializeClusterDocTopics();
	}

	public ConcernArchitecture(String projectName, String projectPath,
			FeatureVectors vectors, String language, String artifactsDir) {
		this(projectName, projectPath, vectors, language,
			artifactsDir, "");
	}

	protected void initializeClusterDocTopics() {
		// Set the DocTopics of each Cluster
		for (Cluster c : super.values())
			this.docTopics.setClusterDocTopic(c, super.language);

		// Map inner classes to their parents
		Map<String,String> oldParentClassMap = new HashMap<>();
		for (Cluster c : super.values()) {
			if (c.getName().contains("$")) {
				String[] tokens = c.getName().split("\\$");
				String parentClassName = tokens[0];
				oldParentClassMap.put(c.getName(), parentClassName);
			}
		}

		Map<Cluster, Cluster> parentClassMap = new LinkedHashMap<>();
		for (Cluster c : super.values()) {
			if (c.getName().contains("$")) {
				String[] tokens = c.getName().split("\\$");
				String parentClassName = tokens[0];
				parentClassMap.put(c, super.get(parentClassName));
			}
		}

		removeClassesWithoutDTI(parentClassMap);
		removeInnerClasses(oldParentClassMap);

		Map<String, Cluster> clustersWithMissingDocTopics =	new HashMap<>();
		for (Cluster c : super.values())
			if (!c.hasDocTopicItem())
				clustersWithMissingDocTopics.put(c.getName(), c);

		super.removeAll(clustersWithMissingDocTopics);

		for (Cluster c : clustersWithMissingDocTopics.values())
			super.remove(c.getName());
	}

	//TODO Change this to the correct format
	private void removeInnerClasses(Map<String, String> parentClassMap) {
		for (Map.Entry<String, String> entry : parentClassMap.entrySet()) {
			Cluster nestedCluster = super.get(entry.getKey());
			if (nestedCluster == null) continue; // was already removed by WithoutDTI
			Cluster parentCluster = super.get(entry.getValue());
			Cluster mergedCluster = mergeClustersUsingTopics(nestedCluster, parentCluster);
			super.remove(parentCluster.getName());
			super.remove(nestedCluster.getName());
			super.put(mergedCluster.getName(), mergedCluster);
		}
	}

	private void removeClassesWithoutDTI(Map<Cluster, Cluster> parentClassMap) {
		// Locate non-inner classes without DTI
		Map<String, Cluster> excessClusters = new HashMap<>();
		for (Cluster c : super.values())
			if (!c.hasDocTopicItem() && !c.getName().contains("$"))
				excessClusters.put(c.getName(), c);

		Map<String, Cluster> excessInners = new HashMap<>();
		// For each Child/Parent pair, if the parent is marked, mark the child
		for (Map.Entry<Cluster, Cluster> entry : parentClassMap.entrySet()) {
			Cluster child = entry.getKey();
			Cluster parent = entry.getValue();
			if (excessClusters.containsKey(parent.getName()))
				excessInners.put(child.getName(), child);
		}

		// Remove them from the analysis
		super.removeAll(excessClusters);
		super.removeAll(excessInners);
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
	//endregion
}
