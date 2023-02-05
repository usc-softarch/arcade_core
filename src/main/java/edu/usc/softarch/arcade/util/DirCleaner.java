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
		String[] ignorePatterns = new String[args.length - 3];
		if (args.length > 3)
			System.arraycopy(args, 3, ignorePatterns, 0, args.length - 3);

		try (FileWriter writer = new FileWriter(args[1])) {
			while (!files.isEmpty()) {
				File currFile = files.poll();
				if (currFile.isFile()) continue;
				if ((currFile.getName().contains("test")
						|| currFile.getName().contains("trunk"))
						&& !isIgnored(ignorePatterns, currFile)) {
					writer.write(currFile.getAbsolutePath() + System.lineSeparator());
					if (!safeMode)
						FileUtil.deleteNonEmptyDirectory(currFile);
				}
				else
					files.addAll(Arrays.asList(currFile.listFiles()));
			}
		}
	}

	private static boolean isIgnored(String[] ignorePatterns,
			File file) {
		for (String ignorePattern : ignorePatterns)
			if (file.getAbsolutePath().contains(ignorePattern))
				return true;
		return false;
	}
}
