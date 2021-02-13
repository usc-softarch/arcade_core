package edu.usc.softarch.arcade.antipattern.detection;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.alg.StrongConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import com.google.common.base.Joiner;
import com.google.common.primitives.Doubles;
import com.thoughtworks.xstream.XStream;

import edu.usc.softarch.arcade.clustering.StringGraph;
import edu.usc.softarch.arcade.clustering.util.ClusterUtil;
import edu.usc.softarch.arcade.config.Config;
import edu.usc.softarch.arcade.facts.ConcernCluster;
import edu.usc.softarch.arcade.facts.driver.ConcernClusterRsf;
import edu.usc.softarch.arcade.topics.DocTopicItem;
import edu.usc.softarch.arcade.topics.DocTopics;
import edu.usc.softarch.arcade.topics.TopicItem;
import edu.usc.softarch.arcade.topics.TopicModelExtractionMethod;
import edu.usc.softarch.arcade.topics.TopicUtil;
import edu.usc.softarch.arcade.topics.UnmatchingDocTopicItemsException;

public class ArchSmellDetector {
	private static Logger logger = Logger.getLogger(ArchSmellDetector.class);
	public static TopicModelExtractionMethod tmeMethod =
		TopicModelExtractionMethod.VAR_MALLET_FILE;
	public static DocTopics docTopics;
	private static double scatteredConcernThreshold = .20;
	private static double parasiticConcernThreshold = .20;
	
	static final Comparator<TopicItem> TOPIC_PROPORTION_ORDER 
		= (TopicItem t1, TopicItem t2) -> {
			Double prop1 = t1.getProportion();
			Double prop2 = t2.getProportion();
			return prop1.compareTo(prop2);
		};
	
	public static void main(String[] args) {
		PropertyConfigurator.configure("cfg" + File.separator + "extractor_logging.cfg");
		
		String detectedSmellsFilename = setupOld(args);
		runAllDetectionAlgs(detectedSmellsFilename);
	}
	
	public static void setupAndRunStructuralDetectionAlgs(String[] args) {
		String depsRsfFilename = args[0];
		String clustersRsfFilename = args[1];
		String detectedSmellsFilename = args[2];
		Config.setDepsRsfFilename(depsRsfFilename);
		Config.setSmellClustersFile(clustersRsfFilename);
		runStructuralDetectionAlgs(detectedSmellsFilename);
	}

	private static String setupOld(String[] args) {
		String detectedSmellsFilename = "";
		
		Options options = new Options();
		
		Option help = new Option( "help", "print this message" );
		Option projFile   = OptionBuilder.withArgName( "file" )
                .hasArg()
                .withDescription(  "project configuration file" )
                .create( "projfile" );
		Option smellClassesFile = OptionBuilder.withArgName("file")
				.hasArg()
				.withDescription("detected smells to affected classes file")
				.create("smellClassesFile");
		Option detectedSmellsFile = OptionBuilder.withArgName("file")
				.hasArg()
				.withDescription("serialized detected smells file")
				.create("detectedSmellsFile");
		
		options.addOption(help);
		options.addOption(projFile);
		options.addOption(smellClassesFile);
		options.addOption(detectedSmellsFile);		
		
		 // create the parser
	    CommandLineParser parser = new GnuParser();
	    try {
	        // parse the command line arguments
	        CommandLine line = parser.parse( options, args );
	        
	        if (line.hasOption("projfile")) {
	        	Config.setProjConfigFilename(line.getOptionValue("projfile"));
	        }
	        if (line.hasOption("detectedSmellsFile")) {
	        	detectedSmellsFilename = line.getOptionValue("detectedSmellsFile");
	        }
	    }
	    catch( ParseException exp ) {
	        // oops, something went wrong
	        System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
	    }
		
		Config.initConfigFromFile(Config.getProjConfigFilename());
		return detectedSmellsFilename;
	}

	public static void runAllDetectionAlgs(String detectedSmellsFilename) {
		Set<Smell> detectedSmells = new LinkedHashSet<>();
		System.out.println("Reading in clusters file: "
			+ Config.getSmellClustersFile());
		Set<ConcernCluster> clusters = ConcernClusterRsf.extractConcernClustersFromRsfFile(Config.getSmellClustersFile());
		
		boolean showBuiltClusters = true;
		if (showBuiltClusters) {
			logger.debug("Found and built clusters:");
			for (ConcernCluster cluster : clusters) {
				logger.debug(cluster.getName());
			}
		}
		
		buildConcernClustersFromConfigTopicsFile(clusters);
		
		for (ConcernCluster cluster : clusters) {
			if (cluster.getDocTopicItem() != null) {
				logger.debug(cluster.getName() + " has topics: ");
				DocTopicItem docTopicItem = cluster.getDocTopicItem();
				Collections.sort(docTopicItem.getTopics(),TOPIC_PROPORTION_ORDER);
				for (TopicItem topicItem : docTopicItem.getTopics()) {
					logger.debug("\t" + topicItem);
				}
			}
		}
		
		Map<String,Set<String>> clusterSmellMap = new HashMap<>();
		
		detectBco(detectedSmells, clusters, clusterSmellMap);
		detectSpfNew(clusters, clusterSmellMap, detectedSmells);
		
		Map<String, Set<String>> depMap = ClusterUtil.buildDependenciesMap(Config.getDepsRsfFilename());
		
		StringGraph clusterGraph = ClusterUtil.buildClusterGraphUsingDepMap(depMap,clusters);
		System.out.print("");
		
		SimpleDirectedGraph<String, DefaultEdge> directedGraph = ClusterUtil.buildConcernClustersDiGraph(
				clusters, clusterGraph);
		
		detectBdc(detectedSmells, clusters, clusterSmellMap, directedGraph);
        
        detectBuo(detectedSmells, clusters, clusterSmellMap, directedGraph);
        
        for (String clusterName : clusterSmellMap.keySet()) {
        	Set<String> smellList = clusterSmellMap.get(clusterName);
        	logger.debug(clusterName + " has smells " + Joiner.on(",").join(smellList));
        }
        
        Map<String, Set<String>> smellClustersMap = buildSmellToClustersMap(clusterSmellMap);
        
        for (Entry<String,Set<String>> entry : smellClustersMap.entrySet()) {
        	logger.debug(entry.getKey() + " : " + entry.getValue());
        }
		
		for (Smell smell : detectedSmells) {
			logger.debug(SmellUtil.getSmellAbbreviation(smell) + " " + smell);
		}

		serializeDetectedSmells(detectedSmellsFilename, detectedSmells);
	}
	
	public static void runStructuralDetectionAlgs(String detectedSmellsFilename) {
		Set<Smell> detectedSmells = new LinkedHashSet<>();
		System.out.println("Reading in clusters file: " + Config.getSmellClustersFile());
		Set<ConcernCluster> clusters = ConcernClusterRsf.extractConcernClustersFromRsfFile(Config.getSmellClustersFile());
		
		boolean showBuiltClusters = true;
		if (showBuiltClusters) {
			logger.debug("Found and built clusters:");
			for (ConcernCluster cluster : clusters) {
				logger.debug(cluster.getName());
			}
		}
		
		Map<String,Set<String>> clusterSmellMap = new HashMap<>();
		
		Map<String, Set<String>> depMap = ClusterUtil.buildDependenciesMap(Config.getDepsRsfFilename());
		
		StringGraph clusterGraph = ClusterUtil.buildClusterGraphUsingDepMap(depMap,clusters);
		System.out.print("");
		
		SimpleDirectedGraph<String, DefaultEdge> directedGraph = ClusterUtil.buildConcernClustersDiGraph(
				clusters, clusterGraph);
		
		detectBdc(detectedSmells, clusters, clusterSmellMap, directedGraph);
        
        detectBuo(detectedSmells, clusters, clusterSmellMap, directedGraph);
        
        for (String clusterName : clusterSmellMap.keySet()) {
        	Set<String> smellList = clusterSmellMap.get(clusterName);
        	logger.debug(clusterName + " has smells " + Joiner.on(",").join(smellList));
        }
        
        Map<String, Set<String>> smellClustersMap = buildSmellToClustersMap(clusterSmellMap);
        
        for (Entry<String,Set<String>> entry : smellClustersMap.entrySet()) {
        	logger.debug(entry.getKey() + " : " + entry.getValue());
        }
		
		for (Smell smell : detectedSmells) {
			logger.debug(SmellUtil.getSmellAbbreviation(smell) + " " + smell);
		}

		serializeDetectedSmells(detectedSmellsFilename, detectedSmells);
	}

	private static void serializeDetectedSmells(String detectedSmellsFilename,
			Set<Smell> detectedSmells) {
		try (PrintWriter writer = new PrintWriter(detectedSmellsFilename, StandardCharsets.UTF_8)) {
			XStream xstream = new XStream();
			String xml = xstream.toXML(detectedSmells);
	    writer.println(xml);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static Map<String, Set<String>> buildSmellToClustersMap(
			Map<String, Set<String>> clusterSmellMap) {
		// create smell to clusters map
        Map<String,Set<String>> smellClustersMap = new HashMap<>();
        for (String clusterName : clusterSmellMap.keySet()) {
        	Set<String> smellList = clusterSmellMap.get(clusterName);
        	for (String smell : smellList) {
        		if (smellClustersMap.containsKey(smell)) {
        			Set<String> mClusters = smellClustersMap.get(smell);
        			mClusters.add(clusterName);
        		}
        		else {
        			Set<String> mClusters = new HashSet<>();
        			mClusters.add(clusterName);
        			smellClustersMap.put(smell, mClusters);
        		}
        	}
        }
		return smellClustersMap;
	}

	protected static void detectBuo(Set<Smell> detectedSmells,
			Set<ConcernCluster> clusters,
			Map<String, Set<String>> clusterSmellMap,
			SimpleDirectedGraph<String, DefaultEdge> directedGraph) {
		Set<String> vertices = directedGraph.vertexSet();
        
        logger.debug("Computing the in and out degress of each vertex");
        List<Double> inDegrees = new ArrayList<>();
        List<Double> outDegrees = new ArrayList<>();
        for (String vertex : vertices) {
        	boolean analyzeThisVertex = true;
        	if (Config.getClusterStartsWith() != null) {
        		analyzeThisVertex = vertex.startsWith(Config.getClusterStartsWith());
        	}
        	if (analyzeThisVertex) {
				logger.debug("\t" + vertex);
				int inDegree = directedGraph.inDegreeOf(vertex);
				int outDegree = directedGraph.outDegreeOf(vertex);
				logger.debug("\t\t in degree: "
						+ inDegree);
				logger.debug("\t\t out degree: "
						+ outDegree);
				inDegrees.add((double)inDegree);
				outDegrees.add((double)outDegree);
			}
        }
        
        double[] inAndOutDegreesArray = new double[inDegrees.size()];
        for (int i=0;i<inDegrees.size();i++) {
        	double inPlusOutAtI = inDegrees.get(i) + outDegrees.get(i);
        	inAndOutDegreesArray[i] = inPlusOutAtI;
        }
        
        
        double[] inDegreesArray = Doubles.toArray(inDegrees);
        double[] outDegreesArray = Doubles.toArray(outDegrees);
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
        	boolean analyzeThisVertex = true;
        	if (Config.getClusterStartsWith() != null) {
        		analyzeThisVertex = vertex.startsWith(Config.getClusterStartsWith());
        	}
        	if (analyzeThisVertex) {
				int inDegree = directedGraph.inDegreeOf(vertex);
				int outDegree = directedGraph.outDegreeOf(vertex);
				if (inDegree > meanInDegrees + stdDevFactor*stdDevInDegrees) {
					logger.debug("\t\t" + vertex + " has brick use overload for in degrees");
					logger.debug("\t\t in degree: "
							+ inDegree);
					updateSmellMap(clusterSmellMap, vertex, "buo");
					addDetectedBuoSmell(detectedSmells, clusters, vertex);
				}
				if (outDegree > meanOutDegrees + stdDevFactor*stdDevOutDegrees) {
					logger.debug("\t\t" + vertex + " has brick use overload for out degrees");
					logger.debug("\t\t out degree: "
							+ outDegree);
					updateSmellMap(clusterSmellMap, vertex, "buo");
					addDetectedBuoSmell(detectedSmells, clusters, vertex);
				}
				if (inDegree + outDegree > 
					meanInDegrees + meanOutDegrees + 
					stdDevFactor*stdDevOutDegrees + stdDevFactor*stdDevInDegrees) {
					logger.debug("\t\t" + vertex + " has brick use overload for both in and out degrees");
					logger.debug("\t\t in degree: "	+ inDegree);
					logger.debug("\t\t out degree: " + outDegree);
					updateSmellMap(clusterSmellMap, vertex, "buo");
					addDetectedBuoSmell(detectedSmells, clusters, vertex);
				}
				int inPlusOutDegree = inDegree + outDegree;
				if ( inPlusOutDegree > meanInAndOutDegrees + stdDevFactor*stdDevInAndOutDegrees ) {
					logger.debug("\t\t" + vertex + " has brick use overload for in plus out degrees");
					logger.debug("\t\t in plus out degrees: "
							+ inPlusOutDegree);
					updateSmellMap(clusterSmellMap, vertex, "buo");
					addDetectedBuoSmell(detectedSmells, clusters, vertex);
				}
				
				
			}
        }
	}

	protected static void detectBdc(Set<Smell> detectedSmells,
			Set<ConcernCluster> clusters,
			Map<String, Set<String>> clusterSmellMap,
			SimpleDirectedGraph<String, DefaultEdge> directedGraph) {
		System.out.println("Finding cycles...");
		CycleDetector cycleDetector = new CycleDetector(directedGraph);
		Set<String> cycleSet = cycleDetector.findCycles();
		logger.debug("Printing the cycle set, i.e., the set of all vertices" + 
			"which participate in at least one cycle in this graph...");
		logger.debug(cycleSet);
		
		StrongConnectivityInspector inspector = new StrongConnectivityInspector(directedGraph);
		List<Set<String>> connectedSets = inspector.stronglyConnectedSets();
		logger.debug("Printing the strongly connected sets of the graph....");
		logger.debug(Joiner.on("\n").join(connectedSets));
		
		int relevantConnectedSetCount = 0;
		Set<Set<String>> bdcConnectedSets = new HashSet<>();
		for (Set<String> connectedSet : connectedSets) {
			if (connectedSet.size() > 2) {
				logger.debug("Counting this strongly connected component set as relevant");
				logger.debug(connectedSet);
				relevantConnectedSetCount++;
				for (String clusterName : connectedSet) {
					updateSmellMap(clusterSmellMap, clusterName, "bdc");
				}
				logger.debug("scc size: " + connectedSet.size());
				bdcConnectedSets.add(connectedSet);
			}
		}
		
		for (Set<String> bdcConnectedSet : bdcConnectedSets) {
			Smell bdc = new BdcSmell();
			Set<ConcernCluster> bdcClusters = new HashSet<>();
			for (String clusterName : bdcConnectedSet) {
				ConcernCluster cluster = getMatchingCluster(clusterName,clusters);
				assert cluster != null : "No matching cluster found for " + clusterName;
				bdcClusters.add(cluster);
				
			}
			bdc.clusters = new HashSet<ConcernCluster>(bdcClusters);
			detectedSmells.add(bdc);
		}
		
		logger.debug("Number of strongly connected components: " + relevantConnectedSetCount);
	}

	protected static StandardDeviation detectBco(Set<Smell> detectedSmells,
			Set<ConcernCluster> clusters,
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
		
		int[] intConcernCountValues = ArrayUtils.toPrimitive(concernCountMap.values().toArray(new Integer[0]) );
		double[] doubleConcernCountValues = new double[intConcernCountValues.length];
		for (int i=0;i<intConcernCountValues.length;i++) {
			doubleConcernCountValues[i] = intConcernCountValues[i];
		}
		double concernCountMean = StatUtils.mean(doubleConcernCountValues);
		double concernCountStdDev = stdDev.evaluate(doubleConcernCountValues);
		logger.debug("relevant concern count mean: " + concernCountMean);
		logger.debug("relevant concern count standard deviation: " + concernCountStdDev);
		
		
		for (ConcernCluster cluster : concernCountMap.keySet()) {
			int concernCount = concernCountMap.get(cluster);
			if (concernCount > concernCountMean + concernCountStdDev) {
				logger.debug("\t" + cluster.getName() + " has brick concern overload.");
				
				Smell bco = new BcoSmell();
				bco.clusters.add(cluster);
				detectedSmells.add(bco);
				
				updateSmellMap(clusterSmellMap, cluster.getName(), "bco");
			}
		}
		return stdDev;
	}

	private static void buildConcernClustersFromConfigTopicsFile(Set<ConcernCluster> clusters) {
		try {
			if (tmeMethod == TopicModelExtractionMethod.VAR_MALLET_FILE) {
				docTopics = new DocTopics(Config.getMalletDocTopicsFilename());
				for (ConcernCluster cluster : clusters) {
					logger.debug("Building doctopics for " + cluster.getName());
					for (String entity : cluster.getEntities()) {
						if (cluster.getDocTopicItem() == null) {
							DocTopicItem newDocTopicItem = null;
							if (Config.getSelectedLanguage() == Config.Language.java) {
								newDocTopicItem = setDocTopicItemForJavaFromMalletFile(entity);
							} else if (Config.getSelectedLanguage() == Config.Language.c) {
								newDocTopicItem = docTopics
										.getDocTopicItemForC(entity);
							} else {
								newDocTopicItem = setDocTopicItemForJavaFromMalletFile(entity);
							}
							cluster.setDocTopicItem(newDocTopicItem);
						} else {
							DocTopicItem entityDocTopicItem = null;
							DocTopicItem mergedDocTopicItem = null;
							if (Config.getSelectedLanguage() == Config.Language.java) {
								entityDocTopicItem = setDocTopicItemForJavaFromMalletFile(entity);
							} else if (Config.getSelectedLanguage() == Config.Language.c) {
								entityDocTopicItem = docTopics
										.getDocTopicItemForC(entity);
							} else {
								entityDocTopicItem = setDocTopicItemForJavaFromMalletFile(entity);
							}

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
			else if (tmeMethod == TopicModelExtractionMethod.MALLET_API) {
				for (ConcernCluster cluster : clusters) {
					logger.debug("Building doctopics for " + cluster.getName());
					for (String entity : cluster.getEntities()) {
						if (cluster.getDocTopicItem() == null) {
							DocTopicItem newDocTopicItem = null;
							if (Config.getSelectedLanguage() == Config.Language.java) {
								newDocTopicItem = setDocTopicItemForJavaFromMalletApi(entity);
							}
							cluster.setDocTopicItem(newDocTopicItem);
						} else {
							DocTopicItem entityDocTopicItem = null;
							DocTopicItem mergedDocTopicItem = null;
							if (Config.getSelectedLanguage() == Config.Language.java) {
								entityDocTopicItem = setDocTopicItemForJavaFromMalletApi(entity);
							} 

							try {
								mergedDocTopicItem = TopicUtil.mergeDocTopicItems(
									cluster.getDocTopicItem(),
									entityDocTopicItem);
							} catch (UnmatchingDocTopicItemsException e) {
								e.printStackTrace(); //TODO handle it
							}
							cluster.setDocTopicItem(mergedDocTopicItem);
						}
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private static void addDetectedBuoSmell(Set<Smell> detectedSmells,
			Set<ConcernCluster> clusters, String vertex) {
		Smell buo = new BuoSmell();
		buo.clusters.add(getMatchingCluster(vertex,clusters));
		detectedSmells.add(buo);
	}
	
	private static ConcernCluster getMatchingCluster(String clusterName,
			Set<ConcernCluster> clusters) {
		for (ConcernCluster cluster : clusters) {
			if (cluster.getName().equals(clusterName)) {
				return cluster;
			}
		}
		return null;
	}

	private static DocTopicItem setDocTopicItemForJavaFromMalletFile(String entity) {
		DocTopicItem newDocTopicItem;
		String docTopicName = TopicUtil.convertJavaClassWithPackageNameToDocTopicName( entity );
		newDocTopicItem = docTopics.getDocTopicItemForJava(docTopicName);
		return newDocTopicItem;
	}
	
	private static DocTopicItem setDocTopicItemForJavaFromMalletApi(String entity) {
		DocTopicItem newDocTopicItem;
		newDocTopicItem = docTopics.getDocTopicItemForJava(entity);
		return newDocTopicItem;
	}

	// #region SCATTERED PARASITIC FUNCTIONALITY ---------------------------------
	/**
	 * Detects Scattered Parasitic Functionality smell instances.
	 * 
	 * @param clusters Input clusters to run detection algorithm on.
	 * @param clusterSmellsMap Mapping of smells per cluster. Altered.
	 * @param detectedSmells Set of detected smells. Altered.
	 */
	protected static void detectSpfNew(
			Set<ConcernCluster> clusters,
			Map<String, Set<String>> clusterSmellsMap,
			Set<Smell> detectedSmells) {
		logger.info("Finding scattered parasitic functionality instances...");

		// Setting thresholds for detection algorithm
		Map<Integer, Integer> topicNumCountMap = new HashMap<>();
		Map<Integer, Set<ConcernCluster>> scatteredTopicToClustersMap 
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
			Set<ConcernCluster> clustersWithScatteredTopics = 
				scatteredTopicToClustersMap.get(topicNum);
			Set<ConcernCluster> affectedClusters = new HashSet<>();

			// Filter out all clusters that don't have topics
			Set<ConcernCluster> validScatteredClusters = 
				new HashSet<>(clustersWithScatteredTopics);
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
			
			Smell spf = new SpfSmell(topicNum);
			spf.clusters = new HashSet<>(affectedClusters);
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
	private static void mapTopicItemsToCluster(
			Set<ConcernCluster> inputClusters,
			Map<Integer, Integer> topicNumCountMap,
			Map<Integer, Set<ConcernCluster>> scatteredTopicToClustersMap) {
		// Filter out all clusters that don't have topics
		Set<ConcernCluster> validClusters = new HashSet<>(inputClusters);
		validClusters.removeIf((ConcernCluster cluster) -> 
			cluster.getDocTopicItem() == null);
		
		for (ConcernCluster cluster : validClusters) {
			// Filter out all topic items that aren't smelly
			DocTopicItem dti = cluster.getDocTopicItem();
			List<TopicItem> smellyTopicItems = new ArrayList<>(dti.getTopics());
			smellyTopicItems.removeIf((TopicItem ti) -> 
				ti.getProportion() < scatteredConcernThreshold);

			for (TopicItem ti : dti.getTopics()) {
				// count the number of times the topic appears
				topicNumCountMap.compute(ti.getTopicNum(), (k, v) ->	(v == null) ? 1 : v++);
				
				// map cluster to the related topicItem
				scatteredTopicToClustersMap.putIfAbsent(ti.getTopicNum(), new HashSet<>());
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
	private static double computeSPFSignificanceThreshold(
			double[] topicCounts, Map<Integer, Integer> topicNumCountMap,
			Map<Integer, Set<ConcernCluster>> scatteredTopicToClustersMap){
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
		for (Map.Entry<Integer, Integer> entry : topicNumCountMap.entrySet()) {
			logger.debug(entry.getKey() + " : " + entry.getValue() );
		}
		logger.debug("topic num : clusters with topic");
		for (Map.Entry<Integer, Set<ConcernCluster>> entry 
				: scatteredTopicToClustersMap.entrySet()) {
			logger.debug(entry.getKey() + " : " + entry.getValue() );
		}

		return topicCountMean + topicCountStdDev;
	}
	// #endregion SCATTERED PARASITIC FUNCTIONALITY ------------------------------

	/**
	 * Adds a smell to the given cluster in clusterSmellMap.
	 * 
	 * @param clusterSmellMap Map of all clusters and their sets of smells.
	 * @param clusterName Name of the cluster to add a smell to.
	 * @param smellAbrv Abbreviation mell to add to the cluster.
	 */
	protected static void updateSmellMap(
			Map<String, Set<String>> clusterSmellMap,
			String clusterName, String smellAbrv) {
		Set<String> smellList = clusterSmellMap.get(clusterName);
		if(smellList == null)
			smellList = new HashSet<>();
		smellList.add(smellAbrv);
		clusterSmellMap.putIfAbsent(clusterName, smellList);
	}
}