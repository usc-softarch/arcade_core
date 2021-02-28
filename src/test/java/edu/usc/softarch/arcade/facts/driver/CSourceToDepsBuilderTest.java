package edu.usc.softarch.arcade.facts.driver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.File;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import edu.usc.softarch.arcade.util.FileUtil;
import edu.usc.softarch.arcade.util.RsfCompare;

public class CSourceToDepsBuilderTest {
    @Test
    public void buildTest1() {
        char fs = File.separatorChar;
        // Builds dependencies rsf for httpd 2.3.8
        String classesDirPath = "." + fs + "src" + fs + "test" + fs + "resources" 
            + fs + "CSourceToDepsBuilderTest_resources"
            + fs + "binaries" + fs + "httpd-2.3.8";
        // Path for output file (rsf file - list of dependencies)
        String depsRsfFilename = "." + fs + "target" + fs + "test_results" 
            + fs + "CSourceToDepsBuilderTest" + fs + "buildTest1Result.rsf";

        // Run CSourceToDepsBuilder.build()
        assertDoesNotThrow(() -> ( // to avoid exceptions stopping JUnit from running tests
            new CSourceToDepsBuilder()).build(classesDirPath, depsRsfFilename));
        String result = assertDoesNotThrow(() -> 
            { return FileUtil.readFile(depsRsfFilename, StandardCharsets.UTF_8); });
        
        // Load oracle file
        String oraclePath = "." + fs + "src" + fs + "test" + fs + "resources" 
            + fs + "CSourceToDepsBuilderTest_resources"
            + fs + "httpd-2.3.8_deps.rsf";
        String oracleResult = assertDoesNotThrow(() ->
            { return FileUtil.readFile(oraclePath, StandardCharsets.UTF_8); });

        // Compare files
        RsfCompare resultRsf = new RsfCompare(result);
        RsfCompare oracleRsf = new RsfCompare(oracleResult);
        // RsfCompare.compareTo returns 0 if files have the same contents
        assertEquals(resultRsf.compareTo(oracleRsf), 0);
    }
    @Test
    public void buildTest2() {
        char fs = File.separatorChar;
        // Builds dependencies rsf for httpd 2.4.26
        String classesDirPath = "." + fs + "src" + fs + "test" + fs + "resources" 
            + fs + "CSourceToDepsBuilderTest_resources"
            + fs + "binaries" + fs + "httpd-2.4.26";
        // Path for output file (rsf file - list of dependencies)
        String depsRsfFilename = "." + fs + "target" + fs + "test_results" 
            + fs + "CSourceToDepsBuilderTest" + fs + "buildTest2Result.rsf";

        // Run CSourceToDepsBuilder.build()
        assertDoesNotThrow(() -> ( // to avoid exceptions stopping JUnit from running tests
            new CSourceToDepsBuilder()).build(classesDirPath, depsRsfFilename));
        String result = assertDoesNotThrow(() -> 
            { return FileUtil.readFile(depsRsfFilename, StandardCharsets.UTF_8); });
        
        // Load oracle file
        String oraclePath = "." + fs + "src" + fs + "test" + fs + "resources" 
            + fs + "CSourceToDepsBuilderTest_resources"
            + fs + "httpd-2.4.26_deps.rsf";
        String oracleResult = assertDoesNotThrow(() ->
            { return FileUtil.readFile(oraclePath, StandardCharsets.UTF_8); });

        // Compare files
        RsfCompare resultRsf = new RsfCompare(result);
        RsfCompare oracleRsf = new RsfCompare(oracleResult);
        // RsfCompare.compareTo returns 0 if files have the same contents
        assertEquals(resultRsf.compareTo(oracleRsf), 0);
    }
    
}