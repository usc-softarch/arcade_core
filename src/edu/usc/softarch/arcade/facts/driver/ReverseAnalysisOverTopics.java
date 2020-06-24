package edu.usc.softarch.arcade.facts.driver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.google.common.base.Joiner;

import cc.mallet.util.Maths;

import edu.usc.softarch.arcade.clustering.ConcernClusteringRunner;
import edu.usc.softarch.arcade.clustering.FastCluster;
import edu.usc.softarch.arcade.clustering.Feature;
import edu.usc.softarch.arcade.clustering.FeatureVector;
import edu.usc.softarch.arcade.clustering.SimCalcUtil;
import edu.usc.softarch.arcade.clustering.util.ClusterUtil;
import edu.usc.softarch.arcade.config.Config;
import edu.usc.softarch.arcade.config.ConfigUtil;
import edu.usc.softarch.arcade.topics.DocTopicItem;
import edu.usc.softarch.arcade.topics.DocTopics;
import edu.usc.softarch.arcade.clustering.Entity;
import edu.usc.softarch.arcade.topics.TopicUtil;

public class ReverseAnalysisOverTopics {
	static Map<String, Integer> featureNameToBitsetIndex = new HashMap<String, Integer>();
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
		Map<String, Map<String, Entity>> map = new HashMap<String, Map<String, Entity>>();

		for (String clusterName : clusterMap.keySet()) { // for each cluster
															// name
			Map<String, Entity> entityToFeatures = new HashMap<String, Entity>(); // using
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
						featureSet = new HashSet<String>();
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
						featureNameToBitsetIndex.put(target, new Integer(
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

	/** method to compute similarity between a pair of entities */
	private double computePairWiseSimilarity(SimilarityMeasure sm,
			Entity entity1, Entity entity2) {
		if (sm == SimilarityMeasure.LIMBO) {
			Set<Integer> c1Indices = entity1.nonZeroFeatureMap.keySet();
			entity1.setNonZeroFeatureMapForLibmoUsingIndices(entity1, entity2,
					c1Indices);

			Set<Integer> c2Indices = entity2.nonZeroFeatureMap.keySet();
			entity2.setNonZeroFeatureMapForLibmoUsingIndices(entity1, entity2,
					c2Indices);
			return (this.getInfoLossMeasure(2, entity1, entity2));
		}
		// System.out.println("Entities : " + entity1.name + " " +
		// entity2.name);
		BitSet fv1 = entity1.featureVector;
		BitSet fv2 = entity2.featureVector;
		int count10 = 0;
		int count01 = 0;
		int count00 = 0;
		int count11 = 0;
		int sum11 = 0;
		for (int i = 0; i < fv1.size(); i++) {
			if (fv1.get(i) && !fv2.get(i)) {
				count10++;
			} else if (!fv1.get(i) && fv2.get(i)) {
				count01++;
			} else if (!fv1.get(i) && !fv2.get(i)) {
				count00++;
			} else {
				count11++;
				sum11 = sum11 + 1 + 1;
			}
		}
		// Unbiased Ellenberg for now
		if (sm == SimilarityMeasure.UELLENBERG) {
			double denom = 0.5 * sum11 + count10 + count01;
			if (denom == 0) {
				return denom;
			}
			return (double) 0.5 * sum11 / (denom);
		} else if (sm == SimilarityMeasure.JS) {
			double denom = count11 + count10 + count01;
			if (denom == 0) {
				return denom;
			}
			return (double) count11 / (denom);
		}
		return 0;
	}

	public static void initializeFileIO(Map<Integer, String> docTopicFilesMap) {
		try {
			out = new BufferedWriter(new FileWriter("outfile.csv"));

		} catch (IOException e) {
			// TODO Auto-generated catch block

			e.printStackTrace();
		}

	}

	private static void writeToFile(Double content) {
		// BufferedWriter out = new BufferedWriter( new
		// FileWriter("outfile.csv"));

		String str = "";

		//System.out.println(content);

		str += content + ",";

		try {
			out.write(str);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/*
		 * try { out.close(); } catch (IOException e) { // TODO Auto-generated
		 * catch block e.printStackTrace(); }
		 */

	}

	/**
	 * method calculates the average sim measure for a cluster
	 * 
	 * @throws IOException
	 **/
	private double computeClusterSimilarity(SimilarityMeasure sm,
			String clusterName,
			Map<String, Map<String, Entity>> clusterNameToEntities)
			throws IOException {
		Map<String, Entity> nameToEntity = clusterNameToEntities
				.get(clusterName);
		Object[] entities = nameToEntity.values().toArray();
		double sum = 0;
		int n = 0; // number of simMeasure values
		for (int i = 0; i < entities.length; i++) {
			for (int j = i + 1; j < entities.length; j++) {
				double simMeasure = computePairWiseSimilarity(sm,
						(Entity) entities[i], (Entity) entities[j]);
				// System.out.println("simMeasure: " + simMeasure);
				// writeToFile(simMeasure);
				sum = sum + simMeasure;
				n++;
			}
		}
		System.out.println("Sum and n are " + sum + " " + n);
		double average = (sum / n);
		return average;
	}

	public static double getInfoLossMeasure(int numberOfEntitiesToBeClustered,
			Entity entity1, Entity entity2) {

		// int featuresLength = cluster.getFeaturesLength();
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
				// System.out.println("firstDist[i] is" + firstDist[i]);
			} else { // this feature is zero
				firstDist[i] = 0;
			}

			/*
			 * if (otherCluster.getNonZeroFeatureMap().get(i) != null) { double
			 * featureValue = otherCluster.getNonZeroFeatureMap().get(i);
			 * secondDist[i] =
			 * featureValue/otherCluster.getNonZeroFeatureMap().size(); } else {
			 * // this feature is zero secondDist[i] = 0; }
			 */
		}
	}

	/**
	 * method that calculates the result for every cluster and displays it
	 * 
	 * @throws IOException
	 */
	private static void calculateResults(
			Map<String, Map<String, Entity>> clusterNameToEntities,
			Map<Integer, String> docTopicFilesMap) throws IOException {
		// int counter = 0;
		Set<String> orderedClusterNames = new TreeSet<String>(
				clusterNameToEntities.keySet());
		for (String clusterName : orderedClusterNames) {
			// counter++;
			// out.write(clusterName + ",");
			Map<String, Entity> nameToEntity = clusterNameToEntities
					.get(clusterName);
			//System.out.println("CLUSTER NAME: " + clusterName);

			double clusterSimUsingJSDivergence = computeJSDivergence(
					clusterName, clusterNameToEntities);
			/*System.out.println("Similarity measure for cluster " + clusterName
					+ " using JSDivergence is " + clusterSimUsingJSDivergence);*/
			writeToFile(clusterSimUsingJSDivergence);
			// if(counter == 24)
			// {
			// break; //REMOVE THIS if you want to compute for all clusters
			// }
		}
	}

	public static void main(String[] args) {
		System.out.println("IN MAIN");
		// PropertyConfigurator.configure(Config.getLoggingConfigFilename());

		// String depsFilename = args[0];
		// String clustersFilename = args[1];

		RsfReader.loadRsfDataFromFile("archstudio4_deps (1).rsf");
		List<List<String>> depFacts = RsfReader.unfilteredFacts;

		RsfReader
				.loadRsfDataFromFile("archstudio4_clean_ground_truth_recovery.rsf");
		List<List<String>> clusterFacts = RsfReader.unfilteredFacts;
		System.out.println("Finished loading data from both files");

		Map<Integer, String> docTopicFilesMap = buildDocTopicFilesMap();

		initializeFileIO(docTopicFilesMap);

		Map<String, Set<String>> clusterMap = ClusterUtil
				.buildClusterMap(clusterFacts);
		Map<String, Map<String, Entity>> clusterNameToEntities = buildFeatureSetPerClusterEntity(
				clusterMap, depFacts);

		Set<Integer> numTopicsSet = new TreeSet<Integer>(
				docTopicFilesMap.keySet());

		Set<String> orderedClusterNames = new TreeSet<String>(
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
					Object[] entities = nameToEntity.values().toArray();
					//System.out.println("INCLUSTER NAME: " + clusterName);
					for (String entityName : nameToEntity.keySet()) {
						Entity entity = nameToEntity.get(entityName);
						entity.docTopicItem = null;
					}
				}
				
				initializeDocTopicsUsingFile(clusterNameToEntities,
						docTopicsFilename, "java");
				// ra.printClusterNameToEntities(clusterNameToEntities);

				out.write("JSDivergence" + numTopics + ",");

				calculateResults(clusterNameToEntities, docTopicFilesMap);
				out.newLine();
				
				

			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static Map<Integer, String> buildDocTopicFilesMap() {
		Map<Integer, String> docTopicFilesMap = new HashMap<Integer, String>();
		try {
			BufferedReader br = new BufferedReader(
					new FileReader(
							"/home/joshua/Documents/Software Engineering Research/subject_systems/archstudio4/doc-topics-filelist.txt"));
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
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return docTopicFilesMap;
	}

	private void printClusterNameToEntities(
			Map<String, Map<String, Entity>> clusterNameToEntities) {
		for (String clusterName : clusterNameToEntities.keySet()) {
			Map<String, Entity> nameToEntity = clusterNameToEntities
					.get(clusterName);

			System.out.println("CLUSTER NAME: " + clusterName);
			for (String entityName : nameToEntity.keySet()) {
				System.out.println("---Entity name--- : " + entityName);
				Entity entity = nameToEntity.get(entityName);
				System.out.println("Entity's featureSet: ");
				Set<String> featureSet = entity.featureSet;
				/*
				 * for(String featureName: featureSet) {
				 * System.out.println(featureName); }
				 */
				/*
				 * System.out.print("Feature vector bitset: "); for(int i = 0; i
				 * < this.bitSetSize; i++) {
				 * System.out.print(entity.featureVector.get(i)); }
				 */
			}
		}
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
			Object[] entities = nameToEntity.values().toArray();
			// System.out.println("INCLUSTER NAME: " + clusterName);
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

	// ----------------------DOCTOPICITEM
	// STUFF-------------------------------------------//
	/** method to load doc-topic-item for each entity */
	private void initializeDocTopicsForEachEntity(
			Map<String, Map<String, Entity>> clusterNameToEntities, String type) {
		// Reference
		// ConcernClusteringRunner.initializeDocTopicsForEachFastCluster(),
		// pretty much the same
		// thing except instead of using FastClusters, this uses Entity data
		// structure
		for (String clusterName : clusterNameToEntities.keySet()) {
			Map<String, Entity> nameToEntity = clusterNameToEntities
					.get(clusterName);
			Object[] entities = nameToEntity.values().toArray();
			System.out.println("INCLUSTER NAME: " + clusterName);
			for (String entityName : nameToEntity.keySet()) {
				Entity entity = nameToEntity.get(entityName);
				if (TopicUtil.docTopics == null)
					TopicUtil.docTopics = TopicUtil
							.getDocTopicsFromHardcodedDocTopicsFile();

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
					// System.out.println("Entities are: " + entity1.name +
					// entity2.name);
					// System.out.println("simMeasure: " + simMeasure);
					// writeToFile(simMeasure);
					sum = sum + simMeasure;
					n++;
				}
			}
		}
		// System.out.println("Sum and n for JSDivergence are " + sum + " " +
		// n);
		double average = (sum / n);
		return average;

	}

}
