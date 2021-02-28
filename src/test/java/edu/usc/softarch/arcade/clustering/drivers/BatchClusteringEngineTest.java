package edu.usc.softarch.arcade.clustering.drivers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.File;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import edu.usc.softarch.arcade.util.FileUtil;
import edu.usc.softarch.arcade.util.RsfCompare;

public class BatchClusteringEngineTest {
    @Test
    public void struts2SingleTest(){
        char fs = File.separatorChar;
        File folder = new File("." + fs + "src" + fs + "test" + fs + "resources" + fs + "_dir with system version_");
        String language = "java";
        String outputDirName = "." + fs + "target" + fs + "test_results" + fs 
        + "BatchClusteringEngineTest";
        String inClassesDir = "." + fs + "src" + fs + "test" + fs + "resources"
        + fs + "JavaSourceToDepsBuilderTest_resources"
        + fs + "binaries" + fs + "struts-2.3.30" + fs + "lib_struts";

        // File with rsf clusters
        // TODO: find a way to get the actual path (will need to modify source code)
        String arcClustersFile = "hard_code_this_path";

        assertDoesNotThrow(() -> (
            new BatchClusteringEngine()).single(folder, language, outputDirName, inClassesDir));
        
        String result = assertDoesNotThrow(() -> 
            { return FileUtil.readFile(arcClustersFile, StandardCharsets.UTF_8); });
        
        String oraclePath = "." + fs + "src" + fs + "test" + fs + "resources"
        + fs + "BatchClusteringEngineTest_resources";

        String oracle = assertDoesNotThrow(() -> 
        { return FileUtil.readFile(oraclePath, StandardCharsets.UTF_8); });

        RsfCompare resultRsf = new RsfCompare(result);
        RsfCompare oracleRsf = new RsfCompare(oracle);

        assertEquals(resultRsf.compareTo(oracleRsf), 0);
    }

    public void httpdSingleTest(){
        char fs = File.separatorChar;
        File folder = new File("." + fs + "src" + fs + "test" + fs + "resources" + fs + "_dir with system version_");
        String language = "c";
        String outputDirName = "." + fs + "target" + fs + "test_results" + fs 
        + "BatchClusteringEngineTest";
        String inClassesDir = "." + fs + "src" + fs + "test" + fs + "resources"
        + fs + "CSourceToDepsBuilderTest_resources"
        + fs + "binaries" + fs + "httpd-2.3.8";

        // File with rsf clusters
        // TODO: find a way to get the actual path (will need to modify source code)
        String arcClustersFile = "hard_code_this_path";

        assertDoesNotThrow(() -> (
            new BatchClusteringEngine()).single(folder, language, outputDirName, inClassesDir));
        
        String result = assertDoesNotThrow(() -> 
            { return FileUtil.readFile(arcClustersFile, StandardCharsets.UTF_8); });
        
        String oraclePath = "." + fs + "src" + fs + "test" + fs + "resources"
        + fs + "BatchClusteringEngineTest_resources";

        String oracle = assertDoesNotThrow(() -> 
        { return FileUtil.readFile(oraclePath, StandardCharsets.UTF_8); });

        RsfCompare resultRsf = new RsfCompare(result);
        RsfCompare oracleRsf = new RsfCompare(oracle);

        assertEquals(resultRsf.compareTo(oracleRsf), 0);
    }
}
