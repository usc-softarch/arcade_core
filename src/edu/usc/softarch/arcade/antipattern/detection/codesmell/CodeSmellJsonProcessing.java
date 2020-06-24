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
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import edu.usc.softarch.arcade.util.FileListing;
import edu.usc.softarch.arcade.util.FileUtil;

public class CodeSmellJsonProcessing {
	static Logger logger = org.apache.logging.log4j.LogManager.getLogger(CodeSmellJsonProcessing.class);
	// Define XML TAGs
	private String FEATURE 		= "feature";
	private String NAME 		= "name";
	private String INBOUND 		= "inbound";
	private String OUTBOUND 	= "outbound";
	private String CLASS		= "class";
	private static String DUPLICATION	= "duplication";
	private static String FILE			= "file";
	private static String PATH			= "path";
	public String PKGNAME;
	
	private HashMap<String, List<String>> clusterList		    = new HashMap<String, List<String>>();		
	private HashMap<String, String>		  class2component		= new HashMap<String, String>();
	private HashMap<Integer, Set<String>> codeClone				= new HashMap<Integer, Set<String>>();
	// Keep whole dependencies in Memory, using map
	// Only keep inbound at the moment
	// Mapping class name and Methond
	public HashMap<String, List<String>> methodList				= new HashMap<String, List<String>>();
	// Mapping method name and inbound/outbound methods
	public HashMap<String, List<String>> inboundDependencies 	= new HashMap<String, List<String>>();
	public HashMap<String, List<String>> outboundDependencies 	= new HashMap<String, List<String>>();

	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
		// TODO Auto-generated method stub
		String 	testDefFinder = "F:\\code_smell\\output-apache-activemq";
		String packageName = "org.apache.jackrabbit";
		String output		= "F:\\ICSE_2016_data";
		
		
		final File jsonDir = FileUtil.checkDir(testDefFinder, false, false);

	
		
		List<File> fileList = FileListing.getFileListing(jsonDir);
//		fileList = FileUtil.sortFileListByVersion(fileList);
		final Set<File> orderedSerFiles = new LinkedHashSet<File>();
		for (final File file : fileList) {
			if (file.getName().endsWith(".json")) {
				orderedSerFiles.add(file);
			}
		}
		
		Map<String, String> versionSmells = new LinkedHashMap<String, String>();
		final String versionSchemeExpr = "[0-9]+\\.[0-9]+(\\.[0-9]+)*+(-(RC|ALPHA|BETA|M|Rc|Alpha|Beta|rc|alpha|beta|deb|b|a|final|Final|FINAL)[0-9]+)*";
		for (final File file : orderedSerFiles) {
			logger.debug(file.getName());
			final String version = FileUtil.extractVersionFromFilename(versionSchemeExpr, file.getName());
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
		
//		for (File key : orderedSerFiles){
//			logger.info("Start detecting smells for one version:" + key.getAbsolutePath());
//			result += countSmells(key.getAbsolutePath());
//		}
//		writeToFile(result, output + File.separator + "Counting_output.csv");
	}

	private static String countSmells(String testDoc, String version){
		
		String result = "";
		
		// read from the URL
		try {
		    Scanner scan;
		    
		    int godclass = 0;
		    int featureenvy = 0;
		    int divergence = 0;
		    int longmethod = 0;
		    int shortgun = 0;
		    

			scan = new Scanner(new File(testDoc));
		    String str = new String();
		    while (scan.hasNext())
		        str += scan.nextLine();
		    scan.close();
		    JSONArray smells = new JSONArray(str);
//		    JSONObject obj = new JSONObject(str);
//		    if (! obj.getString("status").equals("OK"))
//		        return;
//		    
//		    JSONArray smells = obj.getJSONArray("anomalies");
		    for (int i = 0; i < smells.length() ; i ++) {
		    	JSONObject smell = smells.getJSONObject(i);
		    	JSONArray anormaly = smell.getJSONArray("anomalies");
		    	for (int j = 0; j < anormaly.length() ; j ++) {
		    		String type = anormaly.getString(j);
		    		if (type.equals(Constant.ANOMALY_DIVERGENT_CHANGE))
		    			divergence ++;
		    		if (type.equals(Constant.ANOMALY_FEATURE_ENVY))
		    			featureenvy ++;
		    		if (type.equals(Constant.ANOMALY_GOD_CLASS))
		    			godclass ++;
		    		if (type.equals(Constant.ANOMALY_LONG_METHOD))
		    			longmethod ++;
		    		if (type.equals(Constant.ANOMALY_SHOTGUN_SURGERY))
		    			shortgun ++;
		    	}
//		    	logger.info(smell.toString());
		    }
		    
		    result = version + "," + godclass + "," + featureenvy 
		    		+ "," + divergence + "," + longmethod + "," + shortgun + "," + new File(testDoc).getName() + "\n" ; 
		    
		    
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
		
	}
	
	private String getClassName(String featureName) {
		String className ="";
		if(featureName.contains("(")) {
			String tmp = featureName.split(Pattern.quote("("))[0];
			String[] tmpA = tmp.split(Pattern.quote("."));
			for (int i = 0; i <= tmpA.length-3; i++){
				className += tmpA[i] +".";
			}
			className += tmpA[tmpA.length-2];
		} else {
			className	= featureName;
		}
		return className;
	}
	
	private static void writeToFile(String content,String path) throws IOException {
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
