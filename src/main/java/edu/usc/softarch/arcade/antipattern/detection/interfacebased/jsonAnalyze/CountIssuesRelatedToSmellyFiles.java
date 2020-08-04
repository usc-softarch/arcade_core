package edu.usc.softarch.arcade.antipattern.detection.interfacebased.jsonAnalyze;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class CountIssuesRelatedToSmellyFiles {
	/**
	 * Detect if one file has smell, then keep track the number of its related issues
	 */
	public static void main(String[] args) throws IOException, ParseException{
		String issue_json = "F:\\hadoop_data\\hadoop_pkg_full_shorted_removed_dc.json";
		
		Map<String, Integer> countIssuesForSmelly = new HashMap<>();
		Map<String, Integer> countIssuesForNonSmelly = new HashMap<>();
		
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
		
		//count priority
		System.out.println("smell issues");
		for (String file : countIssuesForSmelly.keySet()){
			System.out.print(countIssuesForSmelly.get(file) + ",");
		}
		
		System.out.println();
		System.out.println("non_smell issues");
		for (String file : countIssuesForNonSmelly.keySet()){
			System.out.print(countIssuesForNonSmelly.get(file) + ",");
		}
	}
	
	public static void Count_Critical(JSONArray issues){
		Map<String, Integer> counter = new HashMap<>();
		List<String> Blocker = new ArrayList<>();
		List<String> Critical = new ArrayList<>();
		List<String> Major = new ArrayList<>();
		List<String> Minor = new ArrayList<>();
		List<String> Trivial = new ArrayList<>();
		
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
	
	public static void countPercentage(List<String> values, long high_threshold, long low_threshold) {
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
		List<String> All = new ArrayList<>();
		List<String> Bug = new ArrayList<>();
		List<String> Improvement = new ArrayList<>();
		List<String> Task = new ArrayList<>();
		List<String> SubTask = new ArrayList<>();
		List<String> NewFeature = new ArrayList<>();
		
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