package edu.usc.softarch.arcade.facts.driver;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class BunchClusterToRsfClusterConverter {
	public static void main(String[] args) {
		String inBunchClusterFilename = args[0];
		String outClusterRsfFilename = args[1];
		
		try (BufferedWriter out = new BufferedWriter(new FileWriter(new File(outClusterRsfFilename)))) {
			Scanner scanner = new Scanner(new File(inBunchClusterFilename));
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				String[] clusterLineTokens = line.split("=");
				String clusterName = clusterLineTokens[0];
				clusterName = clusterName.replaceFirst("SS\\(", "").replace(")","");
				String filesOfCluster = clusterLineTokens[1];
				
				String[] filesTokens = filesOfCluster.split("\\s*,\\s*");
				for (String file : filesTokens) {
					String trimmedFile = file.trim();
					out.write("contain " + clusterName + " " + trimmedFile + "\n");
					
				}
				System.out.println(clusterName);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}