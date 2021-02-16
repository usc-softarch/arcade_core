package edu.usc.softarch.arcade.metrics;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mojo.MoJoCalculator;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import edu.usc.softarch.arcade.util.FileListing;
import edu.usc.softarch.arcade.util.FileUtil;

public class MoJoEvolutionAnalyzer {
	public static void main(String[] args) throws FileNotFoundException {
		// File containing only containing recovered architectures stored as rsf files
		String clusterFilesDir = args[0];

		List<File> clusterFiles = FileListing.getFileListing(new File(FileUtil
				.tildeExpandPath(clusterFilesDir)));
		// FileUtil.sortFileListByVersion sorts the list by the versioning
		// scheme found in the filename
		clusterFiles = FileUtil.sortFileListByVersion(clusterFiles);

		for (int comparisonDistance = 1; comparisonDistance < clusterFiles
				.size(); comparisonDistance++) {
			File prevFile = null;
			List<Double> mojoFmValues = new ArrayList<>();
			System.out.println("Comparison distance is: " + comparisonDistance);
			for (int i = 0; i < clusterFiles.size(); i += comparisonDistance) {
				File currFile = clusterFiles.get(i);
				// exclude annoying .ds_store files from OSX
				if (!currFile.getName().equals(".DS_Store")) {
					if (prevFile != null && currFile != null) {
						double mojoFmValue = doMoJoFMComparison(currFile, prevFile);
						mojoFmValues.add(mojoFmValue);
					}
					prevFile = currFile;
				}
			}
			Double[] mojoFmArr = new Double[mojoFmValues.size()];
			mojoFmValues.toArray(mojoFmArr);
			
			DescriptiveStatistics stats = new DescriptiveStatistics(
				Arrays.stream(mojoFmArr).mapToDouble(Double::valueOf).toArray());

			System.out.println(stats);
			System.out.println();
		}
	}

	public static double doMoJoFMComparison(File prevFile, File currFile) {
		MoJoCalculator mojoCalc = new MoJoCalculator(
				prevFile.getAbsolutePath(),
				currFile.getAbsolutePath(), null);
		double mojoFmValue = mojoCalc.mojofm();
		System.out.println("MoJoFM from " + prevFile.getName()
				+ " to " + currFile.getName() + ": " + mojoFmValue);
		return mojoFmValue;
	}
}