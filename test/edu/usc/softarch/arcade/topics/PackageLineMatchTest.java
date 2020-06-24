package edu.usc.softarch.arcade.topics;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.usc.softarch.arcade.util.FileUtil;

public class PackageLineMatchTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		List<String> testStrings = new ArrayList<String>();
		testStrings.add("  package org.orgname.projname; ");
		testStrings.add("package org.orgname.projname; ");
		testStrings.add("  package org.orgname.projname;");
		testStrings.add("  package org.orgname.projname ;");
		
		
		for (String testStr : testStrings) {
			assertEquals(FileUtil.findPackageName(testStr),"org.orgname.projname");
		}
		
		String nonMatchingStr = "package it into here";
		assertNull(nonMatchingStr + " should have returned null",FileUtil.findPackageName(nonMatchingStr));

	}

}
