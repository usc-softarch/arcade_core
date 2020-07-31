package edu.usc.softarch.arcade.facts;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExpertDecomposition {
	public List<Group> groups = new ArrayList<>();
	public Set<Set<String>> allIntraPairs = new HashSet<>();
	
	public void ExpertDecompsition() { }
	
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
