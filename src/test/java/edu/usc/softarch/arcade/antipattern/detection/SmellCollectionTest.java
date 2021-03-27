package edu.usc.softarch.arcade.antipattern.detection;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import com.thoughtworks.xstream.XStream;

import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import edu.usc.softarch.arcade.antipattern.Smell;
import edu.usc.softarch.arcade.antipattern.SmellCollection;
import edu.usc.softarch.arcade.antipattern.Smell.SmellType;
import edu.usc.softarch.arcade.clustering.acdc.ACDC;
import edu.usc.softarch.arcade.clustering.drivers.AcdcWithSmellDetection;
import fj.data.HashSet;

public class SmellCollectionTest {
    // part 1 Test on serialization
    
    @Test
    // Single buo 1
    public void smellCollectionTest_Unit_BUO1() {
        String serFile = ".\\src\\test\\resources\\SmellTest_resources\\UnitTest\\Smell1.ser";
        
        SmellCollection Smells = assertDoesNotThrow(() -> {
            return new SmellCollection(serFile);
        });
        assertTrue(Smells.size() == 1);
        for (Smell smell : Smells) {
            assertTrue(smell.getSmellType() == SmellType.buo);
            assertTrue(smell.getTopicNum() ==  -1);
        }
    }

    @Test
    // Single buo 2
    public void smellCollectionTest_Unit_BUO2() {
        String serFile = ".\\src\\test\\resources\\SmellTest_resources\\UnitTest\\Smell2.ser";
        
        SmellCollection Smells = assertDoesNotThrow(() -> {
            return new SmellCollection(serFile);
        });
        assertTrue(Smells.size() == 1);
        for (Smell smell : Smells) {
            assertTrue(smell.getSmellType() == SmellType.buo);
            assertTrue(smell.getTopicNum() ==  -1);
        }
    }

    @Test
    // multiple buo
    public void smellCollectionTest_Unit_BUO3() {
        String serFile = ".\\src\\test\\resources\\SmellTest_resources\\UnitTest\\Smell3.ser";
        
        SmellCollection Smells = assertDoesNotThrow(() -> {
            return new SmellCollection(serFile);
        });
        assertTrue(Smells.size() == 5);
        for (Smell smell : Smells) {
            assertTrue(smell.getSmellType() == SmellType.buo);
            assertTrue(smell.getTopicNum() ==  -1);
        }
    }

    @Test
    // Mix buo with spf
    public void smellCollectionTest_Unit_BUO4() {
        String serFile = ".\\src\\test\\resources\\SmellTest_resources\\UnitTest\\Smell4.ser";
        
        SmellCollection Smells = assertDoesNotThrow(() -> {
            return new SmellCollection(serFile);
        });
        assertTrue(Smells.size() == 2);
        for (Smell smell : Smells) {
            if (smell.getSmellType() != SmellType.buo) {
                assertTrue(smell.getSmellType() == SmellType.spf);
            }
            if (smell.getSmellType() != SmellType.spf) {
                assertTrue(smell.getSmellType() == SmellType.buo);
            }
            assertTrue(smell.getTopicNum() == -1);
        }
    }

    @Test
    // Single spf 1
    public void smellCollectionTest_Unit_SPF1() {
        String serFile = ".\\src\\test\\resources\\SmellTest_resources\\UnitTest\\Smell5.ser";
        
        SmellCollection Smells = assertDoesNotThrow(() -> {
            return new SmellCollection(serFile);
        });
        assertTrue(Smells.size() == 1);
        for (Smell smell : Smells) {
            assertTrue(smell.getSmellType() == SmellType.spf);
            assertTrue(smell.getTopicNum() ==  -1);
        }
    }

    @Test
    // Single spf 1
    public void smellCollectionTest_Unit_BDC1() {
        String serFile = ".\\src\\test\\resources\\SmellTest_resources\\UnitTest\\Smell6.ser";
        
        SmellCollection Smells = assertDoesNotThrow(() -> {
            return new SmellCollection(serFile);
        });
        assertTrue(Smells.size() == 1);
        for (Smell smell : Smells) {
            assertTrue(smell.getSmellType() == SmellType.bdc);
            assertTrue(smell.getTopicNum() ==  -1);
        }
    }

    @Test
    // Single spf 1
    public void smellCollectionTest_Unit_BCO1() {
        String serFile = ".\\src\\test\\resources\\SmellTest_resources\\UnitTest\\Smell7.ser";
        
        SmellCollection Smells = assertDoesNotThrow(() -> {
            return new SmellCollection(serFile);
        });
        assertTrue(Smells.size() == 1);
        for (Smell smell : Smells) {
            assertTrue(smell.getSmellType() == SmellType.bco);
            assertTrue(smell.getTopicNum() ==  -1);
        }
    }

    @Test
    // pressure spf 1
    public void smellCollectionTest_Unit_BCO_Pressure() {
        String serFile = ".\\src\\test\\resources\\SmellTest_resources\\UnitTest\\Smell8.ser";
        
        SmellCollection Smells = assertDoesNotThrow(() -> {
            return new SmellCollection(serFile);
        });
        assertTrue(Smells.size() == 14);
        for (Smell smell : Smells) {
            assertTrue(smell.getSmellType() == SmellType.bco);
            assertTrue(smell.getTopicNum() ==  -1);
        }
    }

    @Test
    // all smell
    public void smellCollectionTest_Unit_allSmell() {
        String serFile = ".\\src\\test\\resources\\SmellTest_resources\\UnitTest\\Smell9.ser";
        
        SmellCollection Smells = assertDoesNotThrow(() -> {
            return new SmellCollection(serFile);
        });
        assertTrue(Smells.size() == 4);
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
        assertTrue(bco == 1);
        assertTrue(buo == 1);
        assertTrue(spf == 1);
        assertTrue(bdc == 1);
    }

    @Test
    // all smell
    public void smellCollectionTest_Unit_allSmell2() {
        String serFile = ".\\src\\test\\resources\\SmellTest_resources\\UnitTest\\Smell10.ser";
        
        SmellCollection Smells = assertDoesNotThrow(() -> {
            return new SmellCollection(serFile);
        });
        assertTrue(Smells.size() == 6);
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
        assertTrue(bco == 1);
        assertTrue(buo == 1);
        assertTrue(spf == 1);
        System.out.println(bdc);
        assertTrue(bdc == 3);
    }

    @ParameterizedTest
    @CsvSource({
        ".\\src\\test\\resources\\SmellTest_resources\\struts-2.5.2_acdc_smells.ser" + ", " + ".\\src\\test\\resources\\SmellTest_resources\\serialized\\struts-2.5.2_acdc_smells_serialized.ser",
        ".\\src\\test\\resources\\SmellTest_resources\\httpd-2.4.26_acdc_smells.ser" + ", " + ".\\src\\test\\resources\\SmellTest_resources\\serialized\\httpd-2.4.26_acdc_smells_serialized.ser",
        ".\\src\\test\\resources\\SmellTest_resources\\httpd-2.3.8_acdc_smells.ser" + ", " + ".\\src\\test\\resources\\SmellTest_resources\\serialized\\httpd-2.3.8_acdc_smells_serialized.ser",
        ".\\src\\test\\resources\\SmellTest_resources\\struts-2.3.30_acdc_smells.ser" + ", " + ".\\src\\test\\resources\\SmellTest_resources\\serialized\\struts-2.3.30_acdc_smells_serialized.ser"
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
        
        assertTrue(Smells.equals(SerializedSmells));
    }
}
