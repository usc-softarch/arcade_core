package edu.usc.softarch.arcade.clustering.simmeasures;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import edu.usc.softarch.arcade.BaseTest;
import edu.usc.softarch.arcade.clustering.ConcernArchitecture;
import edu.usc.softarch.arcade.clustering.FeatureVectors;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SimilarityMatrixTest extends BaseTest {
	private final String resourcesDir = resourcesBase + fs + "ARC";
	private final String factsDir = resourcesBase + fs + "Facts";
	private final String outputDirPath = outputBase + fs + "ConcernClusteringRunnerTest";

	/**
	 * Tests the SimilarityMatrix initialization.
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
	public void constructorTest(String versionName,	String language,
		String packagePrefix) {
		String artifactsDir = resourcesDir + fs + versionName;
		String oracleSimMatrixPath = artifactsDir + fs + "sim_matrix_oracle.json";
		(new File(outputDirPath)).mkdirs();

		// Deserialize FastFeatureVectors oracle
		String ffVecsFilePath = factsDir + fs + versionName + "_fVectors.json";
		FeatureVectors builderffVecs =
			assertDoesNotThrow(() -> FeatureVectors.deserializeFFVectors(ffVecsFilePath));

		assertNotNull(builderffVecs,
			"failed to deserialize FastFeatureVectors from builder object");

		ConcernArchitecture concernArch = new ConcernArchitecture(versionName,
			outputDirPath, builderffVecs, language,
			artifactsDir + "/base", packagePrefix);

		SimilarityMatrix simMatrix = assertDoesNotThrow(() ->
			new SimilarityMatrix(SimMeasure.SimMeasureType.JS, concernArch) );

		// ------------------------- Generate Oracles ------------------------------

		if (generateOracles) {
			JsonFactory factory = new JsonFactory();
			assertDoesNotThrow(() -> {
				JsonGenerator generator = factory.createGenerator(
					new File(oracleSimMatrixPath), JsonEncoding.UTF8);
				generator.writeStartObject();
				simMatrix.serialize(generator);
				generator.writeEndObject();
				generator.close();
			});
		}

		// ------------------------- Generate Oracles ------------------------------

		SimilarityMatrix oracleSimMatrix = assertDoesNotThrow(() -> {
			JsonFactory factory = new JsonFactory();
			JsonParser parser = factory.createParser(new File(oracleSimMatrixPath));
			parser.nextToken();
			return SimilarityMatrix.deserialize(parser, concernArch);
		});

		assertEquals(oracleSimMatrix, simMatrix);
	}
}
