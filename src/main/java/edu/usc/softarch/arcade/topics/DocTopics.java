package edu.usc.softarch.arcade.topics;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.InstanceList;
import edu.usc.softarch.arcade.clustering.Cluster;
import edu.usc.softarch.arcade.topics.exceptions.UnmatchingDocTopicItemsException;
import edu.usc.softarch.util.EnhancedHashSet;
import edu.usc.softarch.util.EnhancedSet;
import edu.usc.softarch.util.json.EnhancedJsonGenerator;
import edu.usc.softarch.util.json.EnhancedJsonParser;
import edu.usc.softarch.util.json.JsonSerializable;

/**
 * @author joshua
 */
public class DocTopics implements JsonSerializable {
	// #region FIELDS ------------------------------------------------------------
	private static final Map<String, DocTopics> mingleton = new HashMap<>();
	private final String projectVersion;
	private final Map<String, DocTopicItem> dtItemList;
	private Map<Integer, List<String>> topicWordLists;
	// #endregion FIELDS ---------------------------------------------------------
	
	// #region CONSTRUCTORS ------------------------------------------------------
	/**
	 * Deserialization constructor.
	 */
	private DocTopics(String projectVersion) {
		this.projectVersion = projectVersion;
		this.dtItemList = new HashMap<>();
		this.topicWordLists = new TreeMap<>();
	}

	private DocTopics(String artifactsDir, String version) throws Exception {
		this(version);
		// Begin by importing documents from text to feature sequences
		char fs = File.separatorChar;

		int numTopics = 100;

		InstanceList previousInstances = InstanceList.load(
			new File(artifactsDir + fs + version + "_output.pipe"));
		
		TopicInferencer inferencer = TopicInferencer.read(
			new File(artifactsDir + fs + "infer.mallet"));

		// Load TopicItems
		for (cc.mallet.types.Instance previousInstance : previousInstances) {
			DocTopicItem dtItem = new DocTopicItem(
				(String) previousInstance.getName());

			double[] topicDistribution =
				inferencer.getSampledDistribution(
					previousInstance, 1000, 10, 10);

			for (int topicIdx = 0; topicIdx < numTopics; topicIdx++) {
				TopicItem t = new TopicItem(topicIdx, topicDistribution[topicIdx]);
				dtItem.addTopic(t);
			}
			addDocTopicItem(dtItem);
		}

		// Load WordLists
		TopicCompositionParser parser = new TopicCompositionParser(inferencer);
		this.topicWordLists = parser.run();
	}

	public static DocTopics getSingleton(String version) {
		if (mingleton.get(version) == null)
			throw new IllegalStateException("DocTopics must be initialized.");

		return mingleton.get(version);
	}

	public static void initializeSingleton(
			String artifactsDir, String version) throws Exception {
		mingleton.put(version, new DocTopics(artifactsDir, version));
	}

	public static void resetSingleton() {
		mingleton.clear();
	}

	public static void resetSingleton(String version) {
		mingleton.remove(version);
	}

	public static boolean isReady(String version) {
		return mingleton.get(version) != null
			&& !mingleton.get(version).dtItemList.isEmpty();
	}
	// #endregion CONSTRUCTORS ---------------------------------------------------

	// #region ACCESSORS ---------------------------------------------------------
	public void setClusterDocTopic(Cluster c, String language) {
		c.setDocTopicItem(this.getDocTopicItem(c.name, language)); }

	void addDocTopicItem(DocTopicItem dti) {
		if (this.dtItemList.containsKey(dti.source))
			throw new IllegalArgumentException("Can't add a DTI twice: " + dti.source);
		this.dtItemList.put(dti.source, dti);
	}

	public void cleanDocTopics(Collection<DocTopicItem> toKeep) {
		EnhancedSet<DocTopicItem> toKeepSet = new EnhancedHashSet<>(toKeep);
		EnhancedSet<DocTopicItem> completeSet =
			new EnhancedHashSet<>(this.dtItemList.values());

		Collection<DocTopicItem> toRemoveSet = completeSet.difference(toKeepSet);

		for (DocTopicItem dti : toRemoveSet)
			this.dtItemList.remove(dti.source);
	}

	/**
	 * Gets the DocTopicItem of a given name. Use this when reloading DocTopics
	 * from a previous execution, where the DocTopicItems have already been
	 * correctly renamed.
	 *
	 * @param name Name of the DocTopicItem to recover.
	 * @return Associated DocTopicItem.
	 */
	public DocTopicItem getDocTopicItem(String name) {
		return dtItemList.get(name); }

	/**
	 * Gets the DocTopicItem for a given file name or entity.
	 * 
	 * @param name Name of the file or entity.
	 * @param language Source language of the file.
	 * @return Associated DocTopicItem.
	 */
	private DocTopicItem getDocTopicItem(String name, String language) {
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

		for (Map.Entry<String, DocTopicItem> entry : dtItemList.entrySet()) {
			if (entry.getKey().endsWith(name)
				|| altName.equals(entry.getKey().trim()))
				return entry.getValue();
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
		for (DocTopicItem dti : dtItemList.values()) {
			String strippedSource;
			String nameWithoutQuotations = name.replace("\"", "");

			if (dti.source.endsWith(".func")) {
				strippedSource = dti.source.substring(
					dti.source.lastIndexOf('/') + 1,
					dti.source.lastIndexOf(".func"));
				if (strippedSource.contains(nameWithoutQuotations))
					return dti;
			} else if (dti.isCSourced()) {
				//FIXME Make sure this works on Linux and find a permanent fix
				strippedSource = dti.source.replace("\\", "/");
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
			Cluster c1, Cluster c2, String newName, String version)
			throws UnmatchingDocTopicItemsException {
		return mergeDocTopicItems(c1.getDocTopicItem(),
			c2.getDocTopicItem(), newName, version);
	}

	public DocTopicItem mergeDocTopicItems(
			DocTopicItem dti1, DocTopicItem dti2, String newName, String version)
			throws UnmatchingDocTopicItemsException {
		DocTopicItem newDti = new DocTopicItem(dti1, dti2, newName);
		if (newDti.equals(dti1))
			return dti1;
		if (newDti.equals(dti2))
			return dti2;

		mingleton.get(version).dtItemList.remove(dti1.source);
		mingleton.get(version).dtItemList.remove(dti2.source);
		addDocTopicItem(newDti);

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
		generator.writeField("projectVersion", projectVersion);
		generator.writeField("dtItemList", dtItemList.values());
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
		String version = parser.parseString();
		mingleton.put(version, new DocTopics(version));
		Collection<DocTopicItem> dtis = parser.parseCollection(DocTopicItem.class);

		for (DocTopicItem dti : dtis)
			mingleton.get(version).addDocTopicItem(dti);

		Map<Integer, List> topicWordLists = parser.parseMap(
			List.class, false, String.class);
		for (Map.Entry<Integer, List> entry : topicWordLists.entrySet())
			mingleton.get(version).topicWordLists.put(
				entry.getKey(), entry.getValue());

		return mingleton.get(version);
	}
	// #endregion SERIALIZATION --------------------------------------------------
}
