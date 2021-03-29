package edu.usc.softarch.arcade.clustering;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;

import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class ConcernClusterArchitectureTest {
    
    @CsvSource({   
        // httpd cluster file
        "src///test///resources///ConcernClusterArchitectureTest_resources///httpd-2.3.8_acdc_clustered.rsf",
        // struts cluster file
        "src///test///resources///ConcernClusterArchitectureTest_resources///struts-2.3.30_acdc_clustered.rsf"
    })
    @ParameterizedTest
    public void loadFromRsfTest(String fileName){
        String rsfFile = fileName.replace("///", File.separator);

        // make sure the file exists
        File file = new File(fileName);
        assertTrue(file.exists());
        
        // make sure the file is not empty
        assertTrue(file.length() != 0);

        ConcernClusterArchitecture clusters = ConcernClusterArchitecture.loadFromRsf(rsfFile);

        // make sure there are clusters
        assertTrue(clusters.size() > 0);
    }

    @Test
    public void loadFromEmptyRsfTest(){
        String fileName = "src///test///resources///ConcernClusterArchitectureTest_resources///emptyRsf.rsf";
        String rsfFile = fileName.replace("///", File.separator);
        
        // make sure the file exists
        File file = new File(fileName);
        assertTrue(file.exists());

        ConcernClusterArchitecture clusters = ConcernClusterArchitecture.loadFromRsf(rsfFile);

        // empty rsf should have zero clusters
        assertTrue(clusters.size() == 0);
    }

    @Test
    public void loadFromMissingRsfTest(){
        String fileName = "src///test///resources///ConcernClusterArchitectureTest_resources///nonexistent.rsf";
        String rsfFile = fileName.replace("///", File.separator);

        // check that an NullPointerException is thrown when trying to load from nonexistent file
        assertThrows(NullPointerException.class, () -> {
            ConcernClusterArchitecture.loadFromRsf(rsfFile);
        });
    }
}
