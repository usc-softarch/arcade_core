package edu.usc.softarch.arcade.antipattern.detection;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import edu.usc.softarch.arcade.antipattern.Smell;
import edu.usc.softarch.arcade.antipattern.SmellCollection;
import edu.usc.softarch.arcade.util.FileListing;
import edu.usc.softarch.arcade.util.FileUtil;

public class SmellEvolutionAnalyzer {
	private static Logger logger =
		LogManager.getLogger(SmellEvolutionAnalyzer.class);

	public static void main(String[] args) throws IOException {
		// inputDirFilename is the directory containing the .ser files which contain detected smells
		String inputDirFilename = args[0];
		
		List<File> fileList = FileListing.getFileListing(new File(FileUtil.tildeExpandPath(inputDirFilename)));
		fileList = FileUtil.sortFileListByVersion(fileList);
		List<File> orderedSerFiles = new ArrayList<>();
		for (File file : fileList)
			if (file.getName().endsWith(".ser"))
				orderedSerFiles.add(file);
		 		
		double[] smellCounts = new double[orderedSerFiles.size()];
		int idx = 0;
		for (File file : orderedSerFiles) {
			logger.debug(file.getName());
			SmellCollection smells = new SmellCollection(file.getAbsolutePath());
			logger.debug("\tcontains " + smells.size() + " smells");
			
			smellCounts[idx] = smells.size();
			idx++;
			
			logger.debug("\tListing detected smells for file" + file.getName() + ": ");
			for (Smell smell : smells) {
				logger.debug("\t" + smell.getSmellType() + " " + smell);
			}
			
			Pattern p = Pattern.compile("[0-9]+\\.[0-9]+(\\.[0-9]+)*");
			Matcher m = p.matcher(file.getName());
			if (m.find()) {
				System.out.println(m.group(0));
			}
		}
		
		DescriptiveStatistics stats = new DescriptiveStatistics(smellCounts);
		System.out.println();
		System.out.println(stats);
		logger.debug(stats);
	}
}