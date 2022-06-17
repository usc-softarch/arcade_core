package edu.usc.softarch.arcade.facts.issues.handlers;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import edu.usc.softarch.arcade.facts.issues.Commit;
import edu.usc.softarch.arcade.facts.issues.IssueRecord;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class GitLabRestHandler {
	//region EXCEPTION HANDLING
	class GitLabRestHandlerException extends Exception {
		GitLabRestHandlerException(String message, int requestCounter,
				int issueCounter, String projectId) {
			super("GitLabRestHandler failed to recover issues "
				+ "for project with ID " + projectId + " after processing "
				+ issueCounter + " issues and with a total of " + requestCounter
				+ " HTTP requests sent. Please report this exception."
				+ " Local exception message was: " + message);
		}

		GitLabRestHandlerException(String message, int requestCounter,
			int issueCounter, String projectId, Exception cause) {
			super("GitLabRestHandler failed to recover issues "
				+ "for project with ID " + projectId + " after processing "
				+ issueCounter + " issues and with a total of " + requestCounter
				+ " HTTP requests sent. Please report this exception." +
				" Local exception message was: " + message, cause);
		}
	}

	private void throwLocalException(String message)
			throws GitLabRestHandlerException {
		throw new GitLabRestHandlerException(message, this.requestCounter,
			this.issueCounter, this.projectId);
	}

	private void throwLocalException(String message, Exception cause)
			throws GitLabRestHandlerException {
		throw new GitLabRestHandlerException(message, this.requestCounter,
			this.issueCounter, this.projectId, cause);
	}
	//endregion

	//region ATTRIBUTES
	private final String projectId;
	private final HttpClient gitlabClient;
	private Collection<IssueRecord> issueRecords;
	private int requestCounter;
	private int issueCounter;
	private final boolean verbose;
	//endregion

	//region CONSTRUCTORS
	public GitLabRestHandler(String projectId) {
		this(projectId, false);
	}

	public GitLabRestHandler(String projectId, boolean verbose) {
		this.projectId = projectId;
		this.gitlabClient = HttpClient.newHttpClient();
		this.verbose = verbose;
	}
	//endregion

	//region PUBLIC INTERFACE
	public static void main(String[] args) {
		GitLabRestHandler handler =
			new GitLabRestHandler(args[0], true);
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
	public Collection<IssueRecord> getIssueRecords()
			throws IOException, InterruptedException, GitLabRestHandlerException {
		if (this.issueRecords == null) {
			if (this.verbose) {
				DateTimeFormatter dtf =
					DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
				LocalDateTime now = LocalDateTime.now();
				System.out.println(dtf.format(now) + ": Started processing issues for "
					+ "project ID " + this.projectId);
			}
			this.issueRecords = processIssues(getRawIssues());
		}
		return new ArrayList<>(issueRecords);
	}
	//endregion

	//region HTTP
	private Collection<String> getRawIssues()
			throws IOException, InterruptedException, GitLabRestHandlerException {
		// Create URI and result collection
		String baseUri = "https://gitlab.com/api/v4/projects/" + this.projectId
			+ "/issues?per_page=100&page=";
		Collection<String> issuesJson = new ArrayList<>();

		// Get the page limit from HEAD
		HttpRequest head = HttpRequest.newBuilder()
			.method("HEAD", HttpRequest.BodyPublishers.noBody())
			.uri(URI.create(baseUri + 1)).build();
		HttpResponse<Void> headers =
			this.gitlabClient.send(head, HttpResponse.BodyHandlers.discarding());
		int pageLimit = Integer.parseInt(headers.headers().map().get("x-total-pages").get(0));

		// Get the issues
		for (int i = 1; i <= pageLimit; i++)
			issuesJson.add(runHttpRequest(baseUri + i, 0));

		return issuesJson;
	}

	private String getRawCommits(String issueId)
			throws IOException, InterruptedException, GitLabRestHandlerException {
		String baseUri = "https://gitlab.com/api/v4/projects/" + this.projectId
			+ "/issues/" + issueId + "/related_merge_requests";
		return runHttpRequest(baseUri, 0);
	}

	private String getRawCommitChanges(String commitId)
			throws IOException, InterruptedException, GitLabRestHandlerException {
		String baseUri = "https://gitlab.com/api/v4/projects/" + this.projectId
			+ "/merge_requests/" + commitId + "/changes";
		return runHttpRequest(baseUri, 0);
	}

	private String getRawCommitTags(String commitSha)
			throws IOException, InterruptedException, GitLabRestHandlerException {
		String baseUri = "https://gitlab.com/api/v4/projects/" + this.projectId
			+ "/repository/commits/" + commitSha + "/refs?type=tag&per_page=100";
		return runHttpRequest(baseUri, 0);
	}

	private String runHttpRequest(String uri, int counter)
		throws IOException, InterruptedException, GitLabRestHandlerException {
		this.requestCounter++;

		HttpRequest request = HttpRequest.newBuilder()
			.uri(URI.create(uri)).build();
		HttpResponse<String> response =
			this.gitlabClient.send(request, HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() != 200 && counter < 5) {
			switch (response.statusCode()) {
				case 524:
					if (verbose)
						System.out.println("Error running HTTP Request: " + uri
							+ " with status code " + response.statusCode()
							+ " and body " + response.body() + ". This was "
							+ "attempt number " + counter + ". Trying again in 5 seconds.");
					Thread.sleep(5000);
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
	private Collection<IssueRecord> processIssues(Collection<String> rawIssues)
			throws IOException, InterruptedException, GitLabRestHandlerException {
		JsonFactory factory = new JsonFactory();
		Collection<IssueRecord> result = new ArrayList<>();

		for (String rawIssueArray : rawIssues) {
			try (JsonParser parser = factory.createParser(rawIssueArray)) {
				parser.nextToken(); // skip start array

				while (parser.nextToken().equals(JsonToken.START_OBJECT))
					result.add(parseIssue(parser));
			}
		}

		return result;
	}

	private IssueRecord parseIssue(JsonParser parser)
			throws IOException, InterruptedException, GitLabRestHandlerException {
		IssueRecordBuilder issueBuilder =
			new IssueRecordBuilder(DateTimeFormatter.ISO_INSTANT);

		while (parser.nextToken().equals(JsonToken.FIELD_NAME)) {
			String fieldName = parser.getText();
			parser.nextToken();

			switch (fieldName) {
				// Useless fields
				case "id":
				case "project_id":
				case "updated_at":
				case "user_notes_count":
				case "merge_requests_count":
				case "upvotes":
				case "downvotes":
				case "due_date":
				case "confidential":
				case "discussion_locked":
				case "blocking_issues_count":
				case "has_tasks":
				case "moved_to_id":
				case "service_desk_reply_to":
				case "epic_iid":
				case "iteration":
				case "health_status":
				case "assignees":
				case "author":
				case "time_stats":
				case "task_completion_status":
				case "_links":
				case "references":
				case "closed_by":
				case "assignee":
					skipToNextField(parser);
					break;
				// Unknown purpose fields
				case "milestone":
					String milestone = parser.getText();
					if (!milestone.equals("null"))
						throwLocalException("New milestone identified: "
							+ milestone + ", in issue ID: "	+ issueBuilder.id);
					break;
				case "issue_type":
					String issueType = parser.getText();
					if (!issueType.equals("issue"))
						throwLocalException("New issue type identified: "
							+ issueType	+ ", in issue ID: " + issueBuilder.id);
					break;
				case "severity":
					String severity = parser.getText();
					if (!severity.equals("UNKNOWN"))
						throwLocalException("New severity identified: "
							+ severity + ", in issue ID: " + issueBuilder.id);
					break;
				case "type":
					issueBuilder.type = getTextIfNotNull(parser);
					if (!issueBuilder.type.equals("ISSUE"))
						throwLocalException("New type identified: "
							+ issueBuilder.type + ", in issue ID: " + issueBuilder.id);
					break;
				case "weight":
					String weight = parser.getText();
					if (!weight.equals("null"))
						throwLocalException("New weight identified: "
							+ weight + ", in issue ID: " + issueBuilder.id);
					break;
				case "epic":
					String epic = parser.getText();
					if (!epic.equals("null"))
						throwLocalException("New epic identified: "
							+ epic + ", in issue ID: " + issueBuilder.id);
					break;
				// Useful fields
				case "iid":
					issueBuilder.id = getTextIfNotNull(parser);
					break;
				case "title":
					issueBuilder.summary = getTextIfNotNull(parser);
					break;
				case "description":
					issueBuilder.description = getTextIfNotNull(parser);
					break;
				case "state":
					issueBuilder.status = getTextIfNotNull(parser);
					break;
				case "created_at":
					issueBuilder.created = getTextIfNotNull(parser);
					break;
				case "closed_at":
					issueBuilder.resolved = getTextIfNotNull(parser);
					break;
				case "web_url":
					issueBuilder.url = getTextIfNotNull(parser);
					break;
				case "labels":
					while (!parser.nextToken().equals(JsonToken.END_ARRAY))
						issueBuilder.labels.add(parser.getText());
					break;
				// Unknown field
				default:
					throwLocalException("New field identified: "
						+ fieldName + ", in issue ID: " + issueBuilder.id);
			}
		}

		issueBuilder.linkedCommits = processCommits(getRawCommits(issueBuilder.id));
		this.issueCounter++;

		if (this.verbose && this.issueCounter % 100 == 0) {
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
			LocalDateTime now = LocalDateTime.now();
			System.out.println(dtf.format(now) + ": Finished processing "
				+ this.issueCounter + " issues.");
		}

		return issueBuilder.build();
	}
	//endregion

	//region COMMIT PARSER
	private Collection<Commit> processCommits(String rawCommits)
			throws IOException, InterruptedException, GitLabRestHandlerException {
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
			throws IOException, InterruptedException, GitLabRestHandlerException {
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
				case "user":
					skipToNextField(parser);
					break;
				// Unknown purpose fields
				case "milestone":
					String milestone = parser.getText();
					if (!milestone.equals("null"))
						throwLocalException("New milestone identified: "
							+ milestone	+ ", in issue ID: " + commitBuilder.id);
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
					throwLocalException("New field identified: " + fieldName
						+ ", in issue ID: " + commitBuilder.id);
			}
		}

		if (!externalProject) {
			commitBuilder.changes =
				processChanges(getRawCommitChanges(commitBuilder.id), commitBuilder.id);
			commitBuilder.tags =
				processTags(getRawCommitTags(commitBuilder.sha), commitBuilder.sha);
		}

		return commitBuilder.build();
	}
	//endregion

	//region CHANGES PARSER
	private Collection<Map.Entry<String, String>> processChanges(
			String rawChanges, String idForError)
			throws IOException, GitLabRestHandlerException {
		JsonFactory factory = new JsonFactory();
		Collection<Map.Entry<String, String>> result = new ArrayList<>();

		try (JsonParser parser = factory.createParser(rawChanges)) {
			/* Unfortunately, the GitLab API for getting an MR's changes also
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
			throws IOException, GitLabRestHandlerException {
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
					throwLocalException("New field identified: " + fieldName
						+ ", in issue ID: " + idForError);
			}
		}

		return new AbstractMap.SimpleEntry<>(oldPath, newPath);
	}
	//endregion

	//region TAGS PARSER
	private Collection<String> processTags(String rawTags, String shaForError)
			throws IOException, GitLabRestHandlerException {
		JsonFactory factory = new JsonFactory();
		Collection<String> result = new ArrayList<>();

		try (JsonParser parser = factory.createParser(rawTags)) {
			parser.nextToken(); // skip start array

			while (parser.nextToken().equals(JsonToken.START_OBJECT))
				result.add(parseTag(parser, shaForError));
		}

		return result;
	}

	private String parseTag(JsonParser parser, String shaForError)
			throws IOException, GitLabRestHandlerException {
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
					throwLocalException("New field identified: " + fieldName
						+ ", in issue ID: " + shaForError);
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
}
