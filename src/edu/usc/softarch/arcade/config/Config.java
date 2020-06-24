package edu.usc.softarch.arcade.config;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.kohsuke.pdc.ClasspathBuilder;
import org.kohsuke.pdc.ParseDotClasspath;
import org.xml.sax.SAXException;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import edu.usc.softarch.arcade.classgraphs.ClassGraphTransformer;
import edu.usc.softarch.arcade.clustering.ClusteringAlgorithmType;
import edu.usc.softarch.arcade.clustering.FastCluster;
import edu.usc.softarch.arcade.config.Config.Granule;
import edu.usc.softarch.arcade.config.datatypes.Proj;
import edu.usc.softarch.arcade.config.datatypes.RunType;
import edu.usc.softarch.arcade.topics.DocTopics;
import edu.usc.softarch.arcade.topics.TopicModelExtractionMethod;
import edu.usc.softarch.arcade.util.FileUtil;
import edu.usc.softarch.arcade.weka.ClassValueMap;

/**
 * @author joshua
 *
 */
public class Config {
	
	public enum StoppingCriterionConfig  {
		preselected, clustergain
	}
	
	public enum Language {
		java, c
	}
	

	public enum SimMeasure {
		uem, uemnm, js, ilm, scm
	}
	

	public enum Granule {
		func, file, clazz
	}
	

	private static Logger logger = Logger.getLogger(Config.class);
	
	/* Project-specific configuration data */
	private static String projConfigFilename = "cfg" + File.separator + "oodt-resource-0.2.cfg";
	private static String currProjName = "";
	public static String homeLoc = File.separator + "home" + File.separator + "joshua" + File.separator;
	public static Language selectedLanguage = Language.java;
	
	public static String workspaceLoc = homeLoc + "workspace" + File.separator;
	private static String loggingConfigFilename = "cfg" + File.separator + "extractor_logging.cfg";
	public static String DATADIR = "data" + File.separator + currProjName;
	private static String projSrcDir = "";
	private static String[] selectedPkgsArray;
	private static String[] sootClasspathArray;
	private static String sootClasspathStr;
	private static String[] deselectedPkgsArray;
	private static String odemFile = "";
	
	/* Clustering configuration data */
	private static ClusteringAlgorithmType currentClusteringAlgorithm = ClusteringAlgorithmType.WCA;
	private static SimMeasure currSimMeasure = SimMeasure.uem;
	public static SimMeasure getCurrSimMeasure() {
		return currSimMeasure;
	}

	public static void setCurrSimMeasure(SimMeasure currSimMeasure) {
		Config.currSimMeasure = currSimMeasure;
	}

	public static StoppingCriterionConfig stoppingCriterion = StoppingCriterionConfig.clustergain;
	private static int numClusters = 1;
	private static String stopWordsFilename = "cfg" + File.separator + "stopwords.txt";
	public static boolean isExcelFileWritingEnabled = false;
	public static ClusteringAlgorithmType getCurrentClusteringAlgorithm() {
		return currentClusteringAlgorithm;
	}

	public static void setCurrentClusteringAlgorithm(
			ClusteringAlgorithmType currentClusteringAlgorithm) {
		Config.currentClusteringAlgorithm = currentClusteringAlgorithm;
	}

	public static boolean runMojo = false;
	public static boolean usingFvMap = true;
	public static boolean ignoreDependencyFilters = false;
	
	/* Concern properties data */
	private static String malletTopicKeysFilename = "/home/joshua/Documents/Software Engineering Research/Subjects/"
			+ Config.getCurrProjStr() + "/"
			+ Config.getCurrProjStr() + "-" + Config.getNumTopics() + "-topic-keys.txt";
	
	private static String malletWordTopicCountsFilename = "/home/joshua/Documents/Software Engineering Research/Subjects/"
				+ Config.getCurrProjStr() + "/"
				+ Config.getCurrProjStr() + "-" + Config.getNumTopics() + "-word-topic-counts.txt";
	
	private static String malletDocTopicsFilename = "/home/joshua/Documents/Software Engineering Research/Subjects/"
			+ Config.getCurrProjStr() + "/"
			+ Config.getCurrProjStr() + "-" + Config.getNumTopics() + "-doc-topics.txt";
	
	private static int numTopics = 10;
	private static List<Integer> numTopicsList = new ArrayList<Integer>();

	public static List<Integer> getNumTopicsList() {
		return numTopicsList;
	}

	public static void setNumTopicsList(List<Integer> numTopicsList) {
		Config.numTopicsList = numTopicsList;
	}

	public static int getNumTopics() {
		return numTopics;
	}

	public static void setNumTopics(int numTopics) {
		Config.numTopics = numTopics;
	}
	
	public static TopicModelExtractionMethod tmeMethod = TopicModelExtractionMethod.VAR_MALLET_FILE;
	public static String srcDir = ".";
	
	private static String TME_METHOD = "topic_model_extraction_method";

	/* DriverEngine options */
	public static RunType runType = RunType.whole;
	public static boolean useSerializedClassGraph = true;
	public static boolean useXMLClassGraph = true;
	public static boolean forceClustering = true;
	public static boolean enableClustering = true;
	public static boolean enablePostClusteringTasks = false;
	public static boolean forceClassAndMyMethodGraphsConstruction = false;
	public static boolean performPRCalculation = true; // setting this option to true prevents other option phases from running
	public static boolean useFastFeatureVectorsFile = false;
	private static String USE_FAST_FEATURE_VECTORS_FILE = "use_fast_feature_vectors_file";
	
	/* Config data to be REMOVED in the future */
	public static Proj proj = Proj.OODT_Filemgr;
	public static final String lucene1_9FinalStr = "lucene-1.9-final";
	public static final String llamaChatStr = "LlamaChat";
	public static final String freecsStr = "freecs";
	public static final String gujChatStr = "gujChat";
	public static final String lcdClockStr = "LCDClock";
	public static final String jeditStr = "jedit";
	public static final String oodtFilemgrStr = "oodt-filemgr";
	public static final String klaxStr = "KLAX";
	public static final String jigsawStr = "jigsaw2.2.6";
	public static boolean performingTwoProjectTest = false;
	public static boolean performingThreeProjectTest = true;

	private static String depsRsfFilename;

	private static String groundTruthFile;
	private static String smellClustersFile;

	public static void setSmellClustersFile(String smellClustersFile) {
		Config.smellClustersFile = smellClustersFile;
	}

	private static String mojoTargetFile;

	

	private static int startNumClustersRange;

	private static int endNumClustersRange;

	private static int rangeNumClustersStep;

	private static boolean usingPreselectedRange = false;
	private static boolean usingNumTopicsRange = false;

	private static int startNumTopicsRange;

	public static int getStartNumTopicsRange() {
		return startNumTopicsRange;
	}

	public static int getEndNumTopicsRange() {
		return endNumTopicsRange;
	}

	public static int getRangeNumTopicsStep() {
		return rangeNumTopicsStep;
	}

	private static int endNumTopicsRange;

	private static int rangeNumTopicsStep;

	private static String topicsDir;

	private static String expertDecompositionFile;

	private static String concernRecoveryFilePrefix;

	private static List<Integer> clustersToWriteList = null;

	private static Granule clusteringGranule = Granule.file;

	private static List<String> excludedEntities;

	private static String clusterStartsWith;
	
	public static String getClusterStartsWith() {
		return clusterStartsWith;
	}
	
	public static Granule getClusteringGranule() {
		return clusteringGranule;
	}

	public static void setClusteringGranule(Granule clusteringGranule) {
		Config.clusteringGranule = clusteringGranule;
	}

	public static boolean isUsingPreselectedRange()  {
		return usingPreselectedRange;
	}
	
	public static boolean isUsingNumTopicsRange()  {
		return usingNumTopicsRange;
	}
	
	public static int getStartNumClustersRange() {
		return startNumClustersRange;
	}
	public static int getEndNumClustersRange() {
		return endNumClustersRange;
	}
	public static int getRangeNumClustersStep() {
		return rangeNumClustersStep;
	}
	
	public static void initConfigFromFile(String filename) {
		Properties prop = new Properties();
		try {
			//FileInputStream stream = new FileInputStream(filename);
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
			
			/*if (selectedLanguage.equals(Language.java)) {
				setJavaConfigFromFile(prop);
			}
			else if (selectedLanguage.equals(Language.c)) {
				currProjRsfFilename = prop.getProperty("deps_rsf_file");
				if (currProjRsfFilename == null) {
					System.err.println("projects_rsf_loc not set properly in config file");
				}
			}*/
			depsRsfFilename = prop.getProperty("deps_rsf_file");
			if (depsRsfFilename == null) {
				System.out.println("WARNING: deps_rsf_file not set properly in config file");
			}
			
			odemFile = prop.getProperty("odem_file");
			groundTruthFile = prop.getProperty("ground_truth_file");
			smellClustersFile = prop.getProperty("smell_clusters_file");
			mojoTargetFile = prop.getProperty("mojo_target_file");
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
			
			expertDecompositionFile = prop.getProperty("expert_decomposition_file");
			concernRecoveryFilePrefix = prop.getProperty("concern_recovery_file_prefix");

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

				excludedEntities = new ArrayList<String>();
				String excludedEntitiesStr = prop
						.getProperty("excluded_entities");
				if (excludedEntitiesStr != null) {
					String[] excludedEntitiesArray = excludedEntitiesStr
							.split(",");
					for (String excludedEntity : excludedEntitiesArray) {
						excludedEntities.add(excludedEntity.trim());
					}
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
			
			if (prop.getProperty(TME_METHOD ) != null) {
				if (prop.getProperty(TME_METHOD).equals("var_mallet_file")) {
					tmeMethod = TopicModelExtractionMethod.VAR_MALLET_FILE;
				}
				else if (prop.getProperty(TME_METHOD).equals("mallet_api")) {
					tmeMethod = TopicModelExtractionMethod.MALLET_API;
				}
				else {
					tmeMethod = TopicModelExtractionMethod.VAR_MALLET_FILE;
				}
			}
			
			if (prop.getProperty("src_dir") !=null) {
				srcDir = FileUtil.tildeExpandPath(prop.getProperty("src_dir"));
			}
			
			if (prop.getProperty(USE_FAST_FEATURE_VECTORS_FILE) != null) {
				if (prop.getProperty(USE_FAST_FEATURE_VECTORS_FILE).equals("true")) {
					useFastFeatureVectorsFile = true;
				}
				else if (prop.getProperty(USE_FAST_FEATURE_VECTORS_FILE).equals("false")) {
					useFastFeatureVectorsFile = false;
				}
				
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}


	public static String getGroundTruthFile() {
		return groundTruthFile;
	}

	public static String getSmellClustersFile() {
		return smellClustersFile;
	}

	public static void setGroundTruthFile(String groundTruthFile) {
		Config.groundTruthFile = groundTruthFile;
	}


	public static String getOdemFile() {
		return odemFile;
	}


	public static void setOdemFile(String odemFile) {
		Config.odemFile = odemFile;
	}


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


	private static String readFileAsString(String filePath) throws java.io.IOException{
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

	private static void setJavaConfigFromFile(Properties prop)
			throws IOException {
		String selectedPkgsStr = prop.getProperty("selected_pkgs");
		String deselectedPkgsStr = prop.getProperty("deselected_pkgs");
		String sootClassPathJarDirStr = prop
				.getProperty("sootclasspath_jardir");
		String eclipseDotClassPathStr = prop
				.getProperty("eclipse_dot_classpath");

		String[] jarDirsArray;
		sootClasspathStr = prop.getProperty("sootclasspath");
		projSrcDir = prop.getProperty("src_dir");
		projSrcDir.replace("\\","\\\\");
		
		logger.debug("projSrcDir: " + projSrcDir);

		ArrayList<File> extraJars = new ArrayList<File>();

		if (selectedPkgsStr != null) {
			selectedPkgsArray = selectedPkgsStr.split(",");
		}
		if (deselectedPkgsStr != null) {
			deselectedPkgsArray = deselectedPkgsStr.split(",");
		}
		logger.debug("sootClasspathStr: " + sootClasspathStr);
		sootClasspathArray = sootClasspathStr.split(File.pathSeparator);

		if (sootClassPathJarDirStr != null) {
			logger.debug("Printing jars in " + sootClassPathJarDirStr);
			jarDirsArray = sootClassPathJarDirStr.split(":");
			for (String jarDirStr : jarDirsArray) {
				logger.debug("jar directory: " + jarDirStr);
				File jarDir = new File(jarDirStr);
				findExtraJars(extraJars, jarDir);
			}
		}

		ClasspathBuilder builder = new ClasspathBuilder();
		if (eclipseDotClassPathStr != null) {
			File classpathFile = new File(eclipseDotClassPathStr);
			logger.debug("Eclipse dot classpath location: "
					+ eclipseDotClassPathStr);
			try {
				ParseDotClasspath.parseDotClasspath(classpathFile,
						builder);
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		logger.debug(projSrcDir);

		logger.debug(selectedPkgsStr);
		logger.debug(Arrays.toString(selectedPkgsArray));

		logger.debug(deselectedPkgsStr);
		logger.debug(Arrays.toString(deselectedPkgsArray));

		logger.debug(sootClasspathStr);
		logger.debug(Arrays.toString(sootClasspathArray));

		logger.debug("Updating soot classpath by adding extraJars");
		for (File jarFile : extraJars) {
			sootClasspathStr += File.pathSeparator + jarFile.getAbsoluteFile();
		}
		
		sootClasspathStr.replaceAll(File.pathSeparator + File.pathSeparator, File.pathSeparator);

		logger.debug("soot classpath with extra jars: "
				+ sootClasspathStr);

		logger.debug("parsed dot classpath: " + builder.getResult());

		sootClasspathStr += File.pathSeparator + builder.getResult();

		logger.debug("soot classpath with eclipse dot classpath results: "
				+ sootClasspathStr);
		logger.debug("\n");
	}



	private static void findExtraJars(ArrayList<File> extraJars, File jarDir) {
		
		if (jarDir != null) {
			if (jarDir.isDirectory()) {
				for (File jarFile : jarDir.listFiles(new JarFileFilter())) {
					//logger.debug(jarFile);
					if (jarFile.isDirectory()) {
						logger.debug("Going into directory " + jarFile + "...");
						findExtraJars(extraJars, jarFile);
					}
					else {
						extraJars.add(jarFile);
					}
				}
			}
		}
	}
	
	public static String getCurrProjStr() {
		return currProjName;
	}
	
	public static int getNumClusters() {
		return numClusters;
	}
	
	public static void setNumClusters(int inNumClusters) {
		numClusters = inNumClusters;
	}

	public static HashMap<String, String> getCurrProjMap() {
		ClassValueMap.init();
		if (proj.equals(Proj.LlamaChat)) {
			return ClassValueMap.LlamaChatMap;
		} else if (proj.equals(Proj.FreeCS)) {
			return ClassValueMap.freecsMap;
		} else if (proj.equals(Proj.GujChat)) {
			return null;
		} else {
			return null;
		}
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
		String leading = "<";

		if (deselectedPkgsArray != null) {
			for (String pkg : deselectedPkgsArray) {
				if (src.toString().startsWith(leading + pkg))
					return true;
			}
			return false;
		}
		return false;

	}

	public static String getProjSrcDir() {
		return projSrcDir;
	}

	public static void initProjectData(
			ClassGraphTransformer t) {
		t.LlamaChatTMD.docTopicsFilename = "/home/joshua/Documents/Software Engineering Research/Subjects/LlamaChat/LlamaChat-doc-topics.txt";
		t.LlamaChatTMD.topicKeysFilename = "/home/joshua/Documents/Software Engineering Research/Subjects/LlamaChat/LlamaChat-topic-keys.txt";

		t.freecsTMD.docTopicsFilename = "/home/joshua/Documents/Software Engineering Research/Subjects/freecs/freecs-doc-topics.txt";
		t.freecsTMD.topicKeysFilename = "/home/joshua/Documents/Software Engineering Research/Subjects/freecs/freecs-topic-keys.txt";

		String twoProjDocTopicsFilename = "/home/joshua/Documents/Software Engineering Research/Subjects/LlamaChat_freecs/Llamachat_freecs-doc-topics.txt";
		String twoProjTopicKeysFilename = "/home/joshua/Documents/Software Engineering Research/Subjects/LlamaChat_freecs/Llamachat_freecs-topic-keys.txt";

		String threeProjDocTopicsFilename = "/home/joshua/Documents/Software Engineering Research/Subjects/threeChatServerSystems/threeChatServerSystems-doc-topics.txt";
		String threeProjTopicKeysFilename = "/home/joshua/Documents/Software Engineering Research/Subjects/threeChatServerSystems/threeChatServerSystems-topic-keys.txt";

		if (Config.performingThreeProjectTest) {
			t.currDocTopicsFilename = threeProjDocTopicsFilename;
			t.currTopicKeysFilename = threeProjTopicKeysFilename;
		} else if (Config.performingTwoProjectTest) {
			t.currDocTopicsFilename = twoProjDocTopicsFilename;
			t.currTopicKeysFilename = twoProjTopicKeysFilename;
		} else if (Config.proj.equals(Proj.FreeCS)) {
			t.currDocTopicsFilename = t.freecsTMD.docTopicsFilename;
			t.currTopicKeysFilename = t.freecsTMD.topicKeysFilename;
		} else if (Config.proj.equals(Proj.LlamaChat)) {
			t.currDocTopicsFilename = t.LlamaChatTMD.docTopicsFilename;
			t.currTopicKeysFilename =t. LlamaChatTMD.topicKeysFilename;
		} else {
			System.err
					.println("Couldn't identiy the doc-topics and topic-keys files");
			System.exit(1);
		}

		if (Config.proj.equals(Proj.GujChat))
			t.datasetName = Config.gujChatStr;
		else if (Config.proj.equals(Proj.LlamaChat))
			t.datasetName = Config.llamaChatStr;
		else if (Config.proj.equals(Proj.FreeCS))
			t.datasetName = Config.freecsStr;
		else if (Config.proj.equals(Proj.LCDClock))
			t.datasetName = Config.lcdClockStr;
		else if (Config.proj.equals(Proj.JEdit))
			t.datasetName = Config.jeditStr;
		else if (Config.proj.equals(Proj.Lucene1_9Final))
			t.datasetName = Config.lucene1_9FinalStr;
		else if (Config.proj.equals(Proj.OODT_Filemgr))
			t.datasetName = Config.oodtFilemgrStr;
		else if (Config.proj.equals(Proj.KLAX))
			t.datasetName = Config.klaxStr;
		else if (Config.proj.equals(Proj.Jigsaw))
			t.datasetName = Config.jigsawStr;
		else {
			System.err
					.println("Could not identify project string, so couldn't save to arff file");
			System.exit(1);
		}

	}
	
	public static boolean isClassInSelectedPackages(String clazz) {	
		if (selectedPkgsArray == null) {
			//System.out.println("Selected packages are not set so accepted any package");
			return true;
		}
		for (String pkg : selectedPkgsArray) {
			if (clazz.trim().startsWith(pkg)) {
				return true;
			}
		}
		
		return false;
	}

	public static boolean isClassInSelectedPackages(SootClass src) {		
		for (String pkg : selectedPkgsArray) {
			if (src.toString().startsWith(pkg)) {
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean isClassInDeselectedPackages(SootClass src) {
		
		if (deselectedPkgsArray != null) {
			for (String pkg : deselectedPkgsArray) {
				if (src.toString().startsWith(pkg))
						return true;
			}
			return false;
		}
		return false;

	}
	
	public static void setupSootClassPath() {	
		logger.debug("original SOOT_CLASSPATH: " + Scene.v().getSootClassPath());
		
		Scene.v().setSootClassPath(sootClasspathStr);
		
		logger.debug("with correct classes.jar - SOOT_CLASSPATH: " + Scene.v().getSootClassPath());
		
		
	}
	
	public static String getXMLFeatureVectorMapFilename() {
		return DATADIR + File.separator + getCurrProjStr() + "_fvMap.xml";
	}

	public static String getXLSDepsFilename() {
		return DATADIR + File.separator + getCurrProjStr() + "_deps.xls";
	}
	
	public static String getXLSSimMeasureFilename() {
		return DATADIR + File.separator + getCurrProjStr() + "_sim.xls";
	}
	
	public static String getXMLClassGraphFilename() {
		return DATADIR + File.separator + getCurrProjStr() + "_clg.xml";
	}

	public static String getSerializedClassGraphFilename() {
		return DATADIR + File.separator + getCurrProjStr() +"_clg.data";
	}
	
	public static String getSerializedClustersFilename() {
		return DATADIR + File.separator + getCurrProjStr() +"_clusters.data";
	}

	public static String getClusterGraphDotFilename() {
		return DATADIR + File.separator + getCurrProjStr() + "_cluster_graph.dot";
	}

	public static String getMyCallGraphFilename() {
		return DATADIR + File.separator + getCurrProjStr() + "_mycallgraph.data";
	}

	public static String getClassGraphFilename() {
		return DATADIR + File.separator + Config.getCurrProjStr() + ".data";
	}

	public static String getClassesWithUsedMethodsFilename() {
		return DATADIR + File.separator + Config.getCurrProjStr() + "_classesWithUsedMethods.data";
	}

	public static String getClassesWithAllMethodsFilename() {
		return DATADIR + File.separator + Config.getCurrProjStr() + "_classesWithAllMethods.data";
	}
	
	public static String getUnusedMethodsFilename() {
		return DATADIR + File.separator + Config.getCurrProjStr() + "_unusedMethods.data";
	}

	public static String getXMLSmellArchGraphFilename() {
		return DATADIR + File.separator + Config.getCurrProjStr() + "_smellArchGraph.xml";
	}

	public static String getXMLSmellArchFilename() {
		return DATADIR + File.separator + Config.getCurrProjStr() + "_smellArch.xml";
	}

	public static String getSpecifiedSmallArchFromXML() {
		return DATADIR + File.separator + getCurrProjStr() + "_smellArch_specified.xml";
	}

	public static String getMethodInfoFilename() {
		return DATADIR + File.separator + getCurrProjStr() + "_methodInfo.xml";
	}
	
	public static String getNumbereNodeMappingTextFilename() {
		return DATADIR + File.separator + getCurrProjStr() + "_numberedNodeMapping.txt";
	}

	public static String getClusterGraphXMLFilename() {
		return DATADIR + File.separator + getCurrProjStr() + "_cluster_graph.xml";
	}

	public static String getTopicsFilename() {
		return Config.getCurrProjFilenamePrefix() + "_" + Config.getNumTopics() + "_topics.mallet";
	}
	
	public static String getDocTopicsFilename() {
		return Config.getCurrProjFilenamePrefix() + "_" + Config.getNumTopics() + "_doc_topics.txt";
	}
	
	public static String getTopWordsFilename() {
		return Config.getCurrProjFilenamePrefix() + "_" + Config.getNumTopics() + "_top_words_per_topic.txt";
	}
	
	public static String getClustersRSFFilename(int clustersSize) {
		if (Config.currentClusteringAlgorithm.equals(ClusteringAlgorithmType.ARC)) {
			return Config.getCurrProjFilenamePrefix() + "_" + Config.currentClusteringAlgorithm.toString().toLowerCase() + "_" + Config.stoppingCriterion.toString() + "_" + Config.currSimMeasure.toString() + "_" + clustersSize + "_clusters_" + Config.getNumTopics() + "topics.rsf";
		}
		else {
			return Config.getCurrProjFilenamePrefix() + "_" + Config.currentClusteringAlgorithm.toString().toLowerCase() + "_" + Config.stoppingCriterion.toString() + "_" + Config.currSimMeasure.toString() + "_" + clustersSize + "_clusters.rsf";
		}
	}
	
	public static String getDetailedClustersRsfFilename() {
		return Config.getCurrProjFilenamePrefix() + Config.stoppingCriterion.toString() + "_" + Config.currSimMeasure.toString() + "_" + Config.getNumClusters() + "_clusters.rsf";
	}
	
	public static String getCurrProjFilenamePrefix() {
		return DATADIR + File.separator + getCurrProjStr();
	}


	public static String getClassGraphDotFilename() {
		return DATADIR + File.separator + getCurrProjStr() + "_class_graph.dot";
	}


	public static String getLoggingConfigFilename() {
		return loggingConfigFilename;
	}


	public static void setProjConfigFilename(String projConfigFilename) {
		Config.projConfigFilename = projConfigFilename;
	}
	
	public static String getProjConfigFilename() {
		return projConfigFilename;
	}

	public static String getStopWordsFilename() {
		return stopWordsFilename ;
	}
	
	public static String getMojoTargetFile() {
		return mojoTargetFile;
	}



	public static String getXMLFunctionDepGraphFilename() {
		return DATADIR + File.separator + getCurrProjStr() + "_func_dep_graph.xml";
	}



	public static Language getSelectedLanguage() {
		return selectedLanguage;
	}



	public static void setSelectedLanguage(Language inLang) {
		selectedLanguage = inLang;
	}
	
	public void setMalletTopicKeysFilename(String filename) {
		this.malletTopicKeysFilename = filename;
	}

	public static String getMalletTopicKeysFilename() {
		return malletTopicKeysFilename;
	}


	public void setMalletWordTopicCountsFilename(String filename ) {
		this.malletWordTopicCountsFilename = filename;
	}
	
	public static String getMalletWordTopicCountsFilename() {
		return malletWordTopicCountsFilename;
	}

	public static void setMalletDocTopicsFilename(String filename) {
		malletDocTopicsFilename = filename;
	}
	
	public static String getVariableMalletDocTopicsFilename() {
		return Config.getTopicsDir() + File.separator + Config.getCurrProjStr() + "-" + Config.getNumTopics() + "-doc-topics.txt";
	}

	private static String getTopicsDir() {
		return topicsDir;
	}

	public static String getMalletDocTopicsFilename() {
		return malletDocTopicsFilename;
	}

	public static String getNameToFeatureSetMapFilename() {
		return DATADIR + File.separator + getCurrProjStr() + "_name_to_feature_set_map.data";
	}


	public static String getNamesInFeatureSetFilename() {
		return DATADIR + File.separator + getCurrProjStr() + "_names_in_feature_set.data";
	}


	public static String getFastFeatureVectorsFilename() {
		return DATADIR + File.separator + getCurrProjStr() + "_fast_feature_vectors.data";

	}


	public static String getDepsRsfFilename() {
		return depsRsfFilename ;
	}
	
	public static void setDepsRsfFilename(String filename) {
		depsRsfFilename = filename;
	}
	
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

	public static String getTopicsPrecisionRecallCSVFilename(int numClusters) {
		return Config.getCurrProjFilenamePrefix() + numClusters + "_clusters_topics_pr.csv";
	}

	public static String getExpertDecompositionFile() {
		return expertDecompositionFile;
	}

	public static String getConcernRecoveryFilePrefix() {
		return concernRecoveryFilePrefix;
	}

	public static String getMojoWithTopicsCSVFilename(int numClusters) {
		return Config.getCurrProjFilenamePrefix() + numClusters + "_clusters_and_topics_mojo.csv";
	}
	
	public static String getMojoToAuthCSVFilename(List<Integer> numClustersList, String selectedAlg, String simMeasure) {
		return Config.getCurrProjFilenamePrefix() + numClustersList.get(0) + "-" + numClustersList.get(numClustersList.size()-1) + "_" + selectedAlg  + "_" + simMeasure + "_clusters_mojo.csv";
	}
	
	public static String getMojoToNextCSVFilename(List<Integer> numClustersList, String selectedAlg, String simMeasure) {
		return Config.getCurrProjFilenamePrefix() + numClustersList.get(0) + "-" + numClustersList.get(numClustersList.size()-1) + "_" + selectedAlg  + "_" + simMeasure + "_clusters_mojo_next.csv";
	}

	public static String getPrecisionRecallCSVFilename(
			List<Integer> numClustersList) {
		return Config.getCurrProjFilenamePrefix() + numClustersList.get(0) + "-" + numClustersList.get(numClustersList.size()-1) + "_clusters_pr.csv";
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
