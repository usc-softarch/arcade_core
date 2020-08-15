package edu.usc.softarch.arcade.topics;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

/**
 * @author joshua
 */
public class TopicKeySet {
	// #region FIELDS ------------------------------------------------------------
	private Set<TopicKey> set;
	// #endregion FIELDS ---------------------------------------------------------
	
	// #region CONSTRUCTORS ------------------------------------------------------
	public TopicKeySet() { this.set = new HashSet<>(); }
	
	public TopicKeySet(String filename) throws FileNotFoundException {
		this.set = loadFromFile(filename); }
	// #endregion CONSTRUCTORS ---------------------------------------------------

	// #region ACCESSORS ---------------------------------------------------------
	public Set<TopicKey> getSet() { return new HashSet<>(this.set); }
	public TopicKey getTopicKeyByID(int topicNum) {
		for (TopicKey topicKey : this.set)
			if (topicNum == topicKey.getTopicNum())
				return topicKey;
		
		return null;
	}
	
	public int size() { return set.size(); }
	// #endregion ACCESSORS ------------------------------------------------------
	
	// #region IO ----------------------------------------------------------------
	private Set<TopicKey> loadFromFile(String filename)
			throws FileNotFoundException {
		File f = new File(filename);
		String[] items;
		Set<TopicKey> result = new HashSet<>();

		try (Scanner s = new Scanner(f)) {
			while (s.hasNext()) {
				String line = s.nextLine();
				items = line.split("\\s");
				System.out.println(line);
				
				TopicKey tk = new TopicKey();
				
				tk.setTopicNum((Integer.valueOf(items[0])).intValue());
				tk.setAlpha((Double.valueOf(items[1])).doubleValue());
				for (int i=2; i < items.length; i++)
					tk.addWord(items[i]);
				
					result.add(tk);
				
				printStringArray(items);
			}
		}
		
		System.out.println();
		for (TopicKey tk : set)
			System.out.println(tk);

		return result;
	}

	private void printStringArray(String[] items) {
		for (int i=0; i < items.length; i++) {
			System.out.print(items[i] + ",");
		}
		System.out.println();
	}
	// #endregion MISC -----------------------------------------------------------
}