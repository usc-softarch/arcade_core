package edu.usc.softarch.arcade.clustering;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

  public Map<String, Integer> computeArchitectureIndex() {
		Map<String, Integer> architectureIndex = new HashMap<>();
		for (int i = 0; i < this.size(); i++) {
			Cluster cluster = (Cluster) this.values().toArray()[i];
			architectureIndex.put(cluster.getName(), i);
		}
		return architectureIndex;
	}

  public void writeToRsf(Map<String, Integer> architectureIndex, String path)
			throws FileNotFoundException {
		File rsfFile = new File(path);
		rsfFile.getParentFile().mkdirs();

    try (PrintWriter out = new PrintWriter(
        new OutputStreamWriter(
        new FileOutputStream(rsfFile), StandardCharsets.UTF_8))) {
      for (Cluster cluster : this.values()) {
        Integer currentNodeNumber =
          architectureIndex.get(cluster.getName());
        String[] entities = cluster.getName().split(",");
        Set<String> entitiesSet = new HashSet<>(Arrays.asList(entities));
        for (String entity : entitiesSet) {
          out.println("contain " + currentNodeNumber + " " + entity);
        }
      }
    }
	}

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
}
