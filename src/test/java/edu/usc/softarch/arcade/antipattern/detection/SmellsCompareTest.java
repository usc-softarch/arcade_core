package edu.usc.softarch.arcade.antipattern.detection;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.File;
import java.util.AbstractCollection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import edu.usc.softarch.arcade.antipattern.Smell;
import edu.usc.softarch.arcade.antipattern.SmellCollection;
import edu.usc.softarch.arcade.clustering.acdc.ACDC;

public class SmellsCompareTest{
  String OraclePath;
  String source_deps_rsf_path;
  String ACDC_output_cluster_path;
  String targetSerFilename;
  ArchSmellDetector asd;

  @BeforeEach
  public void setUp(){
    char fs = File.separatorChar;

    OraclePath = "." + fs + "src" + fs + "test" + fs + "resources"
      + fs + "ACDCTest_resources"
      + fs + "struts-2.3.30_acdc_smells.ser";

    source_deps_rsf_path = "." + fs + "src" + fs + "test" + fs + "resources"
    + fs + "JavaSourceToDepsBuilderTest_resources" + fs +"struts-2.3.30_deps.rsf";

    ACDC_output_cluster_path = "." + fs + "target" + fs + "ACDC_test_results" + fs + "test_old_deps_acdc_clustered.rsf";

    assertDoesNotThrow(() ->  ACDC.run(source_deps_rsf_path, ACDC_output_cluster_path));

    String targetSerPath = "." + fs + "target" + fs + "test_results";

    File directory = new File(targetSerPath);
    directory.mkdirs();

    targetSerFilename = "." + fs + "target" + fs + "test_results" + fs 
      + "ACDC_test_compare_smells_with_concerns.ser";
  
    asd = new ArchSmellDetector(source_deps_rsf_path,
      ACDC_output_cluster_path, targetSerFilename);
  }

  //These two tests below will fail because there are problems with the generated .ser files which does not match the oracles.


    @ParameterizedTest
    @CsvSource({
      // Test with concerns
      "true",
      //Without concerns
      "false"
    })
    public void test_ACDC_compare_smells(String concerns){       
          
            assertDoesNotThrow(() -> asd.run(true, Boolean.parseBoolean(concerns), true)); 
            SmellCollection OracleSmells = assertDoesNotThrow(() -> {
              return new SmellCollection(OraclePath);
            });
            SmellCollection TargetSmells = assertDoesNotThrow(() -> {
              return new SmellCollection(targetSerFilename);
            });

            AbstractCollection<Smell> OracleAbs = (AbstractCollection<Smell>)OracleSmells;
            AbstractCollection<Smell> TargetAbs = (AbstractCollection<Smell>)TargetSmells;
  
            assertTrue(OracleAbs.containsAll(TargetAbs));
    }

}