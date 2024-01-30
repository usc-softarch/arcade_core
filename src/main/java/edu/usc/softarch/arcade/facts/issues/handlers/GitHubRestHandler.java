package edu.usc.softarch.arcade.facts.issues.handlers;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import edu.usc.softarch.arcade.facts.VersionTree;
import edu.usc.softarch.arcade.facts.issues.Commit;
import edu.usc.softarch.arcade.facts.issues.IssueRecord;
import edu.usc.softarch.util.json.EnhancedJsonGenerator;
import edu.usc.softarch.util.json.EnhancedJsonParser;

import java.util.ArrayList;
import java.util.Collection;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class GitHubRestHandler {
	//region EXCEPTION HANDLING
	public class GitHubRestHandlerException extends Exception {
		GitHubRestHandlerException(String message, int requestCounter,
								   int issueCounter, String projectId) {
			super("GitHubRestHandler failed to recover issues "
				+ "for project with ID " + projectId + " after processing "
				+ issueCounter + " issues and with a total of " + requestCounter
				+ " HTTP requests sent. Please report this exception."
				+ " Local exception message was: " + message);
		}

		GitHubRestHandlerException(String message, int requestCounter,
								   int issueCounter, String projectId, Exception cause) {
			super("GitHubRestHandler failed to recover issues "
				+ "for project with ID " + projectId + " after processing "
				+ issueCounter + " issues and with a total of " + requestCounter
				+ " HTTP requests sent. Please report this exception." +
				" Local exception message was: " + message, cause);
		}
	}

	private void throwLocalException(String message)
			throws GitHubRestHandlerException, IOException {
		throw new GitHubRestHandlerException(message, this.requestCounter,
			this.issueCounter, this.projectId);
	}

	private void throwLocalException(String message, Exception cause)
			throws GitHubRestHandlerException, IOException {
		throw new GitHubRestHandlerException(message, this.requestCounter,
			this.issueCounter, this.projectId, cause);
	}
	//endregion

	//region ATTRIBUTES
	private final String projectId; // Format: "owner/repo"
	private final String filePath;
	private final HttpClient githubClient;
	private final VersionTree versionTree;
	private Map<Integer, IssueRecord> issueRecords;
	private int requestCounter;
	private int issueCounter;
	private final boolean verbose;
	private String authToken;
	//endregion

	//region CONSTRUCTORS
	public GitHubRestHandler(String projectId, VersionTree versionTree,
                             String checkpointFilePath, String authToken) {
		this(projectId, versionTree, checkpointFilePath, authToken, false);	}

	public GitHubRestHandler(String projectId, VersionTree versionTree, String checkpointFilePath, String authToken, boolean verbose) {
		this.projectId = projectId;
		this.filePath = checkpointFilePath;
		this.githubClient = HttpClient.newHttpClient();
		this.versionTree = versionTree;
		this.verbose = verbose;
		this.authToken = authToken;
	}
	//endregion

	//region PUBLIC INTERFACE
	public static void main(String[] args) throws IOException {
		GitHubRestHandler handler = new GitHubRestHandler(
				args[0], VersionTree.deserialize(args[1]), args[2], args[3], true);
		Collection<IssueRecord> issues = null;
		try {
			issues = handler.getIssueRecords();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
			System.exit(-1);
		}

		int issueCount = issues.size();
		long issuesWithLinkedCommits = issues.stream()
				.filter(is -> is.getLinkedCommits().isEmpty()).count();

		System.out.println("Found " + issueCount + " issues, of which "
				+ issuesWithLinkedCommits + " had a linked commit.");
	}
	//region PUBLIC INTERFACE
	//region ACCESSORS


	//region ACCESSORS
	public Collection<IssueRecord> getIssueRecords()
			throws IOException, InterruptedException, GitHubRestHandler.GitHubRestHandlerException {
		if (this.issueRecords == null) {
			if (this.verbose) {
				DateTimeFormatter dtf =
						DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
				LocalDateTime now = LocalDateTime.now();
				System.out.println(dtf.format(now) + ": Started processing issues for "
						+ "project ID " + this.projectId);
			}

			recover();

			if (this.issueRecords.size() % 100 == 0) {
				this.issueRecords.putAll(processIssues(getRawIssues()));
				checkpoint();
			}
		}

		return new ArrayList<>(issueRecords.values());
	}
	//endregion

	//region HTTP
	private Collection<String> getRawIssues()
			throws IOException, InterruptedException, GitHubRestHandler.GitHubRestHandlerException {
		// Create URI and result collection
		String baseUri = "https://api.github.com/repos/" + this.projectId
				+ "/issues?state=all&per_page=100&page=";
		Collection<String> issuesJson = new ArrayList<>();

		// Get the page limit from HEAD
		HttpRequest head = HttpRequest.newBuilder()
				.method("HEAD", HttpRequest.BodyPublishers.noBody())
				.uri(URI.create(baseUri + 1))
				.header("Authorization", "token " + this.authToken)
				.build();
		HttpResponse<Void> headers =
				this.githubClient.send(head, HttpResponse.BodyHandlers.discarding());
		int maxPage = getMaxPageNumber(runHttpRequest(baseUri + 1, 0));
		int pageStart = (this.issueRecords.size() / 100) + 1;

		// Get the issues
		for (int i = pageStart; i <= maxPage; i++)
			issuesJson.add(runHttpRequest(baseUri + i, 0));

		return issuesJson;
	}

	private int getMaxPageNumber(String firstPage)
			throws IOException, InterruptedException, GitHubRestHandler.GitHubRestHandlerException
	{
		int maxPage = 0;
		JsonFactory factory = new JsonFactory();
		try {
			// Create a JsonParser instance
			JsonParser parser = factory.createParser(firstPage);

			// Check if the first token is the start of an array
			if (parser.nextToken() == JsonToken.START_ARRAY) {
				// Iterate over each element in the array (each dictionary)
				while (parser.nextToken() == JsonToken.START_OBJECT) {
					// Inside each object, iterate over the fields
					while (parser.nextToken() != JsonToken.END_OBJECT) {
						String fieldName = parser.getCurrentName();
						parser.nextToken();
						if ("number".equals(fieldName)) {
							maxPage = Integer.parseInt(parser.getText());
							return maxPage/100+1;
						}
					}
				}
			}

			parser.close(); // Close the parser
		} catch (IOException e) {
			e.printStackTrace();
		}
		return maxPage;
	}

	private String getRawCommits(String issueId)
			throws IOException, InterruptedException, GitHubRestHandler.GitHubRestHandlerException {
		String baseUri = "https://api.github.com/repos/" + this.projectId
				+ "/issues/" + issueId + "/related_merge_requests";
		return runHttpRequest(baseUri, 0);
	}

	private String getRawCommitChanges(String commitId)
			throws IOException, InterruptedException, GitHubRestHandler.GitHubRestHandlerException {
		String baseUri = "https://api.github.com/repos/" + this.projectId
				+ "/merge_requests/" + commitId + "/changes";
		return runHttpRequest(baseUri, 0);
	}

	private String getRawCommitTags(String commitSha)
			throws IOException, InterruptedException, GitHubRestHandler.GitHubRestHandlerException {
		String baseUri = "https://api.github.com/repos/" + this.projectId
				+ "/repository/commits/" + commitSha + "/refs?type=tag&per_page=100";
		return runHttpRequest(baseUri, 0);
	}

	private String runHttpRequest(String uri, int counter)
			throws IOException, InterruptedException, GitHubRestHandler.GitHubRestHandlerException {
		this.requestCounter++;

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(uri)).header("Authorization", "token " + this.authToken).build();
		HttpResponse<String> response =
				this.githubClient.send(request, HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() != 200 && counter < 10) {
			switch (response.statusCode()) {
				case 524:
					if (verbose)
						System.out.println("Error running HTTP Request: " + uri
								+ " with status code " + response.statusCode()
								+ " and body " + response.body() + ". This was "
								+ "attempt number " + counter + ". Trying again in 5 seconds.");
					Thread.sleep(10000L * counter);
					return runHttpRequest(uri, ++counter);
				default:
					throwLocalException("Error running HTTP Request: " + uri
							+ " with status code " + response.statusCode()
							+ " and body " + response.body());
			}
		} else if (response.statusCode() != 200)
			throwLocalException("Error running HTTP Request: " + uri
					+ " with status code " + response.statusCode()
					+ " and body " + response.body());

		return response.body();
	}
	//endregion

	//region ISSUE PARSER
	private Map<Integer, IssueRecord> processIssues(Collection<String> rawIssues)
			throws IOException, InterruptedException, GitHubRestHandler.GitHubRestHandlerException {
		JsonFactory factory = new JsonFactory();

		for (String rawIssueArray : rawIssues) {
			try (JsonParser parser = factory.createParser(rawIssueArray)) {
				parser.nextToken(); // skip start array

				while (parser.nextToken().equals(JsonToken.START_OBJECT)) {
					IssueRecord issueRecord = parseIssue(parser);
					if (issueRecord != null)
						this.issueRecords.put(Integer.parseInt(issueRecord.id), issueRecord);
				}
			}
		}

		return this.issueRecords;
	}

	private IssueRecord parseIssue(JsonParser parser)
			throws IOException, InterruptedException, GitHubRestHandler.GitHubRestHandlerException {
		IssueRecordBuilder issueBuilder =
				new IssueRecordBuilder(DateTimeFormatter.ISO_INSTANT);

		while (parser.nextToken().equals(JsonToken.FIELD_NAME)) {
			String fieldName = parser.getText();
			parser.nextToken();

			switch (fieldName) {
				// Handle fields from GitHub issue JSON
				case "number": // GitHub uses "number" for the issue number
					issueBuilder.id = getTextIfNotNull(parser);
					break;
				case "title":
					issueBuilder.summary = getTextIfNotNull(parser);
					break;
				case "body": // GitHub uses "body" for the issue description
					issueBuilder.description = getTextIfNotNull(parser);
					break;
				case "state": // Open or closed state
					issueBuilder.status = getTextIfNotNull(parser);
					break;
				case "created_at":
					issueBuilder.created = getTextIfNotNull(parser);
					break;
				case "closed_at":
					issueBuilder.resolved = getTextIfNotNull(parser);
					break;
				case "html_url": // Web URL of the issue
					issueBuilder.url = getTextIfNotNull(parser);
					break;
				case "labels": // Labels are an array of objects in GitHub
					List<String> labels = new ArrayList<>();
					while (parser.nextToken() != JsonToken.END_ARRAY) {
						if (parser.currentToken() == JsonToken.START_OBJECT) {
							while (parser.nextToken() != JsonToken.END_OBJECT) {
								String labelField = parser.getCurrentName();
								parser.nextToken(); // move to value
								if ("name".equals(labelField)) {
									labels.add(parser.getText());
								}
							}
						}
					}
					issueBuilder.labels = labels;
					break;
				// Skip fields that are not used
				default:
					skipToNextField(parser);
			}
		}

		// GitHub does not directly link commits to issues in the issue API response,
		// so you might need to adjust how you handle linkedCommits in GitHub context
		// issueBuilder.linkedCommits = processCommits(getRawCommits(issueBuilder.id));
		issueBuilder.linkedCommits = processCommits(getRawCommits(issueBuilder.id));
		this.issueCounter++;

		if (this.issueCounter % 100 == 0) {
			checkpoint();
			if (this.verbose) {
				DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
				LocalDateTime now = LocalDateTime.now();
				System.out.println(dtf.format(now) + ": Finished processing "
						+ this.issueCounter + " issues.");
			}
		}

		return issueBuilder.build();
	}
	//endregion

	//region COMMIT PARSER
	private Collection<Commit> processCommits(String rawCommits)
			throws IOException, InterruptedException, GitHubRestHandler.GitHubRestHandlerException {
		JsonFactory factory = new JsonFactory();
		Collection<Commit> result = new ArrayList<>();

		try (JsonParser parser = factory.createParser(rawCommits)) {
			parser.nextToken(); // skip start array

			while (parser.nextToken().equals(JsonToken.START_OBJECT))
				result.add(parseCommit(parser));
		}

		return result;
	}

	private Commit parseCommit(JsonParser parser)
			throws IOException, InterruptedException, GitHubRestHandler.GitHubRestHandlerException {
		boolean externalProject = false;
		CommitBuilder commitBuilder =
				new CommitBuilder(DateTimeFormatter.ISO_INSTANT);

		while (parser.nextToken().equals(JsonToken.FIELD_NAME)) {
			String fieldName = parser.getText();
			parser.nextToken();

			switch (fieldName) {
				// Useless fields
				case "id":
				case "updated_at":
				case "merged_by":
				case "merge_user":
				case "closed_by":
				case "closed_at":
				case "target_branch":
				case "source_branch":
				case "user_notes_count":
				case "upvotes":
				case "downvotes":
				case "author":
				case "assignees":
				case "assignee":
				case "reviewers":
				case "source_project_id":
				case "target_project_id":
				case "draft":
				case "work_in_progress":
				case "merge_when_pipeline_succeeds":
				case "merge_status":
				case "merge_commit_sha":
				case "squash_commit_sha":
				case "discussion_locked":
				case "should_remove_source_branch":
				case "force_remove_source_branch":
				case "allow_collaboration":
				case "allow_maintainer_to_push":
				case "reference":
				case "references":
				case "time_stats":
				case "squash":
				case "task_completion_status":
				case "has_conflicts":
				case "blocking_discussions_resolved":
				case "approvals_before_merge":
				case "changes_count":
				case "latest_build_started_at":
				case "latest_build_finished_at":
				case "first_deployed_to_production_at":
				case "pipeline":
				case "head_pipeline":
				case "diff_refs":
				case "merge_error":
				case "detailed_merge_status":
				case "squash_on_merge":
				case "user":
					skipToNextField(parser);
					break;
				// Unknown purpose fields
				case "milestone":
					skipToNextField(parser);
					break;
				case "prepared_at":
					skipToNextField(parser);
					break;
				// Useful fields
				case "iid":
					commitBuilder.id = getTextIfNotNull(parser);
					break;
				case "title":
					commitBuilder.summary = getTextIfNotNull(parser);
					break;
				case "description":
					commitBuilder.description = getTextIfNotNull(parser);
					break;
				case "state":
					commitBuilder.status = getTextIfNotNull(parser);
					break;
				case "created_at":
					commitBuilder.created = getTextIfNotNull(parser);
					break;
				case "merged_at":
					commitBuilder.merged = getTextIfNotNull(parser);
					break;
				case "labels":
					while (!parser.nextToken().equals(JsonToken.END_ARRAY))
						commitBuilder.labels.add(parser.getText());
					break;
				case "web_url":
					commitBuilder.url = getTextIfNotNull(parser);
					break;
				case "sha":
					commitBuilder.sha = getTextIfNotNull(parser);
					break;
				case "project_id":
					externalProject = !parser.getText().equals(this.projectId);
					break;
				// Unknown field
				default:
					skipToNextField(parser);
			}
		}

		if (!externalProject) {
			commitBuilder.changes =
					processChanges(getRawCommitChanges(commitBuilder.id), commitBuilder.id);
			commitBuilder.versionTags =
					processTags(getRawCommitTags(commitBuilder.sha), commitBuilder.sha);
		}

		return commitBuilder.build();
	}
	//endregion

	//region CHANGES PARSER
	private Collection<Map.Entry<String, String>> processChanges(
			String rawChanges, String idForError)
			throws IOException, GitHubRestHandler.GitHubRestHandlerException {
		JsonFactory factory = new JsonFactory();
		Collection<Map.Entry<String, String>> result = new ArrayList<>();

		try (JsonParser parser = factory.createParser(rawChanges)) {
			/* Unfortunately, the GitHub API for getting an MR's changes also
			 * returns the root information about the MR, which we need to skip. */
			parser.nextToken(); // start parser
			while (!parser.getText().equals("changes")) parser.nextToken();
			parser.nextToken(); // skip start array

			while (parser.nextToken().equals(JsonToken.START_OBJECT))
				result.add(parseChange(parser, idForError));

			parser.nextToken(); // skip field name overflow
			if (parser.getText().equals("true"))
				throwLocalException("Change set overflow for issue " + idForError);
		}

		return result;
	}

	private Map.Entry<String, String> parseChange(
			JsonParser parser, String idForError)
			throws IOException, GitHubRestHandler.GitHubRestHandlerException {
		String oldPath = "";
		String newPath = "";

		while (parser.nextToken().equals(JsonToken.FIELD_NAME)) {
			String fieldName = parser.getText();
			parser.nextToken();

			switch (fieldName) {
				// Useless fields
				case "a_mode":
				case "b_mode":
				case "new_file":
				case "renamed_file":
				case "deleted_file":
				case "diff":
					skipToNextField(parser);
					break;
				// Useful fields
				case "old_path":
					oldPath = getTextIfNotNull(parser);
					break;
				case "new_path":
					newPath = getTextIfNotNull(parser);
					break;
				// Unknown field
				default:
					skipToNextField(parser);
			}
		}

		return new AbstractMap.SimpleEntry<>(oldPath, newPath);
	}
	//endregion

	//region TAGS PARSER
	private Collection<String> processTags(String rawTags, String shaForError)
			throws IOException, GitHubRestHandler.GitHubRestHandlerException {
		JsonFactory factory = new JsonFactory();
		Collection<String> result = new ArrayList<>();

		try (JsonParser parser = factory.createParser(rawTags)) {
			parser.nextToken(); // skip start array

			while (parser.nextToken().equals(JsonToken.START_OBJECT)) {
				String tagValue = parseTag(parser, shaForError);
				if (versionTree.containsVersion(tagValue))
					result.add(tagValue);
			}
		}

		return result;
	}

	private String parseTag(JsonParser parser, String shaForError)
			throws IOException, GitHubRestHandler.GitHubRestHandlerException {
		String result = null;

		while (parser.nextToken().equals(JsonToken.FIELD_NAME)) {
			String fieldName = parser.getText();
			parser.nextToken();

			switch (fieldName) {
				// Useless fields
				case "type":
					skipToNextField(parser);
					break;
				// Useful fields
				case "name":
					result = getTextIfNotNull(parser);
					break;
				// Unknown field
				default:
					skipToNextField(parser);
			}
		}

		return result;
	}
	//endregion

	//region PARSER AUXILIARY
	private void skipToNextField(JsonParser parser) throws IOException {
		if (parser.currentToken().equals(JsonToken.START_ARRAY)) {
			while (!parser.nextToken().equals(JsonToken.END_ARRAY)) {
				if (parser.currentToken().equals(JsonToken.START_ARRAY))
					skipToNextField(parser);
			}
		} else if (parser.currentToken().equals(JsonToken.START_OBJECT)) {
			while (!parser.nextToken().equals(JsonToken.END_OBJECT)) {
				if (parser.currentToken().equals(JsonToken.START_OBJECT))
					skipToNextField(parser);
			}
		}
	}

	private String getTextIfNotNull(JsonParser parser) throws IOException {
		String text = parser.getText();
		if (text.equals("null")) text = null;
		return text;
	}
	//endregion

	//region SERIALIZATION
	private void checkpoint() throws IOException {
		try (EnhancedJsonGenerator generator =
					 new EnhancedJsonGenerator(this.filePath)) {
			generator.writeField("issues", this.issueRecords, true,
					"id", "record");
		}
	}

	private void recover() {
		try (EnhancedJsonParser parser =
					 new EnhancedJsonParser(this.filePath)) {
			this.issueRecords = parser.parseMap(Integer.class, IssueRecord.class);
		} catch (IOException e) {
			this.issueRecords = new HashMap<>();
			if (verbose)
				System.out.println("No record located, running full scan.");
		}
	}
	//endregion
}