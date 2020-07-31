package edu.usc.softarch.arcade.facts.driver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cc.mallet.util.Maths;

import edu.usc.softarch.arcade.clustering.SimCalcUtil;
import edu.usc.softarch.arcade.clustering.util.ClusterUtil;
import edu.usc.softarch.arcade.clustering.Entity;
import edu.usc.softarch.arcade.topics.TopicUtil;

public class ReverseAnalysisOverTopics {
	static Map<String, Integer> featureNameToBitsetIndex = new HashMap<>();
	static int bitSetSize = 0;
	static BufferedWriter out;

	enum SimilarityMeasure {
		UELLENBERG, JS, LIMBO
	};

	SimilarityMeasure sm;

	/**
	 * method that produces a feature vector bitset for each entity in each
	 * cluster
	 **/
	public static Map<String, Map<String, Entity>> buildFeatureSetPerClusterEntity(
			Map<String, Set<String>> clusterMap, List<List<String>> depFacts) {
		Map<String, Map<String, Entity>> map = new HashMap<>();

		for (String clusterName : clusterMap.keySet()) { // for each cluster
															// name
			Map<String, Entity> entityToFeatures = new HashMap<>(); // using
																					// a
																					// map<String,Entity>
																					// instead
																					// of
																					// a
																					// list
																					// of
																					// entities
																					// so
																					// that
																					// getting
																					// the
																					// feature
			// vector for an Entity name will be faster. Mapping name of entity
			// to Entity object.
			for (List<String> depFact : depFacts) {
				Entity entity;
				String source = depFact.get(1);
				String target = depFact.get(2);

				if (clusterMap.get(clusterName).contains(source)) // if cluster
																	// contains
																	// entity
				{
					Set<String> featureSet; // featureSet contains a list of all
											// featureNames for that entity

					if (map.get(clusterName) != null) // if cluster already
														// exists in map that is
														// being built
					{
						entityToFeatures = map.get(clusterName);
					}
					if (entityToFeatures.get(source) != null) {
						featureSet = entityToFeatures.get(source).featureSet;
						entity = entityToFeatures.get(source);
					} else // otherwise create new ones
					{
						entity = new Entity(source);
						featureSet = new HashSet<>();
					}
					featureSet.add(target); // adding target to set of features
											// for that entity
					entity.featureSet = featureSet;
					if (featureNameToBitsetIndex.get(target) == null) // if this
																		// target
																		// has
																		// never
																		// been
																		// encountered
																		// yet
					{
						featureNameToBitsetIndex.put(target, Integer.valueOf(
								bitSetSize));
						entity.featureVector.set(bitSetSize); // setting the
																// spot for this
																// feature as 1
																// in the
																// entitie's
																// feature
																// vector
						bitSetSize++;
					} else {
						entity.featureVector.set(featureNameToBitsetIndex
								.get(target)); // setting that feature to true
					}
					entity.initializeNonZeroFeatureMap(bitSetSize);
					entityToFeatures.put(source, entity);
				}
			}

			map.put(clusterName, entityToFeatures);
		}

		return map;
	}

	public static void initializeFileIO() {
		try {
			out = new BufferedWriter(new FileWriter("outfile.csv"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void writeToFile(Double content) {
		String str = "";
		str += content + ",";

		try {
			out.write(str);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static double getInfoLossMeasure(int numberOfEntitiesToBeClustered,
			Entity entity1, Entity entity2) {
		double[] firstDist = new double[bitSetSize];
		double[] secondDist = new double[bitSetSize];

		normalizeFeatureVectorOfCluster(entity1, bitSetSize, firstDist);
		normalizeFeatureVectorOfCluster(entity2, bitSetSize, secondDist);

		double jsDivergence = Maths.jensenShannonDivergence(firstDist,
				secondDist);
		System.out.println("JsDivergence is " + jsDivergence);
		if (Double.isInfinite(jsDivergence)) {
			jsDivergence = Double.MAX_VALUE;
		}
		System.out
				.println("numentities of entity1 " + entity1.getNumEntities());
		double infoLossMeasure = ((double) entity1.getNumEntities()
				/ numberOfEntitiesToBeClustered + (double) entity2
				.getNumEntities() / numberOfEntitiesToBeClustered)
				* jsDivergence;
		System.out.println("InfoLossMeasure is " + infoLossMeasure);
		if (Double.isNaN(infoLossMeasure)) {
			throw new RuntimeException("infoLossMeasure is NaN");
		}

		return infoLossMeasure;
	}

	private static void normalizeFeatureVectorOfCluster(Entity entity,
			int featuresLength, double[] firstDist) {
		for (int i = 0; i < featuresLength; i++) {
			if (entity.nonZeroFeatureMap.get(i) != null) {
				double featureValue = entity.nonZeroFeatureMap.get(i);
				firstDist[i] = featureValue / entity.nonZeroFeatureMap.size();
			} else { // this feature is zero
				firstDist[i] = 0;
			}
		}
	}

	/**
	 * method that calculates the result for every cluster and displays it
	 * 
	 * @throws IOException
	 */
	private static void calculateResults(
			Map<String, Map<String, Entity>> clusterNameToEntities) {
		Set<String> orderedClusterNames = new TreeSet<>(
				clusterNameToEntities.keySet());
		for (String clusterName : orderedClusterNames) {
			double clusterSimUsingJSDivergence = computeJSDivergence(
					clusterName, clusterNameToEntities);
			writeToFile(clusterSimUsingJSDivergence);
		}
	}

	public static void main(String[] args) {
		System.out.println("IN MAIN");

		RsfReader.loadRsfDataFromFile("archstudio4_deps (1).rsf");
		List<List<String>> depFacts = RsfReader.unfilteredFacts;

		RsfReader
				.loadRsfDataFromFile("archstudio4_clean_ground_truth_recovery.rsf");
		List<List<String>> clusterFacts = RsfReader.unfilteredFacts;
		System.out.println("Finished loading data from both files");

		Map<Integer, String> docTopicFilesMap = buildDocTopicFilesMap();

		initializeFileIO();

		Map<String, Set<String>> clusterMap = ClusterUtil
				.buildClusterMap(clusterFacts);
		Map<String, Map<String, Entity>> clusterNameToEntities = buildFeatureSetPerClusterEntity(
				clusterMap, depFacts);

		Set<Integer> numTopicsSet = new TreeSet<>(
				docTopicFilesMap.keySet());

		Set<String> orderedClusterNames = new TreeSet<>(
				clusterNameToEntities.keySet());

		try {
			out.write(",");
			for (String clusterName : orderedClusterNames) {
				out.write(clusterName + ",");
			}
			out.newLine();

			for (int numTopics : numTopicsSet) {
				String docTopicsFilename = docTopicFilesMap.get(numTopics);
				
				// Reset topic model data
				TopicUtil.docTopics = null;
				for (String clusterName : clusterNameToEntities.keySet()) {
					Map<String, Entity> nameToEntity = clusterNameToEntities
							.get(clusterName);
					for (String entityName : nameToEntity.keySet()) {
						Entity entity = nameToEntity.get(entityName);
						entity.docTopicItem = null;
					}
				}
				
				initializeDocTopicsUsingFile(clusterNameToEntities,
						docTopicsFilename, "java");

				out.write("JSDivergence" + numTopics + ",");

				calculateResults(clusterNameToEntities);
				out.newLine();
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static Map<Integer, String> buildDocTopicFilesMap() {
		Map<Integer, String> docTopicFilesMap = new HashMap<>();
		try (BufferedReader br = new BufferedReader(
			new FileReader(
					"/home/joshua/Documents/Software Engineering Research/subject_systems/archstudio4/doc-topics-filelist.txt"))) {
			
			String line = null;
			while ((line = br.readLine()) != null) {
				System.out.println(line);

				File file = new File(line);
				Pattern pattern = Pattern.compile("-(\\d+)-");
				Matcher matcher = pattern.matcher(file.getName());
				while (matcher.find()) {
					String numOfTopics = matcher.group(1);
					System.out.println(numOfTopics);
					docTopicFilesMap.put(Integer.parseInt(numOfTopics), line);
				}
				System.out.println();
			}

		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return docTopicFilesMap;
	}

	// ----------------------DOCTOPICITEM
	// STUFF-------------------------------------------//
	/** method to load doc-topic-item for each entity */
	private static void initializeDocTopicsUsingFile(
			Map<String, Map<String, Entity>> clusterNameToEntities,
			String filename, String type) {
		// Reference
		// ConcernClusteringRunner.initializeDocTopicsForEachFastCluster(),
		// pretty much the same
		// thing except instead of using FastClusters, this uses Entity data
		// structure
		for (String clusterName : clusterNameToEntities.keySet()) {
			Map<String, Entity> nameToEntity = clusterNameToEntities
					.get(clusterName);
			for (String entityName : nameToEntity.keySet()) {
				Entity entity = nameToEntity.get(entityName);
				if (TopicUtil.docTopics == null)
					TopicUtil.docTopics = TopicUtil
							.getDocTopicsFromFile(filename);

				if (entity.docTopicItem == null)
					TopicUtil.setDocTopicForEntity(TopicUtil.docTopics, entity, type);
			}
		}

	}

	/**
	 * Computes average JS Divergence of each cluster
	 * 
	 * @param clusterName
	 * @param clusterNameToEntities
	 * @return
	 */
	private static double computeJSDivergence(String clusterName,
			Map<String, Map<String, Entity>> clusterNameToEntities) {
		Map<String, Entity> nameToEntity = clusterNameToEntities
				.get(clusterName);
		Object[] entities = nameToEntity.values().toArray();
		double sum = 0;
		int n = 0; // number of simMeasure values
		for (int i = 0; i < entities.length; i++) {
			for (int j = i + 1; j < entities.length; j++) {
				Entity entity1 = (Entity) entities[i];
				Entity entity2 = (Entity) entities[j];
				if ((entity1.docTopicItem != null)
						&& (entity2.docTopicItem != null)) // this makes sure
															// anonymous inner
															// classes don't get
				// included in the computation
				{
					double simMeasure = SimCalcUtil.getJSDivergence(entity1,
							entity2);
					sum = sum + simMeasure;
					n++;
				}
			}
		}
		// average
		return (sum / n);
	}
}
