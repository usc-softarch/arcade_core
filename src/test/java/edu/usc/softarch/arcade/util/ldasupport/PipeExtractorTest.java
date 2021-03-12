package edu.usc.softarch.arcade.util.ldasupport;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;

public class PipeExtractorTest {
    // Get and save the present working directory
    String pwd = System.getProperty("user.dir");

    @BeforeEach
    public void setUp(){
        char fs = File.separatorChar;
        // Make sure directory for test output exists (and create if it doesn't)
        String outputPath = "." + fs + "target" + fs + "test_results" + fs + "PipeExtractorTest";
        (new File(outputPath)).mkdirs();

        // Change the present working directory to location of stoplists/keywords
        System.setProperty("user.dir", pwd + fs + "src" + fs + "main" + fs + "resources" + fs);
    }

    @Test
    public void mainTest(){
        /** Integration test for PipeExtractor **/
        String classesDir = pwd + "/src/test/resources/JavaSourceToDepsBuilderTest_resources/binaries/struts-2.3.30/lib_struts";
        String resultDir = pwd + "/target/test_results/PipeExtractorTest/";
        // Path to oracle pipe file
        String oraclePath = pwd + "/src/test/resources/PipeExtractorTest_resources/Struts2/arc/base/struts-2.3.30/output.pipe";
        
        System.out.println(System.getProperty("user.dir"));

        // Call PipeExtractor.main() 
        // (arguments: sys version dir, output dir, selected language)
        assertDoesNotThrow( () -> {
            PipeExtractor.main(new String[] {classesDir, resultDir, "java"});
        });

        // Read result instances into a set
        InstanceList resultInstances = InstanceList.load(new File(resultDir + "output.pipe"));
        Set<Instance> result = new HashSet<>();
        for (Instance i : resultInstances) {
            result.add(i);
        }
        // Read oracle instances into a set
        InstanceList oracleInstances = InstanceList.load(new File(oraclePath));
        Set<Instance> oracle = new HashSet<>();
        for (Instance i : oracleInstances) {
            oracle.add(i);
        }

        // Compare sets of instances
        assertTrue(oracle.equals(result));
    }

    @AfterEach
    public void cleanUp(){
        // Reset working directory to repo root
        System.setProperty("user.dir", pwd);
    }
}
