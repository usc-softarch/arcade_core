package edu.usc.softarch.arcade;

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

import edu.usc.softarch.arcade.config.Config;
import edu.usc.softarch.arcade.facts.ExpertDecompositionBuilder;
import edu.usc.softarch.arcade.facts.driver.RsfReader;

public class ExpertRecoveryDriver {
static Logger logger = Logger.getLogger(ExpertRecoveryDriver.class);
	
	public static void main(String[] args) {
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
	        	formatter.printHelp( ExpertRecoveryDriver.class.getName(), options );
	        	System.exit(0);
	        }
	    }
	    catch( ParseException exp ) {
	        // oops, something went wrong
	        System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
	    }
		
		PropertyConfigurator.configure(Config.getLoggingConfigFilename());
		logger.debug("Running from " + ExpertRecoveryDriver.class.getName());
		Config.initConfigFromFile(Config.getProjConfigFilename());
		System.out.println("Creating expert decomposition...");
		System.out.println("Reading in rsf file...");
		RsfReader.loadRsfDataForCurrProj();
		//RsfReader.performPreClusteringTasks();
		ExpertDecompositionBuilder.readInExpertDecomposition("/home/joshua/recovery/Expert Decompositions/Bash expert decompositions.txt");
		ExpertDecompositionBuilder.buildExpertDecompositionClusters(RsfReader.startNodesSet);
		ExpertDecompositionBuilder.findMissingElementsInExpertDecomposition();
		ExpertDecompositionBuilder.buildMojoTargetFilesForFunctions();
		//ExpertDecompositionBuilder.buildDocTopicsForClusters();
	}
	
	
}
