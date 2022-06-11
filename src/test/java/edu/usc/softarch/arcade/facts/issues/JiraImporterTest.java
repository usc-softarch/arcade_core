package edu.usc.softarch.arcade.facts.issues;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.usc.softarch.arcade.BaseTest;
import org.junit.jupiter.api.Test;

import java.util.List;

public class JiraImporterTest extends BaseTest {
	private final String resourcesDir =
		resourcesBase + fs + "Facts" + fs + "Issues";

	@Test
	public void getSystemRecordsTest() {
		List<IssueRecord> issues = assertDoesNotThrow(() ->
			JiraImporter.getSystemRecords(resourcesDir));
		assertEquals(5117, issues.size());
	}
}
