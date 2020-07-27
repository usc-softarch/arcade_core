package edu.usc.softarch.arcade.antipattern.detection;

import static org.junit.Assert.*;
import junit.framework.Assert;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PearsonsCorrelationTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		double[] x = {2,1,0};
		double[] y = {0,1,2};
		
		PearsonsCorrelation pc = new PearsonsCorrelation();
		System.out.println("correlation coefficient: " + pc.correlation(x, y));
		Assert.assertEquals(pc.correlation(x, y),-1.0);
	}

}
