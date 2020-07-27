package edu.usc.softarch.arcade.facts;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import edu.usc.softarch.arcade.topics.DocTopicItem;

/**
 * @author joshua
 * 
 */
public class ConcernCluster {
	private String name;
	private Set<String> entities = new HashSet<String>();
	private DocTopicItem docTopicItem = null;
	
	public String getName() {
		return name;
	}

	public Set<String> getEntities() {
		return entities;
	}

	public void setEntities(Set<String> entities) {
		this.entities = entities;
	}
	
	public void addEntity(String entity) {
		entities.add(entity);
	}

	public void setName(String trim) {
		name = trim;
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof ConcernCluster) {
			ConcernCluster cluster = (ConcernCluster) obj;
			if (cluster.name.equals(this.name)) {
				return true;
			}
		}
		if (obj instanceof String) {
			String clusterName = (String)obj;
			if (clusterName.equals(this.name)) {
				return true;
			}
		}
		return false;
	}
	
	public int hashCode() {
		return this.name.hashCode();
	}

	public DocTopicItem getDocTopicItem() {
		return docTopicItem;
	}

	public void setDocTopicItem(DocTopicItem docTopicItem) {
		this.docTopicItem = docTopicItem;
	}
	
	public String toString() {
		return this.name;
	}
}
