package edu.usc.softarch.arcade;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class OracleIntegrityTest extends BaseTest {
	/**
	 * This test stops ARCADE from ever passing CI if someone forgets to turn off
	 * oracle generation.
	 */
	@Test
	public void oracleGenerationIsOffTest() { assertFalse(generateOracles); }
}
