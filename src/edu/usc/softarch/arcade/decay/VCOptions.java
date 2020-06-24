package edu.usc.softarch.arcade.decay;

import com.beust.jcommander.Parameter;

public class VCOptions {
	@Parameter(names = {"-c","-clustersfile"}, description = "clusters rsf file")
	public String clustersFilename;
	
	@Parameter(names = {"-d","-depsfile"}, description = "deps rsf file")
	public String depsFilename;
	
	@Parameter(names = {"-e","-expected-deps-file"}, description = "expected deps rsf file")
	public String expectedDepsFilename;
	
	@Parameter(names = {"-i","-ignored-clusters-file"}, description = "ignored clusters file")
	public String ignoredClustersFilename;

	@Parameter(names = {"-o","-output-file"}, description = "output file")
	public String outputFilename;
}
