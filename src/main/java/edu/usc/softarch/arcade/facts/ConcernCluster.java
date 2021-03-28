package edu.usc.softarch.arcade.facts;

import java.util.Set;
import java.io.Serializable;
import java.util.HashSet;

import edu.usc.softarch.arcade.topics.DocTopicItem;

/**
 * @author joshua
 */
public class ConcernCluster implements Serializable {
	private static final long serialVersionUID = 1L;
	// #region FIELDS ------------------------------------------------------------
	private String name;
	private Set<String> entities = new HashSet<>();
	private DocTopicItem docTopicItem = null;
	// #endregion FIELDS ---------------------------------------------------------
	
	// #region ACCESSORS ---------------------------------------------------------
	public String getName() {	return name; }
	public Set<String> getEntities() { return entities;	}
	public DocTopicItem getDocTopicItem() { return docTopicItem; }

	public void setEntities(Set<String> entities) { this.entities = entities; }
	public void addEntity(String entity) { entities.add(entity); }
	public void setName(String name) { this.name = name; }
	public void setDocTopicItem(DocTopicItem docTopicItem) {
		this.docTopicItem = docTopicItem; }
	// #endregion ACCESSORS ------------------------------------------------------
	
	public boolean equals(Object obj) {
		if (obj instanceof ConcernCluster) {
			ConcernCluster cluster = (ConcernCluster) obj;
			if (cluster.name.equals(this.name))
				return true;
		}
		if (obj instanceof String) {
			String clusterName = (String)obj;
			if (clusterName.equals(this.name))
				return true;
		}
		return false;
	}
	
	public int hashCode() { return this.name.hashCode(); }
	public String toString() { return this.name; }
}