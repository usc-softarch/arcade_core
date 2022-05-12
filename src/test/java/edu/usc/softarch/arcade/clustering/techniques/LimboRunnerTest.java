package edu.usc.softarch.arcade.clustering.techniques;

import edu.usc.softarch.arcade.BaseTest;
import edu.usc.softarch.arcade.util.FileUtil;
import edu.usc.softarch.util.EnhancedHashSet;
import edu.usc.softarch.util.EnhancedSet;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class LimboRunnerTest extends BaseTest {
	private final String resourcesDir = resourcesBase + fs + "Limbo";
	private final String factsDir = resourcesBase + fs + "Facts";
	private final String outputDirPath = outputBase + fs + "Limbo";

	@ParameterizedTest
	@CsvSource({
		// struts 2.3.30
		"struts-2.3.30,"
			+ "java,"
			+ "org.apache.struts2",

		// struts 2.5.2
		"struts-2.5.2,"
			+ "java,"
			+ "org.apache.struts2",

		// httpd 2.3.8
		"httpd-2.3.8,"
			+ "c,"
			+ "",

		// httpd 2.4.26
		"httpd-2.4.26,"
			+ "c,"
			+ ""
	})
	public void LimboRecoveryTest(String systemVersion, String lang,
			String packagePrefix) {
		// Creating relevant arguments
		String fVecsPath = factsDir + fs + systemVersion + "_fVectors.json";
		String oraclePath = resourcesDir + fs + systemVersion
			+ "_limbo_clusters.rsf";
		String resultPath = outputDirPath + fs + systemVersion
			+ "_limbo_100_clusters.rsf";

		assertDoesNotThrow(() ->
			LimboRunner.run(fVecsPath, lang, "preselected",
				100, "il", "archsize",
				100, systemVersion + "_limbo",
				outputDirPath, packagePrefix));

		// Load results
		String result = assertDoesNotThrow(() ->
			FileUtil.readFile(resultPath, StandardCharsets.UTF_8));

		// Load oracle
		String oracle = assertDoesNotThrow(() ->
			FileUtil.readFile((oraclePath), StandardCharsets.UTF_8));

		// RsfCompare.equals() to compare contents of oracle and result files
		EnhancedSet<String> uemResultRsf = new EnhancedHashSet<>(
			Arrays.asList(result.split("\\r?\\n")));
		EnhancedSet<String> uemOracleRsf = new EnhancedHashSet<>(
			Arrays.asList(oracle.split("\\r?\\n")));
		assertEquals(uemResultRsf, uemOracleRsf);
	}
}
