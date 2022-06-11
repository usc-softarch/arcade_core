package edu.usc.softarch.arcade.facts.issues.handlers;

import edu.usc.softarch.arcade.facts.issues.IssueComment;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class IssueCommentBuilder {
	//region ATTRIBUTES
	private final String dateFormat;
	public String id;
	public String author;
	public String created;
	public String text;
	//endregion

	//region CONSTRUCTORS
	public IssueCommentBuilder(String dateFormat) {
		this.dateFormat = dateFormat;	}
	//endregion

	//region PROCESSING
	public IssueComment build() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(this.dateFormat);

		return new IssueComment(this.id, this.author,
			ZonedDateTime.parse(this.created, formatter), this.text);
	}
	//endregion
}
