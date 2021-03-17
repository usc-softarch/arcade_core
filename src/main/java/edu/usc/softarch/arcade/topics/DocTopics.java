package edu.usc.softarch.arcade.topics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.CharSequenceLowercase;
import cc.mallet.pipe.CharSequenceReplace;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import edu.usc.softarch.arcade.util.FileListing;
import edu.usc.softarch.arcade.util.FileUtil;

/**
 * @author joshua
 */
public class DocTopics {
	// #region FIELDS ------------------------------------------------------------
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
		List<Pipe> pipeList = new ArrayList<>();

		/* Pipes: alphanumeric only, camel case separation, lowercase, tokenize,
		 * remove stopwords english, remove stopwords java, stem, map to
		 * features */
		pipeList.add(new CharSequenceReplace(Pattern.compile("[^A-Za-z]"), " "));
		pipeList.add(new CamelCaseSeparatorPipe());
		pipeList.add(new CharSequenceLowercase());
		pipeList.add(new CharSequence2TokenSequence(Pattern
				.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")));
		// pipeList.add(new TokenSequenceRemoveStopwords(new File(
		// 		"stoplists/en.txt"), "UTF-8", false, false, false));
		
		// if (language.equalsIgnoreCase("c")) {
		// 	pipeList.add(new TokenSequenceRemoveStopwords(new File(
		// 			"res/ckeywords"), "UTF-8", false, false, false));
		// 	pipeList.add(new TokenSequenceRemoveStopwords(new File(
		// 			"res/cppkeywords"), "UTF-8", false, false, false));
		// }
		// else
		// 	pipeList.add(new TokenSequenceRemoveStopwords(new File(
		// 			"res/javakeywords"), "UTF-8", false, false, false));

		pipeList.add(new StemmerPipe());
		pipeList.add(new TokenSequence2FeatureSequence());

		InstanceList instances = new InstanceList(new SerialPipes(pipeList));
		logger.debug("Building instances for mallet...");

		// For each file in the source directory, recursively load Instances
		for (File file : FileListing.getFileListing(new File(srcDir))) {
			logger.debug("Should I add " + file.getName() + " to instances?");
			Instance newInstance = loadInstance(file, srcDir);
			if (newInstance != null) instances.addThruPipe(newInstance);
		}
		/*TODO Is anything above this point even doing anything? The instances
		 * variable goes nowhere and the object attributes are only affected past
		 * this point. */

		int numTopics = 100;
		String fs = File.separator;
		
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
				if (dti.getSource().endsWith(nameWithoutQuotations))
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
	private Instance loadInstance(File file, String srcDir) throws IOException {
		// If it is a Java file
		if (file.isFile() && file.getName().endsWith(".java"))
			return loadJavaInstance(file);

		// If it is a C file
		Pattern p = Pattern.compile("\\.(c|cpp|cc|s|h|hpp|icc|ia|tbl|p)$");
		if (p.matcher(file.getName()).find())
			return loadCInstance(file, srcDir);

		return null;
	}

	private Instance loadJavaInstance(File file) throws IOException {
		String shortClassName = file.getName().replace(".java", "");
		String fullClassName = "";
		String packageName = FileUtil.findPackageName(file);
		if (packageName != null) {
			fullClassName = packageName + "." + shortClassName;
			logger.debug("\t I've identified the following full class name"
				+ " from analyzing files: " + fullClassName);
		}

		logger.debug("I'm going to add this file to instances: " + file);
		String data = FileUtil.readFile(file.getAbsolutePath(),
			Charset.defaultCharset());
		return new Instance(data, "X", fullClassName, file.getAbsolutePath());
	}

	private Instance loadCInstance(File file, String srcDir) throws IOException {
		logger.debug("I'm going to add this file to instances: " + file);
		String depsStyleFilename = file.getAbsolutePath().replace(srcDir, "");
		String data = FileUtil.readFile(file.getAbsolutePath(),
			Charset.defaultCharset());
		return new Instance(data, "X", depsStyleFilename, file.getAbsolutePath());
	}

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