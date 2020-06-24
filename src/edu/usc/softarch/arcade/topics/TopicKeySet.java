package edu.usc.softarch.arcade.topics;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

/**
 * @author joshua
 *
 */
public class TopicKeySet {
	public HashSet<TopicKey> set;
	boolean DEBUG = false;

	public TopicKeySet() {
		super();
	}
	
	public TopicKey getTopicKeyByID(int topicNum) {
		for (TopicKey topicKey : set) {
			if (topicNum == topicKey.topicNum) {
				return topicKey;
			}
		}
		
		return null;
	}
	
	public int size() {
		return set.size();
	}
	
	public TopicKeySet(String filename) throws FileNotFoundException {
		set = new HashSet<TopicKey>();
		loadFromFile(filename);
	}

	private void loadFromFile(String filename) throws FileNotFoundException {
		File f = new File(filename);

		Scanner s = new Scanner(f);
		String[] items;
		while (s.hasNext()) {
			String line = s.nextLine();
			items = line.split("\\s");
			System.out.println(line);
			
			TopicKey tk = new TopicKey();
			
			tk.topicNum = (new Integer(items[0])).intValue();
			tk.alpha = (new Double(items[1])).doubleValue();
			
			for (int i=2;i<items.length;i++) {
				tk.words.add(items[i]);
			}
			
			set.add(tk);
			
			if (DEBUG)
				printStringArray(items);
		}
		
		System.out.println();
		for (TopicKey tk : set) {
			System.out.println(tk);
		}
		

		
	}

	private void printStringArray(String[] items) {
		for (int i=0;i<items.length;i++) {
			System.out.print(items[i] + ",");
		}
		System.out.println();
	}
	
}
