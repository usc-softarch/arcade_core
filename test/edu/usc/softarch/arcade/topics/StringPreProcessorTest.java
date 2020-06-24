package edu.usc.softarch.arcade.topics;

import edu.usc.softarch.arcade.topics.StringPreProcessor;
import junit.framework.TestCase;

/**
 * @author joshua
 *
 */
public class StringPreProcessorTest extends TestCase {

	String testMethodStr = "finalizeUser";
	String testDoubleCamelCaseStr = "upDownAround";
	String testStr1 = "electricalInference";
	String testStr2 = "bloggingTipsForLateWorkers";

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testCamelCaseSeparate() {

		assertEquals("finalize user",
				StringPreProcessor.camelCaseSeparate(testMethodStr));
		assertEquals("up down around",
				StringPreProcessor.camelCaseSeparate(testDoubleCamelCaseStr));
		assertEquals("electrical inference",StringPreProcessor.camelCaseSeparate(testStr1));
		assertEquals("blogging tips for late workers",StringPreProcessor.camelCaseSeparate(testStr2));
	}

	public void testStem() {
		assertEquals("final user",StringPreProcessor.stem(StringPreProcessor.camelCaseSeparate(testMethodStr)));
		assertEquals("up down around",StringPreProcessor.stem(StringPreProcessor.camelCaseSeparate(testDoubleCamelCaseStr)));
		assertEquals("electr infer",StringPreProcessor.stem(StringPreProcessor.camelCaseSeparate(testStr1)));
		assertEquals("blog tip for late worker",StringPreProcessor.stem(StringPreProcessor.camelCaseSeparate(testStr2)));
	}
	
	public void testCamellCaseSeparateAndStem() {
		assertEquals("final user",StringPreProcessor.camelCaseSeparateAndStem(testMethodStr));
		assertEquals("up down around",StringPreProcessor.camelCaseSeparateAndStem(testDoubleCamelCaseStr));
		assertEquals("electr infer",StringPreProcessor.camelCaseSeparateAndStem(StringPreProcessor.camelCaseSeparate(testStr1)));
		assertEquals("blog tip for late worker",StringPreProcessor.camelCaseSeparateAndStem(StringPreProcessor.camelCaseSeparate(testStr2)));
	}

}
