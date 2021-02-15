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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.log4j.PropertyConfigurator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.usc.softarch.arcade.antipattern.Smell;
import edu.usc.softarch.arcade.facts.ConcernCluster;
import edu.usc.softarch.arcade.util.FileUtil;

/**
 * Calculates the total number of smells in a ground-truth architecture, the
 * number of ground-truth smells that were matched by an analysis result, the
 * ratio of detected ground-truth smells.
 */
public class SmellDetectionEvaluator {
	// #region ATTRIBUTES --------------------------------------------------------
	private static Logger logger = 
		LogManager.getLogger(SmellDetectionEvaluator.class);

	/**
	 * Comparator for ascending order sorting of <File,Score> pairings.
	 */
	public static class FileScorePairAscending
			implements Comparator<Entry<String,Double>> {
		public int compare(Entry<String,Double> p1, Entry<String,Double> p2) {
			return p1.getValue().compareTo(p2.getValue());
		}
	}

	/**
	 * Comparator for descending order sorting of <File,Score> pairings.
	 */
	public static class FileScorePairDescending
			implements Comparator<Entry<String,Double>> {
		public int compare(Entry<String,Double> p1, Entry<String,Double> p2) {
			return p2.getValue().compareTo(p1.getValue());
		}
	}
	
	/**
	 * This is a map representing the coverage ratio for a technique over a smell
	 * type. The key is a pair consisting of a smell type and a technique
	 * filename. The value is the ratio of the matched smell count for that type
	 * over the number of ground truth smells for that type for the technique.
	 */
	private static Map<Entry<Smell.SmellType,String>,Double> smellTypeTechRatioMap
		= new HashMap<>();
	// #endregion ATTRIBUTES -----------------------------------------------------

	public static void main(String[] args) throws IOException {
		// Setting up variables
		String fs = File.separator;
		PropertyConfigurator.configure("cfg" + fs + "extractor_logging.cfg");

		/**
		 * Key: Ground-truth smell
		 * Value: All matching smells and the technique from which the smell comes
		 */
		Map<Smell, Set<Entry<Smell, String>>> gtSmellToPairMap = new HashMap<>();
		
		/**
		 * Key: .ser filename
		 * Value: coverage ratio
		 */
		Map<String, Double> fileCoverageMap = new HashMap<>();
		
		// Loading arguments
		String groundTruthFile = args[0];
		String techniquesDir = args[1];
		String smellTechTableFilename = args[2];
		String detectedSmellsGtFilename = args[3];
		String smellFileToMojoFilename = (args.length == 5) ? args[4] : null;
		String gtFileOutput = "ground truth file: " + groundTruthFile;
		String techDirOutput = "techniques directory: " + techniquesDir;
		logger.debug(gtFileOutput);
		logger.debug(techDirOutput);
		File techDirFile = new File(techniquesDir);
		if (!techDirFile.exists())
			throw new IOException(techniquesDir + " does not exist.");
		
		// Deserialize ground-truth smells
		Set<Smell> detectedGtSmells =
			SmellUtil.deserializeDetectedSmells(detectedSmellsGtFilename);
		logger.debug("");
		logger.debug("Listing detected gt smells: ");
		for (Smell smell : detectedGtSmells)
			logger.debug(SmellUtil.getSmellAbbreviation(smell) + " " + smell);
		
		// Obtain .ser files from the results of analysis techniques
		File[] detectedSmellsFiles = techDirFile.listFiles(
			(File file) -> file.getName().endsWith(".ser"));

		// Obtain names of detected smells files in techniques directory
		List<File> dsFileList = Arrays.asList(detectedSmellsFiles);
		List<String> dsFilenames = dsFileList.stream() // Turn into a stream
			.map(File::getAbsolutePath)                  // Get absolute paths
			.collect(Collectors.toList());               // Turn back into List

		logger.debug(String.join(",", dsFilenames));
		
		// Deserialize smell files from analysis results
		Map<String,Set<Smell>> fileToSmellInstancesMap = new LinkedHashMap<>();
		for (String dsFilename : dsFilenames) {
			Set<Smell> detectedSmells =
				SmellUtil.deserializeDetectedSmells(dsFilename);
			fileToSmellInstancesMap.put(dsFilename, detectedSmells);
		}
		
		logger.debug("");
		logger.debug("Listing technique filenames: ");
		logger.debug(String.join("\n", fileToSmellInstancesMap.keySet()));

		// For each .ser file from the technique analysis results
		for (Entry<String, Set<Smell>> fsiEntry
				: fileToSmellInstancesMap.entrySet()) {
			String currFilename = fsiEntry.getKey();
			Set<Smell> detectedTechSmells = fsiEntry.getValue();
			
			Map<Smell, Entry<Smell, Double>> maxSmellMap = new LinkedHashMap<>();

			/**
			 * Compares smells from the ground-truth with the smells from the results
			 * of the analysis technique. Looks for those matches with the highest
			 * similarity degree, so as to measure which ground-truth smells were
			 * detected by the analysis technique.
			 */
			// For each smell in the ground-truth
			for (Smell gtSmell : detectedGtSmells) {
				double maxSim = 0;
				Smell maxSmell = null;
				// For each smell in the analysis result
				for (Smell techSmell : detectedTechSmells) {
					// If the two smells are of matching type
					if (gtSmell.getSmellType().equals(techSmell.getSmellType())) {
						// Calculate their similarity
						double sim = calcSimOfSmellInstances(gtSmell, techSmell);
						// And if it is higher than the previous max similarity, record it
						if (sim > maxSim) {
							maxSim = sim;
							maxSmell = techSmell;
						}
					}
				}
				// Finally, put the two matching smells in the result collection
				maxSmellMap.put(gtSmell, Map.entry(maxSmell, maxSim));
			}
			
			// Logging the results for this round
			logger.debug("");
			logger.debug("Each ground-truth smell and it's max match from "
				+ currFilename + ": ");
			for (Smell gtSmell : maxSmellMap.keySet()) {
				Entry<Smell, Double> techSmell = maxSmellMap.get(gtSmell);
				if (techSmell == null) {
					logger.debug(gtSmell.getSmellType() + " " + gtSmell + ", "
						+ techSmell + ": 0");
					continue;
				}
				Smell smellInstance = techSmell.getKey();
				double sim = techSmell.getValue();
				logger.debug(gtSmell.getSmellType() + " " + gtSmell + ", "
					+ smellInstance + ": " + sim);
			}

			// Logging the results of this round that had more than 50% similarity
			logger.debug("");
			logger.debug("Smells > 0.5 matching for " + currFilename + ": ");
			int above50Count = 0;
			// For each smell instance in the ground truth
			for (Smell gtSmell : maxSmellMap.keySet()) {
				Entry<Smell, Double> maxSmell = maxSmellMap.get(gtSmell);

				// If that smell instance has a match in the technique results
				// And its similarity is higher than 50%
				if (maxSmell != null && maxSmell.getValue() > .5) {
					Smell smellInstance = maxSmell.getKey();
					double sim = maxSmell.getValue();

					// Log it in the list of > 50% similarity smells
					logger.debug(gtSmell.getSmellType() + " " + gtSmell + ", " 
						+ smellInstance + ": " + sim);
					Entry<Smell, String> smellFilePair = Map.entry(smellInstance, currFilename);

					Set<Entry<Smell, String>> pairs =
						gtSmellToPairMap.containsKey(gtSmell) // If that gtSmell was already matched
						? gtSmellToPairMap.get(gtSmell)       // Then get the list of matches
						: new HashSet<>();                    // Otherwise initialize one

					// Update the list of matches
					pairs.add(smellFilePair);
					gtSmellToPairMap.put(gtSmell, pairs);
					
					// And increment the count
					above50Count++;
				}
			}

			double coverageRatio =
				(double) above50Count / (double) maxSmellMap.keySet().size();
			logger.debug("Ratio of Smells above 0.5 matching: " + coverageRatio);
			fileCoverageMap.put(currFilename, coverageRatio);
		}

		// If a MojoFM scores file is available, run Pearson correlation test
		if (smellFileToMojoFilename != null)
			computePearsonCorrelationOfMojoFMToSmellAccuracy(
				smellFileToMojoFilename, fileCoverageMap);
		
		// Log the results
		String allHighMatchedSmellsStr = gtSmellToPairMap.keySet().stream()
			.map(key -> key + " = (" + gtSmellToPairMap.get(key) + ")")
			.collect(Collectors.joining("\n"));
		logger.debug("");
		logger.debug("All high matched smells: ");
		logger.debug(allHighMatchedSmellsStr);
		for (Smell gtSmell : gtSmellToPairMap.keySet()) {
			Set<Entry<Smell,String>> pairs = gtSmellToPairMap.get(gtSmell);
			for (Entry<Smell,String> smellFilePair : pairs)
				logger.debug(gtSmell + " -> " + smellFilePair.getKey()
					+ " from " + smellFilePair.getValue());
		}
		logger.debug("");
		logger.debug("Showing matched and unmatched smells:");
		for (Smell gtSmell : detectedGtSmells) {
			if (gtSmellToPairMap.containsKey(gtSmell)) {
				Set<Entry<Smell, String>> smellFilePairs =
					gtSmellToPairMap.get(gtSmell);
				for (Entry<Smell, String> smellFilePair : smellFilePairs)
					logger.debug(gtSmell + " -> " + smellFilePair.getKey()
						+ " from " + smellFilePair.getValue());
			}
			else logger.debug(gtSmell + " has no match");
		}

		/**
		 * Key: A type of smell
		 * Value: The number of instances of that type of smell in the ground truth
		 */
		Map<Smell.SmellType, AtomicInteger> gtSmellTypeCountMap = new HashMap<>();
		for (Smell.SmellType smellType : Smell.SmellType.values()) {
			for (Smell gtSmell : detectedGtSmells) {
				if (gtSmell.getSmellType() == smellType) {
					if (gtSmellTypeCountMap.containsKey(smellType))
						gtSmellTypeCountMap.get(smellType).incrementAndGet();
					else gtSmellTypeCountMap.put(smellType, new AtomicInteger(1));
				}
			}
		}
		
		logger.debug("");
		logger.debug("Number of instances for each smell type in groud-truth smells:");
		logger.debug(gtSmellTypeCountMap.keySet().stream()
			.map(key -> key + " = (" + gtSmellTypeCountMap.get(key) + ")")
			.collect(Collectors.joining("\n")));
		
		/**
		 * Map containing the number of instances a technique matched a smell type.
		 * Key: [Type of smell, Technique Filename]
		 * Value: The number of instances a technique matched a smell type
		 */
		Map<Entry<Smell.SmellType, String>, AtomicInteger> matchedSmellTypeCountMap
			= new HashMap<>();
		logger.debug("");
		logger.debug("Populating the map counting how many times a smell type is matched by a technique:");
		// For each smell instance in the ground-truth
		for (Smell gtSmell : detectedGtSmells) {
			// If there were matches for that smell instance
			if (gtSmellToPairMap.containsKey(gtSmell)) {
				Set<Entry<Smell, String>> smellFilePairs =
					gtSmellToPairMap.get(gtSmell);
				// For each match
				for (Entry<Smell, String> smellFilePair : smellFilePairs) {
					Smell matchedSmell = smellFilePair.getKey();
					String resultsFilename = smellFilePair.getValue();
					// And for each smell type
					for (Smell.SmellType smellType : Smell.SmellType.values()) {
						// If the smell in that match is of the given type
						if (matchedSmell.getSmellType() == smellType) {
							logger.debug(resultsFilename + ":" + matchedSmell
									+ " is an instance of " + smellType);
							Entry<Smell.SmellType, String> pair = Map.entry(
								smellType, resultsFilename);
							//TODO not sure what's going on here
							if (matchedSmellTypeCountMap.containsKey(pair))
								matchedSmellTypeCountMap.get(pair).incrementAndGet();
							else
								matchedSmellTypeCountMap.put(pair, new AtomicInteger(1));
						}
					}
				}
			}
			else logger.debug(gtSmell + " has no match");
		}
		
		logger.debug("");
		logger.debug("Number of instances for each smell type in matched smells:");
		logger.debug(matchedSmellTypeCountMap.keySet().stream()
			.map(key -> key + " = (" + matchedSmellTypeCountMap.get(key) + ")")
			.collect(Collectors.joining("\n")));
		
		List<String> dsShortFilenames = dsFilenames.stream()
			.map(FileUtil::extractFilenamePrefix)
			.collect(Collectors.toList());

		String smellTechTable = "";
		smellTechTable += "," + String.join(",", dsShortFilenames);
		smellTechTable += "\n";

		for (Smell.SmellType smellType : Smell.SmellType.values()) {
			smellTechTable += smellType + ",";
			if (!gtSmellTypeCountMap.containsKey(smellType))
				for (int i = 0; i < dsFilenames.size(); i++)
					smellTechTable += "NA,";
			else {
				for (String filename : dsFilenames) {
					Entry<Smell.SmellType, String> pair = Map.entry(smellType, filename);
					if (matchedSmellTypeCountMap.containsKey(pair)) {
						double ratio = (double) matchedSmellTypeCountMap.get(pair).get()
							/ (double) gtSmellTypeCountMap.get(pair.getKey()).get();
						smellTypeTechRatioMap.put(pair, ratio);
						smellTechTable += ratio + ",";
					}
					else
						smellTechTable += "0,";
				}
			}
			smellTechTable += "\n";
		}
		logger.debug("\n" + smellTechTable);
		
		try (PrintWriter writer = new PrintWriter(smellTechTableFilename, "UTF-8")){
			writer.println(smellTechTable);
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		double numofMatchedGtSmells = (double) gtSmellToPairMap.keySet().size();
		double numOfTotalGtSmells = (double) detectedGtSmells.size();
		
		logger.debug("");
		logger.debug("number of matched gt smells: " + numofMatchedGtSmells);
		logger.debug("number of total gt smells: " + numOfTotalGtSmells);
		double detectedGtSmellsRatio = numofMatchedGtSmells / numOfTotalGtSmells;
		logger.debug("Ratio of detected gt smells: " + detectedGtSmellsRatio);
	}

	private static void computePearsonCorrelationOfMojoFMToSmellAccuracy(
			String smellFileToMojoFilename, Map<String, Double> fileCoverageMap) {
		/**
		 * Key: Smell filename
		 * Value: MojoFM score
		 */
		List<Entry<String, Double>> fileScorePairs = new ArrayList<>();
		Path smellFileToMojoPath = Paths.get(smellFileToMojoFilename);

		// Open an InputStream to read the MojoFM file
		try (InputStream in = Files.newInputStream(smellFileToMojoPath);
				BufferedReader reader = new BufferedReader(
				new InputStreamReader(in))) {
			for (String line = reader.readLine();
			    line != null;
					line = reader.readLine()) {
				String[] tokens = line.split(",");
				String filename = tokens[0];
				double mojoFmScore = Double.parseDouble(tokens[1]);
				fileScorePairs.add(Map.entry(filename, mojoFmScore));
			}
		} catch (IOException x) {
			x.printStackTrace();
		}

		// Sort the results read from the MojoFM file and log them
		Collections.sort(fileScorePairs, new FileScorePairAscending());
		logger.debug("");
		for (Entry<String, Double> pair : fileScorePairs)
			logger.debug(pair.getKey() + "," + pair.getValue());
	
		/**
		 * Set up the two distribution vectors for correlation. The first vector
		 * (coverageArray) represents the coverage distribution of all obtained
		 * technique results. The second vector (scoreArray) represents the
		 * distribution drawn from the MojoFM scores file.
		 */
		double[] coverageArray = new double[fileCoverageMap.entrySet().size()];
		double[] scoreArray = new double[fileScorePairs.size()];
		for (int i = 0; i < fileScorePairs.size(); i++) {
			Entry<String, Double> pair = fileScorePairs.get(i);
			String filename = pair.getKey();
			double score = pair.getValue();
			double coverage = fileCoverageMap.get(filename);
			scoreArray[i] = score;
			coverageArray[i] = coverage;
		}
		
		PearsonsCorrelation pearsons = new PearsonsCorrelation();
		double correlationCoefficient =
			pearsons.correlation(scoreArray, coverageArray);

		logger.debug("");
		logger.debug("MoJoFM Scores:");
		logger.debug(scoreArray);
		logger.debug("Coverage Ratios:");
		logger.debug(coverageArray);
		logger.debug("MoJoFM to Coverage correlation: " + correlationCoefficient);
	}

	/**
	 * Calculates the similarity between two smell instances as the ratio between
	 * the intersection of clusters involved over the union of clusters involved.
	 * 
	 * @param gtSmell Instance of smell from a ground-truth result.
	 * @param techSmell Instance of smell from an analysis technique result.
	 * @return The resultant ratio.
	 */
	private static double calcSimOfSmellInstances(
			Smell gtSmell, Smell techSmell) {
		Set<String> gtEntities = new HashSet<>();
		Set<String> techEntities = new HashSet<>();
		
		for (ConcernCluster cluster : gtSmell.getClusters())
			gtEntities.addAll(cluster.getEntities());
		for (ConcernCluster cluster : techSmell.getClusters())
			techEntities.addAll(cluster.getEntities());
		
		Set<String> intersection = new HashSet<>(gtEntities);
		intersection.retainAll(techEntities);
		
		Set<String> union = new HashSet<>(gtEntities);
		union.addAll(techEntities);

		return (double) intersection.size() / (double) union.size();
	}
}