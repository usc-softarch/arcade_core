package edu.usc.softarch.arcade.antipattern.detection;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import edu.usc.softarch.arcade.clustering.acdc.ACDC;
import edu.usc.softarch.arcade.antipattern.SmellCollection;
import edu.usc.softarch.arcade.clustering.ConcernClusterArchitecture;

/**
 * Tests related to the basic smell detection components of ARCADE:
 * BUO (Brick Use Overload), BDC (Brick Dependency Cycle),
 * BCO (Brick Concern Overload) and SPF (Scattered Parasitic Functionality).
 */
public class ArchSmellDetectorTest {
  private String fs = File.separator;

  @BeforeEach
	public void setUp() {
		String outputPath = "." + fs + "target" + fs + "test_results"
      + fs + "ArchSmellDetectorTest";
		File directory = new File(outputPath);
		directory.mkdirs();
	}

  /**
   * Tests for structural smell detection algorithms: BDC and BUO.
   * 
   * @param oracle Oracle ser file path
   * @param deps Deps rsf file path
   * @param output Output directory
   * @param clusters Output file path for clusters file
   * @param ser Desired filename of result ser
   */
	@ParameterizedTest
	@CsvSource({
		// struts 2.3.30
		".///src///test///resources///ArchSmellDetectorTest_resources"
      + "///edited_struts-2.3.30_acdc_smells.ser,"
		+ ".///src///test///resources///JavaSourceToDepsBuilderTest_resources"
      + "///struts-2.3.30_deps.rsf,"
		+ ".///target///test_results///ArchSmellDetectorTest,"
		+ ".///target///test_results///ArchSmellDetectorTest"
      + "///struts-2.3.30_acdc_clusters.rsf,"
		+ "struts-2.3.30_acdc_smells.ser",

		// struts 2.5.2
		".///src///test///resources///ArchSmellDetectorTest_resources"
      + "///edited_struts-2.5.2_acdc_smells.ser,"
		+ ".///src///test///resources///JavaSourceToDepsBuilderTest_resources"
      + "///struts-2.5.2_deps.rsf,"
		+ ".///target///test_results///ArchSmellDetectorTest,"
		+ ".///target///test_results///ArchSmellDetectorTest"
      + "///struts-2.5.2_acdc_clusters.rsf,"
		+ "struts-2.5.2_acdc_smells.ser",

		// httpd 2.3.8
		".///src///test///resources///ArchSmellDetectorTest_resources"
      + "///edited_httpd-2.3.8_acdc_smells.ser,"
		+ ".///src///test///resources///CSourceToDepsBuilderTest_resources"
      + "///httpd-2.3.8_deps.rsf,"
		+ ".///target///test_results///ArchSmellDetectorTest,"
		+ ".///target///test_results///ArchSmellDetectorTest"
      + "///httpd-2.3.8_acdc_clusters.rsf,"
		+ "httpd-2.3.8_acdc_smells.ser",

		// httpd 2.4.26
		".///src///test///resources///ArchSmellDetectorTest_resources"
      + "///edited_httpd-2.4.26_acdc_smells.ser,"
		+ ".///src///test///resources///CSourceToDepsBuilderTest_resources"
      + "///httpd-2.4.26_deps.rsf,"
		+ ".///target///test_results///ArchSmellDetectorTest,"
		+ ".///target///test_results///ArchSmellDetectorTest"
      + "///httpd-2.4.26_acdc_clusters.rsf,"
		+ "httpd-2.4.26_acdc_smells.ser",
	})
	public void asdWithoutConcernsTest(String oracle, String deps,
      String output, String clusters, String ser) {    
		/** ACDC - smell analyzer integration test **/
		String oraclePath = oracle.replace("///", fs);  
		String depsPath = deps.replace("///", fs);
		String outputPath = output.replace("///", fs);
		String outputClustersPath = clusters.replace ("///", fs);
		
		// Get clusters
		ACDC.run(depsPath, outputClustersPath);
		String resultSerFilename = outputPath + fs + ser;
		ArchSmellDetector asd =
      new ArchSmellDetector(depsPath, outputClustersPath, resultSerFilename);
		
		// Call ArchSmellDetector.run() (with runConcern=false)
		SmellCollection resultSmells = assertDoesNotThrow(
      () -> asd.run(true, false, true)); 

		// Construct SmellCollection objects out of the result and oracle files
		SmellCollection oracleSmells = assertDoesNotThrow(() -> {
			return new SmellCollection(oraclePath);
		});

		/* SmellCollection extends HashSet, so we can use equals() to compare the 
     * result to the oracle */
		assertEquals(oracleSmells, resultSmells);
	}

  /**
   * Sanity checks of the structural smell detection algorithms
   * 
   * @param depsRsfFilename Input deps rsf file path
   * @param clustersRsfFilename Input file path for clusters file
   * @param detectedSmellsFilename Output ser file path
   * @param clusterSmellMapObjectFile Oracle clustermap serialized object
   * @param clusterObjectFile Oracle clusters serialized object
   * @param smellsObjectFile Oracle detected smells serialized object
   * @param version Version
   */
  @CsvSource({
    //Input for asd constructor to run on struts-2.3.30
    "///input_files///struts-2.3.30_deps.rsf,"
    + "///input_files///struts-2.3.30_acdc_clustered.rsf,"
    + "///target///test_results///ArchSmellDetectorTest"
      + "///struts-2.3.30_acdc_smells.ser,"
    + "///runStructuralDetectionAlgs_resources"
      + "///struts-2.3.30_output_run_clusterSmellMap.txt,"
    + "///runStructuralDetectionAlgs_resources"
      + "///struts-2.3.30_output_run_clusters.txt,"
    + "///runStructuralDetectionAlgs_resources"
      + "///struts-2.3.30_output_run_detected_smells.txt,"
    + "struts-2.3.30",

    //Input for asd constructor to run on struts-2.5.2
    "///input_files///struts-2.5.2_deps.rsf,"
    + "///input_files///struts-2.5.2_acdc_clustered.rsf,"
    + "///target///test_results///ArchSmellDetectorTest"
      + "///struts-2.5.2_acdc_smells.ser,"
    + "///runStructuralDetectionAlgs_resources"
      + "///struts-2.5.2_output_run_clusterSmellMap.txt,"
    + "///runStructuralDetectionAlgs_resources"
      + "///struts-2.5.2_output_run_clusters.txt,"
    + "///runStructuralDetectionAlgs_resources"
      + "///struts-2.5.2_output_run_detected_smells.txt,"
    + "struts-2.5.2",

    //Input for asd constructor to run on httpd 2.3.8
    //TODO RESOURCES ARE PLACEHOLDERS REPLACE LATER
    "///input_files///httpd-2.3.8_deps.rsf,"
    + "///input_files///httpd-2.3.8_acdc_clustered.rsf,"
    + "///target///test_results///ArchSmellDetectorTest"
      + "///httpd-2.3.8_acdc_smells.ser,"
    + "///runStructuralDetectionAlgs_resources"
      + "///httpd-2.3.8_output_run_clusterSmellMap.txt,"
    + "///runStructuralDetectionAlgs_resources"
      + "///httpd-2.3.8_output_run_clusters.txt,"
    + "///runStructuralDetectionAlgs_resources"
      + "///httpd-2.3.8_output_run_detected_smells.txt,"
    + "httpd-2.3.8",

    //Input for asd constructor to run on httpd 2.4.26
    //TODO RESOURCES ARE PLACEHOLDERS REPLACE LATER
    "///input_files///httpd-2.4.26_deps.rsf,"
    + "///input_files///httpd-2.4.26_acdc_clustered.rsf,"
    + "///target///test_results///ArchSmellDetectorTest"
      + "///httpd-2.4.26_acdc_smells.ser,"
    + "///runStructuralDetectionAlgs_resources"
      + "///httpd-2.4.26_output_run_clusterSmellMap.txt,"
    + "///runStructuralDetectionAlgs_resources"
      + "///httpd-2.4.26_output_run_clusters.txt,"
    + "///runStructuralDetectionAlgs_resources"
      + "///httpd-2.4.26_output_run_detected_smells.txt,"
    + "httpd-2.4.26",
  })
  @ParameterizedTest
  public void runStructuralDetectionAlgsTest(String depsRsfFilename,
      String clustersRsfFilename, String detectedSmellsFilename, 
      String clusterSmellMapObjectFile, String clusterObjectFile,
      String smellsObjectFile, String version) {
    String resources_dir =
      "src///test///resources///ArchSmellDetectorTest_resources///";
    resources_dir = resources_dir.replace("///", fs);

    depsRsfFilename = resources_dir + depsRsfFilename.replace("///", fs);
    clustersRsfFilename =
      resources_dir + clustersRsfFilename.replace("///", fs);
    detectedSmellsFilename = detectedSmellsFilename.replace("///", fs);

    clusterSmellMapObjectFile = clusterSmellMapObjectFile.replace("///", fs);
    clusterObjectFile = clusterObjectFile.replace("///", fs);
    smellsObjectFile = smellsObjectFile.replace("///", fs);

    ArchSmellDetector asd = new ArchSmellDetector(
      depsRsfFilename, clustersRsfFilename, detectedSmellsFilename, version);
    
    // Initialize variables
		SmellCollection detectedSmells = new SmellCollection();
		ConcernClusterArchitecture clusters =
      ConcernClusterArchitecture.loadFromRsf(clustersRsfFilename);
		Map<String, Set<String>> clusterSmellMap = new HashMap<>();

    asd.runStructuralDetectionAlgs(clusters, detectedSmells, clusterSmellMap);

    /* check that the data structures are not empty after running
     * runStructuralDetectionAlgs */
    assertAll(
        () -> assertTrue(clusters.size() > 0),
        () -> assertTrue(detectedSmells.size() > 0),
        () -> assertTrue(clusterSmellMap.size() > 0)
      );
    
		try {
      ObjectInputStream ois = new ObjectInputStream(
        new FileInputStream(resources_dir + clusterSmellMapObjectFile));
      Map<String, Set<String>> oracle_clusterSmellMap =
        (Map<String, Set<String>>) ois.readObject();
			ois.close();

			ois = new ObjectInputStream(
        new FileInputStream(resources_dir + clusterObjectFile));
      ConcernClusterArchitecture oracle_clusters =
        (ConcernClusterArchitecture) ois.readObject();
			ois.close();

			ois = new ObjectInputStream(
        new FileInputStream(resources_dir + smellsObjectFile));
      SmellCollection oracle_detectedSmells =
        (SmellCollection) ois.readObject();
			ois.close();

      assertAll(
        () -> assertEquals(oracle_clusterSmellMap, clusterSmellMap),
        () -> assertEquals(oracle_clusters, clusters),
        () -> assertEquals(oracle_detectedSmells, detectedSmells)
      );
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
      // if we get here, we done goofed
      fail("Exception caught in runStructuralDetectionAlgsTest");
		}
  }

  @CsvSource({
    //Input for asd constructor to run on struts-2.3.30
    "///input_files///struts-2.3.30_deps.rsf,"
    + "ORACLE - ARC CLUSTER FILE,"
    + "ORACLE - ARC SMELLS FILE,"
    + "///runConcernDetectionAlgs_resources///struts-2.3.30_output_clusterSmellMap_after.txt,"
    + "///runConcernDetectionAlgs_resources///struts-2.3.30_output_clusters_after.txt,"
    + "///runConcernDetectionAlgs_resources///struts-2.3.30_output_detectedSmells_after.txt,"
    + "///runConcernDetectionAlgs_resources///struts-2.3.30_output_clusters_before.txt,"
    + "///runConcernDetectionAlgs_resources///struts-2.3.30_docTopics.txt,"
    + "struts-2.3.30",

    //Input for asd constructor to run on struts-2.5.2
    "///input_files///struts-2.5.2_deps.rsf,"
    + "ORACLE - ARC CLUSTER FILE,"
    + "ORACLE - ARC SMELLS FILE,"
    + "///runConcernDetectionAlgs_resources///struts-2.5.2_output_clusterSmellMap_after.txt,"
    + "///runConcernDetectionAlgs_resources///struts-2.5.2_output_clusters_after.txt,"
    + "///runConcernDetectionAlgs_resources///struts-2.5.2_output_detectedSmells_after.txt,"
    + "///runConcernDetectionAlgs_resources///struts-2.5.2_output_clusters_before.txt,"
    + "///runConcernDetectionAlgs_resources///struts-2.5.2_docTopics.txt,"
    + "struts-2.5.2",
  })
  @ParameterizedTest
  public void runConcernDetectionAlgsTest(String depsRsfFilename,
      String clustersRsfFilename, String detectedSmellsFilename, 
      String clusterSmellMapObjectFile, String clusterObjectFile,
      String smellsObjectFile, String clusterObjectFileBefore,
      String topics, String version) {
    //TODO Fix this so it runs correctly
    //throw new UnsupportedOperationException();
    
    // String resources_dir = "src///test///resources///ArchSmellDetectorTest_resources///";
    // resources_dir = resources_dir.replace("///", File.separator);

    // depsRsfFilename = resources_dir + depsRsfFilename.replace("///", File.separator);
    // clustersRsfFilename = resources_dir + clustersRsfFilename.replace("///", File.separator);
    // detectedSmellsFilename = detectedSmellsFilename.replace("///", File.separator);

    // clusterSmellMapObjectFile = clusterSmellMapObjectFile.replace("///", File.separator);
    // clusterObjectFile = clusterObjectFile.replace("///", File.separator);
    // smellsObjectFile = smellsObjectFile.replace("///", File.separator);
    // clusterObjectFileBefore = clusterObjectFileBefore.replace("///", File.separator);
    // topics = topics.replace("///", File.separator);

    // // Initialize variables
		// SmellCollection detectedSmells = new SmellCollection();
		// ConcernClusterArchitecture clusters = ConcernClusterArchitecture.loadFromRsf(clustersRsfFilename);
		// Map<String, Set<String>> clusterSmellMap = new HashMap<>();
    
    // DocTopics docTopic;
    // ArchSmellDetector asd;
    // try{
    //   ObjectInputStream ois = new ObjectInputStream(new FileInputStream(resources_dir + topics));
    //   docTopic = (DocTopics) ois.readObject();

    //   asd = new ArchSmellDetector(depsRsfFilename, clustersRsfFilename, detectedSmellsFilename,"java",
    //                     TopicModelExtractionMethod.MALLET_API, docTopic, version);

    //   ois.close();
    //   asd.runConcernDetectionAlgs(clusters, detectedSmells, clusterSmellMap);
    // } catch (IOException | ClassNotFoundException e) {
		// 	e.printStackTrace();
    //   fail("Exception caught in runConcernDetectionAlgsTest"); // if we get here, we done goofed
    // }

    // //check that the data structures are not empty after running runConcernDetectionAlgs
    // assertAll(
    //     () -> assertTrue(clusters.size() > 0),
    //     () -> assertTrue(detectedSmells.size() > 0),
    //     () -> assertTrue(clusterSmellMap.size() > 0)
    //   );

    // try {
    //   ObjectInputStream ois = new ObjectInputStream(new FileInputStream(resources_dir + clusterSmellMapObjectFile));
    //   Map<String, Set<String>> oracle_clusterSmellMap = (Map<String, Set<String>>) ois.readObject();
		// 	ois.close();

		// 	ois = new ObjectInputStream(new FileInputStream(resources_dir + clusterObjectFile));
    //   ConcernClusterArchitecture oracle_clusters = (ConcernClusterArchitecture) ois.readObject();
		// 	ois.close();

		// 	ois = new ObjectInputStream(new FileInputStream(resources_dir + smellsObjectFile));
    //   SmellCollection oracle_detectedSmells = (SmellCollection) ois.readObject();
		// 	ois.close();

      
    //   assertAll(
    //     () -> assertEquals(oracle_clusterSmellMap, clusterSmellMap),
    //     () -> assertEquals(oracle_clusters, clusters),
    //     () -> assertEquals(oracle_detectedSmells, detectedSmells)
    //   );
      
		// } catch (IOException | ClassNotFoundException e) {
		// 	e.printStackTrace();
    //   fail("Exception caught in runConcernDetectionAlgsTest");
		// }
  }

  @CsvSource({
    //Input for asd constructor to run on struts-2.3.30
    "///output///struts-2.3.30_deps.rsf,"
    + "///output///struts-2.3.30_acdc_clustered.rsf,"
    + "///output///struts-2.3.30_acdc_smells.ser,"
    + "///runConcernDetectionAlgs_resources///struts-2.3.30_output_clusterSmellMap_after.txt,"
    + "///runConcernDetectionAlgs_resources///struts-2.3.30_output_smellClusterMap_after.txt,"
    + "struts-2.3.30",


    //Input for asd constructor to run on struts-2.5.2
    "///output///struts-2.5.2_deps.rsf,"
    + "///output///struts-2.5.2_acdc_clustered.rsf,"
    + "///output///struts-2.5.2_acdc_smells.ser,"
    + "///runConcernDetectionAlgs_resources///struts-2.5.2_output_clusterSmellMap_after.txt,"
    + "///runConcernDetectionAlgs_resources///struts-2.5.2_output_smellClusterMap_after.txt,"
    + "struts-2.5.2",
  })
  @ParameterizedTest
  public void buildSmellToClustersMapTest(String depsRsfFilename, String clustersRsfFilename, String detectedSmellsFilename, 
                                          String clusterSmellMapBefore, String clusterSmellMapAter, String version) {
    String resources_dir = "src///test///resources///ArchSmellDetectorTest_resources///";
    resources_dir = resources_dir.replace("///", File.separator);

    depsRsfFilename = resources_dir + depsRsfFilename.replace("///", File.separator);
    clustersRsfFilename = resources_dir + clustersRsfFilename.replace("///", File.separator);
    detectedSmellsFilename = detectedSmellsFilename.replace("///", File.separator);

    clusterSmellMapBefore = clusterSmellMapBefore.replace("///", File.separator);
    clusterSmellMapAter = clusterSmellMapAter.replace("///", File.separator);

    ArchSmellDetector asd = new ArchSmellDetector(
      depsRsfFilename, clustersRsfFilename, detectedSmellsFilename, version);

    try {
      //Read in the clusterSmellMap
      ObjectInputStream ois = new ObjectInputStream(new FileInputStream(resources_dir + clusterSmellMapBefore));
      Map<String, Set<String>> oracle_clusterSmellMapBefore = (Map<String, Set<String>>) ois.readObject();
      ois.close();

      //Read in the smellClusterMap
      ois = new ObjectInputStream(new FileInputStream(resources_dir + clusterSmellMapAter));
      Map<String,Set<String>> oracle_clusterSmellMapAfter = (Map<String, Set<String>>) ois.readObject();
      ois.close();

      //Generate the output
      Map<String,Set<String>> smellClusterMap = asd.buildSmellToClustersMap(oracle_clusterSmellMapBefore);
      assertEquals(oracle_clusterSmellMapAfter, smellClusterMap);
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      fail("Exception caught in buildSmellToClustersMapTest"); 
    } catch (IOException | ClassNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      fail("Exception caught in buildSmellToClustersMapTest");  
    }
  }

  /*
  //To run these tests, set updateSmellMap to public
  // #region TESTS updateSmellMap ----------------------------------------------
  @Test
  public void updateSmellMapTest1() {
    // Cluster is not yet in map
    Map<String, Set<String>> clusterSmellMap = new HashMap<>();
    String clusterName = "nameGoesHere";
    String smellAbrv = "buo";

    updateSmellMap(clusterSmellMap, clusterName, smellAbrv);
    Set<String> cluster = clusterSmellMap.get(clusterName);
    assertTrue(cluster != null);
    assertTrue(cluster.contains(smellAbrv));
  }

  @Test
  public void updateSmellMapTest2() {
    // Cluster is in map, does not contain given smell
    Map<String, Set<String>> clusterSmellMap = new HashMap<>();
    String clusterName = "nameGoesHere";
    String smellAbrv = "buo";
    Set<String> cluster = new HashSet<>();
    cluster.add("spf");
    clusterSmellMap.put(clusterName, cluster);

    ArchSmellDetector.updateSmellMap(clusterSmellMap, clusterName, smellAbrv);
    cluster = clusterSmellMap.get(clusterName);
    assertTrue(cluster != null);
    assertTrue(cluster.contains(smellAbrv));
    assertTrue(cluster.contains("spf"));
  }

  @Test
  public void updateSmellMapTest3() {
    // Cluster is in map, contains given smell
    // Cluster is in map, does not contain given smell
    Map<String, Set<String>> clusterSmellMap = new HashMap<>();
    String clusterName = "nameGoesHere";
    String smellAbrv = "buo";
    Set<String> cluster = new HashSet<>();
    cluster.add(smellAbrv);
    clusterSmellMap.put(clusterName, cluster);

    ArchSmellDetector.updateSmellMap(clusterSmellMap, clusterName, smellAbrv);
    cluster = clusterSmellMap.get(clusterName);
    assertTrue(cluster != null);
    assertTrue(cluster.contains(smellAbrv));
    assertTrue(cluster.size() == 1);
  }
  // #endregion TESTS updateSmellMap -------------------------------------------
*/
}
