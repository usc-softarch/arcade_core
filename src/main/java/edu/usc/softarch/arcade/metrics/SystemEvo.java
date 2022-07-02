package edu.usc.softarch.arcade.metrics;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.usc.softarch.arcade.clustering.ConcernClusterArchitecture;
import edu.usc.softarch.arcade.facts.ConcernCluster;
import edu.usc.softarch.arcade.util.McfpDriver;

public class SystemEvo {
	//region PUBLIC INTERFACE
	public static void main(String[] args) {
		run(args[0], args[1]); }

	public static double run(String sourceRsf, String targetRsf) {
		ConcernClusterArchitecture sourceClusters =
			ConcernClusterArchitecture.loadFromRsf(sourceRsf);
		ConcernClusterArchitecture targetClusters =
			ConcernClusterArchitecture.loadFromRsf(targetRsf);

		// Determine the larger architecture
		double numClustersToRemove =
			sourceClusters.size() > targetClusters.size()
				? sourceClusters.size() - targetClusters.size() : 0;
		double numClustersToAdd =
			targetClusters.size() > sourceClusters.size()
				? targetClusters.size() - sourceClusters.size() : 0;

		// Get all the existing entities in the two architectures
		Set<String> sourceEntities = getAllEntitiesInClusters(sourceClusters);
		Set<String> targetEntities = getAllEntitiesInClusters(targetClusters);

		// Remove the intersection from each set
		Set<String> entitiesToRemove = new HashSet<>(sourceEntities);
		entitiesToRemove.removeAll(targetEntities);
		Set<String> entitiesToAdd = new HashSet<>(targetEntities);
		entitiesToAdd.removeAll(sourceEntities);

		// We need to determine the intersection of entities between clusters
		// in the source and target to help us minimize the number of moves
		// We are mapping this problem to a problem of Maximum Weighted Matching,
		// and we use Hungurian Algorithm to solve it.

		// Map source architecture to indices and back
		int ns = sourceClusters.size();
		int nt = targetClusters.size();
		Map<Integer, ConcernCluster> sourceIndex = new HashMap<>();
		Map<ConcernCluster, Integer> sourceReverseIndex = new HashMap<>();
		int counter = 0;
		for (ConcernCluster source: sourceClusters){
			sourceIndex.put(counter, source);
			sourceReverseIndex.put(source, counter);
			counter++;
		}

		// Map target architecture to indices and back
		Map<Integer,ConcernCluster> targetIndex = new HashMap<>();
		Map<ConcernCluster,Integer> targetReverseIndex = new HashMap<>();
		counter = 0;
		for (ConcernCluster target:targetClusters){
			targetIndex.put(counter, target);
			targetReverseIndex.put(target, counter);
			counter++;
		}

		// Create this matrix thing and set all weights to 0
		MWBMatchingAlgorithm ma = new MWBMatchingAlgorithm(ns, nt);
		for (int i = 0; i < ns; i++)
			for (int j = 0; j < nt; j++)
				ma.setWeight(i, j, 0);

		for (ConcernCluster source : sourceClusters) {
			for (ConcernCluster target : targetClusters) {
				Set<String> intersection = new HashSet<>(source.getEntities());
				intersection.retainAll(target.getEntities());
				ma.setWeight(sourceReverseIndex.get(source),
					targetReverseIndex.get(target), intersection.size());
			}
		}

		Map<ConcernCluster, Set<String>> sourceClusterMatchEntities = new HashMap<>();
		Map<ConcernCluster, ConcernCluster> matchOfSourceInTarget = new HashMap<>();
		Map<ConcernCluster, ConcernCluster> matchOfTargetInSource = new HashMap<>();

		int[] match = ma.getMatching();

		for (int i = 0; i < match.length; i++){
			ConcernCluster source = sourceIndex.get(i);
			ConcernCluster target = new ConcernCluster();
			target.setName("-1"); // dummy, in case that the cluster is not matched to any cluster, to avoid null pointer exceptions
			if (match[i] != -1)
				target = targetIndex.get(match[i]);
			matchOfSourceInTarget.put(source, target);
			matchOfTargetInSource.put(target, source);
			Set<String> entitiesIntersection = new HashSet<>(source.getEntities());
			entitiesIntersection.retainAll(target.getEntities());
			sourceClusterMatchEntities.put(source, entitiesIntersection);
		}

		ConcernClusterArchitecture removedSourceClusters = new ConcernClusterArchitecture() ;

		// Remove unmatched clusters
		for (ConcernCluster source: sourceClusters) {
			ConcernCluster matched = matchOfSourceInTarget.get(source);
			if (matched.getName().equals("-1"))
				removedSourceClusters.add(source);
		}

		Set<String> entitiesToMoveInRemovedSourceClusters = new HashSet<>(); // Pooyan! These are the entities in the removed source clusters which exists in the target clusters and have to be moved
		for (ConcernCluster source : removedSourceClusters) {
			Set<String> entities = source.getEntities();
			entities.removeAll(entitiesToRemove); // Make sure we are not trying to move entities that no longer exist in the target cluster
			entitiesToMoveInRemovedSourceClusters.addAll(entities);
		}

		// The clusters that remain after removal of clusters
		ConcernClusterArchitecture remainingSourceClusters = new ConcernClusterArchitecture(sourceClusters);
		remainingSourceClusters.removeAll(removedSourceClusters);

		// for each cluster, the map gives the set of entities that may be moved (not including added or
		// removed entities)
		Map<ConcernCluster,Set<String>> entitiesToMoveInCluster = new HashMap<>();
		for (ConcernCluster remainingCluster : remainingSourceClusters) {
			Set<String> matchedIntersectionEntities = sourceClusterMatchEntities.get(remainingCluster);
			Set<String> currEntitiesToMove = new HashSet<>( remainingCluster.getEntities() );
			if (matchOfSourceInTarget.get(remainingCluster) != null && matchOfTargetInSource.get(matchOfSourceInTarget.get(remainingCluster)).equals(remainingCluster))// Pooyan! if the ramaining cluster is  the base cluster, it is entity should not be removed, otherwise they should be in the current entity to move
				currEntitiesToMove.removeAll(matchedIntersectionEntities); // the problem is here!!! It should move the maxIntersecting Entities since the cluster in the other arc is assigned to another cluster

			currEntitiesToMove.removeAll(entitiesToAdd);
			currEntitiesToMove.removeAll(entitiesToRemove);
			entitiesToMoveInCluster.put(remainingCluster,currEntitiesToMove);
		}

		Set<String> allEntitiesToMove = new HashSet<>();
		for (Set<String> currEntitiesToMove : entitiesToMoveInCluster.values()) {
			allEntitiesToMove.addAll(currEntitiesToMove); // entities to move in clusters not removed
		}
		allEntitiesToMove.addAll(entitiesToMoveInRemovedSourceClusters); // entities to move in removed clusters (i.e., all the entities in those clusters)
		allEntitiesToMove.addAll(entitiesToAdd);
		allEntitiesToMove.addAll(entitiesToRemove);

		double numer = numClustersToRemove + numClustersToAdd + entitiesToRemove.size()
			+ entitiesToAdd.size() + allEntitiesToMove.size();
		double denom = sourceClusters.size() + 2.0 * sourceEntities.size()
			+ targetClusters.size() + 2.0 * targetEntities.size();

		double localSysEvo = (1 - numer / denom) * 100;

		System.out.println("A2A: " + localSysEvo);

		sysEvo = localSysEvo;

		return sysEvo;
	}
	//endregion

	//region ATTRIBUTES
	public static double sysEvo = 0;
	private final McfpDriver mcfpDriver;
//	private final Architecture sourceClusters;
//	private final Architecture targetClusters;
	//endregion

	//region CONSTRUCTORS
	public SystemEvo(String sourceRsf, String targetRsf) throws IOException {
		this.mcfpDriver = new McfpDriver(sourceRsf, targetRsf);
//		this.sourceClusters = Architecture
	}
	//endregion

	private static Set<String> getAllEntitiesInClusters(
			ConcernClusterArchitecture clusters) {
		Set<String> entities = new HashSet<>();
		for (ConcernCluster cluster : clusters)
			entities.addAll(cluster.getEntities());
		return entities;
	}
}
