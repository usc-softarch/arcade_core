package edu.usc.softarch.arcade.facts.driver;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class RsfReaderTest {
    
    @CsvSource({   
        // httpd cluster file
        "src///test///resources///RsfReader_resouces///httpd-2.3.8_acdc_clustered.rsf",
        // struts cluster file
        "src///test///resources///RsfReader_resouces///struts-2.3.30_acdc_clustered.rsf"
    })
    @ParameterizedTest
    public void loadRsfDataFromFileTest(String fileName){
        String rsfFile = fileName.replace("///", File.separator);

        // make sure the file exists
        File file = new File(fileName);
        assertTrue(file.exists());
        
        // make sure the file is not empty
        assertNotEquals(0, file.length());

        RsfReader.loadRsfDataFromFile(rsfFile);
        List<List<String>> list = RsfReader.unfilteredFaCtS;

        // make sure list is populated
        assertTrue(list.size() > 0);
    }

    @Test
    public void loadRsfDataFromEmptyFileTest(){
        String fileName = "src///test///resources///RsfReader_resouces///emptyRsf.rsf";
        String rsfFile = fileName.replace("///", File.separator);
        
        // make sure the file exists
        File file = new File(fileName);
        assertTrue(file.exists());

        RsfReader.loadRsfDataFromFile(rsfFile);
        List<List<String>> list = RsfReader.unfilteredFaCtS;

        // empty rsf should have zero clusters
        assertEquals(0, list.size());
    }

    @Test
    public void loadRsfDataFromMissingFileTest(){
        String fileName = "src///test///resources///RsfReader_resouces///nonexistent.rsf";
        String rsfFile = fileName.replace("///", File.separator);

        // check that an NullPointerException is thrown when trying to load from nonexistent file
        assertThrows(FileNotFoundException.class, () -> {
            RsfReader.loadRsfDataFromFile(rsfFile);
        });
    }
}