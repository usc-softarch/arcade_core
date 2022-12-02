package edu.usc.softarch.arcade;

import edu.usc.softarch.arcade.util.FileUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class RsfCompare {
	public static void main(String[] args) throws IOException {
		String rsf1Path = args[0];
		String rsf2Path = args[1];
		String rsf1 = FileUtil.readFile(rsf1Path, StandardCharsets.UTF_8);
		String rsf2 = FileUtil.readFile(rsf2Path, StandardCharsets.UTF_8);
		RsfCompare rsf1Compare = new RsfCompare(rsf1);
		RsfCompare rsf2Compare = new RsfCompare(rsf2);
		System.out.println(rsf1Compare.equals(rsf2Compare));
	}

	private final Set<String> rsfSet;
	
	// Constructor passes in string (pass in path to FileUtil.readFile)
	public RsfCompare(String rsfContents) {
    this.rsfSet = new HashSet<>(
			Arrays.asList(rsfContents.split("\\r?\\n")));
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof RsfCompare))
			return false;

		RsfCompare toCompare = (RsfCompare) o;
		return this.rsfSet.equals(toCompare.rsfSet);
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		for (String s : rsfSet.stream().sorted().collect(Collectors.toList()))
			result.append(s).append(System.lineSeparator());

		return result.toString();
	}
}
