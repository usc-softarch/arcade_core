package edu.usc.softarch.arcade.topics;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.InstanceList;
import edu.usc.softarch.arcade.clustering.Cluster;

/**
 * @author joshua
 */
public class DocTopics {
	// #region FIELDS ------------------------------------------------------------
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

	public DocTopics(String artifactsDir)
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
			DocTopicItem dtItem = new DocTopicItem(instIndex,
				(String) previousInstances.get(instIndex).getName());

			double[] topicDistribution = 
				inferencer.getSampledDistribution(
					previousInstances.get(instIndex), 1000, 10, 10);
			
			for (int topicIdx = 0; topicIdx < numTopics; topicIdx++) {
				TopicItem t = new TopicItem(topicIdx, topicDistribution[topicIdx]);
				dtItem.addTopic(t);
			}
			dtItemList.add(dtItem);
		}
	}
	// #endregion CONSTRUCTORS ---------------------------------------------------

	// #region ACCESSORS ---------------------------------------------------------
	public List<DocTopicItem> getDocTopicItemList() { return dtItemList; }

	public void setClusterDocTopic(Cluster c, String language) {
		c.docTopicItem = this.getDocTopicItem(c.getName(), language); }

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
			if (dti.source.endsWith(name)
					|| altName.equals(dti.source.trim()))
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

			if (dti.source.endsWith(".func")) {
				strippedSource = dti.source.substring(
					dti.source.lastIndexOf('/') + 1,
					dti.source.lastIndexOf(".func"));
				if (strippedSource.contains(nameWithoutQuotations))
					return dti;
			} else if (dti.isCSourced()) {
				//FIXME Make sure this works on Linux and find a permanent fix
				strippedSource = dti.source.substring(1, dti.source.length());
				strippedSource = strippedSource.replace("\\", "/");
				if (strippedSource.endsWith(nameWithoutQuotations))
					return dti;
			}
			else if (dti.source.endsWith(".S")) {
				String dtiSourceRenamed = dti.source.replace(".S", ".c");
				if (dtiSourceRenamed.endsWith(nameWithoutQuotations))
					return dti;
			}
		}
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
				mergedDocTopicItem = new DocTopicItem(mergedDocTopicItem, currDocTopicItem);
			} catch (UnmatchingDocTopicItemsException e) {
				e.printStackTrace(); //TODO handle it
			}
		}
		return mergedDocTopicItem;
	}
	// #endregion PROCESSING -----------------------------------------------------

	// #region SERIALIZATION -----------------------------------------------------
	public void serializeDocTopics(String filePath) throws IOException {
		(new ObjectMapper()).writeValue(new File(filePath), this);
	}

	public static DocTopics deserializeDocTopics(String filePath)
			throws IOException {
		return (new ObjectMapper()).readValue(new File(filePath), DocTopics.class);
	}
	// #endregion SERIALIZATION --------------------------------------------------
}
