package edu.usc.softarch.arcade.clustering.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.usc.softarch.arcade.callgraph.MyClass;
import edu.usc.softarch.arcade.callgraph.MyMethod;
import edu.usc.softarch.arcade.classgraphs.StringEdge;
import edu.usc.softarch.arcade.clustering.Cluster;
import edu.usc.softarch.arcade.clustering.FastCluster;
import edu.usc.softarch.arcade.clustering.FastFeatureVectors;
import edu.usc.softarch.arcade.clustering.Feature;
import edu.usc.softarch.arcade.clustering.FeatureVector;
import edu.usc.softarch.arcade.clustering.StringGraph;
import edu.usc.softarch.arcade.config.Config;
import edu.usc.softarch.arcade.config.ConfigUtil;
import edu.usc.softarch.arcade.facts.ExpertDecomposition;
import edu.usc.softarch.arcade.facts.ConcernCluster;
import edu.usc.softarch.arcade.facts.Group;
import edu.usc.softarch.arcade.facts.driver.RsfReader;
import edu.usc.softarch.arcade.smellarchgraph.ClusterEdge;
import edu.usc.softarch.arcade.smellarchgraph.SmellArchGraph;
import edu.usc.softarch.arcade.topics.DocTopicItem;
import edu.usc.softarch.arcade.topics.TopicItem;
import edu.usc.softarch.arcade.topics.TopicKey;
import edu.usc.softarch.arcade.topics.TopicKeySet;
import edu.usc.softarch.arcade.topics.TopicUtil;
import edu.usc.softarch.arcade.util.DebugUtil;
import edu.usc.softarch.extractors.cda.odem.Dependencies;
import edu.usc.softarch.extractors.cda.odem.DependsOn;
import edu.usc.softarch.extractors.cda.odem.Type;

/**
 * @author joshua
 * 
 */
public class ClusterUtil {
	private static int numClustersToSplit = 5;
	private static boolean DEBUG = false;
	private static Logger logger = Logger.getLogger(ClusterUtil.class);

	public static void generateLeafClusters(ArrayList<Cluster> clusters) {
		for (Cluster c : clusters) {
			ArrayList<Cluster> startClusters = new ArrayList<Cluster>();
			ArrayList<Cluster> leafClusters = getLeafClusters(c, startClusters);
			logger.debug("Listing each leaf cluster of cluster " + c.name
					+ "...");
			int clusterCount = 0;
			for (Cluster leafC : leafClusters) {
				logger.debug("\t " + clusterCount + ": " + leafC.name);
				clusterCount++;
			}
			c.leafClusters = leafClusters;
		}
	}

	public static void printItemsInClusters(ArrayList<Cluster> clusters) {
		logger.debug("Listing items in each cluster of the clusters...");
		for (Cluster c : clusters) {
			logger.debug("Cluster: " + c.name);
			int fvCount = 0;
			for (FeatureVector fv : c.items) {
				logger.debug("\t " + fvCount + ": " + fv.name);
				fvCount++;
			}
		}
	}

	private static ArrayList<Cluster> getLeafClusters(Cluster c,
			ArrayList<Cluster> startClusters) {
		if (c.left == null && c.right == null) {
			startClusters.add(c);
			return startClusters;
		} else {
			if (c.left != null)
				getLeafClusters(c.left, startClusters);
			if (c.right != null)
				getLeafClusters(c.right, startClusters);
			return startClusters;
		}

	}

	public static ArrayList<Cluster> splitClusters(ArrayList<Cluster> clusters) {
		Cluster root = clusters.get(0);

		Cluster curr = root;
		LinkedList<Cluster> queue = new LinkedList<Cluster>();
		ArrayList<Cluster> splitClusters = new ArrayList<Cluster>();
		splitClusters.add(curr);

		if (DEBUG) {
			logger.debug("Initial split clusters size: " + splitClusters.size());
			logger.debug("Initial split clusters: ");
		}
		prettyPrintSplitClusters(splitClusters);

		queue.offer(curr);
		curr = queue.pop();
		while (curr != null) {

			if (DEBUG) {
				logger.debug("--------------------");
				logger.debug("curr: " + curr);
				logger.debug("--------------------");
				logger.debug("left: " + curr.left);
				logger.debug("--------------------");
				logger.debug("right: " + curr.right);
				logger.debug("--------------------");
			}

			if (DEBUG)
				logger.debug("Item size of current cluster: "
						+ curr.items.size());
			if (curr.items.size() >= 1) {
				if (curr.left != null || curr.right != null) {
					splitClusters.remove(curr);
				}
			}

			if (curr.left != null) {
				if (curr.right != null) {
					if (DEBUG) {
						System.out
								.println("Testing similiarity of left and right cluster: ");
						logger.debug("Left: " + curr.left.simLeftRight + ", : "
								+ curr.right.simLeftRight);
					}
					if (curr.left.simLeftRight < curr.right.simLeftRight) {
						if (DEBUG)
							logger.debug("Splitting left then right");
						queue.offer(curr.left);
						splitClusters.add(curr.left);

						queue.offer(curr.right);
						splitClusters.add(curr.right);
					} else {
						if (DEBUG)
							logger.debug("Splitting right then left");
						queue.offer(curr.right);
						splitClusters.add(curr.right);

						queue.offer(curr.left);
						splitClusters.add(curr.left);
					}

				} else {
					if (DEBUG)
						logger.debug("Splitting only left");
					queue.offer(curr.left);
					splitClusters.add(curr.left);
				}
			} else if (curr.right != null) {
				if (DEBUG)
					logger.debug("Splitting only right");
				queue.offer(curr.right);
				splitClusters.add(curr.right);
			}

			if (DEBUG) {
				logger.debug("Current split clusters size: "
						+ splitClusters.size());
				logger.debug("Current split clusters: ");
				prettyPrintSplitClusters(splitClusters);
				logger.debug("\n");
			}

			if (splitClusters.size() == Config.getNumClusters()) {
				break;
			}

			if (queue.isEmpty())
				break;
			else
				curr = queue.pop();
		}

		return splitClusters;
	}

	public static void prettyPrintSplitClusters(ArrayList<Cluster> splitClusters) {

		int count = 1;
		for (Cluster c : splitClusters) {
			logger.debug(count + ":" + c);
			count++;
		}

	}

	public static void printClustersByLine(ArrayList<Cluster> clusters) {
		for (int i = 0; i < clusters.size(); i++) {
			logger.debug(i + ": " + clusters.get(i));
		}

	}
	
	public static void printFastClustersByLine(ArrayList<FastCluster> clusters) {
		for (int i = 0; i < clusters.size(); i++) {
			logger.debug(i + ": " + clusters.get(i).getName());
		}

	}

	public static StringGraph generateClusterGraph(
			Collection<Cluster> splitClusters) {
		boolean debugMethod = false;
		StringGraph clusterGraph = new StringGraph();
		for (Cluster c1 : splitClusters) {
			for (Cluster c2 : splitClusters) {
				for (Feature f : c1) {
					for (Cluster lc2 : c2.leafClusters) {
						String lc2NameClean = lc2.name.substring(1,
								lc2.name.length() - 1).trim();
						String featureEdgeClean = f.edge.tgtStr.trim();
						if (debugMethod) {
							logger.debug("featureEdgeClean: "
									+ featureEdgeClean);
							logger.debug("lc2NameClean: " + lc2NameClean);
						}

						if (featureEdgeClean.equals(lc2NameClean)
								&& f.value > 0) {
							clusterGraph.addEdge(new StringEdge(c1.name,
									c2.name));
						}
					}
				}
			}
		}
		return clusterGraph;
	}

	public static StringGraph generateFastClusterGraph(
			Collection<FastCluster> splitClusters,
			ArrayList<String> namesInFeatureSet) {
		boolean debugMethod = false;
		StringGraph clusterGraph = new StringGraph();
		for (FastCluster c1 : splitClusters) {
			for (FastCluster c2 : splitClusters) {
				// double[] c1Features = c1.getFeatures();
				Set<Integer> c1Keys = c1.getNonZeroFeatureMap().keySet();
				for (Integer key : c1Keys) {
					Double c1FeatureValue = c1.getNonZeroFeatureMap().get(key);
					String c1FeatureName = namesInFeatureSet.get(key);
					String[] c2Entities = c2.getName().split(",");
					for (String c2EntityName : c2Entities) {
						if (debugMethod) {
							logger.debug("c1FeatureName: " + c1FeatureName
									+ ", c1FeatureValue: " + c1FeatureValue);
							logger.debug("c2EntityName: " + c2EntityName);
						}

						if (c1FeatureName.equals(c2EntityName)) {
							logger.trace("Adding edge (" + c1.getName() + ","
									+ c2.getName() + ")");
							clusterGraph.addEdge(new StringEdge(c1.getName(),
									c2.getName()));
						}
					}
				}
			}
		}
		return clusterGraph;
	}

	public static SmellArchGraph generateSmellArchGraph(
			ArrayList<Cluster> splitClusters) {
		boolean debugMethod = false;
		SmellArchGraph smellArchGraph = new SmellArchGraph();
		for (Cluster c1 : splitClusters) {
			for (Cluster c2 : splitClusters) {
				for (Feature f : c1) {
					for (Cluster lc2 : c2.leafClusters) {
						String lc2NameClean = lc2.name.substring(1,
								lc2.name.length() - 1).trim();
						String featureEdgeClean = f.edge.tgtStr.trim();
						if (debugMethod) {
							logger.debug("featureEdgeClean: "
									+ featureEdgeClean);
							logger.debug("lc2NameClean: " + lc2NameClean);
						}

						if (featureEdgeClean.equals(lc2NameClean)
								&& f.value > 0) {
							smellArchGraph.addEdge(new ClusterEdge(c1, c2));
						}
					}
				}
			}
		}
		return smellArchGraph;
	}

	public static void readInSmellArchFromXML(String filename,
			ArrayList<Cluster> splitClusters, HashSet<TopicKey> topicKeys)
			throws ParserConfigurationException, SAXException, IOException {
		File smellArchXMLFile = new File(filename);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(smellArchXMLFile);
		doc.getDocumentElement().normalize();

		logger.debug("Root element :" + doc.getDocumentElement().getNodeName());
		NodeList topicsList = doc.getElementsByTagName("topics");

		logger.debug("Getting info on topics...");
		logger.debug("----------------------- size: " + topicsList.getLength());

		Node topicsNode = topicsList.item(0);
		if (topicsNode.getNodeType() == Node.ELEMENT_NODE) {
			Element topicsElem = (Element) topicsNode;

			NodeList topicList = topicsElem.getElementsByTagName("topic");

			for (int i = 0; i < topicList.getLength(); i++) {
				Node topicNode = topicList.item(i);
				if (topicNode.getNodeType() == Node.ELEMENT_NODE) {
					Element topicElem = (Element) topicNode;
					int topicNum = Integer.parseInt(topicElem
							.getAttribute("id"));
					logger.debug("topic id: " + topicNum);
					String topicItemTypeFromXML = topicElem
							.getAttribute("type").trim();
					logger.debug("\ttype: " + topicItemTypeFromXML);
					logger.debug("Setting types for topic in each doc-topic of each cluster...");
					for (Cluster splitCluster : splitClusters) {
						logger.debug("Current cluster: " + splitCluster);
						String strippedLeafSplitClusterName = ConfigUtil
								.stripParensEnclosedClassNameWithPackageName(splitCluster);

						if (Pattern.matches(
								ConfigUtil.anonymousInnerClassRegExpr,
								strippedLeafSplitClusterName)) {
							continue;
						}

						if (Pattern.matches(ConfigUtil.doubleInnerClassRegExpr,
								strippedLeafSplitClusterName)) {
							continue;
						}
						for (TopicItem topicItem : splitCluster.docTopicItem.topics) {
							if (topicItem.topicNum == topicNum)
								topicItem.type = topicItemTypeFromXML;
						}

						for (TopicKey topicKey : topicKeys) {
							if (topicKey.topicNum == topicNum) {
								topicKey.type = topicItemTypeFromXML;
							}
						}
					}
				}
			}
		}

		logger.debug("Showing topic types from doc-topic items for each cluster...");
		for (Cluster splitCluster : splitClusters) {
			logger.debug("Current cluster: " + splitCluster);
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
			for (TopicItem topicItem : splitCluster.docTopicItem.topics) {
				logger.debug("topicNum: " + topicItem.topicNum);
				logger.debug("topicItem.type: " + topicItem.type);
			}
		}

		NodeList clusterList = doc.getElementsByTagName("Cluster");

		logger.debug("Setting types for clusters...");
		for (int i = 0; i < clusterList.getLength(); i++) {
			Node clusterNode = clusterList.item(i);

			if (clusterNode.getNodeType() == Node.ELEMENT_NODE) {
				Element clusterElem = (Element) clusterNode;

				NodeList nameList = clusterElem.getElementsByTagName("name");
				Element nameElem = (Element) nameList.item(0);

				String clusterName = (nameElem.getChildNodes()).item(0)
						.getNodeValue();
				logger.debug("Cluster name: " + clusterName);

				String clusterType = clusterElem.getAttribute("type");
				logger.debug("Cluster type: " + clusterType);
				for (Cluster splitCluster : splitClusters) {
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
					logger.debug("Current cluster: " + splitCluster);
					if (splitCluster.name.equals(clusterName)) {
						splitCluster.type = clusterType;
					}
				}
			}

		}

		logger.debug("Showing cluster types for all split clusters...");
		for (Cluster splitCluster : splitClusters) {
			logger.debug("Current cluster: " + splitCluster);
			logger.debug("cluster type: " + splitCluster.type);
		}

	}

	public static void writeOutSmellArchToXML(ArrayList<Cluster> splitClusters)
			throws ParserConfigurationException, TransformerException,
			FileNotFoundException {

		DocumentBuilderFactory docFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("SmellArch");
		doc.appendChild(rootElement);

		Element topicsElem = doc.createElement("topics");
		rootElement.appendChild(topicsElem);
		for (TopicKey topicKey : TopicUtil.getTopicKeyListForCurrProj().set) {
			Element topicElem = doc.createElement("topic");
			topicElem.setAttribute("id", Integer.toString(topicKey.topicNum));
			topicElem.setAttribute("type", "unspec");
			topicsElem.appendChild(topicElem);

			for (String word : topicKey.getWords()) {
				Element wordElem = doc.createElement("word");
				wordElem.appendChild(doc.createTextNode(word));
				topicElem.appendChild(wordElem);
			}
		}

		for (Cluster cluster : splitClusters) {
			String strippedLeafSplitClusterName = ConfigUtil
					.stripParensEnclosedClassNameWithPackageName(cluster);

			if (Pattern.matches(ConfigUtil.anonymousInnerClassRegExpr,
					strippedLeafSplitClusterName)) {
				continue;
			}

			if (Pattern.matches(ConfigUtil.doubleInnerClassRegExpr,
					strippedLeafSplitClusterName)) {
				continue;
			}

			Element clusterElement = doc.createElement("Cluster");
			clusterElement.setAttribute("type", "unspec");
			rootElement.appendChild(clusterElement);

			Element nameElem = doc.createElement("name");
			nameElem.appendChild(doc.createTextNode(cluster.name));
			// Element tgt = doc.createElement("tgt");
			// tgt.appendChild(doc.createTextNode(e.getTgt().toString()));
			clusterElement.appendChild(nameElem);

			Element classesElem = doc.createElement("classes");
			clusterElement.appendChild(classesElem);

			for (MyClass myClass : cluster.getClasses()) {
				Element classElem = doc.createElement("class");
				classElem.appendChild(doc.createTextNode(myClass.className));
				classesElem.appendChild(classElem);

				Element methodsElem = doc.createElement("methods");
				classElem.appendChild(methodsElem);
				for (MyMethod myMethod : myClass.getMethods()) {
					Element methodElem = doc.createElement("method");
					methodElem.appendChild(doc.createTextNode(myMethod
							.toString()));
					methodsElem.appendChild(methodElem);
				}
				// for (MyClass myclass : )
			}

			Element docTopicElem = doc.createElement("doc-topic");
			clusterElement.appendChild(docTopicElem);

			for (TopicItem topicItem : cluster.docTopicItem.topics) {
				Element topicElem = doc.createElement("topic");
				topicElem.setAttribute("id",
						Integer.toString(topicItem.topicNum));
				topicElem.setAttribute("type", "unspec");
				topicElem.appendChild(doc.createTextNode(Double
						.toString(topicItem.proportion)));
				docTopicElem.appendChild(topicElem);
			}

			// clusterElement.appendChild(tgt);
		}

		// write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory
				.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(
				"{http://xml.apache.org/xslt}indent-amount", "2");
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new File(
				Config.getXMLSmellArchFilename()));
		transformer.transform(source, result);

		logger.debug("In "
				+ Thread.currentThread().getStackTrace()[1].getClassName()
				+ ". "
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ ", Wrote " + Config.getXMLSmellArchFilename());

	}

	public static void classifyClustersBasedOnTopicTypes(
			ArrayList<Cluster> splitClusters) {

		for (Cluster cluster : splitClusters) {
			double specTypeWeight = 0;
			double indepTypeWeight = 0;

			String strippedLeafSplitClusterName = ConfigUtil
					.stripParensEnclosedClassNameWithPackageName(cluster);

			if (Pattern.matches(ConfigUtil.anonymousInnerClassRegExpr,
					strippedLeafSplitClusterName)) {
				continue;
			}

			if (Pattern.matches(ConfigUtil.doubleInnerClassRegExpr,
					strippedLeafSplitClusterName)) {
				continue;
			}

			for (TopicItem topicItem : cluster.docTopicItem.topics) {
				if (topicItem.type.equals("indep")) {
					indepTypeWeight += topicItem.proportion;
				} else if (topicItem.type.equals("spec")) {
					specTypeWeight += topicItem.proportion;
				} else {
					logger.error("Invalid type for topicItem: " + topicItem
							+ " in " + cluster);
					System.exit(1);
				}
			}
			if (indepTypeWeight > specTypeWeight) {
				cluster.type = "indep";
			} else {
				cluster.type = "spec";
			}
		}

	}

	public static void writeOutSpecifiedSmellArchToXML(
			ArrayList<Cluster> splitClusters, HashSet<TopicKey> topicKeys)
			throws ParserConfigurationException, TransformerException,
			FileNotFoundException {

		DocumentBuilderFactory docFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("SmellArch");
		doc.appendChild(rootElement);

		Element topicsElem = doc.createElement("topics");
		rootElement.appendChild(topicsElem);
		for (TopicKey topicKey : topicKeys) {
			Element topicElem = doc.createElement("topic");
			topicElem.setAttribute("id", Integer.toString(topicKey.topicNum));
			topicElem.setAttribute("type", topicKey.type);
			topicsElem.appendChild(topicElem);

			for (String word : topicKey.getWords()) {
				Element wordElem = doc.createElement("word");
				wordElem.appendChild(doc.createTextNode(word));
				topicElem.appendChild(wordElem);
			}
		}

		for (Cluster cluster : splitClusters) {
			String strippedLeafSplitClusterName = ConfigUtil
					.stripParensEnclosedClassNameWithPackageName(cluster);

			if (Pattern.matches(ConfigUtil.anonymousInnerClassRegExpr,
					strippedLeafSplitClusterName)) {
				continue;
			}

			if (Pattern.matches(ConfigUtil.doubleInnerClassRegExpr,
					strippedLeafSplitClusterName)) {
				continue;
			}

			Element clusterElement = doc.createElement("Cluster");
			clusterElement.setAttribute("type", cluster.type);
			rootElement.appendChild(clusterElement);

			Element nameElem = doc.createElement("name");
			nameElem.appendChild(doc.createTextNode(cluster.name));
			// Element tgt = doc.createElement("tgt");
			// tgt.appendChild(doc.createTextNode(e.getTgt().toString()));
			clusterElement.appendChild(nameElem);

			Element classesElem = doc.createElement("classes");
			clusterElement.appendChild(classesElem);

			for (MyClass myClass : cluster.getClasses()) {
				Element classElem = doc.createElement("class");
				classElem.appendChild(doc.createTextNode(myClass.className));
				classesElem.appendChild(classElem);

				Element methodsElem = doc.createElement("methods");
				classElem.appendChild(methodsElem);
				for (MyMethod myMethod : myClass.getMethods()) {
					Element methodElem = doc.createElement("method");
					methodElem.appendChild(doc.createTextNode(myMethod
							.toString()));
					methodsElem.appendChild(methodElem);
				}
				// for (MyClass myclass : )
			}

			Element docTopicElem = doc.createElement("doc-topic");
			clusterElement.appendChild(docTopicElem);

			for (TopicItem topicItem : cluster.docTopicItem.topics) {
				Element topicElem = doc.createElement("topic");
				topicElem.setAttribute("id",
						Integer.toString(topicItem.topicNum));
				topicElem.setAttribute("type", topicItem.type);
				topicElem.appendChild(doc.createTextNode(Double
						.toString(topicItem.proportion)));
				docTopicElem.appendChild(topicElem);
			}

			// clusterElement.appendChild(tgt);
		}

		// write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory
				.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(
				"{http://xml.apache.org/xslt}indent-amount", "2");
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new File(
				Config.getSpecifiedSmallArchFromXML()));
		transformer.transform(source, result);

		logger.debug("In "
				+ Thread.currentThread().getStackTrace()[1].getClassName()
				+ ". "
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ ", Wrote " + Config.getSpecifiedSmallArchFromXML());

	}

	public static double computeCentroidUsingStructuralData(FastCluster cluster) {

		double centroidSum = 0;

		/* double[] features = cluster.getFeatures(); */

		Set<Integer> clusterKeys = cluster.getNonZeroFeatureMap().keySet();

		/*
		 * for (int i=0; i<features.length; i++) { centroidSum += features[i]; }
		 */

		for (Integer key : clusterKeys) {
			centroidSum += cluster.getNonZeroFeatureMap().get(key)
					.doubleValue();
		}

		double centroidAvg = centroidSum / cluster.getFeaturesLength();

		double centroid = centroidAvg / cluster.getNumEntities();

		return centroid;
	}

	public static double computeCentroidUsingStructuralData(FeatureVector fv) {

		double centroidSum = 0;

		for (Feature f : fv) {
			centroidSum += f.value;
		}

		double centroidAvg = centroidSum / fv.size();

		return centroidAvg;
	}

	public static double computeGlobalCentroidForStructuralData(
			ArrayList<Double> clusterCentroids) {

		double centroidSum = 0;

		for (Double centroid : clusterCentroids) {
			centroidSum += centroid.doubleValue();
		}

		return centroidSum / clusterCentroids.size();
	}

	public static double computeClusterGainUsingStructuralDataFromFeatureVectorMap(
			ArrayList<Cluster> clusters) {
		ArrayList<Double> clusterCentroids = new ArrayList<Double>();

		for (Cluster c : clusters) {
			clusterCentroids.add(new Double(
					computeCentroidUsingStructuralData(c)));
		}

		double globalCentroid = computeGlobalCentroidForStructuralData(clusterCentroids);

		double clusterGain = 0;
		for (int i = 0; i < clusterCentroids.size(); i++) {
			clusterGain += (clusters.get(i).items.size() - 1)
					* Math.pow(
							Math.abs(globalCentroid
									- clusterCentroids.get(i).doubleValue()), 2);
		}

		return clusterGain;
	}

	public static double computeClusterGainUsingStructuralDataFromFastFeatureVectors(
			ArrayList<FastCluster> fastClusters) {
		ArrayList<Double> clusterCentroids = new ArrayList<Double>();

		for (FastCluster cluster : fastClusters) {
			double centroid = computeCentroidUsingStructuralData(cluster);
			clusterCentroids.add(centroid);
		}

		double globalCentroid = computeGlobalCentroidForStructuralData(clusterCentroids);

		double clusterGain = 0;
		for (int i = 0; i < clusterCentroids.size(); i++) {
			clusterGain += (fastClusters.get(i).getNumEntities() - 1)
					* Math.pow(
							Math.abs(globalCentroid
									- clusterCentroids.get(i).doubleValue()), 2);
		}

		return clusterGain;
	}

	public static DocTopicItem computeGlobalCentroidUsingTopics(
			ArrayList<DocTopicItem> docTopicItems) {
		int firstNonNullDocTopicItemIndex = 0;
		for (; docTopicItems.get(firstNonNullDocTopicItemIndex) == null
				&& firstNonNullDocTopicItemIndex < docTopicItems.size(); firstNonNullDocTopicItemIndex++) {
		}
		DocTopicItem mergedDocTopicItem = new DocTopicItem(
				docTopicItems.get(firstNonNullDocTopicItemIndex));
		for (int i = firstNonNullDocTopicItemIndex; i < docTopicItems.size(); i++) {
			if (docTopicItems.get(i) == null)
				continue;
			DocTopicItem currDocTopicItem = docTopicItems.get(i);
			mergedDocTopicItem = TopicUtil.mergeDocTopicItems(
					mergedDocTopicItem, currDocTopicItem);
		}
		return mergedDocTopicItem;
	}

	public static double computeClusterGainUsingTopics(
			ArrayList<FastCluster> clusters) {
		ArrayList<DocTopicItem> docTopicItems = new ArrayList<DocTopicItem>();
		for (FastCluster c : clusters) {
			docTopicItems.add(c.docTopicItem);
		}
		DocTopicItem globalDocTopicItem = computeGlobalCentroidUsingTopics(docTopicItems);
		logger.debug("Global Centroid Using Topics: "
				+ globalDocTopicItem.toStringWithLeadingTabsAndLineBreaks(0));

		double clusterGain = 0;

		for (int i = 0; i < docTopicItems.size(); i++) {
			clusterGain += (clusters.get(i).getNumEntities() - 1)
					* TopicUtil.jsDivergence(docTopicItems.get(i),
							globalDocTopicItem);
		}

		return clusterGain;

	}

	public static HashMap<String, Integer> createClusterNameToNodeNumberMap(
			ArrayList<Cluster> clusters) {
		HashMap<String, Integer> clusterNameToNodeNumberMap = new HashMap<String, Integer>();
		for (int i = 0; i < clusters.size(); i++) {
			Cluster cluster = clusters.get(i);
			clusterNameToNodeNumberMap.put(cluster.name, new Integer(i));
		}
		return clusterNameToNodeNumberMap;
	}

	public static HashMap<String, Integer> createFastClusterNameToNodeNumberMap(
			List<FastCluster> clusters) {
		HashMap<String, Integer> clusterNameToNodeNumberMap = new HashMap<String, Integer>();
		for (int i = 0; i < clusters.size(); i++) {
			FastCluster cluster = clusters.get(i);
			clusterNameToNodeNumberMap.put(cluster.getName(), new Integer(i));
		}
		return clusterNameToNodeNumberMap;
	}

	public static TreeMap<Integer, String> createNodeNumberToClusterNameMap(
			ArrayList<Cluster> clusters,
			HashMap<String, Integer> clusterNameToNodeNumberMap) {
		TreeMap<Integer, String> nodeNumberToClusterNameMap = new TreeMap<Integer, String>();

		for (Cluster cluster : clusters) {
			nodeNumberToClusterNameMap.put(
					clusterNameToNodeNumberMap.get(cluster.name), cluster.name);
		}

		return nodeNumberToClusterNameMap;
	}

	public static TreeMap<Integer, String> createNodeNumberToFastClusterNameMap(
			List<FastCluster> clusters,
			HashMap<String, Integer> clusterNameToNodeNumberMap) {
		TreeMap<Integer, String> nodeNumberToClusterNameMap = new TreeMap<Integer, String>();

		for (FastCluster cluster : clusters) {
			nodeNumberToClusterNameMap.put(
					clusterNameToNodeNumberMap.get(cluster.getName()),
					cluster.getName());
		}

		return nodeNumberToClusterNameMap;
	}

	public static ByteArrayOutputStream writeRSFToByteArrayOutputStream(
			HashMap<String, Integer> clusterNameToNodeNumberMap,
			ArrayList<Cluster> clusters) throws UnsupportedEncodingException {

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		OutputStreamWriter osw = new OutputStreamWriter(bos);
		PrintWriter out = new PrintWriter(osw);

		logger.debug("Printing each cluster and its leaves...");
		for (Cluster cluster : clusters) {
			Integer currentNodeNumber = clusterNameToNodeNumberMap
					.get(cluster.name);
			logger.debug("Cluster name: " + currentNodeNumber);
			logger.debug("Cluster node number: " + cluster);
			for (Cluster leafCluster : cluster.leafClusters) {
				logger.debug("\t" + leafCluster);
				out.write("contain " + currentNodeNumber + " "
						+ leafCluster.name + '\n');
			}
		}

		out.close();

		logger.debug("Printing sourceStream from writeRSFToByteArrayOutputStream...");
		logger.debug(DebugUtil.convertByteArrayOutputStreamToString(bos));

		return bos;

	}

	public static void writeClusterRSFFile(
			HashMap<String, Integer> clusterNameToNodeNumberMap,
			ArrayList<Cluster> clusters) throws FileNotFoundException,
			UnsupportedEncodingException {
		File rsfFile = new File(Config.getClustersRSFFilename(clusters.size()));

		FileOutputStream fos = new FileOutputStream(rsfFile);
		OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
		PrintWriter out = new PrintWriter(osw);

		logger.debug("Printing each cluster and its leaves...");
		for (Cluster cluster : clusters) {
			Integer currentNodeNumber = clusterNameToNodeNumberMap
					.get(cluster.name);
			logger.debug("Cluster name: " + currentNodeNumber);
			logger.debug("Cluster node number: " + cluster);
			for (Cluster leafCluster : cluster.leafClusters) {
				logger.debug("\t" + leafCluster);
				out.println("contain " + currentNodeNumber + " "
						+ leafCluster.name);
			}
		}

		out.close();
	}

	public static void writeFastClusterRSFFileUsingConfigName(
			HashMap<String, Integer> clusterNameToNodeNumberMap,
			List<FastCluster> clusters) throws FileNotFoundException,
			UnsupportedEncodingException {
		String currentClustersDetailedRsfFilename = Config.getClustersRSFFilename(clusters.size());
		writeFastClustersRsfFile(clusterNameToNodeNumberMap, clusters,
				currentClustersDetailedRsfFilename);
	}

	public static void writeFastClustersRsfFile(
			HashMap<String, Integer> clusterNameToNodeNumberMap,
			List<FastCluster> clusters,
			String currentClustersDetailedRsfFilename)
			throws FileNotFoundException, UnsupportedEncodingException {
		File rsfFile = new File(currentClustersDetailedRsfFilename);

		FileOutputStream fos = new FileOutputStream(rsfFile);
		OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
		PrintWriter out = new PrintWriter(osw);

		logger.trace("Printing each cluster and its leaves...");
		for (FastCluster cluster : clusters) {
			Integer currentNodeNumber = clusterNameToNodeNumberMap.get(cluster
					.getName());
			logger.trace("Cluster name: " + currentNodeNumber);
			logger.trace("Cluster node number: " + cluster);
			String[] entities = cluster.getName().split(",");
			Set<String> entitiesSet = new HashSet<String>( Arrays.asList(entities) );
			int entityCount = 0;
			for (String entity : entitiesSet) {
				/*String clusterLimitedName = DebugUtil.getLimitedString(
						cluster.getName(), 1000);*/
				logger.trace(entityCount + ":\t" + entity);
				out.println("contain " + currentNodeNumber + " " + entity);
				entityCount++;
			}
		}

		out.close();
	}

	public static void printSimilarFeatures(FastCluster c1, FastCluster c2,
			FastFeatureVectors fastFeatureVectors) {
		ArrayList<String> names = fastFeatureVectors.getNamesInFeatureSet();

		int characterLimit = 1000;
		String c1LimitedName = DebugUtil.getLimitedString(c1.getName(),
				characterLimit);
		String c2LimitedName = DebugUtil.getLimitedString(c2.getName(),
				characterLimit);

		logger.debug("Features shared between " + c1LimitedName + " and "
				+ c2LimitedName);

		/*
		 * for (int i=0;i<features1.length;i++) { if (features1[i] > 0 &&
		 * features2[i] > 0) { logger.debug(names.get(i)); } }
		 */

		Set<Integer> c1Keys = c1.getNonZeroFeatureMap().keySet();

		for (Integer key : c1Keys) {
			if (c1.getNonZeroFeatureMap().get(key) != null
					&& c2.getNonZeroFeatureMap().get(key) != null) {
				logger.debug(names.get(key));
			}
		}

	}

	public static FastFeatureVectors deserializeFastFeatureVectors() {
		File fastFeatureVectorsFile = new File(
				Config.getFastFeatureVectorsFilename());
		FastFeatureVectors fastFeatureVectors = null;
		try {
			ObjectInputStream objInStream = new ObjectInputStream(
					new FileInputStream(fastFeatureVectorsFile));

			

			// Deserialize the object

			fastFeatureVectors = (FastFeatureVectors) objInStream.readObject();
			logger.debug("Names in Feature Set:");
			logger.debug(fastFeatureVectors.getNamesInFeatureSet());

		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.debug("Read in serialized feature vectors...");

		return fastFeatureVectors;
	}

	public static Set<String> getNodesInClusterGraph(StringGraph cg) {
		HashSet<String> nodes = new HashSet<String>();
		for (StringEdge edge : cg.edges) {
			nodes.add(edge.srcStr.trim());
			nodes.add(edge.tgtStr.trim());
		}
		return nodes;
	}

	public static Set<String> getClassesInClusters(
			Set<ConcernCluster> clusters) {
		Set<String> classes = new HashSet<String>();
		for (ConcernCluster cluster : clusters) {
			for (String entity : cluster.getEntities()) {
				classes.add(entity.trim());
			}
		}
		return classes;
	}
	
	public static Set<StringGraph> buildInternalGraphs(HashMap<String, Type> typeMap,
			Set<ConcernCluster> clusters) {
		Set<StringGraph> graphs = new HashSet<StringGraph>();
		for (ConcernCluster cluster : clusters) {
			StringGraph currGraph = new StringGraph(cluster.getName().trim());
			for (String entity : cluster.getEntities()) {
				Type type = typeMap.get(entity.trim());
				if (type != null) {
					Dependencies dependencies = type.getDependencies();
					for (DependsOn dependency : dependencies.getDependsOn()) {
						for (String otherEntity : cluster.getEntities()) {
							if (!entity.equals(otherEntity)) {
								if (otherEntity.trim().equals(
										dependency.getName().trim())) {
									StringEdge newEdge = new StringEdge(entity.trim(), otherEntity.trim());
									newEdge.setType(dependency.getClassification());
									currGraph.addEdge(newEdge);
								}
							}
						}
					}
				}
					
				
			}
			graphs.add(currGraph);
		}
		return graphs;
	}
	
	public static StringGraph buildClusterGraphUsingDepMap(Map<String,Set<String>> depMap,
			Set<ConcernCluster> clusters) {
		StringGraph cg = new StringGraph();
		for (ConcernCluster cluster : clusters) {
			for (String entity : cluster.getEntities()) {
				if (depMap.containsKey(entity.trim())) {
					Set<String> dependencies = depMap.get(entity);
					for (String dependency : dependencies) {
						for (ConcernCluster otherCluster : clusters) {
							for (String otherEntity : otherCluster
									.getEntities()) {
								if (otherEntity.trim()
										.equals(dependency.trim())) {
									cg.addEdge(cluster.getName().trim(),
											otherCluster.getName().trim());
								}
							}
						}
					}
				}
			}
		}
		
		
		
		return cg;
	}
	
	public static StringGraph buildClusterGraphUsingOdemClasses(HashMap<String, Type> typeMap,
			Set<ConcernCluster> clusters) {
		StringGraph cg = new StringGraph();
		for (ConcernCluster cluster : clusters) {
			for (String entity : cluster.getEntities()) {
				Type type = typeMap.get(entity.trim());
				if (type != null) {
					Dependencies dependencies = type.getDependencies();
					for (DependsOn dependency : dependencies.getDependsOn()) {
						for (ConcernCluster otherCluster : clusters) {
							for (String otherEntity : otherCluster
									.getEntities()) {
								if (otherEntity.trim().equals(
										dependency.getName().trim())) {
									cg.addEdge(cluster.getName().trim(),
											otherCluster.getName().trim());
								}
							}
						}
					}
				}
			}
		}
		
		
		
		return cg;
	}

	public static Set<ConcernCluster> buildGroundTruthClustersFromPackages(
			Set<String> topLevelPackagesOfUnclusteredClasses,
			Set<String> unClusteredClasses) {
		Set<ConcernCluster> clusters = new HashSet<ConcernCluster>();
		for (String pkg : topLevelPackagesOfUnclusteredClasses) {
			ConcernCluster cluster = new ConcernCluster();
			cluster.setName(pkg.trim());
			for (String clazz : unClusteredClasses) {
				if (clazz.trim().startsWith(pkg.trim())) {
					cluster.addEntity(clazz);
				}
			}
			clusters.add(cluster);
		}
		return clusters;
	}

	public static ByteArrayOutputStream convertExpertDecompositionToByteArrayOutputStream(
			ExpertDecomposition decomposition) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		OutputStreamWriter osw = new OutputStreamWriter(bos);
		PrintWriter out = new PrintWriter(osw);

		logger.debug("Printing each expert decompostion in rsf format that is being converted to ByteArrayOutputStream...");
		int groupCount = 0;
		for (Group group : decomposition.groups) {
			for (String element : group.elements) {
				String rsfTriple = "contain " + groupCount + " " + element + "\n";
				logger.debug(rsfTriple);
				out.write(rsfTriple); 
			}
			groupCount++;
		}

		out.close();

		logger.debug("Printing sourceStream from writeRSFToByteArrayOutputStream...");
		logger.debug(DebugUtil.convertByteArrayOutputStreamToString(bos));

		return bos;
	}
	
	
	/**
	 * 
	 * Creates a map of a cluster name to its entities
	 * 
	 * @param clusterFacts
	 * @return
	 */
	public static Map<String,Set<String>> buildClusterMap(List<List<String>> clusterFacts ) {
		
		Map<String,Set<String>> clusterMap = new HashMap<String,Set<String>>();
		
		for (List<String> fact : clusterFacts) {
			String clusterName = fact.get(1);
			String entity = fact.get(2);
			if (clusterMap.get(clusterName) == null) {
				Set<String> entities = new HashSet<String>();
				entities.add(entity);
				clusterMap.put(clusterName,entities);
			}
			else {
				Set<String> entities = clusterMap.get(clusterName);
				entities.add(entity);
				clusterMap.put(clusterName,entities);
			}
		}
		
		logger.trace("Resulting clusterMap:");
		for (Entry<String,Set<String>> entry : clusterMap.entrySet()) {
			logger.trace(entry.getKey());
			for (String entity : entry.getValue()) {
				logger.trace("\t" + entity);
			}
		}
		
		return clusterMap;
		
	}
	
	public static Map<String,Set<MutablePair<String,String>>> buildInternalEdgesPerCluster(Map<String,Set<String>> clusterMap, List<List<String>> depFacts) {
		Map<String,Set<MutablePair<String,String>>> map = new HashMap<String,Set<MutablePair<String,String>>>();

		for (String clusterName : clusterMap.keySet()) { // for each cluster name
			Set<MutablePair<String,String>> edges = new HashSet<MutablePair<String,String>>();
			for (List<String> depFact : depFacts) {
				String source = depFact.get(1);
				String target = depFact.get(2);
				if (clusterMap.get(clusterName).contains(source) && clusterMap.get(clusterName).contains(target)) { // check if the source and target is in the cluster
					// Add internal edge 
					MutablePair<String, String> edge = new MutablePair<String, String>();
					edge.setLeft(source);
					edge.setRight(target);
					edges.add(edge);
				}
			}
			map.put(clusterName, edges);
		}


		return map;
	}
	
	public static Map<String,Set<MutablePair<String,String>>> buildExternalEdgesPerCluster(Map<String,Set<String>> clusterMap, List<List<String>> depFacts) {
		Map<String,Set<MutablePair<String,String>>> map = new HashMap<String,Set<MutablePair<String,String>>>();

		for (String clusterName : clusterMap.keySet()) { // for each cluster name
			Set<MutablePair<String,String>> edges = new HashSet<MutablePair<String,String>>();
			for (List<String> depFact : depFacts) {
				String source = depFact.get(1);
				String target = depFact.get(2);
				if (clusterMap.get(clusterName).contains(source) && !(clusterMap.get(clusterName).contains(target)) ) { // source is in cluster, but target is not
					// Add external edge 
					MutablePair<String, String> edge = new MutablePair<String, String>();
					edge.setLeft(source);
					edge.setRight(target);
					edges.add(edge);
				}
				if (!(clusterMap.get(clusterName).contains(source)) && clusterMap.get(clusterName).contains(target)) { // target is in cluster, but source is not
					// Add external edge 
					MutablePair<String, String> edge = new MutablePair<String, String>();
					edge.setLeft(source);
					edge.setRight(target);
					edges.add(edge);
				}
			}
			map.put(clusterName, edges);
		}


		return map;
	}
	
	public static Map<String,Set<MutablePair<String,String>>> buildEdgesIntoEachCluster(Map<String,Set<String>> clusterMap, List<List<String>> depFacts) {
		Map<String,Set<MutablePair<String,String>>> map = new HashMap<String,Set<MutablePair<String,String>>>();

		for (String clusterName : clusterMap.keySet()) { // for each cluster name
			Set<MutablePair<String,String>> edges = new HashSet<MutablePair<String,String>>();
			for (List<String> depFact : depFacts) {
				String source = depFact.get(1);
				String target = depFact.get(2);
				if (!(clusterMap.get(clusterName).contains(source)) && clusterMap.get(clusterName).contains(target)) { // target is in cluster, but source is not
					// Add edge that goes into cluster 
					MutablePair<String, String> edge = new MutablePair<String, String>();
					edge.setLeft(source);
					edge.setRight(target);
					edges.add(edge);
				}
			}
			map.put(clusterName, edges);
		}


		return map;
	}
	
	public static Set<List<String>> buildClusterEdges(Map<String,Set<String>> clusterMap, List<List<String>> depFacts) {
		Set<List<String>> edges = new HashSet<List<String>>();

		for (List<String> depFact : depFacts) {
			String source = depFact.get(1);
			String target = depFact.get(2);

			for (String clusterNameSource : clusterMap.keySet()) {
				if (clusterMap.get(clusterNameSource).contains(source)) {
					for (String clusterNameTarget : clusterMap.keySet()) {
						if (clusterMap.get(clusterNameTarget).contains(target)) {
							if (!clusterNameSource.equals(clusterNameTarget)) {
								List<String> edge = new ArrayList<String>();
								edge.add(clusterNameSource);
								edge.add(clusterNameTarget);
								edges.add(edge);
							}
						}
					}
				}
			}
		}

		return edges;
	}
	
	public static void fastClusterPostProcessing(List<FastCluster> fastClusters, FastFeatureVectors fastFeatureVectors) {

		boolean outputResultingClusterGraph = false;
		StringGraph clusterGraph = ClusterUtil.generateFastClusterGraph(
				fastClusters, fastFeatureVectors.getNamesInFeatureSet());
		if (outputResultingClusterGraph) {
			logger.debug("Resulting ClusterGraph...");
			logger.debug(clusterGraph);
		}

		HashMap<String, Integer> clusterNameToNodeNumberMap = ClusterUtil
				.createFastClusterNameToNodeNumberMap(fastClusters);
		TreeMap<Integer, String> nodeNumberToClusterNameMap = ClusterUtil
				.createNodeNumberToFastClusterNameMap(fastClusters,
						clusterNameToNodeNumberMap);

		try {
			clusterGraph.writeNumberedNodeDotFileWithTextMappingFile(
					Config.getClusterGraphDotFilename(),
					clusterNameToNodeNumberMap, nodeNumberToClusterNameMap);
			ClusterUtil.writeFastClusterRSFFileUsingConfigName(clusterNameToNodeNumberMap,
					fastClusters);
			clusterGraph.writeXMLClusterGraph(Config.getClusterGraphXMLFilename());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public static Map<String, Set<String>> buildDependenciesMap(String depsRsfFilename) {
		RsfReader.loadRsfDataFromFile(depsRsfFilename);
		Iterable<List<String>> depFacts = RsfReader.filteredRoutineFacts;
		
		Map<String,Set<String>> depMap = new HashMap<String,Set<String>>();
		
		for (List<String> fact : depFacts) {
			String source = fact.get(1).trim();
			String target = fact.get(2).trim();
			Set<String> dependencies = null;
			if (depMap.containsKey(source)) {
				dependencies = depMap.get(source);
			}
			else {
				dependencies = new HashSet<String>();
			}
			dependencies.add(target);
			depMap.put(source, dependencies);
		}
		return depMap;
	}
	
	public static SimpleDirectedGraph<String, DefaultEdge> buildConcernClustersDiGraph(
			Set<ConcernCluster> clusters, String depsRsfFilename) {
		SimpleDirectedGraph<String, DefaultEdge>  directedGraph = new SimpleDirectedGraph<String,DefaultEdge>(DefaultEdge.class);
		
		for (ConcernCluster cluster : clusters) {
			directedGraph.addVertex(cluster.getName());
		}
		logger.debug("No. of vertices: " + directedGraph.vertexSet().size());
		
		RsfReader.loadRsfDataFromFile(depsRsfFilename);
		Iterable<List<String>> depFacts = RsfReader.filteredRoutineFacts;
		
		for (List<String> fact : depFacts) {
			String source = fact.get(1).trim();
			String target = fact.get(2).trim();
			directedGraph.addEdge(source, target);
        }
		logger.debug("No. of edges: " + directedGraph.edgeSet().size());
		
		return directedGraph;
	}
	
	public static SimpleDirectedGraph<String, DefaultEdge> buildConcernClustersDiGraph(
			Set<ConcernCluster> clusters, StringGraph clusterGraph) {
		SimpleDirectedGraph<String, DefaultEdge>  directedGraph = new SimpleDirectedGraph<String,DefaultEdge>(DefaultEdge.class);
		
		for (ConcernCluster cluster : clusters) {
			directedGraph.addVertex(cluster.getName());
		}
		logger.debug("No. of vertices: " + directedGraph.vertexSet().size());
		
		
		for (StringEdge stringEdge : clusterGraph.edges) {
        	if (!stringEdge.srcStr.equals(stringEdge.tgtStr))
        		directedGraph.addEdge(stringEdge.srcStr, stringEdge.tgtStr);
        }
		logger.debug("No. of edges: " + directedGraph.edgeSet().size());
		
		return directedGraph;
	}

	public static SimpleDirectedGraph<String, DefaultEdge> buildSimpleDirectedGraph(
			String depsFilename, Set<ConcernCluster> clusters) {
		String readingDepsFile = "Reading in deps file: " + depsFilename;
		System.out.println(readingDepsFile);
		logger.info(readingDepsFile);
		Map<String, Set<String>> depMap = ClusterUtil.buildDependenciesMap(depsFilename);
		
		StringGraph clusterGraph = ClusterUtil.buildClusterGraphUsingDepMap(depMap,clusters);
		
		SimpleDirectedGraph<String, DefaultEdge> actualGraph = ClusterUtil.buildConcernClustersDiGraph(
				clusters, clusterGraph);
		return actualGraph;
	}

}
