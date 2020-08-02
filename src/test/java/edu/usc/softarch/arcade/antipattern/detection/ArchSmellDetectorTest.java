package edu.usc.softarch.arcade.antipattern.detection;

/*
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
*/

public class ArchSmellDetectorTest {
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