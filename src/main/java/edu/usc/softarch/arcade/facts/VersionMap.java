package edu.usc.softarch.arcade.facts;

import edu.usc.softarch.arcade.util.FileUtil;
import edu.usc.softarch.arcade.util.Version;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class VersionMap extends HashMap<String, File> {
	//region CONSTRUCTORS
	public VersionMap(String filesDir) throws IOException {
		this(filesDir, null);
	}

	/**
	 * This constructor defaults to RSF type files.
	 */
	public VersionMap(String filesDir, String versionScheme)
			throws IOException {
		this(filesDir, versionScheme, ".rsf");
	}

	public VersionMap(String filesDir, String versionScheme, String extension)
		throws IOException {
		this.putAll(initializeVersionMap(filesDir, versionScheme, extension));
	}

	private Map<String, File> initializeVersionMap(String clusterDirPath,
			String versionScheme, String extension) throws FileNotFoundException {
		Map<String, File> versionMapLoader = new HashMap<>();
		Collection<File> files = FileUtil.getFileListing(new File(clusterDirPath));

		for (File file : files) {
			String fileName = file.getName();
			if (!fileName.contains(extension)) continue;

			String version = (new Version(fileName)).versionValue;

			if (version == null)
				throw new IllegalArgumentException("Could not match version scheme "
					+ versionScheme + " in the name of file " + fileName);

			versionMapLoader.put(version, file);
		}

		return versionMapLoader;
	}
	//endregion
}
