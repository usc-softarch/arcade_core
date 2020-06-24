package edu.usc.softarch.arcade.metrics;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.Parameter;

public class SystemEvoOptions {
	@Parameter
	public List<String> parameters = new ArrayList<String>();
	
	@Parameter(names = "--help", help = true, description = "print this help menu")
	private boolean help;
}
