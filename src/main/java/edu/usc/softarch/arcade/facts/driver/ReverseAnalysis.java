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

public class ReverseAnalysis {	
	static Map<String,Integer> featureNameToBitsetIndex = new HashMap<>();
	static int bitSetSize = 0;
	BufferedWriter out;
	enum SimilarityMeasure {UELLENBERG, JS, LIMBO, BUNCH, UNM, PKG};
	SimilarityMeasure sm;
	enum LangType {JAVA,C};
	static LangType selectedLangType;
	
	/** 
	 * Produces a feature vector bitset for each entity in each cluster
	 */
	public Map<String,Map<String,Entity>> buildFeatureSetPerClusterEntity(
			Map<String,Set<String>> clusterMap, List<List<String>> depFacts) {
		Map<String,Map<String,Entity>> map = new HashMap<>();
			
		for (String clusterName : clusterMap.keySet()) {
			Map<String, Entity> entityToFeatures = new HashMap<>();
			for (List<String> depFact : depFacts) {
				Entity entity;
				String source = depFact.get(1);
				String target = depFact.get(2);
		
				if (clusterMap.get(clusterName).contains(source)) {
					// for each cluster name
					Set<String> featureSet;
				
					if(map.get(clusterName) != null)
						entityToFeatures = map.get(clusterName);
					if(entityToFeatures.get(source) != null) {
						featureSet = entityToFeatures.get(source).featureSet;
						entity = entityToFeatures.get(source);
					}	else {
						entity = new Entity(source);
						featureSet = new HashSet<>();
					}
					featureSet.add(target); //adding target to set of features for that entity
					entity.featureSet = featureSet;
					//if this target has never been encountered yet
					if(featureNameToBitsetIndex.get(target) == null) {
						featureNameToBitsetIndex.put(target, Integer.valueOf(bitSetSize));
						entity.featureVector.set(bitSetSize); //setting the spot for this feature as 1 in the entitie's feature vector
						bitSetSize++;
					}	else
						entity.featureVector.set(featureNameToBitsetIndex.get(target)); //setting that feature to true
					entity.initializeNonZeroFeatureMap(bitSetSize);
					entityToFeatures.put(source,entity);
				}
			}
			map.put(clusterName, entityToFeatures);
		}
		return map;
	}
	
	/*-----------------------------LIMBO STUFF--------------------------------------------*/
	/**copied pasted*/
	public static double getInfoLossMeasure(int numberOfEntitiesToBeClustered,
			Entity entity1,	Entity entity2) {
		double[] firstDist = new double[bitSetSize];
		double[] secondDist = new double[bitSetSize];	
		entity1.normalizeFeatureVectorOfCluster(bitSetSize, firstDist);
		entity2.normalizeFeatureVectorOfCluster(bitSetSize, secondDist);
		
		double jsDivergence = Maths.jensenShannonDivergence(firstDist, secondDist);
		System.out.println("JsDivergence is " + jsDivergence);
		if (Double.isInfinite(jsDivergence))
			jsDivergence = Double.MAX_VALUE;
		System.out.println("numentities of entity1 " + entity1.getNumEntities());
		double infoLossMeasure = ((double) entity1.getNumEntities() / 
			numberOfEntitiesToBeClustered + (double)entity2.getNumEntities() /
			numberOfEntitiesToBeClustered) * jsDivergence;
		System.out.println("InfoLossMeasure is " + infoLossMeasure);
		if (Double.isNaN(infoLossMeasure))
			throw new RuntimeException("infoLossMeasure is NaN");
		return infoLossMeasure;
	}

/*------------------------ UTILITY FUNCTIONS------------------------------------------------- */	
	public void initializeFileIO(String outFilename) {
		try {
			out = new BufferedWriter( new FileWriter(outFilename));
			out.write("ClusterName" + ",");
			out.write("Unbiased Ellenberg" + ",");
			out.write("UnbiasedEllenberg-NM" + ",");
			out.write("LIMBO" + ",");
			out.write("Bunch" + ",");
			out.write("JSDivergence" + ",");
			out.write("Dom" + ",");
			out.write("PKG" + ",");
			out.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
}