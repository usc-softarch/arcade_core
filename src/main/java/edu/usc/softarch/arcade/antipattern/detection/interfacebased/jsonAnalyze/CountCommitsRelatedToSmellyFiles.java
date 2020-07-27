package edu.usc.softarch.arcade.antipattern.detection.interfacebased.jsonAnalyze;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import net.sourceforge.pmd.lang.java.rule.coupling.CouplingBetweenObjectsRule;

public class CountCommitsRelatedToSmellyFiles {
	
	/*
	 * Detect if one file has smell, then keep track the number of its related commits
	 * 
	 * 
	 */
	
	
	public static void main(String[] args) throws FileNotFoundException, IOException, ParseException{
		String issue_json = "F:\\hadoop_data\\hadoop_pkg_full_shorted_removed_dc.json";
		String commit_freq = "F:\\hadoop_data\\hadoop_commits.txt";
		
		HashMap<String, Integer> countIssuesForSmelly = new HashMap<>();
		HashMap<String, Integer> countIssuesForNonSmelly = new HashMap<>();
		
		
		HashMap<String, String> commitFreg = new HashMap<>();
		
		
		// Parse the frequency file
		BufferedReader br = null;
		String sCurrentLine;

		br = new BufferedReader(new FileReader(commit_freq));

		while ((sCurrentLine = br.readLine()) != null) {
			String freg = sCurrentLine.split(" ")[0];
			String fileName = sCurrentLine.split(" ")[1];
//			System.out.println(sCurrentLine);
			commitFreg.put(fileName, freg);
		}
		if (br != null)br.close();
		
		
		JSONParser parser = new JSONParser();
		JSONArray issues = (JSONArray) parser.parse(new FileReader(issue_json));
		
		for (int i = 0; i < issues.size(); i ++){
			JSONObject issue = (JSONObject) issues.get(i);
			boolean isSmell = false;		
			JSONArray commits = (JSONArray) issue.get("commits");
			
			for (int j = 0; j < commits.size(); j++){
				JSONObject commit = (JSONObject) commits.get(j);
				JSONArray files = (JSONArray) commit.get("files");
				for (int k = 0; k < files.size(); k ++){
					JSONObject file = (JSONObject) files.get(k);
					JSONObject smells = (JSONObject) file.get("smells");
					
					if (smells != null && smells.keySet() != null){
						isSmell = true;
					}
					
					if (isSmell){
						Integer counter = countIssuesForSmelly.get(file.get("filename"));
						if (counter == null)
							counter = 1;
						else
							counter ++;

						countIssuesForSmelly.put((String) file.get("filename"), counter);
					}
					else
					{
						if (!((String) issue.get("affect")).equals("")){
							String filename = (String) file.get("filename");
							if (filename.endsWith("java")){
								Integer counter = countIssuesForNonSmelly.get(filename);
								if (counter == null)
									counter = 1;
								else
									counter ++;
								countIssuesForNonSmelly.put(filename, counter);
							}
						}
					}
				}
			}
		}
		
		
		//count commit freg
		System.out.println("smell issues");
		for (String file : countIssuesForSmelly.keySet()){
			System.out.print(commitFreg.get(file) + ",");
		}
		
		System.out.println();
		System.out.println("non_smell issues");
		for (String file : countIssuesForNonSmelly.keySet()){
			System.out.print(commitFreg.get(file) + ",");
		}
	}
}
