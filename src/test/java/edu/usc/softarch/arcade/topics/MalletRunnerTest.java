package edu.usc.softarch.arcade.topics;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Set;

import edu.usc.softarch.arcade.BaseTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;

public class MalletRunnerTest extends BaseTest {
	private final String subjectSystemsDir =
		resourcesBase + fs + "subject_systems";
	private final String arcDir = resourcesBase + fs + "ARC";
	private final String outputDirPath = outputBase + fs + "MalletRunner";

	@ParameterizedTest
	@CsvSource({
		"struts-2.3.30,"
			+ "java",

		"struts-2.5.2,"
			+ "java",

		"httpd-2.3.8,"
			+ "c",

		"httpd-2.4.26,"
			+ "c",
	})
	public void pipeExtractorTest(String version, String language) {
		String srcDir = subjectSystemsDir + fs + version;
		String oraclePath =
			arcDir + fs + version + fs + "base" + fs + version + "_output.pipe";
		String stopWordsDir = "src" + fs + "main" + fs + "resources" + fs + "res";

		(new File(outputDirPath)).mkdirs();

		// malletPath argument is irrelevant since PipeExtractor does not use it
		MalletRunner runner = new MalletRunner(srcDir, language, "",
			outputDirPath, stopWordsDir);
		assertDoesNotThrow(runner::copySource);
		assertDoesNotThrow(runner::runPipeExtractor);
		runner.cleanUp();

		// ------------------------- Generate Oracles ------------------------------

		if (super.generateOracles) {
			assertDoesNotThrow(() -> {
				Path result = Paths.get(outputDirPath + fs + version + "_output.pipe");
				Path oracle = Paths.get(oraclePath);
				Files.copy(result, oracle, StandardCopyOption.REPLACE_EXISTING);
			});
		}

		// ------------------------- Generate Oracles ------------------------------

		// Read result instances into a set
		InstanceList resultInstances = InstanceList.load(
			new File(outputDirPath + File.separatorChar + "output.pipe"));
		Set<InstanceComparator> result = new HashSet<>();
		for (Instance i : resultInstances)
			result.add(new InstanceComparator(i));
		
		// Read oracle instances into a set
		InstanceList oracleInstances = InstanceList.load(new File(oraclePath));
		Set<InstanceComparator> oracle = new HashSet<>();
		for (Instance i : oracleInstances)
			oracle.add(new InstanceComparator(i));

		// Compare sets of instances
		assertEquals(oracle.size(), result.size()); // passes - same size
		assertEquals(oracle, result, "Failed argument is: " + srcDir); // fails - comparing sets of instances doesn't work?
	}
}
