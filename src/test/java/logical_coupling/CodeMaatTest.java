package logical_coupling;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import edu.usc.softarch.arcade.util.FileUtil;

public class CodeMaatTest {

    @ParameterizedTest
    @CsvSource({
        /** Test Parameters */
        // [project.log input file]
        // [project.csv oracle file]

        // httpd
        ".///src///test///resources///CodeMaatTest_resources///httpd///cleaned_httpd_project.log,"
        + ".///src///test///resources///CodeMaatTest_resources///httpd///httpd_oracle_project.csv,",

        // struts
        ".///src///test///resources///CodeMaatTest_resources///struts///cleaned_struts_project.log,"
        + ".///src///test///resources///CodeMaatTest_resources///struts///struts_oracle_project.csv,",
    })
    public void codeMaatTest(String logDir, String oracleDir){
        String logPath = FileUtil.tildeExpandPath(logDir.replace("///", File.separator));
        String oraclePath = FileUtil.tildeExpandPath(oracleDir.replace("///", File.separator));

        String codeMaatDir = ".///ext-tools///code-maat///code-maat-1.0-SNAPSHOT-standalone.jar";
        String codeMaatPath = FileUtil.tildeExpandPath(codeMaatDir.replace("///", File.separator));

        // use ProcessBuilder to create the output project.csv
        ProcessBuilder builder = new ProcessBuilder("java", "-jar", codeMaatPath, "-l", logPath, "-c", "git2", "-a", "coupling");
        Process process = assertDoesNotThrow(()->{
          return builder.start();
        });

        // read in the output from the process input stream
        Map<String, String> output_map = new HashMap<String, String>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));){
          String line;
          while ((line = br.readLine()) != null){
            String[] values = line.split(",");
            output_map.put(values[0]+values[1],values[2]+","+values[3]);
          }
        }catch(IOException e) {
            e.printStackTrace();
            fail("Exception caught in CodeMaatTest when reading in output from process");
        }

        // read in the oracle project.csv
        Map<String, String> oracle_map = new HashMap<String, String>();
        try (BufferedReader br = new BufferedReader(new FileReader(oraclePath));){
          //Map of key = {entity,coupled}, value  = {degree,average-revs}
          String line;
          while ((line = br.readLine()) != null){
            String[] values = line.split(",");
            oracle_map.put(values[0]+values[1],values[2]+","+values[3]);
          }
        }catch(IOException e) {
            e.printStackTrace();
            fail("Exception caught in CodeMaatTest when reading in oracle file");
        }
        
        // check if oracle and generated output match
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
