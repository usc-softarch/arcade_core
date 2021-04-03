package edu.usc.softarch.arcade.facts.driver;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;

import cc.mallet.util.Maths;
import edu.usc.softarch.arcade.clustering.ClusterUtil;
import edu.usc.softarch.arcade.clustering.Entity;
import edu.usc.softarch.arcade.topics.DistributionSizeMismatchException;
import edu.usc.softarch.arcade.topics.TopicUtil;

public class ReverseAnalysis {	
	static Map<String,Integer> featureNameToBitsetIndex = new HashMap<>();
	static int bitSetSize = 0;
	BufferedWriter out;
	enum SimilarityMeasure {UELLENBERG, JS, LIMBO, BUNCH, UNM, PKG};
	SimilarityMeasure sm;
	enum LangType {JAVA,C};
	static LangType selectedLangType;
	
	/**
	 * Calculates the result for every cluster and displays it.
	 * 
	 * @throws IOException
	 */
	private void calculateResults(
			Map<String, Map<String, Entity>> clusterNameToEntities,
			Map<String, Set<MutablePair<String, String>>> internalEdgeMap,
			Map<String, Set<MutablePair<String, String>>> externalEdgeMap,
			Map<String,Integer> pkgSizeMap)
			throws IOException
	{
		Map<String,Set<String>> clusterNameToEntitiesNames = new HashMap<>();
		for (String clusterName : clusterNameToEntities.keySet()) {

			Map<String, Entity> nameToEntity = clusterNameToEntities
					.get(clusterName);
			Object[] entities = nameToEntity.values().toArray();

			Set<String> entityNames = new HashSet<>();
			for (Object obj : entities) {
				Entity entity = (Entity) obj;
				entityNames.add(entity.name);
			}
			clusterNameToEntitiesNames.put(clusterName,entityNames);
			
		}
		
		Map<String,Double> domValMap = DominatorGroundTruthAnalyzer.computeDominatorCriteriaIndicatorValues(clusterNameToEntitiesNames, internalEdgeMap);
		
		for(String clusterName: clusterNameToEntities.keySet())
		{
			out.write(clusterName + ",");
			
			System.out.println("CLUSTER NAME: " + clusterName);
			
			double clusterSimUsingUE = computeClusterSimilarity(SimilarityMeasure.UELLENBERG,clusterName, clusterNameToEntities, internalEdgeMap, externalEdgeMap);
			System.out.println("Similarity measure for cluster " + clusterName + " using UE is " + clusterSimUsingUE);
			writeToFile(clusterSimUsingUE);
			
			double clusterSimUsingUNM = computeClusterSimilarity(SimilarityMeasure.UNM,clusterName, clusterNameToEntities, internalEdgeMap, externalEdgeMap);
			System.out.println("Similarity measure for cluster " + clusterName + " using UNM is " + clusterSimUsingUNM);
			writeToFile(clusterSimUsingUNM);
			
			double clusterSimUsingLimbo = computeClusterSimilarity(SimilarityMeasure.LIMBO,clusterName, clusterNameToEntities, internalEdgeMap, externalEdgeMap);
			writeToFile(clusterSimUsingLimbo);
			System.out.println("Similarity measure for cluster " + clusterName + " using LIMBO is " + clusterSimUsingLimbo);
			
			double clusterSimUsingBunch = computeClusterSimilarity(SimilarityMeasure.BUNCH,clusterName, clusterNameToEntities, internalEdgeMap, externalEdgeMap);
			writeToFile(clusterSimUsingBunch);
			System.out.println("Similarity measure for cluster " + clusterName + " using BUNCH is " + clusterSimUsingBunch);
			
			double clusterSimUsingJSDivergence = computeJSDivergence(clusterName, clusterNameToEntities);
			System.out.println("Similarity measure for cluster " + clusterName + " using JSDivergence is " + clusterSimUsingJSDivergence);
			writeToFile(clusterSimUsingJSDivergence); // - UNCOMMENT THIS OUT FOR JSDIVERGENCE
			
			double clusterSimUsingDom = domValMap.get(clusterName);
			System.out.println("Similarity measure for cluster " + clusterName + " using Subgraph Dominator Pattern is " + clusterSimUsingJSDivergence);
			writeToFile(clusterSimUsingDom); // - UNCOMMENT THIS OUT FOR JSDIVERGENCE
			
			double clusterSimUsingPkg = computePkgClusterSim(clusterName, clusterNameToEntities, pkgSizeMap);
			writeToFile(clusterSimUsingPkg);
			System.out.println("Similarity measure for cluster " + clusterName + " using PKG is " + clusterSimUsingPkg);
			
			out.newLine();
		}
		out.close();
	}
	
	private static Map<String, Integer> computePkgSizes(List<List<String>> pkgFacts) {
		Map<String,Integer> pkgSizeMap = new HashMap<>();
		for (List<String> fact : pkgFacts) {
			String pkgName = fact.get(1);

			if (pkgSizeMap.containsKey(pkgName)) {
				pkgSizeMap.put(pkgName, pkgSizeMap.get(pkgName) + 1);
			} else {
				pkgSizeMap.put(pkgName, 1);
			}
		}
		
		return pkgSizeMap;
	}

	private double computePkgClusterSim(String clusterName,Map<String,Map<String,Entity>> clusterNameToEntities,Map<String,Integer> pkgSizeMap) {
		Map<String, Entity> nameToEntity = clusterNameToEntities.get(clusterName);
		Object[] entities = nameToEntity.values().toArray();
		
		if (entities.length == 0) {
			System.out.println(clusterName + " has no entities, so skipping");
			return 0;
		}

		List<String> entityNames = new ArrayList<>();
		for (Object obj : entities) {
			Entity entity = (Entity) obj;
			entityNames.add(entity.name);
		}
		
		String delimiter = "";
		String regexDelimiter = "";
		if (selectedLangType.equals(LangType.C)) {
			delimiter = "/";
			regexDelimiter = delimiter;
		}
		else if (selectedLangType.equals(LangType.JAVA)) {
			delimiter = ".";
			regexDelimiter = "\\.";
		}
		else
			throw new RuntimeException("Invalid language selected");

		Map<String, Integer> pkgCountMap = new HashMap<>();
		for (Object obj : entities) {
			Entity entity = (Entity) obj;
			
				String[] tokens = entity.name.split(regexDelimiter);
				String directoryName = "";
				List<String> directoryNameParts = new ArrayList<>();
				for (int i = 0; i < tokens.length - 1; i++)
					directoryNameParts.add(tokens[i]);

				directoryName = StringUtils.join(directoryNameParts,delimiter);
				
				if (pkgCountMap.containsKey(directoryName))
					pkgCountMap.put(directoryName, pkgCountMap.get(directoryName) + 1);
				else
					pkgCountMap.put(directoryName, 1);
		}

		int maxCount = 0;
		String maxPkgName = "";
		boolean maxUpdated = false;
		for (Entry<String, Integer> entry : pkgCountMap.entrySet()) {
			int pkgCount = entry.getValue();
			String pkgName = entry.getKey();
			if (pkgCount > maxCount) {
				maxCount = pkgCount;
				maxPkgName = pkgName;
				maxUpdated = true;
			}
		}

		assert maxUpdated;
		if (maxPkgName.endsWith(delimiter))
			maxPkgName = maxPkgName.substring(0, maxPkgName.length()-1);
		
		int pkgSize = 0;
		if (maxPkgName.equals(""))
			pkgSize = pkgSizeMap.get("default.ss");
		else
			pkgSize = pkgSizeMap.get(maxPkgName);
		assert pkgSize != 0;

		//samePkgToClusterSizeRatio
		return(double) maxCount	/ (double) entities.length;
	}
	
	/**
	 * Calculates the average sim measure for a cluster.
	 */
	private double computeClusterSimilarity(SimilarityMeasure sm,
			String clusterName, Map<String,Map<String,Entity>> clusterNameToEntities,
			Map<String,Set<MutablePair<String,String>>> internalEdgeMap,
			Map<String,Set<MutablePair<String,String>>> externalEdgeMap) {
		if (sm == SimilarityMeasure.BUNCH) {
			Set<MutablePair<String,String>> intEdges = internalEdgeMap.get(clusterName);
			Set<MutablePair<String,String>> extEdges = externalEdgeMap.get(clusterName);
			double countInternalEdges = intEdges.size();
			double countExternalEdges = extEdges.size();
			return (2*countInternalEdges)/((2*countInternalEdges) + countExternalEdges);
		} else {
			Map<String, Entity> nameToEntity = clusterNameToEntities.get(clusterName);
			Object[] entities = nameToEntity.values().toArray();
			double sum = 0;
			int n = 0; // number of simMeasure values

			// double-for loop to get two entities to compute pairwise similarity on
			for (int i = 0; i < entities.length; i++) {
				for (int j = i + 1; j < entities.length; j++) {
					double simMeasure = computePairWiseSimilarity(sm,
						(Entity) entities[i], (Entity) entities[j]);
					sum = sum + simMeasure;
					n++;
				}
			}
			System.out.println("Sum and n are " + sum + " " + n);
			//return average
			return n == 0 ? 0 : (sum / n);
		}
	}
	/**method to compute similarity between a pair of entities */
	private double computePairWiseSimilarity(SimilarityMeasure sm,
			Entity entity1, Entity entity2) {
		if(sm == SimilarityMeasure.LIMBO) {
			Set<Integer> c1Indices = entity1.nonZeroFeatureMap.keySet();
			entity1.setNonZeroFeatureMapForLibmoUsingIndices(entity1, entity2, c1Indices);
			
			Set<Integer> c2Indices = entity2.nonZeroFeatureMap.keySet();
			entity2.setNonZeroFeatureMapForLibmoUsingIndices(entity1, entity2, c2Indices);
			return getInfoLossMeasure(2, entity1, entity2);
		}
		
		BitSet fv1 = entity1.featureVector;
		BitSet fv2 = entity2.featureVector;
		int count10 = 0;
		int count01 = 0;
		int count00 = 0;
		int count11 = 0;
		int sum11 = 0;
		for (int i = 0; i < fv1.size(); i++) {
			if (fv1.get(i) && !fv2.get(i)) 
				count10++;
			else if (!fv1.get(i) && fv2.get(i))
				count01++;
			else if (!fv1.get(i) && !fv2.get(i))
				count00++;
			else {
				count11++;
				sum11 = sum11 + 1 + 1;
			}
		}
		if (sm == SimilarityMeasure.UELLENBERG) {
			double denom = 0.5*sum11 + count10 + count01;
			if (denom == 0)
				return denom;
			return 0.5 * sum11 / (denom);
		} else if (sm == SimilarityMeasure.JS) {
			double denom = (double)count11 + count10 + count01;
			if (denom == 0)
				return denom;
			return (double)count11 / (denom);
		}	else if (sm == SimilarityMeasure.UNM)
			return 0.5 * sum11 / (0.5 * sum11 + 2 *
				((double)count10 + count01) + (double)count00 + count11);
		return 0;
	}
	
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
	
	//----------------------DOCTOPICITEM STUFF----------------------------------------------------//
	private void initDocTopics(Map<String, Map<String, Entity>> clusterNameToEntities,
			String docTopicsFilename, String type) {
		for(String clusterName: clusterNameToEntities.keySet())	{
			Map<String, Entity> nameToEntity = clusterNameToEntities.get(clusterName);
			System.out.println("INCLUSTER NAME: " + clusterName);
			for(String entityName: nameToEntity.keySet())	{
				Entity entity = nameToEntity.get(entityName);
				if (TopicUtil.docTopics == null)
					TopicUtil.docTopics = TopicUtil.getDocTopicsFromFile(docTopicsFilename);
				if (entity.docTopicItem == null) {
					try {
						TopicUtil.setDocTopicForEntity(TopicUtil.docTopics, entity, type);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	/**
	 * Computes average JS Divergence of each cluster
	 * @param clusterName
	 * @param clusterNameToEntities
	 * @return
	 */
	private double computeJSDivergence(String clusterName,
			Map<String,Map<String,Entity>> clusterNameToEntities) {
		Map<String, Entity> nameToEntity = clusterNameToEntities.get(clusterName);
		Object[] entities = nameToEntity.values().toArray();
		double sum = 0;
		int n = 0; //number of simMeasure values
		for(int i = 0; i < entities.length; i++) {
			for(int j = i+1; j < entities.length; j++) {
				Entity entity1 = (Entity) entities[i];
				Entity entity2 = (Entity) entities[j];
				//this makes sure anonymous inner classes don't get included
				if ((entity1.docTopicItem != null) && (entity2.docTopicItem != null))	{
					double simMeasure = 0;
					try {
						simMeasure =
							entity1.docTopicItem.getJsDivergence(entity2.docTopicItem);
					} catch (DistributionSizeMismatchException e) {
						e.printStackTrace(); //TODO handle it
					}
					sum = sum + simMeasure;
					n++;
				}
			}	
		}
		return n == 0 ? 0 : (sum / n);
	}
/*----------------------------- MAIN ----------------------------------------------------------*/	
	public static void main(String[] args) {	
		String depsFilename = args[0];
		String authFilename = args[1];
		String topicsFilename = args[2];
		String langType = args[3];
		String outFilename = args[4];
		String pkgFilename = args[5];
		switch(langType) {
			case "c":
				selectedLangType = LangType.C;
				break;
			case "java":
			default:
				selectedLangType = LangType.JAVA;
				break;
		}
		
		RsfReader.loadRsfDataFromFile(depsFilename);
		List<List<String>> depFacts = RsfReader.unfilteredFaCtS;
		RsfReader.loadRsfDataFromFile(authFilename);
		List<List<String>> clusterFacts = RsfReader.unfilteredFaCtS;
		RsfReader.loadRsfDataFromFile(pkgFilename);
		List<List<String>> pkgFacts = RsfReader.unfilteredFaCtS;
		
		System.out.println("Finished loading data from all files");
		ReverseAnalysis ra;
		ra = new ReverseAnalysis();
		ra.initializeFileIO(outFilename);

		Map<String, Set<String>> clusterMap = ClusterUtil
			.buildClusterMap(clusterFacts);
		Map<String, Set<MutablePair<String, String>>> internalEdgeMap = ClusterUtil
			.buildInternalEdgesPerCluster(clusterMap, depFacts);
		Map<String, Set<MutablePair<String, String>>> externalEdgeMap = ClusterUtil
			.buildExternalEdgesPerCluster(clusterMap, depFacts);
		
		Map<String,Integer> pkgSizeMap = computePkgSizes(pkgFacts);

		Map<String,Map<String,Entity>> clusterNameToEntities = ra.buildFeatureSetPerClusterEntity(clusterMap,depFacts);
		ra.initDocTopics(clusterNameToEntities, topicsFilename, langType); // -UNCOMMENT OUT FOR JSDIVERGENCE 
		ra.printClusterNameToEntities(clusterNameToEntities);
		try {
			ra.calculateResults(clusterNameToEntities, internalEdgeMap, externalEdgeMap, pkgSizeMap);
		} catch (IOException e) {
			e.printStackTrace();
		}     
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

	private void writeToFile(Double content) {
		String str = "";
		System.out.println (content);
		str += content + ",";
		try {
			out.write(str);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void printClusterNameToEntities(Map<String, Map<String, Entity>> clusterNameToEntities) {
		for(String clusterName: clusterNameToEntities.keySet())
		{
			Map<String, Entity> nameToEntity = clusterNameToEntities.get(clusterName);
	
			System.out.println("CLUSTER NAME: " + clusterName);
			for(String entityName: nameToEntity.keySet())
				System.out.println("---Entity name--- : " + entityName);
		}
	}
}