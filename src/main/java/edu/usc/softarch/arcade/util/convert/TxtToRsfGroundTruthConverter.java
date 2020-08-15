package edu.usc.softarch.arcade.util.convert;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import org.apache.log4j.PropertyConfigurator;

import edu.usc.softarch.arcade.facts.ConcernCluster;
import edu.usc.softarch.arcade.facts.GroundTruthFileParser;

public class TxtToRsfGroundTruthConverter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		PropertyConfigurator.configure("cfg" + File.separator + "extractor_logging.cfg");
		
		String txtFilename = args[0];
		String rsfFilename = args[1];
		
		GroundTruthFileParser.parseBashStyle(txtFilename);
		
		try (FileWriter out = new FileWriter(rsfFilename)) {
			Map<String,ConcernCluster> clusterMap = GroundTruthFileParser.getClusterMap();
			
			for (String clusterName : clusterMap.keySet()) {
				ConcernCluster cluster = clusterMap.get(clusterName);
				for (String entity : cluster.getEntities()) {
					out.write("contain " + cluster.getName() + " " + entity + "\n");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
