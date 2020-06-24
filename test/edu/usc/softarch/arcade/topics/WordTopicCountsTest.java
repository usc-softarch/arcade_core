package edu.usc.softarch.arcade.topics;

import java.io.FileNotFoundException;

import edu.usc.softarch.arcade.topics.WordTopicCounts;

import junit.framework.TestCase;

/**
 * @author joshua
 *
 */
public class WordTopicCountsTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testWordTopicCountsFromFilenameConstructor() throws FileNotFoundException {
		WordTopicCounts wordTopicCounts = new WordTopicCounts("/home/joshua/Documents/Software Engineering Research/Subjects/freecs/freecs-word-topic-counts.txt");
	}

}
