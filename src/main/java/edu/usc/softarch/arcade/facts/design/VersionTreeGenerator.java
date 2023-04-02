package edu.usc.softarch.arcade.facts.design;

import edu.usc.softarch.arcade.util.FileUtil;
import edu.usc.softarch.arcade.util.Version;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VersionTreeGenerator {
	public static void main(String[] args) throws IOException {
		String dirPath = args[0];
		String outputPath = args[1];

		List<Version> versions = new ArrayList<>();
		List<File> archFiles = FileUtil.getFileListing(dirPath, ".rsf");

		for (File archFile : archFiles)
			versions.add(new Version(archFile.getName()));

		Collections.sort(versions);

		try (FileWriter writer = new FileWriter(outputPath)) {
			for (int i = 1; i < versions.size(); i++)
				writer.write("parent-of " + versions.get(i - 1)
					+ " " + versions.get(i) + System.lineSeparator());
		}
	}
}
