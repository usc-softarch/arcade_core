package edu.usc.softarch.arcade.antipattern.detection;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import edu.usc.softarch.arcade.facts.ConcernCluster;

public class Smell {
	// #region FIELDS ------------------------------------------------------------
	public enum SmellType {
		bco, bdc, buo, spf
	}

	@Deprecated
	Set<ConcernCluster> clusters;
	private Set<ConcernCluster> newClusters;
	private final SmellType smellType;
	// #endregion FIELDS ---------------------------------------------------------

	// #region CONSTRUCTORS ------------------------------------------------------
	@Deprecated
	public Smell() {
		this.smellType = SmellType.spf;
		this.clusters = new HashSet<>();
	}

	public Smell(SmellType smellType) {
		this.smellType = smellType;
		this.newClusters = new HashSet<>();
	}
	// #endregion CONSTRUCTORS ---------------------------------------------------

	// #region ACCESSORS ---------------------------------------------------------
	public Set<ConcernCluster> getClusters() {
		return new HashSet<>(this.newClusters); }
	public SmellType getSmellType() { return this.smellType; }

	public boolean addCluster(ConcernCluster cluster) {
		return this.newClusters.add(cluster); }
	public boolean removeCluster(ConcernCluster cluster) {
		return this.newClusters.remove(cluster); }
	// #endregion ACCESSORS ------------------------------------------------------
	
	public String toString() {
		if(clusters != null) {
			// Makes a list by calling "toString" on every ConcernCluster in clusters
			List<String> clusterTexts =
				clusters.stream().map(ConcernCluster::toString)
					.collect(Collectors.toList());
			return String.join(",", clusterTexts);
		}
		// Makes a list by calling "toString" on every ConcernCluster in clusters
		List<String> clusterTexts =
			newClusters.stream().map(ConcernCluster::toString)
				.collect(Collectors.toList());
		return String.join(",", clusterTexts);
	}
	
	public boolean equals (Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof Smell))
			return false;
		else {
			Smell inSmell = (Smell) obj;
			if(clusters != null)
				return this.clusters.equals(inSmell.clusters);
			return this.newClusters.equals(inSmell.getClusters());
		}
	}
	
	public int hashCode() {
		if(clusters != null)
			return this.clusters.hashCode();
		return this.getClusters().hashCode();
	}
}