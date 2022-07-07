package edu.usc.softarch.arcade.clustering;

import edu.usc.softarch.arcade.BaseTest;
import edu.usc.softarch.arcade.clustering.simmeasures.SimMeasure;
import edu.usc.softarch.arcade.clustering.criteria.SerializationCriterion;
import edu.usc.softarch.arcade.clustering.criteria.StoppingCriterion;
import edu.usc.softarch.arcade.util.FileUtil;
import edu.usc.softarch.util.EnhancedHashSet;
import edu.usc.softarch.util.EnhancedSet;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class WcaTest extends BaseTest {
	private final String resourcesDir = resourcesBase + fs + "WCA";
	private final String factsDir = resourcesBase + fs + "Facts";
	private final String outputDirPath = outputBase + fs + "WCA";

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
	public void WcaRecoveryTest(String systemVersion, String lang,
			String packagePrefix) {
		// Creating relevant arguments
		String fVecsPath = factsDir + fs + systemVersion + "_fVectors.json";
		String oracleFilePathBase = resourcesDir + fs + systemVersion;
		String uemOraclePath = oracleFilePathBase + "_uem_clusters.rsf";
		String uemnmOraclePath = oracleFilePathBase + "_uemnm_clusters.rsf";
		String resultFilePathBase = outputDirPath + fs + systemVersion;
		String uemResultPath = resultFilePathBase + "_uem_100_clusters.rsf";
		String uemnmResultPath = resultFilePathBase + "_uemnm_100_clusters.rsf";

		Architecture archUem = assertDoesNotThrow(() ->
			new Architecture(systemVersion, outputDirPath,
				SimMeasure.SimMeasureType.UEM,
				FeatureVectors.deserializeFFVectors(fVecsPath), lang, packagePrefix));

		Architecture archUemnm = assertDoesNotThrow(() ->
			new Architecture(systemVersion, outputDirPath,
				SimMeasure.SimMeasureType.UEMNM,
				FeatureVectors.deserializeFFVectors(fVecsPath), lang, packagePrefix));

		SerializationCriterion serialCritUem =
			SerializationCriterion.makeSerializationCriterion(
				"archsize", 100, archUem);

		SerializationCriterion serialCritUemnm =
			SerializationCriterion.makeSerializationCriterion(
				"archsize", 100, archUemnm);

		StoppingCriterion stopCrit = StoppingCriterion.makeStoppingCriterion(
			"preselected", 100);

		assertDoesNotThrow(() ->
			Clusterer.run(ClusteringAlgorithmType.WCA, archUem, serialCritUem,
				stopCrit, SimMeasure.SimMeasureType.UEM));

		assertDoesNotThrow(() ->
			Clusterer.run(ClusteringAlgorithmType.WCA, archUemnm, serialCritUemnm,
				stopCrit, SimMeasure.SimMeasureType.UEMNM));

		// Load uem results
		String uemResult = assertDoesNotThrow(() ->
			FileUtil.readFile(uemResultPath, StandardCharsets.UTF_8));

		// ------------------------- Generate Oracles ------------------------------

		if (generateOracles) {
			assertDoesNotThrow(() -> {
				Path resultClusterPath = Paths.get(uemResultPath);
				Path oracleClusterPath = Paths.get(uemOraclePath);
				Files.copy(resultClusterPath,
					oracleClusterPath, StandardCopyOption.REPLACE_EXISTING);
			});
		}

		// ------------------------- Generate Oracles ------------------------------

		// Load uem oracle
		String uemOracle = assertDoesNotThrow(() ->
			FileUtil.readFile((uemOraclePath), StandardCharsets.UTF_8));

		// RsfCompare.equals() to compare contents of oracle and result files
		EnhancedSet<String> uemResultRsf = new EnhancedHashSet<>(
			Arrays.asList(uemResult.split("\\r?\\n")));
		EnhancedSet<String> uemOracleRsf = new EnhancedHashSet<>(
			Arrays.asList(uemOracle.split("\\r?\\n")));
		assertEquals(uemResultRsf, uemOracleRsf);

		// Load uemnm results
		String uemnmResult = assertDoesNotThrow(() ->
			FileUtil.readFile(uemnmResultPath, StandardCharsets.UTF_8));

		// ------------------------- Generate Oracles ------------------------------

		if (generateOracles) {
			assertDoesNotThrow(() -> {
				Path resultClusterPath = Paths.get(uemnmResultPath);
				Path oracleClusterPath = Paths.get(uemnmOraclePath);
				Files.copy(resultClusterPath,
					oracleClusterPath, StandardCopyOption.REPLACE_EXISTING);
			});
		}

		// ------------------------- Generate Oracles ------------------------------

		// Load uemnm oracle
		String uemnmOracle = assertDoesNotThrow(() ->
			FileUtil.readFile((uemnmOraclePath), StandardCharsets.UTF_8));

		// RsfCompare.equals() to compare contents of oracle and result files
		EnhancedSet<String> uemnmResultRsf = new EnhancedHashSet<>(
			Arrays.asList(uemnmResult.split("\\r?\\n")));
		EnhancedSet<String> uemnmOracleRsf = new EnhancedHashSet<>(
			Arrays.asList(uemnmOracle.split("\\r?\\n")));
		assertEquals(uemnmResultRsf, uemnmOracleRsf);
	}
}
