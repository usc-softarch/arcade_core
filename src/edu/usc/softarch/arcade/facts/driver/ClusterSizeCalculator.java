package edu.usc.softarch.arcade.facts.driver;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.moment.Skewness;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.log4j.PropertyConfigurator;

import com.google.common.base.Joiner;

import edu.usc.softarch.arcade.config.Config;
import edu.usc.softarch.arcade.facts.ConcernCluster;
import edu.usc.softarch.arcade.facts.GroundTruthFileParser;

public class ClusterSizeCalculator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		PropertyConfigurator.configure(Config.getLoggingConfigFilename());
		String rsfFilename = args[0];
		
		GroundTruthFileParser.parseRsf(rsfFilename);
		Set<ConcernCluster> clusters = GroundTruthFileParser.getClusters();
		
		for (ConcernCluster cluster : clusters) {
			System.out.println(cluster.getName());
			for (String entity : cluster.getEntities()) {
				System.out.println("\t" + entity);
			}
			System.out.println();
		}
		
		int entityCount = 0;
		for (ConcernCluster cluster : clusters) {
			for (String entity : cluster.getEntities()) {
				entityCount++;
			}
		}
		
		Map<String,Integer> clusterEntityCountMap = new HashMap<String,Integer>();
		
		for (ConcernCluster cluster : clusters) {
			clusterEntityCountMap.put(cluster.getName(), cluster.getEntities().size());
		}
		
		double[] clusterEntityCounts = new double[clusterEntityCountMap.values().size()];
		int i = 0;
		for (int clusterEntityCount : clusterEntityCountMap.values()) {
			clusterEntityCounts[i] = clusterEntityCount;
			i++;
		}
		
		
		
		double clusterEntityCountsMean = StatUtils.mean(clusterEntityCounts);
		StandardDeviation stdDev = new StandardDeviation();
		double clusterEntityCountsStdDev = stdDev.evaluate(clusterEntityCounts);
		Skewness skewness = new Skewness();
		double clusterEntityCountsSkewness = skewness.evaluate(clusterEntityCounts);
		
		System.out.println("Cluster entity counts: ");
		for (Entry<String,Integer> entry : clusterEntityCountMap.entrySet()) {
			System.out.println( entry.getKey() + " : " + entry.getValue());
		}
		
		// Determine small, medium, and large clusters
		double stdDevLargeClusterFactor = 1;
		double stdDevSmallClusterFactor = 0.5;
		double smallClustersThreshold = clusterEntityCountsMean*stdDevSmallClusterFactor;
		double largeClustersThreshold = stdDevLargeClusterFactor*clusterEntityCountsStdDev+clusterEntityCountsMean;
		Set<String> smallClusters = new HashSet<String>();
		Set<String> medClusters = new HashSet<String>();
		Set<String> largeClusters = new HashSet<String>();
		Set<String> belowMeanClusters = new HashSet<String>();
		for (Entry<String,Integer> entry : clusterEntityCountMap.entrySet()) {
			if (entry.getValue() > largeClustersThreshold) {
				largeClusters.add(entry.getKey());
			}
			else if (entry.getValue() < smallClustersThreshold) {
				smallClusters.add(entry.getKey());
			}
			else if (entry.getValue() >= smallClustersThreshold && entry.getValue() < clusterEntityCountsMean){
				medClusters.add(entry.getKey());
			}
			
			if (entry.getValue() < clusterEntityCountsMean) {
				belowMeanClusters.add(entry.getKey());
			}
		}
		
		// Determine singleton clusters
		Set<String> singletonClusters = new HashSet<String>();
		for (Entry<String,Integer> entry : clusterEntityCountMap.entrySet()) {
			if (entry.getValue() == 1) {
				singletonClusters.add(entry.getKey());
			}
		}
		
		System.out.println("Small clusters: ");
		System.out.println("\t" + Joiner.on("\n\t").join(smallClusters));
		System.out.println("Medium clusters: ");
		System.out.println("\t" + Joiner.on("\n\t").join(medClusters));
		System.out.println("Large clusters: ");
		System.out.println("\t" + Joiner.on("\n\t").join(largeClusters));
		System.out.println("Singleton clusters: ");
		System.out.println("\t" + Joiner.on("\n\t").join(singletonClusters));
		System.out.println("Below mean clusters: ");
		System.out.println("\t" + Joiner.on("\n\t").join(belowMeanClusters));
		
		// Determine proportion of small-large and singleton clusters
		double smallClusterProportion = (double)smallClusters.size()/(double)clusters.size();
		double medClusterProportion = (double)medClusters.size()/(double)clusters.size();
		double largeClusterProportion = (double)largeClusters.size()/(double)clusters.size();
		double singletonClusterProportion = (double)singletonClusters.size()/(double)clusters.size();
		double belowMeanClusterProportion = (double)belowMeanClusters.size()/(double)clusters.size();
		System.out.println("Proportion of small clusters: " + smallClusterProportion);
		System.out.println("Proportion of med clusters: " + medClusterProportion);
		System.out.println("Proportion of large clusters: " + largeClusterProportion);
		System.out.println("Proportion of singleton clusters: " + singletonClusterProportion);
		System.out.println("Proportion of below mean clusters: " + belowMeanClusterProportion);
		
		
		System.out.println("Statistics: ");
		System.out.println("\tNo. of clusters: " + clusters.size());
		System.out.println("\tNo. of entities in system: " + entityCount);
		System.out.println("\tmean cluster size: " + clusterEntityCountsMean);
		System.out.println("\tmean cluster size as proportion of total entities in system: " + (double)clusterEntityCountsMean/(double)entityCount);
		System.out.println("\tstandard deviation cluster size: " + clusterEntityCountsStdDev);
		System.out.println("\tskewness cluster size: " + clusterEntityCountsSkewness);

	}

}
