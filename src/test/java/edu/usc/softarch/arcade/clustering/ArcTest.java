package edu.usc.softarch.arcade.clustering;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

import edu.usc.softarch.arcade.BaseTest;
import edu.usc.softarch.arcade.clustering.simmeasures.SimMeasure;
import edu.usc.softarch.arcade.clustering.criteria.SerializationCriterion;
import edu.usc.softarch.arcade.clustering.criteria.StoppingCriterion;
import edu.usc.softarch.arcade.util.FileUtil;
import edu.usc.softarch.util.EnhancedHashSet;
import edu.usc.softarch.util.EnhancedSet;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class ArcTest extends BaseTest {
	private final String resourcesDir = resourcesBase + fs + "ARC";
	private final String factsDir = resourcesBase + fs + "Facts";
	private final String outputDirPath = outputBase + fs + "ARC";

	/**
	 * Tests ARC recovery for a single version of a system.
	 *
	 * @param systemVersion System version.
	 * @param lang System language.
	 * @param arcFileSuffix Expected clusters file suffix.
	 */
	@ParameterizedTest
	@CsvSource({
		// struts 2.3.30
		"struts-2.3.30,"
			+ "java,"
			+ "_JS_182_,"
			+ "org.apache.struts2",

		// struts 2.5.2
		"struts-2.5.2,"
			+ "java,"
			+ "_JS_164_,"
			+ "org.apache.struts2",

		// httpd 2.3.8
		"httpd-2.3.8,"
			+ "c,"
			+ "_JS_71_,"
			+ "",

		// httpd 2.4.26
		"httpd-2.4.26,"
			+ "c,"
			+ "_JS_82_,"
			+ ""
	})
	public void ARCRecoveryTest(String systemVersion, String lang,
			String arcFileSuffix, String packagePrefix) {
		// Creating relevant path Strings
		String sysResources = resourcesDir + fs + systemVersion;
		String artifactsDir = sysResources + fs + "base";
		String arcFileBase = systemVersion + arcFileSuffix;
		String oracleClustersPath =
			sysResources + fs + arcFileBase + "clusters.rsf";
		String oracleConcernsPath =
			sysResources + fs + arcFileBase + "concerns.txt";
		String ffVecs = factsDir + fs + systemVersion + "_fVectors.json";
		String resultClustersFile =
			outputDirPath + fs + arcFileBase + "clusters.rsf";
		String resultConcernsFile =
			outputDirPath + fs + arcFileBase + "concerns.txt";

		Architecture arch = assertDoesNotThrow(() ->
			new Architecture(systemVersion, outputDirPath,
				SimMeasure.SimMeasureType.JS,
				FeatureVectors.deserializeFFVectors(ffVecs), lang,
				artifactsDir, packagePrefix));

		SerializationCriterion serialCrit =
			SerializationCriterion.makeSerializationCriterion(
			"archsizefraction", 0.2, arch);

		StoppingCriterion stopCrit = StoppingCriterion.makeStoppingCriterion(
				"archsizefraction", 0.2, arch);

		assertDoesNotThrow(() ->
			Clusterer.run(ClusteringAlgorithmType.ARC, arch, serialCrit, stopCrit, lang,
				SimMeasure.SimMeasureType.JS));

		/* The expectation here is that this resulting clusters file has the same
		 * name as the oracle clusters file, meaning it has the same number of
		 * clusters and topics. */
		assertTrue(new File(resultClustersFile).exists(),
			"resulting clusters file name does not match oracle clusters file name:"
				+ arcFileBase + "clusters.rsf");
		assertTrue(new File(resultConcernsFile).exists(),
			"resulting concerns file name does not match oracle concerns file name:"
				+ arcFileBase + "concerns.rsf");

		String result = assertDoesNotThrow(() ->
			FileUtil.readFile(resultClustersFile, StandardCharsets.UTF_8));
		String concerns = assertDoesNotThrow(() ->
			FileUtil.readFile(resultConcernsFile, StandardCharsets.UTF_8));

		// ------------------------- Generate Oracles ------------------------------

		if (generateOracles) {
			assertDoesNotThrow(() -> {
				Path resultPath = Paths.get(resultClustersFile);
				Path oraclePath = Paths.get(oracleClustersPath);
				Files.copy(resultPath, oraclePath, StandardCopyOption.REPLACE_EXISTING);
			});
		}

		// ------------------------- Generate Oracles ------------------------------

		// Load oracle
		String oracle = assertDoesNotThrow(() ->
			FileUtil.readFile((oracleClustersPath), StandardCharsets.UTF_8));
		String concernsOracle = assertDoesNotThrow(() ->
			FileUtil.readFile((oracleConcernsPath), StandardCharsets.UTF_8));

		// RsfCompare.equals() to compare contents of oracle and result files
		EnhancedSet<String> resultRsf = new EnhancedHashSet<>(
			Arrays.asList(result.split("\\r?\\n")));
		EnhancedSet<String> oracleRsf = new EnhancedHashSet<>(
			Arrays.asList(oracle.split("\\r?\\n")));
		assertEquals(oracleRsf, resultRsf);
		assertEquals(concernsOracle, concerns);
	}
}
