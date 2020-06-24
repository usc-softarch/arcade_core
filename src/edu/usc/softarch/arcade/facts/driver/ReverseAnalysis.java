package edu.usc.softarch.arcade.facts.driver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.google.common.base.Joiner;

import cc.mallet.util.Maths;
import edu.usc.softarch.arcade.MetricsDriver;
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

public class ReverseAnalysis 
{	
	static Map<String,Integer> featureNameToBitsetIndex = new HashMap<String,Integer>();
	static int bitSetSize = 0;
	BufferedWriter out;
	enum SimilarityMeasure {UELLENBERG, JS, LIMBO, BUNCH, UNM, PKG};
	SimilarityMeasure sm;
	enum LangType {JAVA,C};
	static LangType selectedLangType;
	
	/**method that calculates the result for every cluster and displays it 
	 * @throws IOException */
	private void calculateResults(
			Map<String, Map<String, Entity>> clusterNameToEntities,
			Map<String, Set<MutablePair<String, String>>> internalEdgeMap,
			Map<String, Set<MutablePair<String, String>>> externalEdgeMap,
			HashMap<String,Integer> pkgSizeMap)
			throws IOException
	{
		Map<String,Set<String>> clusterNameToEntitiesNames = new HashMap<String,Set<String>>();
		for (String clusterName : clusterNameToEntities.keySet()) {

			Map<String, Entity> nameToEntity = clusterNameToEntities
					.get(clusterName);
			Object[] entities = nameToEntity.values().toArray();

			Set<String> entityNames = new HashSet<String>();
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
			Map<String, Entity> nameToEntity = clusterNameToEntities.get(clusterName);
			
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
	
	private static HashMap<String, Integer> computePkgSizes(List<List<String>> pkgFacts) {
		HashMap<String,Integer> pkgSizeMap = new HashMap<String,Integer>();
		
		
		
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

	private double computePkgClusterSim(String clusterName,Map<String,Map<String,Entity>> clusterNameToEntities,HashMap<String,Integer> pkgSizeMap) {
		Map<String, Entity> nameToEntity = clusterNameToEntities
				.get(clusterName);
		Object[] entities = nameToEntity.values().toArray();
		
		if (entities.length == 0) {
			System.out.println(clusterName + " has no entities, so skipping");
			return 0;
		}

		List<String> entityNames = new ArrayList<String>();
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
		else {
			throw new RuntimeException("Invalid language selected");
		}

		Map<String, Integer> pkgCountMap = new HashMap<String, Integer>();
		for (Object obj : entities) {
			Entity entity = (Entity) obj;
			
				String[] tokens = entity.name.split(regexDelimiter);
				String directoryName = "";
				List<String> directoryNameParts = new ArrayList<String>();
				for (int i=0;i<tokens.length-1;i++) {
					directoryNameParts.add(tokens[i]);
				}
				directoryName = StringUtils.join(directoryNameParts,delimiter);
				
				if (pkgCountMap.containsKey(directoryName)) {
					pkgCountMap.put(directoryName,
							pkgCountMap.get(directoryName) + 1);
				} else {
					pkgCountMap.put(directoryName, 1);
				}
		}

		Collection<Integer> pkgCounts = pkgCountMap.values();
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
		if (maxPkgName.endsWith(delimiter)) {
			maxPkgName = maxPkgName.substring(0, maxPkgName.length()-1);
		}
		
		int pkgSize = 0;
		if (maxPkgName.equals("")) {
			pkgSize = pkgSizeMap.get("default.ss");
		}
		else {
			pkgSize = pkgSizeMap.get(maxPkgName);
		}
		assert pkgSize != 0;

		double samePkgToClusterSizeRatio = (double) maxCount
				/ (double) entities.length;
		double samePkgToPkgSizeRatio = (double)maxCount/(double)pkgSize;
		double simValWithSize = (samePkgToClusterSizeRatio+samePkgToPkgSizeRatio)/2;
		double simValNoSize = samePkgToClusterSizeRatio;

		return simValNoSize;

	}
	
	/**method calculates the average sim measure for a cluster 
	 * @throws IOException **/
	private double computeClusterSimilarity(SimilarityMeasure sm, String clusterName, Map<String,Map<String,Entity>> clusterNameToEntities, Map<String,Set<MutablePair<String,String>>> internalEdgeMap, Map<String,Set<MutablePair<String,String>>> externalEdgeMap) throws IOException
	{
		if(sm == SimilarityMeasure.BUNCH)
		{
			double countInternalEdges = 0.0;
			double countExternalEdges = 0.0;
			Set<MutablePair<String,String>> intEdges = internalEdgeMap.get(clusterName);
			Set<MutablePair<String,String>> extEdges = externalEdgeMap.get(clusterName);
			for (MutablePair<String,String> edge : intEdges) 
			{ 
					countInternalEdges++;
			}
			for (MutablePair<String,String> edge : extEdges) 
			{ 
					countExternalEdges++;
			}
			double cf = (2*countInternalEdges)/((2*countInternalEdges) + countExternalEdges);
			return cf;
		} 
		else {
			Map<String, Entity> nameToEntity = clusterNameToEntities
					.get(clusterName);
			Object[] entities = nameToEntity.values().toArray();
			double sum = 0;
			int n = 0; // number of simMeasure values
			for (int i = 0; i < entities.length; i++) // double-for loop to get
														// two entities to
														// compute pairwise
														// similarity on
			{
				for (int j = i + 1; j < entities.length; j++) {
					double simMeasure = computePairWiseSimilarity(sm,
							(Entity) entities[i], (Entity) entities[j]);
					sum = sum + simMeasure;
					n++;
				}
			}
			System.out.println("Sum and n are " + sum + " " + n);
			double average = (sum / n);
			return average;
		}
	}
	/**method to compute similarity between a pair of entities */
	private double computePairWiseSimilarity(SimilarityMeasure sm, Entity entity1, Entity entity2)
	{
		if(sm == SimilarityMeasure.LIMBO)
		{
			Set<Integer> c1Indices = entity1.nonZeroFeatureMap.keySet();
			entity1.setNonZeroFeatureMapForLibmoUsingIndices(entity1, entity2, c1Indices);
			
			Set<Integer> c2Indices = entity2.nonZeroFeatureMap.keySet();
			entity2.setNonZeroFeatureMapForLibmoUsingIndices(entity1, entity2, c2Indices);
			return(this.getInfoLossMeasure(2, entity1, entity2));
		}
		
		BitSet fv1 = entity1.featureVector;
		BitSet fv2 = entity2.featureVector;
		int count10 = 0;
		int count01 = 0;
		int count00 = 0;
		int count11 = 0;
		int sum11 = 0;
		for (int i=0;i<fv1.size();i++) 
		{
			if (fv1.get(i) && !fv2.get(i)) 
			{
				count10++;
			}
			else if (!fv1.get(i) && fv2.get(i))
			{
				count01++;
			}
			else if (!fv1.get(i) && !fv2.get(i))
			{
				count00++;
			}
			else
			{
				count11++;
				sum11 = sum11 + 1 + 1;
			}
		}
		if(sm == SimilarityMeasure.UELLENBERG)
		{
			double denom = 0.5*sum11 + count10 + count01;
			if (denom == 0) {
				return denom;
				}
			return (double)0.5*sum11 / (denom);
		}
		else if(sm == SimilarityMeasure.JS)
		{
			double denom = count11 +count10 + count01;
			if (denom == 0) {
				return denom;
			}
			return (double)count11 / (denom);
		}
		else if(sm == SimilarityMeasure.UNM)
		{
			double result = 0.5*sum11/(0.5*sum11+2*((double)count10+(double)count01) + (double)count00 + (double)count11);
			return result;
		}
		return 0;
	}
	
	/** method that produces a feature vector bitset for each entity in each cluster **/
	public Map<String,Map<String,Entity>> buildFeatureSetPerClusterEntity(Map<String,Set<String>> clusterMap, List<List<String>> depFacts) {
		Map<String,Map<String,Entity>> map = new HashMap<String, Map<String,Entity>>();
			
		for (String clusterName : clusterMap.keySet()) 
		{ // for each cluster name
			Map<String,Entity> entityToFeatures = new HashMap<String,Entity>(); //using a map<String,Entity> instead of a list of entities so that getting the feature
			//vector for an Entity name will be faster. Mapping name of entity to Entity object.
			for (List<String> depFact : depFacts) 
			{
				Entity entity;
				String source = depFact.get(1);
				String target = depFact.get(2);
		
				if (clusterMap.get(clusterName).contains(source)) //if cluster contains entity
				{ 
					    Set<String> featureSet; //featureSet contains a list of all featureNames for that entity
					 
					    if(map.get(clusterName) != null) //if cluster already exists in map that is being built
					    {
					    	entityToFeatures = map.get(clusterName);
					    }   
						if(entityToFeatures.get(source) != null) 
						{
								 featureSet = entityToFeatures.get(source).featureSet;
								 entity = entityToFeatures.get(source);
						}
						else //otherwise create new ones
						{
							entity = new Entity(source);
							featureSet = new HashSet<String>();
						}
						featureSet.add(target); //adding target to set of features for that entity
						entity.featureSet = featureSet;
						if(featureNameToBitsetIndex.get(target) == null) //if this target has never been encountered yet
						{
							featureNameToBitsetIndex.put(target, new Integer(bitSetSize));
							entity.featureVector.set(bitSetSize); //setting the spot for this feature as 1 in the entitie's feature vector
							bitSetSize++;
						}
						else
						{
							entity.featureVector.set(featureNameToBitsetIndex.get(target)); //setting that feature to true
						}
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
	public static double getInfoLossMeasure(int numberOfEntitiesToBeClustered, Entity entity1,
			Entity entity2) 
	{
		
		double[] firstDist = new double[bitSetSize];
		double[] secondDist = new double[bitSetSize];	
		normalizeFeatureVectorOfCluster(entity1, bitSetSize, firstDist);
		normalizeFeatureVectorOfCluster(entity2, bitSetSize, secondDist);
		
		double jsDivergence = Maths.jensenShannonDivergence(firstDist, secondDist);
		System.out.println("JsDivergence is " + jsDivergence);
		if (Double.isInfinite(jsDivergence)) {
			jsDivergence = Double.MAX_VALUE;
		}
		System.out.println("numentities of entity1 " + entity1.getNumEntities());
		double infoLossMeasure = ((double) entity1.getNumEntities()/numberOfEntitiesToBeClustered + (double)entity2.getNumEntities()/numberOfEntitiesToBeClustered) * jsDivergence;
		System.out.println("InfoLossMeasure is " + infoLossMeasure);
		if (Double.isNaN(infoLossMeasure)) {
			throw new RuntimeException("infoLossMeasure is NaN");
		}	
		return infoLossMeasure;
	}
	
	private static void normalizeFeatureVectorOfCluster(Entity entity,
			int featuresLength, double[] firstDist) 
	{ 
		for (int i=0;i<featuresLength;i++) 
		{
			if (entity.nonZeroFeatureMap.get(i) != null) 
			{
				double featureValue = entity.nonZeroFeatureMap.get(i);
				firstDist[i] = featureValue/entity.nonZeroFeatureMap.size();
			}
			else { // this feature is zero
				firstDist[i] = 0;
			}
		}
	}
	
	
	//----------------------DOCTOPICITEM STUFF----------------------------------------------------//
	/** method to load doc-topic-item for each entity - reference ConcernClusteringRunner.initializeDocTopicsForEachFastCluster(), pretty much the same
		/thing except instead of using FastClusters, this uses Entity data structure*/
	/*private void initializeDocTopicsForEachEntity(Map<String,Map<String,Entity>> clusterNameToEntities) 
	{
		for(String clusterName: clusterNameToEntities.keySet())
		{
			Map<String, Entity> nameToEntity = clusterNameToEntities.get(clusterName);
			Object[] entities = nameToEntity.values().toArray();
			System.out.println("INCLUSTER NAME: " + clusterName);
			for(String entityName: nameToEntity.keySet())
			{
				Entity entity = nameToEntity.get(entityName);
				if (TopicUtil.docTopics == null)
					TopicUtil.docTopics = TopicUtil.getDocTopicsFromVariableMalletDocTopicsFile();
				if (entity.docTopicItem == null)
					 TopicUtil.setDocTopicForEntity(TopicUtil.docTopics, entity);
			}
		}
	
	}*/
	
	private void initDocTopics(Map<String,Map<String,Entity>> clusterNameToEntities, String docTopicsFilename, String type) 
	{
		for(String clusterName: clusterNameToEntities.keySet())
		{
			Map<String, Entity> nameToEntity = clusterNameToEntities.get(clusterName);
			Object[] entities = nameToEntity.values().toArray();
			System.out.println("INCLUSTER NAME: " + clusterName);
			for(String entityName: nameToEntity.keySet())
			{
				Entity entity = nameToEntity.get(entityName);
				if (TopicUtil.docTopics == null)
					TopicUtil.docTopics = TopicUtil.getDocTopicsFromFile(docTopicsFilename);
				if (entity.docTopicItem == null)
					 TopicUtil.setDocTopicForEntity(TopicUtil.docTopics, entity, type);
			}
		}
	
	}
	
	/**
	 * Computes average JS Divergence of each cluster
	 * @param clusterName
	 * @param clusterNameToEntities
	 * @return
	 */
	private double computeJSDivergence(String clusterName, Map<String,Map<String,Entity>> clusterNameToEntities)
	{
		Map<String, Entity> nameToEntity = clusterNameToEntities.get(clusterName);
		Object[] entities = nameToEntity.values().toArray();
		double sum = 0;
		int n = 0; //number of simMeasure values
		for(int i = 0; i < entities.length; i++)
		{
			for(int j = i+1; j < entities.length; j++)
			{
				Entity entity1 = (Entity) entities[i];
				Entity entity2 = (Entity) entities[j];
				if ((entity1.docTopicItem != null) && (entity2.docTopicItem != null)) //this makes sure anonymous inner classes don't get
					//included in the computation
				{
					double simMeasure = SimCalcUtil.getJSDivergence(entity1, entity2);
					sum = sum + simMeasure;
					n++;
				}
			}	
		}
		double average = (sum/n);
		return average;
	}
/*----------------------------- MAIN ----------------------------------------------------------*/	
	public static void main(String[] args) 	
	{	
		String depsFilename = "";
		String authFilename = "";
		String topicsFilename = "";
		String langType = "";
		String outFilename = "outfile.csv";
		String pkgFilename = "";
		Options options = new Options();
		
		Option help = new Option( "help", "print this message" );
		
		Option depsFileOption = OptionBuilder.withArgName( "file" )
                .hasArg()
                .withDescription(  "RSF dependencies file" )
                .create( "depsFile" );
		
		Option authFileOption = OptionBuilder.withArgName( "file" )
                .hasArg()
                .withDescription(  "RSF authoritative file" )
                .create( "authFile" );
		
		Option topicsFileOption = OptionBuilder.withArgName( "file" )
                .hasArg()
                .withDescription(  "doc topics file" )
                .create( "topicsFile" );
		
		Option langTypeOption = OptionBuilder.withArgName( "langType" )
                .hasArg()
                .withDescription(  "language type [java|c]" )
                .create( "langType" );
		
		Option outFileOption = OptionBuilder.withArgName( "file" )
                .hasArg()
                .withDescription(  "output csv file" )
                .create( "outFile" );
		
		Option pkgFileOption = OptionBuilder.withArgName("file").hasArg()
				.withDescription("rsf file for packaged-based clustering")
				.create("pkgFile");
		
		options.addOption(help);
		options.addOption(depsFileOption);
		options.addOption(authFileOption);
		options.addOption(topicsFileOption);
		options.addOption(outFileOption);
		options.addOption(langTypeOption);
		options.addOption(pkgFileOption);
		
		
		 // create the parser
	    CommandLineParser parser = new GnuParser();
	    try {
	        // parse the command line arguments
	        CommandLine line = parser.parse( options, args );
	        
	        if (line.hasOption("help")) {
	        	// automatically generate the help statement
	        	HelpFormatter formatter = new HelpFormatter();
	        	formatter.printHelp( MetricsDriver.class.getName(), options );
	        	System.exit(0);
	        }
	        if (line.hasOption("depsFile")) {
	        	depsFilename = line.getOptionValue("depsFile");
	        }
	        if (line.hasOption("authFile")) {
	        	authFilename = line.getOptionValue("authFile");
	        }
	        if (line.hasOption("topicsFile")) {
	        	topicsFilename = line.getOptionValue("topicsFile");
	        }
	        if (line.hasOption("outFile")) {
	        	outFilename = line.getOptionValue("outFile");
	        }
	        if (line.hasOption("langType")) {
	        	langType = line.getOptionValue("langType");
	        	if (langType.equals("java")) {
	        		selectedLangType = LangType.JAVA;
	        	}
	        	else if (langType.equals("c")) {
	        		selectedLangType = LangType.C;
	        	}
	        	else {
	        		System.err.println("ERROR: Invalid language selected, forcing selection of java");
	        		selectedLangType = LangType.JAVA;
	        	}
	        	
	        }
	        if (line.hasOption("pkgFile")) {
	        	pkgFilename = line.getOptionValue("pkgFile");
	        }
	    }
	    catch( ParseException exp ) {
	        // oops, something went wrong
	        System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
	    }
		
		//RsfReader.loadRsfDataFromFile("archstudio4_deps (1).rsf");
		//RsfReader.loadRsfDataFromFile("hadoop-0.19-odem-facts.rsf");
		//RsfReader.loadRsfDataFromFile("bash_make_dep_facts.rsf");
		//RsfReader.loadRsfDataFromFile("oodt_0.2_full_clean_odem_facts.rsf");
		RsfReader.loadRsfDataFromFile(depsFilename);
		//RsfReader.loadRsfDataFromFile("mozilla.flat.compact.Author#88AD5.clean.rsf");
		//RsfReader.loadRsfDataFromFile("mozilla.static.flat.compact.rsf");
		//RsfReader.loadRsfDataFromFile("mozilla.static.rel.rsf");
		List<List<String>> depFacts = RsfReader.unfilteredFacts;	
		
		//RsfReader.loadRsfDataFromFile("archstudio4_clean_ground_truth_recovery.rsf");
		//RsfReader.loadRsfDataFromFile("hadoop-0.19_ground_truth.rsf");
		//RsfReader.loadRsfDataFromFile("bash_1.14_ground_truth_recovery.rsf");
		//RsfReader.loadRsfDataFromFile("oodt_0.2_full_ground_truth_recovery.rsf");
		RsfReader.loadRsfDataFromFile(authFilename);
		//RsfReader.loadRsfDataFromFile("mozilla.static.flat.compact.rsf");
		List<List<String>> clusterFacts = RsfReader.unfilteredFacts;
		
		RsfReader.loadRsfDataFromFile(pkgFilename);
		List<List<String>> pkgFacts = RsfReader.unfilteredFacts;
		
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
		
		HashMap<String,Integer> pkgSizeMap = computePkgSizes(pkgFacts);

		Map<String,Map<String,Entity>> clusterNameToEntities = ra.buildFeatureSetPerClusterEntity(clusterMap,depFacts);
		ra.initDocTopics(clusterNameToEntities, topicsFilename, langType); // -UNCOMMENT OUT FOR JSDIVERGENCE 
		ra.printClusterNameToEntities(clusterNameToEntities);
		try {
			ra.calculateResults(clusterNameToEntities, internalEdgeMap, externalEdgeMap, pkgSizeMap);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}     
	}
/*------------------------ UTILITY FUNCTIONS------------------------------------------------- */	
	public void initializeFileIO(String outFilename)
	{
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	private void writeToFile(Double content)
	{
		String str = "";
		System.out.println (content);
		str += content + ",";
		try {
			out.write(str);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	private void printClusterNameToEntities(Map<String, Map<String, Entity>> clusterNameToEntities) 
	{
		for(String clusterName: clusterNameToEntities.keySet())
		{
			Map<String, Entity> nameToEntity = clusterNameToEntities.get(clusterName);
	
			System.out.println("CLUSTER NAME: " + clusterName);
			for(String entityName: nameToEntity.keySet())
			{
				System.out.println("---Entity name--- : " + entityName);
				Entity entity = nameToEntity.get(entityName);
				//System.out.println("Entity's featureSet: ");
			    //	Set<String> featureSet = entity.featureSet;
				/*for(String featureName: featureSet)
				{
					System.out.println(featureName);
				}*/
				/*System.out.print("Feature vector bitset: ");
				for(int i = 0; i < this.bitSetSize; i++)
				{
					System.out.print(entity.featureVector.get(i));
				}*/
			}
		}
	}
	
	
}
