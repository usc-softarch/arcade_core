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
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.usc.softarch.arcade.topics.DistributionSizeMismatchException;
import edu.usc.softarch.arcade.topics.DocTopicItem;
import edu.usc.softarch.arcade.topics.DocTopics;

public class Architecture extends ArrayList<Cluster> {
  private static final long serialVersionUID = 1L;

  public Architecture() { super(); }

  public Architecture(Architecture arch) {
    super();
    for (Cluster c : arch)
      this.add(new Cluster(c));
  }

  public Map<String, Integer> computeArchitectureIndex() {
		Map<String, Integer> architectureIndex = new HashMap<>();
		for (int i = 0; i < this.size(); i++) {
			Cluster cluster = this.get(i);
			architectureIndex.put(cluster.getName(), Integer.valueOf(i));
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
      for (Cluster cluster : this) {
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

		for (Cluster cluster : this) {
			double centroid = cluster.computeStructuralCentroid();
			clusterCentroids.add(centroid);
		}

		double globalCentroid = computeStructuralGlobalCentroid(clusterCentroids);

		double clusterGain = 0;
		for (int i = 0; i < clusterCentroids.size(); i++)
			clusterGain += (this.get(i).getNumEntities() - 1)	* Math.pow(
        Math.abs(globalCentroid - clusterCentroids.get(i).doubleValue()), 2);

		return clusterGain;
	}

  private double computeStructuralGlobalCentroid(
					List<Double> clusterCentroids) {
		double centroidSum = 0;

		for (Double centroid : clusterCentroids)
			centroidSum += centroid.doubleValue();

		return centroidSum / clusterCentroids.size();
	}

  public List<List<Double>> computeJSDivergenceSimMatrix(String simMeasure) {
		List<List<Double>> simMatrixObj = new ArrayList<>(this.size());

		for (int i = 0; i < this.size(); i++)
			simMatrixObj.add(new ArrayList<>(this.size()));

		for (int i = 0; i < this.size(); i++) {
			Cluster c1 = this.get(i);
			for (int j = 0; j < this.size(); j++) {
				Cluster c2 = this.get(j);
				double currJSDivergence = 0;

				if (simMeasure.equalsIgnoreCase("js"))
					try {
						currJSDivergence = c1.docTopicItem.getJsDivergence(c2.docTopicItem);
					} catch (DistributionSizeMismatchException e) {
						e.printStackTrace(); //TODO handle it
					}
				else if (simMeasure.equalsIgnoreCase("scm"))
					currJSDivergence = FastSimCalcUtil.getStructAndConcernMeasure(c1, c2);
				else
					throw new IllegalArgumentException("Invalid similarity measure: " + simMeasure);
				
				simMatrixObj.get(i).add(currJSDivergence);
			}
		}
		return simMatrixObj;
	}

  public List<List<Double>> computeInfoLossSimMatrix(
      int numberOfEntitiesToBeClustered) {
		List<List<Double>> simMatrixObj = new ArrayList<>(this.size());
		
		for (int i = 0; i < this.size(); i++)
			simMatrixObj.add(new ArrayList<>(this.size()));

		for (int i = 0; i < this.size(); i++) {
			Cluster c1 = this.get(i);
			for (int j = 0; j < this.size(); j++) {
				Cluster c2 = this.get(j);

				double currSimMeasure = 0;
				currSimMeasure = FastSimCalcUtil.getInfoLossMeasure(numberOfEntitiesToBeClustered, c1,	c2);
				
				simMatrixObj.get(i).add(currSimMeasure);
			}
		}
		
		return simMatrixObj;
	}

  public List<List<Double>> computeUEMSimMatrix(String simMeasure) {
		List<List<Double>> simMatrixObj = new ArrayList<>(this.size());
		
		for (int i = 0; i < this.size(); i++)
			simMatrixObj.add(new ArrayList<>(this.size()));

		for (int i = 0; i < this.size(); i++) {
			Cluster cluster = this.get(i);
			for (int j = 0; j < this.size(); j++) {
				Cluster otherCluster = this.get(j);

				double currSimMeasure = 0;
				if (simMeasure.equalsIgnoreCase("uem"))
					currSimMeasure =
            FastSimCalcUtil.getUnbiasedEllenbergMeasure(cluster, otherCluster);
				else if (simMeasure.equalsIgnoreCase("uemnm"))
					currSimMeasure =
            FastSimCalcUtil.getUnbiasedEllenbergMeasureNM(cluster, otherCluster);
				else
					throw new IllegalArgumentException(simMeasure
            + " is not a valid similarity measure for WCA");
				
				simMatrixObj.get(i).add(currSimMeasure);
			}
		}
		
		return simMatrixObj;
	}

  public double computeTopicClusterGain() {
		List<DocTopicItem> docTopicItems = new ArrayList<>();
		for (Cluster c : this)
			docTopicItems.add(c.docTopicItem);
		DocTopicItem globalDocTopicItem =
      DocTopics.computeGlobalCentroidUsingTopics(docTopicItems);

		double clusterGain = 0;

		for (int i = 0; i < docTopicItems.size(); i++) {
			try {
				clusterGain += (this.get(i).getNumEntities() - 1)
					* docTopicItems.get(i).getJsDivergence(globalDocTopicItem);
			} catch (DistributionSizeMismatchException e) {
				e.printStackTrace(); //TODO handle it
			}
		}

		return clusterGain;
	}

	public boolean hasOrphans() {
		for (Cluster c : this) {
			if (c.getNumEntities() == 1)
				return true;
		}
		return false;
	}
}
