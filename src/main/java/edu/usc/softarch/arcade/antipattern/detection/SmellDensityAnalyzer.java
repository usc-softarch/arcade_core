package edu.usc.softarch.arcade.antipattern.detection;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import edu.usc.softarch.arcade.antipattern.Smell;
import edu.usc.softarch.arcade.facts.ConcernCluster;
import edu.usc.softarch.arcade.facts.driver.ConcernClusterRsf;
import edu.usc.softarch.arcade.util.FileListing;
import edu.usc.softarch.arcade.util.FileUtil;
import edu.usc.softarch.arcade.util.MapUtil;

public class SmellDensityAnalyzer {
	private static Logger logger =
		LogManager.getLogger(SmellDensityAnalyzer.class);

	public static void main(String[] args) throws IOException {
		// inputDirFilename is the directory containing the .ser files which contain detected smells
		String inputDirFilename = FileUtil.tildeExpandPath(args[0]);
		
		// directory containing the cluster rsf files matching the smells .ser files
		String clustersDirName = FileUtil.tildeExpandPath(args[1]);
		
		List<File> fileList = FileListing.getFileListing(new File(inputDirFilename));
		fileList = FileUtil.sortFileListByVersion(fileList);
		Set<File> orderedSerFiles = new LinkedHashSet<>();
		for (File file : fileList)
			if (file.getName().endsWith(".ser"))
				orderedSerFiles.add(file);
		 	
		Map<String,Set<Smell>> versionSmells = new LinkedHashMap<>();
		String versionSchemeExpr = "[0-9]+\\.[0-9]+(\\.[0-9]+)*";
		
		for (File file : orderedSerFiles) {
			logger.debug(file.getName());
			Set<Smell> smells = SmellUtil.deserializeDetectedSmells(file.getAbsolutePath());
			logger.debug("\tcontains " + smells.size() + " smells");
			
			logger.debug("\tListing detected smells for file" + file.getName() + ": ");
			for (Smell smell : smells) {
				logger.debug("\t" + SmellUtil.getSmellAbbreviation(smell) + " " + smell);
				
			}
			
			String version = FileUtil.extractVersionFromFilename(versionSchemeExpr, file.getName());
			
			assert !version.equals("") : "Could not extract version";
			versionSmells.put(version, smells);
		}
		
		Map<String,Set<ConcernCluster>> versionClusters = new LinkedHashMap<>();
		List<File> clustersFileList = FileListing.getFileListing(new File(clustersDirName));
		for (File file : clustersFileList) {
			Set<ConcernCluster> clusters = ConcernClusterRsf.extractConcernClustersFromRsfFile(file.getAbsolutePath());
			
			String version = FileUtil.extractVersionFromFilename(versionSchemeExpr,file.getName());
			assert !version.equals("") : "Could not extract version";
			versionClusters.put(version, clusters);
		}
		
		versionClusters = MapUtil.sortByKeyVersion(versionClusters);
		versionSmells = MapUtil.sortByKeyVersion(versionSmells);
		
		double[] smellDensityArr = new double[versionClusters.keySet().size()];
		double[] clustersRatioArr = new double[versionClusters.keySet().size()];
		int idx = 0;
		for (String version : versionClusters.keySet()) {
			Set<Smell> smells = versionSmells.get(version);
			
			Set<ConcernCluster> allSmellyClusters = new HashSet<>();
			for (Smell smell : smells) {
				allSmellyClusters.addAll(smell.clusters);
			}
			
			Set<ConcernCluster> clusters = versionClusters.get(version);
			double smellDensity = (double)smells.size()/(double)clusters.size();
			smellDensityArr[idx] = smellDensity;
			
			
			double affectedClustersRatio = (double)allSmellyClusters.size()/(double)clusters.size();
			clustersRatioArr[idx] = affectedClustersRatio;
			
			idx++;
			
			System.out.println("version: " + version);
			System.out.println("# smells: " + smells.size());
			System.out.println("# clusters: " + clusters.size());
			System.out.println("smell density: " + smellDensity);
			System.out.println("ratio of smelly clusters to total clusters: " + affectedClustersRatio);
			System.out.println();
		}
		System.out.println("Smell density stats:");
		DescriptiveStatistics smellDensityStats = new DescriptiveStatistics(smellDensityArr);
		System.out.println(smellDensityStats);
		
		System.out.println("Clusters ratio stats:");
		DescriptiveStatistics clustersRatioStats = new DescriptiveStatistics(clustersRatioArr);
		System.out.println(clustersRatioStats);
	}
}
