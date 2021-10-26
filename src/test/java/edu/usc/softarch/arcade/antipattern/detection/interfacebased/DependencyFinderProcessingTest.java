package edu.usc.softarch.arcade.antipattern.detection.interfacebased;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class DependencyFinderProcessingTest {
	@ParameterizedTest
	@CsvSource({
		/** Test parameters - same as DependencyFinderProcessing.main() **/
		// Path to clustered RSF files directory
		// Path to DepFinder XML files directory
		// Path to clones XML files directory
		// Path to _clean.csv file
		// Fully qualified name of the package under analysis
		// Path to the output file
		// Path to oracle
		
		// nutch 1.7
		".///src///test///resources///DependencyFinderProcessingTest_resources///nutch-1.7///acdc///cluster,"
		+ ".///src///test///resources///DependencyFinderProcessingTest_resources///nutch-1.7///depfinder,"
		+ ".///src///test///resources///DependencyFinderProcessingTest_resources///nutch-1.7///clone,"
		+ ".///src///test///resources///DependencyFinderProcessingTest_resources///nutch-1.7///codemaat///nutch-1.7_clean.csv,"
		+ "org.apache.nutch,"
		+ ".///target///test_results///DependencyFinderProcessingTest///nutch-1.7_acdc_interface_smell.csv,"
		+ ".///src///test///resources///DependencyFinderProcessingTest_resources///json///nutch-1.7_acdc_interface_smell.json",

		// nutch 1.8
		".///src///test///resources///DependencyFinderProcessingTest_resources///nutch-1.8///acdc///cluster,"
		+ ".///src///test///resources///DependencyFinderProcessingTest_resources///nutch-1.8///depfinder,"
		+ ".///src///test///resources///DependencyFinderProcessingTest_resources///nutch-1.8///clone,"
		+ ".///src///test///resources///DependencyFinderProcessingTest_resources///nutch-1.8///codemaat///nutch-1.8_clean.csv,"
		+ "org.apache.nutch,"
		+ ".///target///test_results///DependencyFinderProcessingTest///nutch-1.8_acdc_interface_smell.csv,"
		+ ".///src///test///resources///DependencyFinderProcessingTest_resources///json///nutch-1.8_acdc_interface_smell.json",

		// nutch 1.9
		".///src///test///resources///DependencyFinderProcessingTest_resources///nutch-1.9///acdc///cluster,"
		+ ".///src///test///resources///DependencyFinderProcessingTest_resources///nutch-1.9///depfinder,"
		+ ".///src///test///resources///DependencyFinderProcessingTest_resources///nutch-1.9///clone,"
		+ ".///src///test///resources///DependencyFinderProcessingTest_resources///nutch-1.9///codemaat///nutch-1.9_clean.csv,"
		+ "org.apache.nutch,"
		+ ".///target///test_results///DependencyFinderProcessingTest///nutch-1.9_acdc_interface_smell.csv,"
		+ ".///src///test///resources///DependencyFinderProcessingTest_resources///json///nutch-1.9_acdc_interface_smell.json",
	})
	public void mainTest(String rsf, String deps, String clone, String codemaat, String pkgName, String output, String oracle){
		// Parameter setup
		String clusterDir = rsf.replace("///", File.separator);
		String depfinderDir = deps.replace("///", File.separator);
		String cloneDir = clone.replace("///", File.separator);
		String cleanedCsv = codemaat.replace("///", File.separator);
		String outputFile = output.replace("///", File.separator);
		String oraclePath = oracle.replace("///", File.separator);
		
		// Create output directory if does not exist
		(new File("target" + File.separatorChar + "test_results" + File.separatorChar + "DependencyFinderProcessingTest")).mkdirs();

		// Call DependencyFinderProcessing.main()
		// Output json file will have path outputFile + ".json"
		assertDoesNotThrow( ()-> {
			DependencyFinderProcessing.main(new String[] {clusterDir, depfinderDir, cloneDir, cleanedCsv, pkgName, outputFile});
		});

		ObjectMapper objectMapper = new ObjectMapper();

		// Read in result json (outputFile + ".json")
		Map<String, Map<String, Map<String, Integer>>> outputMap = new HashMap<>();
		try {
			//Read json here
			outputMap = objectMapper.readValue(new File(outputFile + ".json"), new TypeReference<Map<String, Map<String, Map<String, Integer>>>>(){});

		} catch (IOException e) {
			e.printStackTrace();
			fail("DependencyFinderProcessingTest.java: Failed to read in output json");
		}

		// Read in oracle json (oraclePath)
		try {
			//Read json here
			Map<String, Map<String, Map<String, Integer>>> oracleMap = objectMapper.readValue(new File(oraclePath), new TypeReference<Map<String, Map<String, Map<String, Integer>>>>(){});

			// check sizes
			assertEquals(oracleMap.size(), outputMap.size(), "DependencyFinderProcessingTest.java: Failed due to mismatched map sizes");

			for (String versionKey : oracleMap.keySet()){
				// check sizes
				assertEquals(oracleMap.get(versionKey).size(), outputMap.get(versionKey).size(), "DependencyFinderProcessingTest.java: Failed due to mismatched map sizes");
				//This map includes the actual entries, which key: org.apache.nutch.... 
				//and value: Another map which has key: overload/Logical_Dependency/etc and some int value
				for(String classKey : oracleMap.get(versionKey).keySet()){		
					// check sizes
					assertEquals(oracleMap.get(versionKey).get(classKey).size(), outputMap.get(versionKey).get(classKey).size(), "DependencyFinderProcessingTest.java: Failed due to mismatched map sizes");

					for(String valueKey : oracleMap.get(versionKey).get(classKey).keySet()){
						assertEquals(oracleMap.get(versionKey).get(classKey).get(valueKey), outputMap.get(versionKey).get(classKey).get(valueKey));
					}
				}
            }
		} catch (IOException e) {
			e.printStackTrace();
			fail("DependencyFinderProcessingTest.java: Failed to read in oracle json");
		}
	}
}
