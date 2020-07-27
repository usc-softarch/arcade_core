package org.kohsuke.pdc;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ParseDotClasspathTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testPushPull() {
		String[] pushPullClasspath = {"/home/joshua/workspace/pushpull-0.2/.classpath"};
		
		
		try {
			ParseDotClasspath.main(pushPullClasspath);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test 
	public void testHadoopCore() {
		String[] hadoopClasspath = {"/home/joshua/workspace/hadoop-0.20.2-core/.classpath"};
		try {
			ParseDotClasspath.main(hadoopClasspath);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
