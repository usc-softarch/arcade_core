package edu.usc.softarch.arcade;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mojo.MoJoCalculator;

import org.apache.log4j.Logger;

import edu.usc.softarch.arcade.config.Config;

public class MetricsDriver {
	static Logger logger = Logger.getLogger(MetricsDriver.class);

	public static String constructTopicBasedComputedRsfFilename(
			String computedFilePrex, String selectedAlg, String simMeasure,
			String stoppingCriterion, int numClusters, int numTopics) {
		return computedFilePrex + selectedAlg + "_"
				+ stoppingCriterion + "_" + simMeasure + "_"  + numClusters + "_clusters_" + numTopics + "topics.rsf";
	}

	public static String constructNonTopicComputedRsfFilename(
			String computedFilePrex, String selectedAlg, String simMeasure,
			String stoppingCriterion, int numClusters) {
		return computedFilePrex + selectedAlg + "_"
				+ stoppingCriterion + "_" + simMeasure + "_" + numClusters
				+ "_clusters.rsf";
	}
	
	public static void performMoJoForMultiClustersOnFile(
			String computedFilePrex, String authClusteringFile, String selectedAlg, String simMeasure, String stoppingCriterion) {
		List<Integer> numClustersList = new ArrayList<>();
		List<Integer> numTopicsList = new ArrayList<>();
		
		List<Long> mojoToAuthList = new ArrayList<>();
		List<Double> mojoFmToAuthList = new ArrayList<>();
		
		long mojoSum = 0;
		double mojoFmSum = 0;
		int computedClusterCount = 0;
		
		mojoSum = 0;
		mojoFmSum = 0;
		computedClusterCount = 0;
		
		double maxMojoFM = 0;
		int numClustersAtMax = 0;
		int numTopicsAtMax = 0;
		
		for (int numClusters = Config.getStartNumClustersRange(); numClusters <= Config
				.getEndNumClustersRange(); numClusters += Config
				.getRangeNumClustersStep()) {
			for (int numTopics = Config.getStartNumTopicsRange(); numTopics <= Config
					.getEndNumTopicsRange(); numTopics += Config
					.getRangeNumTopicsStep()) {

				numClustersList.add(numClusters);
				numTopicsList.add(numTopics);

				String computedRsfFilename = null;
				if (!selectedAlg.equals("arc")) {
					computedRsfFilename = constructNonTopicComputedRsfFilename(computedFilePrex,
							selectedAlg, simMeasure, stoppingCriterion, numClusters);
				} else {
					computedRsfFilename = constructTopicBasedComputedRsfFilename(computedFilePrex,
							selectedAlg, simMeasure, stoppingCriterion, numClusters,
							numTopics);
				}

				String usingComputedFilename = "Using " + computedRsfFilename
						+ " as computed clusters...";
				logger.debug(usingComputedFilename);
				System.out.println(usingComputedFilename);

				MoJoCalculator mojoCalc = new MoJoCalculator(
						computedRsfFilename, authClusteringFile, null);
				long mojoValue = mojoCalc.mojo();
				mojoToAuthList.add(mojoValue);
				String mojoOutput = "MoJo of " + computedRsfFilename
						+ " compared to authoritative clustering: " + mojoValue;
				logger.debug(mojoOutput);
				System.out.println(mojoOutput);
				mojoSum += mojoValue;

				mojoCalc = new MoJoCalculator(computedRsfFilename,
						authClusteringFile, null);
				double mojoFmValue = mojoCalc.mojofm();
				mojoFmToAuthList.add(mojoFmValue);
				mojoOutput = "MoJoFM of " + computedRsfFilename
						+ " compared to authoritative clustering: "
						+ mojoFmValue;
				logger.debug(mojoOutput);
				System.out.println(mojoOutput);
				mojoFmSum += mojoFmValue;
				
				if (mojoFmValue > maxMojoFM) {
					maxMojoFM = mojoFmValue;
					numClustersAtMax = numClusters;
					numTopicsAtMax = numTopics;
				}

				computedClusterCount++;
			}

		}
		double mojoAvg = mojoSum/computedClusterCount;
		String mojoAvgOutput = "MoJo averge: " + mojoAvg;
		logger.debug(mojoAvgOutput);
		System.out.println(mojoAvgOutput);

		double mojoFmAvg = mojoFmSum/computedClusterCount;
		String mojoFmAvgOutput = "MoJoFM averge: " + mojoFmAvg;
		logger.debug(mojoFmAvgOutput);
		System.out.println(mojoFmAvgOutput);
		
		System.out.println("max mojo fm: " + maxMojoFM);
		System.out.println("num clusters at max: " + numClustersAtMax);
		System.out.println("num topics at max: " + numTopicsAtMax);
		
		System.out.println("Writing MoJo and MoJoFM to csv file " + Config.getMojoToAuthCSVFilename(numClustersList,selectedAlg,simMeasure) + " ...");

		createMojoToAuthListsCSVFile(numClustersList,mojoToAuthList,mojoFmToAuthList,selectedAlg,simMeasure,numTopicsList);
		
	}
	
	public static void performMoJoOperationsForMultipleClustersOnSingleAuthClusteringFile(String selectedAlg, String simMeasure,
			String stoppingCriterion, String computedFilePrex, String authClusteringFile) {
		List<Integer> numClustersList = new ArrayList<>();
		List<Long> mojoList = new ArrayList<>();
		List<Double> mojoFmList = new ArrayList<>();
		
		double maxMojoFm = 0;
		double maxNumClusters = 0;
		double sumMojoFm = 0;
		for (int numClusters = Config.getStartNumClustersRange(); numClusters <= Config
				.getEndNumClustersRange(); numClusters += Config
				.getRangeNumClustersStep()) {
			
			numClustersList.add(numClusters);
			
			String computedRsfFilename = constructNonTopicComputedRsfFilename(computedFilePrex, selectedAlg, simMeasure,
					stoppingCriterion, numClusters);
			String usingComputedFilename = "Using " + computedRsfFilename
					+ " as computed clusters...";
			logger.debug(usingComputedFilename);
			System.out.println(usingComputedFilename);
			
			MoJoCalculator mojoCalc = new MoJoCalculator(
					computedRsfFilename, authClusteringFile, null);
			long mojoValue = mojoCalc.mojo();
			mojoList.add(mojoValue);
			String mojoOutput = "MoJo of " + computedRsfFilename
					+ " compared to authoritative clustering: " + mojoValue;
			logger.debug(mojoOutput);
			System.out.println(mojoOutput);
			
			mojoCalc = new MoJoCalculator(computedRsfFilename,
					authClusteringFile, null);
			double mojoFmValue = mojoCalc.mojofm();
			mojoFmList.add(mojoFmValue);
			mojoOutput = "MoJoFM of " + computedRsfFilename
					+ " compared to authoritative clustering: "
					+ mojoFmValue;
			logger.debug(mojoOutput);
			System.out.println(mojoOutput);
			
			sumMojoFm += mojoFmValue;
			
			if (mojoFmValue > maxMojoFm) {
				maxMojoFm = mojoFmValue;
				maxNumClusters = numClusters;
			}
		}
		
		double avgMojoFm = sumMojoFm/numClustersList.size();
		
		
		String writingMojoToCsvMsg = "Writing MoJo and MoJoFM to csv file " + Config.getMojoToAuthCSVFilename(numClustersList,selectedAlg,simMeasure) + " ...";
		String maxMojoFmMsg = "max mojo fm: " + maxMojoFm;
		String numClusAtMaxMojoFmMsg = "no. clusters at max mojo fm: " + maxNumClusters;
		String avgMojoFmMsg = "avg mojo fm: " + avgMojoFm;
		
		System.out.println(writingMojoToCsvMsg);
		System.out.println();
		System.out.println(maxMojoFmMsg);
		System.out.println(numClusAtMaxMojoFmMsg);
		System.out.println(avgMojoFmMsg);
		
		logger.debug(writingMojoToCsvMsg);
		logger.debug("\n");
		logger.debug(maxMojoFmMsg);
		logger.debug(numClusAtMaxMojoFmMsg);
		logger.debug(avgMojoFmMsg);

		createMojoListsCSVFile(numClustersList,mojoList,mojoFmList,selectedAlg,simMeasure);
	}

	public static void performMojoForSingleAuthClustering(String computedRsfFilename, String authClusteringFile) {
		MoJoCalculator mojoCalc = new MoJoCalculator(computedRsfFilename,authClusteringFile, null);
		long mojoValue = mojoCalc.mojo();
		String mojoOutput = "MoJo of " + computedRsfFilename
				+ " compared to authoritative clustering: " + mojoValue;
		logger.debug(mojoOutput);
		System.out.println(mojoOutput);

		

		mojoCalc = new MoJoCalculator(computedRsfFilename, authClusteringFile, null);
		double mojoFmValue = mojoCalc.mojofm();
		mojoOutput = "MoJoFM of " + computedRsfFilename
				+ " compared to authoritative clustering: " + mojoFmValue;
		logger.debug(mojoOutput);
		System.out.println(mojoOutput);
	}
	
	public static void createMojoListsCSVFile(List<Integer> numClustersList, List<Long> mojoList,
			List<Double> mojoFmList,String selectedAlg, String simMeasure) {
		try (FileWriter fstream = new FileWriter(
			Config.getMojoToAuthCSVFilename(numClustersList,selectedAlg, simMeasure))) {
			BufferedWriter out = new BufferedWriter(fstream);
			
			out.write("number of clusters,");
			for (int numClusters : numClustersList) {
				out.write(numClusters + ",");
			}
			out.write("\n");
			
			writeMojoListsToFile(mojoList, mojoFmList, out);
			
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void createMojoToAuthListsCSVFile(List<Integer> numClustersList, List<Long> mojoList,
			List<Double> mojoFmList,String selectedAlg, String simMeasure, List<Integer> numTopicsList) {
		try (FileWriter fstream = new FileWriter(
			Config.getMojoToAuthCSVFilename(numClustersList,selectedAlg,simMeasure))) {
			BufferedWriter out = new BufferedWriter(fstream);
			
			out.write("number of topics,");
			for (int numTopics : numTopicsList) {
				out.write(numTopics + ",");
			}
			out.write("\n");
			
			out.write("number of clusters,");
			for (int numClusters : numClustersList) {
				out.write(numClusters + ",");
			}
			out.write("\n");
			
			writeMojoListsToFile(mojoList, mojoFmList, out);
			
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void writeMojoListsToFile(List<Long> mojoList,
			List<Double> mojoFmList, BufferedWriter out) throws IOException {
		out.write("MoJo,");
		for (long mojo : mojoList) {
			out.write(mojo + ",");
		}
		out.write("\n");
		
		out.write("MoJoFM,");
		for (double mojoFm : mojoFmList) {
			out.write(mojoFm + ",");
		}
		out.write("\n");
	}
}