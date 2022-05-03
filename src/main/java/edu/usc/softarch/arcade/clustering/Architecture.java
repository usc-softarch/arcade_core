package edu.usc.softarch.arcade.clustering;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import edu.usc.softarch.arcade.topics.DistributionSizeMismatchException;
import edu.usc.softarch.arcade.topics.DocTopicItem;
import edu.usc.softarch.arcade.topics.DocTopics;

public class Architecture extends LinkedHashMap<String, Cluster> {
	//region ATTRIBUTES
	private static final long serialVersionUID = 1L;
	//endregion

	//region CONSTRUCTORS
	public Architecture() {	super(); }

	/**
	 * Clone constructor
	 */
  public Architecture(Architecture arch) {
		for (Cluster c : arch.values())
			this.add(new Cluster(c));
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

	//region SERIALIZATION
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
