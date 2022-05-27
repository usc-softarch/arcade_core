package edu.usc.softarch.arcade.clustering.techniques;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
import edu.usc.softarch.arcade.clustering.FeatureVectors;
import edu.usc.softarch.arcade.clustering.simmeasures.SimMeasure;
import edu.usc.softarch.arcade.clustering.simmeasures.SimilarityMatrix;
import edu.usc.softarch.arcade.clustering.criteria.PreSelectedStoppingCriterion;
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
			+ "_arc_clusters.rsf,"
			+ "org.apache.struts2",

		// struts 2.5.2
		"struts-2.5.2,"
			+ "java,"
			+ "_arc_clusters.rsf,"
			+ "org.apache.struts2",

		// httpd 2.3.8
		"httpd-2.3.8,"
			+ "c,"
			+ "_arc_clusters.rsf,"
			+ "",

		// httpd 2.4.26
		"httpd-2.4.26,"
			+ "c,"
			+ "_arc_clusters.rsf,"
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

		Architecture arch = assertDoesNotThrow(() ->
			new Architecture(systemVersion, outputDirPath,
			FeatureVectors.deserializeFFVectors(ffVecs),
			lang,	packagePrefix));

		SerializationCriterion serialCrit =
			SerializationCriterion.makeSerializationCriterion(
			"archsize", 100, arch);

		StoppingCriterion stopCrit = StoppingCriterion.makeStoppingCriterion(
				"preselected", 0);

		assertDoesNotThrow(() ->
			ConcernClusteringRunnerMock.run(arch, serialCrit, stopCrit, lang,
				"preselected",	SimMeasure.SimMeasureType.JS,
				outputDirPath, artifactsDir));

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
	 * This test primarily verifies that initializeClusterDocTopics is making
	 * modifications to the internal structures of ConcernClusteringRunner.
	 * Checks that ConcernClusteringRunner.featureVectors is not null after the
	 * ConcernClusteringRunner constructor call. Checks that
	 * ConcernClusteringRunner.architecture is modified in the
	 * ConcernClusteringRunner constructor.
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
	public void initDataStructuresTest(String versionName, String language,
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

		Architecture arch = new Architecture(versionName, outputDirPath,
			builderffVecs, language, packagePrefix);

		SerializationCriterion serializationCriterion =
			SerializationCriterion.makeSerializationCriterion(
				"archsize", 0, arch);
		
		// Construct a ConcernClusteringRunner object
		ConcernClusteringRunnerMock runner = new ConcernClusteringRunnerMock(
			language, serializationCriterion, arch, artifactsDir + "/base");

		/* Tests whether fastClusters was altered by
		 * initializeDocTopicsForEachFastCluster */
		Architecture initialArchitecture = runner.getInitialArchitecture();
		Architecture architectureWithDocTopics = runner.getArchitectureWithDocTopics();

		// ------------------------- Generate Oracles ------------------------------

		if (generateOracles) {
			assertDoesNotThrow(() -> {
				ObjectOutputStream out =
					new ObjectOutputStream(new FileOutputStream(initialArchitecturePath));
				out.writeObject(initialArchitecture);
				out.close();
				out = new ObjectOutputStream(new FileOutputStream(architectureWithDocTopicsPath));
				out.writeObject(architectureWithDocTopics);
				out.close();
			});
		}

		// ------------------------- Generate Oracles ------------------------------

		System.out.println("fastClusters size before: " + initialArchitecture.size());
		System.out.println("fastClusters size after: " + architectureWithDocTopics.size());

		// every node should get a cluster, so there should be at least one cluster
		assertFalse(initialArchitecture.isEmpty());
		assertAll(
			() -> assertFalse(architectureWithDocTopics.isEmpty(),
				"fastClusters empty after initializeDocTopicsForEachFastCluster"),
			() -> assertNotEquals(architectureWithDocTopics, initialArchitecture)
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
			() -> assertEquals(initialArchitectureOracle, initialArchitecture,
				"Initial Architectures did not match."),
			() -> assertEquals(architectureWithDocTopicsOracle, architectureWithDocTopics,
				"DocTopic Architectures did not match.")
		);
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

		Architecture arch = new Architecture(versionName, outputDirPath,
			builderffVecs, language, packagePrefix);

		SerializationCriterion serializationCriterion =
			SerializationCriterion.makeSerializationCriterion(
				"archsize", 0, arch);
		
		// Construct a ConcernClusteringRunner object
		ConcernClusteringRunnerMock runner = new ConcernClusteringRunnerMock(
			language, serializationCriterion, arch, artifactsDir + "/base");
		// call computeClustersWithConcernsAndFastClusters()
		assertDoesNotThrow(() -> {
			// copied from BatchClusteringEngine
			int numClusters = (int) ((double) runner.getArchitecture().size() * .20);
			// USING THE CLONE THAT TAKES IN THE VERSION NAME HERE
			runner.computeArchitecture(
				new PreSelectedStoppingCriterion(numClusters),
				"preselected", SimMeasure.SimMeasureType.JS);
		});

		Architecture fastClustersAfterInit = runner.getArchitectureWithDocTopics();
		Architecture fastClustersCompute = runner.getArchitecture();
		// Check that fastClusters not empty after computeClustersWithConcernsAndFastClusters call 
		assertFalse(fastClustersCompute.isEmpty());
		// The size of the fastClusters should be smaller afterward
		assertTrue(fastClustersCompute.size() < fastClustersAfterInit.size());

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
			return SimilarityMatrix.deserialize(parser, runner.getArchitectureWithDocTopics());
		});
		SimilarityMatrix initialSimMatrix = runner.getInitialSimMatrix();

		assertEquals(oracleSimMatrix, initialSimMatrix);
	}
}
