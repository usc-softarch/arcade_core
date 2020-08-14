package edu.usc.softarch.arcade.facts;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.PropertyConfigurator;

import com.google.common.collect.Lists;

import edu.usc.softarch.arcade.config.Config;
import edu.usc.softarch.arcade.facts.driver.RsfReader;

public class SymmetricRsfFactsTransformer {
	public static void main(String[] args) {
		PropertyConfigurator.configure(Config.getLoggingConfigFilename());
		
		String filename = "/home/joshua/recovery/RSFs/linuxRel.rsf";
		RsfReader.loadRsfDataFromFile(filename);
		
		List<List<String>> symmetricFacts = Lists.newArrayList(RsfReader.filteredRoutineFacts);
		
		for (List<String> fact : RsfReader.filteredRoutineFacts) {
			List<String> symmetricFact = new ArrayList<>();
			String type = fact.get(0);
			String source = fact.get(1);
			String target = fact.get(2);
			symmetricFact.add(type);
			symmetricFact.add(target);
			symmetricFact.add(source);
			symmetricFacts.add(symmetricFact);
		}
		
		String extension = filename.substring(filename.lastIndexOf("."),filename.length());
		String prefix = filename.substring(0,filename.lastIndexOf("."));
		
		String outputFilename = prefix + ".symmetric" + extension;
		
		try (BufferedWriter out 
				= new BufferedWriter(new FileWriter(outputFilename))) {
			for (List<String> fact : symmetricFacts)
				out.write(fact.get(0) + " " + fact.get(1) + " " + fact.get(2) + "\n");
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}