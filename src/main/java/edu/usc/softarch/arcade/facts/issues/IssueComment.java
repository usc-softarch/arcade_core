package edu.usc.softarch.arcade.facts.issues;

import edu.usc.softarch.arcade.facts.issues.handlers.IssueCommentBuilder;
import edu.usc.softarch.util.json.EnhancedJsonGenerator;
import edu.usc.softarch.util.json.EnhancedJsonParser;
import edu.usc.softarch.util.json.JsonSerializable;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class IssueComment implements JsonSerializable {
	//region ATTRIBUTES
	public final String id;
	public final String author;
	public final ZonedDateTime created;
	public final String text;
	//endregion

	//region CONSTRUCTORS
	public IssueComment(String id, String author,
			ZonedDateTime created, String text) {
		this.id = id;
		this.author = author;
		this.created = created;
		this.text = text;
	}
	//endregion

	//region SERIALIZATION
	@Override
	public void serialize(EnhancedJsonGenerator generator) throws IOException {
		generator.writeField("id", id);
		generator.writeField("author", author);
		generator.writeField("created",
			created.toString().replace("[Universal]", ""));
		generator.writeField("text", text);
	}

	public static IssueComment deserialize(EnhancedJsonParser parser)
			throws IOException {
		IssueCommentBuilder commentBuilder =
			new IssueCommentBuilder(DateTimeFormatter.ISO_INSTANT);

		commentBuilder.id = parser.parseString();
		commentBuilder.author = parser.parseString();
		commentBuilder.created = parser.parseString();
		commentBuilder.text = parser.parseString();

		return commentBuilder.build();
	}
	//endregion
}
