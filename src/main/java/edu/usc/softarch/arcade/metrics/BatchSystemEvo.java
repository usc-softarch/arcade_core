package edu.usc.softarch.arcade.metrics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

import edu.usc.softarch.arcade.util.FileListing;
import edu.usc.softarch.arcade.util.FileUtil;

public class BatchSystemEvo {
	private static Logger logger = LogManager.getLogger(BatchSystemEvo.class);
	private static DescriptiveStatistics stats;
	
	public static void main(String[] args) throws FileNotFoundException {
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

		String statistics_output_dir = options.parameters.get(1);
		

		if (options.distopt == 1) {
			compareOverDistanceOfOne(clusterFiles,statistics_output_dir);
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

	public static DescriptiveStatistics getStats(){
		return stats;
	}
	
	private static void compareOverDistanceOfOne(
			List<File> clusterFiles, String statistics_output_dir) {
			File prevFile = null;
			List<Double> sysEvoValues = new ArrayList<Double>();
			int comparisonDistance = 1;
			System.out.println("Comparison distance is: " + comparisonDistance);
			for (int i = 0; i < clusterFiles.size(); i += comparisonDistance) {
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
			
			stats = new DescriptiveStatistics(ArrayUtils.toPrimitive(sysEvoArr));

			System.out.println(stats);
			System.out.println();

	}

	private static void compareWithVdistGt1ForAll(
			List<File> clusterFiles) {
			for (int i = 0; i < clusterFiles.size(); i++) {
				List<Double> sysEvoValues = new ArrayList<>();
				System.out.println("start index is: " + i);
				for (int j = i+1; j < clusterFiles.size(); j ++) {
					File file1 = clusterFiles.get(i);
					File file2 = clusterFiles.get(j);
					// exclude annoying .ds_store files from OSX
					if (!file1.getName().equals(".DS_Store")) {
						double sysEvoValue = computeSysEvo(file1,
								file2);
						sysEvoValues.add(sysEvoValue);
					}
				}
				Double[] sysEvoArr = new Double[sysEvoValues.size()];
				sysEvoValues.toArray(sysEvoArr);

				DescriptiveStatistics stats = new DescriptiveStatistics(
						ArrayUtils.toPrimitive(sysEvoArr));

				System.out.println(stats);
				System.out.println();
			}
	}
	
	private static void compareWithVdistGt1ForSubset(
			List<File> clusterFiles) {
		for (int comparisonDistance = 1; comparisonDistance < clusterFiles
				.size(); comparisonDistance++) {
			File prevFile = null;
			List<Double> sysEvoValues = new ArrayList<>();
			System.out.println("Comparison distance is: " + comparisonDistance);
			for (int i = 0; i < clusterFiles.size(); i += comparisonDistance) {
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
