package edu.usc.softarch.arcade.util.convert;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import edu.usc.softarch.arcade.facts.driver.RsfReader;

public class RsfToDotConverter {
	
	public static void main(String[] args) {
		String rsfFilename = args[0];
		String dotFilename = args[1];
		try {
			RsfReader.loadRsfDataFromFile(rsfFilename);
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		List<List<String>> facts = RsfReader.unfilteredFacts;
		
		try (FileWriter out = new FileWriter(dotFilename)) {
			out.write("digraph G {\n");
			
			for (List<String> fact : facts) {
				String source = fact.get(1);
				String target = fact.get(2);
				out.write("\t\"" + source + "\" -> \"" + target + "\";\n"); 
			}
			
			out.write("}\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
