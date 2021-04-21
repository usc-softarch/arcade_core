package edu.usc.softarch.arcade.topics;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.CharSequenceLowercase;
import cc.mallet.pipe.CharSequenceReplace;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.InstanceList;

/**
 * @author joshua
 */
public class DocTopics implements Serializable{
	// #region FIELDS ------------------------------------------------------------
	static final long serialVersionUID = 1L;
	private static Logger logger = LogManager.getLogger(DocTopics.class);
	private List<DocTopicItem> dtItemList;
	// #endregion FIELDS ---------------------------------------------------------
	
	// #region CONSTRUCTORS ------------------------------------------------------
	public DocTopics() { dtItemList = new ArrayList<>(); }
	
	/**
	 * Clone constructor.
	 */
	public DocTopics(DocTopics docTopics) {
		this();
		for (DocTopicItem docTopicItem : docTopics.dtItemList)
			dtItemList.add(new DocTopicItem(docTopicItem));
	}

	public DocTopics(String filename) throws FileNotFoundException {
		loadFromFile(filename);	}

	public DocTopics(String srcDir, String artifactsDir, String language)
			throws Exception {
		this();
		// Begin by importing documents from text to feature sequences
		char fs = File.separatorChar;

		int numTopics = 100;
		
		InstanceList previousInstances = InstanceList.load(
			new File(artifactsDir + fs + "output.pipe"));
		
		TopicInferencer inferencer = TopicInferencer.read(
			new File(artifactsDir + fs + "infer.mallet"));

		for (int instIndex = 0; instIndex < previousInstances.size(); instIndex++) {
			DocTopicItem dtItem = new DocTopicItem();
			dtItem.setDoc(instIndex);
			dtItem.setSource((String) previousInstances.get(instIndex).getName());

			double[] topicDistribution = 
				inferencer.getSampledDistribution(
					previousInstances.get(instIndex), 1000, 10, 10);
			
			for (int topicIdx = 0; topicIdx < numTopics; topicIdx++) {
				TopicItem t = new TopicItem();
				t.setTopicNum(topicIdx);
				t.setProportion(topicDistribution[topicIdx]);
				dtItem.addTopic(t);
			}
			dtItemList.add(dtItem);
		}
	}
	// #endregion CONSTRUCTORS ---------------------------------------------------

	// #region ACCESSORS ---------------------------------------------------------
	public List<DocTopicItem> getDocTopicItemList() { return dtItemList; }

	/**
	 * Gets the DocTopicItem for a given file name or entity.
	 * 
	 * @param name Name of the file or entity.
	 * @param language Source language of the file.
	 * @return Associated DocTopicItem
	 */
	public DocTopicItem getDocTopicItem(String name, String language) {
		if (language.equalsIgnoreCase("java"))
			return getDocTopicItemForJava(name);
		if (language.equalsIgnoreCase("c"))
			return getDocTopicItemForC(name);

		throw new IllegalArgumentException("Unknown source language " + language);
	}

	/**
	 * Gets the DocTopicItem for a given Java file name or Class name.
	 * 
	 * @param name Name of the file or Java class.
	 * @return Associated DocTopicItem.
	 */
	public DocTopicItem getDocTopicItemForJava(String name) {
		String altName = name.replace("/", ".").replace(".java", "").trim();

		for (DocTopicItem dti : dtItemList) {
			if (dti.getSource().endsWith(name) 
					|| altName.equals(dti.getSource().trim()))
				return dti;
		}
		return null;
	}

	/**
	 * Gets the DocTopicItem for a given C file name or entity.
	 * 
	 * @param name Name of the file or C entity.
	 * @return Associated DocTopicItem.
	 */
	public DocTopicItem getDocTopicItemForC(String name) {
		for (DocTopicItem dti : dtItemList) {
			String strippedSource = null;
			String nameWithoutQuotations = name.replace("\"", "");

			if (dti.getSource().endsWith(".func")) {
				strippedSource = dti.getSource().substring(
					dti.getSource().lastIndexOf('/') + 1,
					dti.getSource().lastIndexOf(".func"));
				if (strippedSource.contains(nameWithoutQuotations))
					return dti;
			} else if (dti.isCSourced()) {
				//FIXME Make sure this works on Linux and find a permanent fix
				strippedSource = dti.getSource().substring(1, dti.getSource().length());
				strippedSource = strippedSource.replace("\\", "/");
				if (strippedSource.endsWith(nameWithoutQuotations))
					return dti;
			}
			else if (dti.getSource().endsWith(".S")) {
				String dtiSourceRenamed = dti.getSource().replace(".S", ".c");
				if (dtiSourceRenamed.endsWith(nameWithoutQuotations))
					return dti;
			}
		}
		logger.error("Cannot find doc topic for: " + name);
		return null;
	}
	// #endregion ACCESSORS ------------------------------------------------------

	// #region PROCESSING --------------------------------------------------------
	public static DocTopicItem computeGlobalCentroidUsingTopics(
			List<DocTopicItem> docTopicItems) {
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
			try {
				mergedDocTopicItem = TopicUtil.mergeDocTopicItems(
					mergedDocTopicItem, currDocTopicItem);
			} catch (UnmatchingDocTopicItemsException e) {
				e.printStackTrace(); //TODO handle it
			}
		}
		return mergedDocTopicItem;
	}
	// #endregion PROCESSING -----------------------------------------------------

	// #region IO ----------------------------------------------------------------
	public void loadFromFile(String filename) throws FileNotFoundException {
		logger.debug("Loading DocTopics from file...");
		File f = new File(filename);
		dtItemList = new ArrayList<>();

		try (Scanner s = new Scanner(f)) {
			while (s.hasNext()) {
				String line = s.nextLine();
				if (line.startsWith("#"))
					continue;
				String[] items = line.split("\\s+");
	
				DocTopicItem dtItem = new DocTopicItem();
				dtItem.setDoc((Integer.valueOf(items[0])).intValue());
				dtItem.setSource(items[1]);
	
				TopicItem t = new TopicItem();
				for (int i = 2; i < items.length; i++) {
					if (i % 2 == 0)
						t.setTopicNum((Integer.valueOf(items[i])).intValue());
					else {
						t.setProportion((Double.valueOf(items[i])).doubleValue());
						dtItem.addTopic(t);
						t = new TopicItem(); //TODO Doesn't this nullify the previous two?
					}
				}
				dtItemList.add(dtItem);
				logger.debug(line);
			}
		}

		logger.debug("\n");
		logger.debug(dtItemList);
	}
	// #endregion IO -------------------------------------------------------------
}