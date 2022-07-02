package edu.usc.softarch.arcade.clustering;

import com.fasterxml.jackson.annotation.JsonIgnore;
import edu.usc.softarch.arcade.clustering.simmeasures.SimMeasure;
import edu.usc.softarch.arcade.topics.Concern;
import edu.usc.softarch.arcade.topics.DocTopicItem;
import edu.usc.softarch.arcade.topics.DocTopics;
import edu.usc.softarch.arcade.topics.UnmatchingDocTopicItemsException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConcernArchitecture extends Architecture {
	//region ATTRIBUTES
	@JsonIgnore
	private transient DocTopics docTopics;
	//endregion

	//region CONSTRUCTORS
	public ConcernArchitecture(String projectName, String projectPath,
			SimMeasure.SimMeasureType simMeasure, FeatureVectors vectors,
			String language, String artifactsDir, String packagePrefix)
			throws UnmatchingDocTopicItemsException {
		super(projectName, projectPath, simMeasure,
			vectors, language, packagePrefix);

		try	{
			this.docTopics = DocTopics.deserialize(artifactsDir
				+ File.separator + "docTopics.json");
		} catch (IOException e) {
			System.out.println("No DocTopics file found, generating new one.");
			// Initialize DocTopics from files
			try {
				this.docTopics = new DocTopics(artifactsDir);
				this.docTopics.serialize(artifactsDir
					+ File.separator + "docTopics.json");
			} catch (Exception f) {
				f.printStackTrace();
			}
		}

		initializeClusterDocTopics();
	}

	public ConcernArchitecture(String projectName, String projectPath,
			SimMeasure.SimMeasureType simMeasure, FeatureVectors vectors,
			String language, String artifactsDir)
			throws UnmatchingDocTopicItemsException {
		this(projectName, projectPath, simMeasure, vectors, language,
			artifactsDir, "");
	}

	protected void initializeClusterDocTopics()
			throws UnmatchingDocTopicItemsException {
		// Set the DocTopics of each Cluster
		for (Cluster c : super.values())
			this.docTopics.setClusterDocTopic(c, super.language);

		// Map inner classes to their parents
		Map<String,String> oldParentClassMap = new HashMap<>();
		for (Cluster c : super.values()) {
			if (c.name.contains("$")) {
				String[] tokens = c.name.split("\\$");
				String parentClassName = tokens[0];
				oldParentClassMap.put(c.name, parentClassName);
			}
		}

		Map<Cluster, Cluster> parentClassMap = new LinkedHashMap<>();
		for (Cluster c : super.values()) {
			if (c.name.contains("$")) {
				String[] tokens = c.name.split("\\$");
				String parentClassName = tokens[0];
				parentClassMap.put(c, super.get(parentClassName));
			}
		}

		removeClassesWithoutDTI(parentClassMap);
		removeInnerClasses(oldParentClassMap);

		Map<String, Cluster> clustersWithMissingDocTopics =	new HashMap<>();
		for (Cluster c : super.values())
			if (!c.hasDocTopicItem())
				clustersWithMissingDocTopics.put(c.name, c);

		super.removeAll(clustersWithMissingDocTopics);

		for (Cluster c : clustersWithMissingDocTopics.values())
			super.remove(c.name);
	}

	//TODO Change this to the correct format
	private void removeInnerClasses(Map<String, String> parentClassMap)
			throws UnmatchingDocTopicItemsException {
		for (Map.Entry<String, String> entry : parentClassMap.entrySet()) {
			Cluster nestedCluster = super.get(entry.getKey());
			if (nestedCluster == null) continue; // was already removed by WithoutDTI
			Cluster parentCluster = super.get(entry.getValue());
			Cluster mergedCluster =
				new Cluster(ClusteringAlgorithmType.ARC, nestedCluster, parentCluster);
			super.remove(parentCluster.name);
			super.remove(nestedCluster.name);
			super.put(mergedCluster.name, mergedCluster);
		}
	}

	private void removeClassesWithoutDTI(Map<Cluster, Cluster> parentClassMap) {
		// Locate non-inner classes without DTI
		Map<String, Cluster> excessClusters = new HashMap<>();
		for (Cluster c : super.values())
			if (!c.hasDocTopicItem() && !c.name.contains("$"))
				excessClusters.put(c.name, c);

		Map<String, Cluster> excessInners = new HashMap<>();
		// For each Child/Parent pair, if the parent is marked, mark the child
		for (Map.Entry<Cluster, Cluster> entry : parentClassMap.entrySet()) {
			Cluster child = entry.getKey();
			Cluster parent = entry.getValue();
			if (excessClusters.containsKey(parent.name))
				excessInners.put(child.name, child);
		}

		// Remove them from the analysis
		super.removeAll(excessClusters);
		super.removeAll(excessInners);
	}

	public List<Concern> computeConcernWordBags() {
		List<Concern> concernList = new ArrayList<>();
		for (Cluster cluster : this.values())
			concernList.add(cluster.computeConcern(this.docTopics.getTopicWordLists()));

		return concernList;
	}
	//endregion

	//region SERIALIZATION
	public void serializeBagOfWords() throws FileNotFoundException {
		computeConcernWordBags();

		String fs = File.separator;
		String path = this.projectPath + fs + this.projectName + "_"
			+ this.simMeasure + "_" + this.size() + "_concerns.txt";
		File outputFile = new File(path);
		outputFile.getParentFile().mkdirs();

		Map<Integer, Cluster> architectureIndex = computeArchitectureIndex();

		try (PrintWriter out = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(outputFile), StandardCharsets.UTF_8))) {
			StringBuilder output = new StringBuilder();

			for (Map.Entry<Integer, Cluster> cluster : architectureIndex.entrySet()) {
				DocTopicItem dti = cluster.getValue().getDocTopicItem();
				Concern concernWords = dti.getConcern();
				output.append(cluster.getKey());
				output.append(concernWords);
				output.append(System.lineSeparator());
			}

			out.print(output);
		}
	}
	//endregion
}
