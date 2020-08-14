package edu.usc.softarch.arcade.topics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
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
import edu.usc.softarch.arcade.topics.DocTopicItem.Sort;
import edu.usc.softarch.arcade.util.DebugUtil;

/**
 * @author joshua
 */
public class TopicUtil {
	public static DocTopics docTopics;
	private static Logger logger = Logger.getLogger(TopicUtil.class);
	
	public static String convertJavaClassWithPackageNameToDocTopicName(String name) {
		return name.replaceAll("\\.", "/") + ".java";
	}

	public static double klDivergence(double[] sortedP, double[] sortedQ) {
		double divergence = 0;
		
		double verySmallVal = 0.00000001;
		
		for (int i=0;i<sortedP.length;i++) {
			
			double denominator = 0;
			double numerator = 0;
			
			if ( sortedQ[i] == 0) {
				denominator = verySmallVal;
			}
			else {
				denominator = sortedQ[i];
			}
			if (sortedP[i] == 0) {
				numerator = 2*verySmallVal;
			}
			else {
				numerator = sortedP[i];
			}
					
			divergence += sortedP[i]*Math.log(numerator/denominator);
		}
		
		return divergence;
	}
	
	public static double symmKLDivergence(DocTopicItem pDocTopicItem, DocTopicItem qDocTopicItem) {
		double divergence = 0;
		
		if (pDocTopicItem.topics.size() != qDocTopicItem.topics.size()) {
			logger.error("P size: " + pDocTopicItem.topics.size());
			logger.error("Q size: " + qDocTopicItem.topics.size());
			logger.error("P and Q for Kullback Leibler Divergence not the same size...exiting");
			System.exit(0);
		}
		
		double[] sortedP = new double[pDocTopicItem.topics.size()];
		double[] sortedQ = new double[qDocTopicItem.topics.size()]; 
		
		for (TopicItem pTopicItem : pDocTopicItem.topics) {
			for (TopicItem qTopicItem : qDocTopicItem.topics) {
				if (pTopicItem.topicNum == qTopicItem.topicNum) {
					sortedP[pTopicItem.topicNum] = pTopicItem.proportion;
					sortedQ[qTopicItem.topicNum] = qTopicItem.proportion;
				}
			}
		}
		
		divergence = .5*( klDivergence(sortedP,sortedQ) + klDivergence(sortedQ,sortedP) );
		logger.debug("P distribution values: ");
		for (int i = 0; i < sortedP.length; i++) {
			System.out.format("%.3f,", sortedP[i]);
		}
		logger.debug("\n");

		logger.debug("Q distribution values: ");
		for (int i = 0; i < sortedQ.length; i++) {
			System.out.format("%.3f,", sortedQ[i]);
		}
		logger.debug("\n");

		logger.debug("Symmetric Kullback Leibler Divergence: "
				+ divergence);
		
		return divergence;
	}
	
	public static double jsDivergence(DocTopicItem pDocTopicItem, DocTopicItem qDocTopicItem) {
		double divergence = 0;
		
		if (pDocTopicItem.topics.size() != qDocTopicItem.topics.size()) {
			logger.error("P size: " + pDocTopicItem.topics.size());
			logger.error("Q size: " + qDocTopicItem.topics.size());
			logger.error("P and Q for Jensen Shannon Divergence not the same size...exiting");
			System.exit(0);
		}
		
		double[] sortedP = new double[pDocTopicItem.topics.size()];
		double[] sortedQ = new double[qDocTopicItem.topics.size()];
		
		
		for (TopicItem pTopicItem : pDocTopicItem.topics) {
			sortedP[pTopicItem.topicNum] = pTopicItem.proportion;
		}
		
		for (TopicItem qTopicItem : qDocTopicItem.topics) {
			sortedQ[qTopicItem.topicNum] = qTopicItem.proportion;
		}

		divergence = Maths.jensenShannonDivergence(sortedP, sortedQ);
		
		logger.debug("P distribution values: ");
		for (int i = 0; i < sortedP.length; i++) {
			System.out.format("%.3f,", sortedP[i]);
		}
		logger.debug("\n");

		logger.debug("Q distribution values: ");
		for (int i = 0; i < sortedQ.length; i++) {
			System.out.format("%.3f,", sortedQ[i]);
		}
		logger.debug("\n");

		logger.debug("Jensen Shannon Divergence: " + divergence);
		logger.debug("Symmetric Kullback Leibler Divergence: "
				+ symmKLDivergence(pDocTopicItem, qDocTopicItem));
		
		return divergence;
	}
	
	public static TopicKeySet getTypedTopicKeyList() throws IOException, ParserConfigurationException, SAXException {
		File smellArchXMLFile = new File(Config.getSpecifiedSmallArchFromXML());
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(smellArchXMLFile);
		doc.getDocumentElement().normalize();
		
		logger.debug("Root element :"
				+ doc.getDocumentElement().getNodeName());
		NodeList topicsList = doc.getElementsByTagName("topics");
		
		
		logger.debug("Getting info on topics...");
		logger.debug("----------------------- size: "
				+ topicsList.getLength());

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

					for (TopicKey topicKey : topicKeys.set) {
						if (topicKey.topicNum == topicNum) {
							topicKey.type = topicItemTypeFromXML;
						}
					}

				}
			}
		}
		return topicKeys;
	}
	
	public static TopicKeySet getTopicKeyListForCurrProj() throws FileNotFoundException {
		return new TopicKeySet(Config.getMalletTopicKeysFilename());
	}
	
	public static WordTopicCounts getWordTopicCountsForCurrProj() throws FileNotFoundException {
		return new WordTopicCounts(Config.getMalletWordTopicCountsFilename());
	}
	
	public static DocTopics getDocTopicsFromFile() {
		DocTopics docTopics = null;
		try {
			System.out.println("Loading doc topic file from " + Config.getMalletDocTopicsFilename());
			 docTopics = new DocTopics(
					Config.getMalletDocTopicsFilename());
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		return docTopics;
	}
	
	public static DocTopics getDocTopicsFromFile(String filename) {
		DocTopics docTopics = null;
		try {
			docTopics = new DocTopics(filename);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
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
			// ".*$[a-zA-Z]+
			String anonInnerClassRegExpr = ".*\\$\\D.*";
			if (Pattern.matches(anonInnerClassRegExpr,
					strippedLeafClassName)) {
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

	public static void setDocTopicForFastClusterForMalletFile(DocTopics docTopics, FastCluster leaf) {
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
			logger.debug("set " + ((leaf.docTopicItem == null) ? "null" : leaf.docTopicItem.source)  + " as doc topic for " +  strippedLeafClasName);
		}
		else if (Config.getSelectedLanguage().equals(Language.java)) {
			String docTopicName = convertJavaClassWithPackageNameToDocTopicName( leaf.getName() );
			leaf.docTopicItem = docTopics.getDocTopicItemForJava(docTopicName);
		}
		else
			leaf.docTopicItem = docTopics.getDocTopicItemForJava(strippedLeafClasName);
		logger.debug("\t" + "doc-topic: " + leaf.docTopicItem);
	}
	
	public static DocTopicItem getDocTopicForString(DocTopics docTopics, String element) {
		String strippedLeafClasName = element;
		String dollarSign = "$";
		if (strippedLeafClasName.contains(dollarSign)) {
			String anonInnerClassRegExpr = ".*\\$\\D.*";
			if (Pattern.matches(anonInnerClassRegExpr, strippedLeafClasName)) {
				logger.debug("\t\tfound inner class: " + strippedLeafClasName);
				
				strippedLeafClasName = strippedLeafClasName.substring(
						strippedLeafClasName.lastIndexOf('$') + 1,
						strippedLeafClasName.length());
				
				logger.debug("\t\tstripped to name to: " + strippedLeafClasName);
			}
		}
		
		DocTopicItem docTopicItem = null;
		logger.debug("\t" + strippedLeafClasName);
		if (Config.getSelectedLanguage().equals(Language.c))
			docTopicItem =  docTopics.getDocTopicItemForC(strippedLeafClasName);
		else if (Config.getSelectedLanguage().equals(Language.java))
			docTopicItem =  docTopics.getDocTopicItemForJava(strippedLeafClasName);
		else
			docTopicItem =  docTopics.getDocTopicItemForJava(strippedLeafClasName);
		logger.debug("\t" + "doc-topic: " + docTopicItem);
		
		return docTopicItem;
	}

	public static DocTopicItem mergeDocTopicItems(DocTopicItem docTopicItem,
			DocTopicItem docTopicItem2) {
		if (docTopicItem == null) {
			return new DocTopicItem(docTopicItem2);
		}
		if (docTopicItem2 == null) {
			return new DocTopicItem(docTopicItem);
		}
		DocTopicItem mergedDocTopicItem = new DocTopicItem(docTopicItem);
		docTopicItem.sort(Sort.num);
		docTopicItem2.sort(Sort.num);
		mergedDocTopicItem.sort(Sort.num);
		for (int i=0;i<docTopicItem.topics.size();i++) {
			TopicItem ti1 = docTopicItem.topics.get(i);
			TopicItem ti2 = docTopicItem2.topics.get(i);
			TopicItem mergedTopicItem = mergedDocTopicItem.topics.get(i);
			
			logger.debug("ti1.topicNum: " + ti1.topicNum);
			logger.debug("ti2.topicNum: " + ti2.topicNum);
			logger.debug("ti1.proportion: " + ti1.proportion);
			logger.debug("ti2.proportion: " + ti2.proportion);
			
			assert ti1.topicNum == ti2.topicNum : "In mergeDocTopicItems, nonmatching docTopicItems";
			mergedTopicItem.proportion = (ti1.proportion + ti2.proportion)/2;
			
			logger.debug("mergedTopicItem.topicNum: " + mergedTopicItem.topicNum);
			logger.debug("mergedTopicItem.proportion: " + mergedTopicItem.proportion);
		}
		return mergedDocTopicItem;
	}

	public static void printTwoDocTopics(DocTopicItem docTopicItem,
			DocTopicItem docTopicItem2) {
		if (docTopicItem == null) {
			logger.debug(DebugUtil.addMethodInfo(" first arg is null...returning"));
			return;
		}
		
		if (docTopicItem2 == null) {
			logger.debug(DebugUtil.addMethodInfo(" second arg is null...returning"));
			return;
		}
		
		docTopicItem.sort(Sort.num);
		docTopicItem2.sort(Sort.num);
		
		logger.debug(String.format("%5s%64s%64s\n", "", docTopicItem.source, docTopicItem2.source));
		
		for (int i=0; i < docTopicItem.topics.size(); i ++)
			logger.debug(String.format("%32s%32f%32f\n", docTopicItem.topics.get(i).topicNum, docTopicItem.topics.get(i).proportion, docTopicItem2.topics.get(i).proportion));
	}

	public static TopicItem getMatchingTopicItem(List<TopicItem> topics,
			TopicItem inTopicItem) {
		for (TopicItem currTopicItem : topics) {
			if (currTopicItem.topicNum == inTopicItem.topicNum) {
				return currTopicItem;
			}
		}
		return null;
	}

	public static void printDocTopicProportionSum(DocTopicItem docTopicItem) {
		if (docTopicItem == null) {
			logger.debug("cannot sum doc-topic propoertion for null DocTopicItem");
			return;
		}
		double sum = 0;
		for (TopicItem ti : docTopicItem.topics)
			sum += ti.proportion;
		logger.debug("doc-topic proportion sum: " + sum);
	}

	public static void setDocTopicForFastClusterForMalletApi(FastCluster c) {
		if (Config.getSelectedLanguage().equals(Language.java))
			c.docTopicItem = docTopics.getDocTopicItemForJava(c.getName());
		if (Config.getSelectedLanguage().equals(Language.c))
			c.docTopicItem = docTopics.getDocTopicItemForC(c.getName());
	}
}