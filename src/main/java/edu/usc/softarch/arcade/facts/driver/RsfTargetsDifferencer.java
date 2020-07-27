package edu.usc.softarch.arcade.facts.driver;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;

public class RsfTargetsDifferencer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String firstRsfFilename = args[0];
		String secondRsfFilename = args[1];
		
		RsfReader.loadRsfDataFromFile(firstRsfFilename);
		Set<List<String>> firstFacts = Sets.newHashSet(RsfReader.filteredRoutineFacts);
		
		RsfReader.loadRsfDataFromFile(secondRsfFilename);
		Set<List<String>> secondFacts = Sets.newHashSet(RsfReader.filteredRoutineFacts);
		
		Set<String> firstTargets = new HashSet<String>();
		for (List<String> fact : firstFacts) {
			firstTargets.add(fact.get(2));
		}
		
		Set<String> secondTargets = new HashSet<String>();
		for (List<String> fact : secondFacts) {
			secondTargets.add(fact.get(2));
		}
		
		Set<String> firstDiffSecondSet = new HashSet<String>(firstTargets);
		firstDiffSecondSet.removeAll(secondTargets);
		
		Set<String> secondDiffFirstSet = new HashSet<String>(secondTargets);
		secondDiffFirstSet.removeAll(firstTargets);
		
		System.out.println("First file: " + firstRsfFilename);
		System.out.println("Second file: " + secondRsfFilename);
		
		System.out.println();
		System.out.println("first file - second file:");
		System.out.println(Joiner.on("\n").join(firstDiffSecondSet));
		
		System.out.println();
		System.out.println("second file - first file:");
		System.out.println(Joiner.on("\n").join(secondDiffFirstSet));
 
	}

}
