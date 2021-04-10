package edu.usc.softarch.arcade.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class RsfCompare implements Comparable<RsfCompare> {
    private Set<String> rsfSet;
	
    // Constructor passes in string (pass in path to FileUtil.readFile)
	public RsfCompare(String rsfContents) {
    this.rsfSet = new HashSet<>(Arrays.asList(rsfContents.split("\\r?\\n")));
	}
	
	@Override
	public int compareTo(RsfCompare rsf1) {
        // Returns 0 if contents of the 2 rsf files are the same (regardless of order)
		if (this.rsfSet.equals(rsf1.rsfSet)) {
			return 0;
		}
		else {
			return 1;
		}
	}

	@Override
	public boolean equals(Object o){
		if (!(o instanceof RsfCompare)){
			return false;
		}

		RsfCompare toCompare = (RsfCompare)o;
		return this.rsfSet.equals(toCompare.rsfSet);
	}
}

