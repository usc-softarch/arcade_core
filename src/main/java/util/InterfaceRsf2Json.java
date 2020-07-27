package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.common.collect.Lists;

import edu.usc.softarch.arcade.util.FileUtil;

public class InterfaceRsf2Json {
	// static Logger logger = org.apache.logging.log4j.LogManager.getLogger(DepRsf2Json.class);
	// Define XML TAGs
	private static String COMPONENT_NAME 				= "name";
	private static String CHIDREN 			= "children";
	private static String ADDED 			= "added";
	private static String REMOVED 			= "removed";
	private static String SIZE 				= "size";
	private static String CHANGED_TYPE		= "change_type";
	
	private static String FEATURE 		= "feature";
	private static String NAME 		= "name";
	private static String INBOUND 		= "inbound";
	private static String OUTBOUND 	= "outbound";
	private static String CLASS		= "class";
	
	
	
	public static String PKGNAME;
	
	public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {
		
		String output_file		= "E:\\android\\clustered\\edeg7_7.0.1_acdc_hierarchy.json";
		String xmlFilePath      = "E:\\android\\DepFinders\\edeg7_7.0.1_deps.xml";
		
		String xmlFilePath_old      = "E:\\android\\DepFinders\\s5_6_0_1_deps.xml";
		
		String packageName 		= ""; 
		// create json array to keep information
		JSONArray component_arrays = new JSONArray();
		
		// Hashmap to keep the import
		HashMap<String, List<String>> hierarchy = new HashMap<>();
		
		// read dependencies 
		HashMap<String, List<String>> methodList = DependencyFinderProcessing_ExportJSON(xmlFilePath, packageName);
		HashMap<String, List<String>> methodList_old = DependencyFinderProcessing_ExportJSON(xmlFilePath_old, packageName);
		
	
		// read two files: architecture rsf + interface changes
		String arch_rsf_file		= "E:\\android\\clustered\\edeg7_7.0.1_acdc_clustered.rsf"; 
		final File arch_rsf = FileUtil.checkDir(arch_rsf_file, false, false);

		// Create important Hashset
		try {
			final BufferedReader in = new BufferedReader(new FileReader(arch_rsf));
			String line;
			while ((line = in.readLine()) != null) {
				if (line.trim().isEmpty()) {
					continue;
				}
				
				final Scanner s = new Scanner(line);
				final String expr = "([^\"\\s][^\\s]*[^\"\\s]*)|([\"][^\"]*[\"])";
				// int tokenLimit = 3;

				final String arcType = s.findInLine(expr);
				final String startNode = s.findInLine(expr);
				final String endNode = s.findInLine(expr);
				
				if (!checkIgnore(endNode)){
					List<String> entities = hierarchy.get(startNode);
					if (entities == null)
						entities = new ArrayList<>();
					entities.add(endNode);
					hierarchy.put(startNode, entities);
				}
				
				if (s.findInLine(expr) != null) {
					// logger.error("Found non-triple in file: " + line);
					System.exit(1);
				}
				s.close();
			}
			in.close();
		} catch (final IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		// Create the root object
		
		JSONObject root = new JSONObject();
		root.put(COMPONENT_NAME, "android_framework");
		root.put(CHIDREN, component_arrays);
		
		// for each object in architecture
		// create json object
		for (String component : hierarchy.keySet()){
			// keep entities
			JSONObject details_json = new JSONObject();
			details_json.put(COMPONENT_NAME, component.replace(".ss", ""));
			List<String> entities = hierarchy.get(component);
			JSONArray entities_json = new JSONArray();
			for (String e : entities){
				JSONObject e_json = new JSONObject();
				e_json.put(COMPONENT_NAME, e);
				e_json.put(SIZE, 1);
				
				List<String> interfaces = methodList.get(e);
				if (interfaces == null)
					interfaces = new ArrayList<>();
				List<String> interfaces_old = methodList_old.get(e);
				if (interfaces_old == null)
					interfaces_old = new ArrayList<>();
				
				Set<String> ad = new HashSet<String>(interfaces);
				Set<String> bd = new HashSet<String>(interfaces_old);
				Set<String> addedInterface = ad;
				addedInterface.removeAll(bd);
				
				Set<String> removedInterface = bd;
				removedInterface.removeAll(ad);
				
				ArrayList<String> add = (ArrayList<String>) addedInterface.stream().map(s -> s.replace(e+".", "")).collect(Collectors.toList());
				ArrayList<String> remove = (ArrayList<String>) removedInterface.stream().map(s -> s.replace(e+".", "")).collect(Collectors.toList());
				
				if (addedInterface != null)
					e_json.put(ADDED, add);
				
				if (removedInterface != null)
					e_json.put(REMOVED, remove);
				
				if (addedInterface.size() == 0 && removedInterface.size() == 0)
					e_json.put(CHANGED_TYPE, 0);
				else if (addedInterface.size() == 0)
					e_json.put(CHANGED_TYPE, 2);
				else if (removedInterface.size() == 0)
					e_json.put(CHANGED_TYPE, 1);
				else
					e_json.put(CHANGED_TYPE, 3);
				
				entities_json.add(e_json);
			}
			details_json.put(CHIDREN, entities_json);
			component_arrays.add(details_json);
		}
		
		root.put(CHIDREN, component_arrays);
		
		JSONUtil.writeJSONObject2File(root, output_file);

	}
	
	static private boolean checkIgnore(String inputName){
		if (inputName.startsWith("java."))
			return true;
		return false;
	}
	
	public static HashMap<String, List<String>> DependencyFinderProcessing_ExportJSON(String xmlFilePath, String packageName) throws IOException, ParserConfigurationException, SAXException{
		
		HashMap<String, List<String>> methodList = new HashMap<String, List<String>>();
		
		// Run processing to input dependency into file
		//Handle by DOM Parser
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(true);
		factory.setIgnoringElementContentWhitespace(true);
		
		DocumentBuilder builder = factory.newDocumentBuilder();
		builder.setEntityResolver( new EntityResolver() {
			
			@Override
			public InputSource resolveEntity(String arg0, String arg1)
					throws SAXException, IOException {
				return new InputSource(new StringReader("<?xml version='1.0' encoding='UTF-8'?>"));
			}
		});
		File			file	= new File(xmlFilePath);
		Document		doc 	= builder.parse(file);
		
		// Building class method mapping
		NodeList 		nclist 	= doc.getElementsByTagName(CLASS);
		for (int k = 0; k < nclist.getLength(); k++){
			Node 	node 				= nclist.item(k);
			Element eElement 			= (Element) node;
			String 	key		 			= eElement.getElementsByTagName(NAME).item(0).getTextContent();	
			NodeList 		nflist 		= eElement.getElementsByTagName(FEATURE);
			ArrayList<String> methods	= new ArrayList<String>();
			for (int n = 0; n < nflist.getLength(); n++){
				Element	e 	= (Element) nflist.item(n);
				methods.add(e.getElementsByTagName(NAME).item(0).getTextContent());
			}
			methodList.put(key, methods);
		}
		
		return methodList;
		
	}
	
}
