package edu.usc.softarch.arcade.facts;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ExpertDecomposition {
	public List<Group> groups = new ArrayList<Group>();
	public HashSet<HashSet<String>> allIntraPairs = new HashSet<HashSet<String>> ();
	//public HashSet<HashSet<String>> groupsOfStrings = new HashSet<HashSet<String>>();
	
	public void ExpertDecompsition() {
	}
	
	public String toString() {
		
		return toPrettyString();
	}
	
	public String toPrettyString() {
		String groupsList = "";
		for (Group g : groups) {
			groupsList += "\t" + g + "\n";
		}
		return groupsList;
	}

	public String toTightString() {
		String groupsList = "";
		for (Group g : groups) {
			groupsList += "[" + g + "]";
		}
		return groupsList;
	}
}
