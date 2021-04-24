package edu.usc.softarch.arcade.antipattern.detection.interfacebased;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class DependencyFinderProcessingTest {
	@ParameterizedTest
	@CsvSource({
		/** Test parameters - same as DependencyFinderProcessing.main() **/
		// Root system directory
		// Path to clustered RSF files directory
		// Path to DepFinder XML files directory
		// Path to clones XML files directory
		// Path to _clean.csv file
		// Fully qualified name of the package under analysis
		// Path to the output file
		
		// nutch 1.7
		".///src///test///resources///subject_systems_resources///nutch///src///nutch-1.7,"
		+ ".///src///test///resources///DependencyFinderProcessingTest_resources///nutch-1.7///acdc///cluster,"
		+ ".///src///test///resources///DependencyFinderProcessingTest_resources///nutch-1.7///depfinder,"
		+ ".///src///test///resources///DependencyFinderProcessingTest_resources///nutch-1.7///clone,"
		+ ".///src///test///resources///DependencyFinderProcessingTest_resources///nutch-1.7///codemaat///nutch-1.7_clean.csv,"
		+ "org.apache.nutch,"
		+ ".///target///test_results///DependencyFinderProcessingTest///nutch-1.7_acdc_interface_smell.csv,"
		+ ".///src///test///resources///DependencyFinderProcessingTest_resources///json///nutch-1.7_acdc_interface_smell.json",

		// nutch 1.8
		".///src///test///resources///subject_systems_resources///nutch///src///nutch-1.8,"
		+ ".///src///test///resources///DependencyFinderProcessingTest_resources///nutch-1.8///acdc///cluster,"
		+ ".///src///test///resources///DependencyFinderProcessingTest_resources///nutch-1.8///depfinder,"
		+ ".///src///test///resources///DependencyFinderProcessingTest_resources///nutch-1.8///clone,"
		+ ".///src///test///resources///DependencyFinderProcessingTest_resources///nutch-1.8///codemaat///nutch-1.8_clean.csv,"
		+ "org.apache.nutch,"
		+ ".///target///test_results///DependencyFinderProcessingTest///nutch-1.8_acdc_interface_smell.csv,"
		+ ".///src///test///resources///DependencyFinderProcessingTest_resources///json///nutch-1.8_acdc_interface_smell.json",

		// nutch 1.9
		".///src///test///resources///subject_systems_resources///nutch///src///nutch-1.9,"
		+ ".///src///test///resources///DependencyFinderProcessingTest_resources///nutch-1.9///acdc///cluster,"
		+ ".///src///test///resources///DependencyFinderProcessingTest_resources///nutch-1.9///depfinder,"
		+ ".///src///test///resources///DependencyFinderProcessingTest_resources///nutch-1.9///clone,"
		+ ".///src///test///resources///DependencyFinderProcessingTest_resources///nutch-1.9///codemaat///nutch-1.9_clean.csv,"
		+ "org.apache.nutch,"
		+ ".///target///test_results///DependencyFinderProcessingTest///nutch-1.9_acdc_interface_smell.csv,"
		+ ".///src///test///resources///DependencyFinderProcessingTest_resources///json///nutch-1.9_acdc_interface_smell.json",
	})
	public void mainTest(String sys, String rsf, String deps, String clone, String codemaat, String pkgName, String output, String oracle){
		// Parameter setup
		String sysDir = sys.replace("///", File.separator);
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
			DependencyFinderProcessing.main(new String[] {sysDir, clusterDir, depfinderDir, cloneDir, cleanedCsv, pkgName, outputFile});
		});

		// Read in oracle json (oraclePath)
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			//Read json here
			Map<String, Object> map = objectMapper.readValue(new File(oraclePath),
			new TypeReference<Map<String,Object>>(){});

			for (Entry<String, Object> mapElement  : map.entrySet()){
				Map<String,Object> entries = (Map<String,Object>)mapElement.getValue();		//This map is just to rip off the version key/value pair

				//This map includes the actual entries, which key: org.apache.nutch.... 
				//and value: Another map which has key: overload/Logical_Dependency/etc and some int value
				for(Entry<String,Object> entriesElement : entries.entrySet()){		
					Map<String,Integer> entry = (Map<String,Integer>)entriesElement.getValue();
					for(Entry<String,Integer> entryElement : entry.entrySet()){
						//Compare values here
					}
				}
            }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Read in result json (outputFile + ".json")
		try {
			//Read json here
			Map<String, Object> map = objectMapper.readValue(new File(outputFile + ".json"),
			new TypeReference<Map<String,Object>>(){});

			for (Entry<String, Object> mapElement  : map.entrySet()){
				//This map is just to rip off the version key/value pair
				Map<String,Object> entries = (Map<String,Object>)mapElement.getValue();		

				//This map includes the actual entries, which key: org.apache.nutch.... 
				//and value: Another map which has key: overload/Logical_Dependency/etc and some int value
				for(Entry<String,Object> entriesElement : entries.entrySet()){		
					System.out.println("In entry: " + entriesElement.getKey());
					Map<String,Integer> entry = (Map<String,Integer>)entriesElement.getValue();
					for(Entry<String,Integer> entryElement : entry.entrySet()){
						//Compare values here
					}
				}
            }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Compare oracle to result (assertions)
	}
}
