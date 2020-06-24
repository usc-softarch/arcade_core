package edu.usc.softarch.arcade.util;

import java.util.*;
import java.io.*;
import java.nio.file.Files;

/**
* Recursive file listing under a specified directory.
*  
* @author javapractices.com
* @author Alex Wong
* @author anonymous user
*/
public final class FileListing {

  /**
  * Demonstrate use.
  * 
  * @param aArgs - <tt>aArgs[0]</tt> is the full name of an existing 
  * directory that can be read.
  */
  public static void main(String... aArgs) throws FileNotFoundException {
    File startingDirectory= new File(aArgs[0]);
    List<File> files = FileListing.getFileListing(startingDirectory);

    //print out all file names, in the the order of File.compareTo()
    for(File file : files ){
      System.out.println(file);
    }
  }
  
  /**
  * Recursively walk a directory tree and return a List of all
  * Files found; the List is sorted using File.compareTo().
  *
  * @param aStartingDir is a valid directory, which can be read.
  */
  static public List<File> getFileListing(
    File aStartingDir
  ) throws FileNotFoundException {
    validateDirectory(aStartingDir);
    List<File> result = getFileListingNoSort(aStartingDir,null);
    Collections.sort(result);
    return result;
  }
  
  /**
   * Recursively walk a directory tree and return a List of files matching the FilenameFilter; the List is sorted using File.compareTo(). 
   *
   * @param aStartingDir is a valid directory, which can be read.
   */
   static public List<File> getFileListing(
     File aStartingDir, String extension
   ) throws FileNotFoundException {
     validateDirectory(aStartingDir);
     List<File> result = getFileListingNoSort(aStartingDir,extension);
     Collections.sort(result);
     return result;
   }

  // PRIVATE //
  static private List<File> getFileListingNoSort(
    File aStartingDir, String extension
  ) throws FileNotFoundException {
    List<File> result = new ArrayList<File>();
    File[] filesAndDirs = aStartingDir.listFiles();
    List<File> filesDirs = Arrays.asList(filesAndDirs);
    for(File file : filesDirs) {
      if (extension == null) {
    	  try {
			if (Files.isSymbolicLink(file.toPath())) { // if the file is a symbolic link
				  if (file.getCanonicalFile().exists()) { // check if the file exists
					  result.add(file); //always add, even if directory
				  }
				  else {
					  // don't add it
				  }
			}
			else {
				result.add(file); //always add if not symbolic, even if directory
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	  
    	  
      }
      else if (file.getName().endsWith(extension)) {
    		  result.add(file);
      }
      if ( file.isDirectory() ) {
        //must be a directory
        //recursive call!
        List<File> deeperList = getFileListingNoSort(file,extension);
        result.addAll(deeperList);
      }
    }
    return result;
  }

  /**
  * Directory is valid if it exists, does not represent a file, and can be read.
  */
  static private void validateDirectory (
    File aDirectory
  ) throws FileNotFoundException {
    if (aDirectory == null) {
      throw new IllegalArgumentException("Directory should not be null.");
    }
    if (!aDirectory.exists()) {
      throw new FileNotFoundException("Directory does not exist: " + aDirectory);
    }
    if (!aDirectory.isDirectory()) {
      throw new IllegalArgumentException("Is not a directory: " + aDirectory);
    }
    if (!aDirectory.canRead()) {
      throw new IllegalArgumentException("Directory cannot be read: " + aDirectory);
    }
  }
} 

