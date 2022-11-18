package edu.usc.softarch.arcade.clustering;

import java.io.IOException;

/**
 * Utility to parse a set of RSF files to their DOT format.
 */
public class RsfToDot {
	public static void main(String[] args) throws IOException {
		String mode = args[0];
		String depsPath = args[1];
		String archPath = args[2];
		String outputPath = args[3];

		ReadOnlyArchitecture arch = ReadOnlyArchitecture.readFromRsf(archPath);

		if (mode.equals("full"))
			arch.writeToDotFull(depsPath, outputPath);
		else if (mode.equals("clusters"))
			arch.writeToDotClusters(depsPath, outputPath);
		else
			arch.writeToDot(depsPath, outputPath);
	}
}
