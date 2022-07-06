package edu.usc.softarch.arcade.facts.design;

import edu.usc.softarch.arcade.facts.VersionMap;
import edu.usc.softarch.arcade.facts.VersionTree;
import edu.usc.softarch.arcade.facts.issues.IssueRecord;
import edu.usc.softarch.arcade.facts.issues.handlers.GitLabRestHandler;
import edu.usc.softarch.util.json.EnhancedJsonGenerator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class RecovArEngine {
	//region PUBLIC INTERFACE
	public static void main(String[] args)
			throws GitLabRestHandler.GitLabRestHandlerException, IOException,
			InterruptedException {
		Collection<CodeElementDecision> result;
		if (args.length > 4)
 			result = runWithGitLab(args[0], args[1], args[2], args[3], args[4]);
		else
			result = runWithGitLab(args[0], args[1], args[2], args[3]);

		//TODO this is wrong
		try (EnhancedJsonGenerator generator = new EnhancedJsonGenerator(args[5])) {
			generator.writeField("decisions", result);
		}
	}

	public static Collection<CodeElementDecision> runWithGitLab(
			String clusterDirPath, String versionTreePath, String projectId,
			String checkpointFilePath)
			throws IOException, GitLabRestHandler.GitLabRestHandlerException,
				InterruptedException {
		return runWithGitLab(clusterDirPath, versionTreePath, projectId,
			checkpointFilePath, null);
	}

	public static Collection<CodeElementDecision> runWithGitLab(
			String clusterDirPath, String versionTreePath, String projectId,
			String checkpointFilePath, String versionScheme)
			throws IOException, GitLabRestHandler.GitLabRestHandlerException,
				InterruptedException {
		RecovArEngine engine = new RecovArEngine(clusterDirPath, versionTreePath,
			projectId, checkpointFilePath, versionScheme);

		return engine.getDecisions();
	}
	//endregion

	//region ATTRIBUTES
	private final VersionTree versionTree;
	private final VersionMap versionMap;
	private final DecisionAnalyzer decisionAnalyzer;
	private Collection<CodeElementDecision> decisions;
	//endregion

	//region CONSTRUCTORS
	public RecovArEngine(String clusterDirPath,	String versionTreePath,
			String projectId, String checkpointFilePath, String versionScheme)
			throws IOException, GitLabRestHandler.GitLabRestHandlerException,
				InterruptedException {
		this.versionTree = VersionTree.deserialize(versionTreePath);
		this.versionMap = new VersionMap(versionScheme, clusterDirPath);

		GitLabRestHandler gitLabIssueGrabber = new GitLabRestHandler(
			projectId, versionTree, checkpointFilePath, true);
		Collection<IssueRecord> issues = gitLabIssueGrabber.getIssueRecords();
		this.decisionAnalyzer = new DecisionAnalyzer(issues);
	}
	//endregion

	//region ACCESSORS
	public Collection<CodeElementDecision> getDecisions() throws IOException {
		if (this.decisions == null)
			this.decisions = loadDecisions(this.versionTree);
		return this.decisions;
	}
	//endregion

	//region PROCESSING
	private Collection<CodeElementDecision> loadDecisions(
			VersionTree currentVersion) throws IOException {
		Collection<CodeElementDecision> result = new ArrayList<>();

		for (VersionTree nextVersion : currentVersion.getChildren()) {
			File currentVersionFile = this.versionMap.get(currentVersion.node);
			File nextVersionFile = this.versionMap.get(nextVersion.node);

			ElementChangeAnalyzer changeAnalyzer = new ElementChangeAnalyzer(
				currentVersionFile, nextVersionFile);

			result.addAll(this.decisionAnalyzer.getDecisionList(
				nextVersion.node, changeAnalyzer.getChangeList()));

			result.addAll(this.loadDecisions(nextVersion));
		}

		return result;
	}
	//endregion
}
