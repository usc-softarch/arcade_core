package edu.usc.softarch.arcade.topics;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;

import edu.usc.softarch.arcade.classgraphs.TopicModelData;
import edu.usc.softarch.arcade.config.Config;
import edu.usc.softarch.arcade.config.datatypes.Proj;

import weka.core.Instance;
import weka.core.Instances;

/**
 * @author joshua
 *
 */
public class TopicClustering {

	
	protected static Instances data;
	
	public static String currDocTopicsFilename = "";
	public static String currTopicKeysFilename = "";
	
	public static TopicModelData freecsTMD = new TopicModelData();
	public static TopicModelData LlamaChatTMD = new TopicModelData();
	
	public static String datasetName = "";
	
	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException {
		LlamaChatTMD.docTopicsFilename = "/home/joshua/Documents/Software Engineering Research/Subjects/LlamaChat/LlamaChat-doc-topics.txt";
		LlamaChatTMD.topicKeysFilename = "/home/joshua/Documents/Software Engineering Research/Subjects/LlamaChat/LlamaChat-topic-keys.txt";

		freecsTMD.docTopicsFilename = "/home/joshua/Documents/Software Engineering Research/Subjects/freecs/freecs-doc-topics.txt";
		freecsTMD.topicKeysFilename = "/home/joshua/Documents/Software Engineering Research/Subjects/freecs/freecs-topic-keys.txt";
		
		if (Config.proj.equals(Proj.FreeCS)) {
			currDocTopicsFilename = freecsTMD.docTopicsFilename;
			currTopicKeysFilename = freecsTMD.topicKeysFilename;
		} else if (Config.proj.equals(Proj.LlamaChat)) {
			currDocTopicsFilename = LlamaChatTMD.docTopicsFilename;
			currTopicKeysFilename = LlamaChatTMD.topicKeysFilename;
		} else {
			System.err
					.println("Couldn't identiy the doc-topics and topic-keys files");
			System.exit(1);
		}

		if (Config.proj.equals(Proj.GujChat))
			datasetName = Config.gujChatStr;
		else if (Config.proj.equals(Proj.LlamaChat))
			datasetName = Config.llamaChatStr;
		else if (Config.proj.equals(Proj.FreeCS))
			datasetName = Config.freecsStr;
		else {
			System.err
					.println("Could not identify project string, so couldn't save to arff file");
			System.exit(1);
		}

		
		String datasetsDir = "/home/joshua/Documents/workspace/MyExtractors/datasets";
		FileReader fr;
		
		String arffFilePrefix = "";
		
		if (Config.proj.equals(Proj.LlamaChat)) {
			arffFilePrefix = Config.llamaChatStr;
		}
		else if (Config.proj.equals(Proj.FreeCS)) {
			arffFilePrefix = Config.freecsStr;
		}
		else if (Config.proj.equals(Proj.GujChat)) {
			arffFilePrefix = Config.gujChatStr;
		}
		else {
			System.err.println("Couldn't determine current project for FieldAccessTransformer");
			System.exit(1);
		}
		
		try {
			fr = new FileReader(datasetsDir + "/" + arffFilePrefix + "/" + arffFilePrefix  + ".arff");
			data = new Instances(fr);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		System.out.println("The dataset: ");
		System.out.println("=======================================");
		System.out.println(data);
		System.out.println();
		
		ArrayList<DocTopicItem> dtItemList = new ArrayList<DocTopicItem>();

		System.out.println("Loading doc-topics file...");
		DocTopics dts = new DocTopics(currDocTopicsFilename);
		dtItemList = dts.getDocTopicItemList();

		System.out.println("Loading topic-keys file...");
		TopicKeySet tkList = new TopicKeySet(currTopicKeysFilename);
		System.out.println("Number of topics: " + tkList.size());
		
		Enumeration e = data.enumerateInstances();
		
		System.out.println("Printing out list of classes...");
		while (e.hasMoreElements()) {
			Instance ins = (Instance)e.nextElement();
			System.out.println(ins.stringValue(data.attribute("name")));
			System.out.println("\tpctCallers: " + ins.value(data.attribute("pctCallers")));
			System.out.println("\tpctCallees: " + ins.value(data.attribute("pctCallees")));
			
		}

	}

}
