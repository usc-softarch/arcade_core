package edu.usc.softarch.arcade.facts.issues;

import edu.usc.softarch.arcade.facts.issues.handlers.IssueRecordBuilder;
import edu.usc.softarch.util.json.EnhancedJsonGenerator;
import edu.usc.softarch.util.json.EnhancedJsonParser;
import edu.usc.softarch.util.json.JsonSerializable;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class IssueRecord implements JsonSerializable {
	//region ATTRIBUTES
	public final String id;
	public final String url;
	public final String summary;
	public final String description;
	public final String type;
	public final String priority;
	public final String status;
	public final String resolution;
	public final ZonedDateTime created;
	public final ZonedDateTime resolved;
	private final Collection<String> labels;
	private final Collection<String> versions;
	private final Set<String> fixVersions;
	private final Collection<IssueComment> comments;
	private final Collection<Commit> linkedCommits;
	//endregion

	//region CONSTRUCTORS
	public IssueRecord(String id, String url, String summary, String description,
			String type, String priority, String status, String resolution,
			ZonedDateTime created, ZonedDateTime resolved, Collection<String> labels,
			Collection<String> version, Collection<String> fixVersions,
			Collection<IssueComment> comments, Collection<Commit> linkedCommits) {
		this.id = id;
		this.url = url;
		this.summary = summary;
		this.description = description;
		this.type = type;
		this.priority = priority;
		this.status = status;
		this.resolution = resolution;
		this.created = created;
		this.resolved = resolved;
		this.labels = labels;
		this.versions = version;
		this.fixVersions = new HashSet<>(fixVersions);
		this.comments = comments;
		this.linkedCommits = linkedCommits;
	}
	//endregion

	//region ACCESSORS
	public Collection<String> getLabels() {
		return new ArrayList<>(this.labels); }
	public Collection<String> getVersions() {
		return new ArrayList<>(this.versions); }
	public Collection<String> getFixVersions() {
		if (this.fixVersions.isEmpty()) {
			for (Commit commit : this.linkedCommits)
				this.fixVersions.addAll(commit.getVersionTags());
		}
		return new ArrayList<>(this.fixVersions);
	}
	public Collection<IssueComment> getComments() {
		return new ArrayList<>(this.comments); }
	public Collection<Commit> getLinkedCommits() {
		return new ArrayList<>(this.linkedCommits); }
	public Collection<Map.Entry<String, String>> getFileChanges() {
		Collection<Map.Entry<String, String>> fileChanges = new ArrayList<>();
		for (Commit commit : this.linkedCommits)
			fileChanges.addAll(commit.getChanges());

		return fileChanges;
	}
	//endregion

	//region SERIALIZATION
	@Override
	public void serialize(EnhancedJsonGenerator generator) throws IOException {
		generator.writeField("id", id);
		generator.writeField("url", url);
		generator.writeField("summary", summary);
		generator.writeField("description", description);
		generator.writeField("type", type);
		generator.writeField("priority", priority);
		generator.writeField("status", status);
		generator.writeField("resolution", resolution);
		generator.writeField("created",
			created.toString().replace("[Universal]", ""));
		if (resolved != null)
			generator.writeField("resolved",
				resolved.toString().replace("[Universal]", ""));
		else
			generator.writeField("resolved", "");
		generator.writeField("labels", labels);
		generator.writeField("versions", versions);
		generator.writeField("fixVersions", fixVersions);
		generator.writeField("comments", comments);
		generator.writeField("linkedCommits", linkedCommits);
	}

	public static IssueRecord deserialize(EnhancedJsonParser parser)
			throws IOException {
		IssueRecordBuilder issueBuilder =
			new IssueRecordBuilder(DateTimeFormatter.ISO_INSTANT);

		issueBuilder.id = parser.parseString();
		issueBuilder.url = parser.parseString();
		issueBuilder.summary = parser.parseString();
		issueBuilder.description = parser.parseString();
		issueBuilder.type = parser.parseString();
		issueBuilder.priority = parser.parseString();
		issueBuilder.status = parser.parseString();
		issueBuilder.resolution = parser.parseString();
		issueBuilder.created = parser.parseString();
		issueBuilder.resolved = parser.parseString();
		issueBuilder.labels = parser.parseCollection(String.class);
		issueBuilder.versions = parser.parseCollection(String.class);
		issueBuilder.fixVersions = new HashSet<>(
			parser.parseCollection(String.class));
		issueBuilder.comments = parser.parseCollection(IssueComment.class);
		issueBuilder.linkedCommits = parser.parseCollection(Commit.class);

		return issueBuilder.build();
	}
	//endregion
}
