package edu.usc.softarch.arcade.topics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cc.mallet.util.Maths;
import edu.usc.softarch.arcade.clustering.Cluster;
import edu.usc.softarch.arcade.clustering.Entity;
import edu.usc.softarch.arcade.clustering.FastCluster;
import edu.usc.softarch.arcade.config.Config;
import edu.usc.softarch.arcade.config.ConfigUtil;
import edu.usc.softarch.arcade.config.Config.Language;
import edu.usc.softarch.arcade.util.DebugUtil;

/**
 * @author joshua
 */
public class TopicUtil {
	public static DocTopics docTopics;
	private static Logger logger = LogManager.getLogger(TopicUtil.class);
	
	public static String convertJavaClassWithPackageNameToDocTopicName
			(String name) {
		return name.replace("\\.", "/") + ".java";
	}

	public static double klDivergence(double[] sortedP, double[] sortedQ) {
		double divergence = 0;
		double verySmallVal = 0.00000001;
		
		for (int i=0; i < sortedP.length; i++) {
			double denominator = 0;
			double numerator = 0;

			denominator = (sortedQ[i] == 0) ? verySmallVal : sortedQ[i];
			numerator = (sortedP[i] == 0) ? 2 * verySmallVal : sortedP[i];
					
			divergence += sortedP[i] * Math.log(numerator / denominator);
		}
		
		return divergence;
	}
	
	public static double symmKLDivergence(
			DocTopicItem pDocTopicItem,
			DocTopicItem qDocTopicItem)
			throws DistributionSizeMismatchException {
		double divergence = 0;
		
		// Error due to size mismatch
		if (pDocTopicItem.size() != qDocTopicItem.size()) {
			logger.error("P size: " + pDocTopicItem.size());
			logger.error("Q size: " + qDocTopicItem.size());
			logger.error("P and Q for Kullback Leibler Divergence not the same size");
			throw new DistributionSizeMismatchException(
				"P and Q for Kullback Leibler Divergence not the same size");
		}
		
		double[] sortedP = new double[pDocTopicItem.size()];
		double[] sortedQ = new double[qDocTopicItem.size()];
		List<Integer> intersect = pDocTopicItem.intersection(qDocTopicItem);
		//TODO Test if topicNum is ever > DocTopicItem.size()
		for (Integer topicNum : intersect) {
			sortedP[topicNum] = pDocTopicItem.getTopic(topicNum).getProportion();
			sortedQ[topicNum] = qDocTopicItem.getTopic(topicNum).getProportion();
		}
		
		divergence = 0.5 *
			(klDivergence(sortedP,sortedQ) + klDivergence(sortedQ,sortedP));
		logger.debug("P distribution values: ");
		for (int i = 0; i < sortedP.length; i++)
			System.out.format("%.3f,", sortedP[i]);
		
		logger.debug("\n");

		logger.debug("Q distribution values: ");
		for (int i = 0; i < sortedQ.length; i++)
			System.out.format("%.3f,", sortedQ[i]);

		logger.debug("\n");

		logger.debug("Symmetric Kullback Leibler Divergence: " + divergence);
		
		return divergence;
	}
	
	public static double jsDivergence(
			DocTopicItem pDocTopicItem,
			DocTopicItem qDocTopicItem)
			throws DistributionSizeMismatchException {
		double divergence = 0;
		
		// Error due to size mismatch
		if (pDocTopicItem.size() != qDocTopicItem.size()) {
			logger.error("P size: " + pDocTopicItem.size());
			logger.error("Q size: " + qDocTopicItem.size());
			logger.error("P and Q for Jensen Shannon Divergence not the same size");
			throw new DistributionSizeMismatchException(
				"P and Q for Jensen Shannon Divergence not the same size");
		}
		
		double[] sortedP = new double[pDocTopicItem.size()];
		double[] sortedQ = new double[qDocTopicItem.size()];
		
		for (TopicItem pTopicItem : pDocTopicItem.getTopics())
			sortedP[pTopicItem.getTopicNum()] = pTopicItem.getProportion();
		
		for (TopicItem qTopicItem : qDocTopicItem.getTopics())
			sortedQ[qTopicItem.getTopicNum()] = qTopicItem.getProportion();

		divergence = Maths.jensenShannonDivergence(sortedP, sortedQ);
		
		logger.debug("P distribution values: ");
		for (int i = 0; i < sortedP.length; i++)
			System.out.format("%.3f,", sortedP[i]);

		logger.debug("\n");

		logger.debug("Q distribution values: ");
		for (int i = 0; i < sortedQ.length; i++)
			System.out.format("%.3f,", sortedQ[i]);

		logger.debug("\n");

		logger.debug("Jensen Shannon Divergence: " + divergence);
		logger.debug("Symmetric Kullback Leibler Divergence: "
				+ symmKLDivergence(pDocTopicItem, qDocTopicItem));
		
		return divergence;
	}
	
	public static TopicKeySet getTypedTopicKeyList() 
			throws IOException, ParserConfigurationException, SAXException {
		File smellArchXMLFile = new File(Config.getSpecifiedSmallArchFromXML());

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		dbFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		dbFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(smellArchXMLFile);
		doc.getDocumentElement().normalize();
		
		logger.debug("Root element :" + doc.getDocumentElement().getNodeName());
		NodeList topicsList = doc.getElementsByTagName("topics");
		
		logger.debug("Getting info on topics...");
		logger.debug("----------------------- size: " + topicsList.getLength());

		TopicKeySet topicKeys = TopicUtil.getTopicKeyListForCurrProj();
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

					TopicKey topicKey = topicKeys.getTopicKeyByID(topicNum);
					if (topicKey != null)
						topicKey.setType(topicItemTypeFromXML);
				}
			}
		}
		return topicKeys;
	}
	
	public static TopicKeySet getTopicKeyListForCurrProj()
			throws FileNotFoundException {
		return new TopicKeySet(Config.getMalletTopicKeysFilename());
	}
	
	public static WordTopicCounts getWordTopicCountsForCurrProj() 
			throws FileNotFoundException {
		return new WordTopicCounts(Config.getMalletWordTopicCountsFilename());
	}
	
	public static DocTopics getDocTopicsFromFile() {
		DocTopics docTopics = null;
		try {
			System.out.println("Loading doc topic file from "
				+ Config.getMalletDocTopicsFilename());
			docTopics = new DocTopics(Config.getMalletDocTopicsFilename());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return docTopics;
	}
	
	public static DocTopics getDocTopicsFromFile(String filename) {
		DocTopics docTopics = null;
		try {
			docTopics = new DocTopics(filename);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return docTopics;
	}

	public static DocTopics getDocTopicsFromVariableMalletDocTopicsFile() {
		DocTopics docTopics = null;
		try {
			System.out.println("Loading doc topic file from " + Config.getVariableMalletDocTopicsFilename());
			 docTopics = new DocTopics(Config.getVariableMalletDocTopicsFilename());
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		return docTopics;
	}
	
	public static void setDocTopicForCluster(DocTopics docTopics, Cluster leaf) {
		String strippedLeafClassName = ConfigUtil
			.stripParensEnclosedClassNameWithPackageName(leaf);
		String dollarSign = "$";
		if (strippedLeafClassName.contains(dollarSign)) {
			String anonInnerClassRegExpr = ".*\\$\\D.*";
			if (Pattern.matches(anonInnerClassRegExpr, strippedLeafClassName)) {
				logger.debug("\t\tfound inner class: " + strippedLeafClassName);
				
				strippedLeafClassName = strippedLeafClassName.substring(
					strippedLeafClassName.lastIndexOf('$') + 1,
					strippedLeafClassName.length());
				
				logger.debug("\t\tstripped to name to: " + strippedLeafClassName);
			}
		}
		
		logger.debug("\t" + strippedLeafClassName);
		leaf.docTopicItem = docTopics
			.getDocTopicItemForJava(strippedLeafClassName);
		logger.debug("\t" + "doc-topic: " + leaf.docTopicItem);
	}
	
	/** pretty much the same method as above, except uses Entities instead
	 * of FastClusters.
	 * Appends .java and ignores the entities whose names have $ sign in them 
	 * @param docTopics
	 * @param leaf
	 */
	public static void setDocTopicForEntity(DocTopics docTopics, Entity leaf, String type) throws Exception {
		if (type.equals("java")) {
			String strippedLeafClassName = ConfigUtil
					.stripParensEnclosedClassNameWithPackageName(leaf);

			String dollarSign = "$";
			if (strippedLeafClassName.contains(dollarSign)) {
				String anonInnerClassRegExpr = ".*\\$\\D.*";
				if (Pattern.matches(anonInnerClassRegExpr, strippedLeafClassName)) {
					logger.debug("\t\tfound inner class: " + strippedLeafClassName);

					strippedLeafClassName = strippedLeafClassName.substring(
							strippedLeafClassName.lastIndexOf('$') + 1,
							strippedLeafClassName.length());

					logger.debug("\t\tstripped to name to: " + strippedLeafClassName);
				}
			} else {
				logger.debug("\t" + strippedLeafClassName);
				StringBuilder sb = new StringBuilder(strippedLeafClassName);
				sb.append(".java");
				leaf.docTopicItem = docTopics.getDocTopicItemForJava(sb.toString());
			}
		}
		else if (type.equals("c")) {
			leaf.docTopicItem = docTopics.getDocTopicItemForC(leaf.name);
			if (leaf.docTopicItem == null) {
				throw new Exception("Could not obtain doc topic item for: " + leaf.name);
			}
		}
		else {
			throw new Exception("cannot set doc topic for entity with type: " + type);
		}
	}

	/**
	 * Merges the proportions of two DocTopicItems that contain the same topic
	 * numbers. Merging is done by taking the average of the proportions.
	 * 
	 * @param docTopicItem First DocTopicItem to merge.
	 * @param docTopicItem2 Second DocTopicItem to merge.
	 * @return Merged DocTopicItem.
	 * @throws UnmatchingDocTopicItemsException If the two DocTopicItems contain
	 * 	different topic numbers.
	 */
	public static DocTopicItem mergeDocTopicItems(DocTopicItem docTopicItem,
			DocTopicItem docTopicItem2) throws UnmatchingDocTopicItemsException {
		// If either argument is null, then return the non-null argument
		if (docTopicItem == null)
			return new DocTopicItem(docTopicItem2);
		if (docTopicItem2 == null)
			return new DocTopicItem(docTopicItem);

		// If arguments do not match, throw exception
		if (!docTopicItem.hasSameTopics(docTopicItem2))
			throw new UnmatchingDocTopicItemsException(
				"In mergeDocTopicItems, nonmatching docTopicItems");

		DocTopicItem mergedDocTopicItem = new DocTopicItem(docTopicItem);

		for (int i=0;i<docTopicItem.size();i++) {
			TopicItem ti1 = docTopicItem.getTopic(i);
			TopicItem ti2 = docTopicItem2.getTopic(i);
			TopicItem mergedTopicItem = mergedDocTopicItem.getTopic(i);
			
			logger.debug("ti1.topicNum: " + ti1.getTopicNum());
			logger.debug("ti2.topicNum: " + ti2.getTopicNum());
			logger.debug("ti1.proportion: " + ti1.getProportion());
			logger.debug("ti2.proportion: " + ti2.getProportion());
			
			mergedTopicItem.setProportion(
				ti1.getProportion() + ti2.getProportion() / 2);
			
			logger.debug("mergedTopicItem.topicNum: "
				+ mergedTopicItem.getTopicNum());
			logger.debug("mergedTopicItem.proportion: "
				+ mergedTopicItem.getProportion());
		}
		return mergedDocTopicItem;
	}

	/**
	 * Gets a TopicItem from a list based on its Topic Number.
	 * 
	 * @param topics A list of TopicItems.
	 * @param inTopicItem The desired TopicItem.
	 * @return The desired TopicItem if it is in the list, null otherwise.
	 * @deprecated
	 */
	public static TopicItem getMatchingTopicItem(List<TopicItem> topics,
			TopicItem inTopicItem) {
		for (TopicItem currTopicItem : topics)
			if (currTopicItem.getTopicNum() == inTopicItem.getTopicNum())
				return currTopicItem;

		return null;
	}

	/**
	 * Gets a TopicItem from a list based on its Topic Number.
	 * 
	 * @param topics A list of TopicItems.
	 * @param inTopicItem The desired TopicItem's number.
	 * @return The desired TopicItem if it is in the list, null otherwise.
	 */
	public static TopicItem getMatchingTopicItem(List<TopicItem> topics,
			int inTopicItem) {
		for (TopicItem currTopicItem : topics)
			if (currTopicItem.getTopicNum() == inTopicItem)
				return currTopicItem;

		return null;
	}

	public static void setDocTopicForFastClusterForMalletFile(
			DocTopics docTopics, FastCluster leaf) {
		String strippedLeafClasName = leaf.getName();
		String dollarSign = "$";

		if (strippedLeafClasName.contains(dollarSign)) {
			String anonInnerClassRegExpr = ".*\\$\\D.*";
			if (Pattern.matches(anonInnerClassRegExpr,
					strippedLeafClasName)) {
				logger.debug("\t\tfound inner class: " + strippedLeafClasName);
				
				strippedLeafClasName = strippedLeafClasName.substring(
						strippedLeafClasName.lastIndexOf('$') + 1,
						strippedLeafClasName.length());
				
				logger.debug("\t\tstripped to name to: " + strippedLeafClasName);
			}
		}
		
		logger.debug("\t" + strippedLeafClasName);
		if (Config.getSelectedLanguage().equals(Language.c)) {
			leaf.docTopicItem = docTopics.getDocTopicItemForC(strippedLeafClasName);
			logger.debug("set " + ((leaf.docTopicItem == null) ? "null" : leaf.docTopicItem.getSource())  + " as doc topic for " +  strippedLeafClasName);
		}
		else if (Config.getSelectedLanguage().equals(Language.java)) {
			String docTopicName = convertJavaClassWithPackageNameToDocTopicName( leaf.getName() );
			leaf.docTopicItem = docTopics.getDocTopicItemForJava(docTopicName);
		}
		else
			leaf.docTopicItem = docTopics.getDocTopicItemForJava(strippedLeafClasName);
		logger.debug("\t" + "doc-topic: " + leaf.docTopicItem);
	}

	/**
	 * Sets the DocTopicItem of a FastCluster.
	 * 
	 * @deprecated
	 */
	public static void setDocTopicForFastClusterForMalletApi(FastCluster c) {
		setDocTopicForFastClusterForMalletApi(c, Config.getSelectedLanguage().name());
	}

	/**
	 * Sets the DocTopicItem of a FastCluster.
	 */
	public static void setDocTopicForFastClusterForMalletApi(
			FastCluster c, String language) {
		c.docTopicItem = docTopics.getDocTopicItem(c.getName(), language);
	}

	// #region DEBUG -------------------------------------------------------------
	/**
	 * Prints two DocTopicItems to the debug logger. The two DocTopicItems are
	 * expected to contain the same TopicItem numbers.
	 * 
	 * @param docTopicItem
	 * @param docTopicItem2
	 */
	public static void printTwoDocTopics(DocTopicItem docTopicItem,
			DocTopicItem docTopicItem2) {
		// If either argument is null, do nothing.
		if (docTopicItem == null) {
			logger.debug(DebugUtil.addMethodInfo(" first arg is null...returning"));
			return; //TODO throw exception
		}
		if (docTopicItem2 == null) {
			logger.debug(DebugUtil.addMethodInfo(" second arg is null...returning"));
			return; //TODO throw exception
		}
		
		// Get all topic numbers
		Set<Integer> topicNumbers = docTopicItem.getTopicNumbers();
		
		// Print the source of each DocTopicItem
		logger.debug(String.format(
			"%5s%64s%64s\n",
			"",
			docTopicItem.getSource(),
			docTopicItem2.getSource()));
		
		// For each topic number, print the proportions
		for (Integer i : topicNumbers)
			logger.debug(String.format(
				"%32s%32f%32f\n",
				docTopicItem.getTopic(i).getTopicNum(),
				docTopicItem.getTopic(i).getProportion(),
				docTopicItem2.getTopic(i).getProportion()));
	}
	// #endregion DEBUG ----------------------------------------------------------
}