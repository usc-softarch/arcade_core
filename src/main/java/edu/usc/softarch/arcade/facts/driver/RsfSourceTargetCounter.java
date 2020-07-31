package edu.usc.softarch.arcade.facts.driver;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

public class RsfSourceTargetCounter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String firstRsfFilename = args[0];
		
		RsfReader.loadRsfDataFromFile(firstRsfFilename);
		Set<List<String>> facts = Sets.newHashSet(RsfReader.filteredRoutineFacts);
		
		Set<String> sources = new HashSet<>();
		Set<String> targets = new HashSet<>();
		for (List<String> fact : facts) {
			sources.add(fact.get(1));
			targets.add(fact.get(2));
		}
		
		System.out.println("sources count: " + sources.size());
		System.out.println("targets count: " + targets.size());
	}
}
