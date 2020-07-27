package IssueMapper;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import util.Config;
import util.JSONUtil;
import util.StringUtil;

public class IssueCommitMerger {

	static Config globalConfig = new Config("config/global.properties");
	static String projectName = null;
	static Config projectConfig = null;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		projectName = globalConfig.getValue("project");
		projectConfig = new Config("config/" + projectName + ".properties");

		mergeIssue(projectConfig.getValue("ACDC_FILTER"),
				projectConfig.getValue("ACDC_MERGE"));
		mergeIssue(projectConfig.getValue("ARC_FILTER"),
				projectConfig.getValue("ARC_MERGE"));
		mergeIssue(projectConfig.getValue("PKG_FILTER"),
				projectConfig.getValue("PKG_MERGE"));

		System.out.println("done!");
	}

	/**
	 * merge each commits in the filtered issues, count the total smell number
	 * for each issues, remove the duplicate files in different commits, remove
	 * non-java files, skip files that are not valid, e.g. in test package
	 * 
	 * @param inputFile
	 * @param outputFile
	 */
	public static void mergeIssue(final String inputFile,
			final String outputFile) {
		JSONArray issues = (JSONArray) JSONUtil.readJsonFromFile(inputFile);
		for (int issueIdx = 0; issueIdx < issues.size(); issueIdx++) {
			int issueSmells = 0;
			JSONObject issue = (JSONObject) issues.get(issueIdx);
			JSONArray commits = (JSONArray) issue.get("commits");
			JSONObject newFiles = new JSONObject();
			for (int commitIdx = 0; commitIdx < commits.size(); commitIdx++) {
				JSONObject commit = (JSONObject) commits.get(commitIdx);
				JSONArray files = (JSONArray) commit.get("files");
				for (int fileIdx = 0; fileIdx < files.size(); fileIdx++) {
					JSONObject file = (JSONObject) files.get(fileIdx);
					String filename = (String) file.get("filename");
					String[] pkgPrefixs = projectConfig.getValue("PKG_PREFIX").split("#");
					if ((!newFiles.containsKey(filename))
							&& StringUtil.isValidFilename(filename,
									pkgPrefixs)) {
						issueSmells += Math.toIntExact((long) file
								.get("total_smell"));
						file.remove("filename");
						newFiles.put(filename, file);
					}

				}
			}
			issue.put("issue_smells", issueSmells);
			issue.put("files", newFiles);
			issue.remove("commits");
		}
		JSONUtil.writeJSONArray2File(issues, outputFile);
	}
}
