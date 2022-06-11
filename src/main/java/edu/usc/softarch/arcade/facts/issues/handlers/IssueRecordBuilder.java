package edu.usc.softarch.arcade.facts.issues.handlers;

import edu.usc.softarch.arcade.facts.issues.IssueComment;
import edu.usc.softarch.arcade.facts.issues.IssueRecord;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class IssueRecordBuilder {
	//region ATTRIBUTES
	private final String dateFormat;
	public String url;
	public String summary;
	public String description;
	public String type;
	public String priority;
	public String status;
	public String resolution;
	public String created;
	public String resolved;
	public List<String> versions;
	public List<String> fixVersions;
	public List<IssueComment> comments;
	//endregion

	//region CONSTRUCTORS
	public IssueRecordBuilder(String dateFormat) {
		this.dateFormat = dateFormat;
		this.versions = new ArrayList<>();
		this.fixVersions = new ArrayList<>();
		this.comments = new ArrayList<>();
	}
	//endregion

	//region PROCESSING
	public IssueRecord build() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(this.dateFormat);
		ZonedDateTime resolvedLocal = null;
		if (this.resolved != null)
			resolvedLocal = ZonedDateTime.parse(this.resolved, formatter);

		return new IssueRecord(this.url, this.summary, this.description,
			this.type, this.priority, this.status, this.resolution,
			ZonedDateTime.parse(this.created, formatter), resolvedLocal,
			this.versions, this.fixVersions, this.comments);
	}
	//endregion
}
