package edu.usc.softarch.arcade.facts.driver;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * @author joshua
 */
public class CleanAuthRsfWithRelFacts {
	private static Logger logger = Logger.getLogger(CleanAuthRsfWithRelFacts.class);

	public static void main(String[] args) {
		PropertyConfigurator.configure("cfg" + File.separator + "extractor_logging.cfg");
		
		String authRsfFilename = args[0];
		String depsRsfFilename = args[1];
		String outputFilename = args[2];
		
		RsfReader.loadRsfDataFromFile(authRsfFilename);
		List<List<String>> authFacts = Lists.newArrayList(RsfReader.filteredRoutineFacts);
		System.out.println("number of facts in flat authoritative recovery: " + authFacts.size());

		RsfReader.loadRsfDataFromFile(depsRsfFilename);
		Set<String> allRelFactNodesSet = new HashSet<>(RsfReader.allNodesSet);
		
		List<List<String>> factsToRemove = new ArrayList<>();
		
		for (List<String> fact : authFacts) { // for each fact in the flat authoritative recovery
			String target = fact.get(2);
			if (!allRelFactNodesSet.contains(target)) { // if the target of the fact is not in the relation facts nodes set
				factsToRemove.add(fact); // mark the fact for removal
			}
			
		}
		System.out.println("number of facts to remove: " + factsToRemove.size());
		logger.debug("Facts to remove:");
		logger.debug(Joiner.on("\n").join(factsToRemove));
		
		Set<List<String>> cleanAuthFacts = Sets.newHashSet(authFacts);
		for (List<String> factToRemove : factsToRemove) {
			cleanAuthFacts.remove(factToRemove);
		}
		System.out.println("number of facts in clean flat authoritative recovery: " + cleanAuthFacts.size());
		
		try (FileWriter fw = new FileWriter(outputFilename)) {
			BufferedWriter out = new BufferedWriter(fw);
			System.out.println("Writing to file " + outputFilename + "...");
			for (List<String> fact : cleanAuthFacts) {
				out.write(fact.get(0) + " " + fact.get(1) + " " + fact.get(2) + "\n");
			}
			out.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}