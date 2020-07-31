package edu.usc.softarch.arcade.facts;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.usc.softarch.arcade.topics.DocTopicItem;

public class Group {
	public String name;
	public List<String> files = new ArrayList<>();
	public Set<String> elements = new HashSet<>();
	public Set<Set<String>> intraPairs = new HashSet<>();
	public DocTopicItem docTopicItem = null;
	
	public String toString() { return toPrettyString();	}
	
	private String toPrettyString() {
		String tabAndLineSeparatedFileList="";
		for (String file : files) {
			tabAndLineSeparatedFileList += "\t\t" + file + "\n";
		}
		
		return name + "\n" + tabAndLineSeparatedFileList;
	}

	public String toTightString() {
		String colonSeparatedFileList="";
		for (String file : files) {
			colonSeparatedFileList += file + ":";
		}
		
		return name + "{" + colonSeparatedFileList + "}";
	}
}
