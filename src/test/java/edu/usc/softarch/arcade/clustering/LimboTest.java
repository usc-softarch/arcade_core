package edu.usc.softarch.arcade.clustering;

import edu.usc.softarch.arcade.BaseTest;
import edu.usc.softarch.arcade.clustering.simmeasures.SimMeasure;
import edu.usc.softarch.arcade.clustering.criteria.SerializationCriterion;
import edu.usc.softarch.arcade.clustering.criteria.StoppingCriterion;
import edu.usc.softarch.arcade.util.FileUtil;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class LimboTest extends BaseTest {
	private final String resourcesDir = resourcesBase + fs + "Limbo";
	private final String factsDir = resourcesBase + fs + "Facts";
	private final String outputDirPath = outputBase + fs + "Limbo";

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
	public void LimboRecoveryTest(String projectName, String projectVersion,
			String lang, String packagePrefix) {
		// Creating relevant arguments
		String fullProjectName = projectName + "-" + projectVersion;
		String fVecsPath = factsDir + fs + fullProjectName + "_fVectors.json";
		String oraclePath = resourcesDir + fs + fullProjectName
			+ "_clusters.rsf";
		String resultPath = outputDirPath + fs + fullProjectName
			+ "_IL_100_clusters.rsf";

		Architecture arch = assertDoesNotThrow(() ->
			new Architecture(projectName, projectVersion, outputDirPath,
				SimMeasure.SimMeasureType.IL,
				FeatureVectors.deserializeFFVectors(fVecsPath), lang, packagePrefix));

		SerializationCriterion serialCrit =
			SerializationCriterion.makeSerializationCriterion(
				"archsize", 100, arch);

		StoppingCriterion stopCrit = StoppingCriterion.makeStoppingCriterion(
			"preselected", 100);

		assertDoesNotThrow(() ->
			Clusterer.run(ClusteringAlgorithmType.LIMBO, arch, serialCrit, stopCrit,
				SimMeasure.SimMeasureType.IL));

		// Load results
		String result = assertDoesNotThrow(() ->
			FileUtil.readFile(resultPath, StandardCharsets.UTF_8));

		// ------------------------- Generate Oracles ------------------------------

		if (generateOracles) {
			assertDoesNotThrow(() -> {
				Path resultClusterPath = Paths.get(resultPath);
				Path oracleClusterPath = Paths.get(oraclePath);
				Files.copy(resultClusterPath,
					oracleClusterPath, StandardCopyOption.REPLACE_EXISTING);
			});
		}

		// ------------------------- Generate Oracles ------------------------------

		// Load oracle
		String oracle = assertDoesNotThrow(() ->
			FileUtil.readFile((oraclePath), StandardCharsets.UTF_8));

		// RsfCompare.equals() to compare contents of oracle and result files
		Set<String> uemResultRsf = new HashSet<>(
			Arrays.asList(result.split("\\r?\\n")));
		Set<String> uemOracleRsf = new HashSet<>(
			Arrays.asList(oracle.split("\\r?\\n")));
		assertEquals(uemResultRsf, uemOracleRsf);
	}
}
