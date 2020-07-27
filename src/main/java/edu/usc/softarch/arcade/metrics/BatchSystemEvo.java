package edu.usc.softarch.arcade.metrics;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import mojo.MoJoCalculator;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

import edu.usc.softarch.arcade.config.Config;
import edu.usc.softarch.arcade.util.FileListing;
import edu.usc.softarch.arcade.util.FileUtil;

public class BatchSystemEvo {
	static Logger logger = Logger.getLogger(BatchSystemEvo.class);
	
	public static void main(String[] args) throws FileNotFoundException {
		PropertyConfigurator.configure(Config.getLoggingConfigFilename());

		BatchSystemEvoOptions options = new BatchSystemEvoOptions();
		JCommander jcmd = new JCommander(options);

		try {
			jcmd.parse(args);
		} catch (ParameterException e) {
			logger.debug(e.getMessage());
			jcmd.usage();
			System.exit(1);
		}

		logger.debug(options.parameters);
		logger.debug("\n");
		
		// File containing only containing recovered architectures stored as rsf files
		String clusterFilesDir = options.parameters.get(0);

		List<File> clusterFiles = FileListing.getFileListing(new File(FileUtil
				.tildeExpandPath(clusterFilesDir)));
		// FileUtil.sortFileListByVersion sorts the list by the versioning
		// scheme found in the filename
		clusterFiles = FileUtil.sortFileListByVersion(clusterFiles);

		if (options.distopt == 1) {
			compareOverDistanceOfOne(clusterFiles);
		}
		else if (options.distopt == 2) {
			compareWithVdistGt1ForAll(clusterFiles);
		}
		else if (options.distopt == 3) {
			compareWithVdistGt1ForSubset(clusterFiles);
		}
		else {
			throw new RuntimeException("Unknown value for option distopt: " + options.distopt);
		}

	}
	
	private static void compareOverDistanceOfOne(
			List<File> clusterFiles) {
			File prevFile = null;
			List<Double> sysEvoValues = new ArrayList<Double>();
			int comparisonDistance = 1;
			System.out.println("Comparison distance is: " + comparisonDistance);
			for (int i = 0; i < clusterFiles.size(); i += comparisonDistance) {
				// System.out.println("i: " + i);
				File currFile = clusterFiles.get(i);
				// exclude annoying .ds_store files from OSX
				if (!currFile.getName().equals(".DS_Store")) {
					if (prevFile != null && currFile != null) {
						double sysEvoValue = computeSysEvo(prevFile, currFile);
						sysEvoValues.add(sysEvoValue);
					}
					prevFile = currFile;
				}
			}
			Double[] sysEvoArr = new Double[sysEvoValues.size()];
			sysEvoValues.toArray(sysEvoArr);
			
			DescriptiveStatistics stats = new DescriptiveStatistics(ArrayUtils.toPrimitive(sysEvoArr));

			/*System.out.println("N: " + stats.getN());
			System.out.println("max: " + stats.getMax());
			System.out.println("min: " + stats.getMin());
			System.out.println("mean: " + stats.getMean());*/
			System.out.println(stats);
			
			
			System.out.println();
	}

	private static void compareWithVdistGt1ForAll(
			List<File> clusterFiles) {
		//for (int comparisonDistance = 1; comparisonDistance < clusterFiles
		//		.size(); comparisonDistance++) {
			//File prevFile = null;
			//System.out.println("Comparison distance is: " + comparisonDistance);
			for (int i = 0; i < clusterFiles.size(); i++) {
				List<Double> sysEvoValues = new ArrayList<Double>();
				System.out.println("start index is: " + i);
				for (int j = i+1; j < clusterFiles.size(); j ++) {
					// System.out.println("i: " + i);
					File file1 = clusterFiles.get(i);
					File file2 = clusterFiles.get(j);
					// exclude annoying .ds_store files from OSX
					if (!file1.getName().equals(".DS_Store")) {
						//if (prevFile != null && file1 != null) {
						double sysEvoValue = computeSysEvo(file1,
								file2);
						sysEvoValues.add(sysEvoValue);
						//}
						//prevFile = file1;
					}
				}
				Double[] sysEvoArr = new Double[sysEvoValues.size()];
				sysEvoValues.toArray(sysEvoArr);

				DescriptiveStatistics stats = new DescriptiveStatistics(
						ArrayUtils.toPrimitive(sysEvoArr));

				/*
				 * System.out.println("N: " + stats.getN());
				 * System.out.println("max: " + stats.getMax());
				 * System.out.println("min: " + stats.getMin());
				 * System.out.println("mean: " + stats.getMean());
				 */
				System.out.println(stats);

				System.out.println();
				
				//if (comparisonDistance == 1)
				//	break;
			}
		//}
	}
	
	private static void compareWithVdistGt1ForSubset(
			List<File> clusterFiles) {
		for (int comparisonDistance = 1; comparisonDistance < clusterFiles
				.size(); comparisonDistance++) {
			File prevFile = null;
			List<Double> sysEvoValues = new ArrayList<Double>();
			System.out.println("Comparison distance is: " + comparisonDistance);
			for (int i = 0; i < clusterFiles.size(); i += comparisonDistance) {
				// System.out.println("i: " + i);
				File currFile = clusterFiles.get(i);
				// exclude annoying .ds_store files from OSX
				if (!currFile.getName().equals(".DS_Store")) {
					if (prevFile != null && currFile != null) {
						double sysEvoValue = computeSysEvo(prevFile, currFile);
						sysEvoValues.add(sysEvoValue);
					}
					prevFile = currFile;
				}
			}
			Double[] sysEvoArr = new Double[sysEvoValues.size()];
			sysEvoValues.toArray(sysEvoArr);
			
			DescriptiveStatistics stats = new DescriptiveStatistics(ArrayUtils.toPrimitive(sysEvoArr));

			/*System.out.println("N: " + stats.getN());
			System.out.println("max: " + stats.getMax());
			System.out.println("min: " + stats.getMin());
			System.out.println("mean: " + stats.getMean());*/
			System.out.println(stats);
			
			
			System.out.println();
		}
	}

	public static double computeSysEvo(File prevFile, File currFile) {
		String[] sysEvoArgs = {prevFile.getAbsolutePath(),currFile.getAbsolutePath()};
		SystemEvo.main(sysEvoArgs);
		double sysEvoValue = SystemEvo.sysEvo;
		System.out.println("SysEvo from " + prevFile.getName()
				+ " to " + currFile.getName() + ": " + sysEvoValue);
		return sysEvoValue;
	}

}
