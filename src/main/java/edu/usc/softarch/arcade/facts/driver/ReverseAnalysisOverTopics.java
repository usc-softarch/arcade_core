package edu.usc.softarch.arcade.facts.driver;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cc.mallet.util.Maths;

import edu.usc.softarch.arcade.clustering.Entity;

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


		for (String clusterName : clusterMap.keySet()) {
			// Mapping name of entity to Entity object.
			Map<String, Entity> entityToFeatures = new HashMap<>();
			for (List<String> depFact : depFacts) {
				Entity entity;
				String source = depFact.get(1);
				String target = depFact.get(2);

				if (clusterMap.get(clusterName).contains(source)) {
					// featureSet contains a list of all featureNames for that entity
					Set<String> featureSet;
					// If cluster already exists in map that is being built
					if (map.get(clusterName) != null)
						entityToFeatures = map.get(clusterName);
					if (entityToFeatures.get(source) != null) {
						featureSet = entityToFeatures.get(source).featureSet;
						entity = entityToFeatures.get(source);
					} else {
						entity = new Entity(source);
						featureSet = new HashSet<>();
					}
					// Adding target to set of features for that entity
					featureSet.add(target);
					entity.featureSet = featureSet;
					// If this target has never been encountered yet
					if (featureNameToBitsetIndex.get(target) == null)	{
						featureNameToBitsetIndex.put(target, Integer.valueOf(bitSetSize));
						// Setting this feature as 1 in the entity's feature vector
						entity.featureVector.set(bitSetSize);
						bitSetSize++;
					} else {
						// Setting that feature to true
						entity.featureVector.set(featureNameToBitsetIndex.get(target));
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
}
