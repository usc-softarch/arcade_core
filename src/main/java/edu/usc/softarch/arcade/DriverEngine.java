package edu.usc.softarch.arcade;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import edu.usc.softarch.arcade.clustering.ClusteringEngine;
import edu.usc.softarch.arcade.config.Config;
import edu.usc.softarch.arcade.facts.driver.RsfReader;

/**
 * @author joshua
 */
public class DriverEngine {
	private static Logger logger = Logger.getLogger(DriverEngine.class);

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		Options options = new Options();
		
		Option help = new Option( "help", "print this message" );
		
		Option projFile   = OptionBuilder.withArgName( "file" )
                .hasArg()
                .withDescription(  "project configuration file" )
                .create( "projfile" );
		
		options.addOption(help);
		options.addOption(projFile);
		
		 // create the parser
	    CommandLineParser parser = new GnuParser();
	    try {
	        // parse the command line arguments
	        CommandLine line = parser.parse( options, args );
	        
	        if (line.hasOption("projfile")) {
	        	Config.setProjConfigFilename(line.getOptionValue("projfile"));
	        }
	        if (line.hasOption("help")) {
	        	// automatically generate the help statement
	        	HelpFormatter formatter = new HelpFormatter();
	        	formatter.printHelp( "DriverEngine", options );
	        	System.exit(0);
	        }
	    }
	    catch( ParseException exp ) {
	        // oops, something went wrong
	        System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
	    }
		
		
		PropertyConfigurator.configure("cfg" + File.separator + "extractor_logging.cfg");

		run();
	}

	public static void run() throws Exception {
		Config.initConfigFromFile(Config.getProjConfigFilename());

		File fastFeatureVectorsFile = new File(
				Config.getFastFeatureVectorsFilename());

		if (!fastFeatureVectorsFile.exists() || !Config.useFastFeatureVectorsFile) {
			System.out.println("Reading in rsf file...");
			RsfReader.loadRsfDataForCurrProj();
			RsfReader.performPreClusteringTasks();
		}

		System.out.println("Performing clustering tasks...");

		// Common setup for clustering
		int clustersToStop = Config.getStartNumClustersRange();
		logger.debug("clustersToStop: " + clustersToStop);
		System.out.println("Stopping using " + Config.stoppingCriterion
				+ " criterion...");
		System.out.println("Will stop at " + clustersToStop
				+ " clusters...");
		System.out.println("Using "
				+ Config.getCurrentClusteringAlgorithm()
				+ " clustering algorithm...");
		System.out.println("Using " + Config.getCurrSimMeasure()
				+ " as the similarity measure...");
		Config.setNumClusters(clustersToStop);
		// end common setup for clustering

		if (Config.isUsingPreselectedRange()) {
			List<Integer> clustersToWriteList = new ArrayList<>();

			for (int clustersToWrite = Config
					.getStartNumClustersRange(); clustersToWrite <= Config
					.getEndNumClustersRange(); clustersToWrite += Config
					.getRangeNumClustersStep()) {
				clustersToWriteList.add(clustersToWrite);
			}

			Config.setClustersToWriteList(clustersToWriteList);

			if (Config.isUsingNumTopicsRange()) {
				List<Integer> numTopicsList = new ArrayList<>();
				for (int numTopics = Config.getStartNumTopicsRange(); numTopics <= Config
						.getEndNumTopicsRange(); numTopics += Config
						.getRangeNumTopicsStep()) {
					logger.debug("numTopics: " + numTopics);
					System.out.println("Clustering using " + numTopics
							+ " topics...");
					numTopicsList.add(numTopics);
				}
				Config.setNumTopicsList(numTopicsList);
			}

		}

		ClusteringEngine ce = new ClusteringEngine();
		ce.run();

		System.out.println("Exiting...");

	}
}