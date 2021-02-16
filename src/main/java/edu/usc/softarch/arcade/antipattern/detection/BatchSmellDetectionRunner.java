package edu.usc.softarch.arcade.antipattern.detection;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import mojo.MoJoCalculator;

import edu.usc.softarch.arcade.config.Config;
import edu.usc.softarch.arcade.topics.DocTopics;
import edu.usc.softarch.arcade.topics.TopicModelExtractionMethod;
import edu.usc.softarch.arcade.util.FileUtil;

public class BatchSmellDetectionRunner {

	public static void main(String[] args) throws IOException {
		String gtRsfsDir = args[1];
		String docTopicsFile = args[2];
		String selectedLang = args[3];
		String depsRsfFilename = args[4];
		String techniquesDir = args[5];
		String groundTruthFilename = args[6];
		// obtain rsf files in output directory
		File gtRsfsDirFile = new File(gtRsfsDir);
		File[] newGtFiles =
			gtRsfsDirFile.listFiles(file -> file.getName().endsWith(".rsf"));

		if (selectedLang.equals("c")) {
			Config.setSelectedLanguage(Config.Language.c);
		}
		else if (selectedLang.equals("java")) {
			Config.setSelectedLanguage(Config.Language.java);
		}
		try (PrintWriter writer = new PrintWriter(techniquesDir
				+ File.separatorChar + "mojofm_mapping.csv", "UTF-8")) {
			for (File gtRsfFile : newGtFiles) {
				String prefix = FileUtil.extractFilenamePrefix(gtRsfFile
						.getName());
				String detectedSmellsFilename = techniquesDir + prefix
						+ "_smells.ser";

				ArchSmellDetector asd = new ArchSmellDetector(depsRsfFilename, 
					gtRsfFile.getAbsolutePath(), detectedSmellsFilename, selectedLang,
					TopicModelExtractionMethod.VAR_MALLET_FILE,
					new DocTopics(docTopicsFile));
				asd.run(true, true, true);

				MoJoCalculator mojoCalc = new MoJoCalculator(gtRsfFile.getAbsolutePath(),
						groundTruthFilename, null);
				double mojoFmValue = mojoCalc.mojofm();
				System.out.println(mojoFmValue);

				writer.println(detectedSmellsFilename + "," + mojoFmValue);
			}
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
}