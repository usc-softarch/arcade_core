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

import edu.usc.softarch.arcade.clustering.Cluster;
import edu.usc.softarch.arcade.clustering.Entity;
import edu.usc.softarch.arcade.clustering.FastCluster;
import edu.usc.softarch.arcade.config.Config;
import edu.usc.softarch.arcade.config.Config.Language;

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
	
	public static DocTopics getDocTopicsFromFile(String filename) {
		DocTopics docTopics = null;
		try {
			docTopics = new DocTopics(filename);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return docTopics;
	}
	
	public static void setDocTopicForCluster(DocTopics docTopics, Cluster leaf) {
		String strippedLeafClassName = leaf.getName().substring(leaf.getName().lastIndexOf('.')+1,leaf.getName().length()-1);
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
		leaf.setDocTopicItem(docTopics
			.getDocTopicItemForJava(strippedLeafClassName));
		logger.debug("\t" + "doc-topic: " + leaf.getDocTopicItem());
	}
	
	/** pretty much the same method as above, except uses Entities instead
	 * of FastClusters.
	 * Appends .java and ignores the entities whose names have $ sign in them 
	 * @param docTopics
	 * @param leaf
	 */
	public static void setDocTopicForEntity(DocTopics docTopics, Entity leaf, String type) throws Exception {
		if (type.equals("java")) {
			String strippedLeafClassName = leaf.name.substring(leaf.name.lastIndexOf('.')+1,leaf.name.length());

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
		Set<Integer> topicNumbers = docTopicItem.getTopicNumbers();

		for (Integer i : topicNumbers) {
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
			logger.debug("In, "	+ Thread.currentThread().getStackTrace()[1]
				.getMethodName() + ", " + " first arg is null...returning");
			return; //TODO throw exception
		}
		if (docTopicItem2 == null) {
			logger.debug("In, "	+ Thread.currentThread().getStackTrace()[1]
				.getMethodName() + ", " + " second arg is null...returning");
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