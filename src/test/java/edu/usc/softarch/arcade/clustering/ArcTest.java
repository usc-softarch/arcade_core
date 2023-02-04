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
import edu.usc.softarch.arcade.clustering.data.Architecture;
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
	 * @param systemName System version.
	 * @param lang System language.
	 * @param packagePrefix Package prefix to consider in clustering,
	 *                      only for Java.
	 */
	@ParameterizedTest
	@CsvSource({
		"struts,"
			+ "2.3.30,"
			+ "java,"
			+ "org.apache.struts2",

		"struts,"
			+ "2.5.2,"
			+ "java,"
			+ "org.apache.struts2",

		"httpd,"
			+ "2.3.8,"
			+ "c,"
			+ "",

		"httpd,"
			+ "2.4.26,"
			+ "c,"
			+ ""
	})
	public void ARCRecoveryTest(String systemName, String systemVersion,
			String lang, String packagePrefix) {
		// Creating relevant path Strings
		String systemFullName = systemName + "-" + systemVersion;
		String sysResources = resourcesDir + fs + systemFullName;
		String artifactsDir = sysResources + fs + "base";
		String arcFileBase = systemFullName + "_JS_100_";
		String oracleClustersPath =
			sysResources + fs + arcFileBase + "clusters.rsf";
		String oracleConcernsPath =
			sysResources + fs + arcFileBase + "concerns.txt";
		String depsPath = factsDir + fs + systemFullName + "_deps.rsf";
		String resultClustersFile =
			outputDirPath + fs + arcFileBase + "clusters.rsf";
		String resultConcernsFile =
			outputDirPath + fs + arcFileBase + "concerns.txt";

		boolean fileLevel = !lang.equals("java");
		Architecture arch = assertDoesNotThrow(() ->
			new Architecture(systemName, systemVersion, outputDirPath,
				SimMeasure.SimMeasureType.JS, depsPath, lang,
				artifactsDir, packagePrefix, false, fileLevel));

		SerializationCriterion serialCrit =
			SerializationCriterion.makeSerializationCriterion(
			"archsize", 100, arch);

		StoppingCriterion stopCrit = StoppingCriterion.makeStoppingCriterion(
				"preselected", 100, arch);

		assertDoesNotThrow(() ->
			Clusterer.run(ClusteringAlgorithmType.ARC, arch, serialCrit, stopCrit,
				SimMeasure.SimMeasureType.JS, false));

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
			assertDoesNotThrow(() -> {
				Path resultPath = Paths.get(resultConcernsFile);
				Path oraclePath = Paths.get(oracleConcernsPath);
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
		assertEquals(concernsOracle.replaceAll("\\r\\n", "\\n"),
			concerns.replaceAll("\\r\\n", "\\n"));

		//TODO verify that all entities in the dependencies appear in the results
	}
}
