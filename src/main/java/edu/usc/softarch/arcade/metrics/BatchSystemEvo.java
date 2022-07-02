package edu.usc.softarch.arcade.metrics;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import edu.usc.softarch.arcade.util.FileUtil;

public class BatchSystemEvo {
	//region PUBLIC INTERFACE
	public static void main(String[] args) throws FileNotFoundException {
		run(args[0]);	}

	public static DescriptiveStatistics run(String path)
			throws FileNotFoundException {
		List<File> clusterFiles = FileUtil.getFileListing(
			new File(FileUtil.tildeExpandPath(path)));
		clusterFiles = FileUtil.sortFileListByVersion(clusterFiles);

		System.out.println("Computing A2A with distance 1");
		DescriptiveStatistics result = compareOverDistanceOfOne(clusterFiles);
		System.out.println(result);

		return result;
	}
	//endregion

	//region PROCESSING
	private static DescriptiveStatistics compareOverDistanceOfOne(
			List<File> clusterFiles) {
		File prevFile = null;
		Collection<Double> sysEvoValues = new ArrayList<>();

		for (File currFile : clusterFiles) {
			if (prevFile != null) {
				double sysEvoValue = computeSysEvo(prevFile, currFile);
				sysEvoValues.add(sysEvoValue);
			}
			prevFile = currFile;
		}

		Double[] sysEvoArr = new Double[sysEvoValues.size()];
		sysEvoValues.toArray(sysEvoArr);

		return new DescriptiveStatistics(
			Stream.of(sysEvoArr).mapToDouble(Double::doubleValue).toArray());
	}

	public static double computeSysEvo(File prevFile, File currFile) {
		double sysEvoValue = SystemEvo.run(
			prevFile.getAbsolutePath(), currFile.getAbsolutePath());
		System.out.println("A2A from " + prevFile.getName()
				+ " to " + currFile.getName() + ": " + sysEvoValue);
		return sysEvoValue;
	}
	//endregion
}
