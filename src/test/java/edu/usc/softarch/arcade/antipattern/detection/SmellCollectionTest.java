package edu.usc.softarch.arcade.antipattern.detection;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;

import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import edu.usc.softarch.arcade.antipattern.Smell;
import edu.usc.softarch.arcade.antipattern.SmellCollection;
import edu.usc.softarch.arcade.antipattern.Smell.SmellType;

public class SmellCollectionTest {
    char fs = File.separatorChar;
    // part 1 Test on serialization
    
    @ParameterizedTest
    @CsvSource({
        // Single buo 1
        ".///src///test///resources///SmellCollectionTest_resources///UnitTest///Smell1.ser,"
        + "1",
        // Single buo 2
        ".///src///test///resources///SmellCollectionTest_resources///UnitTest///Smell2.ser,"
        + "1",
        // multiple buo
        ".///src///test///resources///SmellCollectionTest_resources///UnitTest///Smell3.ser,"
        + "5"
    })
    public void smellCollectionTest_Unit_BUO123(
            String serFilePath, String quantityString) {
        String serFile = serFilePath.replace("///", File.separator);
        int quantity = Integer.parseInt(quantityString);
        
        SmellCollection Smells = assertDoesNotThrow(() -> {
            return new SmellCollection(serFile);
        });
        assertEquals(quantity, Smells.size());
        for (Smell smell : Smells) {
            assertEquals(SmellType.buo, smell.getSmellType());
            assertEquals(-1, smell.getTopicNum());
        }
    }

    @Test
    // Mix buo with spf
    public void smellCollectionTest_Unit_BUO4() {
        String serFile = "." + fs + "src" + fs + "test" + fs + "resources" + fs + "SmellCollectionTest_resources" + fs + "UnitTest" + fs + "Smell4.ser";
        
        SmellCollection Smells = assertDoesNotThrow(() -> {
            return new SmellCollection(serFile);
        });
        assertEquals(2, Smells.size());
        for (Smell smell : Smells) {
            if (smell.getSmellType() != SmellType.buo)
                assertEquals(SmellType.spf, smell.getSmellType());
            if (smell.getSmellType() != SmellType.spf)
                assertEquals(SmellType.buo, smell.getSmellType());
            assertEquals(-1, smell.getTopicNum());
        }
    }

    @Test
    // Single spf 1
    public void smellCollectionTest_Unit_SPF1() {
        String serFile = "." + fs + "src" + fs + "test" + fs + "resources" + fs + "SmellCollectionTest_resources" + fs + "UnitTest" + fs + "Smell5.ser";
        
        SmellCollection Smells = assertDoesNotThrow(() -> {
            return new SmellCollection(serFile);
        });
        assertEquals(1, Smells.size());
        for (Smell smell : Smells) {
            assertEquals(SmellType.spf, smell.getSmellType());
            assertEquals(-1, smell.getTopicNum());
        }
    }

    @Test
    // Single spf 1
    public void smellCollectionTest_Unit_BDC1() {
        String serFile = "." + fs + "src" + fs + "test" + fs + "resources" + fs + "SmellCollectionTest_resources" + fs + "UnitTest" + fs + "Smell6.ser";
        
        SmellCollection Smells = assertDoesNotThrow(() -> {
            return new SmellCollection(serFile);
        });
        assertEquals(1, Smells.size());
        for (Smell smell : Smells) {
            assertEquals(SmellType.bdc, smell.getSmellType());
            assertEquals(-1, smell.getTopicNum());
        }
    }

    @Test
    // Single spf 1
    public void smellCollectionTest_Unit_BCO1() {
        String serFile = "." + fs + "src" + fs + "test" + fs + "resources" + fs + "SmellCollectionTest_resources" + fs + "UnitTest" + fs + "Smell7.ser";
        
        SmellCollection Smells = assertDoesNotThrow(() -> {
            return new SmellCollection(serFile);
        });
        assertEquals(1, Smells.size());
        for (Smell smell : Smells) {
            assertEquals(SmellType.bco, smell.getSmellType());
            assertEquals(-1, smell.getTopicNum());
        }
    }

    @Test
    // pressure spf 1
    public void smellCollectionTest_Unit_BCO_Pressure() {
        String serFile = "." + fs + "src" + fs + "test" + fs + "resources" + fs + "SmellCollectionTest_resources" + fs + "UnitTest" + fs + "Smell8.ser";
        
        SmellCollection Smells = assertDoesNotThrow(() -> {
            return new SmellCollection(serFile);
        });
        assertEquals(14, Smells.size());
        for (Smell smell : Smells) {
            assertEquals(SmellType.bco, smell.getSmellType());
            assertEquals(-1, smell.getTopicNum());
        }
    }

    @Test
    // all smell
    public void smellCollectionTest_Unit_allSmell() {
        String serFile = "." + fs + "src" + fs + "test" + fs + "resources" + fs + "SmellCollectionTest_resources" + fs + "UnitTest" + fs + "Smell9.ser";
        
        SmellCollection Smells = assertDoesNotThrow(() -> {
            return new SmellCollection(serFile);
        });
        assertEquals(4, Smells.size());
        int bco = 0;
        int buo = 0;
        int spf = 0;
        int bdc = 0;
        for (Smell smell : Smells) {
            if (smell.getSmellType() == SmellType.bco) {
                bco += 1;
            }
            else if (smell.getSmellType() == SmellType.bdc) {
                bdc += 1;
            }
            else if (smell.getSmellType() == SmellType.buo) {
                buo += 1;
            }
            else if (smell.getSmellType() == SmellType.spf) {
                spf += 1;
            }
        }
        assertEquals(1, bco);
        assertEquals(1, buo);
        assertEquals(1, spf);
        assertEquals(1, bdc);
    }

    @Test
    // all smell
    public void smellCollectionTest_Unit_allSmell2() {
        String serFile = "." + fs + "src" + fs + "test" + fs + "resources" + fs + "SmellCollectionTest_resources" + fs + "UnitTest" + fs + "Smell10.ser";
        
        SmellCollection Smells = assertDoesNotThrow(() -> {
            return new SmellCollection(serFile);
        });
        assertEquals(6, Smells.size());
        int bco = 0;
        int buo = 0;
        int spf = 0;
        int bdc = 0;
        for (Smell smell : Smells) {
            if (smell.getSmellType() == SmellType.bco) {
                bco += 1;
            }
            else if (smell.getSmellType() == SmellType.bdc) {
                bdc += 1;
            }
            else if (smell.getSmellType() == SmellType.buo) {
                buo += 1;
            }
            else if (smell.getSmellType() == SmellType.spf) {
                spf += 1;
            }
        }
        assertEquals(1, bco);
        assertEquals(1, buo);
        assertEquals(1, spf);
        System.out.println(bdc);
        assertEquals(3, bdc);
    }

    @ParameterizedTest
    @CsvSource({
        //TODO docs
        ".///src///test///resources///SmellCollectionTest_resources///struts-2.5.2_acdc_smells.ser,"
        + ".///src///test///resources///SmellCollectionTest_resources///serialized///struts-2.5.2_acdc_smells_serialized.ser",
        //TODO docs
        ".///src///test///resources///SmellCollectionTest_resources///httpd-2.4.26_acdc_smells.ser,"
        + ".///src///test///resources///SmellCollectionTest_resources///serialized///httpd-2.4.26_acdc_smells_serialized.ser",
        //TODO docs
        ".///src///test///resources///SmellCollectionTest_resources///httpd-2.3.8_acdc_smells.ser,"
        + ".///src///test///resources///SmellCollectionTest_resources///serialized///httpd-2.3.8_acdc_smells_serialized.ser",
        //TODO docs
        ".///src///test///resources///SmellCollectionTest_resources///struts-2.3.30_acdc_smells.ser,"
        + ".///src///test///resources///SmellCollectionTest_resources///serialized///struts-2.3.30_acdc_smells_serialized.ser"
    })
    // part 2 serialize then deserialize
    public void smellCollectionTest_STD(String serFile, String serializedFile){       
        String serFile_final = serFile.replace("///", File.separator);  
		String serializedFile_final = serializedFile.replace("///", File.separator);

        SmellCollection Smells = assertDoesNotThrow(() -> {
            return new SmellCollection(serFile_final);
        });
        
        assertDoesNotThrow(() -> {
            Smells.serializeSmellCollection(serializedFile_final);
        });

        SmellCollection SerializedSmells = assertDoesNotThrow(() -> {
            return new SmellCollection(serializedFile_final);
        });
        
        assertEquals(SerializedSmells, Smells);
    }
}