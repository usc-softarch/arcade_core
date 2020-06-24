package edu.usc.softarch.arcade.jira;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.rcarz.jiraclient.Issue;
import net.rcarz.jiraclient.Version;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.google.common.base.Joiner;
import com.thoughtworks.xstream.XStream;

import edu.usc.softarch.arcade.config.Config;
import edu.usc.softarch.arcade.util.FileListing;
import edu.usc.softarch.arcade.util.FileUtil;
import edu.usc.softarch.arcade.util.MapUtil;
import edu.usc.softarch.arcade.util.StopWatch;

public class IssuesAnalyzer {
	static Logger logger = Logger.getLogger(IssuesAnalyzer.class);

	public static void main(String[] args) throws FileNotFoundException {
		PropertyConfigurator.configure(Config.getLoggingConfigFilename());
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		// The directory where the serialized issue files are stored
		String issuesDir = args[0];
		
		List<File> filesList = FileListing.getFileListing(new File(FileUtil.tildeExpandPath(issuesDir)));
		
		List<Issue> allIssues = new ArrayList<Issue>();
		for (File file : filesList) {
			if (file.getName().endsWith(".ser")) {
				List<Issue> issues = JiraUtil.deserializeIssues(file.getAbsolutePath());
				//System.out.println(Joiner.on("\n").join(issues));
				allIssues.addAll(issues);
				System.out.println("Loaded file: " + file.getName());
			}
		}
		
		Map<String,Issue> issuesMap = new HashMap<String,Issue>();
		List<Issue> allIssuesNoDupes = new ArrayList<Issue>();
		for (Issue issue : allIssues) {
			if (!issuesMap.containsKey(issue.getKey())) {
				issuesMap.put(issue.getKey(),issue);
				allIssuesNoDupes.add(issue);
			}
		}
		
		System.out.println("allIssues size: " + allIssues.size());
		System.out.println("allIssuesNoDupes size: " + allIssuesNoDupes.size());
		
		
		// key: version number, value: count of issues for that version
		Map<String,Integer> versionToIssueCountMap = new HashMap<String,Integer>();
		for (Issue issue : allIssues) {
			for (Version version : issue.getVersions()) {
				Integer issueCount = versionToIssueCountMap.get(version.toString());
				if (issueCount != null) {
					issueCount=issueCount+1;
					versionToIssueCountMap.put(version.toString(), issueCount);
				}
				else {
					versionToIssueCountMap.put(version.toString(), 1);
				}
			}
			for (Version version : issue.getFixVersions()) {
				Integer issueCount = versionToIssueCountMap.get(version.toString());
				if (issueCount != null) {
					issueCount=issueCount+1;
					versionToIssueCountMap.put(version.toString(), issueCount);
				}
				else {
					versionToIssueCountMap.put(version.toString(), 1);
				}
			}
		}
		
		// You may need to change the line below so that sortbyKeyVersion works for your project
		versionToIssueCountMap = MapUtil.sortByKeyVersion(versionToIssueCountMap);
		
		System.out.println(Joiner.on("\n").withKeyValueSeparator("=").join(versionToIssueCountMap));
		
        System.out.println("Running time: " + stopWatch.getElapsedTimeSecs());
        
        // The filename that is generated based on the supplied issuesDir
        String mapFilename = issuesDir + File.separatorChar + "version2issuecountmap.obj";
        XStream xstream = new XStream();
        String xml = xstream.toXML(versionToIssueCountMap);
        PrintWriter writer = new PrintWriter(FileUtil.tildeExpandPath(mapFilename));
        writer.print(xml);
        writer.close();

	}

}
