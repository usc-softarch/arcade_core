package edu.usc.softarch.arcade.smellarchgraph;

import java.io.Serializable;

import edu.usc.softarch.arcade.clustering.Cluster;


/**
 * @author joshua
 */
public class ClusterEdge implements Serializable {
	private static final long serialVersionUID = -5252058761923559301L;
	private Cluster src;
	private Cluster tgt;

	public ClusterEdge(Cluster src, Cluster tgt) {
		this.setSrc(src);
		this.setTgt(tgt);
	}
	
	public boolean equals(Object o) {
		ClusterEdge edge = (ClusterEdge) o;
		if (
				this.src.equals(edge.src) &&
				this.tgt.equals(edge.tgt)
			)
			return true;
		else
			return false;
	}
	
	public int hashCode() {
		int hash = 7;
		hash = 37 * hash + (this.src == null ? 0 : this.src.hashCode());
		hash = 37 * hash + (this.tgt == null ? 0 : this.tgt.hashCode());
		return hash;
	}
	
	public String toString() {
		return "(" + getSrc().getName() + "," + getTgt().getName() + ")";
	}
	
	public String toDotString() {
		return "\t\"" + getSrc().getName() + "\" -> \"" + getTgt().getName() + "\";";
	}

	public void setSrc(Cluster src) {
		this.src = src;
	}

	public Cluster getSrc() {
		return src;
	}

	public void setTgt(Cluster tgt) {
		this.tgt = tgt;
	}

	public Cluster getTgt() {
		return tgt;
	}

}
