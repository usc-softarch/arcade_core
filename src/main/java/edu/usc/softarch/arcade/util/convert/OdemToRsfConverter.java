package edu.usc.softarch.arcade.util.convert;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import edu.usc.softarch.arcade.facts.driver.ODEMReader;
import edu.usc.softarch.extractors.cda.odem.DependsOn;
import edu.usc.softarch.extractors.cda.odem.Type;

public class OdemToRsfConverter {
	private static Logger logger = LogManager.getLogger(OdemToRsfConverter.class);
	
	public static void main(String[] args) {
		String odemFileStr = args[0];
		String rsfFileStr = args[1];
	    
	  ODEMReader.setTypesFromODEMFile(odemFileStr);
		List<Type> allTypes = ODEMReader.getAllTypes();
		HashMap<String,Type> typeMap = new HashMap<>();
		for (Type t : allTypes)
			typeMap.put(t.getName().trim(), t);
		
		String convertMsg = "Writing dependencies from ODEM file to RSF file...";
		System.out.println(convertMsg);
		logger.debug(convertMsg);
		File rsfFile = new File(rsfFileStr);
		if (!rsfFile.getParentFile().exists())
			rsfFile.getParentFile().mkdirs();
		try (BufferedWriter out = new BufferedWriter(new FileWriter(rsfFileStr))) {
			for (String typeKey : typeMap.keySet()) {
				Type t = typeMap.get(typeKey);
				for (DependsOn dependency : t.getDependencies().getDependsOn()) {
					String rsfLine = dependency.getClassification() + " "
							+ t.getName() + " " + dependency.getName();
					logger.debug(rsfLine);
					out.write(rsfLine + "\n");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}