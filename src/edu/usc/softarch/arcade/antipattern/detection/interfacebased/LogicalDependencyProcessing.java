package edu.usc.softarch.arcade.antipattern.detection.interfacebased;

/**
 * 
 * @author d.le 
 * 
 * Don't use this any more, only use {@link edu.usc.softarch.arcade.antipattern.detection.interfacebased.DependencyFinderProcessing}
 * 
 */

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
import org.apache.logging.log4j.Logger;
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

public class LogicalDependencyProcessing {
	static Logger logger = org.apache.logging.log4j.LogManager.getLogger(LogicalDependencyProcessing.class);
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
	public HashMap<String, List<String>> classLogicalDependencies 	= new HashMap<String, List<String>>();

	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
		// TODO Auto-generated method stub
		String 	testDefFinder = "F:\\Ivy\\depfinder";
		String 	testRSF = "F:\\Ivy\\rsf";
		String  testClone = "F:\\Ivy\\clone";
		String logicalDep = "F:\\logical_dependencies\\Ivy_output_cleaned_2.csv";
		String packageName = "org.apache.ivy";

		// inputDir is the directory containing the .xml files which
		// contain dependency
//		final File depFinderDir = FileUtil.checkDir(args[0], false, false);
		// directory containing the cluster rsf files matching the xml
		// files
//		final File clustersDir = FileUtil.checkDir(args[1], false, false);
//		final File cloneDir    = FileUtil.checkDir(args[2], false, false);
//		final File packageName = FileUtil.checkDir(args[3], false, false);
		
		final File depFinderDir = FileUtil.checkDir(testDefFinder, false, false);
		final File clustersDir = FileUtil.checkDir(testRSF, false, false);
		final File cloneDir = FileUtil.checkDir(testClone, false, false);

	
		List<File> fileList = FileListing.getFileListing(depFinderDir);
		fileList = FileUtil.sortFileListByVersion(fileList);
		final Set<File> orderedSerFiles = new LinkedHashSet<File>();
		for (final File file : fileList) {
			if (file.getName().endsWith(".xml")) {
				orderedSerFiles.add(file);
			}
		}
		
		fileList = FileListing.getFileListing(clustersDir);
		fileList = FileUtil.sortFileListByVersion(fileList);
		final Set<File> clusterFiles = new LinkedHashSet<File>();
		for (final File file : fileList) {
			if (file.getName().endsWith(".rsf")) {
				clusterFiles.add(file);
			}
		}
		
		fileList = FileListing.getFileListing(cloneDir);
		fileList = FileUtil.sortFileListByVersion(fileList);
		final Set<File> cloneFiles = new LinkedHashSet<File>();
		for (final File file : fileList) {
			if (file.getName().endsWith(".xml")) {
				cloneFiles.add(file);
			}
		}

		Map<String, String> versionSmells = new LinkedHashMap<String, String>();
		final String versionSchemeExpr = "[0-9]+\\.[0-9]+(\\.[0-9]+)*";
		for (final File file : orderedSerFiles) {
			logger.debug(file.getName());
			final String version = FileUtil.extractVersionFromFilename(versionSchemeExpr, file.getName());
			assert !version.equals("") : "Could not extract version";
			versionSmells.put(version, file.getAbsolutePath());
		}
		
		Map<String, String> clusterSmells = new LinkedHashMap<String, String>();
		for (final File file : clusterFiles) {
			logger.debug(file.getName());
			final String version = FileUtil.extractVersionFromFilename(versionSchemeExpr, file.getName());
			assert !version.equals("") : "Could not extract version";
			clusterSmells.put(version, file.getAbsolutePath());
		}
		
		Map<String, String> cloneVersions = new LinkedHashMap<String, String>();
		for (final File file : cloneFiles) {
			logger.debug(file.getName());
			final String version = FileUtil.extractVersionFromFilename(versionSchemeExpr, file.getName());
			assert !version.equals("") : "Could not extract version";
			cloneVersions.put(version, file.getAbsolutePath());
		}
		
		for (String key : versionSmells.keySet()){
			logger.info("Start detecting smells for one version:" + versionSmells.get(key)+", "+clusterSmells.get(key));
			single(versionSmells.get(key), clusterSmells.get(key), packageName, cloneVersions.get(key), logicalDep);
		}
		

	}

	private static void single(String testDoc, String testRSF, String packageName, String testClone, String logicDeps)
			throws IOException, ParserConfigurationException, SAXException {
		
		LogicalDependencyProcessing dp = new LogicalDependencyProcessing(testDoc, packageName);
		dp.clusterList	= dp.readClusterFile(testRSF);
		dp.codeClone	= dp.codeCloneUpdate(testClone, packageName);
		dp.classLogicalDependencies = dp.readLogicalDeps(logicDeps);
		//detect smell
		logger.info("Start detecting smells");
//		dp.DetectSmell();
		dp.detectLogicalDepBetweenComponents();
	}
	
	public HashMap<String, List<String>> readLogicalDeps(String logicalDep){
		
		HashMap<String, List<String>> storage = new HashMap<String, List<String>>();
		
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
	 
		try {
			br = new BufferedReader(new FileReader(logicalDep));
			br.readLine();
			while ((line = br.readLine()) != null) {
	 
			    // use comma as separator
				String[] classes = line.split(cvsSplitBy);
				
				String first = classes[0];
				String second = classes[1];
				List<String> tmp = storage.get(first);
				if (tmp == null) {
					tmp = new ArrayList<String>();
				}
				tmp.add(second);
				storage.put(first, tmp);
				
				tmp = storage.get(second);
				if (tmp == null) {
					tmp = new ArrayList<String>();
				}
				tmp.add(first);
				storage.put(second, tmp);

				
			}
		} catch(Exception e) {
			
		}
		return storage;
	}
	
	
	
	
	public HashMap<String, List<String>> readClusterFile(String filePath){
		HashMap<String, List<String>> clusterList		    = new HashMap<String, List<String>>();
		// Read rsf file and generate the mapping of clusters and classes
		final List<List<String>> facts = Lists.newArrayList();
		final boolean local_debug = false;

		try {
			final BufferedReader in = new BufferedReader(new FileReader(filePath));
			String line;
			// int lineCount = 0;
			// final int limit = 0;
			while ((line = in.readLine()) != null) {
				// if (lineCount == limit && limit != 0) {
				// break;
				// }
				if (local_debug) {
					logger.debug(line);
				}

				if (line.trim().isEmpty()) {
					continue;
				}

				final Scanner s = new Scanner(line);
				// String expr = "([\"\"'])(?:(?=(\\?))\2.)*?\1|([^\"].*[^\"])";
				// String expr = "^[^\"][^\\s]+[^\"]$";
				// String expr = "([^\"\\s][^\\s]*[^\"\\s])"; // any non
				// whitespace characters without quotes or whitespace at the
				// start or beginning
				// String expr = "([\"][^\"]*[\"])"; // any characters in quotes
				// including the quotes
				final String expr = "([^\"\\s][^\\s]*[^\"\\s]*)|([\"][^\"]*[\"])";
				// int tokenLimit = 3;

				final String arcType = s.findInLine(expr);
				final String startNode = s.findInLine(expr);
				final String endNode = s.findInLine(expr);
				final List<String> fact = Lists.newArrayList(arcType, startNode, endNode);
				if (local_debug) {
					logger.debug(fact);
				}
				facts.add(fact);
				// add to cluster
				List<String> currentCluster = clusterList.get(startNode);
				if (currentCluster == null) {
					currentCluster = new ArrayList<String>();
				}
				currentCluster.add(endNode);
				clusterList.put(startNode, currentCluster);
				class2component.put(endNode, startNode);
				if (s.findInLine(expr) != null) {
					logger.error("Found non-triple in file: " + line);
					System.exit(1);
				}

				/*
				 * MatchResult result = s.match(); for (int i=1;
				 * i<=result.groupCount(); i++) logger.debug(i + ": " +
				 * result.group(i)); s.close();
				 */

				// lineCount++;
				/*
				 * if (triple.size() != 3) {
				 * logger.error("Found non-triple in file: " + triple);
				 * System.exit(1); } logger.debug(triple);
				 */
				s.close();
			}
			in.close();
		} catch (final IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
//		return facts;
		return clusterList;
	}
	

	public LogicalDependencyProcessing(String xmlFilePath, String packageName) throws IOException, ParserConfigurationException, SAXException{
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
		
		logger.info("Started processing XML input");
		// Building class method mapping
		NodeList 		nclist 	= doc.getElementsByTagName(CLASS);
		for (int k = 0; k < nclist.getLength(); k++){
			Node 	node 				= nclist.item(k);
			logger.debug("\nCurrent Element :" + node.getNodeName());
			Element eElement 			= (Element) node;
			String 	key		 			= eElement.getElementsByTagName(NAME).item(0).getTextContent();	
			logger.debug("Current key :" + key);
			NodeList 		nflist 		= eElement.getElementsByTagName(FEATURE);
			ArrayList<String> methods	= new ArrayList<String>();
			for (int n = 0; n < nflist.getLength(); n++){
				Element	e 	= (Element) nflist.item(n);
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
	 
				Element eElement 			= (Element) node;
				String 	key		 			= eElement.getElementsByTagName(NAME).item(0).getTextContent();		
				logger.debug("Current key :" + key);

				//Get inbounds
				NodeList inboundNode		= eElement.getElementsByTagName(INBOUND);
				ArrayList<String> inbounds	= new ArrayList();
	 			for (int j = 0; j < inboundNode.getLength(); j++){
	 				inbounds.add(inboundNode.item(j).getTextContent());
	 				logger.debug("Current inbound :" + inboundNode.item(j).getTextContent());
	 			}
	 			
	 			//Get outbounds
	 			NodeList outboundNode		= eElement.getElementsByTagName(OUTBOUND);
				ArrayList<String> outbounds	= new ArrayList();
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
		
		// Handling code clone part
		logger.info("Finished processing XML input");
		
		// Handling 
	}

	public HashMap<Integer, Set<String>> codeCloneUpdate(String xmlFilePath, String packageName) throws ParserConfigurationException, SAXException, IOException {
		//Handle by DOM Parser
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(true);
		factory.setIgnoringElementContentWhitespace(true);
		
		DocumentBuilder builder = factory.newDocumentBuilder();
		File			file	= new File(xmlFilePath);
		Document		doc 	= builder.parse(file);
		HashMap<Integer, Set<String>> codeCloneFromFile = new HashMap<Integer, Set<String>>();		
		
		logger.info("Started processing clone");
		// Building class method mapping
		NodeList 		nclist 	= doc.getElementsByTagName(DUPLICATION);
		for (int k = 0; k < nclist.getLength(); k++){
			Node 	node 				= nclist.item(k);
			logger.debug("\nCurrent Element :" + node.getNodeName());
			Element eElement 			= (Element) node;
			NodeList duplicatedFiles	= eElement.getElementsByTagName(FILE);
			
			//Keep tracks of duplicated class files
			// Handle className
			Set<String> tmp = new HashSet<String>();
			for (int i = 0; i < duplicatedFiles.getLength();  i++){
				Element e 	= (Element) duplicatedFiles.item(i);
//				logger.info(e.getAttribute(PATH));
				String className = e.getAttribute(PATH).replace("\\", ".").split("src")[1].replace(".java", "").substring(1);
				tmp.add(className);
			}
			codeCloneFromFile.put(k, tmp);
		}
		
		// Building inbound dependencies
				
		logger.info("Finished processing clone input");
		return codeCloneFromFile;
	}
	
	public void DetectSmell(){
		detectLogicalDepBetweenComponents();
//		DetectUnusedInterface();
//		DetectUnusedComponent();
//		DetectAmbiguousInterface();
//		DetectSloppyDelegation();
//		DetectLegoSyndomeAndFunctionalityOverload();
//		detectComponentClone();
//		detectComponentClonebyClassLevel();
	}
	
	public void DetectUnusedInterface(){
		// Read all component
		// If there is methods in one class without inbound
		// Indicated that component has Unused Interface
		String content = "";
		Set<String> allComponent = clusterList.keySet();
		Set<String> unusedComponent = new HashSet<String>(); 
		logger.info("componentName, className , methodName");
		content +="componentName, className , methodName\n";
		Iterator<String> it = allComponent.iterator();
		while (it.hasNext()){
			boolean hasSmell	  = false;
			String componentName  = it.next();
			List<String> classList = clusterList.get(componentName);
			for(String className: classList){
				boolean classhasSmell	  = false;
				List<String> methods	= methodList.get(className);
				if (methods != null){
					for(String methodName: methods){
						List<String> inboundMethod	= inboundDependencies.get(methodName);
						if (inboundMethod != null && inboundMethod.size() == 0){
							hasSmell	= true;
							classhasSmell	  = true;
//							logger.info(componentName + "," + className + "," + methodName + ",");
							content +=componentName + "," + className + "," + methodName + ",\n";
							unusedComponent.add(componentName);
						}
					}
				}
//				if (!classhasSmell)
//					logger.info(componentName + "," + className + ", has no smell");
			}
		}
		content += unusedComponent.toString() + "\n";
		String tmp = "Used: ";
		for (String com : allComponent){
			if (!unusedComponent.contains(com)){
				tmp += com +",";
			}
		}
		content += tmp;
		try {
			writeToFile(content, "F:\\output.csv");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			boolean hasSmell	  = true;
			String componentName  = it.next();
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
							hasSmell 	= false;
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
			boolean hasSmell	  	= false;
			String componentName  	= it.next();
			List<String> classList 	= clusterList.get(componentName);
			int outsideCaller		= 0;
			for(String className: classList){
				List<String> methods	= methodList.get(className);
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
				hasSmell	  	= true;
				logger.info(componentName + ",,,");
			}
		}
		
	}
	
	public void DetectSloppyDelegation(){
		// Read all the methods / features
		// If it only have inbound from other components
		// And have no outbound from itself
		// We can considered it as Sloppy Delegation Smell
		
		Set<String> allFeatures	= inboundDependencies.keySet();
		logger.info("featureName, className, inboundCaller");
		String 		className		= "";
		Iterator<String> it = allFeatures.iterator();
		while (it.hasNext()) {
				boolean 	hasSmell 		= true;
				String 		featureName  	= it.next();
				className = getClassName(featureName);
			String currentComponent		= class2component.get(className);
			List<String> inbounds		= inboundDependencies.get(featureName);
			List<String> outbounds		= outboundDependencies.get(featureName);
			
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
				logger.info(featureName + ","+ currentComponent + "," + outbounds.toString());
			}
		}
	}
	
	public void DetectLegoSyndomeAndFunctionalityOverload(){
		// Read all component
		// Count number of public methods per components
		// Compute average
		Set<String> allComponent = clusterList.keySet();
		double[]    optStat		 = new double[allComponent.size()];
		int i = 0;
		HashMap<String, Integer> counter = new HashMap<String, Integer>();
		logger.info("componentName, number of public methods , smells?");
		Iterator<String> it = allComponent.iterator();
		while (it.hasNext()){
			
			String componentName  	= it.next();
			List<String> classList 	= clusterList.get(componentName);
			int numOpt				= 0;
			for(String className: classList){
				List<String> methods	= methodList.get(className);
				if (methods != null) {
							numOpt++;
				}
			}
			counter.put(componentName, numOpt);
//			logger.info(componentName + ", "+ numOpt +",");
			optStat[i] = numOpt;
			i++;
		}
		System.out.println("Operations stats:");
		DescriptiveStatistics OperationStats = new DescriptiveStatistics(optStat);
		System.out.println(OperationStats);
		for (String com : counter.keySet()) {
			boolean hasSmell	  	= false;
			String  smellType		= "";
			if (counter.get(com) > OperationStats.getMean() + 1.5*OperationStats.getStandardDeviation()) {
				hasSmell = true;
				smellType = "Overload";
			}
			if (counter.get(com) < OperationStats.getMean() - 1.5*OperationStats.getStandardDeviation()) {
				hasSmell = true;
				smellType = "Lego";
			}
			if (hasSmell) {
				logger.info(com + ", "+ counter.get(com) +"," + smellType);
			}
		}
		
//			if (outsideCaller == 1){
//				//print them out
//				hasSmell	  	= true;
//				logger.info(componentName + ",,,");
//			}
	}
	
	public void detectComponentClone(){
		Set<Integer> allduplication = codeClone.keySet();
		Iterator<Integer> it = allduplication.iterator();
		logger.info("componentName, other components , smells?");
		while (it.hasNext()) {
			Set<String> classList = codeClone.get(it.next());
			Set<String> componentList = new HashSet<String>();
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
	
	public void detectLogicalDepBetweenComponents(){
		HashMap<String, Set<String>> storage = new HashMap<String, Set<String>>();
		
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
					componentList = new HashSet<String>();
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
		for (String s : storage.keySet()) {
			Set<String> componentList = storage.get(s);
			if (componentList!= null && componentList.size() >0){
				logger.info(s + "," +componentList.toString() + "," + componentList.size());
			}
		}
	}
	
	public void detectComponentClonebyClassLevel(){
		Set<Integer> allduplication = codeClone.keySet();
		Iterator<Integer> it = allduplication.iterator();
		logger.info("componentName, other components , smells?");
		HashMap<String, List<String>> effectedClass = new HashMap<String, List<String>>();
		while (it.hasNext()) {
			Set<String> classList = codeClone.get(it.next());
			Set<String> componentList = new HashSet<String>();
			if (classList.size() >1) {
				for (String c : classList){
					String com = class2component.get(c);
					if (com != null) {
						componentList.add(com);
						
						// add to affected class
						List<String> effected = effectedClass.get(com);
						if (effected == null)
							effected = new ArrayList<String>();
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
			logger.info(com + ", " + allClass.size() + ", "+ effected.size() + ", " + (float) ((effected.size()*100.0f)/allClass.size()));
		}
		
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
	
	private void writeToFile(String content,String path) throws IOException {
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
