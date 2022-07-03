package edu.usc.softarch.arcade.topics;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.InstanceList;
import edu.usc.softarch.arcade.clustering.Cluster;
import edu.usc.softarch.arcade.topics.exceptions.UnmatchingDocTopicItemsException;
import edu.usc.softarch.util.json.EnhancedJsonGenerator;
import edu.usc.softarch.util.json.EnhancedJsonParser;
import edu.usc.softarch.util.json.JsonSerializable;

/**
 * @author joshua
 */
public class DocTopics implements JsonSerializable {
	// #region FIELDS ------------------------------------------------------------
	private static DocTopics singleton;
	private final List<DocTopicItem> dtItemList;
	private Map<Integer, List<String>> topicWordLists;
	// #endregion FIELDS ---------------------------------------------------------
	
	// #region CONSTRUCTORS ------------------------------------------------------
	/**
	 * Deserialization constructor.
	 */
	private DocTopics() {
		this.dtItemList = new ArrayList<>();
		this.topicWordLists = new TreeMap<>();
	}

	private DocTopics(String artifactsDir)	throws Exception {
		this();
		// Begin by importing documents from text to feature sequences
		char fs = File.separatorChar;

		int numTopics = 100;
		
		InstanceList previousInstances = InstanceList.load(
			new File(artifactsDir + fs + "output.pipe"));
		
		TopicInferencer inferencer = TopicInferencer.read(
			new File(artifactsDir + fs + "infer.mallet"));

		// Load TopicItems
		for (int instIndex = 0; instIndex < previousInstances.size(); instIndex++) {
			DocTopicItem dtItem = new DocTopicItem(
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

		// Load WordLists
		TopicCompositionParser parser = new TopicCompositionParser(inferencer);
		this.topicWordLists = parser.run();
	}

	public static DocTopics getSingleton() {
		if (singleton == null)
			throw new IllegalStateException("DocTopics must be initialized.");

		return singleton;
	}

	public static void initializeSingleton(String artifactsDir) throws Exception {
		singleton = new DocTopics(artifactsDir);
	}

	public static void resetSingleton() { singleton = null; }

	public static boolean isReady() {
		return singleton != null;
	}
	// #endregion CONSTRUCTORS ---------------------------------------------------

	// #region ACCESSORS ---------------------------------------------------------
	public List<DocTopicItem> getDocTopicItemList() { return dtItemList; }

	public void setClusterDocTopic(Cluster c, String language) {
		c.setDocTopicItem(this.getDocTopicItem(c.name, language)); }

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

	public Map<Integer, List<String>> getTopicWordLists() {
		return topicWordLists; }

	public DocTopicItem mergeDocTopicItems(
			Cluster c1, Cluster c2, String newName)
			throws UnmatchingDocTopicItemsException {
		return mergeDocTopicItems(c1.getDocTopicItem(), c2.getDocTopicItem(), newName);
	}

	public DocTopicItem mergeDocTopicItems(
			DocTopicItem dti1, DocTopicItem dti2, String newName)
			throws UnmatchingDocTopicItemsException {
		DocTopicItem newDti = new DocTopicItem(dti1, dti2, newName);
		singleton.dtItemList.remove(dti1);
		singleton.dtItemList.remove(dti2);
		singleton.dtItemList.add(newDti);

		return newDti;
	}

	public void renameDocTopicItem(DocTopicItem dti, String newSource) {
		dti.source = newSource; }
	// #endregion ACCESSORS ------------------------------------------------------

	// #region SERIALIZATION -----------------------------------------------------
	public void serialize(String filePath) throws IOException {
		try (EnhancedJsonGenerator generator =
				new EnhancedJsonGenerator(filePath)) {
			serialize(generator);
		}
	}

	@Override
	public void serialize(EnhancedJsonGenerator generator) throws IOException {
		generator.writeField("dtItemList", dtItemList);
		generator.writeField(
			"topicWordLists", topicWordLists, false, "wordList");
	}

	public static DocTopics deserialize(String filePath) throws IOException {
		try (EnhancedJsonParser parser = new EnhancedJsonParser(filePath)) {
			return deserialize(parser);
		}
	}

	public static DocTopics deserialize(EnhancedJsonParser parser)
			throws IOException {
		singleton = new DocTopics();
		singleton.dtItemList.addAll(parser.parseCollection(DocTopicItem.class));

		Map<Integer, List> topicWordLists = parser.parseMap(
			List.class, false, String.class);
		for (Map.Entry<Integer, List> entry : topicWordLists.entrySet())
			singleton.topicWordLists.put(entry.getKey(), entry.getValue());

		return singleton;
	}
	// #endregion SERIALIZATION --------------------------------------------------
}
