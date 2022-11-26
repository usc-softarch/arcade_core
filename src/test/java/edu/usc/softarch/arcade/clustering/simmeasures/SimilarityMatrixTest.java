package edu.usc.softarch.arcade.clustering.simmeasures;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import edu.usc.softarch.arcade.BaseTest;
import edu.usc.softarch.arcade.clustering.data.Architecture;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SimilarityMatrixTest extends BaseTest {
	private final String resourcesDir = resourcesBase + fs + "ARC";
	private final String factsDir = resourcesBase + fs + "Facts";
	private final String outputDirPath = outputBase + fs + "ConcernClusteringRunnerTest";

	/**
	 * Tests the SimilarityMatrix initialization.
	 *
	 * @param projectName System version.
	 * @param language System language.
	 */
	@ParameterizedTest
	@CsvSource({
		// struts 2.3.30
		"struts,"
			+ "2.3.30,"
			+ "java,"
			+ "org.apache.struts2",

		// struts 2.5.2
		"struts,"
			+ "2.5.2,"
			+ "java,"
			+ "org.apache.struts2",

		// httpd-2.3.8
		"httpd,"
			+ "2.3.8,"
			+ "c,"
			+ "",

		// httpd-2.4.26
		"httpd,"
			+ "2.4.26,"
			+ "c,"
			+ ""
	})
	public void constructorTest(String projectName, String projectVersion,
			String language, String packagePrefix) {
		String fullProjectName = projectName + "-" + projectVersion;
		String artifactsDir = resourcesDir + fs + fullProjectName;
		String oracleSimMatrixPath = artifactsDir + fs + "sim_matrix_oracle.json";
		(new File(outputDirPath)).mkdirs();

		String depsPath = factsDir + fs + fullProjectName + "_deps.rsf";

		Architecture concernArch = assertDoesNotThrow(() ->
			new Architecture(projectName, projectVersion, outputDirPath,
				SimMeasure.SimMeasureType.JS, depsPath,
				language, artifactsDir + "/base", packagePrefix));

		SimilarityMatrix simMatrix = assertDoesNotThrow(() ->
			new SimilarityMatrix(SimMeasure.SimMeasureType.JS, concernArch));

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
