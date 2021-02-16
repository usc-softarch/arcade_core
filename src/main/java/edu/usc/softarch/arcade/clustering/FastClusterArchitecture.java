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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.usc.softarch.arcade.config.Config;
import edu.usc.softarch.arcade.config.Config.SimMeasure;
import edu.usc.softarch.arcade.topics.DistributionSizeMismatchException;
import edu.usc.softarch.arcade.topics.DocTopicItem;
import edu.usc.softarch.arcade.topics.DocTopics;

public class FastClusterArchitecture extends ArrayList<FastCluster> {
  private static final long serialVersionUID = 1L;
  private static Logger logger =
    LogManager.getLogger(FastClusterArchitecture.class);

  public FastClusterArchitecture() { super(); }

  public FastClusterArchitecture(FastClusterArchitecture fca) {
    super();
    for (FastCluster fc : fca)
      this.add(fc);
  }

  public Map<String, Integer> createFastClusterNameToNodeNumberMap() {
		Map<String, Integer> clusterNameToNodeNumberMap = new HashMap<>();
		for (int i = 0; i < this.size(); i++) {
			FastCluster cluster = this.get(i);
			clusterNameToNodeNumberMap.put(cluster.getName(), Integer.valueOf(i));
		}
		return clusterNameToNodeNumberMap;
	}

  public void writeFastClustersRsfFile(
			Map<String, Integer> clusterNameToNodeNumberMap,
			String currentClustersDetailedRsfFilename)
			throws FileNotFoundException {
		File rsfFile = new File(currentClustersDetailedRsfFilename);

    try (PrintWriter out = new PrintWriter(
        new OutputStreamWriter(
        new FileOutputStream(rsfFile), StandardCharsets.UTF_8))) {
      logger.trace("Printing each cluster and its leaves...");
      for (FastCluster cluster : this) {
        Integer currentNodeNumber =
          clusterNameToNodeNumberMap.get(cluster.getName());
        logger.trace("Cluster name: " + currentNodeNumber);
        logger.trace("Cluster node number: " + cluster);
        String[] entities = cluster.getName().split(",");
        Set<String> entitiesSet = new HashSet<>(Arrays.asList(entities));
        int entityCount = 0;
        for (String entity : entitiesSet) {
          logger.trace(entityCount + ":\t" + entity);
          out.println("contain " + currentNodeNumber + " " + entity);
          entityCount++;
        }
      }
    }
	}

  public double computeClusterGainUsingStructuralData() {
		List<Double> clusterCentroids = new ArrayList<>();

		for (FastCluster cluster : this) {
			double centroid = cluster.computeCentroidUsingStructuralData();
			clusterCentroids.add(centroid);
		}

		double globalCentroid = computeGlobalCentroidForStructuralData(clusterCentroids);

		double clusterGain = 0;
		for (int i = 0; i < clusterCentroids.size(); i++)
			clusterGain += (this.get(i).getNumEntities() - 1)	* Math.pow(
        Math.abs(globalCentroid - clusterCentroids.get(i).doubleValue()), 2);

		return clusterGain;
	}

  private double computeGlobalCentroidForStructuralData(
			List<Double> clusterCentroids) {
		double centroidSum = 0;

		for (Double centroid : clusterCentroids)
			centroidSum += centroid.doubleValue();

		return centroidSum / clusterCentroids.size();
	}

  public List<List<Double>> createSimilarityMatrixUsingJSDivergence() {
		List<List<Double>> simMatrixObj = new ArrayList<>(this.size());

		for (int i = 0; i < this.size(); i++)
			simMatrixObj.add(new ArrayList<>(this.size()));

		for (int i = 0; i < this.size(); i++) {
			FastCluster cluster = this.get(i);
			for (int j = 0; j < this.size(); j++) {
				FastCluster otherCluster = this.get(j);
				double currJSDivergence = 0;

				if (Config.getCurrSimMeasure().equals(SimMeasure.js))
					try {
						currJSDivergence =
							cluster.docTopicItem.getJsDivergence(otherCluster.docTopicItem);
					} catch (DistributionSizeMismatchException e) {
						e.printStackTrace(); //TODO handle it
					}
				else if (Config.getCurrSimMeasure().equals(SimMeasure.scm))
					currJSDivergence = FastSimCalcUtil.getStructAndConcernMeasure(cluster, otherCluster);
				else
					throw new IllegalArgumentException("Invalid similarity measure: " + Config.getCurrSimMeasure());
				
				simMatrixObj.get(i).add(currJSDivergence);
			}
		}
		return simMatrixObj;
	}

  public List<List<Double>> createSimilarityMatrixUsingInfoLoss(
      int numberOfEntitiesToBeClustered) {
		List<List<Double>> simMatrixObj = new ArrayList<>(this.size());
		
		for (int i = 0; i < this.size(); i++)
			simMatrixObj.add(new ArrayList<>(this.size()));

		for (int i = 0; i < this.size(); i++) {
			FastCluster cluster = this.get(i);
			for (int j = 0; j < this.size(); j++) {
				FastCluster otherCluster = this.get(j);

				double currSimMeasure = 0;
				currSimMeasure = FastSimCalcUtil.getInfoLossMeasure(numberOfEntitiesToBeClustered, cluster,	otherCluster);
				
				simMatrixObj.get(i).add(currSimMeasure);
			}
		}
		
		return simMatrixObj;
	}

  public List<List<Double>> createSimilarityMatrixUsingUEM() {
		List<List<Double>> simMatrixObj = new ArrayList<>(this.size());
		
		for (int i = 0; i < this.size(); i++)
			simMatrixObj.add(new ArrayList<>(this.size()));

		for (int i = 0; i < this.size(); i++) {
			FastCluster cluster = this.get(i);
			for (int j = 0; j < this.size(); j++) {
				FastCluster otherCluster = this.get(j);

				double currSimMeasure = 0;
				if (Config.getCurrSimMeasure().equals(SimMeasure.uem))
					currSimMeasure =
            FastSimCalcUtil.getUnbiasedEllenbergMeasure(cluster, otherCluster);
				else if (Config.getCurrSimMeasure().equals(SimMeasure.uemnm))
					currSimMeasure =
            FastSimCalcUtil.getUnbiasedEllenbergMeasureNM(cluster, otherCluster);
				else
					throw new IllegalArgumentException(Config.getCurrSimMeasure()
            + " is not a valid similarity measure for WCA");
				
				simMatrixObj.get(i).add(currSimMeasure);
			}
		}
		
		return simMatrixObj;
	}

  public double computeClusterGainUsingTopics() {
		List<DocTopicItem> docTopicItems = new ArrayList<>();
		for (FastCluster c : this)
			docTopicItems.add(c.docTopicItem);
		DocTopicItem globalDocTopicItem =
      DocTopics.computeGlobalCentroidUsingTopics(docTopicItems);
		logger.debug("Global Centroid Using Topics: "
			+ globalDocTopicItem.toStringWithLeadingTabsAndLineBreaks(0));

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
}