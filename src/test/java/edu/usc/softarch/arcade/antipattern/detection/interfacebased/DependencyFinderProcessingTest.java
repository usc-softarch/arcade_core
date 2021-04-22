package edu.usc.softarch.arcade.antipattern.detection.interfacebased;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.File;

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

		// Read in result json (outputFile + ".json")

		// Compare oracle to result (assertions)
	}
}
