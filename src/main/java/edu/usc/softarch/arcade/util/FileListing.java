package edu.usc.softarch.arcade.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
* Recursive file listing under a specified directory. Modified from original.
*  
* @author javapractices.com
* @author Alex Wong
* @author anonymous user
*/
public final class FileListing {
  /**
  * Recursively walk a directory tree and return a List of all
  * Files found; the List is sorted using File.compareTo().
  *
  * @param aStartingDir is a valid directory, which can be read.
  */
  public static List<File> getFileListing(File aStartingDir)
			throws FileNotFoundException {
    validateDirectory(aStartingDir);
    List<File> result = getFileListingNoSort(aStartingDir, null);
    Collections.sort(result);
    return result;
  }
  
  /**
   * Recursively walk a directory tree and return a List of files matching the
	 * FilenameFilter; the List is sorted using File.compareTo().
   *
   * @param aStartingDir is a valid directory, which can be read.
	 * @param extension Which file extensions to consider.
   */
   public static List<File> getFileListing(File aStartingDir, String extension)
		 	throws FileNotFoundException {
     validateDirectory(aStartingDir);
     List<File> result = getFileListingNoSort(aStartingDir, extension);
     Collections.sort(result);
     return result;
   }

	/**
	 * Returns a list of all files and directories in the directory tree below
	 * the provided {@link File}. Traverses symbolic links and verifies that
	 * they are live.
	 *
	 * @param aStartingDir is a valid directory, which can be read.
	 * @param extension Which file extensions to consider.
	 */
  private static List<File> getFileListingNoSort(File aStartingDir,
			String extension) {
    List<File> result = new ArrayList<>();
    File[] filesAndDirs = aStartingDir.listFiles();

    for(File file : filesAndDirs) {
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
