package edu.usc.softarch.arcade;

import edu.usc.softarch.arcade.topics.DocTopics;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class BaseTest {
	protected final String fs = File.separator;
	protected final String resourcesBase =
		"." + fs + "src" + fs + "test" + fs + "resources";
	protected final String outputBase =	"." + fs + "target" + fs + "test_results";

	public BaseTest() { DocTopics.resetSingleton(); }

	/* ------------------------------------------------------------------------ */
	/* -------------------------- DANGER ZONE --------------------------------- */
	/* ------------------------------------------------------------------------ */

	/* DO NOT TOUCH THIS ATTRIBUTE. It will trigger a procedure to re-generate
	 * the oracles of every ARC test case. Unless your name is Marcelo, or you
	 * have been given express permission by me to touch this, it must remain
	 * false at all times. */
	protected final boolean generateOracles = true;

	/* ------------------------------------------------------------------------ */
	/* -------------------------- DANGER ZONE --------------------------------- */
	/* ------------------------------------------------------------------------ */

	/**
	 * This test stops ARCADE from ever passing CI if someone forgets to turn off
	 * oracle generation.
	 */
	@Test
	public void oracleGenerationIsOffTest() {
		assertFalse(generateOracles);	}
}
