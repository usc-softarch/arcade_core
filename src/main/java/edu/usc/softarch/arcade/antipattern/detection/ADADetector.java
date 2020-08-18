package edu.usc.softarch.arcade.antipattern.detection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jgrapht.alg.CycleDetector;
import org.jgrapht.alg.StrongConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import edu.usc.softarch.arcade.callgraph.MyCallGraph;
import edu.usc.softarch.arcade.callgraph.MyClass;
import edu.usc.softarch.arcade.callgraph.MyMethod;
import edu.usc.softarch.arcade.classgraphs.StringEdge;
import edu.usc.softarch.arcade.clustering.Cluster;
import edu.usc.softarch.arcade.clustering.StringGraph;
import edu.usc.softarch.arcade.clustering.util.ClusterUtil;
import edu.usc.softarch.arcade.config.Config;
import edu.usc.softarch.arcade.config.ConfigUtil;
import edu.usc.softarch.arcade.smellarchgraph.SmellArchGraph;
import edu.usc.softarch.arcade.topics.DistributionSizeMismatchException;
import edu.usc.softarch.arcade.topics.DocTopicItem;
import edu.usc.softarch.arcade.topics.DocTopics;
import edu.usc.softarch.arcade.topics.StringPreProcessor;
import edu.usc.softarch.arcade.topics.TopicItem;
import edu.usc.softarch.arcade.topics.TopicKey;
import edu.usc.softarch.arcade.topics.TopicKeySet;
import edu.usc.softarch.arcade.topics.TopicUtil;
import edu.usc.softarch.arcade.topics.WordTopicCounts;
import edu.usc.softarch.arcade.topics.WordTopicItem;

/**
 * @author joshua
 */
public class ADADetector {
	// #region DRIVERS -----------------------------------------------------------
	public static void runSmellDetectionAlgorithms(
			List<Cluster> splitClusters) throws IOException,
			ClassNotFoundException,
			ParserConfigurationException, SAXException, TransformerException {
		System.out.println("In "
				+ Thread.currentThread().getStackTrace()[1].getClassName()
				+ "."
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ ",");
		ClusterUtil.generateLeafClusters(splitClusters);

		generateTopicsForSplitClusters(splitClusters);
		printTopicsForSplitClusters(splitClusters);

		Map<String, MyClass> classesWithUsedMethods = (Map<String, MyClass>) deserializeHashMap(Config
				.getClassesWithUsedMethodsFilename());
		Map<String, MyMethod> unusedMethods = (Map<String, MyMethod>) deserializeHashMap(Config
				.getUnusedMethodsFilename());

		System.out.println("Printing classes with used methods...");
		printClassesWithUsedMethods(classesWithUsedMethods);
		System.out.println("Printing unused methods...");
		printUnusedMethods(unusedMethods);

		determineInterfacesForClusters(splitClusters, classesWithUsedMethods);
		printInterfacesOfClusters(splitClusters);

		StringGraph clusterGraph = ClusterUtil
				.generateClusterGraph(splitClusters);
		SmellArchGraph smellArchGraph = ClusterUtil
				.generateSmellArchGraph(splitClusters);

		System.out.println("Resulting ClusterGraph...");
		System.out.println(clusterGraph);
		System.out.println();
		System.out.println("Resulting SmellArchGraph...");
		System.out.println(smellArchGraph);

		writeOutGraphsAndSmellArchToFiles(splitClusters, clusterGraph,
				smellArchGraph);
		
		Set<TopicKey> topicKeys = TopicUtil.getTopicKeyListForCurrProj().getSet();
		ClusterUtil.readInSmellArchFromXML(
				Config.getSpecifiedSmallArchFromXML(), splitClusters, topicKeys);
		
		ClusterUtil.classifyClustersBasedOnTopicTypes(splitClusters);
		
		ClusterUtil.writeOutSpecifiedSmellArchToXML(splitClusters,topicKeys);
		
		for (Cluster cluster1 : splitClusters) {
			for (Cluster cluster2 : splitClusters) {
				System.out.println("Computing JS divergence for " + cluster1 + " and " + cluster2);
				if (!(cluster1.getDocTopicItem() == null || cluster2.getDocTopicItem() == null)) {
					try {
						TopicUtil.jsDivergence(cluster1.getDocTopicItem(),cluster2.getDocTopicItem());
					} catch (DistributionSizeMismatchException e) {
						e.printStackTrace(); //TODO handle it
					}
				}
			}
		}

		MyCallGraph myCallGraph = deserializeMyCallGraph();
		
		writeMethodInfoToXML(splitClusters,myCallGraph);

		int ambiguousInterfaceCount = 0;
		ambiguousInterfaceCount = determineAmbiguousInterface(splitClusters,
				myCallGraph, ambiguousInterfaceCount);

		TopicKeySet topicKeySet = TopicUtil.getTopicKeyListForCurrProj();
		WordTopicCounts wordTopicCounts = TopicUtil
				.getWordTopicCountsForCurrProj();

		System.out
				.println("Computing word-topic probabilities for all words and topics...");
		computeAndShowWordTopicProbabilitiesForAllWordsAndTopics(topicKeySet,
				wordTopicCounts);

		System.out.println("Computing word-topic probabilities and specificity type for methods...");
		computeWordTopicProbabilitiesAndSpecifityTypesForMethods(splitClusters,
				topicKeySet, wordTopicCounts);
		
		int connectorInterfaceImplCount = 0;
		connectorInterfaceImplCount = findConnectorInterfaceImplementationSmells(
				splitClusters, connectorInterfaceImplCount);

		int unacceptablyHighConnectorConcernCount = 0;
		System.out.println("Finding unacceptably high connector concern...");
		unacceptablyHighConnectorConcernCount = findUnacceptablyHighConnectorConcernSmells(
				splitClusters, unacceptablyHighConnectorConcernCount);
		
		int scatteredParasiticFunctionalityCount = 0;
		System.out.println("Finding instances of scattered parasitic functionality...");
		scatteredParasiticFunctionalityCount = findScatteredParasiticFunctionalitySmells(splitClusters);
		
		int procCallBasedExtraneousConnectorCount = 0;
		System.out.println("Finding procedure call-based extraneous connector smells...");
		procCallBasedExtraneousConnectorCount = findProcCalBasedExtraneousConnectors(splitClusters,clusterGraph,procCallBasedExtraneousConnectorCount);
		
		int brickConcernOverloadCount = 0;
		System.out.println("Finding Brick Concern Overload smells...");
		brickConcernOverloadCount = findBrickConcernOverloadSmells(splitClusters,brickConcernOverloadCount);
		
		int unusedInterfaceCount = 0;
		System.out.println("Finding Unused Interface smells...");
		unusedInterfaceCount = findUnusedInterfaceSmells(unusedMethods,unusedInterfaceCount);
		
		int unstableBrickDependencyCount = 0;
		System.out.println("Finding Unstable Brick Dependencies smells...");
		Map<String,Double> brickStabilityMap = new HashMap<>();
		Map<String,Integer> brickFanInMap = new HashMap<>();
		Map<String,Integer> brickFanOutMap = new HashMap<>();
		for (Cluster firstCluster : splitClusters) {
			System.out.println("Current cluster: " + firstCluster);
			int fanOut = 0;
			int fanIn = 0;
			for (StringEdge stringEdge : clusterGraph.edges ) {
				if (stringEdge.getSrcStr().equals(firstCluster.getName())) {
					fanOut++;
					brickFanOutMap.put(firstCluster.getName(), fanOut);
					System.out.println("\tOutgoing edge" + stringEdge);
				}
			}
			for (StringEdge stringEdge : clusterGraph.edges ) {
				if (stringEdge.getTgtStr().equals(firstCluster.getName())) {
					fanIn++;
					brickFanInMap.put(firstCluster.getName(), fanIn);
					System.out.println("\tIncoming edge" + stringEdge);
				}
			}
			double stability = ((double)fanOut/(double)(fanOut + fanIn));
			System.out.println("\tstability: " + stability);
			brickStabilityMap.put(firstCluster.getName(), stability);
		}
		
		for (StringEdge stringEdge : clusterGraph.edges ) {
			double srcStability = brickStabilityMap.get(stringEdge.getSrcStr());
			double tgtStability = brickStabilityMap.get(stringEdge.getTgtStr());
			if (srcStability < tgtStability) {
				System.out.println("\tUnstable brick dependency found " + stringEdge);
				unstableBrickDependencyCount++;
			}
		}
		
		SimpleDirectedGraph<String, DefaultEdge>  directedGraph = new SimpleDirectedGraph<>(DefaultEdge.class);
        
        for (Cluster splitCluster : splitClusters) {
        	directedGraph.addVertex(splitCluster.getName());
        }
        
        for (StringEdge stringEdge : clusterGraph.edges) {
        	if (!stringEdge.getSrcStr().equals(stringEdge.getTgtStr()))
        		directedGraph.addEdge(stringEdge.getSrcStr(), stringEdge.getTgtStr());
        }
        
        System.out.println("Finding cycles...");	
        CycleDetector cycleDetector = new CycleDetector(directedGraph);
        Set<String> cycleSet =  cycleDetector.findCycles();
        System.out.println("Printing the cycle set...");
        System.out.println(cycleSet);
        
        System.out.println("Printing the strongly connected sets of the graph....");
        StrongConnectivityInspector inspector = new StrongConnectivityInspector(directedGraph);
        List<Set<String>> connectedSets = inspector.stronglyConnectedSets();
        
        int relevantConnectedSetCount = 0;
        for (Set<String> connectedSet : connectedSets) {
        	System.out.println();
        	System.out.println(connectedSet);
        	if (connectedSet.size() > 1) {
        		System.out.println("Counting this strongly connected component set as relevant");
        		relevantConnectedSetCount++;
        	}
        	
        }
        
        System.out.println("Finding instances of brick use overload...");
        
        int sumEdges = 0;
        for (Cluster splitCluster : splitClusters) {
        	int currFanIn = (brickFanInMap.get(splitCluster.getName()) == null ? 0 : brickFanInMap.get(splitCluster.getName()));
        	int currFanOut = (brickFanOutMap.get(splitCluster.getName()) == null ? 0 : brickFanOutMap.get(splitCluster.getName()));
        	int totalEdges = currFanIn + currFanOut;
        	sumEdges += totalEdges;
        }
        double meanEdges = ((double)sumEdges/(double)splitClusters.size());
        System.out.println("mean edges: " + meanEdges);
		
        int sumForVar = 0;
        for (Cluster splitCluster : splitClusters) {
        	int currFanIn = (brickFanInMap.get(splitCluster.getName()) == null ? 0 : brickFanInMap.get(splitCluster.getName()));
        	int currFanOut = (brickFanOutMap.get(splitCluster.getName()) == null ? 0 : brickFanOutMap.get(splitCluster.getName()));
        	int totalEdges = currFanIn + currFanOut;
        	sumForVar += Math.pow(totalEdges-meanEdges,2);
        }
        double variance = ((double)sumForVar/(double)splitClusters.size());
        
        double stdDev = Math.sqrt(variance);
        
        System.out.println("variance of edges: " + variance);
        System.out.println("standard deviation: " + stdDev);
        
        int brickUseOverloadCount = 0;
        for (Cluster splitCluster : splitClusters) {
        	int currFanIn = (brickFanInMap.get(splitCluster.getName()) == null ? 0 : brickFanInMap.get(splitCluster.getName()));
        	int currFanOut = (brickFanOutMap.get(splitCluster.getName()) == null ? 0 : brickFanOutMap.get(splitCluster.getName()));
        	int totalEdges = currFanIn + currFanOut;
        	
        	if (totalEdges > meanEdges + stdDev) {
        		System.out.println("Found instance of brick use overload for cluster " + splitCluster);
        		brickUseOverloadCount++;
        	}
        }

		System.out.println("Ambiguous Interface Count: "
				+ ambiguousInterfaceCount);
		System.out.println("Unacceptably High Connector concern count: "
				+ unacceptablyHighConnectorConcernCount);
		System.out.println("Connector Interface Implementation count: "
				+ connectorInterfaceImplCount);
		System.out.println("Scattered Parasitic Functionality count: "
				+ scatteredParasiticFunctionalityCount);
		System.out.println("Procedure Call-Based Extraneous Connector count: "
				+ procCallBasedExtraneousConnectorCount);
		System.out.println("Brick Concern Overload count: "
				+ brickConcernOverloadCount);
		System.out.println("Unused Interface count: "
				+ unusedInterfaceCount);
		System.out.println("Unstable Brick Dependency count: "
				+ unstableBrickDependencyCount);
		System.out.println("Brick Dependency Cycle count: " + relevantConnectedSetCount);
		System.out.println("Brick Use Overload count: " + brickUseOverloadCount);
	}

	private static int determineAmbiguousInterface(
			List<Cluster> splitClusters, MyCallGraph myCallGraph,
			int ambiguousInterfaceCount) {
		for (Cluster splitCluster : splitClusters) {
			System.out.println("Current cluster: " + splitCluster);
			for (MyClass myClass : splitCluster.getClasses()) {
				System.out.println("\tCurrent class: " + myClass);
				for (MyMethod myMethod : myClass.getMethods()) {
					System.out.println("\t\tCurrent method: " + myMethod);
					if (myMethod.getParams().size() == 1 && myMethod.isPublic()) {
						System.out
								.println("\t\t\tCandidate Ambiguous Interface");
						Set<MyMethod> targetEdges = myCallGraph
								.getTargetEdges(myMethod);
						System.out.println("\t\t\ttarget edges size: "
								+ targetEdges.size());
						int relevantTgtMethodCount =0;
						for (MyMethod tgtMethod : targetEdges) {
							if (splitCluster.getClasses()
									.contains(tgtMethod.getDeclaringClass())) {
								System.out.println("\t\t\t\tFound relevant taret method: " + tgtMethod);
								relevantTgtMethodCount++;
								
							}
						}
						
						if (relevantTgtMethodCount > 1) {
							System.out
							.println("\t\t\tPositive Ambiguous Interface");
							ambiguousInterfaceCount++;
						}
					}
				}
			}
		}
		return ambiguousInterfaceCount;
	}
	
	private static int findUnusedInterfaceSmells(
			Map<String, MyMethod> unusedMethods, int unusedInterfaceCount) {
		System.out.println("Number of unused methods: "
				+ unusedMethods.values().size());
		for (MyMethod myMethod : unusedMethods.values()) {
			if (myMethod.isPublic()) {
				System.out.println("\tUnused public method found: " + myMethod);
				unusedInterfaceCount++;
			}
		}
		return unusedInterfaceCount;
	}

	private static int findBrickConcernOverloadSmells(
			List<Cluster> splitClusters, int brickConcernOverloadCount) {
		for (Cluster firstCluster : splitClusters) {
			System.out.println("Current cluster: " + firstCluster);

			boolean invalidInnerClassCluster = false;
			invalidInnerClassCluster = checkIfClusterIsAnInvalidInnerClass(
					firstCluster, invalidInnerClassCluster);
			if (invalidInnerClassCluster) {
				continue;
			}

			double proportionThreshold = 0.20;
			int concernNumberThreshold = 1;

			List<TopicItem> relevantTopics = new ArrayList<>();
			for (TopicItem currTopicItem : firstCluster.getDocTopicItem().getTopics()) {
				if (currTopicItem.getProportion() >= proportionThreshold) {
					relevantTopics.add(currTopicItem);
				}
			}

			if (relevantTopics.size() > concernNumberThreshold) {
				System.out.println("\tBrick concern overload FOUND for "
						+ firstCluster);
				System.out.println("\tRelevant topics are:");
				for (TopicItem relevantTopic : relevantTopics) {
					System.out.println("\t\t" + relevantTopic);
				}
				brickConcernOverloadCount++;
			} else {
				System.out.println("\tBrick concern overload NOT found for"
						+ firstCluster);
				System.out.println("\tRelevant topics are:");
				for (TopicItem relevantTopic : relevantTopics) {
					System.out.println("\t\t" + relevantTopic);
				}
			}

		}
		return brickConcernOverloadCount;
	}

	private static int findProcCalBasedExtraneousConnectors(
			List<Cluster> splitClusters, StringGraph clusterGraph,
			int procCallBasedExtraneousConnectorCount) {
		for (Cluster firstCluster : splitClusters) {
			System.out.println("Current cluster: " + firstCluster);

			String strippedLeafSplitClusterName = ConfigUtil
					.stripParensEnclosedClassNameWithPackageName(firstCluster);

			if (Pattern.matches(ConfigUtil.anonymousInnerClassRegExpr,
					strippedLeafSplitClusterName)) {
				continue;
			}

			if (Pattern.matches(ConfigUtil.doubleInnerClassRegExpr,
					strippedLeafSplitClusterName)) {
				continue;
			}
			if (firstCluster.getType().equals("spec")) {
				for (StringEdge stringEdge : clusterGraph.edges) {
					if (stringEdge.getSrcStr().equals(firstCluster.getName())) {

						Cluster targetCluster = getClusterByName(
								stringEdge.getTgtStr(), splitClusters);

						boolean invalidInnerClassCluster = false;
						invalidInnerClassCluster = checkIfClusterIsAnInvalidInnerClass(
								targetCluster, invalidInnerClassCluster);
						if (invalidInnerClassCluster) {
							continue;
						}
						if (targetCluster.getType().equals("indep")) {
							Cluster connCluster = targetCluster;
							Cluster compCluster = firstCluster;
							Cluster otherCompCluster = findDifferentClusterThatDependsOnConnector(
									compCluster, connCluster, splitClusters,
									clusterGraph);
							if (otherCompCluster == null) {
								System.out
										.println("Did not find proc-call based extraneous adjacent connector");
							} else {
								System.out
										.println("Elements involved in extraneous adjacent connector...");
								System.out.println("\tcompCluster: "
										+ compCluster);
								System.out.println("\tconnCluster: "
										+ connCluster);
								System.out.println("\totherCompCluster: "
										+ otherCompCluster);
								procCallBasedExtraneousConnectorCount++;
							}
						}
					} else if (stringEdge.getTgtStr().equals(firstCluster.getName())) {
						Cluster srcCluster = getClusterByName(
								stringEdge.getSrcStr(), splitClusters);
						boolean invalidInnerClassCluster = false;
						invalidInnerClassCluster = checkIfClusterIsAnInvalidInnerClass(
								srcCluster, invalidInnerClassCluster);
						if (invalidInnerClassCluster) {
							continue;
						}
						if (srcCluster.getType().equals("indep")) {
							Cluster connCluster = srcCluster;
							Cluster compCluster = firstCluster;
							Cluster otherCompCluster = findDifferentClusterThatDependsOnConnector(
									compCluster, connCluster, splitClusters,
									clusterGraph);
							if (otherCompCluster == null) {
								System.out
										.println("Did not find proc-call based extraneous adjacent connector");
							} else {
								System.out
										.println("Elements involved in extraneous adjacent connector...");
								System.out.println("\tcompCluster: "
										+ compCluster);
								System.out.println("\tconnCluster: "
										+ connCluster);
								System.out.println("\totherCompCluster: "
										+ otherCompCluster);
								procCallBasedExtraneousConnectorCount++;
							}
						}
					}
				}
			}
		}
		return procCallBasedExtraneousConnectorCount;
	}
	// #endregion DRIVERS --------------------------------------------------------

	// #region PROCESSING --------------------------------------------------------
	private static boolean haveMatchingTopicItem(List<TopicItem> topics,
			TopicItem inTopicItem) {
		for (TopicItem currTopicItem : topics) {
			if (currTopicItem.getTopicNum() == inTopicItem.getTopicNum()) {
				return true;
			}
		}
		return false;
	}

	private static void computeWordTopicProbabilitiesAndSpecifityTypesForMethods(
			List<Cluster> splitClusters, TopicKeySet topicKeySet,
			WordTopicCounts wordTopicCounts) throws IOException,
			ParserConfigurationException, SAXException {
		for (Cluster splitCluster : splitClusters) {
			System.out.println("Current cluster: " + splitCluster);

			String strippedLeafSplitClusterName = ConfigUtil
					.stripParensEnclosedClassNameWithPackageName(splitCluster);

			if (Pattern.matches(ConfigUtil.anonymousInnerClassRegExpr,
					strippedLeafSplitClusterName)) {
				continue;
			}

			if (Pattern.matches(ConfigUtil.doubleInnerClassRegExpr,
					strippedLeafSplitClusterName)) {
				continue;
			}

			if (splitCluster.getType().equals("indep")) {
				continue;
			}
			for (MyClass myClass : splitCluster.getClasses()) {
				System.out.println("\tCurrent class: " + myClass);
				for (MyMethod myMethod : myClass.getMethods()) {
					System.out.println("\t\tCurrent method: " + myMethod);
					String processedMethodName = StringPreProcessor
							.camelCaseSeparateAndStem(myMethod.getName());
					System.out.println("\t\tProcssed method name: "
							+ processedMethodName);

					Map<Integer, String> positionWordMap = new HashMap<>();
					Map<Integer, Double> queryGivenTopicProbabilitiesMap = new HashMap<>();

					computeQueryGivenTopicProbabilitiesMap(topicKeySet,
							wordTopicCounts, processedMethodName,
							positionWordMap, queryGivenTopicProbabilitiesMap);

					System.out.println();
					determineTopicForMethod(topicKeySet, myMethod,
							queryGivenTopicProbabilitiesMap);

				}
			}
		}
	}

	private static void determineTopicForMethod(TopicKeySet topicKeySet,
			MyMethod myMethod, Map<Integer, Double> queryGivenTopicProbabilitiesMap)
			throws IOException, ParserConfigurationException, SAXException {
		int mostProbableTopic = -1;
		double highestProbSoFar = 0;
		for (TopicKey topicKey : topicKeySet.getSet()) {
			Double currProb = queryGivenTopicProbabilitiesMap
					.get(topicKey.getTopicNum());
			System.out.println("\t\t\ttopic: " + topicKey.getTopicNum());

			System.out
					.println("\t\t\tcurrProb of query given topic: "
							+ currProb);

			if (currProb >= highestProbSoFar) {
				highestProbSoFar = currProb;
				mostProbableTopic = topicKey.getTopicNum();
			}

		}

		TopicKeySet typedTopicKeySet = TopicUtil
				.getTypedTopicKeyList();

		System.out.println("\t\tTopic determined for "
				+ myMethod.getName());
		System.out.println("\t\t topic: " + mostProbableTopic);
		String typeForMostProbableTopic = typedTopicKeySet
		.getTopicKeyByID(mostProbableTopic).getType();
		System.out.println("\t\t topic's type: "
				+ typeForMostProbableTopic);
		System.out
				.println("\t\t prob for method " + myMethod + " given topic: "
						+ highestProbSoFar);
		myMethod.setType(typeForMostProbableTopic);
		System.out.println();
	}

	private static void computeQueryGivenTopicProbabilitiesMap(
			TopicKeySet topicKeySet, WordTopicCounts wordTopicCounts,
			String processedMethodName,
			Map<Integer, String> positionWordMap,
			Map<Integer, Double> queryGivenTopicProbabilitiesMap) {
		for (TopicKey topicKey : topicKeySet.getSet()) {
			String[] wordsInMethodName = processedMethodName
					.split(" ");

			double probabilitySum = 0;
			for (String word : wordsInMethodName) {
				if (!wordTopicCounts.getWordTopicItems()
						.containsKey(word)) {
					continue;
				}
				WordTopicItem wtItem = wordTopicCounts
						.getWordTopicItems().get(word);

				positionWordMap.put(topicKey.getTopicNum(), wtItem.name);

				double probWordGivenTopic = wtItem
						.probabilityWordGivenTopic(topicKey.getTopicNum());

				System.out.println("\t\t\t\tProbability "
						+ wtItem.name + " given "
						+ topicKey.getTopicNum() + ": "
						+ probWordGivenTopic);

				probabilitySum += probWordGivenTopic;

			}

			System.out.println("\t\t\tProbability sum for topic "
					+ topicKey.getTopicNum() + ": " + probabilitySum);
			double probabilityAverage = (probabilitySum / (double) wordsInMethodName.length);

			System.out
					.println("\t\t\tProbability avg for topic "
							+ topicKey.getTopicNum() + ": "
							+ probabilityAverage);
			queryGivenTopicProbabilitiesMap.put(topicKey.getTopicNum(),
					Double.valueOf(probabilityAverage));
		}
	}

	private static void determineInterfacesForClusters(
			List<Cluster> splitClusters, Map<String, MyClass> classes) {
		for (Cluster cluster : splitClusters) {
			System.out.println("Determining interfaces for cluster " + cluster);
			cluster.resetClasses();
			for (Cluster leaf : cluster.getLeafClusters()) {
				String strippedLeafClusterName = leaf.toString().substring(1,
						leaf.toString().length() - 1);
				System.out.println("\t" + strippedLeafClusterName);
				if (classes.containsKey(strippedLeafClusterName)) {
					MyClass myClass = classes.get(strippedLeafClusterName);
					cluster.addClass(myClass);
				}

			}
		}

	}
	
	private static void generateTopicsForSplitClusters(
			List<Cluster> splitClusters) {
		DocTopics docTopics = null;
		docTopics = TopicUtil.getDocTopicsFromFile();

		for (Cluster splitCluster : splitClusters) {
			System.out.println("Current split cluster: " + splitCluster);
			List<Cluster> currLeafClusters = splitCluster.getLeafClusters();
			int leafCounter = 0;

			String strippedLeafSplitClusterName = ConfigUtil
					.stripParensEnclosedClassNameWithPackageName(splitCluster);

			if (Pattern.matches(ConfigUtil.anonymousInnerClassRegExpr,
					strippedLeafSplitClusterName)) {
				continue;
			}

			if (Pattern.matches(ConfigUtil.doubleInnerClassRegExpr,
					strippedLeafSplitClusterName)) {
				continue;
			}

			System.out.println("Setting doc-topic for each item...");
			setDocTopicForEachLeafCluster(docTopics, currLeafClusters,
					leafCounter);

			Cluster refLeaf = getClusterForReferenceOfTopics(currLeafClusters);

			List<TopicItem> topics = createZeroProportionTopicsFromReference(refLeaf);

			calculateNewTopicProportionsForSplitClusters(
					splitCluster, currLeafClusters, topics);

		}
	}
	
	private static void setDocTopicForEachLeafCluster(DocTopics docTopics,
			List<Cluster> currLeafClusters, int leafCounter) {
		for (Cluster leaf : currLeafClusters) {
			System.out.println("\t" + leafCounter + ": " + leaf);
			TopicUtil.setDocTopicForCluster(docTopics, leaf);
		}
	}
	
	private static boolean checkIfClusterIsAnInvalidInnerClass(
			Cluster firstCluster, boolean invalidInnerClassCluster) {
		String strippedLeafSplitClusterName = ConfigUtil
				.stripParensEnclosedClassNameWithPackageName(firstCluster);

		if (Pattern.matches(
				ConfigUtil.anonymousInnerClassRegExpr,
				strippedLeafSplitClusterName)) {
			invalidInnerClassCluster = true;
		}

		if (Pattern.matches(ConfigUtil.doubleInnerClassRegExpr,
				strippedLeafSplitClusterName)) {
			invalidInnerClassCluster = true;
		}
		return invalidInnerClassCluster;
	}

	private static Cluster findDifferentClusterThatDependsOnConnector(
			Cluster compCluster, Cluster connCluster, List<Cluster> splitClusters, StringGraph clusterGraph) {
		for (Cluster cluster : splitClusters) {
			if (!cluster.getName().equals(compCluster.getName())) {
				for (StringEdge stringEdge : clusterGraph.edges ) {
					if (stringEdge.getSrcStr().equals(cluster.getName())) {
						Cluster targetCluster = getClusterByName(stringEdge.getTgtStr(), splitClusters);
						boolean invalidInnerClassCluster = false;
						invalidInnerClassCluster = checkIfClusterIsAnInvalidInnerClass(
								targetCluster, invalidInnerClassCluster);
						if (invalidInnerClassCluster) {
							continue;
						}
						if (targetCluster.getType().equals("spec") && !targetCluster.getName().equals(compCluster.getName())) {
							return targetCluster;
						}
					}
					else if (stringEdge.getTgtStr().equals(cluster.getName())) {
						Cluster srcCluster = getClusterByName(stringEdge.getSrcStr(), splitClusters);
						boolean invalidInnerClassCluster = false;
						invalidInnerClassCluster = checkIfClusterIsAnInvalidInnerClass(
								srcCluster, invalidInnerClassCluster);
						if (invalidInnerClassCluster) {
							continue;
						}
						if (srcCluster.getType().equals("spec") && !srcCluster.getName().equals(compCluster.getName())) {
							return srcCluster;
						}
					}
				}
			}
		}
		return null;
	}

	private static Cluster getClusterByName(String tgtStr,
			List<Cluster> splitClusters) {
		for (Cluster cluster : splitClusters) {
			if (cluster.getName().equals(tgtStr))
				return cluster;
		}
		return null;
		
	}

	private static int findScatteredParasiticFunctionalitySmells(
			List<Cluster> splitClusters) {
		double threshold1 = 0.30;
		double threshold2 = 0.30;
		
		Set<TopicItem> scatteredTopics = new HashSet<>();
		
		for (Cluster firstCluster : splitClusters) {
			System.out.println("Current cluster: " + firstCluster);

			String strippedLeafSplitClusterName = ConfigUtil
					.stripParensEnclosedClassNameWithPackageName(firstCluster);

			if (Pattern.matches(ConfigUtil.anonymousInnerClassRegExpr,
					strippedLeafSplitClusterName)) {
				continue;
			}

			if (Pattern.matches(ConfigUtil.doubleInnerClassRegExpr,
					strippedLeafSplitClusterName)) {
				continue;
			}

			for (TopicItem firstTopicItem : firstCluster.getDocTopicItem().getTopics()) {
				if (firstTopicItem.getProportion() > threshold1) {
					for (Cluster secondCluster : splitClusters) {
						String strippedSecondClusterName = ConfigUtil
								.stripParensEnclosedClassNameWithPackageName(secondCluster);

						if (Pattern.matches(
								ConfigUtil.anonymousInnerClassRegExpr,
								strippedSecondClusterName)) {
							continue;
						}

						if (Pattern.matches(ConfigUtil.doubleInnerClassRegExpr,
								strippedSecondClusterName)) {
							continue;
						}
						for (TopicItem secondTopicItem : secondCluster.getDocTopicItem().getTopics()) {
							

							if (secondTopicItem.getProportion() > threshold2 && firstTopicItem.getTopicNum() == secondTopicItem.getTopicNum()) {
								for (TopicItem thirdTopicItem : firstCluster.getDocTopicItem().getTopics()) {
									if (thirdTopicItem.getProportion() > threshold1 && !thirdTopicItem.equals(firstTopicItem)) {
										
										System.out.println("\t" + firstCluster + " has scattered topic " + firstTopicItem);
										System.out.println("\t" + firstCluster + " has orthogonal topic " + thirdTopicItem);
										System.out.println("\t" + secondCluster + " has scattered topic " + secondTopicItem);
										scatteredTopics.add(firstTopicItem);
									}
								}
							}
						}
					}
				}
			}
		}
		return scatteredTopics.size();
	}

	private static void computeAndShowWordTopicProbabilitiesForAllWordsAndTopics(
			TopicKeySet topicKeySet, WordTopicCounts wordTopicCounts) {
		for (WordTopicItem wtItem : wordTopicCounts.getWordTopicItems()
				.values()) {
			for (TopicKey topicKey : topicKeySet.getSet()) {
				System.out.println("P(" + wtItem.name + "," + topicKey.getTopicNum()
						+ ") = "
						+ wtItem.probabilityWordGivenTopic(topicKey.getTopicNum()));
			}
		}
	}

	private static int findUnacceptablyHighConnectorConcernSmells(
			List<Cluster> splitClusters,
			int unacceptablyHighConnectorConcernCount) {
		for (Cluster splitCluster : splitClusters) {
			System.out.println("Current cluster: " + splitCluster);

			String strippedLeafSplitClusterName = ConfigUtil
					.stripParensEnclosedClassNameWithPackageName(splitCluster);

			if (Pattern.matches(ConfigUtil.anonymousInnerClassRegExpr,
					strippedLeafSplitClusterName)) {
				continue;
			}

			if (Pattern.matches(ConfigUtil.doubleInnerClassRegExpr,
					strippedLeafSplitClusterName)) {
				continue;
			}

			if (splitCluster.getType().equals("indep")) {
				continue;
			}
			for (TopicItem topicItem : splitCluster.getDocTopicItem().getTopics()) {
				System.out.println("\ttopic id : " + topicItem.getTopicNum());
				System.out.println("\ttopic type : " + topicItem.getType());
				System.out.println("\ttopic proportion : "
						+ topicItem.getProportion());
				if (topicItem.getType().equals("indep")
						&& topicItem.getProportion() > 0.10) {
					System.out
							.println("\t counting as unacceptably high connector concern: "
									+ splitCluster);
					unacceptablyHighConnectorConcernCount++;
				}
			}
		}
		return unacceptablyHighConnectorConcernCount;
	}

	private static int findConnectorInterfaceImplementationSmells(
			List<Cluster> splitClusters, int connectorInterfaceImplCount) {
		for (Cluster splitCluster : splitClusters) {
			System.out.println("Current cluster: " + splitCluster);

			String strippedLeafSplitClusterName = ConfigUtil
					.stripParensEnclosedClassNameWithPackageName(splitCluster);

			if (Pattern.matches(ConfigUtil.anonymousInnerClassRegExpr,
					strippedLeafSplitClusterName)) {
				continue;
			}

			if (Pattern.matches(ConfigUtil.doubleInnerClassRegExpr,
					strippedLeafSplitClusterName)) {
				continue;
			}

			if (splitCluster.getType().equals("indep")) {
				continue;
			}
			for (MyClass myClass : splitCluster.getClasses()) {
				System.out.println("\tCurrent class: " + myClass);
				for (MyMethod myMethod : myClass.getMethods()) {
					System.out.println("\t\tCurrent method: " + myMethod);
					if (splitCluster.getType().equals("spec") && myMethod.isPublic() && myMethod.getType().equals("indep")) {
						System.out.println("\t\t\tFound instance of connector interface implementation...");
						System.out.println("\t\t\t" + myMethod + " of "+ splitCluster + " has type " + myMethod.getType() + " while " + splitCluster + " has " + splitCluster.getType());
						connectorInterfaceImplCount++;
					}
				}
			}
		}
		return connectorInterfaceImplCount;
	}
	
	private static int calculateNewTopicProportionsForSplitClusters(
			Cluster splitCluster, List<Cluster> currLeafClusters,
			List<TopicItem> topics) {
		int leafCounter;
		leafCounter = 0;
		System.out.println("Creating new topic items for split cluster: "
				+ splitCluster);
		int nonAnonInnerClassLeafCounter = 0;
		for (Cluster leaf : currLeafClusters) {
			System.out.println("\t" + leafCounter + ": " + leaf);
			System.out.println("\t" + "doc-topic: " + leaf.getDocTopicItem());
			leafCounter++;
			String strippedClassName = ConfigUtil
					.stripParensEnclosedClassNameWithPackageName(leaf);
			// ".*\\$\\d+"
			if (Pattern.matches(ConfigUtil.anonymousInnerClassRegExpr,
					strippedClassName)) {
				continue;
			}
			if (Pattern.matches(ConfigUtil.doubleInnerClassRegExpr,
					strippedClassName)) {
				continue;
			}
			nonAnonInnerClassLeafCounter++;
			for (int j = 0; j < leaf.getDocTopicItem().size(); j++) {
				TopicItem currLeafTopicItem = leaf.getDocTopicItem().getTopics()
						.get(j);
				if (haveMatchingTopicItem(topics, currLeafTopicItem)) {
					TopicItem matchingTopicItem = TopicUtil.getMatchingTopicItem(topics,
							currLeafTopicItem);
					matchingTopicItem.increaseProportion(currLeafTopicItem.getProportion());
				} else {
					TopicItem newTopicItem = new TopicItem(currLeafTopicItem);
					newTopicItem.setProportion(newTopicItem.getProportion());
					topics.add(newTopicItem);
				}
			}

		}
		splitCluster.setDocTopicItem(new DocTopicItem());
		for (TopicItem topicItem : topics) {
			splitCluster.addTopicItem(new TopicItem(topicItem));
		}

		System.out.println("splitCluster " + splitCluster
				+ "'s new topics summed only...");
		System.out.println(splitCluster.getDocTopicItem().getTopics());
		System.out.println("nonAnonInnerClassLeafCounter: "
				+ nonAnonInnerClassLeafCounter);

		for (TopicItem topicItem : splitCluster.getDocTopicItem().getTopics()) {
			topicItem.divideProportion(nonAnonInnerClassLeafCounter);
		}

		System.out.println("splitCluster " + splitCluster
				+ "'s new topics averaged...");
		System.out.println(splitCluster.getDocTopicItem().getTopics());
		return nonAnonInnerClassLeafCounter;
	}

	private static List<TopicItem> createZeroProportionTopicsFromReference(
			Cluster refLeaf) {
		System.out.println("Copying first leafs topics to new topics...");
		List<TopicItem> topics = new ArrayList<>();
		for (TopicItem topicItem : refLeaf.getDocTopicItem().getTopics()) {
			topics.add(new TopicItem(topicItem));
		}

		System.out
				.println("Zeroing out proportions for TopicItems in new topics...");
		for (TopicItem topicItem : topics) {
			topicItem.setProportion(0);
		}

		System.out.println("Verifying zero out worked...");
		for (TopicItem topicItem : topics) {
			System.out.println("topicNum: " + topicItem.getTopicNum()
					+ ", proportion: " + topicItem.getProportion());
		}
		return topics;
	}

	private static Cluster getClusterForReferenceOfTopics(
			List<Cluster> currLeafClusters) {
		Cluster refLeaf = null;
		for (Cluster leaf : currLeafClusters) {
			if (leaf.getDocTopicItem() != null) {
				refLeaf = leaf;
			}
		}
		return refLeaf;
	}
	// #endregion PROCESSING -----------------------------------------------------

	// #region IO ----------------------------------------------------------------
	private static MyCallGraph deserializeMyCallGraph() throws IOException,
			ClassNotFoundException {
		String filename = Config.getMyCallGraphFilename();
		// Read from disk using FileInputStream
		FileInputStream f_in = new FileInputStream(filename);

		// Read object using ObjectInputStream
		ObjectInputStream obj_in = new ObjectInputStream(f_in);

		// Read an object
		Object obj = obj_in.readObject();

		MyCallGraph locClg = null;
		if (obj instanceof MyCallGraph) {
			// Cast object to a Vector
			locClg = (MyCallGraph) obj;
		}

		return locClg;
	}
	
	private static HashMap<?, ?> deserializeHashMap(String filename)
			throws IOException, ClassNotFoundException {

		// Read from disk using FileInputStream
		FileInputStream f_in = new FileInputStream(filename);

		// Read object using ObjectInputStream
		ObjectInputStream obj_in = new ObjectInputStream(f_in);

		// Read an object
		Object obj = obj_in.readObject();

		HashMap<?, ?> hashMap = null;
		if (obj instanceof HashMap<?, ?>) {
			// Cast object to a Vector
			hashMap = (HashMap<?, ?>) obj;
		}

		return hashMap;
	}

	private static void writeMethodInfoToXML(
			List<Cluster> splitClusters, MyCallGraph myCallGraph) throws ParserConfigurationException, TransformerException {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		// classgraph elements
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("SmellArchGraph");
		doc.appendChild(rootElement);

		for (Cluster splitCluster : splitClusters) {
			System.out.println("Current cluster: " + splitCluster);
			Element clusterElem = doc.createElement("cluster");
			rootElement.appendChild(clusterElem);
			clusterElem.setAttribute("name",splitCluster.toString());

			for (MyClass myClass : splitCluster.getClasses()) {
				System.out.println("\tCurrent class: " + myClass);
				Element classElem = doc.createElement("class");
				clusterElem.appendChild(classElem);
				classElem.setAttribute("name",myClass.getClassName());

				for (MyMethod myMethod : myClass.getMethods()) {
					System.out.println("\t\tCurrent method: " + myMethod);
					Element methodElem = doc.createElement("method");
					classElem.appendChild(methodElem);
					methodElem.setAttribute("name",myMethod
							.toString());
					for (String param : myMethod.getParams()) {
						System.out.println("\t\t\tCurrent param: " + param);
						Element paramElem = doc.createElement("param");
						methodElem.appendChild(paramElem);
						paramElem.setAttribute("name",param);
					}
					
					Element retValElem = doc.createElement("retval");
					methodElem.appendChild(retValElem);
					retValElem.setAttribute("name",myMethod.getRetVal());
				}
			}
		}
		
		 //write the content into xml file
		  TransformerFactory transformerFactory = TransformerFactory.newInstance();
		  Transformer transformer = transformerFactory.newTransformer();
		  transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		  DOMSource source = new DOMSource(doc);
		  StreamResult result =  new StreamResult(new File(Config.getMethodInfoFilename()));
		  transformer.transform(source, result);
	 
		  System.out.println("In " + Thread.currentThread().getStackTrace()[1].getClassName() 
				  + ". " + Thread.currentThread().getStackTrace()[1].getMethodName() 
				  + ", Wrote " + Config.getMethodInfoFilename());
	}
	
	private static void writeOutGraphsAndSmellArchToFiles(
			List<Cluster> splitClusters, StringGraph clusterGraph,
			SmellArchGraph smellArchGraph) {
		try {
			clusterGraph.writeDotFile(Config.getClusterGraphDotFilename());
			clusterGraph.writeXMLClusterGraph(Config.getClusterGraphXMLFilename());
			smellArchGraph.writeXMLSmellArchGraph(Config.getXMLSmellArchGraphFilename());

			ClusterUtil.writeOutSmellArchToXML(splitClusters);
			

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}

	private static void printUnusedMethods(
			Map<String, MyMethod> unusedMethods) {
		for (MyMethod m : unusedMethods.values()) {
			System.out.println("\t" + m.toString());
		}

	}
	
	private static void printInterfacesOfClusters(List<Cluster> splitClusters) {
		for (Cluster cluster : splitClusters) {
			System.out.println("Printing interfaces of cluster " + cluster);
			for (MyClass myClass : cluster.getClasses()) {
				System.out.println("\t comprising class: " + myClass);
				System.out.println(myClass.methodsToString(2));
			}
		}

	}
	
	private static void printClassesWithUsedMethods(
			Map<String, MyClass> classesWithMethodsInMyCallGraph) {
		for (MyClass c : classesWithMethodsInMyCallGraph.values()) {
			System.out.println("Showing linked methods in " + c + "...");
			System.out.println(c.methodsToString(1));
		}
	}
	
	private static void printTopicsForSplitClusters(
			List<Cluster> splitClusters) {
		System.out
				.println("Printing document-topic distribution for each split cluster...");
		for (Cluster splitCluster : splitClusters) {
			System.out.println("\t" + splitCluster);
			System.out.println("\t\t" + splitCluster.getDocTopicItem());
		}

	}
	// #endregion IO -------------------------------------------------------------
}