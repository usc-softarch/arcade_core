package edu.usc.softarch.arcade.antipattern.detection;

import static org.junit.jupiter.api.Assertions.assertAll;
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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import edu.usc.softarch.arcade.antipattern.SmellCollection;
import edu.usc.softarch.arcade.clustering.ConcernClusterArchitecture;

public class ArchSmellDetectorTest {
  @CsvSource({
    //Input for asd constructor to run on struts-2.3.30
    "///output///struts-2.3.30_deps.rsf,"
    + "///output///struts-2.3.30_acdc_clustered.rsf,"
    + "///output///struts-2.3.30_acdc_smells.ser,"
    + "///runStructuralDetectionAlgs_resources///struts-2.3.30_output_run_clusterSmellMap.txt,"
    + "///runStructuralDetectionAlgs_resources///struts-2.3.30_output_run_clusters.txt,"
    + "///runStructuralDetectionAlgs_resources///struts-2.3.30_output_run_detected_smells.txt",

    //Input for asd constructor to run on struts-2.5.2
    "///output///struts-2.5.2_deps.rsf,"
    + "///output///struts-2.5.2_acdc_clustered.rsf,"
    + "///output///struts-2.5.2_acdc_smells.ser,"
    + "///runStructuralDetectionAlgs_resources///struts-2.5.2_output_run_clusterSmellMap.txt,"
    + "///runStructuralDetectionAlgs_resources///struts-2.5.2_output_run_clusters.txt,"
    + "///runStructuralDetectionAlgs_resources///struts-2.5.2_output_run_detected_smells.txt",
    
  })
  @ParameterizedTest
  public void runStructuralDetectionAlgsTest(String depsRsfFilename, String clustersRsfFilename, String detectedSmellsFilename, 
                                             String clusterSmellMapObjectFile, String clusterObjectFile, String smellsObjectFile){
    String resources_dir = "src///test///resources///ArchSmellDetector_resources///";
    resources_dir = resources_dir.replace("///", File.separator);

    depsRsfFilename = resources_dir + depsRsfFilename.replace("///", File.separator);
    clustersRsfFilename = resources_dir + clustersRsfFilename.replace("///", File.separator);
    detectedSmellsFilename = detectedSmellsFilename.replace("///", File.separator);

    clusterSmellMapObjectFile = clusterSmellMapObjectFile.replace("///", File.separator);
    clusterObjectFile = clusterObjectFile.replace("///", File.separator);
    smellsObjectFile = smellsObjectFile.replace("///", File.separator);

    ArchSmellDetector asd;
    asd = new ArchSmellDetector(depsRsfFilename, clustersRsfFilename, detectedSmellsFilename);
    
    // Initialize variables
		SmellCollection detectedSmells = new SmellCollection();
		ConcernClusterArchitecture clusters = ConcernClusterArchitecture.loadFromRsf(clustersRsfFilename);
		Map<String, Set<String>> clusterSmellMap = new HashMap<>();

    asd.runStructuralDetectionAlgs(clusters, detectedSmells, clusterSmellMap);

    // check that the data structures are not empty after running runStructuralDetectionAlgs
    assertAll(
        () -> assertTrue(clusters.size() > 0),
        () -> assertTrue(detectedSmells.size() > 0),
        () -> assertTrue(clusterSmellMap.size() > 0)
      );
    
		try {
      ObjectInputStream ois = new ObjectInputStream(new FileInputStream(resources_dir + clusterSmellMapObjectFile));
      Map<String, Set<String>> oracle_clusterSmellMap = (Map<String, Set<String>>) ois.readObject();
			ois.close();

			ois = new ObjectInputStream(new FileInputStream(resources_dir + clusterObjectFile));
      ConcernClusterArchitecture oracle_clusters = (ConcernClusterArchitecture) ois.readObject();
			ois.close();

			ois = new ObjectInputStream(new FileInputStream(resources_dir + smellsObjectFile));
      SmellCollection oracle_detectedSmells = (SmellCollection) ois.readObject();
			ois.close();

      assertAll(
        () -> assertTrue(clusterSmellMap.equals(oracle_clusterSmellMap)),
        () -> assertTrue(clusters.equals(oracle_clusters)),
        () -> assertTrue(detectedSmells.equals(oracle_detectedSmells))
      );

		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
      fail("Exception caught in runStructuralDetectionAlgsTest"); // if we get here, we done goofed
		}
  }

  @CsvSource({
    //Input for asd constructor to run on struts-2.3.30
    "///output///struts-2.3.30_deps.rsf,"
    + "///output///struts-2.3.30_acdc_clustered.rsf,"
    + "///output///struts-2.3.30_acdc_smells.ser,"
    + "///runConcernDetectionAlgs_resources///struts-2.3.30_output_clusterSmellMap_after.txt,"
    + "///runConcernDetectionAlgs_resources///struts-2.3.30_output_clusters_after.txt,"
    + "///runConcernDetectionAlgs_resources///struts-2.3.30_output_detectedSmells_after.txt,"
    + "///runConcernDetectionAlgs_resources///struts-2.3.30_output_clusters_before.txt",

    //Input for asd constructor to run on struts-2.5.2
    "///output///struts-2.5.2_deps.rsf,"
    + "///output///struts-2.5.2_acdc_clustered.rsf,"
    + "///output///struts-2.5.2_acdc_smells.ser,"
    + "///runConcernDetectionAlgs_resources///struts-2.5.2_output_clusterSmellMap_after.txt,"
    + "///runConcernDetectionAlgs_resources///struts-2.5.2_output_clusters_after.txt,"
    + "///runConcernDetectionAlgs_resources///struts-2.5.2_output_detectedSmells_after.txt,"
    + "///runConcernDetectionAlgs_resources///struts-2.5.2_output_clusters_before.txt",
  })
  @ParameterizedTest
  public void runConcernDetectionAlgsTest(String depsRsfFilename, String clustersRsfFilename, String detectedSmellsFilename, 
                                             String clusterSmellMapObjectFile, String clusterObjectFile, String smellsObjectFile,
                                             String clusterObjectFileBefore){


    String resources_dir = "src///test///resources///ArchSmellDetector_resources///";
    resources_dir = resources_dir.replace("///", File.separator);

    depsRsfFilename = resources_dir + depsRsfFilename.replace("///", File.separator);
    clustersRsfFilename = resources_dir + clustersRsfFilename.replace("///", File.separator);
    detectedSmellsFilename = detectedSmellsFilename.replace("///", File.separator);

    clusterSmellMapObjectFile = clusterSmellMapObjectFile.replace("///", File.separator);
    clusterObjectFile = clusterObjectFile.replace("///", File.separator);
    smellsObjectFile = smellsObjectFile.replace("///", File.separator);
    clusterObjectFileBefore = clusterObjectFileBefore.replace("///", File.separator);

    ArchSmellDetector asd;
    asd = new ArchSmellDetector(depsRsfFilename, clustersRsfFilename, detectedSmellsFilename);

    // Initialize variables
		SmellCollection detectedSmells = new SmellCollection();
		ConcernClusterArchitecture clusters = ConcernClusterArchitecture.loadFromRsf(clustersRsfFilename);
		Map<String, Set<String>> clusterSmellMap = new HashMap<>();
    
    // TODO: Might not need this try/catch block now that we have unit tests for loadFromRsf
    try{
      ObjectInputStream ois = new ObjectInputStream(new FileInputStream(resources_dir + clusterObjectFileBefore));
      ConcernClusterArchitecture oracle_clusters2 = (ConcernClusterArchitecture) ois.readObject();
      assertTrue(clusters.equals(oracle_clusters2));
      // TODO: check if the ConcernClusterArchitecture object is empty?
      ois.close();
    } catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
      fail("Exception caught in runConcernDetectionAlgsTest"); // if we get here, we done goofed
    }

    asd.runConcernDetectionAlgs(clusters, detectedSmells, clusterSmellMap);

    // check that the data structures are not empty after running runConcernDetectionAlgs
    assertAll(
        () -> assertTrue(clusters.size() > 0),
        () -> assertTrue(detectedSmells.size() > 0),
        () -> assertTrue(clusterSmellMap.size() > 0)
      );

    try {
      ObjectInputStream ois = new ObjectInputStream(new FileInputStream(resources_dir + clusterSmellMapObjectFile));
      Map<String, Set<String>> oracle_clusterSmellMap = (Map<String, Set<String>>) ois.readObject();
			ois.close();

			ois = new ObjectInputStream(new FileInputStream(resources_dir + clusterObjectFile));
      ConcernClusterArchitecture oracle_clusters = (ConcernClusterArchitecture) ois.readObject();
			ois.close();

			ois = new ObjectInputStream(new FileInputStream(resources_dir + smellsObjectFile));
      SmellCollection oracle_detectedSmells = (SmellCollection) ois.readObject();
			ois.close();

      assertAll(
        () -> assertTrue(clusterSmellMap.equals(oracle_clusterSmellMap)),
        () -> assertTrue(clusters.equals(oracle_clusters)),
        () -> assertTrue(detectedSmells.equals(oracle_detectedSmells))
      );

		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
      fail("Exception caught in runConcernDetectionAlgsTest");
		}

  }

  @CsvSource({
    //Input for asd constructor to run on struts-2.3.30
    "///output///struts-2.3.30_deps.rsf,"
    + "///output///struts-2.3.30_acdc_clustered.rsf,"
    + "///output///struts-2.3.30_acdc_smells.ser,"
    + "///runConcernDetectionAlgs_resources///struts-2.3.30_output_clusterSmellMap_after.txt,"
    + "///runConcernDetectionAlgs_resources///struts-2.3.30_output_smellClusterMap_after.txt",


    //Input for asd constructor to run on struts-2.5.2
    "///output///struts-2.5.2_deps.rsf,"
    + "///output///struts-2.5.2_acdc_clustered.rsf,"
    + "///output///struts-2.5.2_acdc_smells.ser,"
    + "///runConcernDetectionAlgs_resources///struts-2.5.2_output_clusterSmellMap_after.txt,"
    + "///runConcernDetectionAlgs_resources///struts-2.5.2_output_smellClusterMap_after.txt",
  })
  @ParameterizedTest
  public void buildSmellToClustersMapTest(String depsRsfFilename, String clustersRsfFilename, String detectedSmellsFilename, 
                                          String clusterSmellMapBefore, String clusterSmellMapAter){
    String resources_dir = "src///test///resources///ArchSmellDetector_resources///";
    resources_dir = resources_dir.replace("///", File.separator);

    depsRsfFilename = resources_dir + depsRsfFilename.replace("///", File.separator);
    clustersRsfFilename = resources_dir + clustersRsfFilename.replace("///", File.separator);
    detectedSmellsFilename = detectedSmellsFilename.replace("///", File.separator);

    clusterSmellMapBefore = clusterSmellMapBefore.replace("///", File.separator);
    clusterSmellMapAter = clusterSmellMapAter.replace("///", File.separator);

    ArchSmellDetector asd;
    asd = new ArchSmellDetector(depsRsfFilename, clustersRsfFilename, detectedSmellsFilename);

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
      assertTrue(smellClusterMap.equals(oracle_clusterSmellMapAfter));

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