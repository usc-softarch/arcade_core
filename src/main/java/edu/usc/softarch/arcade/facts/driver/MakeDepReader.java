package edu.usc.softarch.arcade.facts.driver;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class MakeDepReader {
	private static Logger logger = LogManager.getLogger(MakeDepReader.class);
	
	public static void main(String[] args) throws FileNotFoundException {
		logger.debug("Running from " + MakeDepReader.class.getName());

		String filename = args[0];
		Scanner scanner = new Scanner(new FileInputStream(filename));
		Map<String,List<String>> depMap = new HashMap<>();
		String currDotCFile = "";
		
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			String[] tokens = line.split("\\s");
			
			
			for (String token : tokens) {
				String trimmedToken = token.trim();
				
				if (trimmedToken.endsWith(".c") || trimmedToken.endsWith(".h")) {
					trimmedToken = trimmedToken.substring(0,trimmedToken.length());
					List<String> deps = null;
					if (depMap.containsKey(currDotCFile)) {
						deps = depMap.get(currDotCFile);
					}
					else {
						deps = new ArrayList<>();
					}
					deps.add(trimmedToken);
					depMap.put(currDotCFile,deps);
				}
				if (trimmedToken.endsWith(".o:")) {
					trimmedToken = trimmedToken.substring(0,trimmedToken.length()-1);
					currDotCFile = trimmedToken.replace(".o",".c");
				}
				logger.debug(trimmedToken + "");
				
			}
			logger.debug("\n");
		}
		
		Set<String> cFiles = depMap.keySet();
		for (String cFile : cFiles) {
			logger.debug(cFile + " has dependencies to ");
			List<String> deps = depMap.get(cFile);
			for (String dep : deps) {
				logger.debug("\t" + dep);
			}
		}
		
		String outRsfFile = args[1];
		try (FileWriter fstream = new FileWriter(outRsfFile)) {
			BufferedWriter out = new BufferedWriter(fstream);
			for (String cFile : cFiles) {
				List<String> deps = depMap.get(cFile);
				for (String dep : deps) {
					out.write("depends " + cFile + " " + dep + "\n");
				}
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}