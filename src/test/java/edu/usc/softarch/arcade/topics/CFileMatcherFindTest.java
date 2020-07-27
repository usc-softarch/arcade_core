package edu.usc.softarch.arcade.topics;

import static org.junit.Assert.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CFileMatcherFindTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		Pattern p = Pattern.compile("\\.(c|cpp|cc|s|h|hpp|icc|ia|tbl|p)$");
		Matcher m = p.matcher("first.c");
		assertTrue(m.find());
		
		m = p.matcher("second.jjsjsj");
		assertFalse(m.find());
	}

}
