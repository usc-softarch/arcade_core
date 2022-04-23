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
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

import edu.usc.softarch.arcade.antipattern.SmellCollection;
import edu.usc.softarch.arcade.antipattern.detection.ArchSmellDetector;
import edu.usc.softarch.arcade.clustering.Cluster;
import edu.usc.softarch.arcade.clustering.FeatureVectors;
import edu.usc.softarch.arcade.clustering.SimilarityMatrix;
import edu.usc.softarch.arcade.clustering.criteria.PreSelectedStoppingCriterion;
import edu.usc.softarch.arcade.topics.DocTopics;
import edu.usc.softarch.arcade.topics.TopicModelExtractionMethod;
import edu.usc.softarch.arcade.topics.TopicUtil;
import edu.usc.softarch.arcade.util.FileUtil;
import edu.usc.softarch.arcade.util.RsfCompare;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import edu.usc.softarch.arcade.clustering.Architecture;

public class ConcernClusteringRunnerTest {
	private final String fs = File.separator;
	private final String resourcesBase =
		"." + fs + "src" + fs + "test" + fs + "resources";
	private final String resourcesDir = resourcesBase + fs + "ARC";
	private final String subjectSystemsDir = resourcesBase + fs + "subject_systems";
	private final String outputDirPath =
		"." + fs + "target" + fs + "test_results" + fs + "ConcernClusteringRunnerTest";

	/* ------------------------------------------------------------------------ */
	/* -------------------------- DANGER ZONE --------------------------------- */
	/* ------------------------------------------------------------------------ */

	/* DO NOT TOUCH THIS ATTRIBUTE. It will trigger a procedure to re-generate
	 * the oracles of every ARC test case. Unless your name is Marcelo, or you
	 * have been given express permission by me to touch this, it must remain
	 * false at all times. */
	private final boolean generateOracles = false;

	/* ------------------------------------------------------------------------ */
	/* -------------------------- DANGER ZONE --------------------------------- */
	/* ------------------------------------------------------------------------ */

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
			+ "_239_topics_234_arc_clusters.rsf",

		// struts 2.5.2
		"struts-2.5.2,"
			+ "java,"
			+ "_284_topics_275_arc_clusters.rsf",

		// httpd 2.3.8
		"httpd-2.3.8,"
			+ "c,"
			+ "_46_topics_71_arc_clusters.rsf",

		// httpd 2.4.26
		"httpd-2.4.26," +
			"c,"
			+ "_50_topics_82_arc_clusters.rsf"
	})
	public void ARCRecoveryTest(String systemVersion, String lang,
			String arcFileSuffix) {
		// Creating relevant path Strings
		String sysDir = subjectSystemsDir + fs + systemVersion;
		String sysResources = resourcesDir + fs + systemVersion;
		String artifactsDir = sysResources + fs + "base";
		String oracleFilePath = sysResources + fs + systemVersion + arcFileSuffix;
		String ffVecs = sysResources + fs + systemVersion + "_ffVecs.json";
		String resultClustersFile = outputDirPath + fs + systemVersion + arcFileSuffix;

		assertDoesNotThrow(() ->
			ConcernClusteringRunnerMock.runARC(lang, outputDirPath, sysDir, ffVecs, artifactsDir));

		/* The expectation here is that this resulting clusters file has the same
		 * name as the oracle clusters file, meaning it has the same number of
		 * clusters and topics. */
		assertTrue(new File(resultClustersFile).exists(),
			"resulting clusters file name does not match oracle clusters file name:"
				+ resultClustersFile);

		String result = assertDoesNotThrow(() ->
			FileUtil.readFile(resultClustersFile, StandardCharsets.UTF_8));

		// Load oracle
		String oracle = assertDoesNotThrow(() ->
			FileUtil.readFile((oracleFilePath), StandardCharsets.UTF_8));

		// RsfCompare.equals() to compare contents of oracle and result files
		RsfCompare resultRsf = new RsfCompare(result);
		RsfCompare oracleRsf = new RsfCompare(oracle);
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
			+ "_239_topics_234_arc_",

		// struts 2.5.2
		"struts-2.5.2,"
			+ "java,"
			+ "_284_topics_275_arc_",

		// httpd 2.3.8
		"httpd-2.3.8,"
			+ "c,"
			+ "_46_topics_71_arc_",

		// httpd 2.4.26
		"httpd-2.4.26,"
			+ "c,"
			+ "_50_topics_82_arc_"
	})
	public void asdWithConcernsTest(String systemVersion, String lang, String arcFileSuffix) {
		// Creating relevant path Strings
		String sysResources = resourcesDir + fs + systemVersion;
		String depsRsfFile = sysResources + fs + systemVersion + "_deps.rsf";
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
		+ "java",

		// struts 2.5.2
		"struts-2.5.2,"
		+ "java",

		// httpd-2.3.8
		"httpd-2.3.8,"
		+ "c",

		// httpd-2.4.26
		"httpd-2.4.26,"
		+ "c",
	})
	public void initDataStructuresTest(String versionName, String language) {
		String fullSrcDir = subjectSystemsDir + fs + versionName;
		String artifactsDir = resourcesDir + fs + versionName;
		String initialArchitecturePath = artifactsDir + fs + "initial_architecture.txt";
		String architectureWithDocTopicsPath =
			artifactsDir + fs + "architecture_with_doc_topics.txt";
		
		// Deserialize FastFeatureVectors oracle
		String ffVecsFilePath = artifactsDir + fs + versionName + "_ffVecs.json";
		FeatureVectors builderffVecs = null;

		try {
			builderffVecs = FeatureVectors.deserializeFFVectors(ffVecsFilePath);
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		if (builderffVecs == null)
			fail("failed to deserialize FastFeatureVectors from builder object");
		
		// Construct a ConcernClusteringRunner object
		ConcernClusteringRunnerMock runner = new ConcernClusteringRunnerMock(
			builderffVecs, fullSrcDir, artifactsDir + "/base", language);

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
			() -> assertEquals(initialArchitectureOracle, initialArchitecture),
			() -> assertEquals(architectureWithDocTopicsOracle, architectureWithDocTopics)
		);
		resetClusterAges();
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
		+ "java",

		// struts 2.5.2
		"struts-2.5.2,"
		+ "java",

		// httpd-2.3.8
		"httpd-2.3.8,"
		+ "c",

		// httpd-2.4.26
		"httpd-2.4.26,"
		+ "c",
	})
	public void computeArchitectureTest(String versionName,	String language) {
		String fullSrcDir = subjectSystemsDir + fs + versionName;
		String artifactsDir = resourcesDir + fs + versionName;
		String architectureWithDocTopicsPath =
			artifactsDir + fs + "architecture_with_doc_topics.txt";
		(new File(outputDirPath)).mkdirs();
		
		// Deserialize FastFeatureVectors oracle
		String ffVecsFilePath = artifactsDir + fs + versionName + "_ffVecs.json";
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
		
		// Construct a ConcernClusteringRunner object
		ConcernClusteringRunnerMock runner = new ConcernClusteringRunnerMock(
			builderffVecs, fullSrcDir, artifactsDir + "/base", language);
		// call computeClustersWithConcernsAndFastClusters()
		assertDoesNotThrow(() -> {
			// copied from BatchClusteringEngine
			int numClusters = (int) ((double) runner.getArchitecture().size() * .20);
			// USING THE CLONE THAT TAKES IN THE VERSION NAME HERE
			runner.computeArchitecture(
				new PreSelectedStoppingCriterion(numClusters, runner),
				"preselected", SimilarityMatrix.SimMeasure.JS);
		});

		Architecture fastClustersAfterInit = runner.getArchitectureWithDocTopics();
		Architecture fastClustersCompute = runner.getArchitecture();
		// Check that fastClusters not empty after computeClustersWithConcernsAndFastClusters call 
		assertFalse(fastClustersCompute.isEmpty());
		// The size of the fastClusters should be smaller afterward
		assertTrue(fastClustersCompute.size() < fastClustersAfterInit.size());

		Architecture simMatrixInputOracle = assertDoesNotThrow(() -> {
			ObjectInputStream in =
				new ObjectInputStream(new FileInputStream(architectureWithDocTopicsPath));
			return (Architecture) in.readObject();
		});

		SimilarityMatrix oracleSimMatrix = assertDoesNotThrow(() ->
			new SimilarityMatrix(SimilarityMatrix.SimMeasure.JS, simMatrixInputOracle));
		SimilarityMatrix initialSimMatrix = runner.getInitialSimMatrix();

		assertEquals(oracleSimMatrix, initialSimMatrix);
		resetClusterAges();
	}

	private void resetClusterAges() {
		Cluster dummy = new Cluster();
		Class<? extends Cluster> clusterClass = dummy.getClass();
		Field ageCounterField;
		try {
			ageCounterField = clusterClass.getDeclaredField("ageCounter");
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
		ageCounterField.setAccessible(true);
		try {
			ageCounterField.set(null, 0);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * This test stops ARCADE from ever passing CI if someone forgets to turn off
	 * oracle generation.
	 */
	@Test
	public void oracleGenerationIsOffTest() {
		assertFalse(generateOracles);	}
}
