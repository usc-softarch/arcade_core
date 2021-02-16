package edu.usc.softarch.arcade.antipattern;

import java.util.List;
import java.util.stream.Collectors;

import edu.usc.softarch.arcade.clustering.ConcernClusterArchitecture;
import edu.usc.softarch.arcade.facts.ConcernCluster;

public class Smell {
	// #region FIELDS ------------------------------------------------------------
	public enum SmellType {
		bco, bdc, buo, spf
	}

	private ConcernClusterArchitecture clusters;
	private final SmellType smellType;
	private final int topicNum;
	// #endregion FIELDS ---------------------------------------------------------

	// #region CONSTRUCTORS ------------------------------------------------------
	public Smell(SmellType smellType) {
		this.smellType = smellType;
		this.topicNum = -1;
		this.clusters = new ConcernClusterArchitecture();
	}

	public Smell(int topicNum) {
		this.smellType = SmellType.spf;
		this.topicNum = topicNum;
		this.clusters = new ConcernClusterArchitecture();
	}
	// #endregion CONSTRUCTORS ---------------------------------------------------

	// #region ACCESSORS ---------------------------------------------------------
	public ConcernClusterArchitecture getClusters() {
		return new ConcernClusterArchitecture(this.clusters); }
	public SmellType getSmellType() { return this.smellType; }
	public int getTopicNum() { return this.topicNum; }

	public boolean addCluster(ConcernCluster cluster) {
		return this.clusters.add(cluster); }
	public boolean removeCluster(ConcernCluster cluster) {
		return this.clusters.remove(cluster); }
	// #endregion ACCESSORS ------------------------------------------------------
	
	public String toString() {
		// Makes a list by calling "toString" on every ConcernCluster in clusters
		List<String> clusterTexts =
			clusters.stream().map(ConcernCluster::toString)
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
			return this.clusters.equals(inSmell.getClusters());
		}
	}
	
	public int hashCode() {
		return this.getClusters().hashCode();
	}
}