package edu.usc.softarch.arcade.facts;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import edu.usc.softarch.arcade.config.Config;
import edu.usc.softarch.arcade.facts.driver.RsfReader;
import edu.usc.softarch.arcade.topics.DocTopicItem;
import edu.usc.softarch.arcade.topics.TopicUtil;

public class ExpertDecompositionBuilder {
	static Logger logger = Logger.getLogger(ExpertDecompositionBuilder.class);
	public static ArrayList<ExpertDecomposition> expertDecompositions = null;
	
	public static void testFactFiltering() {
		PropertyConfigurator.configure(Config.getLoggingConfigFilename());
		
		Iterator<String> startNodesIterator = RsfReader.startNodesSet.iterator();
		
		logger.debug("Printing start nodes from shell.c...");
		
		while (startNodesIterator.hasNext()) {
			String startNodeStr = startNodesIterator.next();
			if (startNodeStr.contains("shell.c")) {
				logger.debug(startNodeStr);
			}
		}
	}
	
	public static void readInExpertDecomposition(String filename) {
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(filename));

			expertDecompositions = new ArrayList<ExpertDecomposition>();

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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private static void prettyLogExpertDecompositions(
			ArrayList<ExpertDecomposition> expertDecompositions) {
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

	public static void buildIntraPairsForExpertDecompositions() {
		int decompositionCounter = 1;
		for (ExpertDecomposition expertDecomposition : expertDecompositions) {
			logger.debug("Building intrapairs for expert decomposition " + decompositionCounter);
			for (Group group : expertDecomposition.groups) {
				for (String element1 : group.elements) {
					for (String element2 : group.elements) {
						HashSet<String> intraPair = new HashSet<String>();
						intraPair.add(element1);
						intraPair.add(element2);
						group.intraPairs.add(intraPair);
					}
				}
			}
			decompositionCounter++;
		}
		
		decompositionCounter = 1;
		for (ExpertDecomposition expertDecomposition : expertDecompositions) {
			logger.debug("Printing intrapairs for expert decomposition " + decompositionCounter);
			for (Group group : expertDecomposition.groups) {
				logger.debug("\t" + group);
				logger.debug("\t\t" + group.elements);
				for (HashSet<String> intraPair : group.intraPairs) {
					logger.debug("\t\t\t" + intraPair);
				}
			}
		}
		
		decompositionCounter = 1;
		for (ExpertDecomposition expertDecomposition : expertDecompositions) {
			logger.debug("Building all intrapairs for expert decomposition " + decompositionCounter);
			for (Group group : expertDecomposition.groups) {
				expertDecomposition.allIntraPairs.addAll(group.intraPairs);
			}
		}
		
		for (ExpertDecomposition expertDecomposition : expertDecompositions) {
			logger.debug("Printing all intrapairs for expert decomposition " + decompositionCounter);
			for (HashSet<String> intraPair : expertDecomposition.allIntraPairs) {
				logger.debug("\t" + intraPair);
			}
		}
		
	}

	public static void buildDocTopicsForClusters() {
		int decompositionCounter = 1;
		for (ExpertDecomposition expertDecomposition : expertDecompositions) {
			logger.debug("Printing intrapairs for expert decomposition " + decompositionCounter);
			for (Group group : expertDecomposition.groups) {
				logger.debug("group summary info:\n\t" + group);
				logger.debug("\t\tgroup elements: " + group.elements);
				DocTopicItem prevDocTopicItem = null;
				DocTopicItem mergedDocTopicItem = null;
				for (String element : group.elements) {
					if (TopicUtil.docTopics == null)
						TopicUtil.docTopics = TopicUtil.getDocTopicsFromFile();
						
					boolean docTopicDebug = false;
					DocTopicItem currDocTopicItem = TopicUtil.getDocTopicForString(TopicUtil.docTopics, element);
					if (prevDocTopicItem != null) {
						if (docTopicDebug)
							logger.debug("Merging " + prevDocTopicItem + " with " + currDocTopicItem);
						mergedDocTopicItem = TopicUtil.mergeDocTopicItems(prevDocTopicItem,currDocTopicItem);
					}
					else {
						if (docTopicDebug)
							logger.debug("Merging " + mergedDocTopicItem + " with " + currDocTopicItem);
						mergedDocTopicItem = TopicUtil.mergeDocTopicItems(mergedDocTopicItem,currDocTopicItem);
					}
				}
				group.docTopicItem = mergedDocTopicItem;
				if (group.docTopicItem != null)
					logger.debug("doc-topic for group:\n" + group.docTopicItem.toStringWithLeadingTabsAndLineBreaks(2));
				else 
					logger.debug("group has no doc-topics");
				
				TopicUtil.printDocTopicProportionSum(group.docTopicItem);
			}
		}
		
	}

	public static void buildMojoTargetFilesForFunctions() {
		int decompositionCounter = 1;
		for (ExpertDecomposition expertDecomposition : expertDecompositions) {
			logger.debug("Printing elements in the groups of expert decomposition " + decompositionCounter);
			int groupCount = 0;
			String mojoTargeFilename = Config.DATADIR + File.separator + Config.getCurrProjStr() + "_"
					+ "expert_decomposition_" + decompositionCounter + ".rsf";
			try {
				FileWriter fstream = new FileWriter(mojoTargeFilename);
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
			try {
				FileWriter fstream = new FileWriter(mojoTargeFilename);
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
		List<HashSet<String>> listOfElementsOfDecomp = new ArrayList<HashSet<String>>();
		for (ExpertDecomposition expertDecomposition : expertDecompositions) {
			HashSet<String> elementsOfDecomp = new HashSet<String>();
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
					Set<String> difference = new HashSet<String>(listOfElementsOfDecomp.get(i));
					difference.removeAll(listOfElementsOfDecomp.get(j));
					logger.debug(difference);					
				}
			}
			
		}
		
	}
}
