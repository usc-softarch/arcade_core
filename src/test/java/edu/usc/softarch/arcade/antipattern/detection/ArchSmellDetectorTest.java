package edu.usc.softarch.arcade.antipattern.detection;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;

import edu.usc.softarch.arcade.antipattern.SmellCollection;
import edu.usc.softarch.arcade.clustering.ConcernClusterArchitecture;
import edu.usc.softarch.arcade.clustering.acdc.ACDC;

/*
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
*/

public class ArchSmellDetectorTest {

  String source_deps_rsf_path;
  String ACDC_output_cluster_path;
  String targetSerFilename;
  ArchSmellDetector asd;
  
  @BeforeEach
  public void setUp(){
    char fs = File.separatorChar;

    source_deps_rsf_path = "." + fs + "src" + fs + "test" + fs + "resources"
    + fs + "JavaSourceToDepsBuilderTest_resources_old" + fs +"arcade_old_deps_oracle.rsf";

    ACDC_output_cluster_path = "." + fs + "target" + fs + "ACDC_test_results" + fs + "test_old_deps_acdc_clustered.rsf";

    assertDoesNotThrow(() -> ACDC.run(source_deps_rsf_path, ACDC_output_cluster_path));

    String targetSerPath = "." + fs + "target" + fs + "test_results";

    File directory = new File(targetSerPath);
    directory.mkdirs();

    targetSerFilename = "." + fs + "target" + fs + "test_results" + fs 
      + "ACDC_test_compare_smells_with_concerns.ser";
  
    asd = new ArchSmellDetector(source_deps_rsf_path, ACDC_output_cluster_path, targetSerFilename);
  }

  // TODO: Make this a parameterized test specific to different versions
  //       tests for httpd-2.3.8, httpd-2.4.26, structs-2.3.30, and structs 2.5.2

  // this test shouldn't pass right now since I haven't solved making this run for a specific version yet
  // and also making sure I have the right serialized objects for each version

  @Test
  public void runStructuralDetectionAlgsTest(){

    // Initialize variables
		SmellCollection detectedSmells = new SmellCollection();
		ConcernClusterArchitecture clusters = ConcernClusterArchitecture.loadFromRsf(ACDC_output_cluster_path);
		Map<String, Set<String>> clusterSmellMap = new HashMap<>();

    asd.runStructuralDetectionAlgs(clusters, detectedSmells, clusterSmellMap);
    
		try {
      String resources_dir = "src///test///resources///ArchSmellDetector_resources///runStructuralDetectionAlgs_resouces///";
      resources_dir = resources_dir.replace("///", File.separator);

      ObjectInputStream ois = new ObjectInputStream(new FileInputStream(resources_dir + "output_run_clusterSmellMap.txt"));
      Map<String, Set<String>> oracle_clusterSmellMap = (Map<String, Set<String>>) ois.readObject();
			assert(clusterSmellMap.equals(oracle_clusterSmellMap));
			ois.close();

			ois = new ObjectInputStream(new FileInputStream(resources_dir + "output_run_clusters.txt"));
      ConcernClusterArchitecture oracle_clusters = (ConcernClusterArchitecture) ois.readObject();
			assert(clusters.equals(oracle_clusters));
			ois.close();

			ois = new ObjectInputStream(new FileInputStream(resources_dir + "output_run_detected_smells.txt"));
      SmellCollection oracle_detectedSmells = (SmellCollection) ois.readObject();
			assert(detectedSmells.equals(oracle_detectedSmells));
			ois.close();

		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
  }

  /* To run these tests, set updateSmellMap to public
  // #region TESTS updateSmellMap ----------------------------------------------
  @Test
  public void updateSmellMapTest1() {
    // Cluster is not yet in map
    Map<String, Set<String>> clusterSmellMap = new HashMap<>();
    String clusterName = "nameGoesHere";
    String smellAbrv = "buo";

    ArchSmellDetector.updateSmellMap(clusterSmellMap, clusterName, smellAbrv);
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