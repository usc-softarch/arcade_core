package edu.usc.softarch.arcade.facts.issues.handlers;

import edu.usc.softarch.arcade.facts.issues.Commit;
import edu.usc.softarch.arcade.facts.issues.IssueComment;
import edu.usc.softarch.arcade.facts.issues.IssueRecord;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;

public class IssueRecordBuilder {
	//region ATTRIBUTES
	private final DateTimeFormatter formatter;
	public String id;
	public String url;
	public String summary;
	public String description;
	public String type;
	public String priority;
	public String status;
	public String resolution;
	public String created;
	public String resolved;
	public Collection<String> labels;
	public Collection<String> versions;
	public Collection<String> fixVersions;
	public Collection<IssueComment> comments;
	public Collection<Commit> linkedCommits;
	//endregion

	//region CONSTRUCTORS
	public IssueRecordBuilder(String dateFormat) {
		this(DateTimeFormatter.ofPattern(dateFormat)); }

	public IssueRecordBuilder(DateTimeFormatter formatter) {
		this.formatter = formatter;
		this.labels = new ArrayList<>();
		this.versions = new ArrayList<>();
		this.fixVersions = new ArrayList<>();
		this.comments = new ArrayList<>();
	}
	//endregion

	//region PROCESSING
	public IssueRecord build() {
		ZonedDateTime resolvedLocal = null;
		ZonedDateTime createdLocal;

		if (this.resolved != null && !this.resolved.isEmpty()) {
			try {
				resolvedLocal = ZonedDateTime.parse(this.resolved, this.formatter);
			} catch (DateTimeParseException e) {
				resolvedLocal = ZonedDateTime.parse(this.resolved,
					this.formatter.withZone(ZoneId.of("Universal")));
			}
		}

		try {
			createdLocal = ZonedDateTime.parse(this.created, this.formatter);
		} catch (DateTimeParseException e) {
			try {
				createdLocal = ZonedDateTime.parse(this.created,
					this.formatter.withZone(ZoneId.of("Universal")));
			} catch (DateTimeParseException f) {
				createdLocal = ZonedDateTime.parse(this.created,
					DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mmX")
						.withZone(ZoneId.of("Universal")));
			}
		}

		return new IssueRecord(this.id, this.url, this.summary, this.description,
			this.type, this.priority, this.status, this.resolution, createdLocal,
			resolvedLocal, this.labels, this.versions,
			this.fixVersions, this.comments, this.linkedCommits);
	}
	//endregion
}
