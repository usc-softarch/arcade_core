package edu.usc.softarch.arcade.facts;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import edu.usc.softarch.arcade.clustering.ConcernClusterArchitecture;
import edu.usc.softarch.arcade.facts.driver.RsfReader;

public class GroundTruthFileParser {
	private static Logger logger =
		LogManager.getLogger(GroundTruthFileParser.class);
	private static ConcernClusterArchitecture clusters = 
		new ConcernClusterArchitecture();
	private static Map<String, ConcernCluster> clusterMap = new HashMap<>();

	public static Map<String, ConcernCluster> getClusterMap() {
		return clusterMap;
	}
	
	public static ConcernClusterArchitecture getClusters() {
		return clusters;
	}
	
	public static void parseBashStyle(String groundTruthFile) {
		clusters = new ConcernClusterArchitecture();
		clusterMap = new HashMap<>();
		try (FileInputStream fstream = new FileInputStream(groundTruthFile)) {
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			
			String filenameWithExtensionPattern = ".+\\.\\w+\\s*";
			
			String currentClusterName = "";
			while ((strLine = br.readLine()) != null) {
				if (strLine.trim().length() == 0) { // skip lines of only whitespace
					continue;
				}
				if (!strLine.matches(filenameWithExtensionPattern)) { // found cluster name
					logger.debug("Found cluster title: " + strLine);
					currentClusterName = strLine.trim().replace(" ", "_");
					
					ConcernCluster newCluster = new ConcernCluster();
					newCluster.setName(currentClusterName);
					clusterMap.put(currentClusterName,newCluster);
				}
				else { // found filename with extension
					ConcernCluster cluster = clusterMap.get(currentClusterName);
					String filename = strLine.trim();
					cluster.addEntity(filename);
					clusterMap.put(currentClusterName,cluster);
				}
			}
			
			logger.debug("Printing out read in ground truth clusters...");
			
			for (ConcernCluster savedCluster : clusters) {
				logger.debug(savedCluster.getName());
				for (String entity : savedCluster.getEntities()) {
					logger.debug(entity);
				}
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void parseHadoopStyle(String groundTruthFile) {
		clusters = new ConcernClusterArchitecture();
		try (FileInputStream fstream = new FileInputStream(groundTruthFile)) {
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			// Pattern to match the title of a cluster (a number followed by zero or more lowercase letters a colon followed by zero or more whitespace and one or more of any character
			String titleClusterPattern = "\\d+[a-z]*:\\s*.+"; 
			String cleanJavaClassPatternStr = ".*(org/.+)\\.java$";
			ConcernCluster newCluster = null;
			while ((strLine = br.readLine()) != null) {
				if (strLine.matches(titleClusterPattern)) {
					logger.debug("Found cluster title: " + strLine);
					newCluster = new ConcernCluster();
					newCluster.setName(strLine.trim());
					clusters.add(newCluster);
				}
				Pattern cleanJavaClassPattern = Pattern.compile(cleanJavaClassPatternStr);
				Matcher m = cleanJavaClassPattern.matcher(strLine);
				if (m.matches()) {
					String firstGroup = m.group(1);
					logger.debug("g1: " + firstGroup);
					String javaClassName = firstGroup.replace("/", ".");
					logger.debug("renamed: " + javaClassName);
					newCluster.addEntity(javaClassName);
				}
			}
			
			logger.debug("Printing out read in ground truth clusters...");
			
			for (ConcernCluster savedCluster : clusters) {
				logger.debug(savedCluster.getName());
				for (String entity : savedCluster.getEntities()) {
					logger.debug(entity);
				}
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void parseRsf(String groundTruthFile) {
		clusterMap = new HashMap<>();
		RsfReader.loadRsfDataFromFile(groundTruthFile);
		List<List<String>> unfilteredFacts = RsfReader.unfilteredFaCtS;
		
		for (List<String> fact : unfilteredFacts) {

			String clusterName = fact.get(1).trim();
			String containedClass = fact.get(2).trim();
			logger.debug("Found class: " + containedClass);

			ConcernCluster currCluster = null;
			if (!clusterMap.containsKey(clusterName)) { // This is a new cluster
				logger.debug("Creating new cluster: " + clusterName);
				currCluster = new ConcernCluster();
				currCluster.setName(clusterName);
				currCluster.addEntity(containedClass);
				clusterMap.put(clusterName, currCluster);
			} else { // This is an already added cluster
				currCluster = clusterMap.get(clusterName);
				currCluster.addEntity(containedClass);
			}
		}
		
		clusters = new ConcernClusterArchitecture(clusterMap.values());
		
		logger.debug("Printing out read in ground truth clusters...");
		
		for (ConcernCluster savedCluster : clusters) {
			logger.debug(savedCluster.getName());
			for (String entity : savedCluster.getEntities()) {
				logger.debug(entity);
			}
		}
	}
}