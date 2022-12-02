package edu.usc.softarch.arcade;

import edu.usc.softarch.arcade.topics.DocTopics;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;

public abstract class BaseTest {
	protected static final String fs = File.separator;
	protected static final String resourcesBase =
		"." + fs + "src" + fs + "test" + fs + "resources";
	protected static final String outputBase =
		"." + fs + "target" + fs + "test_results";

	/* ------------------------------------------------------------------------ */
	/* -------------------------- DANGER ZONE --------------------------------- */
	/* ------------------------------------------------------------------------ */

	/* DO NOT TOUCH THIS ATTRIBUTE. It will trigger a procedure to re-generate
	 * the oracles of every test case. Unless your name is Marcelo, or you
	 * have been given express permission by me to touch this, it must remain
	 * false at all times. */
	protected final boolean generateOracles = false;

	/* ------------------------------------------------------------------------ */
	/* -------------------------- DANGER ZONE --------------------------------- */
	/* ------------------------------------------------------------------------ */

	@BeforeEach
	public void resetDocTopics() { DocTopics.resetSingleton(); }
}
