package edu.usc.softarch.arcade.facts.issues;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class Commit {
	//region ATTRIBUTES
	public final String id;
	public final String summary;
	public final String description;
	public final String status;
	public final String url;
	public final String sha;
	public final ZonedDateTime created;
	public final ZonedDateTime merged;
	private final Collection<String> labels;
	private final Collection<String> versionTags;
	private final Collection<Map.Entry<String, String>> changes;
	//endregion

	//region CONSTRUCTORS
	public Commit(String id, String summary, String description, String status,
			String url, String sha, ZonedDateTime created, ZonedDateTime merged,
			Collection<String> labels, Collection<String> versionTags,
			Collection<Map.Entry<String, String>> changes) {
		this.id = id;
		this.summary = summary;
		this.description = description;
		this.status = status;
		this.url = url;
		this.sha = sha;
		this.created = created;
		this.merged = merged;
		this.labels = labels;
		this.versionTags = versionTags;
		this.changes = changes;
	}
	//endregion

	//region ACCESSORS
	public Collection<String> getLabels() { return new ArrayList<>(this.labels); }
	public Collection<String> getVersionTags() {
		return new ArrayList<>(this.versionTags); }
	public Collection<Map.Entry<String, String>> getChanges() {
		return new ArrayList<>(this.changes); }
	//endregion
}
