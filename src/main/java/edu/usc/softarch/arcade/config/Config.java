package edu.usc.softarch.arcade.config;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import soot.SootClass;
import soot.SootMethod;
import edu.usc.softarch.arcade.classgraphs.ClassGraphTransformer;
import edu.usc.softarch.arcade.clustering.ClusteringAlgorithmType;
import edu.usc.softarch.arcade.util.FileUtil;

//TODO This class is an abomination and must be destroyed.
/**
 * @author joshua
 */
public class Config {
	public enum StoppingCriterionConfig { preselected, clustergain }
	public enum Language { java, c }
	public enum SimMeasure { uem, uemnm, js, ilm, scm }
	public enum Granule { func, file, clazz	}
	
	private static Logger logger = LogManager.getLogger(Config.class);
	
	/* Project-specific configuration data */
	//TODO Refactor this out of EVERYTHING
	private static String projConfigFilename = 
		"cfg" + File.separator + "oodt-resource-0.2.cfg";
	private static String currProjName = "";
	public static Language selectedLanguage = Language.java;
	
	public static String DATADIR = "data" + File.separator + currProjName;
	private static String[] selectedPkgsArray;
	private static String odemFile = "";
	
	/* Clustering configuration data */
	//TODO Pretty sure this is also dead
	private static ClusteringAlgorithmType currentClusteringAlgorithm = 
		ClusteringAlgorithmType.WCA;
	private static SimMeasure currSimMeasure = SimMeasure.uem;
	public static SimMeasure getCurrSimMeasure() { return currSimMeasure; }
	public static void setCurrSimMeasure(SimMeasure currSimMeasure) {
		Config.currSimMeasure = currSimMeasure;	}

	public static StoppingCriterionConfig stoppingCriterion = 
		StoppingCriterionConfig.clustergain;
	private static int numClusters = 1;
	public static ClusteringAlgorithmType getCurrentClusteringAlgorithm() {
		return currentClusteringAlgorithm; }

	public static boolean ignoreDependencyFilters = false;
	
	/* Concern properties data */
	//TODO Bad bad bad bad
	private static String malletTopicKeysFilename = 
		"/home/joshua/Documents/Software Engineering Research/Subjects/"
		+ Config.getCurrProjStr() + "/" + Config.getCurrProjStr() + "-"
		+ Config.getNumTopics() + "-topic-keys.txt";
	
	private static String malletWordTopicCountsFilename = 
		"/home/joshua/Documents/Software Engineering Research/Subjects/"
		+ Config.getCurrProjStr() + "/"	+ Config.getCurrProjStr() + "-"
		+ Config.getNumTopics() + "-word-topic-counts.txt";
	
	private static String malletDocTopicsFilename =
		"/home/joshua/Documents/Software Engineering Research/Subjects/"
		+ Config.getCurrProjStr() + "/"	+ Config.getCurrProjStr() + "-"
		+ Config.getNumTopics() + "-doc-topics.txt";
	
	private static int numTopics = 10;
	private static List<Integer> numTopicsList = new ArrayList<>();

	public static List<Integer> getNumTopicsList() { return numTopicsList; }
	public static void setNumTopicsList(List<Integer> numTopicsList) {
		Config.numTopicsList = numTopicsList;	}

	public static int getNumTopics() { return numTopics; }
	public static void setNumTopics(int numTopics) {
		Config.numTopics = numTopics;	}
	
	public static String srcDir = ".";

	/* DriverEngine options */
	public static boolean useFastFeatureVectorsFile = false;
	private static String depsRsfFilename;
	private static String groundTruthFile;
	private static String smellClustersFile;
	public static void setSmellClustersFile(String smellClustersFile) {
		Config.smellClustersFile = smellClustersFile;	}

	private static int startNumClustersRange;
	private static int endNumClustersRange;
	private static int rangeNumClustersStep;
	private static boolean usingPreselectedRange = false;
	private static boolean usingNumTopicsRange = false;

	private static int startNumTopicsRange;
	public static int getStartNumTopicsRange() { return startNumTopicsRange; }
	public static int getEndNumTopicsRange() { return endNumTopicsRange; }
	public static int getRangeNumTopicsStep() {	return rangeNumTopicsStep; }

	private static int endNumTopicsRange;
	private static int rangeNumTopicsStep;
	private static String topicsDir;
	private static List<Integer> clustersToWriteList = null;
	private static Granule clusteringGranule = Granule.file;
	private static List<String> excludedEntities;
	private static String clusterStartsWith;
	public static String getClusterStartsWith() {	return clusterStartsWith;	}
	public static Granule getClusteringGranule() { return clusteringGranule; }
	public static boolean isUsingPreselectedRange() {
		return usingPreselectedRange; }
	public static boolean isUsingNumTopicsRange() { return usingNumTopicsRange; }
	public static int getStartNumClustersRange() { return startNumClustersRange; }
	public static int getEndNumClustersRange() { return endNumClustersRange; }
	public static int getRangeNumClustersStep() { return rangeNumClustersStep; }
	public static String getGroundTruthFile() { return groundTruthFile;	}
	public static String getSmellClustersFile() {	return smellClustersFile;	}
	public static String getOdemFile() { return odemFile;	}
	public static String getCurrProjStr() {	return currProjName; }
	public static int getNumClusters() { return numClusters; }
	public static void setNumClusters(int inNumClusters) {
		numClusters = inNumClusters; }

	private static void setConcernProperties(Properties prop) {
		malletTopicKeysFilename = prop.getProperty("topic_keys_file");
		malletWordTopicCountsFilename = prop.getProperty("word_topic_counts_file");
		malletDocTopicsFilename = prop.getProperty("doc_topics_file");
		
		if (malletTopicKeysFilename == null) {
			logger.error("topic_keys_file not set");
		}
		if (malletWordTopicCountsFilename == null) {
			logger.error("word_topics_file not set");
		}
		if (malletDocTopicsFilename == null) {
			logger.error("doc_topics_file");
		}
	}
	
	//TODO My eyes are bleeding.
	public static void initConfigFromFile(String filename) {
		Properties prop = new Properties();
		try {
			String propertyFileContents = readFileAsString(filename);
			prop.load(new StringReader(propertyFileContents.replace("\\", "\\\\")));

			String projName = prop.getProperty("project_name");
			logger.debug(projName);
			currProjName = projName;
			DATADIR = "data" + File.separator + currProjName;
			
			String numClustersStr = prop.getProperty("num_clusters");
			logger.debug(numClustersStr);
			if (numClustersStr != null) {
				numClusters = Integer.parseInt(numClustersStr);
			}
			
			String stopCriterionStr = prop.getProperty("stop_criterion");
			if (stopCriterionStr != null) {
				if (stopCriterionStr.equalsIgnoreCase("cluster_gain")) {
					stoppingCriterion = StoppingCriterionConfig.clustergain;
				} else if (stopCriterionStr.equalsIgnoreCase("preselected")) {
					stoppingCriterion = StoppingCriterionConfig.preselected;
				}
			}
			
			String clusteringAlgorithmStr = prop.getProperty("clustering_algorithm");
			logger.debug("cluster_algorithm: " + clusteringAlgorithmStr);
			if (clusteringAlgorithmStr != null) {
				if (clusteringAlgorithmStr.equals("wca")) {
					currentClusteringAlgorithm = ClusteringAlgorithmType.WCA;
				}
				else if (clusteringAlgorithmStr.equals("arc")) {
					currentClusteringAlgorithm = ClusteringAlgorithmType.ARC;
					setConcernProperties(prop);
					setCurrSimMeasure(SimMeasure.js);
				}
				else if (clusteringAlgorithmStr.equals("limbo")) {
					currentClusteringAlgorithm = ClusteringAlgorithmType.LIMBO;
					setCurrSimMeasure(SimMeasure.ilm);
				}
				else {
					currentClusteringAlgorithm = ClusteringAlgorithmType.WCA;
				}
			}
			else {
				currentClusteringAlgorithm = ClusteringAlgorithmType.WCA;
			}
			
			String lang = prop.getProperty("lang");
			if (lang.equals("java")) {
				selectedLanguage = Language.java;
			}
			else if (lang.equals("c")) {
				selectedLanguage = Language.c;
			}
			
			if (selectedLanguage.equals(Language.java)) {
				String selectedPkgsStr = prop.getProperty("selected_pkgs");
				if (selectedPkgsStr != null) {
					selectedPkgsArray = selectedPkgsStr.split(",");
				}
			}
			
			depsRsfFilename = prop.getProperty("deps_rsf_file");
			if (depsRsfFilename == null) {
				System.out.println("WARNING: deps_rsf_file not set properly in config file");
			}
			
			odemFile = prop.getProperty("odem_file");
			groundTruthFile = prop.getProperty("ground_truth_file");
			smellClustersFile = prop.getProperty("smell_clusters_file");
			String preselectedRange = prop.getProperty("preselected_range");
			if (preselectedRange != null) {
				String[] tokens = preselectedRange.split(",");
				if (tokens.length != 3) {
					String errMsg = "wrong number of tokens for preselected range: expected 3, got "
							+ tokens.length;
					logger.error(errMsg);
					System.err.println(errMsg);
					System.exit(1);
				}
				usingPreselectedRange = true;
				startNumClustersRange = Integer.parseInt(tokens[0]);
				endNumClustersRange = Integer.parseInt(tokens[1]);
				rangeNumClustersStep = Integer.parseInt(tokens[2]);

				logger.debug("start: " + startNumClustersRange + ", " + "range: "
						+ endNumClustersRange + ", " + "step: " + rangeNumClustersStep);
			}
			
			topicsDir = prop.getProperty("topics_dir");
			
			String numTopicsRange = prop.getProperty("numtopics_range");
			if (numTopicsRange != null) {
				String[] tokens = numTopicsRange.split(",");
				if (tokens.length != 3) {
					String errMsg = "wrong number of tokens for numtopics range: expected 3, got "
							+ tokens.length;
					logger.error(errMsg);
					System.err.println(errMsg);
					System.exit(1);
				}
				usingNumTopicsRange = true;
				startNumTopicsRange = Integer.parseInt(tokens[0]);
				endNumTopicsRange = Integer.parseInt(tokens[1]);
				rangeNumTopicsStep = Integer.parseInt(tokens[2]);
				
				logger.debug("start: " + startNumTopicsRange + ", " + "range: "
						+ endNumTopicsRange + ", " + "step: " + rangeNumTopicsStep);
			}

			String currSimMeasureStr = prop.getProperty("sim_measure");
			if (currSimMeasureStr != null) {
				if (currSimMeasureStr.trim().equals("uem")) {
					setCurrSimMeasure(SimMeasure.uem);
				} else if (currSimMeasureStr.trim().equals("uemnm")) {
					setCurrSimMeasure(SimMeasure.uemnm);
				} else if (currSimMeasureStr.trim().equals("js")) {
					setCurrSimMeasure(SimMeasure.js);
				} else if (currSimMeasureStr.trim().equals("ilm")) {
					setCurrSimMeasure(SimMeasure.ilm);
				} else if (currSimMeasureStr.trim().equals("scm")) {
					setCurrSimMeasure(SimMeasure.scm);
				} else {
					throw new IllegalArgumentException(currSimMeasureStr
							+ " is not a valid value for sim_measure");
				}
			}
			else {
				System.out.println("WARNING: No sim_measure property set");
			}

			String granuleStr = prop.getProperty("granule");
			if (granuleStr != null) {
				if (granuleStr.trim().equals("file")) {
					clusteringGranule = Granule.file;
				} else if (granuleStr.trim().equals("func")) {
					clusteringGranule = Granule.func;
				} else if (granuleStr.trim().equals("class")) {
					clusteringGranule = Granule.clazz;
				}

				excludedEntities = new ArrayList<>();
				String excludedEntitiesStr = prop.getProperty("excluded_entities");
				if (excludedEntitiesStr != null) {
					String[] excludedEntitiesArray = excludedEntitiesStr.split(",");
					for (String excludedEntity : excludedEntitiesArray)
						excludedEntities.add(excludedEntity.trim());
				}
			}
			else {
				System.out.println("WARNING: No granule property set");
			}
			
			malletDocTopicsFilename = prop.getProperty("doc_topics_file");
			clusterStartsWith = prop.getProperty("cluster_starts_with");
			if (clusterStartsWith == null) {
				clusterStartsWith = null;
			}
			else {
				clusterStartsWith = clusterStartsWith.trim();
			}
			
			if (prop.getProperty("ignore_dependency_filters") != null)  {
				if (prop.getProperty("ignore_dependency_filters").equals("true")) {
					ignoreDependencyFilters = true;
				}
				else if (prop.getProperty("ignore_dependency_filters").equals("false")) {
					ignoreDependencyFilters = false;
				}
			}
			
			if (prop.getProperty("src_dir") !=null) {
				srcDir = FileUtil.tildeExpandPath(prop.getProperty("src_dir"));
			}
			
			if (prop.getProperty("use_fast_feature_vectors_file") != null) {
				if (prop.getProperty("use_fast_feature_vectors_file").equals("true")) {
					useFastFeatureVectorsFile = true;
				}
				else if (prop.getProperty("use_fast_feature_vectors_file").equals("false")) {
					useFastFeatureVectorsFile = false;
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String readFileAsString(String filePath) 
			throws java.io.IOException{
		byte[] buffer = new byte[(int) new File(filePath).length()];
		BufferedInputStream f = null;
		try {
				f = new BufferedInputStream(new FileInputStream(filePath));
				f.read(buffer);
		} finally {
				if (f != null) try { f.close(); } catch (IOException ignored) { }
		}
		return new String(buffer);
	}

	public static boolean isMethodInSelectedPackages(SootMethod src) {
		char leading = '<';
		
		for (String pkg : selectedPkgsArray) {
			if (src.toString().startsWith(leading + pkg)) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isMethodInDeselectedPackages(SootMethod src) {
		return false;
	}

	//TODO I have a sneaking suspicion that this method is never actually used.
	public static void initProjectData(
			ClassGraphTransformer t) {
		t.LlamaChatTMD.setDocTopicsFilename("/home/joshua/Documents/Software Engineering Research/Subjects/LlamaChat/LlamaChat-doc-topics.txt");
		t.LlamaChatTMD.setTopicKeysFilename("/home/joshua/Documents/Software Engineering Research/Subjects/LlamaChat/LlamaChat-topic-keys.txt");

		t.freecsTMD.setDocTopicsFilename("/home/joshua/Documents/Software Engineering Research/Subjects/freecs/freecs-doc-topics.txt");
		t.freecsTMD.setTopicKeysFilename("/home/joshua/Documents/Software Engineering Research/Subjects/freecs/freecs-topic-keys.txt");

		String threeProjDocTopicsFilename = "/home/joshua/Documents/Software Engineering Research/Subjects/threeChatServerSystems/threeChatServerSystems-doc-topics.txt";
		String threeProjTopicKeysFilename = "/home/joshua/Documents/Software Engineering Research/Subjects/threeChatServerSystems/threeChatServerSystems-topic-keys.txt";

		t.currDocTopicsFilename = threeProjDocTopicsFilename;
		t.currTopicKeysFilename = threeProjTopicKeysFilename;
		t.datasetName = "oodt-filemgr";
	}
	
	public static boolean isClassInSelectedPackages(String clazz) {	
		if (selectedPkgsArray == null) return true;
		for (String pkg : selectedPkgsArray)
			if (clazz.trim().startsWith(pkg)) return true;
		return false;
	}

	public static boolean isClassInSelectedPackages(SootClass src) {		
		for (String pkg : selectedPkgsArray)
			if (src.toString().startsWith(pkg)) return true;
		return false;
	}
	
	public static String getXMLFeatureVectorMapFilename() {
		return DATADIR + File.separator + getCurrProjStr() + "_fvMap.xml"; }

	public static String getXLSDepsFilename() {
		return DATADIR + File.separator + getCurrProjStr() + "_deps.xls"; }
	
	public static String getXLSSimMeasureFilename() {
		return DATADIR + File.separator + getCurrProjStr() + "_sim.xls"; }
	
	public static String getXMLClassGraphFilename() {
		return DATADIR + File.separator + getCurrProjStr() + "_clg.xml"; }
	
	public static String getSerializedClustersFilename() {
		return DATADIR + File.separator + getCurrProjStr() +"_clusters.data"; }

	public static String getClusterGraphDotFilename() {
		return DATADIR + File.separator + getCurrProjStr() + "_cluster_graph.dot"; }

	public static String getMyCallGraphFilename() {
		return DATADIR + File.separator + getCurrProjStr() + "_mycallgraph.data"; }

	public static String getClassesWithUsedMethodsFilename() {
		return DATADIR + File.separator + Config.getCurrProjStr()
			+ "_classesWithUsedMethods.data";
	}

	public static String getClassesWithAllMethodsFilename() {
		return DATADIR + File.separator + Config.getCurrProjStr()
			+ "_classesWithAllMethods.data";
	}
	
	public static String getUnusedMethodsFilename() {
		return DATADIR + File.separator + Config.getCurrProjStr()
			+ "_unusedMethods.data";
	}

	public static String getXMLSmellArchGraphFilename() {
		return DATADIR + File.separator + Config.getCurrProjStr()
			+ "_smellArchGraph.xml";
	}

	public static String getXMLSmellArchFilename() {
		return DATADIR + File.separator + Config.getCurrProjStr()
			+ "_smellArch.xml";
	}

	public static String getSpecifiedSmallArchFromXML() {
		return DATADIR + File.separator + getCurrProjStr()
			+ "_smellArch_specified.xml";
	}

	public static String getMethodInfoFilename() {
		return DATADIR + File.separator + getCurrProjStr()
			+ "_methodInfo.xml";
	}
	
	public static String getNumbereNodeMappingTextFilename() {
		return DATADIR + File.separator + getCurrProjStr()
			+ "_numberedNodeMapping.txt";
	}

	public static String getClusterGraphXMLFilename() {
		return DATADIR + File.separator + getCurrProjStr()
			+ "_cluster_graph.xml";
	}
	
	public static String getClustersRSFFilename(int clustersSize) {
		if (Config.currentClusteringAlgorithm.equals(ClusteringAlgorithmType.ARC)) {
			return Config.getCurrProjFilenamePrefix() + "_"
				+ Config.currentClusteringAlgorithm.toString().toLowerCase() + "_"
				+ Config.stoppingCriterion.toString() + "_"
				+ Config.currSimMeasure.toString() + "_" + clustersSize + "_clusters_"
				+ Config.getNumTopics() + "topics.rsf";
		} else {
			return Config.getCurrProjFilenamePrefix() + "_"
				+ Config.currentClusteringAlgorithm.toString().toLowerCase() + "_"
				+ Config.stoppingCriterion.toString() + "_"
				+ Config.currSimMeasure.toString() + "_" + clustersSize
				+ "_clusters.rsf";
		}
	}
	
	public static String getCurrProjFilenamePrefix() {
		return DATADIR + File.separator + getCurrProjStr(); }

	public static String getClassGraphDotFilename() {
		return DATADIR + File.separator + getCurrProjStr() + "_class_graph.dot"; }

	public static void setProjConfigFilename(String projConfigFilename) {
		Config.projConfigFilename = projConfigFilename;	}
	
	public static String getProjConfigFilename() {
		return projConfigFilename; }

	public static String getXMLFunctionDepGraphFilename() {
		return DATADIR + File.separator + getCurrProjStr() + "_func_dep_graph.xml";
	}

	public static Language getSelectedLanguage() { return selectedLanguage; }

	public static void setSelectedLanguage(Language inLang) {
		selectedLanguage = inLang; }

	public static String getMalletTopicKeysFilename() {
		return malletTopicKeysFilename;	}
	
	public static String getMalletWordTopicCountsFilename() {
		return malletWordTopicCountsFilename;	}

	public static void setMalletDocTopicsFilename(String filename) {
		malletDocTopicsFilename = filename;	}
	
	public static String getVariableMalletDocTopicsFilename() {
		return Config.getTopicsDir() + File.separator + Config.getCurrProjStr()
			+ "-" + Config.getNumTopics() + "-doc-topics.txt";
	}

	private static String getTopicsDir() { return topicsDir;	}

	public static String getMalletDocTopicsFilename() {
		return malletDocTopicsFilename;	}

	public static String getNameToFeatureSetMapFilename() {
		return DATADIR + File.separator + getCurrProjStr()
			+ "_name_to_feature_set_map.data";
	}

	public static String getNamesInFeatureSetFilename() {
		return DATADIR + File.separator + getCurrProjStr()
			+ "_names_in_feature_set.data";
	}

	public static String getFastFeatureVectorsFilename() {
		return DATADIR + File.separator + getCurrProjStr()
			+ "_fast_feature_vectors.data";
	}

	public static String getDepsRsfFilename() {	return depsRsfFilename; }
	public static void setDepsRsfFilename(String filename) {
		depsRsfFilename = filename;	}
	
	public static String getInternalGraphDotFilename(String clusterName) {
		String cleanClusterName = clusterName.replaceAll("[\\/:*?\"<>|\\s]","_");
		return DATADIR + File.separator + "internal_clusters" + File.separator 
				+ getCurrProjStr() + "_" + cleanClusterName + "_internal_cluster_graph.dot";
	}
	
	public static String getGroundTruthRsfFilename() {
		return DATADIR + File.separator + getCurrProjStr() + "_ground_truth.rsf";
	}
	
	public static String getFullGroundTruthClusterGraphDotFilename() {
		return DATADIR + File.separator + getCurrProjStr() + "_full_ground_truth_cluster_graph.dot";
	}

	public static String getNonPkgBasedGroundTruthClusterGraphDotFilename() {
		return DATADIR + File.separator + getCurrProjStr() + "_non_pkg_based_ground_truth_cluster_graph.dot";
	}
	
	public static String getPkgBasedGroundTruthClusterGraphDotFilename() {
		return DATADIR + File.separator + getCurrProjStr() + "_pkg_based_ground_truth_cluster_graph.dot";
	}
	
	public static String getMojoToAuthCSVFilename(List<Integer> numClustersList, String selectedAlg, String simMeasure) {
		return Config.getCurrProjFilenamePrefix() + numClustersList.get(0) + "-" + numClustersList.get(numClustersList.size()-1) + "_" + selectedAlg  + "_" + simMeasure + "_clusters_mojo.csv";
	}

	public static void setClustersToWriteList(List<Integer> inClustersToWriteList) {
		clustersToWriteList  = inClustersToWriteList;
	}

	public static List<Integer> getClustersToWriteList() {
		return clustersToWriteList;
	}

	public static String getFilteredRoutineFactsFilename() {
		return Config.getCurrProjFilenamePrefix() + "_filteredRoutineFacts.rsf";
	}

	public static String getFilteredFactsFilename() {
		return Config.getCurrProjFilenamePrefix() + "_filteredFacts.rsf";
	}
	
	public static String getClassGraphRsfFilename() {
		return Config.getCurrProjFilenamePrefix() + "_class_graph_facts.rsf";
	}

	public static List<String> getExcludedEntities() {
		return excludedEntities;
	}
}