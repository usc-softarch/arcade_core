package edu.usc.softarch.arcade.topics;

import java.io.FileNotFoundException;

import edu.usc.softarch.arcade.topics.DocTopics;

import junit.framework.TestCase;

/**
 * @author joshua
 *
 */
public class DocTopicsTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testLoadFromFile() throws FileNotFoundException {
		DocTopics dt = new DocTopics("/home/joshua/Documents/Software Engineering Research/subject_systems/LlamaChat/LlamaChat-doc-topics.txt");
		
	}
}
