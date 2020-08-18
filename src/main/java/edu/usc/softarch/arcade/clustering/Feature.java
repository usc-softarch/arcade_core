package edu.usc.softarch.arcade.clustering;

import java.io.Serializable;

import edu.usc.softarch.arcade.classgraphs.StringEdge;

/**
 * @author joshua
 */
public class Feature implements Serializable {
	// #region FIELDS ------------------------------------------------------------
	private static final long serialVersionUID = -2658336048930856739L;

	private StringEdge edge;
	private Double value;
	// #endregion FIELDS ---------------------------------------------------------
	
	// #region CONSTRUCTORS ------------------------------------------------------
	public Feature() { super(); }

	public Feature(StringEdge edge, Double value) {
		setEdge(edge);
		setValue(value);
	}
	// #endregion CONSTRUCTORS ---------------------------------------------------

	// #region ACCESSORS ---------------------------------------------------------
	public StringEdge getEdge() { return new StringEdge(edge); }
	public Double getValue() { return this.value; }

	public void setEdge(StringEdge edge) { this.edge = edge; }
	public void setValue(Double value) { this.value = value; }
	// #endregion ACCESSORS ------------------------------------------------------

	// #region MISC --------------------------------------------------------------
	public String toString() {
		return edge + (this.value == 1 ? ":true" : ":false");
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
		hash = 37 * hash + this.value.hashCode();
		return hash;
	}
	// #endregion MISC -----------------------------------------------------------
}