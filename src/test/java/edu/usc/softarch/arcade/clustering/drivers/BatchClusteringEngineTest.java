package edu.usc.softarch.arcade.clustering.drivers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.File;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import org.junit.jupiter.api.Test;

import edu.usc.softarch.arcade.util.FileUtil;
import edu.usc.softarch.arcade.util.RsfCompare;

public class BatchClusteringEngineTest {
    @ParameterizedTest
    @CsvSource({
        // Input: [dir with system version], [system language], [test file output dir name], [path from dir with system version to its binaries], [oracle path]
        // struts 2.3.30
        ".///src///test///resources///JavaSourceToDepsBuilderTest_resources///binaries///struts-2.3.30,"
        + "java,"
        + ".///target///test_results///BatchClusteringEngineTest,"
        + "lib_struts,"
        + ".///src///test///resources///BatchClusteringEngineTest_resources///struts-2.3.30_239_topics_234_arc_clusters.rsf"

    })
    // NOTE: test is not working (NPE in TopicUtil.setDocTopicForFastClusterForMalletApi, still investigating)
    public void singleTest(String sysVersionDir, String lang, String testOutputDir, String classesDir, String oraclePath){
        /* Tests recovery for a single version of a system */
        // Format paths
        String sysDir = sysVersionDir.replace("///", File.separator);
        String outputDirName = testOutputDir.replace("///", File.separator);
        String inClassesDir = classesDir.replace("///", File.separator);
        String oracleFilePath = oraclePath.replace("///", File.separator);

        assertDoesNotThrow(() -> BatchClusteringEngine.single(new File(sysDir), lang, outputDirName, inClassesDir));

        // Result file with clusters
        // TODO: get its actual path (serialize and deserialize ConcernClusteringRunner obj)
        String resultClustersFile = "get_from_ConcernClusteringRunner";
        String result = assertDoesNotThrow(() -> {
            return FileUtil.readFile(resultClustersFile, StandardCharsets.UTF_8); 
        });

        // Load oracle
        String oracle = assertDoesNotThrow(() -> {
            return FileUtil.readFile(oracleFilePath, StandardCharsets.UTF_8); 
        });

        // RsfCompare.equals() to compare contents of oracle and result files
        RsfCompare resultRsf = new RsfCompare(result);
        RsfCompare oracleRsf = new RsfCompare(oracle);
        assertTrue(oracleRsf.equals(resultRsf));
    }
}
