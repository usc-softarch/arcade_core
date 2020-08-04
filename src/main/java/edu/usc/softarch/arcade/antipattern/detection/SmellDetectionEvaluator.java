package edu.usc.softarch.arcade.antipattern.detection;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
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
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import edu.usc.softarch.arcade.config.Config;
import edu.usc.softarch.arcade.facts.ConcernCluster;
import edu.usc.softarch.arcade.util.FileUtil;

public class SmellDetectionEvaluator {
	private static Logger logger = Logger.getLogger(SmellDetectionEvaluator.class);
	
	// the key is a pair consisting of a smell type and technique filename
	// the value is the ratio of the matched smell count for that type over the number of ground truth smells for that type for the technique
	// this is essentially the coverage ratio for the technique over a smell type
	public static Map<Pair<Class,String>,Double> smellTypeTechRatioMap =
		new TreeMap<>((Pair<Class, String> o1, Pair<Class, String> o2) ->
			o1.getLeft().getName().compareTo(o2.getLeft().getName()));
	public static boolean configureLogging = true;
	
	public static void resetData() {
		smellTypeTechRatioMap = new HashMap<>();
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
		File[] detectedSmellsFiles = techDirFile.listFiles(
			(File file) -> file.getName().endsWith(".ser")
		);

		// obtain names of detected smells files in techniques directory
		List<File> dsFileList = Arrays.asList(detectedSmellsFiles);
		List<String> dsFilenames = Lists.newArrayList(Iterables.transform(
				dsFileList, (final File file) -> file.getAbsolutePath()
				));

		logger.debug(Joiner.on(",").join(dsFilenames));
		
		Map<String,Set<Smell>> fileToSmellInstancesMap = new LinkedHashMap<>();
		for (String dsFilename : dsFilenames) {
			Set<Smell> detectedSmells = SmellUtil.deserializeDetectedSmells(dsFilename);
			fileToSmellInstancesMap.put(dsFilename,detectedSmells);
		}
		
		logger.debug("");
		logger.debug("Listing technique filenames: ");
		logger.debug(Joiner.on("\n").join(fileToSmellInstancesMap.keySet()));

		// Key: Ground-truth smell, Value: All matching smell and the technique from which the smell originates
		Map<Smell,Set<Pair<Smell,String>>> gtSmellToPairMap = new HashMap<>();
		
		// Key: ser filename, Value: coverage ratio
		Map<String,Double> fileCoverageMap = new HashMap<>();
		for (Entry<String, Set<Smell>> fsiEntry : fileToSmellInstancesMap.entrySet()) {
			String currFilename = fsiEntry.getKey();
			Set<Smell> detectedTechSmells = fsiEntry.getValue();
			
			Map<Smell,Smell> maxSmellMap = new LinkedHashMap<>();
			for (Smell gtSmell : detectedGtSmells) {
				double maxSim = 0;
				Smell maxSmell = null;
				for (Smell techSmell : detectedTechSmells) {
					if (gtSmell.getClass().equals(techSmell.getClass())) {
						double sim = calcSimOfSmellInstances(gtSmell,techSmell);
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
						Pair<Smell,String> smellFilePair = new ImmutablePair<>(maxSmell,currFilename);
						if (gtSmellToPairMap.containsKey(gtSmell)) {
							Set<Pair<Smell,String>> pairs = gtSmellToPairMap.get(gtSmell);
							pairs.add(smellFilePair);
						}
						else {
							Set<Pair<Smell,String>> pairs = new HashSet<>();
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
		Map<Class,AtomicInteger> gtSmellTypeCountMap = new HashMap<>();
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
		Map<Pair<Class,String>,AtomicInteger> matchedSmellTypeCountMap = new HashMap<>();
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
							Pair<Class, String> pair = new ImmutablePair<>(
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
				dsFilenames, (final String filename) -> FileUtil.extractFilenamePrefix(filename)
				));
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
					Pair<Class, String> pair = new ImmutablePair<>(
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
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		double numofMatchedGtSmells = (double)gtSmellToPairMap.keySet().size();
		double numOfTotalGtSmells = (double)detectedGtSmells.size();
		
		logger.debug("");
		logger.debug("number of matched gt smells: " + numofMatchedGtSmells);
		logger.debug("number of total gt smells: " + numOfTotalGtSmells);
		double detectedGtSmellsRatio = numofMatchedGtSmells/numOfTotalGtSmells;
		logger.debug("Ratio of detected gt smells: " + detectedGtSmellsRatio);
	}

	private static void computePearsonCorrelationOfMojoFMToSmellAccuracy(
			String smellFileToMojoFilename, Map<String, Double> fileCoverageMap) {
		// key: smell filename, value: mojofm score
		List<Pair<String, Double>> fileScorePairs = new ArrayList<>();
		Path smellFileToMojoPath = Paths.get(smellFileToMojoFilename);
		try (InputStream in = Files.newInputStream(smellFileToMojoPath);
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(in))) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] tokens = line.split(",");
				String filename = tokens[0];
				double mojoFmScore = Double.parseDouble(tokens[1]);
				fileScorePairs.add(new ImmutablePair<>(filename,
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
		Set<String> gtEntities = new HashSet<>();
		Set<String> techEntities = new HashSet<>();
		
		for (ConcernCluster cluster : gtSmell.clusters) {
			gtEntities.addAll(cluster.getEntities());
		}
		for (ConcernCluster cluster : techSmell.clusters) {
			techEntities.addAll(cluster.getEntities());
		}
		
		Set<String> intersection = new HashSet<>(gtEntities);
		intersection.retainAll(techEntities);
		
		Set<String> union = new HashSet<>(gtEntities);
		union.addAll(techEntities);
		
		// simRatio
		return (double)intersection.size()/(double)union.size();
	}
}
