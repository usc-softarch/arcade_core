package edu.usc.softarch.arcade.clustering;

import edu.usc.softarch.arcade.BaseTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class ConcernArchitectureTest extends BaseTest {
	private final String resourcesDir = resourcesBase + fs + "ARC";
	private final String factsDir = resourcesBase + fs + "Facts";
	private final String outputDirPath = outputBase + fs + "ConcernClusteringRunnerTest";

	/**
	 * This test verifies that {@link Architecture} and
	 * {@link ConcernArchitecture} are being initialized correctly.
	 *
	 * @param versionName System version.
	 * @param language System language.
	 */
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

		// httpd-2.3.8
		"httpd-2.3.8,"
			+ "c,"
			+ "",

		// httpd-2.4.26
		"httpd-2.4.26,"
			+ "c,"
			+ ""
	})
	public void constructorTest(String versionName, String language,
			String packagePrefix) {
		String artifactsDir = resourcesDir + fs + versionName;
		String initialArchitecturePath = artifactsDir + fs + "initial_architecture.txt";
		String architectureWithDocTopicsPath =
			artifactsDir + fs + "architecture_with_doc_topics.txt";

		// Deserialize FastFeatureVectors oracle
		String ffVecsFilePath = factsDir + fs + versionName + "_fVectors.json";
		FeatureVectors builderffVecs = null;

		try {
			builderffVecs = FeatureVectors.deserializeFFVectors(ffVecsFilePath);
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		if (builderffVecs == null)
			fail("failed to deserialize FastFeatureVectors from builder object");

		ConcernArchitectureMock arch = new ConcernArchitectureMock(versionName,
			outputDirPath, builderffVecs, language, artifactsDir + "/base",
			packagePrefix);

		// ------------------------- Generate Oracles ------------------------------

		if (generateOracles) {
			assertDoesNotThrow(() -> {
				ObjectOutputStream out =
					new ObjectOutputStream(new FileOutputStream(initialArchitecturePath));
				out.writeObject(arch.initialArchitecture);
				out.close();
				out = new ObjectOutputStream(new FileOutputStream(architectureWithDocTopicsPath));
				out.writeObject(arch.architectureWithDocTopics);
				out.close();
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
		Architecture initialArchitectureOracle = assertDoesNotThrow(() -> {
			ObjectInputStream in =
				new ObjectInputStream(new FileInputStream(initialArchitecturePath));
			return (Architecture) in.readObject();
		});
		Architecture architectureWithDocTopicsOracle = assertDoesNotThrow(() -> {
			ObjectInputStream in =
				new ObjectInputStream(new FileInputStream(architectureWithDocTopicsPath));
			return (Architecture) in.readObject();
		});

		assertAll(
			() -> assertEquals(initialArchitectureOracle, arch.initialArchitecture,
				"Initial Architectures did not match."),
			() -> assertEquals(architectureWithDocTopicsOracle, arch.architectureWithDocTopics,
				"DocTopic Architectures did not match.")
		);
	}
}
