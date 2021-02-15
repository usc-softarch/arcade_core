package edu.usc.softarch.arcade.antipattern.detection.interfacebased;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.common.collect.Lists;

import edu.usc.softarch.arcade.util.FileListing;
import edu.usc.softarch.arcade.util.FileUtil;

/**
 * @author d.le
 * 
 * Find interface based architectural smell + co-change smell
 */
public class DependencyFinderProcessing_ExportJSON {
	private static Logger logger =
		LogManager.getLogger(DependencyFinderProcessing_ExportJSON.class);
	// Define XML TAGs
	private String FEATURE = "feature";
	private String NAME = "name";
	private String INBOUND = "inbound";
	private String OUTBOUND = "outbound";
	private String CLASS = "class";
	private static String DUPLICATION	= "duplication";
	private static String FILE = "file";
	private static String PATH = "path";
	
	private static String summary ="SUMMARY:\n version, unused interface, unused block, sloppy, lego, function overload, duplicate functionality, logical deps\n";
	private static String details ="DETAILS:\n"; 
	private static JSONObject details_json = new JSONObject();
	
	private static String mainFolder 	= "subject_systems\\Struts2\\";
	private static String testRSF 		= mainFolder + "acdc\\cluster";
	private static String testDefFinder = mainFolder + "depfinder";
	private static String testClone 	= mainFolder + "clone";
	private static String logicalDep 	= mainFolder + "struts2_cleaned.csv";
	private static String outputDest 	= mainFolder + "struts2_acdc_interface_smell.csv";
	private static String packageName 	=  "org.apache.struts2"; 
	
	private Map<String, List<String>> clusterList = new HashMap<>();		
	private Map<String, String> class2component = new HashMap<>();
	private Map<Integer, Set<String>> codeClone = new HashMap<>();
	// Keep whole dependencies in Memory, using map
	// Only keep inbound at the moment
	// Mapping class name and Methond
	private Map<String, List<String>> methodList = new HashMap<>();
	// Mapping method name and inbound/outbound methods
	private Map<String, List<String>> inboundDependencies = new HashMap<>();
	private Map<String, List<String>> outboundDependencies = new HashMap<>();
	
	private Map<String, List<String>> inboundClasses = new HashMap<>();
	private Map<String, List<String>> outboundClasses = new HashMap<>();

	private Map<String, List<String>> classLogicalDependencies = new HashMap<>();
	
	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
		String mainFolder; 	
		String testRSF;
		String testDefFinder;
		String testClone;
		String logicalDep;
		String outputDest;
		String packageName;
		
		if (args.length == 7)
		{
			mainFolder = args[0];
			testRSF = mainFolder + args[1];
			testDefFinder = mainFolder + args[2];
			testClone = mainFolder + args[3];
			logicalDep = mainFolder + args[4];
			packageName = args[5]; 
			outputDest = mainFolder + args[6];		
		} else {
			mainFolder = DependencyFinderProcessing_ExportJSON.mainFolder;
			testRSF = mainFolder + DependencyFinderProcessing_ExportJSON.testRSF;
			testDefFinder = mainFolder + DependencyFinderProcessing_ExportJSON.testDefFinder;
			testClone = mainFolder + DependencyFinderProcessing_ExportJSON.testClone;
			logicalDep = mainFolder + DependencyFinderProcessing_ExportJSON.logicalDep;
			outputDest = mainFolder + DependencyFinderProcessing_ExportJSON.outputDest;
			packageName = DependencyFinderProcessing_ExportJSON.packageName;
		}
		final File depFinderDir = FileUtil.checkDir(testDefFinder, false, false);
		final File clustersDir = FileUtil.checkDir(testRSF, false, false);
		final File cloneDir = FileUtil.checkDir(testClone, false, false);

		List<File> fileList = FileListing.getFileListing(depFinderDir);
		fileList = FileUtil.sortFileListByVersion(fileList);
		final Set<File> orderedSerFiles = new LinkedHashSet<>();
		for (final File file : fileList) {
			if (file.getName().endsWith(".xml")) {
				orderedSerFiles.add(file);
			}
		}
		
		fileList = FileListing.getFileListing(clustersDir);
		fileList = FileUtil.sortFileListByVersion(fileList);
		final Set<File> clusterFiles = new LinkedHashSet<>();
		for (final File file : fileList) {
			if (file.getName().endsWith(".rsf")) {
				clusterFiles.add(file);
			}
		}
		
		fileList = FileListing.getFileListing(cloneDir);
		fileList = FileUtil.sortFileListByVersion(fileList);
		final Set<File> cloneFiles = new LinkedHashSet<>();
		for (final File file : fileList) {
			if (file.getName().endsWith(".xml")) {
				cloneFiles.add(file);
			}
		}

		Map<String, String> versionSmells = new LinkedHashMap<>();
		final String versionSchemeExpr = "[0-9]+\\.[0-9]+(\\.[0-9]+)*+((-|\\.)(RC|ALPHA|BETA|M|Rc|Alpha|Beta|rc|alpha|beta)[0-9])*";
		for (final File file : orderedSerFiles) {
			logger.debug(file.getName());
			final String version = FileUtil.extractVersionFromFilename(versionSchemeExpr, file.getName());
			assert !version.equals("") : "Could not extract version";
			versionSmells.put(version, file.getAbsolutePath());
		}
		
		Map<String, String> clusterSmells = new LinkedHashMap<>();
		for (final File file : clusterFiles) {
			logger.debug(file.getName());
			final String version = FileUtil.extractVersionFromFilename(versionSchemeExpr, file.getName());
			assert !version.equals("") : "Could not extract version";
			clusterSmells.put(version, file.getAbsolutePath());
		}
		
		Map<String, String> cloneVersions = new LinkedHashMap<>();
		for (final File file : cloneFiles) {
			logger.debug(file.getName());
			final String version = FileUtil.extractVersionFromFilename(versionSchemeExpr, file.getName());
			assert !version.equals("") : "Could not extract version";
			cloneVersions.put(version, file.getAbsolutePath());
		}
		
		for (String key : versionSmells.keySet()){
			logger.info("Start detecting smells for one version:" + versionSmells.get(key)+", "+clusterSmells.get(key));
			String smellFile = versionSmells.get(key);
			String clusterFile =  clusterSmells.get(key);
			String cloneFile   = cloneVersions.get(key);
			if (smellFile == null || clusterFile ==null || cloneFile == null)
				continue;
			single(versionSmells.get(key), clusterSmells.get(key), packageName, cloneVersions.get(key), key, logicalDep, outputDest);
		}
	}

	private static void single(String testDoc, String testRSF, String packageName, String testClone, String version, String logicalDep, String outputDest)
			throws IOException, ParserConfigurationException, SAXException {
		
		DependencyFinderProcessing_ExportJSON dp = new DependencyFinderProcessing_ExportJSON(testDoc, packageName);
		dp.clusterList	= dp.readClusterFile(testRSF);
		dp.codeClone	= dp.codeCloneUpdate(testClone);
		dp.classLogicalDependencies = dp.readLogicalDeps(logicalDep);
		//detect smell
		logger.info("Start detecting smells");
		
		details += "\n" + version + ":\n";
		JSONObject list_files_per_versions = new JSONObject();
		details_json.put(version, list_files_per_versions);
		
		dp.DetectSmell(version, packageName);
		
		// finish summary and move to new version
		summary += "\n";
		try {
			writeToFile(summary + "\n" + details, outputDest);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public void detectLogicalDepBetweenComponents(String version){
		Map<String, Set<String>> storage = new HashMap<>();
		
		Set<String> allLogicalDeps = classLogicalDependencies.keySet();
		Iterator<String> it = allLogicalDeps.iterator();
		logger.info("componentName, other components , smells?");
		while (it.hasNext()) {
			String fromClass = it.next();
			String fromComp = class2component.get(fromClass);
			if (fromComp != null) {
				List<String> classList = classLogicalDependencies.get(fromClass);
				Set<String> componentList = storage.get(fromComp);
				if (componentList == null )
					componentList = new HashSet<>();
				if (classList != null) {
					for (String c : classList){
						String com = class2component.get(c);
						if (com != null) {
							componentList.add(com);
						}
					}
					storage.put(fromComp, componentList);
				}
			}
		}
		int total = 0;
		for (String s : storage.keySet()) {
			Set<String> componentList = storage.get(s);
			if (componentList!= null && !componentList.isEmpty()){
				int numOfConnection = componentList.size();
				// don't care if this is inner dependencies
				if (componentList.contains(s)) {
					numOfConnection --;
				}
				if (numOfConnection > 0) {
					total ++;
					logger.info(s + "," +componentList.toString() + "," + numOfConnection);
					details += s + "," +componentList.toString() + "," + numOfConnection + "\n";
					
					JSONObject file_list = (JSONObject) details_json.get(version);
					// add all lego & overload to json, only consider class, not whole component
					for (String classname : clusterList.get(s)){
						JSONObject temp = (JSONObject) file_list.get(classname);
						if(temp == null)
							temp = new JSONObject();
						temp.put("Logical_Dependency", 1);
						file_list.put(StringUtil.cutInnterClass(classname), temp);
					}
				}
			}
		}
		summary += total + ",";
	}
	
	public Map<String, List<String>> readLogicalDeps(String logicalDep){
		Map<String, List<String>> storage = new HashMap<>();
		String line = "";
		String cvsSplitBy = ",";
	 
		try (BufferedReader br = new BufferedReader(new FileReader(logicalDep))) {
			br.readLine();
			while ((line = br.readLine()) != null) {
			  // use comma as separator
				String[] classes = line.split(cvsSplitBy);
				
				String first = classes[0];
				String second = classes[1];
				List<String> tmp = storage.get(first);
				if (tmp == null) {
					tmp = new ArrayList<>();
				}
				tmp.add(second);
				storage.put(first, tmp);
				
				tmp = storage.get(second);
				if (tmp == null) {
					tmp = new ArrayList<>();
				}
				tmp.add(first);
				storage.put(second, tmp);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return storage;
	}
	
	
	public Map<String, List<String>> readClusterFile(String filePath){
		Map<String, List<String>> clusterList = new HashMap<>();
		// Read rsf file and generate the mapping of clusters and classes
		final List<List<String>> facts = Lists.newArrayList();

		try (BufferedReader in = new BufferedReader(new FileReader(filePath))) {
			String line;
			while ((line = in.readLine()) != null) {
				logger.debug(line);

				if (line.trim().isEmpty()) continue;

				final Scanner s = new Scanner(line);
				final String expr = "([^\"\\s][^\\s]*[^\"\\s]*)|([\"][^\"]*[\"])";

				final String arcType = s.findInLine(expr);
				final String startNode = s.findInLine(expr);
				final String endNode = s.findInLine(expr);
				final List<String> fact = Lists.newArrayList(arcType, startNode, endNode);
				logger.debug(fact);
				facts.add(fact);
				// add to cluster
				List<String> currentCluster = clusterList.get(startNode);
				if (currentCluster == null)
					currentCluster = new ArrayList<>();
				currentCluster.add(endNode);
				clusterList.put(startNode, currentCluster);
				class2component.put(endNode, startNode);
				if (s.findInLine(expr) != null) {
					logger.error("Found non-triple in file: " + line);
					System.exit(1);
				}

				s.close();
			}
		} catch (final IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return clusterList;
	}
	

	public DependencyFinderProcessing_ExportJSON(String xmlFilePath, String packageName) throws IOException, ParserConfigurationException, SAXException{
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
		File file	= new File(xmlFilePath);
		Document doc = builder.parse(file);
		
		logger.info("Started processing XML input");
		// Building class method mapping
		NodeList nclist = doc.getElementsByTagName(CLASS);
		for (int k = 0; k < nclist.getLength(); k++){
			Node node = nclist.item(k);
			logger.debug("\nCurrent Element :" + node.getNodeName());
			Element eElement = (Element) node;
			String key = eElement.getElementsByTagName(NAME).item(0).getTextContent();	
			logger.debug("Current key :" + key);
			NodeList nflist = eElement.getElementsByTagName(FEATURE);
			List<String> methods	= new ArrayList<>();
			for (int n = 0; n < nflist.getLength(); n++){
				Element	e = (Element) nflist.item(n);
				methods.add(e.getElementsByTagName(NAME).item(0).getTextContent());
				logger.debug("Current methods :" + e.getElementsByTagName(NAME).item(0).getTextContent());
			}
			methodList.put(key, methods);
		}
		
		// Building inbound dependencies
		NodeList 		nlist 	= doc.getElementsByTagName(FEATURE);
		for (int i = 0; i < nlist.getLength(); i++){
			Node node = nlist.item(i);
			logger.debug("\nCurrent Element :" + node.getNodeName());
			 
			if (node.getNodeType() == Node.ELEMENT_NODE) {
	 
				Element eElement = (Element) node;
				String key = eElement.getElementsByTagName(NAME).item(0).getTextContent();		
				logger.debug("Current key :" + key);

				//Get inbounds
				NodeList inboundNode = eElement.getElementsByTagName(INBOUND);
				List<String> inbounds = new ArrayList<>();
	 			for (int j = 0; j < inboundNode.getLength(); j++){
	 				inbounds.add(inboundNode.item(j).getTextContent());
	 				logger.debug("Current inbound :" + inboundNode.item(j).getTextContent());
	 			}
	 			
	 			//Get outbounds
	 			NodeList outboundNode		= eElement.getElementsByTagName(OUTBOUND);
				List<String> outbounds	= new ArrayList<>();
	 			for (int j = 0; j < outboundNode.getLength(); j++){
	 				outbounds.add(outboundNode.item(j).getTextContent());
	 				logger.debug("Current inbound :" + outboundNode.item(j).getTextContent());
	 			}
	 			if (key.startsWith(packageName)){
		 			inboundDependencies.put(key, inbounds);
		 			outboundDependencies.put(key, outbounds);
	 			}
			}
		}
		
		// Doing the samething with class type
		nlist 	= doc.getElementsByTagName(CLASS);
		for (int i = 0; i < nlist.getLength(); i++){
			Node node = nlist.item(i);
			logger.debug("\nCurrent Element :" + node.getNodeName());
			 
			if (node.getNodeType() == Node.ELEMENT_NODE) {
	 
				Element eElement = (Element) node;
				String key = eElement.getElementsByTagName(NAME).item(0).getTextContent();		
				logger.debug("Current key :" + key);

				//Get inbounds
				NodeList inboundNode = eElement.getElementsByTagName(INBOUND);
				List<String> inbounds = new ArrayList<>();
	 			for (int j = 0; j < inboundNode.getLength(); j++){
	 				inbounds.add(inboundNode.item(j).getTextContent());
	 				logger.debug("Current inbound :" + inboundNode.item(j).getTextContent());
	 			}
	 			
	 			//Get outbounds
	 			NodeList outboundNode = eElement.getElementsByTagName(OUTBOUND);
				List<String> outbounds	= new ArrayList<>();
	 			for (int j = 0; j < outboundNode.getLength(); j++){
	 				outbounds.add(outboundNode.item(j).getTextContent());
	 				logger.debug("Current inbound :" + outboundNode.item(j).getTextContent());
	 			}
	 			if (key.startsWith(packageName)){
		 			inboundClasses.put(key, inbounds);
		 			outboundClasses.put(key, outbounds);
	 			}
			}
		}
		
		// Handling code clone part
		logger.info("Finished processing XML input");
	}

	public Map<Integer, Set<String>> codeCloneUpdate(String xmlFilePath) throws ParserConfigurationException, SAXException, IOException {
		//Handle by DOM Parser
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(true);
		factory.setIgnoringElementContentWhitespace(true);
		
		DocumentBuilder builder = factory.newDocumentBuilder();
		File			file	= new File(xmlFilePath);
		Document		doc 	= builder.parse(file);
		Map<Integer, Set<String>> codeCloneFromFile = new HashMap<>();		
		
		logger.info("Started processing clone");
		// Building class method mapping
		NodeList nclist = doc.getElementsByTagName(DUPLICATION);
		for (int k = 0; k < nclist.getLength(); k++){
			Node node = nclist.item(k);
			logger.debug("\nCurrent Element :" + node.getNodeName());
			Element eElement = (Element) node;
			NodeList duplicatedFiles = eElement.getElementsByTagName(FILE);
			
			//Keep tracks of duplicated class files
			// Handle className
			Set<String> tmp = new HashSet<>();
			for (int i = 0; i < duplicatedFiles.getLength();  i++){
				Element e = (Element) duplicatedFiles.item(i);
				if (! e.getAttribute(PATH).contains("src"))
					continue;
				String path =  e.getAttribute(PATH);
				String className = "";
				if (path.contains("//")) {
				 className = e.getAttribute(PATH).replace("\\", ".").split("\\.src")[1].replace(".java", "").substring(1);
				} else if (path.contains("/")){
				  className = e.getAttribute(PATH).replace("/", ".").split("\\.src")[1].replace(".java", "").substring(1);
				}
				if (className.startsWith("main."))
					className = className.replace("main.", "");
				if (className.startsWith("test."))
					className = className.replace("test.", "");
				tmp.add(className);
			}
			codeCloneFromFile.put(k, tmp);
		}
		
		// Building inbound dependencies
		logger.info("Finished processing clone input");
		return codeCloneFromFile;
	}
	
	public void DetectSmell(String version, String packageName){
		summary += version + ",";
		DetectUnusedInterface(version, packageName);

		details += "Sloppy Delegation:\n";
		details += "component, affected class, total class, percentages\n";
		DetectSloppyDelegation(version, packageName);
		
		details += "\nLego and Overload:\n";
		details += "componentName, number of public methods , smells\n";
		DetectLegoSyndomeAndFunctionalityOverload(version, packageName);
		
		details += "\nCode colone based:\n";
		details += "componentName, affected classes, total classes, percentages\n";
		detectComponentClonebyClassLevel(version);

		//detect cochange components
		details += "\nCo change: \n";
		details += "componentName, other components , smells?\n";
		detectLogicalDepBetweenComponents(version);
	}
	
	public void DetectUnusedInterface(String version, String packageName){
		// Read all component
		// If there all methods in one class without inbound
		// Indicated that component has Unused Interface
		String content = "";
		JSONObject file_list = (JSONObject) details_json.get(version);
		
		Set<String> allComponent = clusterList.keySet();
		Set<String> unusedComponent = new HashSet<>(); 
		Set<String> unusedInterface = new HashSet<>(); 
		logger.info("componentName, className , methodName");
		content += "\nUnused interface: \n";
		content += "componentName, className\n";
		Iterator<String> it = allComponent.iterator();
		while (it.hasNext()){
			String componentName = it.next();
			List<String> classList = clusterList.get(componentName);
			for(String className: classList){
				if (!className.startsWith(packageName))
					break;
				boolean classhasSmell = true;
				List<String> methods	= methodList.get(className);
				if (methods != null){
					for(String methodName: methods){
						List<String> inboundMethod	= inboundDependencies.get(methodName);
						if (inboundMethod != null && inboundMethod.size() > 0){
							classhasSmell = false;
							break;
						}
					}
					
					if (classhasSmell == true){
						List<String> inboundClass	= inboundClasses.get(className);
						if (inboundClass == null || inboundClass.size() == 0){
							logger.info(componentName + "," + className + ",,");
							content +=componentName + "," + className + ",,\n";

							unusedComponent.add(componentName);
							unusedInterface.add(className);
						}
					}
				}
			}
		}
		
		// check unused component
		Set<String> unsedCompSmell = new HashSet<>();
		logger.info("\nUnused components:");
		content += "\nUnused components:\n";
		content += "component Name,\n";
		for (String com :allComponent) {
			List<String> classList = clusterList.get(com);
			boolean unsedCom =  true;
			boolean containPackageNameClass = false;
			for (String className : classList) {
				if (!className.startsWith(packageName))
					continue;
				containPackageNameClass = true;
				if (!unusedInterface.contains(className)) {
					unsedCom = false;
				}
			}
			if (unsedCom && containPackageNameClass){
				logger.info(com);	
				content += com + "\n";
				unsedCompSmell.add(com);
			}
		}
		
		summary += unusedInterface.size() +  "," + unsedCompSmell.size() + ",";
		details += content + "\n";
		
		//add all unused interface to json
		for (String classname:unusedInterface){
			JSONObject temp = (JSONObject) file_list.get(classname);
			if(temp == null)
				temp = new JSONObject();
			temp.put("Unused_Interface", 1);
			file_list.put(StringUtil.cutInnterClass(classname), temp);
		}
		// add all unused component to json
		for (String unused : unsedCompSmell){
			List<String> classList = clusterList.get(unused);
				for (String classname : classList){
					JSONObject temp = (JSONObject) file_list.get(classname);
					if(temp == null)
						temp = new JSONObject();
					temp.put("Unused_Comp", 1);
					file_list.put(StringUtil.cutInnterClass(classname), temp);
				}
		}
	}
	
	public void DetectUnusedComponent(){
		// Read all component
		// If there all methods in all class without inbound from outside of the components
		// Indicated that component has block
		Set<String> allComponent = clusterList.keySet();
		logger.info("componentName, className , methodName");
		Iterator<String> it = allComponent.iterator();
		while (it.hasNext()){
			boolean hasSmell = true;
			String componentName = it.next();
			List<String> classList = clusterList.get(componentName);
			outterloop:
			for(String className: classList){
				List<String> methods	= methodList.get(className);
				if (methods != null) {
					for(String methodName: methods){
						// extract class contains the method
						String tmp = methodName.split(Pattern.quote("("))[0];
						String[] tmpA = tmp.split(Pattern.quote("."));
						String containClass = "";
						for (int i = 0; i <= tmpA.length-3; i++){
							containClass += tmpA[i] +".";
						}
						containClass += tmpA[tmpA.length-2];
						// if this class doesn't belong to this component, then this is not unused component
						if (!classList.contains(containClass)){
							hasSmell = false;
							break outterloop;
						}
					}
				}
			}
			if (hasSmell){
				//print them out
				logger.info(componentName + ",,,");
			}
		}
	}
	
	public void DetectAmbiguousInterface(){
		// Read all component
		// If there is only one interface within a components are using by other component, indicated as Ambiguous Interface
		// Indicated that component has block
		
		Set<String> allComponent = clusterList.keySet();
		logger.info("componentName, className , methodName");
		Iterator<String> it = allComponent.iterator();
		while (it.hasNext()){
			String componentName = it.next();
			List<String> classList = clusterList.get(componentName);
			int outsideCaller = 0;
			for(String className: classList){
				List<String> methods = methodList.get(className);
				if (methods != null) {
					for(String methodName: methods){
						// count number of method using by outside 
						// extract class contains the method
						String tmp = methodName.split(Pattern.quote("("))[0];
						String[] tmpA = tmp.split(Pattern.quote("."));
						String containClass = "";
						for (int i = 0; i <= tmpA.length-3; i++){
							containClass += tmpA[i] +".";
						}
						containClass += tmpA[tmpA.length-2];
						
						// if this class doesn't belong to this component, then this is not unused component
						if (!classList.contains(containClass)){
							outsideCaller++;
						}
					}
				}
			}
			if (outsideCaller == 1){
				//print them out
				logger.info(componentName + ",,,");
			}
		}
	}
	
	public void DetectSloppyDelegation(String version, String packageName){
		// Read all the methods / features
		// If it only have inbound from other components
		// And have no outbound from itself
		// We can considered it as Sloppy Delegation Smell
		Set<String> allFeatures	= inboundDependencies.keySet();
		logger.info("component, affected class, total class, percentages");
		String className = "";
		Iterator<String> it = allFeatures.iterator();
		HashMap<String, Set<String>> componentHasSmell = new HashMap<>();
		
		while (it.hasNext()) {
			String featureName = it.next();
			className = getClassName(featureName);
			if (className.startsWith(packageName) ){
				boolean hasSmell = true;
				String currentComponent = class2component.get(className);
				List<String> inbounds = inboundDependencies.get(featureName);
				List<String> outbounds = outboundDependencies.get(featureName);
				
				if (outbounds.size() == 0){
					for (String in : inbounds){
						String compName = class2component.get(getClassName(in));
						// TODO doubble check why it can be null ???
						if (currentComponent!= null && currentComponent.equals(compName)){
							hasSmell = false;
							break;
						}
					}
				} else {
					hasSmell	= false;
				}
				
				if (hasSmell) {
					Set<String> classes = componentHasSmell.get(currentComponent);
					if (classes == null) {
						classes = new HashSet<>();
					} 
					classes.add(getClassName(featureName));
					componentHasSmell.put(currentComponent, classes);
				}
			}
		}
		System.out.println(version);
		for (String s : componentHasSmell.keySet()){
			if (s == null)
					continue;
			details += s + ","+ componentHasSmell.get(s).size() + "," + clusterList.get(s).size() + "," + (float) componentHasSmell.get(s).size()*100/clusterList.get(s).size() + "\n";
		}
		
		// add all sloopy delegation to json, only consider class, not whole component
		JSONObject file_list = (JSONObject) details_json.get(version);
		for (String s : componentHasSmell.keySet()){
				for (String classname : componentHasSmell.get(s)){
					JSONObject temp = (JSONObject) file_list.get(classname);
					if(temp == null)
						temp = new JSONObject();
					temp.put("Sloopy_Delegation", 1);
					file_list.put(StringUtil.cutInnterClass(classname), temp);
				}
		}
		
		summary += componentHasSmell.keySet().size() +",";
	}
	
	public void DetectLegoSyndomeAndFunctionalityOverload(String version, String packageName){
		// Read all component
		// Count number of public methods per components
		// Compute average
		int lego = 0;
		int over = 0;
		Set<String> allComponent = clusterList.keySet();
		double[]    optStat		 = new double[allComponent.size()];
		int i = 0;
		Map<String, Integer> counter = new HashMap<>();
		logger.info("componentName, number of public methods , smells?");
		Iterator<String> it = allComponent.iterator();
		while (it.hasNext()){
			
			String componentName  	= it.next();
			List<String> classList 	= clusterList.get(componentName);
			int numOpt				= 0;
			for(String className: classList){
				List<String> methods	= methodList.get(className);
				if (className.startsWith(packageName) && methods != null) {
							numOpt++;
				}
			}
			counter.put(componentName, numOpt);
			optStat[i] = numOpt;
			i++;
		}
		
		JSONObject file_list = (JSONObject) details_json.get(version);
		
		System.out.println("Operations stats:");
		DescriptiveStatistics OperationStats = new DescriptiveStatistics(optStat);
		System.out.println(OperationStats);
		for (String com : counter.keySet()) {
			boolean hasSmell = false;
			String  smellType = "";
			if (counter.get(com) > OperationStats.getMean() + 1.5*OperationStats.getStandardDeviation()) {
				hasSmell = true;
				smellType = "Overload";
				over ++;
			}
			if (counter.get(com) < OperationStats.getMean() - 0.75*OperationStats.getStandardDeviation()) {
				hasSmell = true;
				smellType = "Lego";
				lego ++;
			}
			if (hasSmell) {
				logger.info(com + ", "+ counter.get(com) +"," + smellType);
				details += com + ", "+ counter.get(com) +"," + smellType + "\n";
				
				// add all lego & overload to json, only consider class, not whole component
				for (String classname : clusterList.get(com)){
					JSONObject temp = (JSONObject) file_list.get(classname);
					if(temp == null)
						temp = new JSONObject();
					temp.put(smellType, 1);
					file_list.put(StringUtil.cutInnterClass(classname), temp);
				}
			}
		}
		
		summary += lego +"," + over +",";
	}
	
	public void detectComponentClone(){
		Set<Integer> allduplication = codeClone.keySet();
		Iterator<Integer> it = allduplication.iterator();
		logger.info("componentName, other components , smells?");
		while (it.hasNext()) {
			Set<String> classList = codeClone.get(it.next());
			Set<String> componentList = new HashSet<>();
			if (classList.size() >1) {
				for (String c : classList){
					String com = class2component.get(c);
					if (com != null) {
						componentList.add(com);
					}
				}
				if (componentList.size() >1){
					logger.info(componentList.toString());
				}
					
			}
		}
	}
	
	public void detectComponentClonebyClassLevel(String version){
		Set<Integer> allduplication = codeClone.keySet();
		Iterator<Integer> it = allduplication.iterator();
		logger.info("componentName, other components , smells?");
		int total = 0;
		Map<String, List<String>> effectedClass = new HashMap<>();
		while (it.hasNext()) {
			Set<String> classList = codeClone.get(it.next());
			Set<String> componentList = new HashSet<>();
			if (classList.size() >1) {
				for (String c : classList){
					String com = class2component.get(c);
					if (com != null) {
						componentList.add(com);
						
						// add to affected class
						List<String> effected = effectedClass.get(com);
						if (effected == null)
							effected = new ArrayList<>();
						effected.add(c);
						effectedClass.put(com, effected);
					}
				}
				if (componentList.size() >1){
					logger.info(componentList.toString());
				}
			}
		}
		
		// Analyze component with many affected class
		logger.info("componentName, totalClass , effected?");
		for (String com : effectedClass.keySet()) {
			List<String> effected = effectedClass.get(com);
			List<String> allClass = clusterList.get(com);
			float effect = (effected.size()*100.0f)/allClass.size();
			logger.info(com + ", " + allClass.size() + ", "+ effected.size() + ", " + effect);
			// 10% of class has code clone
			if (effect > 10) {
				details += com +", "+ effected.size() + ", " + allClass.size()  + ", " + effect +"\n";
				total ++;
				
				JSONObject file_list = (JSONObject) details_json.get(version);
				// add all lego & overload to json, only consider class, not whole component
				for (String classname : clusterList.get(com)){
					JSONObject temp = (JSONObject) file_list.get(classname);
					if(temp == null)
						temp = new JSONObject();
					temp.put("Clone_Comp", 1);
					file_list.put(StringUtil.cutInnterClass(classname), temp);
				}
			}
		}
		summary += total + ",";
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
			className	= featureName.substring(0, featureName.lastIndexOf("."));
		}
		return className;
	}
	
	private static void writeToFile(String content,String path) throws IOException {
		File file = new File(path);
		File json_file = new File(path+".json");
		// if file doesnt exists, then create it
		if (!file.exists())
			file.createNewFile();
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(content);
		bw.close();
		
		// if file doesnt exists, then create it
		if (!json_file.exists()) {
			json_file.createNewFile();
		}
		fw = new FileWriter(json_file.getAbsolutePath());
		bw = new BufferedWriter(fw);
		bw.write(details_json.toJSONString());
		bw.close();

		System.out.println("Done");
	}
}