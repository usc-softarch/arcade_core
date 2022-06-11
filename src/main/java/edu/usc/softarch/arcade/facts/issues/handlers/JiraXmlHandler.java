package edu.usc.softarch.arcade.facts.issues.handlers;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import edu.usc.softarch.arcade.facts.issues.IssueRecord;

import java.util.ArrayList;
import java.util.List;

public class JiraXmlHandler extends DefaultHandler {
	//region ATTRIBUTES
	StringBuilder data;
	IssueRecordBuilder issueBuilder;
	IssueCommentBuilder commentBuilder;
	public final List<IssueRecord> issues;
	//endregion

	//region CONSTRUCTORS
	public JiraXmlHandler() {
		issues = new ArrayList<>();	}
	//endregion

	//region PROCESSING
	@Override
	public void startElement(String uri, String localName,
			String qName, Attributes attributes) throws SAXException {
		this.data = new StringBuilder();

		switch (qName) {
			case "item":
				this.issueBuilder =
					new IssueRecordBuilder("EEE, d MMM yyyy HH:mm:ss Z");
				break;
			case "comment":
				this.commentBuilder =
					new IssueCommentBuilder("EEE, d MMM yyyy HH:mm:ss Z");
				this.commentBuilder.id = attributes.getValue("id");
				this.commentBuilder.author = attributes.getValue("author");
				this.commentBuilder.created = attributes.getValue("created");
				break;
		}
	}

	@Override
	public void endElement(String uri, String localName,
			String qName) throws SAXException {
		switch (qName) {
			case "item":
				this.issues.add(this.issueBuilder.build());
				break;
			case "link":
				this.issueBuilder.url = data.toString();
				break;
			case "summary":
				this.issueBuilder.summary = data.toString();
				break;
			case "description":
				this.issueBuilder.description = data.toString();
				break;
			case "type":
				this.issueBuilder.type = data.toString();
				break;
			case "priority":
				this.issueBuilder.priority = data.toString();
				break;
			case "status":
				this.issueBuilder.status = data.toString();
				break;
			case "resolution":
				this.issueBuilder.resolution = data.toString();
				break;
			case "created":
				this.issueBuilder.created = data.toString();
				break;
			case "resolved":
				this.issueBuilder.resolved = data.toString();
				break;
			case "version":
				this.issueBuilder.versions.add(data.toString());
				break;
			case "fixVersion":
				this.issueBuilder.fixVersions.add(data.toString());
				break;
			case "comment":
				this.commentBuilder.text = data.toString();
				this.issueBuilder.comments.add(this.commentBuilder.build());
				break;
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		data.append(new String(ch, start, length));	}
	//endregion
}
