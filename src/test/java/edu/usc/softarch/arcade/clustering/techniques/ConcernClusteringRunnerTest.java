package edu.usc.softarch.arcade.clustering.techniques;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import edu.usc.softarch.arcade.BaseTest;
import edu.usc.softarch.arcade.antipattern.SmellCollection;
import edu.usc.softarch.arcade.antipattern.detection.ArchSmellDetector;
import edu.usc.softarch.arcade.clustering.ConcernArchitecture;
import edu.usc.softarch.arcade.clustering.FeatureVectors;
import edu.usc.softarch.arcade.clustering.criteria.ArchSizeFractionStoppingCriterion;
import edu.usc.softarch.arcade.clustering.simmeasures.SimMeasure;
import edu.usc.softarch.arcade.clustering.simmeasures.SimilarityMatrix;
import edu.usc.softarch.arcade.clustering.criteria.SerializationCriterion;
import edu.usc.softarch.arcade.clustering.criteria.StoppingCriterion;
import edu.usc.softarch.arcade.topics.DocTopics;
import edu.usc.softarch.arcade.topics.TopicModelExtractionMethod;
import edu.usc.softarch.arcade.topics.TopicUtil;
import edu.usc.softarch.arcade.util.FileUtil;
import edu.usc.softarch.util.EnhancedHashSet;
import edu.usc.softarch.util.EnhancedSet;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import edu.usc.softarch.arcade.clustering.Architecture;

public class ConcernClusteringRunnerTest extends BaseTest {
	private final String resourcesDir = resourcesBase + fs + "ARC";
	private final String factsDir = resourcesBase + fs + "Facts";
	private final String outputDirPath = outputBase + fs + "ConcernClusteringRunnerTest";

	/**
	 * Tests ARC recovery for a single version of a system.
	 *
	 * @param systemVersion System version.
	 * @param lang System language.
	 * @param arcFileSuffix Expected clusters file suffix.
	 */
	@ParameterizedTest
	@CsvSource({
		// struts 2.3.30
		"struts-2.3.30,"
			+ "java,"
			+ "_181_clusters.rsf,"
			+ "org.apache.struts2",

		// struts 2.5.2
		"struts-2.5.2,"
			+ "java,"
			+ "_163_clusters.rsf,"
			+ "org.apache.struts2",

		// httpd 2.3.8
		"httpd-2.3.8,"
			+ "c,"
			+ "_71_clusters.rsf,"
			+ "",

		// httpd 2.4.26
		"httpd-2.4.26,"
			+ "c,"
			+ "_82_clusters.rsf,"
			+ ""
	})
	public void ARCRecoveryTest(String systemVersion, String lang,
			String arcFileSuffix, String packagePrefix) {
		// Creating relevant path Strings
		String sysResources = resourcesDir + fs + systemVersion;
		String artifactsDir = sysResources + fs + "base";
		String oracleFilePath = sysResources + fs + systemVersion + arcFileSuffix;
		String ffVecs = factsDir + fs + systemVersion + "_fVectors.json";
		String resultClustersFile = outputDirPath + fs + systemVersion + arcFileSuffix;

		ConcernArchitecture arch = assertDoesNotThrow(() ->
			new ConcernArchitecture(systemVersion, outputDirPath,
			FeatureVectors.deserializeFFVectors(ffVecs),
			lang,	artifactsDir, packagePrefix));

		SerializationCriterion serialCrit =
			SerializationCriterion.makeSerializationCriterion(
			"archsizefraction", 0.2, arch);

		StoppingCriterion stopCrit = StoppingCriterion.makeStoppingCriterion(
				"archsizefraction", 0.2, arch);

		assertDoesNotThrow(() ->
			ConcernClusteringRunnerMock.run(arch, serialCrit, stopCrit, lang,
				"archsizefraction",	SimMeasure.SimMeasureType.JS));

		/* The expectation here is that this resulting clusters file has the same
		 * name as the oracle clusters file, meaning it has the same number of
		 * clusters and topics. */
		assertTrue(new File(resultClustersFile).exists(),
			"resulting clusters file name does not match oracle clusters file name:"
				+ resultClustersFile);

		String result = assertDoesNotThrow(() ->
			FileUtil.readFile(resultClustersFile, StandardCharsets.UTF_8));

		// ------------------------- Generate Oracles ------------------------------

		if (generateOracles) {
			assertDoesNotThrow(() -> {
				Path resultPath = Paths.get(resultClustersFile);
				Path oraclePath = Paths.get(oracleFilePath);
				Files.copy(resultPath, oraclePath, StandardCopyOption.REPLACE_EXISTING);
			});
		}

		// ------------------------- Generate Oracles ------------------------------

		// Load oracle
		String oracle = assertDoesNotThrow(() ->
			FileUtil.readFile((oracleFilePath), StandardCharsets.UTF_8));

		// RsfCompare.equals() to compare contents of oracle and result files
		EnhancedSet<String> resultRsf = new EnhancedHashSet<>(
			Arrays.asList(result.split("\\r?\\n")));
		EnhancedSet<String> oracleRsf = new EnhancedHashSet<>(
			Arrays.asList(oracle.split("\\r?\\n")));
		assertEquals(oracleRsf, resultRsf);
	}

	/**
	 * ARC - smell analyzer integration test.
	 *
	 * @param systemVersion System version.
	 * @param lang System language.
	 * @param arcFileSuffix Expected result files' suffix.
	 */
	@ParameterizedTest
	@CsvSource({
		// struts 2.3.30
		"struts-2.3.30,"
			+ "java,"
			+ "_arc_",

		// struts 2.5.2
		"struts-2.5.2,"
			+ "java,"
			+ "_arc_",

		// httpd 2.3.8
		"httpd-2.3.8,"
			+ "c,"
			+ "_arc_",

		// httpd 2.4.26
		"httpd-2.4.26,"
			+ "c,"
			+ "_arc_"
	})
	public void asdWithConcernsTest(String systemVersion, String lang, String arcFileSuffix) {
		// Creating relevant path Strings
		String sysResources = resourcesDir + fs + systemVersion;
		String depsRsfFile = factsDir + fs + systemVersion + "_deps.rsf";
		String docTopicsPath = sysResources + fs + systemVersion + arcFileSuffix + "docTopics.json";
		String oracleFilePath = sysResources + fs + systemVersion + "_arc_smells.ser";
		String clusterFile = sysResources + fs + systemVersion + arcFileSuffix + "clusters.rsf";
		String resultFile = outputDirPath + fs + systemVersion + "_arc_smells.ser";

		assertDoesNotThrow(() -> {
			TopicUtil.docTopics = DocTopics.deserializeDocTopics(docTopicsPath);
			ArchSmellDetector asd = new ArchSmellDetector(
				depsRsfFile, clusterFile, resultFile, lang,
				TopicModelExtractionMethod.MALLET_API, TopicUtil.docTopics);
			asd.run(true, true, true);
		});

		// ------------------------- Generate Oracles ------------------------------

		if(generateOracles) {
			assertDoesNotThrow(() -> {
				Path resultPath = Paths.get(resultFile);
				Path oraclePath = Paths.get(oracleFilePath);
				Files.copy(resultPath, oraclePath, StandardCopyOption.REPLACE_EXISTING);
			});
		}

		// ------------------------- Generate Oracles ------------------------------

		// Construct SmellCollection objects out of the oracle and result files
		SmellCollection resultSmells = assertDoesNotThrow(() -> new SmellCollection(resultFile));
		SmellCollection oracleSmells = assertDoesNotThrow(() -> new SmellCollection(oracleFilePath));

		// SmellCollection extends HashSet, so we can use equals() to compare the result to the oracle
		assertEquals(oracleSmells, resultSmells);
	}

	/**
	 * Checks whether fastClusters is modified by
	 * updateFastClustersAndSimMatrixToReflectMergedCluster.
	 * stopCriterion = clustergain is not currently in use.
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
	public void computeArchitectureTest(String versionName,	String language,
			String packagePrefix) {
		String artifactsDir = resourcesDir + fs + versionName;
		String oracleSimMatrixPath = artifactsDir + fs + "sim_matrix_oracle.json";
		(new File(outputDirPath)).mkdirs();
		
		// Deserialize FastFeatureVectors oracle
		String ffVecsFilePath = factsDir + fs + versionName + "_fVectors.json";
		FeatureVectors builderffVecs = null;

		try {
			builderffVecs = FeatureVectors.deserializeFFVectors(ffVecsFilePath);
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		if (builderffVecs == null){
			fail("failed to deserialize FastFeatureVectors from builder object");
		}

		ConcernArchitecture concernArch = new ConcernArchitecture(versionName,
			outputDirPath, builderffVecs, language,
			artifactsDir + "/base", packagePrefix);

		SerializationCriterion serializationCriterion =
			SerializationCriterion.makeSerializationCriterion(
				"archsize", 0, concernArch);
		
		// Construct a ConcernClusteringRunner object
		ConcernClusteringRunnerMock runner = new ConcernClusteringRunnerMock(
			language, serializationCriterion, concernArch);
		Architecture initialArchitecture = new Architecture(runner.getArchitecture());
		// call computeClustersWithConcernsAndFastClusters()
		assertDoesNotThrow(() -> {
			// USING THE CLONE THAT TAKES IN THE VERSION NAME HERE
			runner.computeArchitecture(
				new ArchSizeFractionStoppingCriterion(runner.getArchitecture(), .2),
				"archsizefraction", SimMeasure.SimMeasureType.JS);
		});

		// ------------------------- Generate Oracles ------------------------------

		if (generateOracles) {
			JsonFactory factory = new JsonFactory();
			assertDoesNotThrow(() -> {
				JsonGenerator generator = factory.createGenerator(
					new File(oracleSimMatrixPath), JsonEncoding.UTF8);
				generator.writeStartObject();
				runner.getInitialSimMatrix().serialize(generator);
				generator.writeEndObject();
				generator.close();
			});
		}

		// ------------------------- Generate Oracles ------------------------------

		SimilarityMatrix oracleSimMatrix = assertDoesNotThrow(() -> {
			JsonFactory factory = new JsonFactory();
			JsonParser parser = factory.createParser(new File(oracleSimMatrixPath));
			parser.nextToken();
			return SimilarityMatrix.deserialize(parser, initialArchitecture);
		});
		SimilarityMatrix initialSimMatrix = runner.getInitialSimMatrix();

		assertEquals(oracleSimMatrix, initialSimMatrix);
	}
}
