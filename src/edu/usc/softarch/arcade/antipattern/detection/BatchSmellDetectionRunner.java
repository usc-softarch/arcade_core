package edu.usc.softarch.arcade.antipattern.detection;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import mojo.MoJoCalculator;

import org.apache.log4j.PropertyConfigurator;

import edu.usc.softarch.arcade.config.Config;
import edu.usc.softarch.arcade.util.FileUtil;

public class BatchSmellDetectionRunner {

	public static void main(String[] args) {
		
		
		
		String smellClassesFilename = args[0];
		String gtRsfsDir = args[1];
		String docTopicsFile = args[2];
		String selectedLang = args[3];
		String depsRsfFilename = args[4];
		String techniquesDir = args[5];
		String groundTruthFilename = args[6];
		// obtain rsf files in output directory
		File gtRsfsDirFile = new File(gtRsfsDir);
		File[] newGtFiles = gtRsfsDirFile.listFiles(new FileFilter() {
			public boolean accept(File file) {
				return file.getName().endsWith(".rsf");
			}
		});

		PropertyConfigurator.configure(Config.getLoggingConfigFilename());

		Config.setMalletDocTopicsFilename(docTopicsFile);
		if (selectedLang.equals("c")) {
			Config.setSelectedLanguage(Config.Language.c);
		}
		else if (selectedLang.equals("java")) {
			Config.setSelectedLanguage(Config.Language.java);
		}
		Config.setDepsRsfFilename(depsRsfFilename);
		try {
			String mojoFmMappingFilename = "mojofm_mapping.csv";
			PrintWriter writer = new PrintWriter(techniquesDir
					+ File.separatorChar + mojoFmMappingFilename, "UTF-8");
			for (File gtRsfFile : newGtFiles) {
				Config.setSmellClustersFile(gtRsfFile.getAbsolutePath()); 
				String prefix = FileUtil.extractFilenamePrefix(gtRsfFile
						.getName());
				String detectedSmellsFilename = techniquesDir + prefix
						+ "_smells.ser";

				ArchSmellDetector.runAllDetectionAlgs(detectedSmellsFilename);

				MoJoCalculator mojoCalc = new MoJoCalculator(gtRsfFile.getAbsolutePath(),
						groundTruthFilename, null);
				double mojoFmValue = mojoCalc.mojofm();
				System.out.println(mojoFmValue);

				writer.println(detectedSmellsFilename + "," + mojoFmValue);

			}
			writer.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
