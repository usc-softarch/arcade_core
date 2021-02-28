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
        // TODO: generate oracles from bitbucket
        String classesDirPath = "." + fs + "src" + fs + "test" + fs + "resources" 
            + fs + "binary_path_here";
        // Path for output file (rsf file - list of dependencies)
        String depsRsfFilename = "." + fs + "target" + fs + "test_results" 
            + fs + "CSourceToDepsBuilderTest" + fs + "buildTestResult.rsf";

        // Run CSourceToDepsBuilder.build()
        assertDoesNotThrow(() -> ( // to avoid exceptions stopping JUnit from running tests
            new CSourceToDepsBuilder()).build(classesDirPath, depsRsfFilename));
        String result = assertDoesNotThrow(() -> 
            { return FileUtil.readFile(depsRsfFilename, StandardCharsets.UTF_8); });
        
        // Load oracle file
        String oraclePath = "." + fs + "src" + fs + "test" + fs + "resources" 
            + fs +  "oracle_file_path_here.rsf";
        String oracleResult = assertDoesNotThrow(() ->
            { return FileUtil.readFile(oraclePath, StandardCharsets.UTF_8); });


        // Compare files
        RsfCompare resultRsf = new RsfCompare(result);
        RsfCompare oracleRsf = new RsfCompare(oracleResult);
        // RsfCompare.compareTo returns 0 if files have the same contents
        assertEquals(resultRsf.compareTo(oracleRsf), 0);
    }
}