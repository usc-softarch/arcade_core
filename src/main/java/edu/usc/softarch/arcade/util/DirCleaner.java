package edu.usc.softarch.arcade.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Utility to delete test directories from Java subjects.
 */
public class DirCleaner {
	public static void main(String[] args) throws IOException {
		Queue<File> files = new LinkedList<>();
		files.add(new File(args[0]));
		boolean safeMode = Boolean.parseBoolean(args[2]);

		try (FileWriter writer = new FileWriter(args[1])) {
			while (!files.isEmpty()) {
				File currFile = files.poll();
				if (currFile.isFile()) continue;
				if (currFile.getName().contains("test")) {
					writer.write(currFile.getAbsolutePath() + System.lineSeparator());
					if (!safeMode)
						FileUtil.deleteNonEmptyDirectory(currFile);
				}
				else
					files.addAll(Arrays.asList(currFile.listFiles()));
			}
		}
	}
}
