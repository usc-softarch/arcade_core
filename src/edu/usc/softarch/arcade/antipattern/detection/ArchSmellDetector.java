package edu.usc.softarch.arcade.antipattern.detection;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
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

import edu.usc.softarch.arcade.classgraphs.StringEdge;
import edu.usc.softarch.arcade.clustering.StringGraph;
import edu.usc.softarch.arcade.clustering.util.ClusterUtil;
import edu.usc.softarch.arcade.config.Config;
import edu.usc.softarch.arcade.facts.ConcernCluster;
import edu.usc.softarch.arcade.facts.driver.ConcernClusterRsf;
import edu.usc.softarch.arcade.facts.driver.RsfReader;
import edu.usc.softarch.arcade.topics.DocTopicItem;
import edu.usc.softarch.arcade.topics.DocTopics;
import edu.usc.softarch.arcade.topics.TopicItem;
import edu.usc.softarch.arcade.topics.TopicModelExtractionMethod;
import edu.usc.softarch.arcade.topics.TopicUtil;

public class ArchSmellDetector {

	static Logger logger = Logger.getLogger(ArchSmellDetector.class);
	public static TopicModelExtractionMethod tmeMethod = TopicModelExtractionMethod.VAR_MALLET_FILE;
	
	
	static final Comparator<TopicItem> TOPIC_PROPORTION_ORDER = new Comparator<TopicItem>() {
		public int compare(TopicItem t1, TopicItem t2) {
			Double prop1 = t1.proportion;
			Double prop2 = t2.proportion;
			return prop1.compareTo(prop2);
		}
	};

	public static DocTopics docTopics;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		PropertyConfigurator.configure(Config.getLoggingConfigFilename());
		
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
		Set<Smell> detectedSmells = new LinkedHashSet<Smell>();
		System.out.println("Reading in clusters file: " + Config.getSmellClustersFile());
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
				Collections.sort(docTopicItem.topics,TOPIC_PROPORTION_ORDER);
				for (TopicItem topicItem : docTopicItem.topics) {
					logger.debug("\t" + topicItem);
				}
			}
		}
		
		Map<String,Set<String>> clusterSmellMap = new HashMap<String,Set<String>>();
		
		detectBco(detectedSmells, clusters,
				clusterSmellMap);
		
		//detectSpfOld(clusters, clusterSmellMap);
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
        
        //buildSmellToClassesMap(clusters, smellClustersMap);
		
		for (Smell smell : detectedSmells) {
			logger.debug(SmellUtil.getSmellAbbreviation(smell) + " " + smell);
		}

		serializeDetectedSmells(detectedSmellsFilename, detectedSmells);
	}
	
	private static void runStructuralDetectionAlgs(String detectedSmellsFilename) {
		Set<Smell> detectedSmells = new LinkedHashSet<Smell>();
		System.out.println("Reading in clusters file: " + Config.getSmellClustersFile());
		Set<ConcernCluster> clusters = ConcernClusterRsf.extractConcernClustersFromRsfFile(Config.getSmellClustersFile());
		
		boolean showBuiltClusters = true;
		if (showBuiltClusters) {
			logger.debug("Found and built clusters:");
			for (ConcernCluster cluster : clusters) {
				logger.debug(cluster.getName());
			}
		}
		
		Map<String,Set<String>> clusterSmellMap = new HashMap<String,Set<String>>();
		
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
        
        //buildSmellToClassesMap(clusters, smellClustersMap);
		
		for (Smell smell : detectedSmells) {
			logger.debug(SmellUtil.getSmellAbbreviation(smell) + " " + smell);
		}

		serializeDetectedSmells(detectedSmellsFilename, detectedSmells);
	}

	private static void serializeDetectedSmells(String detectedSmellsFilename,
			Set<Smell> detectedSmells) {
		try {
			PrintWriter writer;
			writer = new PrintWriter(detectedSmellsFilename, "UTF-8");
			
			XStream xstream = new XStream();
			String xml = xstream.toXML(detectedSmells);
			
	        writer.println(xml);
	        
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void buildSmellToClassesMap(Set<ConcernCluster> clusters,
			Map<String, Set<String>> smellClustersMap) {
		// create smell to classes map
        Map<String,Set<String>> smellClassesMap = new HashMap<String,Set<String>>();
        for (Entry<String,Set<String>> entry : smellClustersMap.entrySet()) {
        	String smell = entry.getKey();
        	Set<String> mClusters = entry.getValue();
        	for (String clusterName : mClusters) {
        		ConcernCluster cluster = findCluster(clusters,clusterName);
        		assert cluster != null : "Could not find cluster " + clusterName;
        		if (smellClassesMap.containsKey(smell)) {
        			Set<String> classes = smellClassesMap.get(smell);
        			classes.addAll(cluster.getEntities());
        		}
        		else {
        			Set<String> classes = new HashSet<String>();
        			classes.addAll(cluster.getEntities());
        			smellClassesMap.put(smell, classes);
        		}
        	}
        }
	}

	private static Map<String, Set<String>> buildSmellToClustersMap(
			Map<String, Set<String>> clusterSmellMap) {
		// create smell to clusters map
        Map<String,Set<String>> smellClustersMap = new HashMap<String,Set<String>>();
        for (String clusterName : clusterSmellMap.keySet()) {
        	Set<String> smellList = clusterSmellMap.get(clusterName);
        	for (String smell : smellList) {
        		if (smellClustersMap.containsKey(smell)) {
        			Set<String> mClusters = smellClustersMap.get(smell);
        			mClusters.add(clusterName);
        		}
        		else {
        			Set<String> mClusters = new HashSet<String>();
        			mClusters.add(clusterName);
        			smellClustersMap.put(smell, mClusters);
        		}
        	}
        }
		return smellClustersMap;
	}

	private static void detectBuo(Set<Smell> detectedSmells,
			Set<ConcernCluster> clusters,
			Map<String, Set<String>> clusterSmellMap,
			SimpleDirectedGraph<String, DefaultEdge> directedGraph) {
		Set<String> vertices = directedGraph.vertexSet();
        
        logger.debug("Computing the in and out degress of each vertex");
        List<Double> inDegrees = new ArrayList<Double>();
        List<Double> outDegrees = new ArrayList<Double>();
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

	private static void detectBdc(Set<Smell> detectedSmells,
			Set<ConcernCluster> clusters,
			Map<String, Set<String>> clusterSmellMap,
			SimpleDirectedGraph<String, DefaultEdge> directedGraph) {
		System.out.println("Finding cycles...");
		CycleDetector cycleDetector = new CycleDetector(directedGraph);
		Set<String> cycleSet = cycleDetector.findCycles();
		//logger.debug("Printing the cycle set, i.e., the set of all vertices which participate in at least one cycle in this graph...");
		//logger.debug(cycleSet);
		
		
        StrongConnectivityInspector inspector = new StrongConnectivityInspector(directedGraph);
        List<Set<String>> connectedSets = inspector.stronglyConnectedSets();
        //logger.debug("Printing the strongly connected sets of the graph....");
        //logger.debug(Joiner.on("\n").join(connectedSets));
        
        int relevantConnectedSetCount = 0;
        Set<Set<String>> bdcConnectedSets = new HashSet<Set<String>>();
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
        	Set<ConcernCluster> bdcClusters = new HashSet<ConcernCluster>();
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

	private static StandardDeviation detectBco(Set<Smell> detectedSmells,
			Set<ConcernCluster> clusters,
			Map<String, Set<String>> clusterSmellMap) {
		System.out.println("Finding brick concern overload instances...");
		double concernOverloadTopicThreshold = .10;
		//double concernCountThreshold = 2;
		Map<ConcernCluster,Integer> concernCountMap = new HashMap<ConcernCluster,Integer>(); 
		for (ConcernCluster cluster : clusters) {
			if (cluster.getDocTopicItem() != null) {
				logger.debug("Have doc-topics for " + cluster.getName());
				DocTopicItem docTopicItem = cluster.getDocTopicItem();
				int concernCount = 0;
				for (TopicItem topicItem : docTopicItem.topics)  {
					if (topicItem.proportion > concernOverloadTopicThreshold) {
						logger.debug("\t" + cluster.getName() + " is beyond concern overload threshold for " + topicItem);
						concernCount++;
					}
				}
				concernCountMap.put(cluster, concernCount);
			}
		}
		
		
		StandardDeviation stdDev = new StandardDeviation();
		
		int[] intConcernCountValues = ArrayUtils.toPrimitive( (Integer[])concernCountMap.values().toArray(new Integer[0]) );
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

							mergedDocTopicItem = TopicUtil.mergeDocTopicItems(
									cluster.getDocTopicItem(),
									entityDocTopicItem);
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

							mergedDocTopicItem = TopicUtil.mergeDocTopicItems(
									cluster.getDocTopicItem(),
									entityDocTopicItem);
							cluster.setDocTopicItem(mergedDocTopicItem);
						}
					}
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
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
	
	private static ConcernCluster findCluster(
			Set<ConcernCluster> clusters, String clusterName) {
		for (ConcernCluster cluster : clusters) {
			if (cluster.getName().equals(clusterName))
				return cluster;
		}
		return null;
	}

	private static void detectSpfNew(
			Set<ConcernCluster> clusters,
			Map<String, Set<String>> clusterSmellsMap,
			Set<Smell> detectedSmells) {
		System.out.println("Finding scattered parasitic functionality instances...");
		double scatteredConcernThreshold = .20;
		double parasiticConcernThreshold = scatteredConcernThreshold;
		
		Map<Integer,Integer> topicNumCountMap = new HashMap<Integer,Integer>();
		Map<Integer, Set<ConcernCluster>> scatteredTopicToClustersMap = new HashMap<Integer, Set<ConcernCluster>>();
		
		
		for (ConcernCluster cluster : clusters) {
			if (cluster.getDocTopicItem() != null) {
				DocTopicItem dti = cluster.getDocTopicItem();
				for (TopicItem ti : dti.topics) {
					if (ti.proportion >= scatteredConcernThreshold) {
						// count the number of times the topic appears
						if (topicNumCountMap.containsKey(ti.topicNum)) {
							int topicNumCount = topicNumCountMap
									.get(ti.topicNum);
							topicNumCount++;
							topicNumCountMap.put(ti.topicNum, topicNumCount);
						} else {
							topicNumCountMap.put(ti.topicNum, 1);
						}
						
						// determine which clusters have each topic
						if (scatteredTopicToClustersMap.containsKey(ti.topicNum)) {
							Set<ConcernCluster> clustersWithTopic = scatteredTopicToClustersMap.get(ti.topicNum);
							clustersWithTopic.add(cluster);

						}
						else {
							Set<ConcernCluster> clustersWithTopic = new HashSet<ConcernCluster>();
							clustersWithTopic.add(cluster);
							scatteredTopicToClustersMap.put(ti.topicNum, clustersWithTopic);

						}
					}
				}
			}
		}
		
		double[] topicCounts = new double[topicNumCountMap.keySet().size()];
		double topicCountMean = 0;
		double topicCountStdDev = 0;

		int topicNumCounter = 0;
		for (int topicNum : topicNumCountMap.values()) {
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
		for (Map.Entry<Integer, Set<ConcernCluster>> entry : scatteredTopicToClustersMap.entrySet()) {
			logger.debug(entry.getKey() + " : " + entry.getValue() );
		}
		
		for (int topicNum : topicNumCountMap.keySet()) {
			int topicCount = topicNumCountMap.get(topicNum);
			if (topicCount > topicCountMean + topicCountStdDev) {
				Set<ConcernCluster> clustersWithScatteredTopics = scatteredTopicToClustersMap.get(topicNum);
				
				Set<ConcernCluster> affectedClusters = new HashSet<ConcernCluster>();
				for (ConcernCluster cluster : clustersWithScatteredTopics) {
					if (cluster.getDocTopicItem() != null) {
						DocTopicItem dti = cluster.getDocTopicItem();
						for (TopicItem ti : dti.topics) {
							if (ti.topicNum != topicNum) {
								if (ti.proportion >= parasiticConcernThreshold) {
									logger.debug(cluster.getName() + " has spf with scattered concern " + topicNum);
									
									
									if (clusterSmellsMap.containsKey(cluster.getName())) {
										Set<String> smells = clusterSmellsMap.get(cluster.getName());
										smells.add("spf");
									}
									else {
										Set<String> smells = new HashSet<String>();
										smells.add("spf");
										clusterSmellsMap.put(cluster.getName(), smells);
									}
									
									affectedClusters.add(cluster);
								}
							}
						}
					}
				}
				
				Smell spf = new SpfSmell(topicNum);
				spf.clusters = new HashSet<ConcernCluster>(affectedClusters);
				detectedSmells.add(spf);
				
			}
		}
		
	}

	private static void updateSmellMap(
			Map<String, Set<String>> clusterSmellMap,
			String clusterName, String smellAbrv) {
		Set<String> smellList = null;
		if (clusterSmellMap.containsKey(clusterName)) {
			smellList = clusterSmellMap.get(clusterName);
			smellList.add(smellAbrv); 	
			//clusterSmellMap.put(clusterName, smellList);
		}
		else {
			smellList = new HashSet<String>();
			smellList.add(smellAbrv);
			clusterSmellMap.put(clusterName, smellList);
		}
	}


}
