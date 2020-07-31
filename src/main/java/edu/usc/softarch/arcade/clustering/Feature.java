package edu.usc.softarch.arcade.clustering;

import java.io.Serializable;

import edu.usc.softarch.arcade.classgraphs.StringEdge;

/**
 * @author joshua
 */
public class Feature implements Serializable {
	private static final long serialVersionUID = -2658336048930856739L;
	public StringEdge edge;
	public double value;
	
	Feature(StringEdge edge, double value) {
		this.edge = edge;
		this.value = value;
	}
	
	public Feature() { super(); }

	public String toString() {
		if (this.value == 1) 
			return edge + ":true";
		else 
			return edge + ":false";
	}
	
	public boolean equals(Object o) {
		if(!(o instanceof Feature))
			return false;

		Feature f = (Feature)o;
		return this.edge.equals(f.edge) && this.value == f.value;
	}
	
	public int hashCode() {
		int hash = 7;
		hash = 37 * hash + (this.edge == null ? 0 : this.edge.hashCode());
		hash = 37 * hash + (Double.valueOf(this.value)).hashCode();
		return hash;
	}
}
