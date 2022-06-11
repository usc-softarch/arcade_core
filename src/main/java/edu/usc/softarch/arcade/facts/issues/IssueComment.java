package edu.usc.softarch.arcade.facts.issues;

import java.time.ZonedDateTime;

public class IssueComment {
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
}
