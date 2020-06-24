package edu.usc.softarch.arcade.topics;

import java.io.FileNotFoundException;

import edu.usc.softarch.arcade.topics.TopicKeySet;

import junit.framework.TestCase;

/**
 * @author joshua
 *
 */
public class TopicKeyTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testLoadFromFile() throws FileNotFoundException {
		TopicKeySet tk = new TopicKeySet("/home/joshua/Documents/Software Engineering Research/Subjects/LlamaChat/LlamaChat-topic-keys.txt");
		
	}

}
