package edu.usc.softarch.arcade.metrics;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

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
		".///src///test///resources///System_evo_resources///outputs_acdc///statistics_acdc_Struts2.txt,"
		+ ".///src///test///resources///System_evo_resources///cluster_files_acdc///Struts2",

		// ACDC httpd
		".///src///test///resources///System_evo_resources///outputs_acdc///statistics_acdc_httpd.txt,"
		+ ".///src///test///resources///System_evo_resources///cluster_files_acdc///httpd",

		// ARC Struts2
		".///src///test///resources///System_evo_resources///outputs_arc///statistics_arc_Struts2.txt,"
		+ ".///src///test///resources///System_evo_resources///cluster_files_arc///Struts2",

		// ARC httpd
		".///src///test///resources///System_evo_resources///outputs_arc///statistics_arc_httpd.txt,"
		+ ".///src///test///resources///System_evo_resources///cluster_files_arc///httpd",



	})
    public void batchSystemEvoTest(String oracle_path, String clusters_dir){
        String oraclePath = oracle_path.replace("///", File.separator);  
		String clustersDir = clusters_dir.replace("///", File.separator);

        String[] args = {clustersDir,outputPath};

		assertDoesNotThrow(() -> BatchSystemEvo.main(args));

		try{
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(oraclePath));
			DescriptiveStatistics oracle_stats = (DescriptiveStatistics) ois.readObject();
			ois.close();
			
			DescriptiveStatistics output_stats = BatchSystemEvo.getStats();
			
			assertAll(
				() -> assertEquals(oracle_stats.getN(), output_stats.getN(),"N does not match the oracle"),
				() -> assertEquals(oracle_stats.getN(), output_stats.getN(),"N does not match the oracle"),
				() -> assertEquals(oracle_stats.getMin(), output_stats.getMin(),"Min does not match the oracle"),
				() -> assertEquals(oracle_stats.getMax(), output_stats.getMax(),"Max does not match the oracle"),
				() -> assertEquals(oracle_stats.getMean(), output_stats.getMean(),"Mean does not match the oracle"),
				() -> assertEquals(oracle_stats.getStandardDeviation(), output_stats.getStandardDeviation(),"StandardDeviation does not match the oracle"),
				() -> assertEquals(oracle_stats.getPercentile(50), output_stats.getPercentile(50),"Median does not match the oracle"),
				() -> assertEquals(oracle_stats.getSkewness(), output_stats.getSkewness(),"Skewness does not match the oracle"),
				() -> assertEquals(oracle_stats.getKurtosis(), output_stats.getKurtosis(),"Kurtosis does not match the oracle")
			  );
			
			  
			//assertTrue(output_stats.toString().equals(oracle_stats.toString()));
		}catch(IOException | ClassNotFoundException e){
			e.printStackTrace();
		}

		

    }
}
