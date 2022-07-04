package edu.usc.softarch.arcade.antipattern.detection;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.jgrapht.alg.connectivity.KosarajuStrongConnectivityInspector;
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
import edu.usc.softarch.arcade.topics.exceptions.UnmatchingDocTopicItemsException;

public class ArchSmellDetector {
	//region ATTRIBUTES
	private final String depsRsfFilename;
	private final String clustersRsfFilename;
	private final String detectedSmellsFilename;
	private final String language;
	private final double scatteredConcernThreshold;
	private final double parasiticConcernThreshold;

	static final Comparator<TopicItem> TOPIC_PROPORTION_ORDER
		= (TopicItem t1, TopicItem t2) -> {
			Double prop1 = t1.proportion;
			Double prop2 = t2.proportion;
			return prop1.compareTo(prop2);
		};
	//endregion ATTRIBUTES

	//region CONSTRUCTORS
	public ArchSmellDetector(String depsRsfFilename, String clustersRsfFilename,
			String detectedSmellsFilename) {
		this(depsRsfFilename, clustersRsfFilename, detectedSmellsFilename, 
			"structural-irrelevant", .20,
			.20);
	}

	public ArchSmellDetector(String depsRsfFilename, String clustersRsfFilename,
			String detectedSmellsFilename, String language) {
		this(depsRsfFilename, clustersRsfFilename, detectedSmellsFilename,
			language, .20, .20);
	}

	public ArchSmellDetector(String depsRsfFilename, String clustersRsfFilename,
			String detectedSmellsFilename, String language,
			double scatteredConcernThreshold, double parasiticConcernThreshold) {
		this.depsRsfFilename = depsRsfFilename;
		this.clustersRsfFilename = clustersRsfFilename;
		this.detectedSmellsFilename = detectedSmellsFilename;
		this.language = language;
		this.scatteredConcernThreshold = scatteredConcernThreshold;
		this.parasiticConcernThreshold = parasiticConcernThreshold;
	}
	//endregion CONSTRUCTORS
	
	public static void main(String[] args) throws IOException {
		String depsRsfFilename = args[0];
		String clustersRsfFilename = args[1];
		String detectedSmellsFilename = args[2];
		String language = args[3];
		String docTopicsPath = args[4];
		String isArc = args[5];

		if (isArc.equals("true")) {
			DocTopics.deserialize(docTopicsPath);
			ArchSmellDetector asd = new ArchSmellDetector(depsRsfFilename,
				clustersRsfFilename, detectedSmellsFilename, language);
			asd.run(true, true, true);
		} else {
			ArchSmellDetector asd = new ArchSmellDetector(depsRsfFilename,
				clustersRsfFilename, detectedSmellsFilename);
			asd.run(true, false, true);
		}
	}

	public SmellCollection run(boolean runStructural, boolean runConcern, 
			boolean runSerialize) throws IOException {
		// Make sure at least one type of smell detection algorithms was selected
		if (!runConcern && !runStructural)
			throw new IllegalArgumentException("At least one type of smell "
				+ "detection must be selected.");

		// Initialize variables
		SmellCollection detectedSmells = new SmellCollection();
		ConcernClusterArchitecture clusters = loadClusters();

		// Execute detection algorithms
		if (runConcern)
			runConcernDetectionAlgs(clusters, detectedSmells);
		if (runStructural)
			runStructuralDetectionAlgs(clusters, detectedSmells);

		// Serialize results
		if (runSerialize) {
			new File(detectedSmellsFilename).getParentFile().mkdirs();
			detectedSmells.serialize(detectedSmellsFilename);
		}

		// Return results
		return detectedSmells;
	}

	public void runConcernDetectionAlgs(ConcernClusterArchitecture clusters,
			SmellCollection detectedSmells) {
		buildConcernClustersFromMalletAPI(clusters);

		for (ConcernCluster cluster : clusters) {
			if (cluster.getDocTopicItem() != null) {
				DocTopicItem docTopicItem = cluster.getDocTopicItem();
				docTopicItem.getTopics().sort(TOPIC_PROPORTION_ORDER);
			}
		}

		detectBco(detectedSmells, clusters);
		detectSpfNew(clusters, detectedSmells);
	}

	public void runStructuralDetectionAlgs(ConcernClusterArchitecture clusters,
			SmellCollection detectedSmells) {
		Map<String, Set<String>> depMap = 
		ClusterUtil.buildDependenciesMap(this.depsRsfFilename);
	
		StringGraph clusterGraph = clusters.buildClusterGraphUsingDepMap(depMap);
		System.out.print("");
		
		SimpleDirectedGraph<String, DefaultEdge> directedGraph = 
			clusters.buildConcernClustersDiGraph(clusterGraph);
		
		detectBdc(detectedSmells, clusters, directedGraph);
		detectBuo(detectedSmells, clusters, directedGraph);
	}

	/**
	 * Loads clusters from the RSF file pointed to by this.clustersRsfFilename.
	 */
	private ConcernClusterArchitecture loadClusters() {
		return ConcernClusterArchitecture.loadFromRsf(this.clustersRsfFilename);
	}

	private void buildConcernClustersFromMalletAPI(
			ConcernClusterArchitecture clusters) {
		for (ConcernCluster cluster : clusters) {
			for (String entity : cluster.getEntities()) {
				DocTopicItem entityDocTopicItem = null;
				if (this.language.equalsIgnoreCase("java"))
					entityDocTopicItem = setDocTopicItemForJavaFromMalletApi(entity);

				if (cluster.getDocTopicItem() == null)
					cluster.setDocTopicItem(entityDocTopicItem);
				else {
					DocTopicItem mergedDocTopicItem = null;

					try {
						mergedDocTopicItem = DocTopics.getSingleton().mergeDocTopicItems(
							cluster.getDocTopicItem(), entityDocTopicItem, cluster.getName());
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
		newDocTopicItem = DocTopics.getSingleton().getDocTopicItemForJava(entity);
		return newDocTopicItem;
	}

	protected void detectBuo(SmellCollection detectedSmells,
			ConcernClusterArchitecture clusters,
			SimpleDirectedGraph<String, DefaultEdge> directedGraph) {
		Set<String> vertices = directedGraph.vertexSet();
        
		List<Double> inDegrees = new ArrayList<>();
		List<Double> outDegrees = new ArrayList<>();
		for (String vertex : vertices) {
			int inDegree = directedGraph.inDegreeOf(vertex);
			int outDegree = directedGraph.outDegreeOf(vertex);
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

		double stdDevFactor = 1.5;
		for (String vertex : vertices) {
			int inDegree = directedGraph.inDegreeOf(vertex);
			int outDegree = directedGraph.outDegreeOf(vertex);
			if (inDegree > meanInDegrees + stdDevFactor * stdDevInDegrees) {
				addDetectedBuoSmell(detectedSmells, clusters, vertex);
			}
			if (outDegree > meanOutDegrees + stdDevFactor * stdDevOutDegrees) {
				addDetectedBuoSmell(detectedSmells, clusters, vertex);
			}
			if (inDegree + outDegree > meanInDegrees + meanOutDegrees + 
				stdDevFactor * stdDevOutDegrees + stdDevFactor * stdDevInDegrees) {
				addDetectedBuoSmell(detectedSmells, clusters, vertex);
			}
			int inPlusOutDegree = inDegree + outDegree;
			if (inPlusOutDegree > meanInAndOutDegrees + stdDevFactor * stdDevInAndOutDegrees) {
				addDetectedBuoSmell(detectedSmells, clusters, vertex);
			}
		}
	}

	protected void detectBdc(SmellCollection detectedSmells,
			ConcernClusterArchitecture clusters,
			SimpleDirectedGraph<String, DefaultEdge> directedGraph) {
		KosarajuStrongConnectivityInspector<String, DefaultEdge> inspector =
			new KosarajuStrongConnectivityInspector<>(directedGraph);
		List<Set<String>> connectedSets = inspector.stronglyConnectedSets();

		Set<Set<String>> bdcConnectedSets = new HashSet<>();
		for (Set<String> connectedSet : connectedSets) {
			if (connectedSet.size() > 2) {
				bdcConnectedSets.add(connectedSet);
			}
		}
		
		for (Set<String> bdcConnectedSet : bdcConnectedSets) {
			Smell bdc = new Smell(Smell.SmellType.bdc);
			for (String clusterName : bdcConnectedSet) {
				ConcernCluster cluster = getMatchingCluster(clusterName,clusters);
				bdc.addCluster(cluster);
			}
			detectedSmells.add(bdc);
		}
	}

	protected StandardDeviation detectBco(SmellCollection detectedSmells,
			ConcernClusterArchitecture clusters) {
		double concernOverloadTopicThreshold = .10;
		Map<ConcernCluster,Integer> concernCountMap = new HashMap<>(); 
		for (ConcernCluster cluster : clusters) {
			if (cluster.getDocTopicItem() != null) {
				DocTopicItem docTopicItem = cluster.getDocTopicItem();
				int concernCount = 0;
				for (TopicItem topicItem : docTopicItem.getTopics())  {
					if (topicItem.proportion > concernOverloadTopicThreshold) {
						concernCount++;
					}
				}
				concernCountMap.put(cluster, concernCount);
			}
		}
		
		StandardDeviation stdDev = new StandardDeviation();

		double[] doubleConcernCountValues =
			concernCountMap.values().stream().mapToDouble(Double::valueOf).toArray();

		double concernCountMean = StatUtils.mean(doubleConcernCountValues);
		double concernCountStdDev = stdDev.evaluate(doubleConcernCountValues);
		
		for (ConcernCluster cluster : concernCountMap.keySet()) {
			int concernCount = concernCountMap.get(cluster);
			if (concernCount > concernCountMean + concernCountStdDev) {
				Smell bco = new Smell(Smell.SmellType.bco);
				bco.addCluster(cluster);
				detectedSmells.add(bco);
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
	 * Detects Scattered Parasitic Functionality smell instances.
	 * 
	 * @param clusters Input clusters to run detection algorithm on.
	 * @param detectedSmells Set of detected smells. Altered.
	 */
	protected void detectSpfNew(
			ConcernClusterArchitecture clusters,
			SmellCollection detectedSmells) {
		// Setting thresholds for detection algorithm
		Map<Integer, Integer> topicNumCountMap = new HashMap<>();
		Map<Integer, ConcernClusterArchitecture> scatteredTopicToClustersMap 
			= new HashMap<>();
		
		mapTopicItemsToCluster(
			clusters, topicNumCountMap, scatteredTopicToClustersMap);

		final double significanceThreshold =
			computeSPFSignificanceThreshold(topicNumCountMap);

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
					ti.topicNum == topicNum || ti.proportion < parasiticConcernThreshold);

				for (int i = 0; i < significantTopicItems.size(); i++) {
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
				ti.proportion < scatteredConcernThreshold);

			for (TopicItem ti : smellyTopicItems) {
				// count the number of times the topic appears
				topicNumCountMap.compute(ti.topicNum, (k, v) -> (v == null) ? 1 : ++v);
				
				// map cluster to the related topicItem
				scatteredTopicToClustersMap.putIfAbsent(
					ti.topicNum, new ConcernClusterArchitecture());
				scatteredTopicToClustersMap.get(ti.topicNum).add(cluster);
			}
		}
	}

	/**
	 * Computes the significance threshold for Scattered Parasitic Functionality
	 * smells. This represents the number of clusters a topic item must appear in
	 * to be considered an SPF.
	 * 
	 * @param topicNumCountMap Map of topic item counts.
	 */
	private double computeSPFSignificanceThreshold(
			Map<Integer, Integer> topicNumCountMap) {
		double topicCountMean;
		double topicCountStdDev;
		double[] topicCounts = topicNumCountMap.values().stream()
			.mapToDouble(Integer::doubleValue).toArray();

		topicCountMean = StatUtils.mean(topicCounts);
		topicCountStdDev = (new StandardDeviation()).evaluate(topicCounts);

		return topicCountMean + topicCountStdDev;
	}
}
