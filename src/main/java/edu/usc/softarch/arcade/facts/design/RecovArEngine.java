package edu.usc.softarch.arcade.facts.design;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
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
	public static void main(String[] args)
			throws GitLabRestHandler.GitLabRestHandlerException, IOException,
			InterruptedException {
		Collection<Decision> result =
			runWithGitLab(args[0], args[1], args[2], args[3]);

		JsonFactory factory = new JsonFactory();
		try (JsonGenerator generator = factory.createGenerator(
				new File(args[4]), JsonEncoding.UTF8)) {
			generator.writeStartObject();
			generator.writeArrayFieldStart("decisions");
			for (Decision decision : result) {
				generator.writeStartObject();
				decision.serialize(generator);
				generator.writeEndObject();
			}
			generator.writeEndArray();
			generator.writeEndObject();
		}
	}

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

		GitLabRestHandler gitLabIssueGrabber =
			new GitLabRestHandler(projectId, versionTree, true);
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
			if (!clusterFileName.contains(".rsf")) continue;
			Matcher versionMatcher = versionPattern.matcher(clusterFileName);
			if (!versionMatcher.find())
				throw new IllegalArgumentException("Could not match version scheme "
					+ versionScheme + " in the name of cluster file " + clusterFileName);
			if (versionMatcher.groupCount() > 1)
				throw new IllegalArgumentException("Found multiple matches for "
					+ "version scheme " + versionScheme + " in the name of cluster file "
					+ clusterFileName);
			String version = versionMatcher.group(0);
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

		for (VersionTree nextVersion : currentVersion.getChildren()) {
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
