package edu.usc.softarch.arcade.clustering;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import edu.usc.softarch.arcade.clustering.techniques.ClusteringAlgoRunner;
import edu.usc.softarch.arcade.config.Config;
import edu.usc.softarch.arcade.topics.DistributionSizeMismatchException;
import edu.usc.softarch.arcade.topics.DocTopicItem;
import edu.usc.softarch.arcade.topics.DocTopics;

public class Architecture extends TreeMap<String, Cluster> {
	//region ATTRIBUTES
	private static final long serialVersionUID = 1L;
	private String projectName;
	private String projectPath;
	//endregion

	//region CONSTRUCTORS
	public Architecture() {	super(); }

	/**
	 * Clone constructor
	 */
  public Architecture(Architecture arch) {
		for (Cluster c : arch.values())
			this.add(new Cluster(c));

		this.projectName = arch.projectName;
		this.projectPath = arch.projectPath;
	}

	public Architecture(String projectName, String projectPath,
			FeatureVectors vectors, String language, String packagePrefix) {
		this.projectName = projectName;
		this.projectPath = projectPath;
		initializeClusters(vectors, language, packagePrefix);
	}

	private void initializeClusters(FeatureVectors vectors, String language,
			String packagePrefix) {
		// For each cell in the adjacency matrix
		for (String name : vectors.getFeatureVectorNames()) {
			// Get the vector relative to that cell
			BitSet featureSet = vectors.getNameToFeatureSetMap().get(name);
			// Create a cluster containing only that cell
			Cluster cluster = new Cluster(name, featureSet,
				vectors.getNamesInFeatureSet());

			// Add the cluster except extraordinary circumstances (assume always)
			addClusterConditionally(cluster, language, packagePrefix);
		}

		ClusteringAlgoRunner.numberOfEntitiesToBeClustered = this.size();
	}

	private void addClusterConditionally(Cluster cluster, String language,
			String packagePrefix) {
		// If the source language is C or C++, add the only C-based entities
		if (language.equalsIgnoreCase("c")) {
			Pattern p = Pattern.compile("\\.(c|cpp|cc|s|h|hpp|icc|ia|tbl|p)$");
			// First condition to be assumed true
			// Second condition to be assumed true
			// Third condition checks whether the cluster is based on a valid C entity
			if (Config.getClusteringGranule().equals(Config.Granule.file) &&
				!cluster.getName().startsWith("/") &&
				p.matcher(cluster.getName()).find())
				this.put(cluster.getName(), cluster);
		}

		// This block is used only for certain older modules, disregard
		if (Config.getClusteringGranule().equals(Config.Granule.func)) {
			if (cluster.getName().equals("\"##\""))
				return;
			this.put(cluster.getName(), cluster);
		}

		// If the source language is Java, add all clusters that match prefix
		if (language.equalsIgnoreCase("java")
			&& (packagePrefix.isEmpty()
			|| cluster.getName().startsWith(packagePrefix)))
			this.put(cluster.getName(), cluster);
	}
	//endregion

	//region ACCESSORS
	public boolean hasOrphans() {
		for (Cluster c : this.values()) {
			if (c.getNumEntities() == 1)
				return true;
		}
		return false;
	}

	public void add(Cluster c) {
		this.put(c.getName(), c);
	}

	public void removeAll(Architecture arch) {
		for (String key : arch.keySet())
			this.remove(key);
	}
	//endregion

	//region PROCESSING
  public double computeStructuralClusterGain() {
		List<Double> clusterCentroids = new ArrayList<>();

		for (Cluster cluster : this.values()) {
			double centroid = cluster.computeStructuralCentroid();
			clusterCentroids.add(centroid);
		}

		double globalCentroid = computeStructuralGlobalCentroid(clusterCentroids);

		double clusterGain = 0;
		for (int i = 0; i < clusterCentroids.size(); i++)
			clusterGain += (((Cluster) this.values().toArray()[i]).getNumEntities() - 1)	* Math.pow(
        Math.abs(globalCentroid - clusterCentroids.get(i)), 2);

		return clusterGain;
	}

  private double computeStructuralGlobalCentroid(
					List<Double> clusterCentroids) {
		double centroidSum = 0;

		for (Double centroid : clusterCentroids)
			centroidSum += centroid;

		return centroidSum / clusterCentroids.size();
	}

  public double computeTopicClusterGain() {
		List<DocTopicItem> docTopicItems = new ArrayList<>();
		for (Cluster c : this.values())
			docTopicItems.add(c.docTopicItem);
		DocTopicItem globalDocTopicItem =
      DocTopics.computeGlobalCentroidUsingTopics(docTopicItems);

		double clusterGain = 0;

		for (int i = 0; i < docTopicItems.size(); i++) {
			try {
				clusterGain += (((Cluster) this.values().toArray()[i]).getNumEntities() - 1)
					* docTopicItems.get(i).getJsDivergence(globalDocTopicItem);
			} catch (DistributionSizeMismatchException e) {
				e.printStackTrace(); //TODO handle it
			}
		}

		return clusterGain;
	}
	//endregion

	//region SERIALIZATION
	public void writeToRsf() throws FileNotFoundException {
		String fs = File.separator;
		String path = this.projectPath + fs + this.projectName + "_"
			+ this.size() + "_clusters.rsf";
		this.writeToRsf(path);
	}

	public void writeToRsf(String path) throws FileNotFoundException {
		File rsfFile = new File(path);
		rsfFile.getParentFile().mkdirs();

		Map<Integer, String> architectureIndex = computeArchitectureIndex();

		try (PrintWriter out = new PrintWriter(
			new OutputStreamWriter(
				new FileOutputStream(rsfFile), StandardCharsets.UTF_8))) {
			for (Map.Entry<Integer, String> cluster : architectureIndex.entrySet()) {
				Integer clusterIndex = cluster.getKey();
				String[] entities = cluster.getValue().split(",");
				Set<String> entitiesSet = new HashSet<>(Arrays.asList(entities));
				for (String entity : entitiesSet) {
					out.println("contain " + clusterIndex + " " + entity);
				}
			}
		}
	}

	private Map<Integer, String> computeArchitectureIndex() {
		List<String> orderedClusterNames = this.values().stream()
			.map(Cluster::getName).sorted().collect(Collectors.toList());

		Map<Integer, String> architectureIndex = new TreeMap<>();
		for (int i = 0; i < this.size(); i++)
			architectureIndex.put(i, orderedClusterNames.get(i));

		return architectureIndex;
	}
	//endregion
}
