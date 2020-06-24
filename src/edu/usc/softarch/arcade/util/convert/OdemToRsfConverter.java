package edu.usc.softarch.arcade.util.convert;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
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

import edu.usc.softarch.arcade.MetricsDriver;
import edu.usc.softarch.arcade.config.Config;
import edu.usc.softarch.arcade.facts.driver.ODEMReader;
import edu.usc.softarch.extractors.cda.odem.DependsOn;
import edu.usc.softarch.extractors.cda.odem.Type;

public class OdemToRsfConverter {

	static Logger logger = Logger.getLogger(OdemToRsfConverter.class);
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		PropertyConfigurator.configure(Config.getLoggingConfigFilename());
		String odemFileStr = "/home/joshua/cda/hadoop-0.19.odem";
		String rsfFileStr = "/home/joshua/workspace/MyExtractors/data/hadoop-0.19/hadoop-0.19-odem-facts.rsf";

		Options options = new Options();

		Option help = new Option("help", "print this message");
		Option odemFile   = OptionBuilder.withArgName( "file" )
                .hasArg()
                .withDescription(  "ODEM file to be converted" )
                .create( "odemFile" );
		Option rsfFileOption   = OptionBuilder.withArgName( "file" )
                .hasArg()
                .withDescription(  "resulting RSF file" )
                .create( "rsfFile" );
		
		options.addOption(help);
		options.addOption(odemFile);
		options.addOption(rsfFileOption);
		
		CommandLineParser parser = new GnuParser();
	    try {
	        // parse the command line arguments
	        CommandLine line = parser.parse( options, args );
	        
	        if (line.hasOption("help")) {
	        	// automatically generate the help statement
	        	HelpFormatter formatter = new HelpFormatter();
	        	formatter.printHelp( MetricsDriver.class.getName(), options );
	        	System.exit(0);
	        }
	        if (line.hasOption("odemFile")) {
	        	odemFileStr = line.getOptionValue("odemFile");
	        }
	        if (line.hasOption("odemFile")) {
	        	rsfFileStr = line.getOptionValue("rsfFile");
	        }
	    }
	    catch( ParseException exp ) {
	        // oops, something went wrong
	        System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
	    }
	    
	    ODEMReader.setTypesFromODEMFile(odemFileStr);
		List<Type> allTypes = ODEMReader.getAllTypes();
		HashMap<String,Type> typeMap = new HashMap<String,Type>();
		for (Type t : allTypes) {
			typeMap.put(t.getName().trim(), t);
		}
		
		String convertMsg = "Writing dependencies from ODEM file to RSF file...";
		System.out.println(convertMsg);
		logger.debug(convertMsg);
		try {
			File rsfFile = new File(rsfFileStr);
			if (!rsfFile.getParentFile().exists()) {
				rsfFile.getParentFile().mkdirs();
			}
			FileWriter fw = new FileWriter(rsfFileStr);
			BufferedWriter out = new BufferedWriter(fw);
			for (String typeKey : typeMap.keySet()) {
				Type t = typeMap.get(typeKey);
				for (DependsOn dependency : t.getDependencies().getDependsOn()) {
					String rsfLine = dependency.getClassification() + " "
							+ t.getName() + " " + dependency.getName();
					logger.debug(rsfLine);
					out.write(rsfLine + "\n");
				}
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
