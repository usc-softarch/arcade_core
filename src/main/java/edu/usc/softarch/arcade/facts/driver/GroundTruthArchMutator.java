package edu.usc.softarch.arcade.facts.driver;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import mojo.MoJoCalculator;
import edu.usc.softarch.arcade.config.Config;
import edu.usc.softarch.arcade.facts.ConcernCluster;
import edu.usc.softarch.arcade.util.FileUtil;

public class GroundTruthArchMutator {

	public static void main(String[] args) {
		String groundTruthFilename = args[0];
		String outputDir = args[1];
		Set<ConcernCluster> clusters = ConcernClusterRsf.extractConcernClustersFromRsfFile(groundTruthFilename);
		Set<String> allEntitiesSet = new LinkedHashSet<String>();
		List<String> allEntitiesList = new ArrayList<String>();
		
		
		for (ConcernCluster cluster : clusters) {
			allEntitiesList.addAll( cluster.getEntities() );
		}
		
		allEntitiesSet.addAll(allEntitiesList);
		allEntitiesList.clear();
		allEntitiesList.addAll(allEntitiesSet);
		
		
		int seedLimit = 10;
		for (int seed = 0; seed < seedLimit; seed++) {
			Random rand = new Random(seed);
			int tenPercentOfEntities = (int) Math.ceil(0.10 * allEntitiesList
					.size());
			List<String> selectedEntities = new ArrayList<String>();
			System.out.println("Randomly selected entitites:");
			for (int i = 0; i < tenPercentOfEntities; i++) {
				String selectedEntity = allEntitiesList.get(rand
						.nextInt(allEntitiesList.size()));
				selectedEntities.add(selectedEntity);
				System.out.println(selectedEntity);
			}

			for (String entity : selectedEntities) {
				ConcernCluster containingCluster = findContainingCluster(
						clusters, entity);
				assert containingCluster != null : "Obtained null cluster for "
						+ entity;

				ConcernCluster targetCluster = randomlySelectTargetCluster(
						clusters, containingCluster, seed);

				int containingClusterBeforeSize = containingCluster
						.getEntities().size();
				containingCluster.getEntities().remove(entity);
				assert containingCluster.getEntities().size() == containingClusterBeforeSize - 1;

				int targetClusterBeforeSize = targetCluster.getEntities()
						.size();
				targetCluster.getEntities().add(entity);
				assert targetCluster.getEntities().size() == targetClusterBeforeSize + 1;
			}

			writeMutatedClusterRsfFile(clusters, seed, groundTruthFilename,
					outputDir);
		}
		
		// obtain rsf files in output directory
		File outputDirFile = new File(outputDir);
		File[] newGtFiles = outputDirFile.listFiles(new FileFilter() {
			public boolean accept(File file) {
				return file.getName().endsWith(".rsf");
			}
		});

		String mojoFmMappingFilename = "mojofm_mapping.csv";
		try {
			PrintWriter writer = new PrintWriter(outputDir + File.separatorChar
					+ mojoFmMappingFilename, "UTF-8");
			for (File newGtFile : newGtFiles) {
				MoJoCalculator mojoCalc = new MoJoCalculator(
						newGtFile.getAbsolutePath(), groundTruthFilename, null);
				double mojoFmValue = mojoCalc.mojofm();
				System.out.println(mojoFmValue);

				writer.println(newGtFile.getAbsolutePath() + "," + mojoFmValue);

			}
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		
	}
	
	public static void writeMutatedClusterRsfFile(Set<ConcernCluster> clusters, long seed,
			String groundTruthFilename,String outputDir) {
		String suffix = FileUtil.extractFilenameSuffix(groundTruthFilename);
		String prefix = FileUtil.extractFilenamePrefix(groundTruthFilename);

		String newGroundTruthFilename = outputDir + File.separatorChar + prefix + "_" + seed + suffix;
		
		
		try {
			PrintWriter writer = new PrintWriter(newGroundTruthFilename,
					"UTF-8");
			for (ConcernCluster cluster : clusters) {
				for (String entity : cluster.getEntities()) {
					String line = "contain " + cluster.getName() + " " + entity;
					writer.println(line);
				}
			}
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private static ConcernCluster randomlySelectTargetCluster(
			Set<ConcernCluster> clusters, ConcernCluster containingCluster, long seed) {
		
			Set<ConcernCluster> copiedClusters = new HashSet<ConcernCluster>(clusters);
			copiedClusters.remove(containingCluster);
			
			List<ConcernCluster> reducedClustersList = new ArrayList<ConcernCluster>(copiedClusters);
			Random rand = new Random(seed);
			
			ConcernCluster targetCluster = reducedClustersList.get( rand.nextInt(reducedClustersList.size()) );
			assert targetCluster != null : "Obtained null cluster when randomly selecting target cluster";
			return targetCluster;
	}

	private static ConcernCluster findContainingCluster(
			Set<ConcernCluster> clusters, String inEntity) {
		for (ConcernCluster cluster : clusters) {
			for (String clusterEntity : cluster.getEntities() ) {
				if (clusterEntity.equals(inEntity)) {
					return cluster;
				}
			}
		}
		return null;
	}

}
