package edu.usc.softarch.arcade.metrics;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;
import java.util.Scanner;

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
			Scanner sc = new Scanner(new File(oraclePath));  
			DescriptiveStatistics output_stats = BatchSystemEvo.getStats();
			while (sc.hasNext()){
				String line = sc.nextLine();
				String arr[] = line.split(":");
				oracle_map.put(arr[0],Double.parseDouble(arr[1]));
			}
			
			/*
			Round both the oracle and the output to 10 sig figs and compare.
			Otherwise tests will not pass because the oracle and the output has different
			number of significant figures
			*/
			assertAll(
				() -> assertEquals(oracle_map.get("n"), (double)(output_stats.getN()),"N does not match the oracle"),
				() -> assertEquals(new BigDecimal(oracle_map.get("min")).round(new MathContext(10)).doubleValue(), 
					new BigDecimal(output_stats.getMin()).round(new MathContext(10)).doubleValue(),"Min does not match the oracle"),
				() -> assertEquals(new BigDecimal(oracle_map.get("max")).round(new MathContext(10)).doubleValue(), 
					new BigDecimal(output_stats.getMax()).round(new MathContext(10)).doubleValue(),"Max does not match the oracle"),
				() -> assertEquals(new BigDecimal(oracle_map.get("mean")).round(new MathContext(10)).doubleValue(), 
					new BigDecimal(output_stats.getMean()).round(new MathContext(10)).doubleValue(),"Mean does not match the oracle"),
				() -> assertEquals(new BigDecimal(oracle_map.get("std dev")).round(new MathContext(10)).doubleValue(), 
					new BigDecimal(output_stats.getStandardDeviation()).round(new MathContext(10)).doubleValue(),"StandardDeviation does not match the oracle"),
				() -> assertEquals(new BigDecimal(oracle_map.get("median")).round(new MathContext(10)).doubleValue(), 
					new BigDecimal(output_stats.getPercentile(50)).round(new MathContext(10)).doubleValue(),"Median does not match the oracle"),
				() -> assertEquals(new BigDecimal(oracle_map.get("skewness")).round(new MathContext(10)).doubleValue(), 
					new BigDecimal(output_stats.getSkewness()).round(new MathContext(10)).doubleValue(),"Skewness does not match the oracle"),
				() -> assertEquals(new BigDecimal(oracle_map.get("kurtosis")).round(new MathContext(10)).doubleValue(), 
					new BigDecimal(output_stats.getKurtosis()).round(new MathContext(10)).doubleValue(),"Kurtosis does not match the oracle")
				);	
			
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}