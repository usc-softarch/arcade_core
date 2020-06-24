package edu.usc.softarch.arcade.facts.driver;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.usc.softarch.arcade.util.Pair;

public class DepsRsfToBunchMdg {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String inDepsFilename = args[0];
		String outBunchDepsFilename = args[1];
		
		RsfReader.loadRsfDataFromFile(inDepsFilename);
		List<List<String>> depFacts = RsfReader.unfilteredFacts;
		
		try {
			FileWriter out = new FileWriter(new File(outBunchDepsFilename));
			
			Set<Pair<String,String>> mdgPairs = new HashSet<Pair<String,String>>();		
			for (List<String> depFact : depFacts) {
				String relType = depFact.get(0);
				String source = depFact.get(1);
				String target = depFact.get(2);
				//System.out.println(relType + " " + source + " " + target);
				
				out.write(source + " " + target + "\n");
				
			}
			
			out.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
