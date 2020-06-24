package edu.usc.softarch.arcade.topics;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

/**
 * @author joshua
 *
 */
public class WordTopicCounts {
	public HashMap<String, WordTopicItem> getWordTopicItems() {
		return wordTopicItems;
	}

	HashMap<String, WordTopicItem> wordTopicItems = new HashMap<String,WordTopicItem>();
	
	public WordTopicCounts(String filename) throws FileNotFoundException {
		loadFromFile(filename);
	}

	private void loadFromFile(String filename) throws FileNotFoundException {
		File f = new File(filename);

		Scanner s = new Scanner(f);

		wordTopicItems = new HashMap<String,WordTopicItem>();
		
		while (s.hasNext()) {
			String line = s.nextLine();
			if (line.startsWith("#")) {
				continue;
			}
			String[] items = line.split(" ");
			
			WordTopicItem wtItem = new WordTopicItem();
			wtItem.id = (new Integer(items[0])).intValue();
			wtItem.name = items[1];

			wtItem.topicIDWordCountMap = new HashMap<Integer,Integer>();

			for (int i = 2; i < items.length; i++) {
				String topicWordCountStr = items[i];
				String[] topicWordCount = topicWordCountStr.split(":");
				
				Integer topicNum = Integer.parseInt(topicWordCount[0]);
				Integer wordCount = Integer.parseInt(topicWordCount[1]);
				
				wtItem.topicIDWordCountMap.put(topicNum, wordCount);
				
			}
			wordTopicItems.put(wtItem.name,wtItem);
			System.out.println(line);

		}
		
		System.out.println();
		for (WordTopicItem wtItem : wordTopicItems.values()) {
			System.out.println(wtItem);
		}
		
	}
}
