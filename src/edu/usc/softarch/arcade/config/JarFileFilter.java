package edu.usc.softarch.arcade.config;

import java.io.File;
import java.io.FileFilter;

/**
 * @author joshua
 *
 */
public class JarFileFilter implements FileFilter {

	public boolean accept(File f) {
		if (f.getAbsoluteFile().toString().endsWith(".jar") || f.isDirectory()) {
			return true;
		}
		return false;
	}
	
}