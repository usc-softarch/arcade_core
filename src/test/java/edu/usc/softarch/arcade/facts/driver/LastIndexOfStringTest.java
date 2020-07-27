package edu.usc.softarch.arcade.facts.driver;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LastIndexOfStringTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		String filename = "/home/joshua/bash_gt.rsf";
		System.out.println( filename.substring(filename.lastIndexOf("."),filename.length() ) );
		System.out.println( filename.substring(0,filename.lastIndexOf(".")) );
	}

}
