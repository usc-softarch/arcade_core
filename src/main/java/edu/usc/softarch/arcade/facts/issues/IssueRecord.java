package edu.usc.softarch.arcade.facts.issues;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class IssueRecord {
	//region ATTRIBUTES
	public final String url;
	public final String summary;
	public final String description;
	public final String type;
	public final String priority;
	public final String status;
	public final String resolution;
	public final ZonedDateTime created;
	public final ZonedDateTime resolved;
	private final List<String> versions;
	private final List<String> fixVersions;
	private final List<IssueComment> comments;
	//endregion

	//region CONSTRUCTORS
	public IssueRecord(String url, String summary, String description,
			String type, String priority, String status, String resolution,
			ZonedDateTime created, ZonedDateTime resolved, List<String> version,
			List<String> fixVersions, List<IssueComment> comments) {
		this.url = url;
		this.summary = summary;
		this.description = description;
		this.type = type;
		this.priority = priority;
		this.status = status;
		this.resolution = resolution;
		this.created = created;
		this.resolved = resolved;
		this.versions = version;
		this.fixVersions = fixVersions;
		this.comments = comments;
	}
	//endregion

	//region ACCESSORS
	public List<String> getVersions() { return new ArrayList<>(this.versions); }
	public List<String> getFixVersions() {
		return new ArrayList<>(this.fixVersions); }
	public List<IssueComment> getComments() {
		return new ArrayList<>(this.comments); }
	//endregion
}
