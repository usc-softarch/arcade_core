package edu.usc.softarch.arcade.antipattern.detection;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.usc.softarch.arcade.BaseTest;
import edu.usc.softarch.arcade.topics.DocTopics;
import edu.usc.softarch.arcade.topics.TopicModelExtractionMethod;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import edu.usc.softarch.arcade.antipattern.SmellCollection;

/**
 * Tests related to the basic smell detection components of ARCADE:
 * BUO (Brick Use Overload), BDC (Brick Dependency Cycle),
 * BCO (Brick Concern Overload) and SPF (Scattered Parasitic Functionality).
 */
public class ArchSmellDetectorTest extends BaseTest {
	private final String resourcesDir = resourcesBase + fs + "ArchSmellDetector";
	private final String factsDir = resourcesBase + fs + "Facts";
	private final String outputDirPath = outputBase + fs
		+ "ArchSmellDetectorTest";
	private final String arcDir = resourcesBase + fs + "ARC";

  /**
   * Tests for structural smell detection algorithms: BDC and BUO.
   * 
	 * @param version Name and version of the subject system
   */
	@ParameterizedTest
	@CsvSource({
		"struts-2.3.30,"
		+ "181,"
		+ "java",

		"struts-2.5.2,"
		+ "163,"
		+ "java",

		"httpd-2.3.8,"
		+ "71,"
		+ "c",

		"httpd-2.4.26,"
		+ "82,"
		+ "c"
	})
	public void runTest(String version, String size, String language) {
		String oraclePath =	resourcesDir + fs + version + "_smells.ser";
		String depsPath = factsDir + fs + version + "_deps.rsf";
		String outputClustersPath = arcDir + fs + version + fs
			+ version + "_js_" + size + "_clusters.rsf";
		String resultSerFilename = outputDirPath + fs + version + "_arc_smells.ser";
		String docTopicsPath = arcDir + fs + version + fs + "base"
			+ fs + "docTopics.json";

		DocTopics docTopics = assertDoesNotThrow(
			() -> DocTopics.deserialize(docTopicsPath));
		ArchSmellDetector asd =
      new ArchSmellDetector(depsPath, outputClustersPath, resultSerFilename,
				language, TopicModelExtractionMethod.MALLET_API, docTopics);
		
		// Call ArchSmellDetector.run() (with runConcern=false)
		SmellCollection resultSmells = assertDoesNotThrow(
      () -> asd.run(true, true, false));

		if (super.generateOracles)
			assertDoesNotThrow(() ->
				resultSmells.serializeSmellCollection(oraclePath));

		// Construct SmellCollection objects out of the result and oracle files
		SmellCollection oracleSmells =
			assertDoesNotThrow(() -> new SmellCollection(oraclePath));

		/* SmellCollection extends HashSet, so we can use equals() to compare the
     * result to the oracle */
		assertEquals(oracleSmells, resultSmells);
	}
}
