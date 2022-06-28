package edu.usc.softarch.arcade.facts.issues.handlers;

import edu.usc.softarch.arcade.facts.issues.IssueComment;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class IssueCommentBuilder {
	//region ATTRIBUTES
	private final DateTimeFormatter formatter;
	public String id;
	public String author;
	public String created;
	public String text;
	//endregion

	//region CONSTRUCTORS
	public IssueCommentBuilder(String dateFormat) {
		this(DateTimeFormatter.ofPattern(dateFormat));	}

	public IssueCommentBuilder(DateTimeFormatter formatter) {
		this.formatter = formatter;
	}
	//endregion

	//region PROCESSING
	public IssueComment build() {
		return new IssueComment(this.id, this.author,
			ZonedDateTime.parse(this.created, this.formatter), this.text);
	}
	//endregion
}
