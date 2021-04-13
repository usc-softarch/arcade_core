package logical_coupling;


import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;


public class CleanUpCodeMaatTest {

  @ParameterizedTest
  @CsvSource({
    /** Test parameters **/
    // [oracle file path]
    // [directory of the file containing the csv]

    // ACDC Struts2 
    ".///src///test///resources///CleanUpCodeMaat_resources///oracle_project_struts_.csv,"
    + ".///src///test///resources///CleanUpCodeMaat_resources///",
  })
  public void cleanUpCodeMaatTest(String oracle_path, String csv_dir){
    String output_csv = ".///src///test///resources///CleanUpCodeMaat_resources///project_struts_clean.csv";

    String oraclePath = oracle_path.replace("///", File.separator);  
    String csvDir = csv_dir.replace("///", File.separator);
    String outputCsv = output_csv.replace("///", File.separator);

    String[] args = {csvDir};

    Map<String, String> oracle_map = new HashMap<String, String>();
    Map<String, String> output_map = new HashMap<String, String>();
  
    //Try to read in oracle csv
    try (BufferedReader br = new BufferedReader(new FileReader(oraclePath));){
      //Map of key = {entity,coupled}, value  = {degree,average-revs}
      String line;
      while ((line = br.readLine()) != null){
        String[] values = line.split(",");
        oracle_map.put(values[0]+values[1],values[2]+","+values[3]);
      }
    }catch(IOException e) {
        e.printStackTrace();
        fail("failed to read in oracle file");
    }

    cleanUpCodeMaat.main(args);

    //Try to read in output csv
    try (BufferedReader br = new BufferedReader(new FileReader(outputCsv));){
      //Map of key = {entity,coupled}, value  = {degree,average-revs}
      String line;
      while ((line = br.readLine()) != null){
        String[] values = line.split(",");
        output_map.put(values[0]+values[1],values[2]+","+values[3]);
      }
    }catch(IOException e) {
        e.printStackTrace();
        fail("failed to read in output file");
    }

    for (Map.Entry<String,String> mapElement  : oracle_map.entrySet()){
      String key = (String)mapElement.getKey();
      if(key.equals("entitycoupled")){
        continue;
      }
      String output_value = output_map.get(key);
      if(output_value == null){
        fail("output does not contain key: " + key);
      }
      String values[] = ((String)mapElement.getValue()).split(",");
      int oracle_degree = Integer.parseInt(values[0]);
      int oracle_avg_revs = Integer.parseInt(values[1]);
      
      String output_values[] = ((String)output_value).split(",");
      int output_degree = Integer.parseInt(output_values[0]);
      int output_avg_revs = Integer.parseInt(output_values[1]);

      assertAll(
        () -> assertEquals(oracle_degree, output_degree, "Output degree does not match the oracle for key" + key),
        () -> assertEquals(oracle_avg_revs, output_avg_revs, "Output average-revs does not match the oracle for key" + key)
      );
    }

  }
}
