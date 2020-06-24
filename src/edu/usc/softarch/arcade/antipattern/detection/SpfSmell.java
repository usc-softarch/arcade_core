package edu.usc.softarch.arcade.antipattern.detection;

public class SpfSmell extends Smell {
	final int topicNum;
	
	SpfSmell(int topicNum) {
		this.topicNum = topicNum;
	}
	
	public boolean equals (Object obj) {
		if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (!(obj instanceof SpfSmell))
            return false;
        else {
        	SpfSmell inSmell = (SpfSmell)obj;
        	if (clusters.equals(inSmell.clusters)) {
        		if (this.topicNum == inSmell.topicNum) {
        			return true;
        		}
        		else {
        			return false;
        		}
        	}
        	else {
        		return false;
        	}
        }
        
	}
	
}
