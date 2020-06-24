package edu.usc.softarch.arcade.config;

import edu.usc.softarch.arcade.config.Config;
import junit.framework.TestCase;

/**
 * @author joshua
 *
 */
public class CurrProjTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testGetConfigFromFile() {
		Config.initConfigFromFile("/home/joshua/workspace/MyExtractors/llamachat.cfg");
		Config.initConfigFromFile("/home/joshua/workspace/MyExtractors/klax.cfg");
		Config.initConfigFromFile("/home/joshua/workspace/MyExtractors/jigsaw.cfg");
		Config.initConfigFromFile("/home/joshua/workspace/MyExtractors/plasma.cfg");
		Config.initConfigFromFile("/home/joshua/workspace/MyExtractors/hadoop-0.20.2-mapred.cfg");
		Config.initConfigFromFile("/home/joshua/workspace/MyExtractors/pushpull-0.2.cfg");
	}
	

}
