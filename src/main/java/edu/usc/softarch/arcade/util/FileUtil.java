package edu.usc.softarch.arcade.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FileUtil {
	private static Logger logger = LogManager.getLogger(FileUtil.class);

	/**
	 * Extracts the name of a file without its extension.
	 * 
	 * @param filename Relative or absolute path to file.
	 * @return File's name without extension.
	 */
	public static String extractFilenamePrefix(String filename) {
		return filename.substring(filename.lastIndexOf(File.separatorChar)+1,
			filename.lastIndexOf("."));
	}

	/**
	 * Extracts the name of a file without its extension.
	 * 
	 * @param theFile Name or path to desired file's prefix.
	 */
	public static String extractFilenamePrefix(final File theFile) {
		logger.entry(theFile);
		logger.traceExit();
		return FilenameUtils.getBaseName(theFile.getName());
	}

	/**
	 * Extracts the extension of a given file.
	 * 
	 * @param filename Relative or absolute path to file.
	 * @return File's extension.
	 */
	public static String extractFilenameSuffix(String filename) {
		return filename.substring(filename.lastIndexOf("."),filename.length());
	}

	/**
	 * Extracts the extension of a given file.
	 * 
	 * @param theFile Name or path to desired file's extension.
	 */
	public static String extractFilenameSuffix(final File theFile) {
		logger.entry(theFile);
		logger.traceExit();
		return FilenameUtils.getExtension(theFile.getName());
	}

	/**
	 * Returns contents of a file as a String.
	 * 
	 * @param path Path of the desired file.
	 * @param encoding Encoding of the file, used to parse its contents.
	 * @return The contents of the file.
	 * @throws IOException If file cannot be read.
	 */
	public static String readFile(String path, Charset encoding)
			throws IOException { //TODO If possible, fail on incorrect encoding
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return encoding.decode(ByteBuffer.wrap(encoded)).toString();
	}

	/**
	 * Finds the first package name in a file.
	 * 
	 * @param filename Path to desired file.
	 * @return First package name found in the file.
	 * @throws IOException If file cannot be read.
	 */
	public static String getPackageNameFromJavaFile(String filename)
			throws IOException {
		String packageName = null;
		try (BufferedReader reader 
				= new BufferedReader(new FileReader(filename))) {
			String line = reader.readLine();
			if(line != null)
				packageName = findPackageName(line);
		}
		return packageName;
	}

	/**
	 * Extracts the name of a java code's package.
	 * 
	 * @param entry String from which to extract the package name.
	 * @return Code's package name.
	 */
	public static String findPackageName(String entry) {
		Pattern pattern = Pattern.compile("\\s*package\\s+(.+)\\s*;\\s*");
		Matcher matcher = pattern.matcher(entry);
		if (matcher.find()) return matcher.group(1).trim();
		return null;
	}

	/**
	 * Expands a path relative to home to an absolute path.
	 *
	 * @param path Path to expand.
	 */
	public static String tildeExpandPath(String path) {
		if (path.startsWith("~" + File.separator)) {
			path = System.getProperty("user.home") + path.substring(1);
		}
		return path;
	}

	/**
	 * Sorts files by version.
	 *
	 * @param inList List of Files to sort.
	 * @param reverse True for descending order, false for ascending.
	 */
	public static List<File> sortFileListByVersion(List<File> inList,
		boolean reverse) {
		List<File> outList = new ArrayList<File>(inList);

		Collections.sort(
			outList,
			(File o1, File o2) -> {
				String version1 = extractVersion(o1.getName());
				String[] parts1 = version1.split("\\.");
				String version2 = extractVersion(o2.getName());
				String[] parts2 = version2.split("\\.");

				// Number of version "parts" (separated by ".") of the
				// simpler version.
				int minLength =
					parts1.length > parts2.length 
					? parts2.length : parts1.length;

				// For each version part shared by both compared versions
				for (int i = 0; i < minLength; i++) {
					try {
						Integer part1 = Integer.parseInt(parts1[i]);
						Integer part2 = Integer.parseInt(parts2[i]);
						int compareToVal = part1.compareTo(part2);
						if (compareToVal != 0) {
							logger.debug("compareTo " + version1 + " to "
								+ version2 + ": " + compareToVal);
							return compareToVal;
						}
					} catch (NumberFormatException e) {
						logger.debug("Invalid part using string comparison for "
							+ version1 + " to " + version2 + ": "
							+ version1.compareTo(version2));
						//TODO Check if this can't replace the rest of this try/catch
						return version1.compareTo(version2);
					}
				}
				//TODO Check if this can't replace the rest of this try/catch
				return version1.compareTo(version2);
			});

		if(reverse) Collections.reverse(outList);
		return outList;
	}

	/**
	 * Sorts files by version in ascending order.
	 *
	 * @param inList List of Files to sort.
	 */
	public static List<File> sortFileListByVersion(List<File> inList) {
		return sortFileListByVersion(inList, false);
	}

	/**
	 * Check if a file exists.
	 *
	 * @param fileName The path of the file.
	 * @param create Create the file if it doesn't exist yet.
	 * @param exitOnNoExist Exit system if the file doesn't exist.
	 * @return
	 */
	public static File checkFile(final String fileName, final boolean create,
		final boolean exitOnNoExist) {

		logger.entry(fileName, create, exitOnNoExist);

		final File f = new File(fileName);

		// Check if file exists
		if (!f.exists()) {
			logger.trace(fileName + " does not exist");

			// Attempts to create file
			if (create) {
				logger.trace(" - making - ");
				try {
					if (f.createNewFile()) {
						logger.trace(" succeeded");
					} else {
						logger.trace(" failed ");
						logger.traceExit();
						System.out.println("### Could not create file: " + fileName);
						System.out.println("Exiting");
						System.exit(-1);
					}
				} catch (final IOException e) {
					logger.trace(" failed due to IOException");
					logger.traceExit();
					System.exit(-1);
				}
			}

			// Exits system if file could not be created
			else if (exitOnNoExist) {
				logger.trace(" - exiting");
				System.out.println("### File that must exist does not exist: " + fileName);
				System.out.println("Exiting");
				System.exit(-1);
			}
		}

		logger.traceExit();
		return f;
	}

	/**
	 * Check if a directory exists.
	 *
	 * @param dirName Path to the directory.
	 * @param create Whether the directory should be created if it doesn't exist.
	 * @param exitOnNoExist If true, system will exit if directory doesn't exist.
	 * @return The directory.
	 */
	public static File checkDir(final String dirName, final boolean create,
		final boolean exitOnNoExist) {
		//TODO Check if create should have priority over exitOnNoExist

		logger.entry(dirName, create, exitOnNoExist);
		final File f = new File(dirName);

		// Check if given file is a directory
		if (!f.isDirectory()) {
			logger.trace(dirName + " is not a directory - ");

			// Exit program if directory is required
			if (exitOnNoExist) {
				System.out.println("### Directory that must exist does not exist: "
					+ dirName);
				System.out.println("Exiting");
				logger.trace("exiting");
				logger.traceExit();
				System.exit(-1);
			}

			// Create directory if desired
			if (create) {
				logger.trace("making - ");
				if (f.mkdirs()) {
					logger.trace(" succeeded");
				} else {
					logger.trace(" failed");
					logger.traceExit();
					System.out.println("### Could not create directory: " + dirName);
					System.out.println("Exiting");
					System.exit(-1);
				}
			}
		}

		logger.traceExit();
		return f;
	}

	/**
	 * @param versionSchemeExpr Regex for version matching.
	 * @param filename Path or name of file from which to extract version.
	 */
	public static String extractVersionFromFilename(String versionSchemeExpr,
			String filename) {
		//TODO Refactor to be callable by extractVersionPretty()
		String version="";
		Pattern p = Pattern.compile(versionSchemeExpr);
		Matcher m = p.matcher(filename);
		if (m.find()) {
			version = m.group(0);
			logger.trace(version + " is the version of " + filename);
		}
		return version;
	}

	/**
	 * @param name Path or name of file from which to extract version.
	 */
	public static String extractVersion(String name) {
		//TODO Refactor to call extractVersionFromFilename()
		Pattern p = Pattern.compile("[0-9]+\\.[0-9]+(\\.[0-9]+)*");
		Matcher m = p.matcher(name);
		if (m.find()) {
			return m.group(0);
		}
		return null;
	}

	/**
	 * @param name Path or name of file from which to extract version.
	 */
	public static String extractVersionPretty(final String name) {
		//TODO Refactor to call extractVersionFromFilename()
		logger.entry(name);

		String patternString = "[0-9]+\\.[0-9]+(\\.[0-9]+)*+(-(RC|ALPHA|BETA|M" +
			"|Rc|Alpha|Beta|rc|alpha|beta|deb|b|a|final|Final|FINAL)[0-9]+)*";
		final Pattern p = Pattern.compile(patternString);
		final Matcher m = p.matcher(name);

		if (m.find()) {
			logger.traceExit();
			return m.group(0);
		}

		logger.traceExit();
		return null;
	}

	/**
	 * Makes a List by calling "toString" on every item of collection
	 */
	public static <E extends Object> List<String>
			collectionToString(Collection<E> collection) {
		return collection.stream().map(E::toString).collect(Collectors.toList());
	}

	/**
	 * Casts an Iterable into a List
	 */
	public static <E extends Object> List<E> iterableToCollection
			(Iterable<E> iterable) {
		List<E> toReturn = new ArrayList<>();
		iterable.forEach(toReturn::add);
		return toReturn;
	}
}
