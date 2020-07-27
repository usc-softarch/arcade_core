package edu.usc.softarch.arcade.antipattern.detection;

import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Joiner;

import edu.usc.softarch.arcade.facts.ConcernCluster;

public abstract class Smell {	
	Set<ConcernCluster> clusters = new HashSet<ConcernCluster>();
	
	public String toString() {
		return Joiner.on(",").join(clusters);
	}
	
	public boolean equals (Object obj) {
		if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (!(obj instanceof Smell))
            return false;
        else {
        	Smell inSmell = (Smell)obj;
        	if (this.clusters.equals(inSmell.clusters)) {
        		return true;
        	}
        	else {
        		return false;
        	}
        }
        
	}
	
	public int hashCode() {
		return this.clusters.hashCode();
	}
}
