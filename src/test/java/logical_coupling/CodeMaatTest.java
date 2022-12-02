package logical_coupling;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import edu.usc.softarch.arcade.BaseTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class CodeMaatTest extends BaseTest {
	private static final String resourcesDir1 =
		resourcesBase + fs + "CodeMaatTest_resources";
	private static final String resourcesDir2 =
		resourcesBase + fs + "CleanUpCodeMaat_resources";
	private static final String codeMaatPath = "." + fs + "ext-tools" + fs
		+ "code-maat" + fs + "code-maat-1.0-SNAPSHOT-standalone.jar";

	private Map<String, String> read(Reader in) throws IOException {
		Map<String, String> result = new HashMap<>();

		try (BufferedReader br = new BufferedReader(in)) {
			String line = br.readLine(); // Skip header
			while ((line = br.readLine()) != null) {
				String[] values = line.split(",");
				result.put(values[0] + values[1], values[2] + "," + values[3]);
			}
		}

		return result;
	}

	private void assertMapsAreEqual(
			Map<String, String> outputMap, Map<String, String> oracleMap) {
		for (Map.Entry<String, String> entry : oracleMap.entrySet()) {
			String entityPair = entry.getKey();
			String result = outputMap.get(entityPair);
			assertNotNull(result);

			String[] oraclesValues = entry.getValue().split(",");
			int oracleDegree = Integer.parseInt(oraclesValues[0]);
			int oracleAvgRevs = Integer.parseInt(oraclesValues[1]);

			String[] outputValues = result.split(",");
			int outputDegree = Integer.parseInt(outputValues[0]);
			int outputAvgRevs = Integer.parseInt(outputValues[1]);

			assertAll(
				() -> assertEquals(oracleDegree, outputDegree,
					"Output degree does not match the oracle for key "
						+ entityPair),
				() -> assertEquals(oracleAvgRevs, outputAvgRevs,
					"Output average-revs does not match the oracle for key "
						+ entityPair)
			);
		}
	}

	@ParameterizedTest
	@CsvSource({
		"httpd",

		"struts",

		"nutch_17",

		"nutch_18",

		"nutch_19"
	})
	public void codeMaatTest(String subjectSystem) {
		// Creating relevant path Strings
		String sysResources = resourcesDir1 + fs + subjectSystem;
		String logPath = sysResources + fs + "cleaned_" + subjectSystem + ".log";
		String oraclePath = sysResources + fs + subjectSystem + "_oracle.csv";

		// Use ProcessBuilder to create the output project.csv
		ProcessBuilder builder = new ProcessBuilder("java", "-jar",
			codeMaatPath, "-l", logPath, "-c", "git2", "-a", "coupling");
		Process process = assertDoesNotThrow(builder::start);

		Map<String, String> outputMap = assertDoesNotThrow(() ->
			read(new InputStreamReader(process.getInputStream())));
		Map<String, String> oracleMap = assertDoesNotThrow(() ->
			read(new FileReader(oraclePath)));

		assertMapsAreEqual(outputMap, oracleMap);
	}

	@ParameterizedTest
	@CsvSource({
		"struts,"
			+ "",

		"nutch,"
			+ "17",

		"nutch,"
			+ "18",

		"nutch,"
			+ "19"
	})
	public void cleanUpCodeMaatTest(String systemName, String systemVersion) {
		// Creating relevant path Strings
		String sysResources = resourcesDir2 + fs + systemName;
		String fileBase = "project_" + systemName + "_" + systemVersion + "_";
		String oraclePath = sysResources + fs + "oracle_" + fileBase + ".csv";
		String outputCsv = sysResources + fs + fileBase + "clean.csv";

		String[] args = { sysResources };
		cleanUpCodeMaat.main(args);

		Map<String, String> outputMap =
			assertDoesNotThrow(() -> this.read(new FileReader(outputCsv)));
		Map<String, String> oracleMap =
			assertDoesNotThrow(() -> this.read(new FileReader(oraclePath)));

		assertMapsAreEqual(outputMap, oracleMap);
	}
}
