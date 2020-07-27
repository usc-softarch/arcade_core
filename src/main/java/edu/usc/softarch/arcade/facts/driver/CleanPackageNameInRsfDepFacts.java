package edu.usc.softarch.arcade.facts.driver;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CleanPackageNameInRsfDepFacts {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String depsFilename = args[0];
		String cleanDepsFilename = args[1];
		String stripBeforePackageName = args[2];
		
		RsfReader.loadRsfDataFromFile(depsFilename);
		List<List<String>> depFacts = RsfReader.unfilteredFacts;
		
		
		try {
			FileWriter out = new FileWriter(cleanDepsFilename);
			for (List<String> fact : depFacts) {
				String rel = fact.get(0);
				String source = fact.get(1);
				String target = fact.get(2);
				String cleanSource = source;
				String cleanTarget = target;
				
				if (source.contains(stripBeforePackageName)) {
					cleanSource = source.substring(source.indexOf(stripBeforePackageName),source.length());
				}
				if (target.contains(stripBeforePackageName)) {
					cleanTarget = target.substring(target.indexOf(stripBeforePackageName),target.length());
				}
				
				out.write(rel + " " + cleanSource + " " + cleanTarget + "\n");
				
			}
			
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		

	}

}
