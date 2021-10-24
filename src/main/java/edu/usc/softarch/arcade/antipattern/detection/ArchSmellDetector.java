package edu.usc.softarch.arcade.antipattern.detection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.alg.connectivity.KosarajuStrongConnectivityInspector;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import edu.usc.softarch.arcade.antipattern.Smell;
import edu.usc.softarch.arcade.antipattern.SmellCollection;
import edu.usc.softarch.arcade.clustering.ClusterUtil;
import edu.usc.softarch.arcade.clustering.ConcernClusterArchitecture;
import edu.usc.softarch.arcade.clustering.StringGraph;
import edu.usc.softarch.arcade.facts.ConcernCluster;
import edu.usc.softarch.arcade.topics.DocTopicItem;
import edu.usc.softarch.arcade.topics.DocTopics;
import edu.usc.softarch.arcade.topics.TopicItem;
import edu.usc.softarch.arcade.topics.TopicModelExtractionMethod;
import edu.usc.softarch.arcade.topics.TopicUtil;
import edu.usc.softarch.arcade.topics.UnmatchingDocTopicItemsException;

public class ArchSmellDetector {
	// #region ATTRIBUTES --------------------------------------------------------
	private static Logger logger = LogManager.getLogger(ArchSmellDetector.class);
	private String depsRsfFilename;
	private String clustersRsfFilename;
	private String detectedSmellsFilename;
	private String language;
	private TopicModelExtractionMethod tmeMethod;
	private DocTopics docTopics;
	private double scatteredConcernThreshold;
	private double parasiticConcernThreshold;
	private String version;

	static final Comparator<TopicItem> TOPIC_PROPORTION_ORDER 
		= (TopicItem t1, TopicItem t2) -> {
			Double prop1 = t1.getProportion();
			Double prop2 = t2.getProportion();
			return prop1.compareTo(prop2);
		};
	// #endregion ATTRIBUTES -----------------------------------------------------

	// #region CONSTRUCTORS ------------------------------------------------------
	public ArchSmellDetector(String depsRsfFilename, String clustersRsfFilename,
			String detectedSmellsFilename) {
		this(depsRsfFilename, clustersRsfFilename, detectedSmellsFilename, 
			"structural-irrelevant", .20, .20, null, null);
	}

	public ArchSmellDetector(String depsRsfFilename, String clustersRsfFilename, String detectedSmellsFilename, String version) {
		this(depsRsfFilename, clustersRsfFilename, detectedSmellsFilename, "structural-irrelevant", .20, .20, null,
				null);
		this.version = version;
	}

	public ArchSmellDetector(String depsRsfFilename, String clustersRsfFilename,
			String detectedSmellsFilename, double scatteredConcernThreshold,
			double parasiticConcernThreshold) {
		this(depsRsfFilename, clustersRsfFilename, detectedSmellsFilename, 
			"structural-irrelevant", scatteredConcernThreshold,
			parasiticConcernThreshold, null, null);
	}

	public ArchSmellDetector(String depsRsfFilename, String clustersRsfFilename,
			String detectedSmellsFilename, String language,
			TopicModelExtractionMethod tmeMethod,	DocTopics docTopics) {
		this(depsRsfFilename, clustersRsfFilename, detectedSmellsFilename,
			language, .20, .20, tmeMethod, docTopics);
	}

	public ArchSmellDetector(String depsRsfFilename, String clustersRsfFilename,
	String detectedSmellsFilename, String language,
	TopicModelExtractionMethod tmeMethod,	DocTopics docTopics, String version) {
		this(depsRsfFilename, clustersRsfFilename, detectedSmellsFilename,
		language, .20, .20, tmeMethod, docTopics);
		this.version = version;
	}

	public ArchSmellDetector(String depsRsfFilename, String clustersRsfFilename,
			String detectedSmellsFilename, String language,
			double scatteredConcernThreshold, double parasiticConcernThreshold,
			TopicModelExtractionMethod tmeMethod, DocTopics docTopics) {
		this.depsRsfFilename = depsRsfFilename;
		this.clustersRsfFilename = clustersRsfFilename;
		this.detectedSmellsFilename = detectedSmellsFilename;
		this.language = language;
		this.scatteredConcernThreshold = scatteredConcernThreshold;
		this.parasiticConcernThreshold = parasiticConcernThreshold;
		this.tmeMethod = tmeMethod;
		this.docTopics = docTopics;
	}
	// #endregion CONSTRUCTORS ---------------------------------------------------
	
	// #region PUBLIC INTERFACE --------------------------------------------------
	public SmellCollection run(boolean runStructural, boolean runConcern, 
			boolean runSerialize) throws IOException {
		// Make sure at least one type of smell detection algorithms was selected
		if(!runConcern && !runStructural)
			throw new IllegalArgumentException(); //TODO sort this out properly

		// Initialize variables
		SmellCollection detectedSmells = new SmellCollection();
		ConcernClusterArchitecture clusters = loadClusters();
		Map<String,Set<String>> clusterSmellMap = new HashMap<>();


		// Execute detection algorithms
		if(runConcern)
			runConcernDetectionAlgs(clusters, detectedSmells, clusterSmellMap);
		if(runStructural)
			runStructuralDetectionAlgs(clusters, detectedSmells, clusterSmellMap);

		// Invert map: instead of smells per cluster, clusters per smell
		Map<String, Set<String>> smellClustersMap = 
			buildSmellToClustersMap(clusterSmellMap);

		// Log the results
		for (String clusterName : clusterSmellMap.keySet()) {
			Set<String> smellList = clusterSmellMap.get(clusterName);
			logger.debug(clusterName + " has smells "
				+ String.join(",", smellList));
		}
		for (Entry<String,Set<String>> entry : smellClustersMap.entrySet())
			logger.debug(entry.getKey() + " : " + entry.getValue());
		for (Smell smell : detectedSmells)
			logger.debug(smell.getSmellType() + " " + smell);

		// Serialize results
		if(runSerialize)
			detectedSmells.serializeSmellCollection(detectedSmellsFilename);

		// Return results
		return detectedSmells;
	}

	public void runConcernDetectionAlgs(ConcernClusterArchitecture clusters,
			SmellCollection detectedSmells, Map<String,Set<String>> clusterSmellMap) {

		
		//Added for testing
		//Serialization for test oracles.
		
		/*
		char fs = File.separatorChar;
		try{
			String resources_dir = "src///test///resources///ArchSmellDetectorTest_resources///runConcernDetectionAlgs_resources///";
			resources_dir = resources_dir.replace("///", File.separator);

			ObjectOutputStream oosDSmells2 = new ObjectOutputStream(new FileOutputStream(resources_dir + version + "_output_detectedSmells_before.txt"));
			oosDSmells2.writeObject(detectedSmells);
			oosDSmells2.flush();
			oosDSmells2.close();



			ObjectOutputStream oosClusters2 = new ObjectOutputStream(new FileOutputStream(resources_dir + version + "_output_clusters_before.txt"));
			oosClusters2.writeObject(clusters);
			oosClusters2.flush();
			oosClusters2.close();

			ObjectOutputStream oosCSM2 = new ObjectOutputStream(new FileOutputStream(resources_dir + version + "_output_clusterSmellMap_before.txt"));
			oosCSM2.writeObject(clusterSmellMap);
			oosCSM2.flush();
			oosCSM2.close();

		}catch(IOException e){
			e.printStackTrace();
		}
		*/

		if (this.tmeMethod == TopicModelExtractionMethod.MALLET_API)
			buildConcernClustersFromMalletAPI(clusters);
		if (this.tmeMethod == TopicModelExtractionMethod.VAR_MALLET_FILE)
			buildConcernClustersFromConfigTopicsFile(clusters);
		
		for (ConcernCluster cluster : clusters) {
			if (cluster.getDocTopicItem() != null) {
				logger.debug(cluster.getName() + " has topics: ");
				DocTopicItem docTopicItem = cluster.getDocTopicItem();
				Collections.sort(docTopicItem.getTopics(), TOPIC_PROPORTION_ORDER);
				for (TopicItem topicItem : docTopicItem.getTopics()) {
					logger.debug("\t" + topicItem);
				}
			}
		}

		detectBco(detectedSmells, clusters, clusterSmellMap);
		detectSpfNew(clusters, clusterSmellMap, detectedSmells);

		//Added for testing
		//Serialization for test oracles.
		/*
		try{
			String resources_dir = "src///test///resources///ArchSmellDetectorTest_resources///runConcernDetectionAlgs_resources///";
			resources_dir = resources_dir.replace("///", File.separator);
			
			ObjectOutputStream oosDSmells2 = new ObjectOutputStream(new FileOutputStream(resources_dir + version + "_output_detectedSmells_after.txt"));
			oosDSmells2.writeObject(detectedSmells);
			oosDSmells2.flush();
			oosDSmells2.close();
			ObjectOutputStream oosClusters2 = new ObjectOutputStream(new FileOutputStream(resources_dir + version + "_output_clusters_after.txt"));
			oosClusters2.writeObject(clusters);
			oosClusters2.flush();
			oosClusters2.close();
			ObjectOutputStream oosCSM2 = new ObjectOutputStream(new FileOutputStream(resources_dir + version + "_output_clusterSmellMap_after.txt"));
			oosCSM2.writeObject(clusterSmellMap);
			oosCSM2.flush();
			oosCSM2.close();
			
			
		}catch(IOException e){
			e.printStackTrace();
		}
		*/
	}

	public void runStructuralDetectionAlgs(ConcernClusterArchitecture clusters,
			SmellCollection detectedSmells, Map<String,Set<String>> clusterSmellMap) {
		Map<String, Set<String>> depMap = 
		ClusterUtil.buildDependenciesMap(this.depsRsfFilename);
	
		StringGraph clusterGraph = clusters.buildClusterGraphUsingDepMap(depMap);
		System.out.print("");
		
		SimpleDirectedGraph<String, DefaultEdge> directedGraph = 
			clusters.buildConcernClustersDiGraph(clusterGraph);
		
		detectBdc(detectedSmells, clusters, clusterSmellMap, directedGraph);
		detectBuo(detectedSmells, clusters, clusterSmellMap, directedGraph);

		//Added for testing
		//Serialization for test oracles.
		/*
		String resources_dir = "src///test///resources///ArchSmellDetectorTest_resources///runStructuralDetectionAlgs_resources///";
      	resources_dir = resources_dir.replace("///", File.separator);
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(resources_dir + version + "_output_run_clusterSmellMap.txt"));
			oos.writeObject(clusterSmellMap);
			oos.flush();
			oos.close();

			oos = new ObjectOutputStream(new FileOutputStream(resources_dir + version + "_output_run_clusters.txt"));
			oos.writeObject(clusters);
			oos.flush();
			oos.close();

			oos = new ObjectOutputStream(new FileOutputStream(resources_dir + version + "_output_run_detected_smells.txt"));
			oos.writeObject(detectedSmells);
			oos.flush();
			oos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
	}
	// #endregion PUBLIC INTERFACE -----------------------------------------------

	// #region IO ----------------------------------------------------------------
	/**
	 * Loads clusters from the RSF file pointed to by this.clustersRsfFilename.
	 */
	private ConcernClusterArchitecture loadClusters() {
		System.out.println("Reading in clusters file: " + this.clustersRsfFilename);
		ConcernClusterArchitecture clusters = ConcernClusterArchitecture.
			loadFromRsf(this.clustersRsfFilename);
		logger.debug("Found and built clusters:");
		for (ConcernCluster cluster : clusters)
			logger.debug(cluster.getName());

		return clusters;
	}

	private void buildConcernClustersFromConfigTopicsFile(
			ConcernClusterArchitecture clusters) {
		for (ConcernCluster cluster : clusters) {
			logger.debug("Building doctopics for " + cluster.getName());
			for (String entity : cluster.getEntities()) {
				DocTopicItem entityDocTopicItem = null;
				switch(this.language.toLowerCase()) {
					case "c":
						entityDocTopicItem = this.docTopics.getDocTopicItemForC(entity);
						break;
					case "java":
					default:
						entityDocTopicItem = setDocTopicItemForJavaFromMalletFile(entity);
						break;
				}

				if (cluster.getDocTopicItem() == null)
					cluster.setDocTopicItem(entityDocTopicItem);
				else {
					DocTopicItem mergedDocTopicItem = null;

					try {
						mergedDocTopicItem = TopicUtil.mergeDocTopicItems(
							cluster.getDocTopicItem(), entityDocTopicItem);
					} catch (UnmatchingDocTopicItemsException e) {
						e.printStackTrace(); //TODO handle it
					}

					cluster.setDocTopicItem(mergedDocTopicItem);
				}
			}
		}
	}

	private DocTopicItem setDocTopicItemForJavaFromMalletFile(String entity) {
		DocTopicItem newDocTopicItem;
		String docTopicName = entity.replace("\\.", "/") + ".java";
		newDocTopicItem = docTopics.getDocTopicItemForJava(docTopicName);
		return newDocTopicItem;
	}

	private void buildConcernClustersFromMalletAPI(
			ConcernClusterArchitecture clusters) {
		for (ConcernCluster cluster : clusters) {
			logger.debug("Building doctopics for " + cluster.getName());
			for (String entity : cluster.getEntities()) {
				DocTopicItem entityDocTopicItem = null;
				if (this.language.equalsIgnoreCase("java"))
					entityDocTopicItem = setDocTopicItemForJavaFromMalletApi(entity);

				if (cluster.getDocTopicItem() == null)
					cluster.setDocTopicItem(entityDocTopicItem);
				else {
					DocTopicItem mergedDocTopicItem = null;

					try {
						mergedDocTopicItem = TopicUtil.mergeDocTopicItems(
							cluster.getDocTopicItem(), entityDocTopicItem);
					} catch (UnmatchingDocTopicItemsException e) {
						e.printStackTrace(); //TODO handle it
					}

					cluster.setDocTopicItem(mergedDocTopicItem);
				}
			}
		}
	}
	
	private DocTopicItem setDocTopicItemForJavaFromMalletApi(String entity) {
		DocTopicItem newDocTopicItem;
		newDocTopicItem = docTopics.getDocTopicItemForJava(entity);
		return newDocTopicItem;
	}
	// #endregion IO -------------------------------------------------------------

	/**
	 * Creates a map of clusters per smell from a map of smells per cluster.
	 * 
	 * @param clusterSmellMap Map of smells per cluster.
	 * @return Map of clusters per smell.
	 */

	//Changed buildSmellToClustersMap from private to protected for testing
	protected Map<String, Set<String>> buildSmellToClustersMap(
			Map<String, Set<String>> clusterSmellMap) {
		Map<String,Set<String>> smellClustersMap = new HashMap<>();

		for (String clusterName : clusterSmellMap.keySet()) {
			Set<String> smellList = clusterSmellMap.get(clusterName);
			for (String smell : smellList) {
				if (smellClustersMap.containsKey(smell))
					smellClustersMap.get(smell).add(clusterName);
				else {
					Set<String> mClusters = new HashSet<>();
					mClusters.add(clusterName);
					smellClustersMap.put(smell, mClusters);
				}
			}
		}

		//Added for creating test oracles, uncomment to produce new test oracles
		/*
		char fs = File.separatorChar;
		try{
			String resources_dir = "src///test///resources///ArchSmellDetectorTest_resources///runConcernDetectionAlgs_resources///";
			resources_dir = resources_dir.replace("///", File.separator);

			ObjectOutputStream oosDSmells2 = new ObjectOutputStream(new FileOutputStream(resources_dir + version + "_output_smellClusterMap_after.txt"));
			oosDSmells2.writeObject(smellClustersMap);
			oosDSmells2.flush();
			oosDSmells2.close();

		}catch(IOException e){
			e.printStackTrace();
		}
		*/

		return smellClustersMap;
	}

	// #region DETECTION ALGORITHMS ----------------------------------------------
	protected void detectBuo(SmellCollection detectedSmells,
			ConcernClusterArchitecture clusters, 
			Map<String, Set<String>> clusterSmellMap,
			SimpleDirectedGraph<String, DefaultEdge> directedGraph) {
		Set<String> vertices = directedGraph.vertexSet();
        
		logger.debug("Computing the in and out degress of each vertex");
		List<Double> inDegrees = new ArrayList<>();
		List<Double> outDegrees = new ArrayList<>();
		for (String vertex : vertices) {
			logger.debug("\t" + vertex);
			int inDegree = directedGraph.inDegreeOf(vertex);
			int outDegree = directedGraph.outDegreeOf(vertex);
			logger.debug("\t\t in degree: " + inDegree);
			logger.debug("\t\t out degree: " + outDegree);
			inDegrees.add((double)inDegree);
			outDegrees.add((double)outDegree);
		}
        
		double[] inAndOutDegreesArray = new double[inDegrees.size()];
		for (int i=0; i<inDegrees.size(); i++) {
			double inPlusOutAtI = inDegrees.get(i) + outDegrees.get(i);
			inAndOutDegreesArray[i] = inPlusOutAtI;
		}
    
		double[] inDegreesArray = inDegrees.stream()
			.mapToDouble(Double::doubleValue).toArray();
		double[] outDegreesArray = outDegrees.stream()
			.mapToDouble(Double::doubleValue).toArray();
		double meanInDegrees = StatUtils.mean(inDegreesArray);
		double meanOutDegrees = StatUtils.mean(outDegreesArray);
		double meanInAndOutDegrees = StatUtils.mean(inAndOutDegreesArray);
		
		StandardDeviation stdDev = new StandardDeviation();
		double stdDevInDegrees = stdDev.evaluate(inDegreesArray);
		double stdDevOutDegrees = stdDev.evaluate(outDegreesArray);
		double stdDevInAndOutDegrees = stdDev.evaluate(inAndOutDegreesArray);
		logger.debug("mean of in degrees: " + meanInDegrees);
		logger.debug("mean of out degrees: " + meanOutDegrees);
		logger.debug("mean of in plus out degrees: " + meanInAndOutDegrees);
		logger.debug("std dev of in degrees: " + stdDevInDegrees);
		logger.debug("std dev of out degrees: " + stdDevOutDegrees);
		logger.debug("std dev of in plus out degrees: " + stdDevInAndOutDegrees);
        
		double stdDevFactor = 1.5;
		for (String vertex : vertices) {
			int inDegree = directedGraph.inDegreeOf(vertex);
			int outDegree = directedGraph.outDegreeOf(vertex);
			if (inDegree > meanInDegrees + stdDevFactor * stdDevInDegrees) {
				logger.debug("\t\t" + vertex + " has brick use overload for in degrees");
				logger.debug("\t\t in degree: "	+ inDegree);
				updateSmellMap(clusterSmellMap, vertex, "buo");
				addDetectedBuoSmell(detectedSmells, clusters, vertex);
			}
			if (outDegree > meanOutDegrees + stdDevFactor * stdDevOutDegrees) {
				logger.debug("\t\t" + vertex + " has brick use overload for out degrees");
				logger.debug("\t\t out degree: " + outDegree);
				updateSmellMap(clusterSmellMap, vertex, "buo");
				addDetectedBuoSmell(detectedSmells, clusters, vertex);
			}
			if (inDegree + outDegree > meanInDegrees + meanOutDegrees + 
				stdDevFactor * stdDevOutDegrees + stdDevFactor * stdDevInDegrees) {
				logger.debug("\t\t" + vertex + " has brick use overload for both in and out degrees");
				logger.debug("\t\t in degree: "	+ inDegree);
				logger.debug("\t\t out degree: " + outDegree);
				updateSmellMap(clusterSmellMap, vertex, "buo");
				addDetectedBuoSmell(detectedSmells, clusters, vertex);
			}
			int inPlusOutDegree = inDegree + outDegree;
			if (inPlusOutDegree > meanInAndOutDegrees + stdDevFactor * stdDevInAndOutDegrees) {
				logger.debug("\t\t" + vertex + " has brick use overload for in plus out degrees");
				logger.debug("\t\t in plus out degrees: " + inPlusOutDegree);
				updateSmellMap(clusterSmellMap, vertex, "buo");
				addDetectedBuoSmell(detectedSmells, clusters, vertex);
			}
		}
	}

	protected void detectBdc(SmellCollection detectedSmells,
			ConcernClusterArchitecture clusters,
			Map<String, Set<String>> clusterSmellMap,
			SimpleDirectedGraph<String, DefaultEdge> directedGraph) {
		logger.debug("Finding cycles...");
		CycleDetector<String, DefaultEdge> cycleDetector =
			new CycleDetector<>(directedGraph);
		Set<String> cycleSet = cycleDetector.findCycles();
		logger.debug("Printing the cycle set, i.e., the set of all vertices" + 
			"which participate in at least one cycle in this graph...");
		logger.debug(cycleSet);
		
		KosarajuStrongConnectivityInspector<String, DefaultEdge> inspector =
			new KosarajuStrongConnectivityInspector<>(directedGraph);
		List<Set<String>> connectedSets = inspector.stronglyConnectedSets();
		List<String> connectedSetsString =
			connectedSets.stream().map(Set::toString).collect(Collectors.toList());
		logger.debug("Printing the strongly connected sets of the graph....");
		logger.debug(String.join("\n", connectedSetsString));
		
		int relevantConnectedSetCount = 0;
		Set<Set<String>> bdcConnectedSets = new HashSet<>();
		for (Set<String> connectedSet : connectedSets) {
			if (connectedSet.size() > 2) {
				logger.debug("Counting this strongly connected component set as relevant");
				logger.debug(connectedSet);
				relevantConnectedSetCount++;
				for (String clusterName : connectedSet)
					updateSmellMap(clusterSmellMap, clusterName, "bdc");
				logger.debug("scc size: " + connectedSet.size());
				bdcConnectedSets.add(connectedSet);
			}
		}
		
		for (Set<String> bdcConnectedSet : bdcConnectedSets) {
			Smell bdc = new Smell(Smell.SmellType.bdc);
			for (String clusterName : bdcConnectedSet) {
				ConcernCluster cluster = getMatchingCluster(clusterName,clusters);
				assert cluster != null : "No matching cluster found for " + clusterName;
				bdc.addCluster(cluster);
			}
			detectedSmells.add(bdc);
		}
		
		logger.debug("Number of strongly connected components: " + relevantConnectedSetCount);
	}

	protected StandardDeviation detectBco(SmellCollection detectedSmells,
			ConcernClusterArchitecture clusters,
			Map<String, Set<String>> clusterSmellMap) {
		System.out.println("Finding brick concern overload instances...");

		double concernOverloadTopicThreshold = .10;
		Map<ConcernCluster,Integer> concernCountMap = new HashMap<>(); 
		for (ConcernCluster cluster : clusters) {
			if (cluster.getDocTopicItem() != null) {
				logger.debug("Have doc-topics for " + cluster.getName());
				DocTopicItem docTopicItem = cluster.getDocTopicItem();
				int concernCount = 0;
				for (TopicItem topicItem : docTopicItem.getTopics())  {
					if (topicItem.getProportion() > concernOverloadTopicThreshold) {
						logger.debug("\t" + cluster.getName() + " is beyond concern overload threshold for " + topicItem);
						concernCount++;
					}
				}
				concernCountMap.put(cluster, concernCount);
			}
		}
		
		StandardDeviation stdDev = new StandardDeviation();
		
		int[] intConcernCountValues =
			concernCountMap.values().stream().mapToInt(Integer::valueOf).toArray();
		double[] doubleConcernCountValues = new double[intConcernCountValues.length];
		for (int i=0; i<intConcernCountValues.length; i++)
			doubleConcernCountValues[i] = intConcernCountValues[i];
		
		double concernCountMean = StatUtils.mean(doubleConcernCountValues);
		double concernCountStdDev = stdDev.evaluate(doubleConcernCountValues);
		logger.debug("relevant concern count mean: " + concernCountMean);
		logger.debug("relevant concern count standard deviation: " + concernCountStdDev);
		
		
		for (ConcernCluster cluster : concernCountMap.keySet()) {
			int concernCount = concernCountMap.get(cluster);
			if (concernCount > concernCountMean + concernCountStdDev) {
				logger.debug("\t" + cluster.getName() + " has brick concern overload.");
				
				Smell bco = new Smell(Smell.SmellType.bco);
				bco.addCluster(cluster);
				detectedSmells.add(bco);
				
				updateSmellMap(clusterSmellMap, cluster.getName(), "bco");
			}
		}
		
		return stdDev;
	}

	private void addDetectedBuoSmell(SmellCollection detectedSmells,
			ConcernClusterArchitecture clusters, String vertex) {
		Smell buo = new Smell(Smell.SmellType.buo);
		buo.addCluster(getMatchingCluster(vertex,clusters));
		detectedSmells.add(buo);
	}
	
	private ConcernCluster getMatchingCluster(String clusterName,
			ConcernClusterArchitecture clusters) {
		for (ConcernCluster cluster : clusters)
			if (cluster.getName().equals(clusterName))
				return cluster;

		return null;
	}

	/**
	 * Adds a smell to the given cluster in clusterSmellMap.
	 * 
	 * @param clusterSmellMap Map of all clusters and their sets of smells.
	 * @param clusterName Name of the cluster to add a smell to.
	 * @param smellAbrv Abbreviation mell to add to the cluster.
	 */
	protected void updateSmellMap(Map<String, Set<String>> clusterSmellMap,
			String clusterName, String smellAbrv) {
		Set<String> smellList = clusterSmellMap.get(clusterName);
		if(smellList == null)
			smellList = new HashSet<>();
		smellList.add(smellAbrv);
		clusterSmellMap.putIfAbsent(clusterName, smellList);
	}

	// #region SCATTERED PARASITIC FUNCTIONALITY ---------------------------------
	/**
	 * Detects Scattered Parasitic Functionality smell instances.
	 * 
	 * @param clusters Input clusters to run detection algorithm on.
	 * @param clusterSmellsMap Mapping of smells per cluster. Altered.
	 * @param detectedSmells Set of detected smells. Altered.
	 */
	protected void detectSpfNew(
			ConcernClusterArchitecture clusters,
			Map<String, Set<String>> clusterSmellsMap,
			SmellCollection detectedSmells) {
		logger.info("Finding scattered parasitic functionality instances...");


		// Setting thresholds for detection algorithm
		Map<Integer, Integer> topicNumCountMap = new HashMap<>();
		Map<Integer, ConcernClusterArchitecture> scatteredTopicToClustersMap 
			= new HashMap<>();
		
		mapTopicItemsToCluster(
			clusters, topicNumCountMap, scatteredTopicToClustersMap);

		final double significanceThreshold = computeSPFSignificanceThreshold(
			new double[topicNumCountMap.keySet().size()], topicNumCountMap,
			scatteredTopicToClustersMap);

		// Select significant topic numbers
		Set<Integer> significantTopicNums = 
			new HashSet<>(topicNumCountMap.keySet());
		significantTopicNums.removeIf((Integer topicNum) ->
			topicNumCountMap.get(topicNum) <= significanceThreshold);

		for (int topicNum : significantTopicNums) {
			ConcernClusterArchitecture clustersWithScatteredTopics = 
				scatteredTopicToClustersMap.get(topicNum);
			ConcernClusterArchitecture affectedClusters =
				new ConcernClusterArchitecture();

			// Filter out all clusters that don't have topics
			ConcernClusterArchitecture validScatteredClusters = 
				new ConcernClusterArchitecture(clustersWithScatteredTopics);
			validScatteredClusters.removeIf((ConcernCluster cluster) -> 
				cluster.getDocTopicItem() == null);

			for (ConcernCluster cluster : validScatteredClusters) {
				// Filter out all topic items that aren't smelly
				DocTopicItem dti = cluster.getDocTopicItem();
				List<TopicItem> significantTopicItems = new ArrayList<>(dti.getTopics());
				significantTopicItems.removeIf((TopicItem ti) -> 
					ti.getTopicNum() == topicNum || ti.getProportion() < parasiticConcernThreshold);

				for (int i = 0; i < significantTopicItems.size(); i++) {
					logger.debug(cluster.getName()
						+ " has spf with scattered concern " + topicNum);

					clusterSmellsMap.putIfAbsent(cluster.getName(), new HashSet<>());
					clusterSmellsMap.get(cluster.getName()).add("spf");
					affectedClusters.add(cluster);
				}
			}
			
			Smell spf = new Smell(topicNum);
			for (ConcernCluster affectedCluster : affectedClusters)
				spf.addCluster(affectedCluster);
			detectedSmells.add(spf);
		}

	}

	/**
	 * Maps topic items to clusters.
	 * 
	 * @param inputClusters Clusters on which to run the mapping.
	 * @param topicNumCountMap Count of occurrences of each topic. Altered.
	 * @param scatteredTopicToClustersMap Mapping of topics to clusters.
	 */
	private void mapTopicItemsToCluster(
			ConcernClusterArchitecture inputClusters,
			Map<Integer, Integer> topicNumCountMap,
			Map<Integer, ConcernClusterArchitecture> scatteredTopicToClustersMap) {
		// Filter out all clusters that don't have topics
		ConcernClusterArchitecture validClusters =
			new ConcernClusterArchitecture(inputClusters);
		validClusters.removeIf((ConcernCluster cluster) -> 
			cluster.getDocTopicItem() == null);
		
		for (ConcernCluster cluster : validClusters) {
			// Filter out all topic items that aren't smelly
			DocTopicItem dti = cluster.getDocTopicItem();
			List<TopicItem> smellyTopicItems = new ArrayList<>(dti.getTopics());
			smellyTopicItems.removeIf((TopicItem ti) -> 
				ti.getProportion() < scatteredConcernThreshold);

			for (TopicItem ti : smellyTopicItems) {
				// count the number of times the topic appears
				topicNumCountMap.compute(ti.getTopicNum(), (k, v) -> (v == null) ? 1 : ++v);
				
				// map cluster to the related topicItem
				scatteredTopicToClustersMap.putIfAbsent(
					ti.getTopicNum(), new ConcernClusterArchitecture());
				scatteredTopicToClustersMap.get(ti.getTopicNum()).add(cluster);
			}
		}
	}

	/**
	 * Computes the significance threshold for Scattered Parasitic Functionality
	 * smells. This represents the number of clusters a topic item must appear in
	 * to be considered an SPF.
	 * 
	 * @param topicCounts Number of appearances of each topic item number.
	 * @param topicNumCountMap Map of topic item counts.
	 * @param scatteredTopicToClustersMap Map of clusters per topic item.
	 * @return
	 */
	private double computeSPFSignificanceThreshold(
			double[] topicCounts, Map<Integer, Integer> topicNumCountMap,
			Map<Integer, ConcernClusterArchitecture> scatteredTopicToClustersMap){
		double topicCountMean = 0;
		double topicCountStdDev = 0;
		int topicNumCounter = 0;

		for (Integer topicNum : topicNumCountMap.values()) {
			topicCounts[topicNumCounter] = (double)topicNum;
			topicNumCounter++;
		}

		topicCountMean = StatUtils.mean(topicCounts);
		StandardDeviation stdDev = new StandardDeviation();
		topicCountStdDev = stdDev.evaluate(topicCounts);
		
		logger.debug("topic count mean: " + topicCountMean);
		logger.debug("topic count standard deviation: " + topicCountStdDev);

		logger.debug("topic num : count");
		for (Map.Entry<Integer, Integer> entry : topicNumCountMap.entrySet())
			logger.debug(entry.getKey() + " : " + entry.getValue() );

		logger.debug("topic num : clusters with topic");
		for (Map.Entry<Integer, ConcernClusterArchitecture> entry 
				: scatteredTopicToClustersMap.entrySet())
			logger.debug(entry.getKey() + " : " + entry.getValue() );

		return topicCountMean + topicCountStdDev;
	}
	// #endregion SCATTERED PARASITIC FUNCTIONALITY ------------------------------
	// #endregion DETECTION ALGORITHMS -------------------------------------------
}