package edu.usc.softarch.arcade.metrics;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SystemEvoTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test1() {
		String[] args = {"testdata/mojo/2s_1b_1.rsf","testdata/mojo/2s_1b_2.rsf"};
		SystemEvo.main(args);
		assertEquals(80,SystemEvo.sysEvo,0);
	}
	
	@Test
	public void test2() {
		String[] args = {"testdata/mojo/a2.rsf","testdata/mojo/b2.rsf"};
		SystemEvo.main(args);
		assertEquals(71,SystemEvo.sysEvo,1);
	}
	
	@Test
	public void test3() {
		String[] args = {"testdata/mojo/c8.rsf","testdata/mojo/b2.rsf"};
		SystemEvo.main(args);
		assertEquals(84,SystemEvo.sysEvo,1);
	}
	
	@Test
	public void testForZero() {
		String[] args = {"testdata/mojo/c1.rsf","testdata/mojo/c2.rsf"};
		SystemEvo.main(args);
		assertEquals(0,SystemEvo.sysEvo,0);
		
		String[] args2 = {"testdata/mojo/c1.rsf","testdata/mojo/c3.rsf"};
		SystemEvo.main(args2);
		assertEquals(0,SystemEvo.sysEvo,0);
	}
	
	@Test
	public void test4() {
		String[] args = {"testdata/mojo/c2.rsf","testdata/mojo/c3.rsf"};
		SystemEvo.main(args);
		assertEquals(46,SystemEvo.sysEvo,1);
	}
	
	@Test
	public void test5() {
		String[] args = {"testdata/mojo/c4.rsf","testdata/mojo/c3.rsf"};
		SystemEvo.main(args);
		assertEquals(66,SystemEvo.sysEvo,1);
	}
	
	@Test
	public void test6() {
		String[] args = {"testdata/mojo/c5.rsf","testdata/mojo/c3.rsf"};
		SystemEvo.main(args);
		assertEquals(89,SystemEvo.sysEvo,1);
	}
	
	@Test
	public void test7() {
		String[] args = {"testdata/mojo/c4.rsf","testdata/mojo/c5.rsf"};
		SystemEvo.main(args);
		assertEquals(77,SystemEvo.sysEvo,1);
	}
	
	@Test
	public void test8() {
		String[] args = {"testdata/mojo/c6.rsf","testdata/mojo/c3.rsf"};
		SystemEvo.main(args);
		assertEquals(80,SystemEvo.sysEvo,1);
	}
	
	@Test
	public void test9() {
		String[] args = {"testdata/mojo/c7.rsf","testdata/mojo/c3.rsf"};
		SystemEvo.main(args);
		assertEquals(91,SystemEvo.sysEvo,1);
	}
	
	@Test
	public void test10() {
		String[] args = {"testdata/mojo/c7.rsf","testdata/mojo/c6.rsf"};
		SystemEvo.main(args);
		assertEquals(91,SystemEvo.sysEvo,1);
	}
	
	@Test
	public void test11() {
		String[] args = {"testdata/mojo/c6.rsf","testdata/mojo/c7.rsf"};
		SystemEvo.main(args);
		assertEquals(91,SystemEvo.sysEvo,1);
	}

}
