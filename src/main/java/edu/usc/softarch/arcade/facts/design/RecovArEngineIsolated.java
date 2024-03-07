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

public class RecovArEngineIsolated {
	//region PUBLIC INTERFACE
	public static void main(String[] args)
			throws GitLabRestHandler.GitLabRestHandlerException, IOException,
			InterruptedException {
		Collection<CodeElementDecision> result = null;

		if (args.length >= 5)
 			result = runWithGitHub(args[0], args[1], args[2], args[3]);
		else
			System.out.println("Usage: java -cp ARCADE_Core.jar edu.usc.softarch.arcade.facts.design.RecovArEngineIsolated <cluster_directory> <versionTree_path> <updated_issue_path> <version_regex> <output_json_path>");

		EnhancedJsonGenerator generator = new EnhancedJsonGenerator(args[4]);
		generator.writeField("decisions", result);
		generator.close();
	}

	public static Collection<CodeElementDecision> runWithGitHub(
			String clusterDirPath, String versionTreePath,
			String checkpointFilePath, String versionScheme)
			throws IOException, GitLabRestHandler.GitLabRestHandlerException,
				InterruptedException {
		RecovArEngineIsolated engine = new RecovArEngineIsolated(clusterDirPath, versionTreePath, checkpointFilePath, versionScheme);

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
	public RecovArEngineIsolated(String clusterDirPath, String versionTreePath, String checkpointFilePath, String versionScheme)
			throws IOException, GitLabRestHandler.GitLabRestHandlerException,
				InterruptedException {
		this.versionTree = VersionTree.deserialize(versionTreePath);
		this.versionMap = new VersionMap(clusterDirPath, versionScheme);

		GitLabRestHandler gitLabIssueGrabber = new GitLabRestHandler(
				"GitHub", versionTree, checkpointFilePath, true);
		Collection<IssueRecord> issues = gitLabIssueGrabber.getGitHubIssueRecords();
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
