package edu.usc.softarch.arcade.jira;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

import net.rcarz.jiraclient.Issue;

import com.thoughtworks.xstream.XStream;

import edu.usc.softarch.arcade.util.FileUtil;

public class JiraUtil {
	public static List<Issue> deserializeIssues(
			String filename) {
		XStream xstream = new XStream();
		String xml = null;
		try {
			xml = FileUtil.readFile(filename,StandardCharsets.UTF_8);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		List<Issue> issuesList = (List<Issue>)xstream.fromXML(xml);
		return issuesList;
	}
}
