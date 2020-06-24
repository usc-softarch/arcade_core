package edu.usc.softarch.arcade;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
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

import edu.usc.softarch.arcade.clustering.util.ClusterUtil;
import edu.usc.softarch.arcade.config.Config;
import edu.usc.softarch.arcade.facts.ExpertDecomposition;
import edu.usc.softarch.arcade.facts.ExpertDecompositionBuilder;
import edu.usc.softarch.arcade.facts.IntraPairFromClustersRsfBuilder;
import edu.usc.softarch.arcade.facts.PrecisionRecallCalculator;
import edu.usc.softarch.arcade.facts.driver.RsfReader;

public class MetricsDriver {
	static Logger logger = Logger.getLogger(MetricsDriver.class);
	
	public static void main(String[] args) {
		String computedFilePrefix = "/home/joshua/workspace/MyExtractors/data/linux/linux_";
		String authClusteringFile = "/home/joshua/recovery/Expert Decompositions/linuxFullAuthcontain.rsf";
		String selectedAlg = "arc";
		String simMeasure = "uem";
		String stoppingCriterion = "preselected";
		String computedClustersFile = "/home/joshua/workspace/acdc/linux_acdc_clustered.rsf";
		boolean isUsingExpertDecompFile = false;
		
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
	        if (line.hasOption("use_expert_decomp_file")) {
	        	isUsingExpertDecompFile = true;
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

		
		//String computedRsfFilename = Config.getClustersRSFFilename();
		//String selectedAlg = "arc";
		
		
		// Builds rsf file from expert decompositions file and performs metric calcs
		/*if (Config.getExpertDecompositionFile() != null) {
			buildExpertDecompositionClustersFromRSFFile();
			performMoJoOperationsWithExpertDecompositions(selectedAlg, simMeasure,stoppingCriterion,computedFilePrefix);
			performPrecisionRecallOperationsForConcernRecovery(selectedAlg, simMeasure, stoppingCriterion, computedFilePrefix);
			System.exit(0);
		}*/
		
		
		
		//performMoJoOperationsForConcernRecovery(selectedAlg, simMeasure,stoppingCriterion);
		
		if (selectedAlg.equals("wca") || selectedAlg.equals("limbo")) {
			performMoJoOperationsForMultipleClustersOnSingleAuthClusteringFile(selectedAlg, simMeasure,
					stoppingCriterion, computedFilePrefix, authClusteringFile);
			/*performPrecisionRecallOperationsForMultipleClustersOnSingleAuthClusteringFile(selectedAlg, simMeasure,
					stoppingCriterion, computedFilePrefix, authClusteringFile);
			if (isUsingExpertDecompFile) {
				performPrecisionRecallOperations(selectedAlg, simMeasure, stoppingCriterion);
			}*/
		}
		
		if (selectedAlg.equals("acdc")) {
			performMojoForSingleAuthClustering(computedClustersFile,authClusteringFile);
			//performPrecisionRecallForSingleAuthClustering(computedClustersFile,authClusteringFile);
		}

		if (selectedAlg.equals("arc")) {
			performMoJoForMultiClustersOnFile(computedFilePrefix,authClusteringFile,selectedAlg, simMeasure,stoppingCriterion);
			//performPrecisionRecallForMultiClustersOnFile(computedFilePrefix,authClusteringFile,selectedAlg, simMeasure,stoppingCriterion);
		}
	}
	
	private static void performPrecisionRecallForMultiClustersOnFile(
			String computedFilePrex, String authClusteringFile, String selectedAlg, String simMeasure, String stoppingCriterion) {
		List<Integer> numClustersList = new ArrayList<Integer>();
		List<Integer> numTopicsList = new ArrayList<Integer>();
		
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

				HashSet<HashSet<String>> allIntraPairsFromClustersRsf = IntraPairFromClustersRsfBuilder
						.buildIntraPairsFromClustersRsf(computedRsfFilename);
				
				HashSet<HashSet<String>> allIntraPairsFromAuthClustersRsf = IntraPairFromClustersRsfBuilder
						.buildIntraPairsFromClustersRsf(authClusteringFile);
				
				double precision = PrecisionRecallCalculator.computePrecision(
						allIntraPairsFromClustersRsf,
						allIntraPairsFromAuthClustersRsf);
				String precisionOutput = "Precision of " + computedRsfFilename + " compared to "  + authClusteringFile + " : " + precision;
				logger.debug(precisionOutput);
				System.out.println(precisionOutput);
				
				double recall = PrecisionRecallCalculator.computeRecall(
						allIntraPairsFromClustersRsf,
						allIntraPairsFromAuthClustersRsf);
				String recallOutput = "Recall of " + computedRsfFilename + " compared to "  + authClusteringFile + " : " + recall;
				logger.debug(recallOutput);
				System.out.println(recallOutput);

			}

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
		List<Integer> numClustersList = new ArrayList<Integer>();
		List<Integer> numTopicsList = new ArrayList<Integer>();
		
		
		List<Long> mojoToNextList = new ArrayList<Long>();
		List<Double> mojoFmToNextList = new ArrayList<Double>();
		
		List<Long> mojoToAuthList = new ArrayList<Long>();
		List<Double> mojoFmToAuthList = new ArrayList<Double>();
		
		long mojoSum = 0;
		double mojoFmSum = 0;
		int computedClusterCount = 0;
		
		/*for (int numClusters = Config.getStartNumClustersRange(); numClusters < Config
				.getEndNumClustersRange(); numClusters += Config
				.getRangeNumClustersStep()) {
			for (int numTopics = Config.getStartNumTopicsRange(); numTopics <= Config
					.getEndNumTopicsRange(); numTopics += Config
					.getRangeNumTopicsStep()) {

				numClustersList.add(numClusters);
				numTopicsList.add(numTopics);

				String computedRsfFilename1 = null;
				String computedRsfFilename2 = null;
				if (!selectedAlg.equals("arc")) {
					computedRsfFilename1 = constructNonTopicComputedRsfFilename(computedFilePrex,
							selectedAlg, simMeasure, stoppingCriterion, numClusters);
					computedRsfFilename2 = constructNonTopicComputedRsfFilename(computedFilePrex,
							selectedAlg, simMeasure, stoppingCriterion, numClusters+1);
				} else {
					computedRsfFilename1 = constructTopicBasedComputedRsfFilename(computedFilePrex,
							selectedAlg, simMeasure, stoppingCriterion, numClusters,
							numTopics);
					computedRsfFilename2 = constructTopicBasedComputedRsfFilename(computedFilePrex,
							selectedAlg, simMeasure, stoppingCriterion, numClusters+1,
							numTopics);
				}
				
				File computedRsfFile1 = new File(computedRsfFilename1);
				File computedRsfFile2 = new File(computedRsfFilename2);
				
				if (!computedRsfFile1.exists()) {
					System.err.println(computedRsfFile1.getName() + " does not exist, so skipping it");
					continue;
				}
				
				if (!computedRsfFile2.exists()) {
					System.err.println(computedRsfFile2.getName() + " does not exist, so skipping it");
					continue;
				}

				String usingComputedFilename = "Using " + computedRsfFilename1 + " and " + computedRsfFilename2
						+ " as computed clusters...";
				logger.debug(usingComputedFilename);
				System.out.println(usingComputedFilename);

				MoJoCalculator mojoCalc = new MoJoCalculator(
						computedRsfFilename1, computedRsfFilename2, null);
				long mojoValue = mojoCalc.mojo();
				mojoToNextList.add(mojoValue);
				String mojoOutput = "MoJo of " + computedRsfFilename1
						+ " compared to " + computedRsfFilename2 + ": " + mojoValue;
				logger.debug(mojoOutput);
				System.out.println(mojoOutput);
				mojoSum += mojoValue;

				mojoCalc = new MoJoCalculator(computedRsfFilename1,
						computedRsfFilename2, null);
				double mojoFmValue = mojoCalc.mojofm();
				mojoFmToNextList.add(mojoFmValue);
				mojoOutput = "MoJoFM of " + computedRsfFilename1
						+ " compared to " + computedRsfFilename2 + ": "
						+ mojoFmValue;
				logger.debug(mojoOutput);
				System.out.println(mojoOutput);
				mojoFmSum += mojoFmValue;

				computedClusterCount++;
			}

		}
		
		createMojoListsCSVFile(Config.getMojoToNextCSVFilename(numClustersList, selectedAlg, simMeasure),numClustersList,mojoToNextList,mojoFmToNextList,selectedAlg,simMeasure,numTopicsList);
		
		System.out.println("---------------------------------------------------------------");*/
		
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
		//mojoAvgList.add(mojoAvg);

		double mojoFmAvg = mojoFmSum/computedClusterCount;
		String mojoFmAvgOutput = "MoJoFM averge: " + mojoFmAvg;
		logger.debug(mojoFmAvgOutput);
		System.out.println(mojoFmAvgOutput);
		//mojoFmAvgList.add(mojoFmAvg);
		
		System.out.println("max mojo fm: " + maxMojoFM);
		System.out.println("num clusters at max: " + numClustersAtMax);
		System.out.println("num topics at max: " + numTopicsAtMax);
		
		System.out.println("Writing MoJo and MoJoFM to csv file " + Config.getMojoToAuthCSVFilename(numClustersList,selectedAlg,simMeasure) + " ...");

		createMojoToAuthListsCSVFile(numClustersList,mojoToAuthList,mojoFmToAuthList,selectedAlg,simMeasure,numTopicsList);
		
	}

	/*private static void performMoJoOperationsForFile(String computedRsfFilename, String selectedAlg) {
		List<Integer> numClustersList = new ArrayList<Integer>();
		List<Double> mojoAvgList = new ArrayList<Double>();
		List<Double> mojoFmAvgList = new ArrayList<Double>();
		for (int numClusters = Config.getStartNumClustersRange(); numClusters <= Config
				.getEndNumClustersRange(); numClusters += Config
				.getRangeNumClustersStep()) {
			
			numClustersList.add(numClusters);

			String usingComputedFilename = "Using " + computedRsfFilename
					+ " as computed clusters...";
			logger.debug(usingComputedFilename);
			System.out.println(usingComputedFilename);

			performMojoOperationsForComputedRsfFilenameWithExpertDecompositions(mojoAvgList,
					mojoFmAvgList, computedRsfFilename);

		}
		System.out.println("Writing MoJo average, and MoJoFM to csv file " + Config.getMojoCSVFilename(numClustersList,selectedAlg) + " ...");

		createMojoCSVFile(numClustersList,mojoAvgList,mojoFmAvgList,selectedAlg);

	}*/
	
	private static void performPrecisionRecallOperationsForMultipleClustersOnSingleAuthClusteringFile(String selectedAlg, String simMeasure,
			String stoppingCriterion, String computedFilePrex, String authClusteringFile) {
		List<Integer> numClustersList = new ArrayList<Integer>();
		
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
			
			
			HashSet<HashSet<String>> allIntraPairsFromClustersRsf = IntraPairFromClustersRsfBuilder
					.buildIntraPairsFromClustersRsf(computedRsfFilename);
			
			HashSet<HashSet<String>> allIntraPairsFromAuthClustersRsf = IntraPairFromClustersRsfBuilder
					.buildIntraPairsFromClustersRsf(authClusteringFile);
			
			double precision = PrecisionRecallCalculator.computePrecision(
					allIntraPairsFromClustersRsf,
					allIntraPairsFromAuthClustersRsf);
			String precisionOutput = "Precision of computed data compared to authoritative clustering: " + precision;
			logger.debug(precisionOutput);
			System.out.println(precisionOutput);
			
			double recall = PrecisionRecallCalculator.computeRecall(
					allIntraPairsFromClustersRsf,
					allIntraPairsFromAuthClustersRsf);
			String recallOutput = "Recall of computed data compared to decomposition: " + recall;
			logger.debug(recallOutput);
			System.out.println(recallOutput);
			
			
		}
		
	}
	
	private static void performMoJoOperationsForMultipleClustersOnSingleAuthClusteringFile(String selectedAlg, String simMeasure,
			String stoppingCriterion, String computedFilePrex, String authClusteringFile) {
		List<Integer> numClustersList = new ArrayList<Integer>();
		List<Long> mojoList = new ArrayList<Long>();
		List<Double> mojoFmList = new ArrayList<Double>();
		
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
	
	private static void performMoJoOperationsWithExpertDecompositions(String selectedAlg, String simMeasure,
			String stoppingCriterion, String computedFilePrex) {
		List<Integer> numClustersList = new ArrayList<Integer>();
		List<Double> mojoAvgList = new ArrayList<Double>();
		List<Double> mojoFmAvgList = new ArrayList<Double>();
		
		for (int numClusters = Config.getStartNumClustersRange(); numClusters <= Config
				.getEndNumClustersRange(); numClusters += Config
				.getRangeNumClustersStep()) {
			for (int numTopics = Config.getStartNumTopicsRange(); numTopics <= Config
					.getEndNumTopicsRange(); numTopics += Config
					.getRangeNumTopicsStep()) {

				numClustersList.add(numClusters);

				String computedRsfFilename;

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

				performMojoOperationsForComputedRsfFilenameWithExpertDecompositions(
						mojoAvgList, mojoFmAvgList, computedRsfFilename);
			}

		}
		System.out.println("Writing MoJo average, and MoJoFM to csv file " + Config.getMojoToAuthCSVFilename(numClustersList,selectedAlg,simMeasure) + " ...");
		createMojoCSVFile(numClustersList,mojoAvgList,mojoFmAvgList,selectedAlg,simMeasure);

	}
	
	private static void performPrecisionRecallForSingleAuthClustering(String computedRsfFilename, String authClusteringFile) {
		HashSet<HashSet<String>> allIntraPairsFromClustersRsf = IntraPairFromClustersRsfBuilder
				.buildIntraPairsFromClustersRsf(computedRsfFilename);
		
		HashSet<HashSet<String>> allIntraPairsFromAuthClustersRsf = IntraPairFromClustersRsfBuilder
				.buildIntraPairsFromClustersRsf(authClusteringFile);
		
		double precision = PrecisionRecallCalculator.computePrecision(
				allIntraPairsFromClustersRsf,
				allIntraPairsFromAuthClustersRsf);
		String precisionOutput = "Precision of computed data compared to authoritative clustering: " + precision;
		logger.debug(precisionOutput);
		System.out.println(precisionOutput);
		
		double recall = PrecisionRecallCalculator.computeRecall(
				allIntraPairsFromClustersRsf,
				allIntraPairsFromAuthClustersRsf);
		String recallOutput = "Recall of computed data compared to decomposition: " + recall;
		logger.debug(recallOutput);
		System.out.println(recallOutput);
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

	private static void performMojoOperationsForComputedRsfFilenameWithExpertDecompositions(
			List<Double> mojoAvgList, List<Double> mojoFmAvgList,
			String computedRsfFilename) {
		List<ByteArrayOutputStream> baosExpertDecompositions = new ArrayList<ByteArrayOutputStream>();
		for (ExpertDecomposition d : ExpertDecompositionBuilder.expertDecompositions) {
			ByteArrayOutputStream baos = ClusterUtil
					.convertExpertDecompositionToByteArrayOutputStream(d);
			baosExpertDecompositions.add(baos);
		}

		int decompositionCount = 0;
		long mojoSum = 0;
		for (ByteArrayOutputStream baos : baosExpertDecompositions) {
			MoJoCalculator mojoCalc = new MoJoCalculator(
					computedRsfFilename, null, null);
			mojoCalc.targetStream = baos;
			long mojoValue = mojoCalc.mojo();
			String mojoOutput = "MoJo of " + computedRsfFilename
					+ " compared to expert decomposition "
					+ decompositionCount + ": " + mojoValue;
			logger.debug(mojoOutput);
			System.out.println(mojoOutput);
			mojoSum += mojoValue;
			decompositionCount++;
		}
		
		
		decompositionCount = 0;
		double mojoFmSum = 0;
		for (ByteArrayOutputStream baos : baosExpertDecompositions) {
			MoJoCalculator mojoCalc = new MoJoCalculator(
					computedRsfFilename, null, null);
			mojoCalc.targetStream = baos;
			double mojoFmValue = mojoCalc.mojofm();
			String mojoOutput = "MoJoFM of " + computedRsfFilename
					+ " compared to expert decomposition "
					+ decompositionCount + ": " + mojoFmValue;
			logger.debug(mojoOutput);
			System.out.println(mojoOutput);
			mojoFmSum += mojoFmValue;
			decompositionCount++;
		}
		
		double mojoAvg = mojoSum/decompositionCount;
		String mojoAvgOutput = "MoJo averge: " + mojoAvg;
		logger.debug(mojoAvgOutput);
		System.out.println(mojoAvgOutput);
		mojoAvgList.add(mojoAvg);

		double mojoFmAvg = mojoFmSum/decompositionCount;
		String mojoFmAvgOutput = "MoJoFM averge: " + mojoFmAvg;
		logger.debug(mojoFmAvgOutput);
		System.out.println(mojoFmAvgOutput);
		mojoFmAvgList.add(mojoFmAvg);
	}

	/*private static void performMoJoOperationsForConcernRecovery(String selectedAlg,
			String stoppingCriterion) {
		for (int numClusters=Config.getStartNumClustersRange();numClusters<=Config.getEndNumClustersRange();numClusters+=Config.getRangeNumClustersStep()) {
			List<Integer> numTopicsList = new ArrayList<Integer>();
			List<Double> mojoAvgList = new ArrayList<Double>();
			List<Double> mojoFmAvgList = new ArrayList<Double>();
			for (int numTopics = Config.getStartNumTopicsRange();numTopics<=Config.getEndNumTopicsRange();numTopics+=Config.getRangeNumTopicsStep()) {
				numTopicsList.add(numTopics);
				
				String computedRsfFilename = Config.getConcernRecoveryFilePrefix()
						+ selectedAlg
						+ "_"
						+ stoppingCriterion
						+ "_"
						+ numClusters + "_clusters_" + numTopics + "topics.rsf";
				String usingComputedFilename = "Using " + computedRsfFilename
						+ " as computed clusters...";
				logger.debug(usingComputedFilename);
				System.out.println(usingComputedFilename);
				
				performMojoOperationsForComputedRsfFilenameWithExpertDecompositions(mojoAvgList,
						mojoFmAvgList, computedRsfFilename);
				
				
				
			}	
			
			System.out.println("Writing topics, MoJo average, and MoJoFM to csv file to csv file...");
			createMojoCSVWithTopicsFile(numClusters,numTopicsList,mojoAvgList,mojoFmAvgList);
		}
		
		
		
	}*/
	
	private static void performPrecisionRecallOperations(String selectedAlg, String simMeasure,
			String stoppingCriterion) {

		List<Integer> numClustersList = new ArrayList<Integer>();
		List<Double> precisionAvgList = new ArrayList<Double>();
		List<Double> recallAvgList = new ArrayList<Double>();
		for (int numClusters = Config.getStartNumClustersRange(); numClusters <= Config
				.getEndNumClustersRange(); numClusters += Config
				.getRangeNumClustersStep()) {
			numClustersList.add(numClusters);

			String computedRsfFilename = Config.getConcernRecoveryFilePrefix()
					+ selectedAlg + "_" + simMeasure + "_" + stoppingCriterion + "_" + numClusters
					+ "_clusters.rsf";
			HashSet<HashSet<String>> allIntraPairsFromClustersRsf = IntraPairFromClustersRsfBuilder
					.buildIntraPairsFromClustersRsf(computedRsfFilename);
			buildRequiredIntraPairDataFromExpertDecomposition();

			String usingComputedFilename = "Using " + computedRsfFilename
					+ " as computed clusters...";
			logger.debug(usingComputedFilename);
			System.out.println(usingComputedFilename);
			int decompositionCounter = 0;
			double precisionSum = 0;
			for (ExpertDecomposition decomposition : ExpertDecompositionBuilder.expertDecompositions) {
				double precision = PrecisionRecallCalculator.computePrecision(
						allIntraPairsFromClustersRsf,
						decomposition.allIntraPairs);
				String precisionOutput = "Precision of computed data compared to decomposition "
						+ decompositionCounter + " " + precision;
				logger.debug(precisionOutput);
				System.out.println(precisionOutput);
				precisionSum += precision;
				decompositionCounter++;
			}

			decompositionCounter = 0;
			double recallSum = 0;
			for (ExpertDecomposition decomposition : ExpertDecompositionBuilder.expertDecompositions) {
				double recall = PrecisionRecallCalculator.computeRecall(
						allIntraPairsFromClustersRsf,
						decomposition.allIntraPairs);
				String recallOutput = "Recall of computed data compared to decomposition "
						+ decompositionCounter + " " + recall;
				logger.debug(recallOutput);
				System.out.println(recallOutput);
				recallSum += recall;
				decompositionCounter++;
			}

			double precisionAvg = precisionSum / decompositionCounter;
			String precisionAvgOutput = "Precision averge: " + precisionAvg;
			logger.debug(precisionAvgOutput);
			System.out.println(precisionAvgOutput);
			precisionAvgList.add(precisionAvg);

			double recallAvg = recallSum / decompositionCounter;
			String recallAvgOutput = "Recall average: " + recallAvg;
			logger.debug(recallAvgOutput);
			System.out.println(recallAvgOutput);
			recallAvgList.add(recallAvg);

		}// numClusters loop
		System.out
				.println("Writing precision average, and recall average to csv file for clusters...");
		createPrecisionRecallCSVFile(numClustersList, precisionAvgList,
				recallAvgList);

	}

	private static void createPrecisionRecallCSVFile(List<Integer> numClustersList,
			List<Double> precisionAvgList, List<Double> recallAvgList) {
		try {
			FileWriter fstream = new FileWriter(
					Config.getPrecisionRecallCSVFilename(numClustersList));
			BufferedWriter out = new BufferedWriter(fstream);
			writeNumClustersListToFile(numClustersList, out);
			
			out.write("average precision,");
			for (double precisionAvg : precisionAvgList) {
				out.write(precisionAvg + ",");
			}
			out.write("\n");
			
			out.write("average recall,");
			for (double recallAvg : recallAvgList) {
				out.write(recallAvg + ",");
			}
			out.write("\n");
			
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	private static void writeNumClustersListToFile(
			List<Integer> numClustersList, BufferedWriter out) throws IOException {
		out.write("number of clusters,");
		for (int numClusters : numClustersList) {
			out.write(numClusters + ",");
		}
		out.write("\n");
		
	}

	private static void performPrecisionRecallOperationsForConcernRecovery(String selectedAlg, String simMeasure,
			String stoppingCriterion, String computedFilePrefix) {
		
		for (int numClusters=Config.getStartNumClustersRange();numClusters<=Config.getEndNumClustersRange();numClusters+=Config.getRangeNumClustersStep()) {
			List<Integer> numTopicsList = new ArrayList<Integer>();
			List<Double> precisionAvgList = new ArrayList<Double>();
			List<Double> recallAvgList = new ArrayList<Double>();
			for (int numTopics = Config.getStartNumTopicsRange();numTopics<=Config.getEndNumTopicsRange();numTopics+=Config.getRangeNumTopicsStep()) {
				numTopicsList.add(numTopics);
				
				String computedRsfFilename = null;
				
				if (!selectedAlg.equals("arc")) {
					computedRsfFilename = constructNonTopicComputedRsfFilename(computedFilePrefix,
							selectedAlg, simMeasure, stoppingCriterion, numClusters);
				} else {
					computedRsfFilename = constructTopicBasedComputedRsfFilename(computedFilePrefix,
							selectedAlg, simMeasure, stoppingCriterion, numClusters,
							numTopics);
				}
				
				HashSet<HashSet<String>> allIntraPairsFromClustersRsf = IntraPairFromClustersRsfBuilder
						.buildIntraPairsFromClustersRsf(computedRsfFilename);
				buildRequiredIntraPairDataFromExpertDecomposition();

				String usingComputedFilename = "Using " + computedRsfFilename
						+ " as computed clusters...";
				logger.debug(usingComputedFilename);
				System.out.println(usingComputedFilename);
				int decompositionCounter = 0;
				double precisionSum=0;
				for (ExpertDecomposition decomposition : ExpertDecompositionBuilder.expertDecompositions) {
					double precision = PrecisionRecallCalculator
							.computePrecision(allIntraPairsFromClustersRsf,
									decomposition.allIntraPairs);
					String precisionOutput = "Precision of computed data compared to decomposition "
							+ decompositionCounter + " " + precision;
					logger.debug(precisionOutput);
					System.out.println(precisionOutput);
					precisionSum += precision;
					decompositionCounter++;
				}

				decompositionCounter = 0;
				double recallSum=0;
				for (ExpertDecomposition decomposition : ExpertDecompositionBuilder.expertDecompositions) {
					double recall = PrecisionRecallCalculator.computeRecall(
							allIntraPairsFromClustersRsf,
							decomposition.allIntraPairs);
					String recallOutput = "Recall of computed data compared to decomposition "
							+ decompositionCounter + " " + recall;
					logger.debug(recallOutput);
					System.out.println(recallOutput);
					recallSum += recall;
					decompositionCounter++;
				}
				
				double precisionAvg = precisionSum/decompositionCounter;
				String precisionAvgOutput = "Precision averge: " + precisionAvg;
				logger.debug(precisionAvgOutput);
				System.out.println(precisionAvgOutput);
				precisionAvgList.add(precisionAvg);
				
				double recallAvg = recallSum/decompositionCounter;
				String recallAvgOutput = "Recall average: " + recallAvg;
				logger.debug(recallAvgOutput);
				System.out.println(recallAvgOutput);
				recallAvgList.add(recallAvg);
			} // numTopics loop
			System.out.println("Writing topics, precision average, and recall average to csv file...");
			createTopicsPrecisionRecallCSVFile(numClusters,numTopicsList,precisionAvgList,recallAvgList);
		}// numClusters loop
		
	}
	
	private static void createTopicsPrecisionRecallCSVFile(int numClusters, List<Integer> numTopicsList,
			List<Double> precisionAvgList, List<Double> recallAvgList) {
		try {
			FileWriter fstream = new FileWriter(
					Config.getTopicsPrecisionRecallCSVFilename(numClusters));
			BufferedWriter out = new BufferedWriter(fstream);
			writeNumTopicsListToFile(numTopicsList, out);
			
			out.write("average precision,");
			for (double precisionAvg : precisionAvgList) {
				out.write(precisionAvg + ",");
			}
			out.write("\n");
			
			out.write("average recall,");
			for (double recallAvg : recallAvgList) {
				out.write(recallAvg + ",");
			}
			out.write("\n");
			
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	private static void writeNumTopicsListToFile(List<Integer> numTopicsList,
			BufferedWriter out) throws IOException {
		out.write("number of topics,");
		for (int numTopics : numTopicsList) {
			out.write(numTopics + ",");
		}
		out.write("\n");
	}
	
	private static void createMojoListsCSVFile(List<Integer> numClustersList, List<Long> mojoList,
			List<Double> mojoFmList,String selectedAlg, String simMeasure) {
		try {
			FileWriter fstream = new FileWriter(
					Config.getMojoToAuthCSVFilename(numClustersList,selectedAlg, simMeasure));
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
	
	private static void createMojoListsCSVFile(String filename, List<Integer> numClustersList, List<Long> mojoList,
			List<Double> mojoFmList,String selectedAlg, String simMeasure, List<Integer> numTopicsList) {
		try {
			FileWriter fstream = new FileWriter(filename);
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
	
	private static void createMojoToAuthListsCSVFile(List<Integer> numClustersList, List<Long> mojoList,
			List<Double> mojoFmList,String selectedAlg, String simMeasure, List<Integer> numTopicsList) {
		try {
			FileWriter fstream = new FileWriter(
					Config.getMojoToAuthCSVFilename(numClustersList,selectedAlg,simMeasure));
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

	private static void createMojoCSVFile(List<Integer> numClustersList, List<Double> mojoAvgList,
			List<Double> mojoFmAvgList,String selectedAlg, String simMeasure) {
		try {
			FileWriter fstream = new FileWriter(
					Config.getMojoToAuthCSVFilename(numClustersList,selectedAlg, simMeasure));
			BufferedWriter out = new BufferedWriter(fstream);
			
			/*out.write("decomposition,");
			for (int i=0;i<mojoAvgList.size();i++) {
				out.write(i + ",");
			}
			out.write("\n");*/
			
			out.write("number of clusters,");
			for (int numClusters : numClustersList) {
				out.write(numClusters + ",");
			}
			out.write("\n");
			
			writeMojoAveragesToFile(mojoAvgList, mojoFmAvgList, out);
			
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	private static void writeMojoAveragesToFile(List<Double> mojoAvgList,
			List<Double> mojoFmAvgList, BufferedWriter out) throws IOException {
		out.write("MoJo average,");
		for (double mojoAvg : mojoAvgList) {
			out.write(mojoAvg + ",");
		}
		out.write("\n");
		
		out.write("MoJoFM average,");
		for (double mojoFm : mojoFmAvgList) {
			out.write(mojoFm + ",");
		}
		out.write("\n");
	}
	
	/*private static void createMojoCSVWithTopicsFile(int numClusters, List<Integer> numTopicsList, List<Double> mojoAvgList, List<Double> mojoFmAvgList) {
		try {
			FileWriter fstream = new FileWriter(
					Config.getMojoWithTopicsCSVFilename(numClusters));
			BufferedWriter out = new BufferedWriter(fstream);
			writeNumTopicsListToFile(numTopicsList, out);
			
			writeMojoAveragesToFile(mojoAvgList, mojoFmAvgList, out);
			
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}*/


	private static void buildRequiredIntraPairDataFromExpertDecomposition() {
		ExpertDecompositionBuilder.buildIntraPairsForExpertDecompositions();
	}

	private static void buildExpertDecompositionClustersFromRSFFile() {
		System.out.println("Creating expert decomposition...");
		System.out.println("Reading in rsf file...");
		RsfReader.loadRsfDataForCurrProj();
		//RsfReader.performPreClusteringTasks();
		ExpertDecompositionBuilder.readInExpertDecomposition(Config.getExpertDecompositionFile());
		ExpertDecompositionBuilder.buildExpertDecompositionClusters(RsfReader.startNodesSet);
	}
}

