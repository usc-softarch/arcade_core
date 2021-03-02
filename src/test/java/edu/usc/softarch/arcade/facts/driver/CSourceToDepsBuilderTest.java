package edu.usc.softarch.arcade.facts.driver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.File;
import java.nio.charset.StandardCharsets;

import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import edu.usc.softarch.arcade.util.FileUtil;
import edu.usc.softarch.arcade.util.RsfCompare;

public class CSourceToDepsBuilderTest {
    // @Before
    // public void setUp(){
    //   // Create ./target/test_results/CSourceToDepsBuilderTest/ if it does not already exist
    //   // Note: I guess this technically should happen in the build() function itself
    //   // (because that's how it is in the Java one)
    //   char fs = File.separatorChar;
    //   String outputPath = FileUtil.tildeExpandPath("." + fs + "target" + fs + "test_results" + fs + "CSourceToDepsBuilderTest");
    //   (new File(outputPath)).getParentFile().mkdirs();
    // }

    @ParameterizedTest
    @CsvSource({
      // httpd 2.3.8
      ".///src///test///resources///CSourceToDepsBuilderTest_resources///binaries///httpd-2.3.8,"
      + ".///target///test_results///CSourceToDepsBuilderTest///httpd-2.3.8buildTestResult.rsf,"
      + ".///src///test///resources///CSourceToDepsBuilderTest_resources///httpd-2.3.8_deps.rsf",
      // httpd 2.4.26
      ".///src///test///resources///CSourceToDepsBuilderTest_resources///binaries///httpd-2.4.26,"
      + ".///target///test_results///CSourceToDepsBuilderTest///httpd 2.4.26buildTestResult.rsf,"
      + ".///src///test///resources///CSourceToDepsBuilderTest_resources///httpd 2.4.26_deps.rsf",
    })
    public void buildTest(String classesDirPath, String depsRsfFilename, String oraclePath){
        /** Builds the dependencies RSF file for C system **/
        // Format the paths properly
        String classes = classesDirPath.replace("///", File.separator);
        String deps = depsRsfFilename.replace("///", File.separator);
        String oracle = oraclePath.replace("///", File.separator);

        // Run CSourceToDepsBuilder.build()
        assertDoesNotThrow(() -> (new CSourceToDepsBuilder()).build(classes, deps));
        String result = assertDoesNotThrow(() ->
            { return FileUtil.readFile(deps, StandardCharsets.UTF_8); });

        // Load oracle
        String oracleResult = assertDoesNotThrow(() ->
            { return FileUtil.readFile(oracle, StandardCharsets.UTF_8); });

        // Use RsfCompare.compareTo to compare file contents
            // returns 0 if files have the same contents
        RsfCompare resultRsf = new RsfCompare(result);
        RsfCompare oracleRsf = new RsfCompare(oracleResult);
        assertEquals(0, oracleRsf.compareTo(resultRsf));
    }
}