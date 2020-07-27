package edu.usc.softarch.arcade.facts.driver;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RandomTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		Random rand = new Random(1);
		System.out.println( rand.nextInt(100) );
		
		
		System.out.println( rand.nextInt(100) );
	}

}
