package edu.usc.softarch.arcade.facts;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import edu.usc.softarch.arcade.config.Config;

public class ExpertDecompositionBuilder {
	private static Logger logger =
		LogManager.getLogger(ExpertDecompositionBuilder.class);
	private static List<ExpertDecomposition> expertDecompositions = null;
	
	public static void readInExpertDecomposition(String filename) {
		try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
			expertDecompositions = new ArrayList<>();

			ExpertDecomposition currDecomposition = null;
			Group currGroup = null;
			String line = null;
			while ((line = reader.readLine()) != null) {

				if (currDecomposition != null
						&& line.toLowerCase().contains("expert decomp")) {
					currDecomposition.groups.add(currGroup);
					expertDecompositions.add(currDecomposition);
					currDecomposition = new ExpertDecomposition();
					currGroup = null;
				} else if (currDecomposition == null
						&& line.toLowerCase().contains("expert decomp")) {
					currDecomposition = new ExpertDecomposition();
				}

				if (line.startsWith("*")) {
					if (currGroup != null) {
						currDecomposition.groups.add(currGroup);
					}
					currGroup = new Group();
					currGroup.name = line.substring(line.indexOf("*") + 1,
							line.length());
				}

				if (line.toLowerCase().trim().endsWith(".c")) {
					currGroup.files.add(line);
				}

			}
			
			if (currDecomposition != null) {
				currDecomposition.groups.add(currGroup);
				expertDecompositions.add(currDecomposition);
			}
			
			logger.debug("Printing expert decompositions...");
			prettyLogExpertDecompositions(expertDecompositions);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	private static void prettyLogExpertDecompositions(
			List<ExpertDecomposition> expertDecompositions) {
		int decompositionCounter = 1;
		for (ExpertDecomposition decomposition : expertDecompositions) {
			logger.debug("*** Expert Decomposition " + decompositionCounter + " ***");
			logger.debug(decomposition);
			decompositionCounter++;
		}
		
	}

	public static void buildExpertDecompositionClusters(
			Set<String> startNodesSet) {
		if (expertDecompositions == null) {
			System.err.println("ExpertDecompositionBuilder.expertDecompositions is null in " +  Thread.currentThread().getStackTrace()[2].getMethodName()  );
		}
		
		for (ExpertDecomposition expertDecomposition : expertDecompositions) {
			for (Group group : expertDecomposition.groups) {
				for (String file : group.files) {
					for (String startNode : startNodesSet) {
						if ( startNode.toLowerCase().contains(file.toLowerCase().trim()) ) {
							group.elements.add(startNode);
						}
					}
				}
			}
		}
		
		int decompositionCounter = 1;
		for (ExpertDecomposition expertDecomposition : expertDecompositions) {
			logger.debug("Printing elements in the groups of expert decomposition " + decompositionCounter);
			for (Group group : expertDecomposition.groups) {
				logger.debug("\t" + group);
				logger.debug("\t\t" + group.elements);
			}
			decompositionCounter++;
		}
	}

	public static void buildMojoTargetFilesForFunctions() {
		int decompositionCounter = 1;
		for (ExpertDecomposition expertDecomposition : expertDecompositions) {
			logger.debug("Printing elements in the groups of expert decomposition " + decompositionCounter);
			int groupCount = 0;
			String mojoTargeFilename = Config.DATADIR + File.separator + Config.getCurrProjStr() + "_"
					+ "expert_decomposition_" + decompositionCounter + ".rsf";
			try (FileWriter fstream = new FileWriter(mojoTargeFilename)) {
				BufferedWriter out = new BufferedWriter(fstream);

				for (Group group : expertDecomposition.groups) {
					logger.debug("\t" + group);
					logger.debug("\t\t" + group.elements);
					for (String element : group.elements) {
						out.write("contain " + groupCount + " " + element + "\n");
					}
					groupCount++;
				}
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			decompositionCounter++;
		}
	}
	
	public static void buildMojoTargetFilesForFiles() {
		int decompositionCounter = 1;
		for (ExpertDecomposition expertDecomposition : expertDecompositions) {
			logger.debug("Printing elements in the groups of expert decomposition " + decompositionCounter);
			int groupCount = 0;
			String mojoTargeFilename = Config.DATADIR + File.separator + Config.getCurrProjStr() + "_"
					+ "expert_decomposition_file_level_" + decompositionCounter + ".rsf";
			try (FileWriter fstream = new FileWriter(mojoTargeFilename)) {
				BufferedWriter out = new BufferedWriter(fstream);

				for (Group group : expertDecomposition.groups) {
					logger.debug("\t" + group);
					logger.debug("\t\t" + group.elements);
					for (String file : group.files) {
						out.write("contain " + groupCount + " " + file + "\n");
					}
					groupCount++;
				}
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			decompositionCounter++;
		}
	}

	public static void findMissingElementsInExpertDecomposition() {
		List<Set<String>> listOfElementsOfDecomp = new ArrayList<>();
		for (ExpertDecomposition expertDecomposition : expertDecompositions) {
			Set<String> elementsOfDecomp = new HashSet<>();
			for (Group group : expertDecomposition.groups) {
				for (String element : group.elements) {
					elementsOfDecomp.add(element);
				}
			}
			listOfElementsOfDecomp.add(elementsOfDecomp);
		}
		
		for (int i=0;i<listOfElementsOfDecomp.size();i++) {
			for (int j=0;j<listOfElementsOfDecomp.size();j++) {
				if (i!=j) {
					logger.debug("Different between expert decompositions " + i + "  and " + j);
					Set<String> difference = new HashSet<>(listOfElementsOfDecomp.get(i));
					difference.removeAll(listOfElementsOfDecomp.get(j));
					logger.debug(difference);					
				}
			}
			
		}
	}
}