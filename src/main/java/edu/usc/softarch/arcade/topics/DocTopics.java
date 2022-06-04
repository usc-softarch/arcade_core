package edu.usc.softarch.arcade.topics;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.InstanceList;
import com.fasterxml.jackson.core.JsonToken;
import edu.usc.softarch.arcade.clustering.Cluster;

/**
 * @author joshua
 */
public class DocTopics {
	// #region FIELDS ------------------------------------------------------------
	private List<DocTopicItem> dtItemList;
	private Map<Integer, List<String>> topicWordLists;
	// #endregion FIELDS ---------------------------------------------------------
	
	// #region CONSTRUCTORS ------------------------------------------------------
	/**
	 * Deserialization constructor.
	 */
	public DocTopics() {
		this.dtItemList = new ArrayList<>();
		this.topicWordLists = new TreeMap<>();
	}
	
	/**
	 * Clone constructor.
	 */
	public DocTopics(DocTopics docTopics) {
		this();
		for (DocTopicItem docTopicItem : docTopics.dtItemList)
			this.dtItemList.add(new DocTopicItem(docTopicItem));
		for (Map.Entry<Integer, List<String>> topic : topicWordLists.entrySet())
			this.topicWordLists.put(topic.getKey(), new ArrayList<>(topic.getValue()));
	}

	public DocTopics(String artifactsDir)	throws Exception {
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
	// #endregion CONSTRUCTORS ---------------------------------------------------

	// #region ACCESSORS ---------------------------------------------------------
	public List<DocTopicItem> getDocTopicItemList() { return dtItemList; }

	public void setClusterDocTopic(Cluster c, String language) {
		c.setDocTopicItem(this.getDocTopicItem(c.getName(), language)); }

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

	public List<Concern> getConcerns() {
		List<Concern> concernList = new ArrayList<>();
		for (DocTopicItem dti : dtItemList)
			concernList.add(dti.computeConcern(this.topicWordLists));

		return concernList;
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
	public void serialize(String filePath) throws IOException {
		JsonFactory factory = new JsonFactory();
		try (JsonGenerator generator = factory.createGenerator(
				new File(filePath), JsonEncoding.UTF8)) {
			generator.writeStartObject();
			this.serialize(generator);
			generator.writeEndObject();
		}
	}

	public void serialize(JsonGenerator generator) throws IOException {
		generator.writeArrayFieldStart("dtItemList");
		for (DocTopicItem dti : dtItemList) {
			generator.writeStartObject();
			dti.serialize(generator);
			generator.writeEndObject();
		}
		generator.writeEndArray();

		generator.writeArrayFieldStart("topicWordLists");
		for (List<String> wordList : topicWordLists.values()) {
			generator.writeStartObject();
			generator.writeFieldName("wordList");
			generator.writeArray(wordList.toArray(new String[0]), 0, wordList.size());
			generator.writeEndObject();
		}
		generator.writeEndArray();
	}

	public static DocTopics deserialize(String filePath) throws IOException {
		JsonFactory factory = new JsonFactory();

		try (JsonParser parser = factory.createParser(new File(filePath))) {
			parser.nextToken(); // skip start object
			return deserialize(parser);
		}
	}

	public static DocTopics deserialize(JsonParser parser) throws IOException {
		DocTopics toReturn = new DocTopics();

		parser.nextToken(); // skip field name dtItemList
		parser.nextToken(); // skip start array
		while (parser.nextToken().equals(JsonToken.START_OBJECT)) {
			DocTopicItem dti = DocTopicItem.deserialize(parser);
			toReturn.dtItemList.add(dti);
			parser.nextToken(); // skip end object
		}

		parser.nextToken(); // skip field name topicWordLists
		parser.nextToken(); // skip start array
		int i = 0;
		while (parser.nextToken().equals(JsonToken.START_OBJECT)) {
			parser.nextToken(); // skip field name wordList
			List<String> wordList = new ArrayList<>();
			parser.nextToken(); // skip start array
			while (!parser.nextToken().equals(JsonToken.END_ARRAY))
				wordList.add(parser.getText());
			toReturn.topicWordLists.put(i++, wordList);
			parser.nextToken(); // skip end object
		}

		return toReturn;
	}
	// #endregion SERIALIZATION --------------------------------------------------
}
