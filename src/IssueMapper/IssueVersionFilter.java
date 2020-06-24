package IssueMapper;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import util.Config;
import util.Constant;
import util.JSONUtil;
import util.StringUtil;

public class IssueVersionFilter {

	static Set<String> versions;
	static Config globalConfig = new Config("config/global.properties");
	static String projectName = null;
	static Config projectConfig = null;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		projectName = globalConfig.getValue("project");
		projectConfig = new Config("config/" + projectName + ".properties");
		
		versions = getVersionsFromSmell(projectConfig.getValue("SMELL_ACDC"));
		filterIssues(projectConfig.getValue("ALL_ACDC"), projectConfig.getValue("ACDC_FILTER"));
		versions = getVersionsFromSmell(projectConfig.getValue("SMELL_ARC"));
		filterIssues(projectConfig.getValue("ALL_ARC"), projectConfig.getValue("ARC_FILTER"));
		versions = getVersionsFromSmell(projectConfig.getValue("SMELL_PKG"));
		filterIssues(projectConfig.getValue("ALL_PKG"), projectConfig.getValue("PKG_FILTER"));
		
		 versions = getVersionsFromIssue(projectConfig.getValue("ISSUE_FILE"));
		 StringUtil.printStringSet(versions);
		System.out.println("done!");

	}

	/**
	 * filter the issues only output the issues whose affected version have
	 * smells remove dependency cycles add total smell numbers for each file
	 * 
	 * @param inputIssues
	 * @param outputIssues
	 */
	public static void filterIssues(final String inputIssues,
			final String outputIssues) {
		JSONArray issues = (JSONArray) JSONUtil.readJsonFromFile(inputIssues);
		for (int i = 0; i < issues.size(); i++) {
			JSONObject issue = (JSONObject) issues.get(i);
			String affectVersion = (String) issue.get("affect");
			String issueID = (String) issue.get("issue_id");
			if (!versions.contains(affectVersion)) {
				issues.remove(i);
				// remove one issue, the index should be reduced by 1!!!
				i--;
				continue;
			}
			JSONArray commits = (JSONArray) issue.get("commits");
			for (int j = 0; j < commits.size(); j++) {
				JSONObject commit = (JSONObject) commits.get(j);
				JSONArray files = (JSONArray) commit.get("files");
				for (int k = 0; k < files.size(); k++) {
					JSONObject file = (JSONObject) files.get(k);
					if (file.get("smells") == null) {
						file.put("total_smell", 0);
					} else {
						JSONObject smellsPerFile = (JSONObject) file
								.get("smells");
						if (smellsPerFile.containsKey("bdc")) {
							smellsPerFile.remove("bdc");
						}
						int total_smell = 0;
						Iterator it = smellsPerFile.entrySet().iterator();
						while (it.hasNext()) {
							Map.Entry pair = (Map.Entry) it.next();
							total_smell += Integer.parseInt(pair.getValue()
									.toString());
						}
						file.put("total_smell", total_smell);
					}
				}
			}
		}
		JSONUtil.writeJSONArray2File(issues, outputIssues);
	}

	/**
	 * read the class smell file, get all the versions which have the smells
	 * data
	 * 
	 * @return
	 */
	public static Set<String> getVersionsFromSmell(final String inputFile) {
		JSONArray smells = (JSONArray) JSONUtil.readJsonFromFile(inputFile);
		Set<String> versions = new HashSet<String>();
		for (int i = 0; i < smells.size(); i++) {
			JSONObject smell = (JSONObject) smells.get(i);
			versions.add(smell.get("version").toString());
		}
		return versions;
	}

	/**
	 * read the original issue file, get all the versions in "affect" field
	 * 
	 * @param inputFile
	 * @return
	 */
	public static Set<String> getVersionsFromIssue(final String issueFile) {
		JSONArray issues = (JSONArray) JSONUtil.readJsonFromFile(issueFile);
		Set<String> versions = new HashSet<String>();
		for (int i = 0; i < issues.size(); i++) {
			JSONObject issue = (JSONObject) issues.get(i);
			versions.add(issue.get("affect").toString());
		}
		return versions;
	}
}
