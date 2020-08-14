package edu.usc.softarch.arcade.facts.driver;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class DepsRsfToBunchMdg {
	public static void main(String[] args) {
		String inDepsFilename = args[0];
		String outBunchDepsFilename = args[1];
		
		RsfReader.loadRsfDataFromFile(inDepsFilename);
		List<List<String>> depFacts = RsfReader.unfilteredFacts;
		
		try (FileWriter out = new FileWriter(new File(outBunchDepsFilename))) {
			for (List<String> depFact : depFacts) {
				String source = depFact.get(1);
				String target = depFact.get(2);
				
				out.write(source + " " + target + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
