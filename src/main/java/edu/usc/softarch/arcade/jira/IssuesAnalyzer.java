package edu.usc.softarch.arcade.jira;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.rcarz.jiraclient.Issue;
import net.rcarz.jiraclient.Version;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.thoughtworks.xstream.XStream;

import edu.usc.softarch.arcade.util.FileListing;
import edu.usc.softarch.arcade.util.FileUtil;
import edu.usc.softarch.arcade.util.MapUtil;

public class IssuesAnalyzer {
	static Logger logger = LogManager.getLogger(IssuesAnalyzer.class);

	public static void main(String[] args) throws FileNotFoundException {
		// The directory where the serialized issue files are stored
		String issuesDir = args[0];
		
		List<File> filesList = FileListing.getFileListing(new File(FileUtil.tildeExpandPath(issuesDir)));
		
		List<Issue> allIssues = new ArrayList<>();
		for (File file : filesList) {
			if (file.getName().endsWith(".ser")) {
				List<Issue> issues = JiraUtil.deserializeIssues(file.getAbsolutePath());
				allIssues.addAll(issues);
				System.out.println("Loaded file: " + file.getName());
			}
		}
		
		Map<String,Issue> issuesMap = new HashMap<>();
		List<Issue> allIssuesNoDupes = new ArrayList<>();
		for (Issue issue : allIssues) {
			if (!issuesMap.containsKey(issue.getKey())) {
				issuesMap.put(issue.getKey(),issue);
				allIssuesNoDupes.add(issue);
			}
		}
		
		System.out.println("allIssues size: " + allIssues.size());
		System.out.println("allIssuesNoDupes size: " + allIssuesNoDupes.size());
		
		
		// key: version number, value: count of issues for that version
		Map<String,Integer> versionToIssueCountMap = new HashMap<>();
		for (Issue issue : allIssues) {
			for (Version version : issue.getVersions()) {
				Integer issueCount = versionToIssueCountMap.get(version.toString());
				if (issueCount != null) {
					issueCount=issueCount+1;
					versionToIssueCountMap.put(version.toString(), issueCount);
				}
				else
					versionToIssueCountMap.put(version.toString(), 1);
			}
			for (Version version : issue.getFixVersions()) {
				Integer issueCount = versionToIssueCountMap.get(version.toString());
				if (issueCount != null) {
					issueCount=issueCount+1;
					versionToIssueCountMap.put(version.toString(), issueCount);
				}
				else
					versionToIssueCountMap.put(version.toString(), 1);
			}
		}
		
		// You may need to change the line below so that sortbyKeyVersion works for your project
		versionToIssueCountMap = MapUtil.sortByKeyVersion(versionToIssueCountMap);
		Map<String, Integer> versionToIssueCountMapPrintable =
			new HashMap<>(versionToIssueCountMap);
		
		System.out.println(String.join("\n", versionToIssueCountMapPrintable
			.keySet().stream()
			.map(key -> key + "=" + versionToIssueCountMapPrintable.get(key))
			.collect(Collectors.toList())));

		// The filename that is generated based on the supplied issuesDir
		String mapFilename = issuesDir + File.separatorChar + "version2issuecountmap.obj";
		XStream xstream = new XStream();
		String xml = xstream.toXML(versionToIssueCountMap);
		PrintWriter writer = new PrintWriter(FileUtil.tildeExpandPath(mapFilename));
		writer.print(xml);
		writer.close();
	}
}