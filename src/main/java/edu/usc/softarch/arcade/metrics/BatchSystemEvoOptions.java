package edu.usc.softarch.arcade.metrics;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.Parameter;

public class BatchSystemEvoOptions {
	@Parameter(description="[the directory you want to compute SysEvo for]")
	public List<String> parameters = new ArrayList<String>();
	
	@Parameter(names = "-distopt", description = "1 for vdist = 1, 2 for all combinations of vdist > 1, 3 for a subset of combinations of vdist > 1")
	public Integer distopt=1;

	
	@Parameter(names = "--help", help = true, description = "print this help menu")
	private boolean help;
}
