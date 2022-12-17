package edu.usc.softarch.arcade.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Utility class for dealing with {@link File}s and filenames. Partly taken
 * from javapractices.com by Alex Wong.
 */
public class FileUtil {
	/**
	 * Extracts the name of a file without its extension.
	 * 
	 * @param filename Relative or absolute path to file.
	 * @return File's name without extension.
	 */
	public static String extractFilenamePrefix(String filename) {
		return filename.substring(filename.lastIndexOf(File.separatorChar) + 1,
			filename.lastIndexOf("."));
	}

	/**
	 * Extracts the name of a file without its extension.
	 * 
	 * @param theFile File from which to extract name.
	 */
	public static String extractFilenamePrefix(final File theFile) {
		return FileUtil.extractFilenamePrefix(theFile.getName()); }

	/**
	 * Extracts the extension of a given file.
	 * 
	 * @param filename Relative or absolute path to file.
	 * @return File's extension.
	 */
	public static String extractFilenameSuffix(String filename) {
		return filename.substring(filename.lastIndexOf("."));
	}

	/**
	 * Extracts the extension of a given file.
	 * 
	 * @param theFile File from which to extract extension.
	 */
	public static String extractFilenameSuffix(final File theFile) {
		return FileUtil.extractFilenameSuffix(theFile.getName());
	}

	/**
	 * Returns contents of a file as a String. Assumes encoding UTF 8.
	 *
	 * @param path Path of the desired file.
	 * @return The contents of the file.
	 * @throws IOException If file cannot be read.
	 */
	public static String readFile(String path) throws IOException {
		return readFile(path, StandardCharsets.UTF_8);
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
			throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return encoding.decode(ByteBuffer.wrap(encoded)).toString();
	}

	/**
	 * Finds the first package name in a {@link File}. There generally should not
	 * be more than one package declaration, but since these are text files, it is
	 * possible there may be multiple. In that case, latter ones are discarded.
	 *
	 * @param file {@link File} from which to extract the package name.
	 * @return Code's package name, if the file declares one.
	 */
	public static String findPackageName(File file) throws IOException {
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			for (String line = reader.readLine();
					line != null; line = reader.readLine()) {
				String packageName = findPackageName(line);
				if (packageName != null) return packageName;
			}
		}
		return null;
	}

	/**
	 * Extracts the name of a java code's package from a {@link String}.
	 * 
	 * @param entry {@link String} from which to extract the package name.
	 * @return Code's package name, if the line declares one.
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
		if (path.startsWith("~" + File.separator))
			return System.getProperty("user.home") + path.substring(1);
		return path;
	}

	/**
	 * Sorts files by version. Is able to process any file with a name matching
	 * the pattern (\d+(\.\d+)?(\.\d+)?(alpha\d?|beta\d?|rc\d?)?).
	 *
	 * If there are multiple matches (i.e. there are multiple version strings in
	 * the name of the file), it will give an incorrect result because you
	 * shouldn't have two versions in your filename what are you even doing...
	 *
	 * @param inList List of Files to sort.
	 * @param reverse True for descending order, false for ascending.
	 * @see VersionComparator
	 */
	public static List<File> sortFileListByVersion(List<File> inList,
			boolean reverse) {
		List<File> outList = new ArrayList<>(inList);
		outList.sort(new VersionComparator());

		if (reverse) Collections.reverse(outList);
		return outList;
	}

	/**
	 * Sorts files by version in ascending order.
	 *
	 * @param inList List of Files to sort.
	 */
	public static List<File> sortFileListByVersion(List<File> inList) {
		return sortFileListByVersion(inList, false); }

	/**
	 * Check if a file exists.
	 *
	 * @param fileName The path of the file.
	 * @param create Create the file if it doesn't exist yet.
	 * @param exitOnNoExist Exit system if the file doesn't exist.
	 */
	public static File checkFile(final String fileName, final boolean create,
			final boolean exitOnNoExist) throws IOException {
		final File f = new File(fileName);

		if (!f.exists()) {
			if (create && !f.createNewFile())
				throw new IOException("File " + fileName + " already exists.");

			else if (exitOnNoExist)
				throw new FileNotFoundException("File " + fileName + " does not exist.");
		}

		return f;
	}

	/**
	 * Check if a directory exists. exitOnNoExist takes priority over create: if
	 * both are true and directory does not exist, it will not be created.
	 *
	 * @param dirName Path to the directory.
	 * @param create Whether the directory should be created if it doesn't exist.
	 * @param exitOnNoExist If true, system will exit if directory doesn't exist.
	 * @return The directory.
	 */
	public static File checkDir(final String dirName, final boolean create,
			final boolean exitOnNoExist) throws FileNotFoundException {
		final File f = new File(dirName);

		if (!f.isDirectory()) {
			if (exitOnNoExist)
				throw new FileNotFoundException("Directory " + dirName + " does not exist.");

			if (create && !f.mkdirs())
				throw new FileNotFoundException("Directory " + dirName + " could not be created.");
		}

		return f;
	}

	/**
	 * Extracts a version {@link String} from a {@link String} representing a
	 * file name.
	 *
	 * @param versionSchemeExpr Regex for version matching.
	 * @param filename {@link String} from which to extract version.
	 */
	public static String extractVersion(String versionSchemeExpr,
			String filename) {
		if (versionSchemeExpr == null) return extractVersion(filename);

		Pattern p = Pattern.compile(versionSchemeExpr);
		Matcher m = p.matcher(filename);

		if (m.find()) return m.group(0);
		return null;
	}

	/**
	 * Extracts a version {@link String} from a {@link String} representing a
	 * file name. Correctness is dubious, given that versioning schemes are
	 * arbitrary.
	 *
	 * @param name {@link String} from which to extract version.
	 */
	public static String extractVersion(String name) {
		return extractVersion("[0-9]+\\.[0-9]+(\\.[0-9]+)*", name); }

	/**
	 * Extracts a version {@link String} from a {@link String} representing a
	 * file name. Includes options for letters and other blasphemies. Correctness
	 * is dubious, given that versioning schemes are arbitrary.
	 *
	 * @param name {@link String} from which to extract version.
	 */
	public static String extractVersionForMonsters(String name) {
		return extractVersion("[0-9]+\\.[0-9]+(\\.[0-9]+)*+(-(RC|ALPHA|BETA" +
			"|M|Rc|Alpha|Beta|rc|alpha|beta|deb|b|a|final|Final|FINAL)[0-9]+)*",
			name);
	}

	/**
	 * Makes a List by calling "toString" on every item of a collection.
	 */
	public static <E> List<String> collectionToString(Collection<E> collection) {
		return collection.stream()
			.filter(Objects::nonNull)
			.map(E::toString)
			.collect(Collectors.toList());
	}

	/**
	 * If a {@link String} represents the name of a Java inner class, returns
	 * the name of the parent class.
	 */
	public static String cutInnerClass(String dir) {
		if (dir.contains("$")) return dir.split("$")[0];
		return dir;
	}

	/**
	 * Verifies that all input directories contain the specified classes
	 * directory, which is meant to contain binary files for analysis.
	 */
	public static boolean checkClassesDirs(File inputDir, String classesDirs) {
		String fs = File.separator;
		// List all files in inputDir
		List<File> versionDirectories = Arrays.asList(inputDir.listFiles());
		// Remove if not a directory
		versionDirectories.removeIf(file -> !file.isDirectory());

		for (File d : versionDirectories) {
			File currentClassesDir = new File(d.getAbsolutePath()
				+ fs + classesDirs);
			if (!currentClassesDir.isDirectory()) return false;
		}
		return true;
	}

	/**
	 * Recursively walk a directory tree and return a List of all
	 * Files found; the List is sorted using File.compareTo().
	 *
	 * @param dir is a valid directory, which can be read.
	 */
	public static List<File> getFileListing(File dir)
		throws FileNotFoundException {
		validateDirectory(dir);
		List<File> result = getFileListingNoSort(dir, null);
		Collections.sort(result);
		return result;
	}

	/**
	 * Recursively walk a directory tree and return a List of all
	 * Files found; the List is sorted using File.compareTo().
	 *
	 * @param dirPath is a valid path to a directory, which can be read.
	 */
	public static List<File> getFileListing(String dirPath)
			throws FileNotFoundException {
		return getFileListing(new File(dirPath));
	}

	/**
	 * Recursively walk a directory tree and return a List of files matching the
	 * FilenameFilter; the List is sorted using File.compareTo().
	 *
	 * @param dir is a valid directory, which can be read.
	 * @param extension Which file extensions to consider.
	 */
	public static List<File> getFileListing(File dir, String extension)
		throws FileNotFoundException {
		validateDirectory(dir);
		List<File> result = getFileListingNoSort(dir, extension);
		Collections.sort(result);
		return result;
	}

	/**
	 * Deletes a non-empty directory. Use with care.
	 */
	public static void deleteNonEmptyDirectory(File dir) {
		if (dir.isDirectory())
			for (File file : dir.listFiles())
				deleteNonEmptyDirectory(file);
		dir.delete();
	}

	/**
	 * Returns a list of all files and directories in the directory tree below
	 * the provided {@link File}. Traverses symbolic links and verifies that
	 * they are live.
	 *
	 * @param dir is a valid directory, which can be read.
	 * @param extension Which file extensions to consider.
	 */
	private static List<File> getFileListingNoSort(File dir, String extension) {
		List<File> result = new ArrayList<>();
		File[] filesAndDirs = dir.listFiles();

		for(File file : filesAndDirs) {
			// Catch to ignore the taint from evil Mac users
			if (file.getName().equals(".DS_Store")) continue;

			if (extension == null)
				result.addAll(getFile(file));
			else if (file.getName().endsWith(extension))
				result.add(file);

			if (file.isDirectory()) { // If it is a directory, recurse
				List<File> deeperList = getFileListingNoSort(file, extension);
				result.addAll(deeperList);
			}
		}

		return result;
	}

	/**
	 * Returns all files immediatelly under the given {@link File}, regardless
	 * of extension.
	 */
	private static List<File> getFile(File file) {
		List<File> result = new ArrayList<>();

		try {
			if (Files.isSymbolicLink(file.toPath())) { // if the file is a symlink
				if (file.getCanonicalFile().exists()) // check if the file exists
					result.add(file); //always add, even if directory
			}
			else
				result.add(file); //always add if not symlink, even if directory
		} catch (IOException e) {
			e.printStackTrace(); //TODO handle it
		}

		return result;
	}

	/**
	 * Directory is valid if it exists, does not represent a file, and can be read.
	 */
	private static boolean validateDirectory (File aDirectory)
		throws FileNotFoundException {
		if (aDirectory == null)
			throw new IllegalArgumentException("Directory should not be null.");

		if (!aDirectory.exists())
			throw new FileNotFoundException("Directory does not exist: " + aDirectory);

		if (!aDirectory.isDirectory())
			throw new IllegalArgumentException("Is not a directory: " + aDirectory);

		if (!aDirectory.canRead())
			throw new IllegalArgumentException("Directory cannot be read: " + aDirectory);

		return true;
	}
}
