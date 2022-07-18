package edu.usc.softarch.arcade.clustering;

import edu.usc.softarch.arcade.BaseTest;
import edu.usc.softarch.arcade.clustering.simmeasures.SimMeasure;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class ArchitectureTest extends BaseTest {
	private final String resourcesDir = resourcesBase + fs + "ARC";
	private final String factsDir = resourcesBase + fs + "Facts";
	private final String outputDirPath = outputBase + fs + "ConcernClusteringRunnerTest";

	/**
	 * This test verifies that {@link Architecture} is being initialized correctly.
	 *
	 * @param projectName System version.
	 * @param language System language.
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
	public void constructorTest(String projectName, String projectVersion,
			String language, String packagePrefix) {
		String fullProjectName = projectName + "-" + projectVersion;
		String artifactsDir = resourcesDir + fs + fullProjectName;
		String initialArchitecturePath = artifactsDir + fs + "initial_architecture.json";
		String architectureWithDocTopicsPath =
			artifactsDir + fs + "architecture_with_doc_topics.json";

		// Deserialize FastFeatureVectors oracle
		String ffVecsFilePath = factsDir + fs + fullProjectName + "_fVectors.json";
		FeatureVectors builderffVecs = null;

		try {
			builderffVecs = FeatureVectors.deserializeFFVectors(ffVecsFilePath);
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		if (builderffVecs == null)
			fail("failed to deserialize FastFeatureVectors from builder object");

		FeatureVectors finalBuilderffVecs = builderffVecs;
		ArchitectureMock arch = assertDoesNotThrow(() ->
			new ArchitectureMock(projectName, projectVersion, outputDirPath,
				SimMeasure.SimMeasureType.IL, finalBuilderffVecs, language,
				artifactsDir + "/base", packagePrefix));

		// ------------------------- Generate Oracles ------------------------------

		if (generateOracles) {
			assertDoesNotThrow(() -> {
				arch.initialArchitecture.serialize(initialArchitecturePath);
				arch.architectureWithDocTopics.serialize(architectureWithDocTopicsPath);
			});
		}

		// ------------------------- Generate Oracles ------------------------------

		System.out.println("fastClusters size before: " + arch.initialArchitecture.size());
		System.out.println("fastClusters size after: " + arch.architectureWithDocTopics.size());

		// every node should get a cluster, so there should be at least one cluster
		assertFalse(arch.initialArchitecture.isEmpty());
		assertAll(
			() -> assertFalse(arch.architectureWithDocTopics.isEmpty(),
				"fastClusters empty after initializeDocTopicsForEachFastCluster"),
			() -> assertNotEquals(arch.architectureWithDocTopics, arch.initialArchitecture)
		);

		// check the integrity of both data structures, before and after docTopics
		Architecture initialArchitectureOracle = assertDoesNotThrow(
			() -> Architecture.deserialize(initialArchitecturePath));
		Architecture architectureWithDocTopicsOracle = assertDoesNotThrow(
			() -> Architecture.deserialize(architectureWithDocTopicsPath));

		assertAll(
			() -> assertEquals(initialArchitectureOracle, arch.initialArchitecture,
				"Initial Architectures did not match."),
			() -> assertEquals(architectureWithDocTopicsOracle, arch.architectureWithDocTopics,
				"DocTopic Architectures did not match.")
		);
	}
}
