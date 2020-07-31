package edu.usc.softarch.arcade.antipattern.detection.interfacebased.jsonAnalyze;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class DetectSmellIssues {
	public static void main(String[] args) throws IOException, ParseException{
		String issue_json = "F:\\hadoop_data\\hadoop_pkg_full_shorted_removed_dc.json";
		String merged_output_file = "F:\\hadoop_data\\hadoop_pkg_all";
		
		
		JSONParser parser = new JSONParser();
		JSONArray issues = (JSONArray) parser.parse(new FileReader(issue_json));
		JSONArray non_smell_issues = new JSONArray();
		JSONArray smell_issues = new JSONArray();
		
		for (int i = 0; i < issues.size(); i ++){
			JSONObject issue = (JSONObject) issues.get(i);
			boolean isSmell = false;		
			JSONArray commits = (JSONArray) issue.get("commits");
			
			commitloop:
			for (int j = 0; j < commits.size(); j++){
				JSONObject commit = (JSONObject) commits.get(j);
				JSONArray files = (JSONArray) commit.get("files");
				for (int k = 0; k < files.size(); k ++){
					JSONObject file = (JSONObject) files.get(k);
					JSONObject smells = (JSONObject) file.get("smells");
					
					if (smells != null){
						isSmell = true;
						break commitloop;
					}
				}
			}
			
			if (isSmell)
				smell_issues.add(issue);
			else
				if (!((String) issue.get("affect")).equals("")){
					non_smell_issues.add(issue);
				}
		}
		
		File json_file = new File(merged_output_file+"_smell.json");
		// if file doesnt exists, then create it
		if (!json_file.exists()) {
			json_file.createNewFile();
		}
		FileWriter fw = new FileWriter(json_file.getAbsolutePath());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(smell_issues.toJSONString());
		bw.close();
		
		json_file = new File(merged_output_file+"_non_smell.json");
		// if file doesnt exists, then create it
		if (!json_file.exists()) {
			json_file.createNewFile();
		}
		fw = new FileWriter(json_file.getAbsolutePath());
		bw = new BufferedWriter(fw);
		bw.write(non_smell_issues.toJSONString());
		bw.close();

		System.out.println("Done");
		
		//count priority
		System.out.println("smell issues");
		Count_Issues_Type(smell_issues);
		
		System.out.println();
		System.out.println("non_smell issues");
		Count_Issues_Type(non_smell_issues);
	}
	
	public static void Count_Critical(JSONArray issues){
		Map<String, Integer> counter = new HashMap<>();
		Vector<String> Blocker = new Vector<>();
		Vector<String> Critical = new Vector<>();
		Vector<String> Major = new Vector<>();
		Vector<String> Minor = new Vector<>();
		Vector<String> Trivial = new Vector<>();
		
		for (int i = 0; i < issues.size(); i ++){
			JSONObject issue = (JSONObject) issues.get(i);
			String priority = (String) issue.get("priority");
			String time = String.valueOf((issue.get("time")));
			
			switch (priority) {
			case "Blocker":
				Blocker.add(time);
				break;
				
			case "Critical":
				Critical.add(time);
				break;
	
			case "Major":
				Major.add(time);
				break;
			
			case "Minor":
				Minor.add(time);
				break;

			case "Trivial":
				Trivial.add(time);
				break;

			default:
				break;
			} 
			
			Integer tmp = counter.get(priority);
			if (tmp == null)
				tmp = 1;
			else
				tmp ++;
			
			counter.put(priority, tmp);
		}
		
		for (String priority : counter.keySet()){
			System.out.printf(priority + "," + counter.get(priority) + ",%.2f\n", (float) counter.get(priority)/issues.size());
		}
		
		long high_threshold = 90000;
		long log_threshold = 70000;
		countPercentage(Blocker, high_threshold, log_threshold);
		countPercentage(Critical, high_threshold, log_threshold);
		countPercentage(Major, high_threshold, log_threshold);
		countPercentage(Minor, high_threshold, log_threshold);
		countPercentage(Trivial, high_threshold, log_threshold);
	}
	
	public static void countPercentage(Vector<String> values, long high_threshold, long low_threshold) {
		int counter = 0;
		for (String s: values){
			long time = Long.parseLong(s);
			if (time < high_threshold && time > low_threshold){
				counter ++;
			}
		}
		System.out.printf("Larger than %d %.2f\n", high_threshold, (float) counter/values.size());
	}

	public static void Count_Issues_Type(JSONArray issues){
		
		Map<String, Integer> counter = new HashMap<>();
		Vector<String> All = new Vector<>();
		Vector<String> Bug = new Vector<>();
		Vector<String> Improvement = new Vector<>();
		Vector<String> Task = new Vector<>();
		Vector<String> SubTask = new Vector<>();
		Vector<String> NewFeature = new Vector<>();
		
		for (int i = 0; i < issues.size(); i ++){
			JSONObject issue = (JSONObject) issues.get(i);
			String type = (String) issue.get("type");
			String time = String.valueOf((issue.get("time")));
			
			switch (type) {
			case "Bug":
				Bug.add(time);
				break;
				
			case "Improvement":
				Improvement.add(time);
				break;
	
			case "Task":
				Task.add(time);
				break;
			
			case "Sub-task":
				SubTask.add(time);
				break;

			case "New Feature":
				NewFeature.add(time);
				break;

			default:
				break;
			} 
			
			Integer tmp = counter.get(type);
			if (tmp == null)
				tmp = 1;
			else
				tmp ++;
			
			counter.put(type, tmp);
		}
		
		for (String type : counter.keySet()){
			System.out.printf(type + "," + counter.get(type) + ",%.2f\n", (float) counter.get(type)/issues.size());
		}
		
		All.addAll(Improvement);
		All.addAll(Bug);
		All.addAll(NewFeature);
		All.addAll(Task);
		All.addAll(SubTask);
		
		long high_threshold = 90000;
		long log_threshold = 85000;
		countPercentage(Improvement, high_threshold, log_threshold);
		countPercentage(Bug, high_threshold, log_threshold);
		countPercentage(NewFeature, high_threshold, log_threshold);
		countPercentage(Task, high_threshold, log_threshold);
		countPercentage(SubTask, high_threshold, log_threshold);
		countPercentage(All, high_threshold, log_threshold);
	}
}
