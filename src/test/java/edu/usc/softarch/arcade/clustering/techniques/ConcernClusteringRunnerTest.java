package edu.usc.softarch.arcade.clustering.techniques;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import edu.usc.softarch.arcade.facts.driver.CSourceToDepsBuilder;
import edu.usc.softarch.arcade.facts.driver.JavaSourceToDepsBuilder;
import edu.usc.softarch.arcade.facts.driver.SourceToDepsBuilder;

public class ConcernClusteringRunnerTest {
    // members to check: 
        // public static FastClusterArchitecture fastClusters;
        // protected static ArrayList<Cluster> clusters;
        // protected static FastFeatureVectors fastFeatureVectors;
    @ParameterizedTest
    @CsvSource({
        // Test parameters (5): 
        // [directory with single sys version], 
        // [subdirectory (of the previous argument) containing the binaries], 
        // [desired output directory], 
        // [system language],
        // [desired name for dependencies rsf]
        ".///src///test///resources///JavaSourceToDepsBuilderTest_resources///binaries///struts-2.3.30,"
        + "lib_struts,"
        + ".///target///test_results///ConcernClusteringRunnerTest,"
        + "java,"
        + "struts-2.3.30_ccr_test.rsf",
    })
    public void updateFastClustersAndSimMatrixToReflectMergedClusterTest(String versionDir, String inClassesDir, String outputDir, String language, String depsRsf){
        char fs = File.separatorChar;
        String outputPath = "." + fs + "target" + fs + "test_results" + fs + "ConcernClusteringRunnerTest";
        (new File(outputPath)).mkdirs();
        
        String fullSrcDir = versionDir.replace("///", File.separator);
        String outputDirName = outputDir.replace("///", File.separator);
        
        // Construct builder object
        SourceToDepsBuilder builder = new JavaSourceToDepsBuilder();;
        if (language.equals("c")){
            builder = new CSourceToDepsBuilder();
        }
        else if (!language.equals("java")) { // fail test if not java or c
            fail("invalid language given as test parameter: must be \"java\" or \"c\"");
        }

        // assertDoesNotThrow with builder? will figure this out later
        try{
            builder.build(fullSrcDir + File.separator + inClassesDir, outputDirName + File.separator + depsRsf);
        } catch (IOException ioe){
            ioe.printStackTrace();
        }
        // Construct a ConcernClusteringRunnerObject
        ConcernClusteringRunner runner = new ConcernClusteringRunner(
            builder.getFfVecs(), fullSrcDir, outputDirName + "/base", language);
        
        // Serialize runner.fastClusters, runner.clusters, runner.fastfeaturevectors
        
    }

    // TEST ALL METHODS THAT MODIFY FastClusterArchitecture fastClusters
    // updateFastClustersAndSimMatrixToReflectMergedCluster (need to test) - called by computeClustersWithConcernsAndFastClusters 
    // initializeDocTopicsForEachFastCluster --> lots of stuff happens here, test!
    // updateFastClustersAndSimMatrixToReflectMergedCluster

}
