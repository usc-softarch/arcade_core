package edu.usc.softarch.arcade.jira;

import java.util.ArrayList;
import java.util.List;

import java.io.File;
import net.rcarz.jiraclient.BasicCredentials;
import net.rcarz.jiraclient.CustomFieldOption;
import net.rcarz.jiraclient.Field;
import net.rcarz.jiraclient.Issue;
import net.rcarz.jiraclient.JiraClient;
import net.rcarz.jiraclient.JiraException;


public class JiraClientExample {

	private static final String JIRA_URL = "https://issues.apache.org/jira";
	
	public static void main(String[] args) throws Exception {

        BasicCredentials creds = new BasicCredentials("batman", "pow! pow!");
        JiraClient jira = new JiraClient("https://jira.example.com/jira", creds);

        try {
            /* Retrieve issue TEST-123 from JIRA. We'll get an exception if this fails. */
            final Issue issue = jira.getIssue("TEST-123");

            /* Print the issue key. */
            System.out.println(issue);

            /* You can also do it like this: */
            System.out.println(issue.getKey());

            /* Vote for the issue. */
            issue.vote();

            /* And also watch it. Add Robin too. */
            issue.addWatcher(jira.getSelf());
            issue.addWatcher("robin");

            /* Open the issue and assign it to batman. */
            issue.transition()
                .field(Field.ASSIGNEE, "batman")
                .execute("Open");

            /* Add two comments, with one limited to the developer role. */
            issue.addComment("No problem. We'll get right on it!");
            issue.addComment("He tried to send a whole Internet!", "role", "Developers");

            /* Print the reporter's username and then the display name */
            System.out.println("Reporter: " + issue.getReporter());
            System.out.println("Reporter's Name: " + issue.getReporter().getDisplayName());

            /* Print existing labels (if any). */
            for (String l : issue.getLabels())
                System.out.println("Label: " + l);

            /* Change the summary and add two labels to the issue. The double-brace initialiser
               isn't required, but it helps with readability. */
            issue.update()
                .field(Field.SUMMARY, "tubes are clogged")
                .field(Field.LABELS, new ArrayList() {{
                    addAll(issue.getLabels());
                    add("foo");
                    add("bar");
                }})
                .field(Field.PRIORITY, Field.valueById("1")) /* you can also set the value by ID */
                .execute();

            /* You can also update values with field operations. */
            issue.update()
                .fieldAdd(Field.LABELS, "baz")
                .fieldRemove(Field.LABELS, "foo")
                .execute();

            /* Print the summary. We have to refresh first to pickup the new value. */
            issue.refresh();
            System.out.println("New Summary: " + issue.getSummary());

            /* Now let's start progress on this issue. */
            issue.transition().execute("Start Progress");

            /* Pretend customfield_1234 is a text field. Get the raw field value... */
            Object cfvalue = issue.getField("customfield_1234");

            /* ... Convert it to a string and then print the value. */
            String cfstring = Field.getString(cfvalue);
            System.out.println(cfstring);

            /* And finally, change the value. */
            issue.update()
                .field("customfield_1234", "new value!")
                .execute();

            /* Pretend customfield_5678 is a multi-select box. Print out the selected values. */
            List<CustomFieldOption> cfselect = Field.getResourceArray(
                CustomFieldOption.class,
                issue.getField("customfield_5678"),
                jira.getRestClient()
            );
            for (CustomFieldOption cfo : cfselect)
                System.out.println("Custom Field Select: " + cfo.getValue());

            /* Print out allowed values for the custom multi-select box. */
            List<CustomFieldOption> allowedValues = jira.getCustomFieldAllowedValues("customfield_5678", "TEST", "Task");
            for (CustomFieldOption customFieldOption : allowedValues)
                System.out.println(customFieldOption.getValue());

            /* Set two new values for customfield_5678. */
            issue.update()
                .field("customfield_5678", new ArrayList() {{
                    add("foo");
                    add("bar");
                    add(Field.valueById("1234")); /* you can also update using the value ID */
                }})
                .execute();

            /* Add an attachment */
            File file = new File("C:\\Users\\John\\Desktop\\screenshot.jpg");
            issue.addAttachment(file);

            /* And finally let's resolve it as incomplete. */
            issue.transition()
                .field(Field.RESOLUTION, "Incomplete")
                .execute("Resolve Issue");

            /* Create a new issue. */
            Issue newIssue = jira.createIssue("TEST", "Bug")
                .field(Field.SUMMARY, "Bat signal is broken")
                .field(Field.DESCRIPTION, "Commissioner Gordon reports the Bat signal is broken.")
                .field(Field.REPORTER, "batman")
                .field(Field.ASSIGNEE, "robin")
                .execute();
            System.out.println(newIssue);

            /* Link to the old issue */
            newIssue.link("TEST-123", "Dependency");

            /* Create sub-task */
            Issue subtask = newIssue.createSubtask()
                .field(Field.SUMMARY, "replace lightbulb")
                .execute();

            /* Search for issues */
            Issue.SearchResult sr = jira.searchIssues("assignee=batman");
            System.out.println("Total: " + sr.total);
            for (Issue i : sr.issues)
                System.out.println("Result: " + i);

        } catch (JiraException ex) {
            System.err.println(ex.getMessage());

            if (ex.getCause() != null)
                System.err.println(ex.getCause().getMessage());
        }

	}

}
