package edu.usc.softarch.arcade.topics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

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
import edu.usc.softarch.arcade.config.Config;
import edu.usc.softarch.arcade.util.FileListing;
import edu.usc.softarch.arcade.util.FileUtil;

/**
 * @author joshua
 */
public class DocTopics {
	List<DocTopicItem> dtItemList = new ArrayList<>();
	private Logger logger = Logger.getLogger(DocTopics.class);
	
	public DocTopics() {
		super();
	}
	
	public DocTopics(DocTopics docTopics) {
		for (DocTopicItem docTopicItem : docTopics.dtItemList) {
			dtItemList.add(new DocTopicItem(docTopicItem));
		}
	}
	
	public DocTopics(String srcDir, String artifactsDir) throws Exception {
		// Begin by importing documents from text to feature sequences
		List<Pipe> pipeList = new ArrayList<>();
		int numTopics = 100;
		// Pipes: alphanumeric only, camel case separation, lowercase, tokenize,
		// remove stopwords english, remove stopwords java, stem, map to
		// features
		pipeList.add(new CharSequenceReplace(Pattern.compile("[^A-Za-z]"), " "));
		pipeList.add(new CamelCaseSeparatorPipe());
		pipeList.add(new CharSequenceLowercase());
		pipeList.add(new CharSequence2TokenSequence(Pattern
				.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")));
		pipeList.add(new TokenSequenceRemoveStopwords(new File(
				"stoplists/en.txt"), "UTF-8", false, false, false));
		
		if (Config.getSelectedLanguage().equals(Config.Language.c)) {
			pipeList.add(new TokenSequenceRemoveStopwords(new File(
					"res/ckeywords"), "UTF-8", false, false, false));
			pipeList.add(new TokenSequenceRemoveStopwords(new File(
					"res/cppkeywords"), "UTF-8", false, false, false));
		}
		else {
			pipeList.add(new TokenSequenceRemoveStopwords(new File(
					"res/javakeywords"), "UTF-8", false, false, false));
		}
		pipeList.add(new StemmerPipe());
		pipeList.add(new TokenSequence2FeatureSequence());

		InstanceList instances = new InstanceList(new SerialPipes(pipeList));
		String testDir = srcDir;
		logger.debug("Building instances for mallet...");
		for (File file : FileListing.getFileListing(new File(testDir))) {
			logger.debug("Should I add " + file.getName() + " to instances?");
			if (file.isFile() && file.getName().endsWith(".java")) {
				String shortClassName = file.getName().replace(".java", "");
				BufferedReader reader = new BufferedReader(new FileReader(file));
				String line = null;
				String fullClassName = "";
				while ((line = reader.readLine()) != null) {
					String packageName = FileUtil.findPackageName(line);
					if (packageName != null) {
						fullClassName = packageName + "." + shortClassName;
						logger.debug("\t I've identified the following full class name from analyzing files: " + fullClassName);
					}
				}
				reader.close();
				logger.debug("I'm going to add this file to instances: " + file);
				String data = FileUtil.readFile(file.getAbsolutePath(),
						Charset.defaultCharset());
				Instance instance = new Instance(data, "X", fullClassName,
						file.getAbsolutePath());
				instances.addThruPipe(instance);
			}
			Pattern p = Pattern.compile("\\.(c|cpp|cc|s|h|hpp|icc|ia|tbl|p)$");
			// if we found a c or c++ file
			if ( p.matcher(file.getName()).find() ) {
				logger.debug("I'm going to add this file to instances: " + file);
				String depsStyleFilename = file.getAbsolutePath().replace(testDir, "");
				String data = FileUtil.readFile(file.getAbsolutePath(),
						Charset.defaultCharset());
				Instance instance = new Instance(data, "X", depsStyleFilename,
						file.getAbsolutePath());
				instances.addThruPipe(instance);
			}
		}
		
		InstanceList previousInstances = InstanceList.load(new File(artifactsDir+"/output.pipe"));
		
		TopicInferencer inferencer = 
				TopicInferencer.read(new File(artifactsDir+"/infer.mallet"));
		
		for (int instIndex = 0; instIndex < previousInstances.size(); instIndex++) {
			DocTopicItem dtItem = new DocTopicItem();
			dtItem.doc = instIndex;
			dtItem.source = (String) previousInstances.get(instIndex).getName();

			dtItem.topics = new ArrayList<>();

			double[] topicDistribution = inferencer.getSampledDistribution(previousInstances.get(instIndex), 1000, 10, 10);
			for (int topicIdx = 0; topicIdx < numTopics; topicIdx++) {
				TopicItem t = new TopicItem();
				t.topicNum = topicIdx;
				t.proportion = topicDistribution[topicIdx];
				dtItem.topics.add(t);
			}
			dtItemList.add(dtItem);
		}
	}
	
	/**
	 * @param filename
	 * @throws FileNotFoundException
	 */
	public DocTopics(String filename) throws FileNotFoundException {
		loadFromFile(filename);
	}
	
	public DocTopicItem getDocTopicItemForJava(String name) {
		for (DocTopicItem dti : dtItemList) {
			String altName = name.replace("/", ".").replace(".java", "").trim();
			if (dti.source.endsWith(name) || altName.equals(dti.source.trim()))
				return dti;
		}
		return null;
	}
	
	public DocTopicItem getDocTopicItemForC(String name) {
		for (DocTopicItem dti : dtItemList) {
			String strippedSource = null;
			String nameWithoutQuotations = null;
			if (dti.source.endsWith(".func")) {
				strippedSource = dti.source.substring(dti.source.lastIndexOf('/')+1,dti.source.lastIndexOf(".func"));
				nameWithoutQuotations = name.replace("\"", "");
				if (strippedSource.contains(nameWithoutQuotations))
					return dti;
			} else if (dti.source.endsWith(".c") || dti.source.endsWith(".h")
					|| dti.source.endsWith(".tbl") || dti.source.endsWith(".p")
					|| dti.source.endsWith(".cpp") || dti.source.endsWith(".s")
					|| dti.source.endsWith(".hpp") || dti.source.endsWith(".icc")
					|| dti.source.endsWith(".ia")) {
				nameWithoutQuotations = name.replace("\"", "");
				if (dti.source.endsWith(nameWithoutQuotations))
					return dti;
			}
			else if (dti.source.endsWith(".S")) {
				String dtiSourceRenamed = dti.source.replace(".S", ".c");
				nameWithoutQuotations = name.replace("\"", "");
				if (dtiSourceRenamed.endsWith(nameWithoutQuotations))
					return dti;
			}
		}
		logger.error("Cannot find doc topic for: " + name);
		return null;
	}
	
	public List<DocTopicItem> getDocTopicItemList() {
		return dtItemList;
	}

	public void loadFromFile(String filename) throws FileNotFoundException {
		logger.debug("Loading DocTopics from file...");
		File f = new File(filename);

		Scanner s = new Scanner(f);

		dtItemList = new ArrayList<>();
		
		while (s.hasNext()) {
			String line = s.nextLine();
			if (line.startsWith("#")) {
				continue;
			}
			String[] items = line.split("\\s+");

			DocTopicItem dtItem = new DocTopicItem();
			dtItem.doc = (Integer.valueOf(items[0])).intValue();
			dtItem.source = items[1];

			dtItem.topics = new ArrayList<>();

			TopicItem t = new TopicItem();
			for (int i = 2; i < items.length; i++) {
				if (i % 2 == 0) {
					t.topicNum = (Integer.valueOf(items[i])).intValue();
				} else {
					t.proportion = (Double.valueOf(items[i])).doubleValue();
					dtItem.topics.add(t);
					t = new TopicItem();
				}
			}
			dtItemList.add(dtItem);
			logger.debug(line);
		}
		
		logger.debug("\n");
		for (DocTopicItem dtItem : dtItemList) {
			logger.debug(dtItem);
		}
	}	
}