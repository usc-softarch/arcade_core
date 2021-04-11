package edu.usc.softarch.arcade.metrics;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class BatchSystemEvoTest {
	private String outputPath;
	private char fs; 

	@BeforeEach
	public void setUp(){
		fs = File.separatorChar;
		outputPath = "." + fs + "target" + fs + "test_results" + fs + "BatchSystemEvoTest";
		File directory = new File(outputPath);
		outputPath = outputPath + fs;
		directory.mkdirs();
	}

  @ParameterizedTest
	@CsvSource({
		/** Test parameters **/
		// [oracle txt file path]
		// [clusters rsf directory]
		// [Statistics output path]

		// ACDC Struts2 
		".///src///test///resources///System_evo_resources///outputs_acdc///Struts2_oracle.csv,"
		+ ".///src///test///resources///System_evo_resources///cluster_files_acdc///Struts2",

		// ACDC httpd
		".///src///test///resources///System_evo_resources///outputs_acdc///httpd_oracle.csv,"
		+ ".///src///test///resources///System_evo_resources///cluster_files_acdc///httpd",

		// ARC Struts2
		".///src///test///resources///System_evo_resources///outputs_arc///Struts2_oracle.csv,"
		+ ".///src///test///resources///System_evo_resources///cluster_files_arc///Struts2",

		// ARC httpd
		".///src///test///resources///System_evo_resources///outputs_arc///httpd_oracle.csv,"
		+ ".///src///test///resources///System_evo_resources///cluster_files_arc///httpd",
	})
	public void batchSystemEvoTest(String oracle_path, String clusters_dir){
		String oraclePath = oracle_path.replace("///", File.separator);  
		String clustersDir = clusters_dir.replace("///", File.separator);
		String[] args = {clustersDir,outputPath};

		assertDoesNotThrow(() -> BatchSystemEvo.main(args));

		

		try {
			HashMap<String, Double> oracle_map = new HashMap<String, Double>();
			BufferedReader br = new BufferedReader(new FileReader(oraclePath));  
			DescriptiveStatistics output_stats = BatchSystemEvo.getStats();
			String line;
			while ((line = br.readLine()) != null){
				String arr[] = line.split(":");
				oracle_map.put(arr[0],Double.parseDouble(arr[1]));
			}
			br.close();

			assertAll(
				() -> assertEquals(oracle_map.get("n"), (double)(output_stats.getN()),"N does not match the oracle"),
				() -> assertEquals(oracle_map.get("min"), output_stats.getMin(),"Min does not match the oracle"),
				() -> assertEquals(oracle_map.get("max"), output_stats.getMax(),"Max does not match the oracle"),
				() -> assertEquals(oracle_map.get("mean"), output_stats.getMean(),"Mean does not match the oracle"),
				() -> assertEquals(oracle_map.get("std dev"), output_stats.getStandardDeviation(),"StandardDeviation does not match the oracle"),
				() -> assertEquals(oracle_map.get("median"), output_stats.getPercentile(50),"Median does not match the oracle"),
				() -> assertEquals(oracle_map.get("skewness"), output_stats.getSkewness(),"Skewness does not match the oracle"),
				() -> assertEquals(oracle_map.get("kurtosis"), output_stats.getKurtosis(),"Kurtosis does not match the oracle")
				);	
			
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}