package edu.usc.softarch.arcade.antipattern.detection;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.thoughtworks.xstream.XStream;

import edu.usc.softarch.arcade.config.Config;
import edu.usc.softarch.arcade.facts.ConcernCluster;
import edu.usc.softarch.arcade.util.FileUtil;

public class SmellDetectionEvaluator {
	
	
	static Logger logger = Logger.getLogger(SmellDetectionEvaluator.class);
	
	// the key is a pair consisting of a smell type and technique filename
	// the value is the ratio of the matched smell count for that type over the number of ground truth smells for that type for the technique
	// this is essentially the coverage ratio for the technique over a smell type
	public static Map<Pair<Class,String>,Double> smellTypeTechRatioMap = new TreeMap<Pair<Class,String>,Double>(new ClassStringPairComparator());
	public static boolean configureLogging = true;
	
	public static void resetData() {
		smellTypeTechRatioMap = new HashMap<Pair<Class,String>,Double>();
	}
	
	public static class FileScorePairAscending implements Comparator<Pair<String,Double>>{

	    public int compare(Pair<String,Double> p1, Pair<String,Double> p2) {
	        return p1.getRight().compareTo(p2.getRight()); // ascending order
	    }
	}
	
	public static class FileScorePairDescending implements Comparator<Pair<String,Double>>{

	    public int compare(Pair<String,Double> p1, Pair<String,Double> p2) {
	        return p2.getRight().compareTo(p1.getRight()); // descending order
	    }
	}

	public static void main(String[] args) {
		if (configureLogging) {
			PropertyConfigurator.configure(Config.getLoggingConfigFilename());
		}
		
		resetData();
		
		String groundTruthFile = args[0];
		String techniquesDir = args[1];
		String smellTechTableFilename = args[2];
		String detectedSmellsGtFilename = args[3];
		String smellFileToMojoFilename = null;
		if (args.length == 5) {
			smellFileToMojoFilename = args[4];
		}
		String gtFileOutput = "ground truth file: " + groundTruthFile;
		String techDirOutput = "techniques directory: " + techniquesDir;
		System.out.println(gtFileOutput);
		logger.debug(gtFileOutput);
		System.out.println(techDirOutput);
		logger.debug(techDirOutput);

		
		File techDirFile = new File(techniquesDir);
		assert techDirFile.exists() : techniquesDir + " does not exist.";
		
		
		// Deserialize detected gt smells
		Set<Smell> detectedGtSmells = SmellUtil.deserializeDetectedSmells(detectedSmellsGtFilename);
		logger.debug("");
		logger.debug("Listing detected gt smells: ");
		for (Smell smell : detectedGtSmells) {
			logger.debug(SmellUtil.getSmellAbbreviation(smell) + " " + smell);
		}
		
		// obtain ser files in techniques directory
		File[] detectedSmellsFiles = techDirFile.listFiles(new FileFilter() {
			public boolean accept(File file) {
				return file.getName().endsWith(".ser");
			}
		});

		// obtain names of detected smells files in techniques directory
		List<File> dsFileList = Arrays.asList(detectedSmellsFiles);
		List<String> dsFilenames = Lists.newArrayList(Iterables.transform(
				dsFileList, new Function<File, String>() {

					public String apply(final File file) {
						return file.getAbsolutePath();
					}
				}));

		logger.debug(Joiner.on(",").join(dsFilenames));
		
		Map<String,Set<Smell>> fileToSmellInstancesMap = new LinkedHashMap<String,Set<Smell>>();
		for (String dsFilename : dsFilenames) {
			Set<Smell> detectedSmells = SmellUtil.deserializeDetectedSmells(dsFilename);
			fileToSmellInstancesMap.put(dsFilename,detectedSmells);
		}
		
		logger.debug("");
		logger.debug("Listing technique filenames: ");
		logger.debug(Joiner.on("\n").join(fileToSmellInstancesMap.keySet()));

		// Key: Ground-truth smell, Value: All matching smell and the technique from which the smell originates
		Map<Smell,Set<Pair<Smell,String>>> gtSmellToPairMap = new HashMap<Smell,Set<Pair<Smell,String>>>();
		
		
		// Key: ser filename, Value: coverage ratio
		Map<String,Double> fileCoverageMap = new HashMap<String,Double>();
		for (Entry<String, Set<Smell>> fsiEntry : fileToSmellInstancesMap.entrySet()) {
			String currFilename = fsiEntry.getKey();
			Set<Smell> detectedTechSmells = fsiEntry.getValue();
			
			Map<Smell,Smell> maxSmellMap = new LinkedHashMap<Smell,Smell>();
			for (Smell gtSmell : detectedGtSmells) {
				double maxSim = 0;
				Smell maxSmell = null;
				for (Smell techSmell : detectedTechSmells) {
					if (gtSmell.getClass().equals(techSmell.getClass())) {
						double sim = calcSimOfSmellInstances(gtSmell,techSmell);
						//logger.debug(gtSmell + ", " + techSmell + ": " + sim);
						if (sim > maxSim) {
							maxSim = sim;
							maxSmell = techSmell;
						}
					}
				}
				maxSmellMap.put(gtSmell, maxSmell);
			}
			
			logger.debug("");
			logger.debug("Each ground-truth smell and it's max match from " + currFilename + ": ");
			for (Smell gtSmell : maxSmellMap.keySet()) {
				Smell techSmell = maxSmellMap.get(gtSmell);
				if (techSmell != null) {
					double sim = calcSimOfSmellInstances(gtSmell, techSmell);
					logger.debug(SmellUtil.getSmellAbbreviation(gtSmell) + " " + gtSmell + ", " + techSmell + ": " + sim);
				} else {
					logger.debug(SmellUtil.getSmellAbbreviation(gtSmell) + " " + gtSmell + ", " + techSmell + ": 0");
				}
			}

			logger.debug("");
			logger.debug("Smells > 0.5 matching for " + currFilename + ": ");
			int above50Count = 0;
			for (Smell gtSmell : maxSmellMap.keySet()) {
				Smell maxSmell = maxSmellMap.get(gtSmell);
				if (maxSmell != null) {
					double sim = calcSimOfSmellInstances(gtSmell, maxSmell);
					if (sim > .5) {
						logger.debug(SmellUtil.getSmellAbbreviation(gtSmell) + " " + gtSmell + ", " + maxSmell + ": " + sim);
						Pair<Smell,String> smellFilePair = new ImmutablePair<Smell,String>(maxSmell,currFilename);
						if (gtSmellToPairMap.containsKey(gtSmell)) {
							Set<Pair<Smell,String>> pairs = gtSmellToPairMap.get(gtSmell);
							pairs.add(smellFilePair);
						}
						else {
							Set<Pair<Smell,String>> pairs = new HashSet<Pair<Smell,String>>();
							pairs.add(smellFilePair);
							gtSmellToPairMap.put(gtSmell,pairs);
						}
						
						above50Count++;
					}
				}
			}
			double coverageRatio = (double)above50Count/(double)maxSmellMap.keySet().size();
			logger.debug("Ratio of Smells above 0.5 matching: " + coverageRatio);
			fileCoverageMap.put(currFilename,coverageRatio);
		}

		if (args.length == 5) {
			computePearsonCorrelationOfMojoFMToSmellAccuracy(
					smellFileToMojoFilename, fileCoverageMap);
		}
		
		
		String allHighMatchedSmellsStr = Joiner.on("\n").withKeyValueSeparator("=").join(gtSmellToPairMap);
		logger.debug("");
		logger.debug("All high matched smells: ");
		logger.debug(allHighMatchedSmellsStr);
		
		for (Smell gtSmell : gtSmellToPairMap.keySet()) {
			Set<Pair<Smell,String>> pairs = gtSmellToPairMap.get(gtSmell);
			for (Pair<Smell,String> smellFilePair : pairs) {
				logger.debug(gtSmell + " -> " + smellFilePair.getLeft() + " from " + smellFilePair.getRight());
			}
		}
		
		logger.debug("");
		logger.debug("Showing matched and unmatched smells:");
		for (Smell gtSmell : detectedGtSmells) {
			if (gtSmellToPairMap.containsKey(gtSmell)) {
				Set<Pair<Smell, String>> smellFilePairs = gtSmellToPairMap
						.get(gtSmell);
				for (Pair<Smell, String> smellFilePair : smellFilePairs) {
					logger.debug(gtSmell + " -> " + smellFilePair.getLeft()
							+ " from " + smellFilePair.getRight());
				}
			}
			else {
				logger.debug(gtSmell + " has no match");
			}
		}
		
		// key: a type of smell, value: the number of instances of that type of smell in the ground truth
		Map<Class,AtomicInteger> gtSmellTypeCountMap = new HashMap<Class,AtomicInteger>();
		for (Class smellClass : SmellUtil.getSmellClasses()) {
			for (Smell gtSmell : detectedGtSmells) {
				if (smellClass.isInstance(gtSmell)) {
					if (gtSmellTypeCountMap.containsKey(smellClass)) {
						gtSmellTypeCountMap.get(smellClass).incrementAndGet();
					}
					else {
						gtSmellTypeCountMap.put(smellClass, new AtomicInteger(1));
					}
					
				}
			}
		}
		
		logger.debug("");
		logger.debug("Number of instances for each smell type in groud-truth smells:");
		logger.debug(Joiner.on("\n").withKeyValueSeparator("=").join(gtSmellTypeCountMap));
		
		// map containing the number of instances a technique matched a smell type
		// key: (type of smell, technique filename), value: the number of instances a technique matched a smell type
		Map<Pair<Class,String>,AtomicInteger> matchedSmellTypeCountMap = new HashMap<Pair<Class,String>,AtomicInteger>();
		logger.debug("");
		logger.debug("Populating the map counting how many times a smell type is matched by a technique:");
		for (Smell gtSmell : detectedGtSmells) {
			
			if (gtSmellToPairMap.containsKey(gtSmell)) {
				Set<Pair<Smell, String>> smellFilePairs = gtSmellToPairMap
						.get(gtSmell);
				for (Pair<Smell, String> smellFilePair : smellFilePairs) {
					Smell matchedSmell = smellFilePair.getLeft();
					String resultsFilename = smellFilePair.getRight();
					for (Class smellClass : SmellUtil.getSmellClasses()) {
						if (smellClass.isInstance(matchedSmell)) {
							logger.debug(resultsFilename + ":" + matchedSmell
									+ " is an instance of "
									+ smellClass.getSimpleName());
							Pair<Class, String> pair = new ImmutablePair<Class, String>(
									smellClass, resultsFilename);
							if (matchedSmellTypeCountMap.containsKey(pair)) {
								matchedSmellTypeCountMap.get(pair)
										.incrementAndGet();
							} else {
								matchedSmellTypeCountMap.put(pair,
										new AtomicInteger(1));
							}
						}
					}
				}
			} else {
				logger.debug(gtSmell + " has no match");
			}
		}
		
		logger.debug("");
		logger.debug("Number of instances for each smell type in matched smells:");
		logger.debug(Joiner.on("\n").withKeyValueSeparator("=").join(matchedSmellTypeCountMap));
		
		List<String> dsShortFilenames = Lists.newArrayList(Iterables.transform(
				dsFilenames, new Function<String, String>() {

					public String apply(final String filename) {
						return FileUtil.extractFilenamePrefix(filename);
					}
				}));
		String smellTechTable = "";
		smellTechTable += "," + Joiner.on(",").join(dsShortFilenames);
		smellTechTable += "\n";
		for (Class smellClass : SmellUtil.getSmellClasses()) {
			smellTechTable += smellClass.getSimpleName() + ",";
			if (!gtSmellTypeCountMap.containsKey(smellClass)) {
				for (String filename : dsFilenames) {
					smellTechTable += "NA,";
				}
			} else {
				for (String filename : dsFilenames) {
					Pair<Class, String> pair = new ImmutablePair<Class, String>(
							smellClass, filename);
					if (matchedSmellTypeCountMap.containsKey(pair)) {
						double ratio = (double) matchedSmellTypeCountMap.get(
								pair).get()
								/ (double) gtSmellTypeCountMap.get(
										pair.getLeft()).get();
						smellTypeTechRatioMap.put(pair, ratio);
						smellTechTable += ratio + ",";
					} else {
						smellTechTable += "0,";
					}
				}
			}
			smellTechTable += "\n";
		}
		logger.debug("\n" + smellTechTable);
		
		PrintWriter writer;
		try {
			writer = new PrintWriter(smellTechTableFilename, "UTF-8");
			
			writer.println(smellTechTable);
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		double numofMatchedGtSmells = (double)gtSmellToPairMap.keySet().size();
		double numOfTotalGtSmells = (double)detectedGtSmells.size();
		
		logger.debug("");
		logger.debug("number of matched gt smells: " + numofMatchedGtSmells);
		logger.debug("number of total gt smells: " + numOfTotalGtSmells);
		double detectedGtSmellsRatio = numofMatchedGtSmells/numOfTotalGtSmells;
		logger.debug("Ratio of detected gt smells: " + detectedGtSmellsRatio);
		
		//analyzeSmellsPerTypeAcrossTechniques(groundTruthFile,
		//		smellTechTableFilename, csvFileToSmellsMap, techDirFile);
		
		
		
	}

	private static void computePearsonCorrelationOfMojoFMToSmellAccuracy(
			String smellFileToMojoFilename, Map<String, Double> fileCoverageMap) {
		// key: smell filename, value: mojofm score
		List<Pair<String, Double>> fileScorePairs = new ArrayList<Pair<String, Double>>();
		Path smellFileToMojoPath = Paths.get(smellFileToMojoFilename);
		try (InputStream in = Files.newInputStream(smellFileToMojoPath);
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(in))) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] tokens = line.split(",");
				String filename = tokens[0];
				double mojoFmScore = Double.parseDouble(tokens[1]);
				fileScorePairs.add(new ImmutablePair<String, Double>(filename,
						mojoFmScore));
			}
		} catch (IOException x) {
			System.err.println(x);
		}

		Collections.sort(fileScorePairs, new FileScorePairAscending());
		logger.debug("");
		for (Pair<String, Double> pair : fileScorePairs) {
			logger.debug(pair.getLeft() + "," + pair.getRight());
		}
	
		double[] coverageArray = new double[fileCoverageMap.entrySet().size()];
		double[] scoreArray = new double[fileScorePairs.size()];
		int i=0;
		for (Pair<String,Double> pair : fileScorePairs) {
			String filename = pair.getLeft();
			double score = pair.getRight();
			double coverage = fileCoverageMap.get(filename);
			scoreArray[i] = score;
			coverageArray[i] = coverage;
			i++;			
		}
		
		PearsonsCorrelation pearsons = new PearsonsCorrelation();
		double correlationCoefficient = pearsons.correlation(scoreArray, coverageArray);
		
		List<Double> coverageList = Arrays.asList( ArrayUtils.toObject(coverageArray) );
		List<Double> scoreList = Arrays.asList( ArrayUtils.toObject(scoreArray) );
		logger.debug("");
		logger.debug("MoJoFM Scores:");
		logger.debug(scoreList);
		logger.debug("Coverage Ratios:");
		logger.debug(coverageList);
		logger.debug("MoJoFM to Coverage correlation: " + correlationCoefficient);
	}

	private static double calcSimOfSmellInstances(Smell gtSmell, Smell techSmell) {
		Set<String> gtEntities = new HashSet<String>();
		Set<String> techEntities = new HashSet<String>();
		
		for (ConcernCluster cluster : gtSmell.clusters) {
			gtEntities.addAll(cluster.getEntities());
		}
		for (ConcernCluster cluster : techSmell.clusters) {
			techEntities.addAll(cluster.getEntities());
		}
		
		Set<String> intersection = new HashSet<String>(gtEntities);
		intersection.retainAll(techEntities);
		
		Set<String> union = new HashSet<String>(gtEntities);
		union.addAll(techEntities);
		
		double simRatio = (double)intersection.size()/(double)union.size();
		
		return simRatio;
	}

	private static void analyzeSmellsPerTypeAcrossTechniques(
			String groundTruthFile, String smellTechTableFilename,
			File techDirFile) {
		Map<String, Map<String, Set<String>>  > csvFileToSmellsMap = 
				new HashMap<String, Map<String, Set<String>>  >();
		
		extractSmellsInfoFromFile(groundTruthFile, csvFileToSmellsMap);
		
		// obtain csv files in techniques directory
		File[] csvFiles = techDirFile.listFiles(new FileFilter() {
			public boolean accept(File file) {
				return file.getName().endsWith(".csv");
			}
		});
		
		// obtain names of csv files in techniques directory
		List<File> csvFileList = Arrays.asList(csvFiles);
		List<String> csvFilenames = Lists.newArrayList(Iterables
				.transform(csvFileList, new Function<File, String>() {

					public String apply(final File file) {
						return file.getAbsolutePath();
					}
				}));
		
		logger.debug(Joiner.on(",").join(csvFilenames));
		
		// create csv file to smells map for each csv file
		for (String csvFilename : csvFilenames) {
			extractSmellsInfoFromFile(csvFilename,csvFileToSmellsMap);
		}

		// show some info about the smells in each csv file
		for (Entry<String, Map<String, Set<String>>> f2sEntry : csvFileToSmellsMap.entrySet()) {
			logger.debug("Current file: " + f2sEntry.getKey());
			int valuesLimit = 10;
			logSmellToClassesMap(f2sEntry.getValue(), valuesLimit);
		}
		
		Map<String, Set<String>>  gtSmellsMap = csvFileToSmellsMap.get(groundTruthFile);
		
		Map<Pair<String,String>, Double> stRatioMap = new HashMap<Pair<String,String>, Double>();
		
		for (String gtSmell : gtSmellsMap.keySet()) {
			Set<String> gtClasses = gtSmellsMap.get(gtSmell);
			logger.debug("\n");
			for (Entry<String, Map<String, Set<String>>> f2sEntry : csvFileToSmellsMap.entrySet()) {
				String techFilename = f2sEntry.getKey();
				Map<String, Set<String>> techSmellsMap = f2sEntry.getValue();
				Set<String > techClasses = techSmellsMap.get(gtSmell); // use ground truth smell to get corresponding techniques classes that also have the same smell
				
				if (techClasses == null) {
					techClasses = new HashSet<String>();
				}
				
				Set<String> intersectingClasses = new HashSet<String>(gtClasses);
				intersectingClasses.retainAll(techClasses);
				
				Set<String> unionOfClasses = new HashSet<String>(gtClasses);
				unionOfClasses.addAll(techClasses);
				
				double ratio = (double)intersectingClasses.size()/(double)unionOfClasses.size();
				//double ratio = (double)intersectingClasses.size()/(double)techClasses.size();
				logger.debug("gt with " + techFilename + " for " + gtSmell + ": " + ratio);
				
				Pair<String,String> smellTechPair = new ImmutablePair<String,String>(gtSmell, techFilename);
				stRatioMap.put(smellTechPair, ratio);
			}			
		}
		
		List<String> smellNames = new ArrayList<String>(gtSmellsMap.keySet());
		
		List<String> csvFilenamesNoPaths = Lists.newArrayList(Iterables
				.transform(csvFilenames, new Function<String, String>() {

					public String apply(final String filename) {
						return filename.substring(filename.lastIndexOf(File.separatorChar)+1,filename.length()-1);
						
					}
				}));
		
		String smellTechTable = "";
		String header = "," + Joiner.on(",").join(csvFilenamesNoPaths);
		
		smellTechTable += "\n";
		smellTechTable += header + "\n";
		for (String smell : smellNames) {
			String row = smell + ",";
			for (int i=0;i<csvFilenames.size();i++) {
				double ratio = stRatioMap.get(new ImmutablePair<String,String>(smell,csvFilenames.get(i)));
				row += ratio + ",";
			}
			row +="\n";
			smellTechTable += row;
		}
		logger.debug(smellTechTable);
		
		PrintWriter writer;
		try {
			writer = new PrintWriter(smellTechTableFilename, "UTF-8");
			
			writer.println(smellTechTable);
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void extractSmellsInfoFromFile(String groundTruthFile,
			Map<String, Map<String, Set<String>>> fileToSmellsMap) {
		try {
			BufferedReader reader;
			reader = new BufferedReader(new FileReader(groundTruthFile));
			String line = null;
			
			Map<String, Set<String>> smellToClassesMap = new HashMap<String, Set<String>>();
			while ((line = reader.readLine()) != null) {
				// System.out.println(line);
				List<String> lineElements = Arrays.asList(line.split(","));
				String smell = lineElements.get(0);
				Set<String> classes = new HashSet(lineElements.subList(1,
						lineElements.size() - 1));
				smellToClassesMap.put(smell, classes);
			}
			final String result = Joiner.on("\n")
					.withKeyValueSeparator("=")
					.join(smellToClassesMap);
			logger.debug(result);
			
			fileToSmellsMap.put(groundTruthFile,smellToClassesMap);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void logSmellToClassesMap(
			Map<String, Set<String>> smellToClassesMap, int valuesLimit) {
		int valuesLimitIndex = valuesLimit - 1;
		for (Entry<String,Set<String>> s2cEntry : smellToClassesMap.entrySet()) {
			List<String> classesList = new ArrayList<String>(s2cEntry.getValue());
			List<String> limitedClassesList = null;
			if (classesList.size() < valuesLimit) {
				limitedClassesList = classesList;
			}
			else {
				limitedClassesList = classesList.subList(0, valuesLimitIndex);
			}
			String formattedEntry = s2cEntry.getKey() + " : " + limitedClassesList;
			logger.debug(formattedEntry);
		}
	}

}
