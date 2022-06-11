package edu.usc.softarch.arcade.facts.issues;

import edu.usc.softarch.arcade.facts.issues.handlers.JiraXmlHandler;
import edu.usc.softarch.arcade.util.FileUtil;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JiraImporter {
	public static List<IssueRecord> getSystemRecords(String path)
			throws IOException, ParserConfigurationException, SAXException {
		List<IssueRecord> results = new ArrayList<>();
		List<File> issueFiles = FileUtil.getFileListing(path);

		for (File issueFile : issueFiles) {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			parser.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			parser.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
			JiraXmlHandler handler = new JiraXmlHandler();
			parser.parse(issueFile, handler);
			results.addAll(handler.issues);
		}

		return results;
	}
}
