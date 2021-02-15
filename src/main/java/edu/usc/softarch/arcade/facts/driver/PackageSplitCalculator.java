package edu.usc.softarch.arcade.facts.driver;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import edu.usc.softarch.arcade.facts.ConcernCluster;
import edu.usc.softarch.arcade.facts.GroundTruthFileParser;

public class PackageSplitCalculator {
	private static String exprToGrabPackageName = "(.+)\\..+$"; // for java packages
	private static Pattern grabPkgPattern = Pattern.compile(exprToGrabPackageName);

	public static void main(String[] args) {
		String clusterRsfFilename = args[0];
		String fileType = "java";
		if (args.length >= 2)	fileType = args[1];
		
		if (fileType.equals("c")) {
			exprToGrabPackageName = "(.+)\\/.+$";
			grabPkgPattern = Pattern.compile(exprToGrabPackageName);
		}
		else if (fileType.equals("java")) {
			// use default
		}
		
		GroundTruthFileParser.parseRsf(clusterRsfFilename);
		
		Set<ConcernCluster> clusters = GroundTruthFileParser.getClusters();
		Set<String> clusterNames = new HashSet<>();
		
		for (ConcernCluster cluster : clusters) {
			clusterNames.add(cluster.getName());
		}
		
		int clusterCountOfEntitiesNotInSamePackage = 0;
		Set<String> clustersWithEntitiesNotInSamePackage = new HashSet<>();
		Set<String> clustersWithEntitiesInSamePackage = new HashSet<>(clusterNames);
		
		// Count and record the clusters that have entities from different packages in them
		for (ConcernCluster cluster : clusters) {
			String prevEntityPkg = null;
			for (String entity : cluster.getEntities()) {
				Matcher matcher = grabPkgPattern.matcher(entity);
				String currEntityPkg = null;
				if (matcher.find()) {
					currEntityPkg = matcher.group(1);
				}
				if (prevEntityPkg == null) {
					prevEntityPkg = currEntityPkg;
				}
				else {
					if (!prevEntityPkg.equals(currEntityPkg)) {
						clusterCountOfEntitiesNotInSamePackage++;
						clustersWithEntitiesNotInSamePackage.add(cluster.getName());
						break;
					}
				}
			}
		}
	
		clustersWithEntitiesInSamePackage.removeAll(clustersWithEntitiesNotInSamePackage);
		
		Map<String,Set<String>> splitPkgs = new HashMap<>();
		for (String clusterName1 : clustersWithEntitiesInSamePackage) {
			String pkgName1 = getPackageNameOfFirstEntity(clusterName1, clusters);
			for (String clusterName2 : clustersWithEntitiesInSamePackage) {
				if (!clusterName1.equals(clusterName2)) {
					String pkgName2 = getPackageNameOfFirstEntity(clusterName2,
							clusters);
					if (pkgName1.equals(pkgName2)) {
						String splitPkg = pkgName1;
						Set<String> clustersOfSplitPkg = splitPkgs
								.get(splitPkg);
						if (clustersOfSplitPkg == null) {
							clustersOfSplitPkg = new HashSet<>();
						}
						clustersOfSplitPkg.add(clusterName1);
						clustersOfSplitPkg.add(clusterName2);
						splitPkgs.put(splitPkg, clustersOfSplitPkg);
					}
				}
			}
		}
		
		assert clusterCountOfEntitiesNotInSamePackage == clustersWithEntitiesNotInSamePackage.size();
		assert clustersWithEntitiesInSamePackage.size() + clustersWithEntitiesNotInSamePackage.size() == clusters.size();
		
		int numOfSplitClusters = 0;
		for (Set<String> splitClustersOfSinglePkg : splitPkgs.values()) {
			numOfSplitClusters += splitClustersOfSinglePkg.size();
		}
		
		Set<String> splitClusters = new HashSet<>();
		for (Set<String> splitClustersOfSinglePkg : splitPkgs.values()) {
			splitClusters.addAll(splitClustersOfSinglePkg);
		}
		
		Set<String> clustersWherePkgsIsNotEnough = new HashSet<>(splitClusters);
		clustersWherePkgsIsNotEnough.addAll(clustersWithEntitiesNotInSamePackage);
		
		double proportionOfSplitClusters = (double)numOfSplitClusters/(double)clusters.size();
		double proportionOfClustersNotInTheSamePkg = (double)clusterCountOfEntitiesNotInSamePackage/(double)clusters.size();
		double proportionWherePkgIsNotEnough = (double)clustersWherePkgsIsNotEnough.size()/(double)clusters.size();
		System.out.println("clusters count with entities that are NOT in the same package: " + clusterCountOfEntitiesNotInSamePackage);
		System.out.println("clusters count with entities that ARE in the same package: " + clustersWithEntitiesInSamePackage.size());
		System.out.println("total no. of clusters: " + clusters.size());
		System.out.println("proportion of clusters not in the same package: " + proportionOfClustersNotInTheSamePkg );
		System.out.println("clusters with entities that are NOT in the same package: ");
		System.out.println(String.join("\n", clustersWithEntitiesNotInSamePackage));
		System.out.println("clusters with entities that ARE in the same package: ");
		System.out.println(String.join("\n", clustersWithEntitiesInSamePackage));
		System.out.println("no. of split clusters: " + numOfSplitClusters);
		System.out.println("split packages: ");
		System.out.println(String.join("\n", splitPkgs.keySet().stream()
			.map(key -> key + ":" + splitPkgs.get(key)).collect(Collectors.toList())));
		System.out.println("proportion of split clusters: " + proportionOfSplitClusters);
		System.out.println("no. of clusters where package structure is not enough: " + clustersWherePkgsIsNotEnough.size());
		System.out.println("proportion of clusters where package structure is not enough: " + proportionWherePkgIsNotEnough);
		System.out.println("components where packages and directories are not enough:");
		System.out.println(String.join("\n", clustersWherePkgsIsNotEnough));
	}

	private static String getPackageNameOfFirstEntity(String clusterName, Set<ConcernCluster> clusters) {
		for (ConcernCluster cluster : clusters) {
			if (cluster.getName().equals(clusterName)) {
				String firstEntity = (String) cluster.getEntities().toArray()[0];
				Matcher matcher = grabPkgPattern.matcher(firstEntity);
				if (matcher.find()) {
					return matcher.group(1);
				} else {
					return "";
				}
			}
		}
		throw new RuntimeException("Should not exit for loop above");
	}
}