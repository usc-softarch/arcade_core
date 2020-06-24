package edu.usc.softarch.arcade.facts;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import edu.usc.softarch.arcade.topics.DocTopicItem;

public class Group {
	public String name;
	public List<String> files = new ArrayList<String>();
	public HashSet<String> elements = new HashSet<String>();
	public HashSet<HashSet<String>> intraPairs = new HashSet<HashSet<String>>();
	public DocTopicItem docTopicItem = null;
	
	public String toString() {
		
		return toPrettyString();
	}
	
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
