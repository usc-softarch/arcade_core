package edu.usc.softarch.arcade;

import com.beust.jcommander.Parameter;

public class PkgsWithSmellDetectionOptions {
	@Parameter(names = {"-c","--clustersDir"}, description = "directory with packages as clustered stored as rsf file", required=true)
	public String clustersDir;
	
	@Parameter(names = {"-s","--smellsDir"}, description = "directory where generated smell ser files are stored to", required=true)
	public String smellsDir;
	
	@Parameter(names = {"-d","--depsDir"}, description = "directory with dependency rsf files for the versions to analayze", required=true	)
	public String depsDir;
	
	@Parameter(names = "--help", help = true, description = "print this help menu")
	private boolean help;
}
