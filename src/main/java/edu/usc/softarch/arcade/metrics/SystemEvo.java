package edu.usc.softarch.arcade.metrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.google.common.base.Joiner;

import edu.usc.softarch.arcade.config.Config;
import edu.usc.softarch.arcade.facts.ConcernCluster;
import edu.usc.softarch.arcade.facts.driver.ConcernClusterRsf;

public class SystemEvo {
	static Logger logger = Logger.getLogger(SystemEvo.class);
	
	public static double sysEvo = 0;
	
	public static void main(String[] args) {
		PropertyConfigurator.configure(Config.getLoggingConfigFilename());
		sysEvo = 0;
		SystemEvoOptions options = new SystemEvoOptions();
		JCommander jcmd = new JCommander(options);
		
		try {
			jcmd.parse(args); 
		}
		catch (ParameterException e) {
			logger.debug(e.getMessage());
			jcmd.usage();
			System.exit(1);
		}
		
		logger.debug(options.parameters);
		logger.debug("\n");
		
		String sourceRsf = options.parameters.get(0);
		String targetRsf = options.parameters.get(1);
		
		Set<ConcernCluster> sourceClusters = ConcernClusterRsf.extractConcernClustersFromRsfFile(sourceRsf);
		Set<ConcernCluster> targetClusters = ConcernClusterRsf.extractConcernClustersFromRsfFile(targetRsf);
		
		logger.debug("Source clusters: ");
		logger.debug(clustersToString(sourceClusters));
		logger.debug("Target clusters: ");
		logger.debug(clustersToString(targetClusters));
		
		double numClustersToRemove = sourceClusters.size() >  targetClusters.size() ? sourceClusters.size() - targetClusters.size() : 0;
		double numClustersToAdd = targetClusters.size() > sourceClusters.size() ? targetClusters.size() - sourceClusters.size() : 0;
		
		logger.debug("\n");
		logger.debug("number of clusters to remove: " + numClustersToRemove);
		logger.debug("number of clusters to add: " + numClustersToAdd);
		
		Set<String> sourceEntities = getAllEntitiesInClusters(sourceClusters);
		Set<String> targetEntities = getAllEntitiesInClusters(targetClusters);
		
		logger.debug("\n");
		logger.debug("source entities: " + sourceEntities);
		logger.debug("target entities: " + targetEntities);
		
		Set<String> entitiesToRemove = new HashSet<String>(sourceEntities);
		entitiesToRemove.removeAll(targetEntities);
		Set<String> entitiesToAdd = new HashSet<String>(targetEntities);
		entitiesToAdd.removeAll(sourceEntities);
		
		logger.debug("\n");
		logger.debug("entities to remove: " + entitiesToRemove);
		logger.debug("entities to add: " + entitiesToAdd);

		// We need to determine the  intersection of entities between clusters in the source and target to help us minimize the number of moves
		// Pooyan!: We are maping this problem to a problem of Maximum Weighted Matching, and we use Hungurian Algorithm to solve it. 
		
		int ns = sourceClusters.size();
		int nt = targetClusters.size();
		Map<Integer,ConcernCluster> sourceNumToCluster = new HashMap<Integer,ConcernCluster>(); // Pooyan! It maps every source_cluster to a number from 0 to ns-1
		Map<ConcernCluster,Integer> sourceClusterToNum = new HashMap<ConcernCluster,Integer>(); // Pooyan! It maps every target_cluster to a number from 0 to nm-1
		int counter = 0 ;
		for (ConcernCluster source:sourceClusters){
			sourceNumToCluster.put(counter, source);
			sourceClusterToNum.put(source,  counter);
			counter++;
		}

		Map<Integer,ConcernCluster> targetNumToCluster = new HashMap<Integer,ConcernCluster>();
		Map<ConcernCluster,Integer> targetClusterToNum = new HashMap<ConcernCluster,Integer>();
		counter = 0;
		for (ConcernCluster target:targetClusters){
			targetNumToCluster.put(counter,  target);
			targetClusterToNum.put(target, counter);
			counter++;
		}
		
		MWBMatchingAlgorithm ma = new MWBMatchingAlgorithm(ns,nt);// Pooyan! Initiating the mathching
		for (int i=0;i<ns;i++)
			for (int j=0;j<nt;j++)
				ma.setWeight(i, j, 0);
		
		for (ConcernCluster sourceCluster : sourceClusters) {
			for (ConcernCluster targetCluster : targetClusters) {
				Set<String> entitiesIntersection = new HashSet<String>(sourceCluster.getEntities());
				entitiesIntersection.retainAll(targetCluster.getEntities());
				ma.setWeight(sourceClusterToNum.get(sourceCluster), targetClusterToNum.get(targetCluster), entitiesIntersection.size()); // Pooyan the weight of (source,target) as the interesection between them
			}	
		}
		
		Map<ConcernCluster,Set<String>> sourceClusterMatchEntities = new HashMap<ConcernCluster,Set<String>>(); //Pooyan! It keeps the source Cluster Match Entities, not necessarily the max match 
		Map<ConcernCluster,ConcernCluster> matchOfSourceInTarget = new HashMap<ConcernCluster,ConcernCluster>();//Pooyan! It keeps the matched cluster in target for every source
		Map<ConcernCluster,ConcernCluster> matchOfTargetInSource = new HashMap<ConcernCluster,ConcernCluster>();//Pooyan! It keeps the matched cluster in source for every target
		
		int[] match = ma.getMatching(); // Pooyan! calculates the max weighted match;
		
		for (int i=0;i<match.length;i++){

			ConcernCluster source = sourceNumToCluster.get(i);
			ConcernCluster target = new ConcernCluster();
			target.setName("-1"); // Pooyan! dummy, in case that the cluster is not matched to any cluster, to avoid null pointer exceptions
			if (match[i]!=-1)
				target=targetNumToCluster.get(match[i]) ;					
			matchOfSourceInTarget.put(source, target); // Pooyan! set the match of source
			matchOfTargetInSource.put(target, source); // Pooyan! set the match of target
			Set<String> entitiesIntersection = new HashSet<String>(source.getEntities());
			entitiesIntersection.retainAll(target.getEntities());
			sourceClusterMatchEntities.put(source, entitiesIntersection);	
			logger.debug("Pooyan -> "+source.getName() +" is matched to "+target.getName()+ " - the interesection size is " + entitiesIntersection.size() );
		}
		
		logger.debug("\n");
		logger.debug("Pooyan -> cluster -> intersecting entities in the matched source clusters");
		logger.debug(Joiner.on("\n").withKeyValueSeparator("->").join(sourceClusterMatchEntities));
		logger.debug("Pooyan -> cluster -> matched clusters in target cluster for every source cluster");
		logger.debug(Joiner.on("\n").withKeyValueSeparator("->").useForNull("null").join(matchOfSourceInTarget));
		
		int sourceClusterRemovalCount=0 ;
		Set<ConcernCluster> removedSourceClusters = new HashSet<ConcernCluster> () ;
		
		//Pooyan! unmatched clusters must be removed
		for (ConcernCluster source:sourceClusters){
			ConcernCluster matched = matchOfSourceInTarget.get(source);
			if (matched.getName().equals("-1")){
				sourceClusterRemovalCount++;
				removedSourceClusters.add(source);
			}
		}
		logger.debug("Pooyan -> Removed source clusters:");
		logger.debug(Joiner.on(",").join(removedSourceClusters));
		
		Set<String> entitiesToMoveInRemovedSourceClusters = new HashSet<String>(); // Pooyan! These are the entities in the removed source clusters which exists in the target clusters and have to be moved 
		logger.debug("Entities of removed clusters:");
		for (ConcernCluster source : removedSourceClusters) {
			Set<String> entities = source.getEntities();
			entities.removeAll(entitiesToRemove); // Make sure we are not trying to move entities that no longer exist in the target cluster
			logger.debug("Pooyan -> these in enitities in: "+ source.getName() + " will be moved: " + entities);
			entitiesToMoveInRemovedSourceClusters.addAll(entities);
		}
		
		// The clusters that remain after removal of clusters 
		Set<ConcernCluster> remainingSourceClusters = new HashSet<ConcernCluster>(sourceClusters);
		remainingSourceClusters.removeAll(removedSourceClusters);
		
		// for each cluster, the map gives the set of entities that may be moved (not including added or
		// removed entities)
		Map<ConcernCluster,Set<String>> entitiesToMoveInCluster = new HashMap<ConcernCluster,Set<String>>(); 
		for (ConcernCluster remainingCluster : remainingSourceClusters) {
			Set<String> matchedIntersectionEntities = sourceClusterMatchEntities.get(remainingCluster);
			Set<String> currEntitiesToMove = new HashSet<String>( remainingCluster.getEntities() );
			if (matchOfSourceInTarget.get(remainingCluster) != null && matchOfTargetInSource.get(matchOfSourceInTarget.get(remainingCluster)).equals(remainingCluster))// Pooyan! if the ramaining cluster is  the base cluster, it is entity should not be removed, otherwise they should be in the current entity to move
				currEntitiesToMove.removeAll(matchedIntersectionEntities); // the problem is here!!! It should move the maxIntersecting Entities since the cluster in the other arc is assigned to another cluster
			else{
				//logger.debug("Pooyan -> /*");
				//logger.debug("Pooyan -> remainingCluster: "+remainingCluster.getName());
				//logger.debug("Pooyan -> clusterToMaxIntersectingCluster.get(remainingCluster): " +clusterToMaxIntersectingCluster.get(remainingCluster).getName());
				//logger.debug("Pooyan -> targetClusterMatchInSource.get(clusterToMaxIntersectingCluster.get(remainingCluster)): "+targetClusterMatchInSource.get(clusterToMaxIntersectingCluster.get(remainingCluster)));
			}
			
			currEntitiesToMove.removeAll(entitiesToAdd);
			currEntitiesToMove.removeAll(entitiesToRemove);
			entitiesToMoveInCluster.put(remainingCluster,currEntitiesToMove);
			for (String e:currEntitiesToMove)
				logger.debug("Pooyan -> remaining cluster " + remainingCluster.getName() + ", adn current entity to move :" +e);
		}
		
		Set<String> allEntitiesToMove = new HashSet<String>();
		for (Set<String> currEntitiesToMove : entitiesToMoveInCluster.values()) {
			allEntitiesToMove.addAll(currEntitiesToMove); // entities to move in clusters not removed
		}
		allEntitiesToMove.addAll(entitiesToMoveInRemovedSourceClusters); // entities to move in removed clusters (i.e., all the entities in those clusters)
		allEntitiesToMove.addAll(entitiesToAdd);
		allEntitiesToMove.addAll(entitiesToRemove);
		
		for (String e:allEntitiesToMove)
			logger.debug("Pooyan -> enitity to be moved: " +e); 
		logger.debug("entities to move in each cluster: ");
		logger.debug(Joiner.on("\n").withKeyValueSeparator("->").join(entitiesToMoveInCluster));
		
		int movesForAddedEntities = entitiesToAdd.size();
		int movesForRemovedEntities = entitiesToRemove.size();
		
		logger.debug("\n");
		logger.debug("moves for added entities: " + movesForAddedEntities);
		logger.debug("moves for removed entities: " + movesForRemovedEntities);
		
		
		// Don't think I need this block for actual sysevo computation
		Map<String,ConcernCluster> entityToTargetCluster = new HashMap<String,ConcernCluster>(); 		
		for (ConcernCluster sourceCluster : sourceClusters) {
			Set<String> sourceEntitiesToMove = new HashSet<String>( sourceCluster.getEntities() ); // entities that exist already and might be moved
			sourceEntitiesToMove.removeAll(entitiesToAdd); // so you need to ignore added entities
			sourceEntitiesToMove.removeAll(entitiesToRemove); // and removed entities.
			for (ConcernCluster targetCluster : targetClusters) {
				Set<String> currTargetEntitites = targetCluster.getEntities();
				Set<String> intersectingEntities = new HashSet<String>(sourceEntitiesToMove); // entities in both the current source and target cluster
				intersectingEntities.retainAll(currTargetEntitites);
				logger.debug("intersecting entities: ");
				logger.debug(intersectingEntities);
				for (String entity : intersectingEntities) { // mark that these source entities belong to this target cluster
					entityToTargetCluster.put(entity,targetCluster);
				}
			}
		}
		
		logger.debug("Pooyan -> numClustersToRemove: " +numClustersToRemove);
		logger.debug("Pooyan -> numClustersToAdd: " + numClustersToAdd);
		logger.debug("Pooyan -> entitiesToRemove.size(): " + entitiesToRemove.size());
		logger.debug("Pooyan -> entitiesToAdd.size(): " + entitiesToAdd.size() ) ;
		logger.debug("Pooyan -> allEntitiesToMove.size(): " + allEntitiesToMove.size() );
		logger.debug("Show which target cluster each entity (not added or removed) belongs to");
		
		logger.debug(Joiner.on("\n").withKeyValueSeparator("->").join(entityToTargetCluster));
			
		double numer = numClustersToRemove + numClustersToAdd + (double)( entitiesToRemove.size() ) + (double)( entitiesToAdd.size() ) + (double)( allEntitiesToMove.size() );
		double denom = (double)( sourceClusters.size() ) + 2*(double)( sourceEntities.size() ) + (double)( targetClusters.size() ) + 2*(double)( targetEntities.size() );
		logger.debug("Pooyan -> denum: " + denom);
		
		double localSysEvo = (1-numer/denom)*100;
		
		logger.debug("sysevo: " + localSysEvo);
		
		sysEvo = localSysEvo;
			
	}
	
	
	private static Map<ConcernCluster,Set<String>> entriesSortedByEntitiesSize(Map<ConcernCluster,Set<String>> map) {
		List<Entry<ConcernCluster,Set<String>>> list = new ArrayList<Entry<ConcernCluster,Set<String>>>(map.entrySet());
		
		// sort list based on comparator
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                //return ((Comparable) ((Map.Entry) (o1)).getValue())
                //                      .compareTo(((Map.Entry) (o2)).getValue());
            	Entry<ConcernCluster,Set<String>> e1 = (Entry<ConcernCluster,Set<String>>)o1;
            	Entry<ConcernCluster,Set<String>> e2 = (Entry<ConcernCluster,Set<String>>)o2;
            	return e1.getValue().size() - e2.getValue().size(); // ascending order
            }
        });
        
        Map<ConcernCluster,Set<String>> sortedMap = new LinkedHashMap<ConcernCluster,Set<String>>();
        for (Entry<ConcernCluster,Set<String>> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
	}

	private static String clustersToString(Set<ConcernCluster> sourceClusters) {
		String output = "";
		for (ConcernCluster cluster : sourceClusters) {
			output += cluster.getName() +  ": ";
			for (String entity : cluster.getEntities()) {
				output += entity + " ";
			}
			output += "\n";
		}
		return output;
	}

	private static Set<String> getAllEntitiesInClusters(
			Set<ConcernCluster> clusters) {
		Set<String> entities = new HashSet<String>();
		for (ConcernCluster cluster : clusters) {
			entities.addAll( cluster.getEntities() );
		}
		return entities;
	}

}