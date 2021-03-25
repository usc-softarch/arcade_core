package edu.usc.softarch.arcade.clustering.drivers;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.File;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import edu.usc.softarch.arcade.util.FileUtil;
import edu.usc.softarch.arcade.util.RsfCompare;

public class BatchClusteringEngineTest {
    @ParameterizedTest
    @CsvSource({
        /*** Test parameters: ***/
        // [dir with single system version], 
        // [system language], 
        // [test file output dir name], (IMPORTANT: should also contain output.pipe and infer.mallet files)
        // [subject system binaries location], 
        // [path to oracle file], 
        // [expected clusters file name]

        // struts 2.3.30
        ".///src///test///resources///BatchClusteringEngineTest_resources///src///struts-2.3.30,"
        + "java,"
        + ".///target///test_results///BatchClusteringEngineTest,"
        + "lib_struts,"
        + ".///src///test///resources///BatchClusteringEngineTest_resources///struts-2.3.30_239_topics_234_arc_clusters.rsf,"
        + "struts-2.3.30_239_topics_234_arc_clusters.rsf",

        // // struts 2.5.2
        // ".///src///test///resources///BatchClusteringEngineTest_resources///src///struts-2.5.2,"
        // + "java,"
        // + ".///target///test_results///BatchClusteringEngineTest,"
        // + "lib_struts,"
        // + ".///src///test///resources///BatchClusteringEngineTest_resources///struts-2.5.2_284_topics_275_arc_clusters.rsf,"
        // + "struts-2.5.2_284_topics_275_arc_clusters.rsf",

        // // httpd 2.3.8
        // ".///src///test///resources///CSourceToDepsBuilderTest_resources///src///httpd-2.3.8,"
        // + "c,"
        // + ".///target///test_results///BatchClusteringEngineTest,"
        // + "/,"
        // + ".///src///test///resources///BatchClusteringEngineTest_resources///httpd-2.3.8_46_topics_71_arc_clusters.rsf,"
        // + "httpd-2.3.8_46_topics_71_arc_clusters.rsf",

        // // httpd 2.4.26
        // ".///src///test///resources///CSourceToDepsBuilderTest_resources///src///httpd-2.4.26,"
        // + "c,"
        // + ".///target///test_results///BatchClusteringEngineTest,"
        // + "/,"
        // + ".///src///test///resources///BatchClusteringEngineTest_resources///httpd-2.4.26_50_topics_82_arc_clusters.rsf,"
        // + "httpd-2.4.26_50_topics_82_arc_clusters.rsf"
    })
    public void singleTest(String sysVersionDir, String lang, String testOutputDir, String classesDir, String oraclePath, String arcFilename){
        /** Tests recovery for a single version of a system **/
        // Format paths
        String sysDir = sysVersionDir.replace("///", File.separator);
        String outputDirName = testOutputDir.replace("///", File.separator);
        // String inClassesDir = classesDir.replace("///", File.separator);
        String oracleFilePath = oraclePath.replace("///", File.separator);

        assertDoesNotThrow(() -> {
            BatchClusteringEngine.single(new File(sysDir), lang, outputDirName, classesDir);
        });

        // Result file with clusters
        String resultClustersFile = outputDirName + File.separator + arcFilename;

        // The expectation here is that this resulting clusters file has the same name as the oracle clusters file
        String result = assertDoesNotThrow(() -> {
            return FileUtil.readFile(resultClustersFile, StandardCharsets.UTF_8); // Throws IOException if file does not exist
        });

        // Load oracle
        String oracle = assertDoesNotThrow(() -> {
            return FileUtil.readFile((oracleFilePath), StandardCharsets.UTF_8); 
        });

        // RsfCompare.equals() to compare contents of oracle and result files
        RsfCompare resultRsf = new RsfCompare(result);
        RsfCompare oracleRsf = new RsfCompare(oracle);
        assertTrue(oracleRsf.equals(resultRsf));
    }
}
