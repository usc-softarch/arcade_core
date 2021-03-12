package edu.usc.softarch.arcade.antipattern.detection;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
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
    
		try {
      ObjectInputStream ois = new ObjectInputStream(new FileInputStream(resources_dir + clusterSmellMapObjectFile));
      Map<String, Set<String>> oracle_clusterSmellMap = (Map<String, Set<String>>) ois.readObject();
			assertTrue(clusterSmellMap.equals(oracle_clusterSmellMap));
      // TODO: check if this map or sets inside are empty?
			ois.close();

			ois = new ObjectInputStream(new FileInputStream(resources_dir + clusterObjectFile));
      ConcernClusterArchitecture oracle_clusters = (ConcernClusterArchitecture) ois.readObject();
			assertTrue(clusters.equals(oracle_clusters));
      // TODO: check if the ConcernClusterArchitecture object is empty?
			ois.close();

			ois = new ObjectInputStream(new FileInputStream(resources_dir + smellsObjectFile));
      SmellCollection oracle_detectedSmells = (SmellCollection) ois.readObject();
			assertTrue(detectedSmells.equals(oracle_detectedSmells));
      // TODO: check if SmellCollection is empty?
			ois.close();

		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
      assertTrue(false); // if we get here, we done goofed
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