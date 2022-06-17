package edu.usc.softarch.arcade.facts.design;

import edu.usc.softarch.arcade.facts.VersionTree;
import edu.usc.softarch.arcade.facts.issues.IssueRecord;
import edu.usc.softarch.arcade.facts.issues.handlers.GitLabRestHandler;
import edu.usc.softarch.arcade.util.FileUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RecovArEngine {
	//region PUBLIC INTERFACE
	public static Collection<Decision> runWithGitLab(String clusterDirPath,
			String versionTreePath, String projectId, String versionScheme)
			throws IOException, GitLabRestHandler.GitLabRestHandlerException,
				InterruptedException {
		RecovArEngine engine = new RecovArEngine(
			clusterDirPath, versionTreePath, projectId, versionScheme);

		return engine.getDecisions();
	}
	//endregion

	//region ATTRIBUTES
	private final VersionTree versionTree;
	private final Map<String, File> versionMap;
	private final DecisionAnalyzer decisionAnalyzer;
	private Collection<Decision> decisions;
	//endregion

	//region CONSTRUCTORS
	public RecovArEngine(String clusterDirPath,	String versionTreePath,
			String projectId, String versionScheme)
			throws IOException, GitLabRestHandler.GitLabRestHandlerException,
				InterruptedException {
		this.versionTree = VersionTree.deserialize(versionTreePath);
		this.versionMap = initializeVersionMap(versionScheme, clusterDirPath);

		GitLabRestHandler gitLabIssueGrabber = new GitLabRestHandler(projectId);
		Collection<IssueRecord> issues = gitLabIssueGrabber.getIssueRecords();
		this.decisionAnalyzer = new DecisionAnalyzer(issues);
	}

	private Map<String, File> initializeVersionMap(String versionScheme,
			String clusterDirPath) throws FileNotFoundException {
		Map<String, File> versionMapLoader = new HashMap<>();
		Collection<File> clusterFiles =
			FileUtil.getFileListing(new File(clusterDirPath));
		Pattern versionPattern = Pattern.compile(versionScheme);

		for (File clusterFile : clusterFiles) {
			String clusterFileName = clusterFile.getName();
			Matcher versionMatcher = versionPattern.matcher(clusterFileName);
			if (!versionMatcher.find())
				throw new IllegalArgumentException("Could not match version scheme "
					+ versionScheme + " in the name of cluster file " + clusterFileName);
			if (versionMatcher.groupCount() > 1)
				throw new IllegalArgumentException("Found multiple matches for "
					+ "version scheme " + versionScheme + " in the name of cluster file "
					+ clusterFileName);
			String version = versionMatcher.group(1);
			versionMapLoader.put(version, clusterFile);
		}

		return versionMapLoader;
	}
	//endregion

	//region ACCESSORS
	public Collection<Decision> getDecisions() throws IOException {
		if (this.decisions == null)
			this.decisions = loadDecisions(this.versionTree);
		return this.decisions;
	}
	//endregion

	//region PROCESSING
	private Collection<Decision> loadDecisions(VersionTree currentVersion)
			throws IOException {
		Collection<Decision> result = new ArrayList<>();

		for (VersionTree nextVersion : versionTree.getChildren()) {
			File currentVersionFile = this.versionMap.get(currentVersion.node);
			File nextVersionFile = this.versionMap.get(nextVersion.node);

			ChangeAnalyzer changeAnalyzer = new ChangeAnalyzer(
				currentVersionFile, nextVersionFile);

			result.addAll(this.decisionAnalyzer.getDecisionList(
				nextVersion.node, changeAnalyzer.getChangeList()));

			result.addAll(this.loadDecisions(nextVersion));
		}

		return result;
	}
	//endregion
}
