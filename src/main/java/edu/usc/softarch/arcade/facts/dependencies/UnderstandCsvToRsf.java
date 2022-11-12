package edu.usc.softarch.arcade.facts.dependencies;

import edu.usc.softarch.arcade.facts.DependencyGraph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;

public class UnderstandCsvToRsf {
	public static void main(String[] args) throws IOException {
		String csvPath = args[0];
		String rsfPath = args[1];
		String projectRootName = args[2];
		boolean fileLevel = Boolean.parseBoolean(args[3]);

		run(csvPath, rsfPath, projectRootName, fileLevel);
	}

	public static void run(String csvPath, String rsfPath,
			String projectRootName, boolean fileLevel) throws IOException {
		DependencyGraph result = new DependencyGraph();

		try (BufferedReader br = new BufferedReader(new FileReader(csvPath))) {
			String line = br.readLine(); // Skip header

			while ((line = br.readLine()) != null) {
				if (line.trim().isEmpty()) continue; // Skip empty lines

				String[] entry = line.split(",");
				String from = entry[0];
				String to = entry[1];
				if (!from.contains(projectRootName) || !to.contains(projectRootName))
					continue; // Skip unrelated dependencies

				if (fileLevel) {
					from = from.split(Pattern.quote(projectRootName + File.separator))[1];
					to = to.split(Pattern.quote(projectRootName + File.separator))[1];
				}
				result.add(from.replace("\"", ""),
					to.replace("\"", ""));
			}
		}

		File f = new File(rsfPath);
		f.getParentFile().mkdirs();

		result.writeToRsf(rsfPath);
	}
}
