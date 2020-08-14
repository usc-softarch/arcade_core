package edu.usc.softarch.arcade;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mojo.MoJoCalculator;

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

public class MetricsDriver {
	static Logger logger = Logger.getLogger(MetricsDriver.class);
	
	public static void main(String[] args) {
		String computedFilePrefix = "/home/joshua/workspace/MyExtractors/data/linux/linux_";
		String authClusteringFile = "/home/joshua/recovery/Expert Decompositions/linuxFullAuthcontain.rsf";
		String selectedAlg = "arc";
		String simMeasure = "uem";
		String stoppingCriterion = "preselected";
		String computedClustersFile = "/home/joshua/workspace/acdc/linux_acdc_clustered.rsf";
		
		Options options = new Options();
		
		Option help = new Option( "help", "print this message" );
		Option useExpertDecompFile = new Option("use_expert_decomp_file","uses the expert decomposition file property from project config file");
		
		Option projFile   = OptionBuilder.withArgName( "file" )
                .hasArg()
                .withDescription(  "project configuration file" )
                .create( "projfile" );
		
		Option computedFilePrefixOption = OptionBuilder.withArgName( "file" )
                .hasArg()
                .withDescription(  "prefix of computed clustering file" )
                .create( "computedFilePrefix" );
		
		Option authClusteringFileOption = OptionBuilder.withArgName( "file" )
                .hasArg()
                .withDescription(  "authoritative clustering file" )
                .create( "authClusteringFile" );
		
		Option algOption = OptionBuilder.withArgName( "selectedAlg" )
                .hasArg()
                .withDescription(  "Select the algorithm used to create computed clustering file [acdc|wca|arc]" )
                .create( "alg" );
		
		Option simMeasureOption = OptionBuilder.withArgName( "simMeasure" )
                .hasArg()
                .withDescription(  "Select the similarity measured used to create computed clustering file [uem|uemnm|js]" )
                .create( "simMeasure" );
		
		Option stoppingCriterionOption = OptionBuilder.withArgName( "criterion" )
                .hasArg()
                .withDescription(  "Select the stopping criterion [preselected|clustergain]" )
                .create( "stoppingCriterion" ); 
		
		Option computedClustersFileOption = OptionBuilder.withArgName( "file" )
                .hasArg()
                .withDescription(  "File containing clusters computed by clustering algorithm" )
                .create( "computedClustersFile" );
		
		options.addOption(help);
		options.addOption(useExpertDecompFile);
		options.addOption(projFile);
		options.addOption(computedFilePrefixOption);
		options.addOption(authClusteringFileOption);
		options.addOption(algOption);
		options.addOption(simMeasureOption);
		options.addOption(stoppingCriterionOption);
		options.addOption(computedClustersFileOption);
		
		
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
	        	formatter.printHelp( MetricsDriver.class.getName(), options );
	        	System.exit(0);
	        }
	        if (line.hasOption("computedFilePrefix")) {
	        	computedFilePrefix = line.getOptionValue("computedFilePrefix");
	        }
	        if (line.hasOption("authClusteringFile")) {
	        	authClusteringFile = line.getOptionValue("authClusteringFile");
	        }
	        if (line.hasOption("alg")) {
	        	selectedAlg = line.getOptionValue("alg");
	        }
	        if (line.hasOption("simMeasure")) {
	        	simMeasure = line.getOptionValue("simMeasure");
	        }
	        if (line.hasOption("stoppingCriterion")) {
	        	stoppingCriterion = line.getOptionValue("stoppingCriterion");
	        }
	        if (line.hasOption("computedClustersFile")) {
	        	computedClustersFile = line.getOptionValue("computedClustersFile");
	        }
	    }
	    catch( ParseException exp ) {
	        // oops, something went wrong
	        System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
	    }
		
		PropertyConfigurator.configure(Config.getLoggingConfigFilename());
		logger.debug("Running from " + MetricsDriver.class.getName());
		Config.initConfigFromFile(Config.getProjConfigFilename());
		
		if (selectedAlg.equals("wca") || selectedAlg.equals("limbo")) {
			performMoJoOperationsForMultipleClustersOnSingleAuthClusteringFile(selectedAlg, simMeasure,
					stoppingCriterion, computedFilePrefix, authClusteringFile);
		}
		
		if (selectedAlg.equals("acdc")) {
			performMojoForSingleAuthClustering(computedClustersFile,authClusteringFile);
		}

		if (selectedAlg.equals("arc")) {
			performMoJoForMultiClustersOnFile(computedFilePrefix,authClusteringFile,selectedAlg, simMeasure,stoppingCriterion);
		}
	}

	private static String constructTopicBasedComputedRsfFilename(
			String computedFilePrex, String selectedAlg, String simMeasure,
			String stoppingCriterion, int numClusters, int numTopics) {
		return computedFilePrex + selectedAlg + "_"
				+ stoppingCriterion + "_" + simMeasure + "_"  + numClusters + "_clusters_" + numTopics + "topics.rsf";
	}

	private static String constructNonTopicComputedRsfFilename(
			String computedFilePrex, String selectedAlg, String simMeasure,
			String stoppingCriterion, int numClusters) {
		return computedFilePrex + selectedAlg + "_"
				+ stoppingCriterion + "_" + simMeasure + "_" + numClusters
				+ "_clusters.rsf";
	}
	
	private static void performMoJoForMultiClustersOnFile(
			String computedFilePrex, String authClusteringFile, String selectedAlg, String simMeasure, String stoppingCriterion) {
		List<Integer> numClustersList = new ArrayList<>();
		List<Integer> numTopicsList = new ArrayList<>();
		
		List<Long> mojoToAuthList = new ArrayList<>();
		List<Double> mojoFmToAuthList = new ArrayList<>();
		
		long mojoSum = 0;
		double mojoFmSum = 0;
		int computedClusterCount = 0;
		
		mojoSum = 0;
		mojoFmSum = 0;
		computedClusterCount = 0;
		
		double maxMojoFM = 0;
		int numClustersAtMax = 0;
		int numTopicsAtMax = 0;
		
		for (int numClusters = Config.getStartNumClustersRange(); numClusters <= Config
				.getEndNumClustersRange(); numClusters += Config
				.getRangeNumClustersStep()) {
			for (int numTopics = Config.getStartNumTopicsRange(); numTopics <= Config
					.getEndNumTopicsRange(); numTopics += Config
					.getRangeNumTopicsStep()) {

				numClustersList.add(numClusters);
				numTopicsList.add(numTopics);

				String computedRsfFilename = null;
				if (!selectedAlg.equals("arc")) {
					computedRsfFilename = constructNonTopicComputedRsfFilename(computedFilePrex,
							selectedAlg, simMeasure, stoppingCriterion, numClusters);
				} else {
					computedRsfFilename = constructTopicBasedComputedRsfFilename(computedFilePrex,
							selectedAlg, simMeasure, stoppingCriterion, numClusters,
							numTopics);
				}

				String usingComputedFilename = "Using " + computedRsfFilename
						+ " as computed clusters...";
				logger.debug(usingComputedFilename);
				System.out.println(usingComputedFilename);

				MoJoCalculator mojoCalc = new MoJoCalculator(
						computedRsfFilename, authClusteringFile, null);
				long mojoValue = mojoCalc.mojo();
				mojoToAuthList.add(mojoValue);
				String mojoOutput = "MoJo of " + computedRsfFilename
						+ " compared to authoritative clustering: " + mojoValue;
				logger.debug(mojoOutput);
				System.out.println(mojoOutput);
				mojoSum += mojoValue;

				mojoCalc = new MoJoCalculator(computedRsfFilename,
						authClusteringFile, null);
				double mojoFmValue = mojoCalc.mojofm();
				mojoFmToAuthList.add(mojoFmValue);
				mojoOutput = "MoJoFM of " + computedRsfFilename
						+ " compared to authoritative clustering: "
						+ mojoFmValue;
				logger.debug(mojoOutput);
				System.out.println(mojoOutput);
				mojoFmSum += mojoFmValue;
				
				if (mojoFmValue > maxMojoFM) {
					maxMojoFM = mojoFmValue;
					numClustersAtMax = numClusters;
					numTopicsAtMax = numTopics;
				}

				computedClusterCount++;
			}

		}
		double mojoAvg = mojoSum/computedClusterCount;
		String mojoAvgOutput = "MoJo averge: " + mojoAvg;
		logger.debug(mojoAvgOutput);
		System.out.println(mojoAvgOutput);

		double mojoFmAvg = mojoFmSum/computedClusterCount;
		String mojoFmAvgOutput = "MoJoFM averge: " + mojoFmAvg;
		logger.debug(mojoFmAvgOutput);
		System.out.println(mojoFmAvgOutput);
		
		System.out.println("max mojo fm: " + maxMojoFM);
		System.out.println("num clusters at max: " + numClustersAtMax);
		System.out.println("num topics at max: " + numTopicsAtMax);
		
		System.out.println("Writing MoJo and MoJoFM to csv file " + Config.getMojoToAuthCSVFilename(numClustersList,selectedAlg,simMeasure) + " ...");

		createMojoToAuthListsCSVFile(numClustersList,mojoToAuthList,mojoFmToAuthList,selectedAlg,simMeasure,numTopicsList);
		
	}
	
	private static void performMoJoOperationsForMultipleClustersOnSingleAuthClusteringFile(String selectedAlg, String simMeasure,
			String stoppingCriterion, String computedFilePrex, String authClusteringFile) {
		List<Integer> numClustersList = new ArrayList<>();
		List<Long> mojoList = new ArrayList<>();
		List<Double> mojoFmList = new ArrayList<>();
		
		double maxMojoFm = 0;
		double maxNumClusters = 0;
		double sumMojoFm = 0;
		for (int numClusters = Config.getStartNumClustersRange(); numClusters <= Config
				.getEndNumClustersRange(); numClusters += Config
				.getRangeNumClustersStep()) {
			
			numClustersList.add(numClusters);
			
			String computedRsfFilename = constructNonTopicComputedRsfFilename(computedFilePrex, selectedAlg, simMeasure,
					stoppingCriterion, numClusters);
			String usingComputedFilename = "Using " + computedRsfFilename
					+ " as computed clusters...";
			logger.debug(usingComputedFilename);
			System.out.println(usingComputedFilename);
			
			MoJoCalculator mojoCalc = new MoJoCalculator(
					computedRsfFilename, authClusteringFile, null);
			long mojoValue = mojoCalc.mojo();
			mojoList.add(mojoValue);
			String mojoOutput = "MoJo of " + computedRsfFilename
					+ " compared to authoritative clustering: " + mojoValue;
			logger.debug(mojoOutput);
			System.out.println(mojoOutput);
			
			mojoCalc = new MoJoCalculator(computedRsfFilename,
					authClusteringFile, null);
			double mojoFmValue = mojoCalc.mojofm();
			mojoFmList.add(mojoFmValue);
			mojoOutput = "MoJoFM of " + computedRsfFilename
					+ " compared to authoritative clustering: "
					+ mojoFmValue;
			logger.debug(mojoOutput);
			System.out.println(mojoOutput);
			
			sumMojoFm += mojoFmValue;
			
			if (mojoFmValue > maxMojoFm) {
				maxMojoFm = mojoFmValue;
				maxNumClusters = numClusters;
			}
		}
		
		double avgMojoFm = sumMojoFm/numClustersList.size();
		
		
		String writingMojoToCsvMsg = "Writing MoJo and MoJoFM to csv file " + Config.getMojoToAuthCSVFilename(numClustersList,selectedAlg,simMeasure) + " ...";
		String maxMojoFmMsg = "max mojo fm: " + maxMojoFm;
		String numClusAtMaxMojoFmMsg = "no. clusters at max mojo fm: " + maxNumClusters;
		String avgMojoFmMsg = "avg mojo fm: " + avgMojoFm;
		
		System.out.println(writingMojoToCsvMsg);
		System.out.println();
		System.out.println(maxMojoFmMsg);
		System.out.println(numClusAtMaxMojoFmMsg);
		System.out.println(avgMojoFmMsg);
		
		logger.debug(writingMojoToCsvMsg);
		logger.debug("\n");
		logger.debug(maxMojoFmMsg);
		logger.debug(numClusAtMaxMojoFmMsg);
		logger.debug(avgMojoFmMsg);

		createMojoListsCSVFile(numClustersList,mojoList,mojoFmList,selectedAlg,simMeasure);
	}

	private static void performMojoForSingleAuthClustering(String computedRsfFilename, String authClusteringFile) {
		MoJoCalculator mojoCalc = new MoJoCalculator(computedRsfFilename,authClusteringFile, null);
		long mojoValue = mojoCalc.mojo();
		String mojoOutput = "MoJo of " + computedRsfFilename
				+ " compared to authoritative clustering: " + mojoValue;
		logger.debug(mojoOutput);
		System.out.println(mojoOutput);

		

		mojoCalc = new MoJoCalculator(computedRsfFilename, authClusteringFile, null);
		double mojoFmValue = mojoCalc.mojofm();
		mojoOutput = "MoJoFM of " + computedRsfFilename
				+ " compared to authoritative clustering: " + mojoFmValue;
		logger.debug(mojoOutput);
		System.out.println(mojoOutput);
	}
	
	private static void createMojoListsCSVFile(List<Integer> numClustersList, List<Long> mojoList,
			List<Double> mojoFmList,String selectedAlg, String simMeasure) {
		try (FileWriter fstream = new FileWriter(
			Config.getMojoToAuthCSVFilename(numClustersList,selectedAlg, simMeasure))) {
			BufferedWriter out = new BufferedWriter(fstream);
			
			out.write("number of clusters,");
			for (int numClusters : numClustersList) {
				out.write(numClusters + ",");
			}
			out.write("\n");
			
			writeMojoListsToFile(mojoList, mojoFmList, out);
			
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void createMojoToAuthListsCSVFile(List<Integer> numClustersList, List<Long> mojoList,
			List<Double> mojoFmList,String selectedAlg, String simMeasure, List<Integer> numTopicsList) {
		try (FileWriter fstream = new FileWriter(
			Config.getMojoToAuthCSVFilename(numClustersList,selectedAlg,simMeasure))) {
			BufferedWriter out = new BufferedWriter(fstream);
			
			out.write("number of topics,");
			for (int numTopics : numTopicsList) {
				out.write(numTopics + ",");
			}
			out.write("\n");
			
			out.write("number of clusters,");
			for (int numClusters : numClustersList) {
				out.write(numClusters + ",");
			}
			out.write("\n");
			
			writeMojoListsToFile(mojoList, mojoFmList, out);
			
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void writeMojoListsToFile(List<Long> mojoList,
			List<Double> mojoFmList, BufferedWriter out) throws IOException {
		out.write("MoJo,");
		for (long mojo : mojoList) {
			out.write(mojo + ",");
		}
		out.write("\n");
		
		out.write("MoJoFM,");
		for (double mojoFm : mojoFmList) {
			out.write(mojoFm + ",");
		}
		out.write("\n");
	}
}