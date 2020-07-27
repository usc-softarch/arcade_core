package edu.usc.softarch.arcade.jira;

import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.thoughtworks.xstream.XStream;

import edu.usc.softarch.arcade.config.Config;
import net.rcarz.jiraclient.BasicCredentials;
import net.rcarz.jiraclient.Issue;
import net.rcarz.jiraclient.Issue.FluentCreate;
import net.rcarz.jiraclient.JiraClient;
import net.rcarz.jiraclient.JiraException;
import net.rcarz.jiraclient.RestClient;


public class JiraClientPrototype {

	static Logger logger = Logger.getLogger(JiraClientPrototype.class);
	private static final String JIRA_URL = "https://issues.apache.org/jira";
	
	public static void main(String[] args) throws Exception {
		PropertyConfigurator.configure(Config.getLoggingConfigFilename());
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		// username and password to access Apache's JIRA repo
		System.out.println("username:");
		String username = br.readLine();
		System.out.println("password:");
		String password = br.readLine();
        BasicCredentials creds = new BasicCredentials(username, password);
        JiraClient jira = new JiraClient(JIRA_URL, creds);
        
        // Directory and prefix of all issues
        String allIssuesPrefix = args[0];

        try {

            /* Search for issues */
        	// There is a 200 issue limit for results of searches to Apache's JIRA repo
        	int maxResults = 200;
        	// jql search string for all HADOOP projects
        	String jqlString = args[1];
            Issue.SearchResult sr = jira.searchIssues(jqlString,"*all",maxResults,0);
            System.out.println("Total: " + sr.total);
            int totalIssues = sr.total;
            
            
            for (int i=0;i<totalIssues;i+=maxResults) {
            	String issuesFilename = allIssuesPrefix + "_startat_" + i + ".ser";
            	//File allIssuesFile = new File(allIssuesFilename);

            	sr = jira.searchIssues(jqlString,"*all",maxResults,i);
                for (Issue issue : sr.issues)
                    logger.debug("Result: " + issue);
                                
                XStream xstream = new XStream();
                xstream.omitField(RestClient.class, "httpClient"); // do not serialize this field
                String allIssuesStr = xstream.toXML(sr.issues);
                
                System.out.println("Writing file " + issuesFilename);
                PrintWriter writer = new PrintWriter(new File(issuesFilename));
                writer.print(allIssuesStr);
                writer.close();
                
                
                System.out.println("Obtained issues starting at: " + i);
            }
            
            
            

        } catch (JiraException ex) {
            System.err.println(ex.getMessage());

            if (ex.getCause() != null)
                System.err.println(ex.getCause().getMessage());
        }

	}

}
