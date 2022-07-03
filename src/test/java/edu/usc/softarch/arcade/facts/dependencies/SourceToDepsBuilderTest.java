package edu.usc.softarch.arcade.facts.dependencies;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import edu.usc.softarch.arcade.BaseTest;
import edu.usc.softarch.arcade.clustering.FeatureVectors;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import edu.usc.softarch.arcade.util.FileUtil;
import edu.usc.softarch.arcade.RsfCompare;

public class SourceToDepsBuilderTest extends BaseTest {
	private final String outputPath = outputBase + fs + "Facts";
	private final String resourcesDir = resourcesBase + fs + "Facts";
	private final String subjectSystemsDir = resourcesBase + fs + "subject_systems";

	/**
	 * Builds the dependencies RSF and Feature Vectors files for subject systems.
	 *
	 * @param systemVersion System version to test with.
	 * @param classesDirName Name of the binaries directory.
	 * @param language Language of the subject system.
	 */
	@ParameterizedTest
	@CsvSource({
		// httpd 2.3.8
		"httpd-2.3.8,"
		+ ","
		+ "c",
		// httpd 2.4.26
		"httpd-2.4.26,"
		+ ","
		+ "c",
		// struts2 (2.3.30)
		"struts-2.3.30,"
		+ "lib_struts,"
		+ "java",
		// struts2 (2.5.2)
		"struts-2.5.2,"
		+ "lib_struts,"
		+ "java"
	})
	public void buildTest(String systemVersion, String classesDirName,
			String language) {
		String classes = subjectSystemsDir + fs + systemVersion;
		if (language.equals("java"))
			classes += fs + classesDirName;
		String deps = outputPath + fs + systemVersion + "_deps.rsf";
		String depsOracle = resourcesDir + fs + systemVersion + "_deps.rsf";
		String fVectors = outputPath + fs + systemVersion + "_fVectors.json";
		String fVectorsOracle = resourcesDir + fs + systemVersion + "_fVectors.json";

		// Run SourceToDepsBuilder.build()
		String finalClasses = classes;
		assertDoesNotThrow(() -> {
			if (language.equals("c"))
				(new CSourceToDepsBuilder()).build(finalClasses, deps, fVectors);
			if (language.equals("java"))
				(new JavaSourceToDepsBuilder()).build(finalClasses, deps, fVectors);
		});
		String result = assertDoesNotThrow(() ->
			FileUtil.readFile(deps, StandardCharsets.UTF_8));

		// ------------------------- Generate Oracles ------------------------------

		if (generateOracles) {
			assertDoesNotThrow(() -> {
				// Deps RSF Oracle
				Path resultPath = Paths.get(deps);
				Path oraclePath = Paths.get(depsOracle);
				Files.copy(resultPath, oraclePath, StandardCopyOption.REPLACE_EXISTING);

				// fVecs Oracle
				resultPath = Paths.get(fVectors);
				oraclePath = Paths.get(fVectorsOracle);
				Files.copy(resultPath, oraclePath, StandardCopyOption.REPLACE_EXISTING);
			});
		}

		// ------------------------- Generate Oracles ------------------------------

		// Load oracle
		String oracleResult = assertDoesNotThrow(() ->
			FileUtil.readFile(depsOracle, StandardCharsets.UTF_8));

		// Use RsfCompare.equals to compare file contents
		RsfCompare resultRsf = new RsfCompare(result);
		RsfCompare oracleRsf = new RsfCompare(oracleResult);
		assertEquals(oracleRsf, resultRsf);

		FeatureVectors resultVectors = assertDoesNotThrow(() ->
			FeatureVectors.deserializeFFVectors(fVectors));
		FeatureVectors oracleVectors = assertDoesNotThrow(() ->
			FeatureVectors.deserializeFFVectors(fVectorsOracle));
		assertEquals(oracleVectors, resultVectors);
	}
}
