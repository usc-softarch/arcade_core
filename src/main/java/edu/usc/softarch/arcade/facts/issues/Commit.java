package edu.usc.softarch.arcade.facts.issues;

import edu.usc.softarch.arcade.facts.issues.handlers.CommitBuilder;
import edu.usc.softarch.arcade.util.json.EnhancedJsonGenerator;
import edu.usc.softarch.arcade.util.json.EnhancedJsonParser;
import edu.usc.softarch.arcade.util.json.JsonSerializable;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Commit implements JsonSerializable {
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

	//region SERIALIZATION

	@Override
	public void serialize(EnhancedJsonGenerator generator) throws IOException {
		generator.writeField("id", id);
		generator.writeField("summary", summary);
		generator.writeField("description", description);
		generator.writeField("status", status);
		generator.writeField("url", url);
		generator.writeField("sha", sha);
		generator.writeField("created",
			created.toString().replace("[Universal]", ""));
		if (this.merged != null)
			generator.writeField("merged",
				merged.toString().replace("[Universal]", ""));
		else
			generator.writeField("merged", "");
		generator.writeField("labels", labels);
		generator.writeField("versionTags", versionTags);

		Map<String, String> changesMap = new HashMap<>();
		for (Map.Entry<String, String> change : changes)
			changesMap.put(change.getKey(), change.getValue());

		generator.writeField("changes", changesMap, true,
			"before", "after");
	}

	public static Commit deserialize(EnhancedJsonParser parser)
			throws IOException {
		CommitBuilder builder = new CommitBuilder(DateTimeFormatter.ISO_INSTANT);

		builder.id = parser.parseString();
		builder.summary = parser.parseString();
		builder.description = parser.parseString();
		builder.status = parser.parseString();
		builder.url = parser.parseString();
		builder.sha = parser.parseString();
		builder.created = parser.parseString();
		builder.merged = parser.parseString();
		builder.labels = parser.parseCollection(String.class);
		builder.versionTags = parser.parseCollection(String.class);

		Map<String, String> changesMap = parser.parseMap(String.class, String.class);
		builder.changes.addAll(changesMap.entrySet());

		return builder.build();
	}
	//endregion
}
