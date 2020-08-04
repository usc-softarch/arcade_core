package edu.usc.softarch.arcade.antipattern.detection.codesmell;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import edu.usc.softarch.arcade.util.FileListing;
import edu.usc.softarch.arcade.util.FileUtil;

public class CodeSmellJsonProcessing {
	private static Logger logger = LogManager.getLogger(CodeSmellJsonProcessing.class);
	
	// Keep whole dependencies in Memory, using map
	// Only keep inbound at the moment
	// Mapping class name and Methond
	public Map<String, List<String>> methodList = new HashMap<>();
	// Mapping method name and inbound/outbound methods
	public Map<String, List<String>> inboundDependencies = new HashMap<>();
	public Map<String, List<String>> outboundDependencies = new HashMap<>();

	public static void main(String[] args)
			throws IOException {
		String 	testDefFinder = "F:\\code_smell\\output-apache-activemq";
		String output		= "F:\\ICSE_2016_data";
		
		File jsonDir = FileUtil.checkDir(testDefFinder, false, false);
		
		List<File> fileList = FileListing.getFileListing(jsonDir);
		Set<File> orderedSerFiles = new LinkedHashSet<>();
		for (File file : fileList) {
			if (file.getName().endsWith(".json")) {
				orderedSerFiles.add(file);
			}
		}
		
		Map<String, String> versionSmells = new LinkedHashMap<>();
		String versionSchemeExpr = "[0-9]+\\.[0-9]+(\\.[0-9]+)*+(-(RC|ALPHA|BETA|M|Rc|Alpha|Beta|rc|alpha|beta|deb|b|a|final|Final|FINAL)[0-9]+)*";
		for (File file : orderedSerFiles) {
			logger.debug(file.getName());
			String version = FileUtil.extractVersionFromFilename(versionSchemeExpr, file.getName());
			assert !version.equals("") : "Could not extract version";
			versionSmells.put(version, file.getAbsolutePath());
		}
		
		String result ="version, godclass, feature envy, divergence, long method, shortgun\n";
		for (String key : versionSmells.keySet()){
			logger.info("Start detecting smells for one version:" + key);
			result += countSmells(versionSmells.get(key), key);
		}
		String fileName = testDefFinder.substring(testDefFinder.lastIndexOf("\\"));
		String projectName = fileName.substring(fileName.lastIndexOf("-"));		
		writeToFile(result, output + File.separator + projectName +"_counting_output.csv");
	}

	private static String countSmells(String testDoc, String version){
		String result = "";
		
		// read from the URL
		try (Scanner scan = new Scanner(new File(testDoc))) {
			int godclass = 0;
			int featureenvy = 0;
			int divergence = 0;
			int longmethod = 0;
			int shortgun = 0;

			String str = "";
			while (scan.hasNext())
				str += scan.nextLine();
			JSONArray smells = new JSONArray(str);
			for (int i = 0; i < smells.length() ; i ++) {
				JSONObject smell = smells.getJSONObject(i);
				JSONArray anormaly = smell.getJSONArray("anomalies");
				for (int j = 0; j < anormaly.length() ; j ++) {
					String type = anormaly.getString(j);
					if (type.equals("DivergentChange"))
						divergence++;
					if (type.equals("FeatureEnvy"))
						featureenvy++;
					if (type.equals("GodClass"))
						godclass++;
					if (type.equals("LongMethod"))
						longmethod++;
					if (type.equals("ShotgunSurgery"))
						shortgun++;
				}
			}
			
			result = version + "," + godclass + "," + featureenvy 
				+ "," + divergence + "," + longmethod + "," + shortgun + ","
				+ new File(testDoc).getName() + "\n";
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	private static void writeToFile(String content,String path)
			throws IOException {
		File file = new File(path);

		// if file doesnt exists, then create it
		if (!file.exists()) {
			file.createNewFile();
		}

		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(content);
		bw.close();

		System.out.println("Done");
	}
}