package edu.usc.softarch.arcade.facts.issues.handlers;

import edu.usc.softarch.arcade.facts.issues.Commit;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class CommitBuilder {
	//region ATTRIBUTES
	private final DateTimeFormatter formatter;
	public String id;
	public String summary;
	public String description;
	public String status;
	public String url;
	public String sha;
	public String created;
	public String merged;
	public Collection<String> labels;
	public Collection<String> versionTags;
	public Collection<Map.Entry<String, String>> changes;
	//endregion

	//region CONSTRUCTORS
	public CommitBuilder(String dateFormat) {
		this(DateTimeFormatter.ofPattern(dateFormat)); }

	public CommitBuilder(DateTimeFormatter formatter) {
		this.formatter = formatter;
		this.labels = new ArrayList<>();
		this.versionTags = new ArrayList<>();
		this.changes = new ArrayList<>();
	}
	//endregion

	//region PROCESSING
	public Commit build() {
		ZonedDateTime mergedLocal = null;
		ZonedDateTime createdLocal;

		if (this.merged != null && !this.merged.isEmpty()) {
			try {
				mergedLocal = ZonedDateTime.parse(this.merged, this.formatter);
			} catch (DateTimeParseException e) {
				mergedLocal = ZonedDateTime.parse(this.merged,
					this.formatter.withZone(ZoneId.of("Universal")));
			}
		}

		try {
			createdLocal = ZonedDateTime.parse(this.created, this.formatter);
		} catch (DateTimeParseException e) {
			createdLocal = ZonedDateTime.parse(this.created,
				this.formatter.withZone(ZoneId.of("Universal")));
		}

		return new Commit(this.id, this.summary, this.description, this.status,
			this.url, this.sha, createdLocal, mergedLocal,
			this.labels, this.versionTags, this.changes);
	}
	//endregion
}
