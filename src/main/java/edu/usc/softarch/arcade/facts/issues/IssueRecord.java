package edu.usc.softarch.arcade.facts.issues;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class IssueRecord {
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
	private final Collection<String> fixVersions;
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
		this.fixVersions = fixVersions;
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
		return new ArrayList<>(this.fixVersions); }
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
}
