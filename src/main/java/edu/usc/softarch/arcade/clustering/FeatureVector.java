package edu.usc.softarch.arcade.clustering;

import java.util.ArrayList;

import edu.usc.softarch.arcade.topics.DocTopicItem;

/**
 * @author joshua
 */
public class FeatureVector extends ArrayList<Feature> {
	private static final long serialVersionUID = -2684090300773683383L;
	public String name = "";
	public DocTopicItem docTopicItem;
	
	public DocTopicItem getDocTopicItem() {
		return docTopicItem;
	}

	public void setDocTopicItem(DocTopicItem docTopicItem) {
		this.docTopicItem = docTopicItem;
	}

	public FeatureVector() {
		super();
	}
	
	public FeatureVector(String name) {
		this.name = name;
	}
	
	public String toBinaryForm() {
		String str = "";
		for (int i=0;i<this.size(); i++) {
			Feature f = this.get(i);
			str = (Double.valueOf(f.value)).toString();
		}
		return str;
	}
	
	public void changeFeatureValue(String tgtStr, double value) {
		for (Feature f : this) {
			if (tgtStr.equals(f.edge.getTgtStr())) {
				f.value = value;
			}
		}
	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof FeatureVector))
			return false;

		FeatureVector fv = (FeatureVector)o;
		return this.name.equals(fv.name);
	}
	
	@Override
	public int hashCode() {
		int hash = 7;
		hash = 37 * hash + (this.name == null ? 0 : this.name.hashCode());
		return hash;
	}
	
	public boolean equals(FeatureVector fv) {
		
		boolean isEqual = true;
		
		for (int i=0;i<fv.size();i++) {
			if (!fv.get(i).equals(this.get(i))) {
				isEqual = false;
				break;
			}
		}
		return isEqual;
	}
}
