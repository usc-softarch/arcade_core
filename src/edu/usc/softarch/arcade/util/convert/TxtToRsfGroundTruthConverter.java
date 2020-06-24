package edu.usc.softarch.arcade.util.convert;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.PropertyConfigurator;

import edu.usc.softarch.arcade.config.Config;
import edu.usc.softarch.arcade.facts.ConcernCluster;
import edu.usc.softarch.arcade.facts.GroundTruthFileParser;

public class TxtToRsfGroundTruthConverter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		PropertyConfigurator.configure(Config.getLoggingConfigFilename());
		
		String txtFilename = args[0];
		String rsfFilename = args[1];
		
		GroundTruthFileParser.parseBashStyle(txtFilename);
		
		Set<ConcernCluster> clusters = GroundTruthFileParser.getClusters();
		
		try {
			FileWriter out = new FileWriter(rsfFilename);
			
			Map<String,ConcernCluster> clusterMap = GroundTruthFileParser.getClusterMap();
			
			for (String clusterName : clusterMap.keySet()) {
				ConcernCluster cluster = clusterMap.get(clusterName);
				for (String entity : cluster.getEntities()) {
					out.write("contain " + cluster.getName() + " " + entity + "\n");
				}
			}
			
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
