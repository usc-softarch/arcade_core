package edu.usc.softarch.arcade.clustering;

import java.util.ArrayList;

import edu.usc.softarch.arcade.topics.DocTopicItem;
import edu.usc.softarch.arcade.topics.TopicItem;

/**
 * @author joshua
 */
public class FeatureVector extends ArrayList<Feature> {
	// #region FIELDS ------------------------------------------------------------
	private static final long serialVersionUID = -2684090300773683383L;

	private String name;
	private DocTopicItem docTopicItem;
	// #endregion FIELDS ---------------------------------------------------------
	
	// #region CONSTRUCTORS ------------------------------------------------------
	public FeatureVector() {
		super();
		setName("");
	}
	
	public FeatureVector(String name) {
		super();
		setName(name);
	}

	/**
	 * Clone constructor.
	 */
	public FeatureVector(FeatureVector fve) {
		super(fve);
		setName(fve.getName());
	}
	// #endregion CONSTRUCTORS ---------------------------------------------------

	// #region ACCESSORS ---------------------------------------------------------
	public String getName() { return this.name; }
	public DocTopicItem getDocTopicItem() { return this.docTopicItem; }
	public String getInnermostPackageName() {
		return getName().substring(
			getName().lastIndexOf('.') + 1, getName().length() - 1);
	}

	public void setName(String name) { this.name = name; }
	public void setDocTopicItem(DocTopicItem docTopicItem) {
		this.docTopicItem = docTopicItem; }
	public TopicItem addTopicItem(TopicItem topicItem) {
		return this.docTopicItem.addTopic(topicItem); }

	public String toBinaryForm() {
		String str = "";
		for (Feature f : this)
			str = f.getValue().toString();

		return str;
	}

	public void changeFeatureValue(String tgtStr, double value) {
		for (Feature f : this)
			if (tgtStr.equals(f.getEdge().getTgtStr()))
				f.setValue(value);
	}

	public double getSumSharedFeatures(FeatureVector toCompare) {
		double sharedFeatureSum = 0;

		for (int i = 0; i < this.size(); i++) {
			Feature f1 = this.get(i);
			Feature f2 = toCompare.get(i);
			
			if (f1.getValue() > 0 && f2.getValue() > 0)
				sharedFeatureSum = f1.getValue() + f2.getValue();
		}
		
		return sharedFeatureSum;
	}

	public int getNum01Features(FeatureVector toCompare) {
		int count = 0;
		
		for (int i = 0; i < this.size(); i++) {
			Feature f1 = this.get(i);
			Feature f2 = toCompare.get(i);
			
			if (f1.getValue() == 0 && f2.getValue() > 0) count++;
		}

		return count;
	}

	public int getNum10Features(FeatureVector toCompare) {
		int count = 0;

		for (int i = 0; i < this.size(); i++) {
			Feature f1 = this.get(i);
			Feature f2 = toCompare.get(i);
			
			if (f1.getValue() > 0 && f2.getValue() == 0) count++;
		}
		
		return count;
	}
	// #endregion ACCESSORS ------------------------------------------------------
	
	// #region MISC --------------------------------------------------------------
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof FeatureVector))
			return false;

		FeatureVector fv = (FeatureVector) o;
		return this.name.equals(fv.name);
	}
	
	@Override
	public int hashCode() {
		int hash = 7;
		hash = 37 * hash + (this.name == null ? 0 : this.name.hashCode());
		return hash;
	}
	
	/**
	 * @deprecated
	 */
	public boolean equals(FeatureVector fv) {
		//TODO this needs to be ported to the other equals method
		boolean isEqual = true;
		
		for (int i=0;i<fv.size();i++) {
			if (!fv.get(i).equals(this.get(i))) {
				isEqual = false;
				break;
			}
		}
		return isEqual;
	}
	// #endregion MISC -----------------------------------------------------------
}
