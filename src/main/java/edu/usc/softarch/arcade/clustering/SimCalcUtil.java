package edu.usc.softarch.arcade.clustering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Pattern;

import edu.usc.softarch.arcade.config.ConfigUtil;
import edu.usc.softarch.arcade.topics.TopicUtil;

/**
 * @author joshua
 */
public class SimCalcUtil {
	public static Map<Set<FeatureVector>, Integer> sharedFeaturesMap = new HashMap<>();
	public static Map<List<FeatureVector>, Integer> oneZeroFeaturesMap = new HashMap<>();
	public static Map<List<FeatureVector>, Integer> zeroOneFeaturesMap = new HashMap<>();
	public static Map<Set<FeatureVector>, Double> sumSharedFeaturesMap = new HashMap<>();
	public static Map<List<FeatureVector>, Double> divergenceMap = new HashMap<>();
	
	public static void verifySymmetricFeatureVectorOrdering(FeatureVector fv1, FeatureVector fv2) {
		for (int i = 0;i<fv1.size();i++) {
			if (!fv1.get(i).edge.tgtStr.equals(fv2.get(i).edge.tgtStr)) {
				System.out.println("In, " + Thread.currentThread().getStackTrace()[1].getMethodName() + ", Feature order incorrect");
				System.exit(0);
			}
		}
		System.out.println("In, " + Thread.currentThread().getStackTrace()[1].getMethodName() + ", Feature order correct...continuing");
	}
	
	public static void verifySymmetricClusterOrdering(Vector<Cluster> clusters) {
		Cluster firstCluster = clusters.firstElement();
		for (Cluster c : clusters) {
			for (int i=0;i<firstCluster.size();i++) {
				if (!firstCluster.get(i).edge.tgtStr.equals(c.get(i).edge.tgtStr)) {
					System.out.println("In, " + Thread.currentThread().getStackTrace()[1].getMethodName() + "Feature order incorrect");
					System.exit(0);
				}
				
			}
		}
		System.out.println("In, " + Thread.currentThread().getStackTrace()[1].getMethodName() + "Feature order correct...continuing");
	}
	
	public static double getJSDivergence(FastCluster cluster, FastCluster otherCluster) {
		return TopicUtil.jsDivergence(cluster.docTopicItem, otherCluster.docTopicItem);
	}
	
	public static double getJSDivergence(Entity entity1, Entity entity2) {
		return TopicUtil.jsDivergence(entity1.docTopicItem, entity2.docTopicItem);
	}
	
	
	public static double getJSDivergence(FeatureVector fv1, FeatureVector fv2) {
		String strippedLeafSplitClusterName = ConfigUtil.stripParensEnclosedClassNameWithPackageName(fv1);
		
		if (Pattern.matches(ConfigUtil.anonymousInnerClassRegExpr, strippedLeafSplitClusterName))
			return Integer.MAX_VALUE;
		
		if (Pattern.matches(ConfigUtil.doubleInnerClassRegExpr, strippedLeafSplitClusterName))
			return Integer.MAX_VALUE;
		
		strippedLeafSplitClusterName = ConfigUtil.stripParensEnclosedClassNameWithPackageName(fv2);
		
		if (Pattern.matches(ConfigUtil.anonymousInnerClassRegExpr, strippedLeafSplitClusterName))
			return Integer.MAX_VALUE;
		
		if (Pattern.matches(ConfigUtil.doubleInnerClassRegExpr, strippedLeafSplitClusterName))
			return Integer.MAX_VALUE;
		
		List<FeatureVector> featureVecPair = new ArrayList<>(2);
		featureVecPair.add(fv1);
		featureVecPair.add(fv2);
		
		double divergence = 0; 
		
		if (divergenceMap.containsKey(featureVecPair))
			divergence = divergenceMap.get(featureVecPair);
		else {
			divergence = TopicUtil.jsDivergence(fv1.docTopicItem, fv2.docTopicItem);
			divergenceMap.put(new ArrayList<>(featureVecPair), divergence);
		}
		
		return divergence;
	}
	
	public static double getUnbiasedEllenbergMeasure(FeatureVector fv1, FeatureVector fv2) {
		List<FeatureVector> featureVecPair = new ArrayList<>(2);
		featureVecPair.add(fv1);
		featureVecPair.add(fv2);
		double sumSharedFeatures = 0;
		int num10Features = 0;
		int num01Features = 0;
		if (sumSharedFeaturesMap.containsKey(featureVecPair)) {
			sumSharedFeatures = sumSharedFeaturesMap.get(featureVecPair);
		}
		else {
			sumSharedFeatures = getSumSharedFeatures(fv1,fv2);
			sumSharedFeaturesMap.put(new HashSet<>(featureVecPair), sumSharedFeatures);
		}
		
		if (oneZeroFeaturesMap.containsKey(featureVecPair)) {
			num10Features = oneZeroFeaturesMap.get(featureVecPair);
		}
		else {
			num10Features = getNum10Features(fv1,fv2);
			oneZeroFeaturesMap.put(featureVecPair, num10Features);
		}
		
		if (zeroOneFeaturesMap.containsKey(featureVecPair)) {
			num01Features = zeroOneFeaturesMap.get(featureVecPair);
		}
		else {
			num01Features = getNum01Features(fv1,fv2);
			zeroOneFeaturesMap.put(featureVecPair, num01Features);
		}
		
		double denom = 0.5*sumSharedFeatures +num10Features + num01Features;
		if (denom == 0) {
			return denom;
		}
		return 0.5*sumSharedFeatures / (denom);
	}
	
	public static double getJaccardSim(FeatureVector fv1, FeatureVector fv2) {
		List<FeatureVector> featureVecPair = new ArrayList<>(2);
		featureVecPair.add(fv1);
		featureVecPair.add(fv2);
		int numSharedFeatures = 0;
		int num10Features = 0;
		int num01Features = 0;
		if (sharedFeaturesMap.containsKey(featureVecPair)) {
			numSharedFeatures = sharedFeaturesMap.get(featureVecPair);
		}
		else {
			numSharedFeatures = getNumSharedFeatures(fv1,fv2);
			sharedFeaturesMap.put(new HashSet<>(featureVecPair), numSharedFeatures);
		}
		
		if (oneZeroFeaturesMap.containsKey(featureVecPair)) {
			num10Features = oneZeroFeaturesMap.get(featureVecPair);
		}
		else {
			num10Features = getNum10Features(fv1,fv2);
			oneZeroFeaturesMap.put(featureVecPair, num10Features);
		}
		
		if (zeroOneFeaturesMap.containsKey(featureVecPair)) {
			num01Features = zeroOneFeaturesMap.get(featureVecPair);
		}
		else {
			num01Features = getNum01Features(fv1,fv2);
			zeroOneFeaturesMap.put(featureVecPair, num01Features);
		}
		
		double denom = numSharedFeatures +num10Features + num01Features;
		if (denom == 0) {
			return denom;
		}
		return (double)numSharedFeatures / (denom);
	}
	
	public static int getNum10Features(FeatureVector fv1, FeatureVector fv2) {
		int count=0;
		for (int i=0;i<fv1.size();i++) {
			Feature f = fv1.get(i);
			Feature f2 = fv2.get(i);
			System.out.println("f.edge.tgtStr: " + f.edge.tgtStr);
			System.out.println("f2.edge.tgtStr: " + f2.edge.tgtStr);
			System.out.println("f.value: " + f.value);
			System.out.println("f2.value: " + f2.value);
			System.out.println();
			
			if (f.value > 0 && f2.value == 0) {
				count++;
				System.out.println("Increased 11 count to: " + count);
			}
		}
		return count;
	}
	
	public static int getNum01Features(FeatureVector fv1, FeatureVector fv2) {
		int count=0;
		
		for (int i=0;i<fv1.size();i++) {
			Feature f = fv1.get(i);
			Feature f2 = fv2.get(i);
			System.out.println("f.edge.tgtStr: " + f.edge.tgtStr);
			System.out.println("f2.edge.tgtStr: " + f2.edge.tgtStr);
			System.out.println("f.value: " + f.value);
			System.out.println("f2.value: " + f2.value);
			System.out.println();
			
			if (f.value == 0 && f2.value > 0) {
				count++;
				System.out.println("Increased 11 count to: " + count);
			}
		}
		return count;
	}
	
	public static int getNumSharedFeatures(FeatureVector fv1, FeatureVector fv2) {
		int count=0;
		
		for (int i=0;i<fv1.size();i++) {
			Feature f = fv1.get(i);
			Feature f2 = fv2.get(i);
			System.out.println("f.edge.tgtStr: " + f.edge.tgtStr);
			System.out.println("f2.edge.tgtStr: " + f2.edge.tgtStr);
			System.out.println("f.value: " + f.value);
			System.out.println("f2.value: " + f2.value);
			System.out.println();
			
			if (f.edge.tgtStr.equals(f2.edge.tgtStr) && f.value == 1 && f2.value == 1) {
				count++;
				System.out.println("Increased 11 count to: " + count);
			}
		}
		
		return count;
	}
	
	public static double getSumSharedFeatures(FeatureVector fv1, FeatureVector fv2) {
		double sharedFeatureSum = 0;
		for (int i=0;i<fv1.size();i++) {
			Feature f = fv1.get(i);
			Feature f2 = fv2.get(i);
			System.out.println("f.edge.tgtStr: " + f.edge.tgtStr);
			System.out.println("f2.edge.tgtStr: " + f2.edge.tgtStr);
			System.out.println("f.value: " + f.value);
			System.out.println("f2.value: " + f2.value);
			System.out.println();
			
			if (f.value > 0 && f2.value > 0)
				sharedFeatureSum = f.value + f2.value;
		}
		
		return sharedFeatureSum;
	}
	
	public static double getSumOneZeroFeatures(FeatureVector fv1, FeatureVector fv2) {
		double oneZeroFeatureSum = 0;
		for (int i=0;i<fv1.size();i++) {
			Feature f = fv1.get(i);
			Feature f2 = fv2.get(i);
			System.out.println("f.edge.tgtStr: " + f.edge.tgtStr);
			System.out.println("f2.edge.tgtStr: " + f2.edge.tgtStr);
			System.out.println("f.value: " + f.value);
			System.out.println("f2.value: " + f2.value);
			System.out.println();
			
			if (f.value > 0 && f2.value == 0)
				oneZeroFeatureSum = f.value + f2.value;
		}
		
		return oneZeroFeatureSum;
	}
	
	public static double getSumZeroOneFeatures(FeatureVector fv1, FeatureVector fv2) {
		double zeroOneFeatureSum = 0;
		for (int i=0;i<fv1.size();i++) {
			Feature f = fv1.get(i);
			Feature f2 = fv2.get(i);
			System.out.println("f.edge.tgtStr: " + f.edge.tgtStr);
			System.out.println("f2.edge.tgtStr: " + f2.edge.tgtStr);
			System.out.println("f.value: " + f.value);
			System.out.println("f2.value: " + f2.value);
			System.out.println();
			
			if (f.value == 0 && f2.value > 0)
				zeroOneFeatureSum = f.value + f2.value;
		}
		
		return zeroOneFeatureSum;
	}
}