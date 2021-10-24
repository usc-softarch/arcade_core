package edu.usc.softarch.arcade.clustering;

import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.usc.softarch.arcade.topics.DocTopicItem;

public class Entity
{
	public String name;
	public Set <String> featureSet = new HashSet<>();
	public BitSet featureVector = new BitSet();
	public Map<Integer,Double> nonZeroFeatureMap = new HashMap<>();
	public int numOfEntities = 1;
	public DocTopicItem docTopicItem;
	public int getNumEntities()
	{
		return numOfEntities;
	}
	public Entity(String name)
	{
		this.name = name;
	}
	public void initializeNonZeroFeatureMap(int bitSetSize)
	{
		nonZeroFeatureMap = new HashMap<>();
		for (int i=0;i<bitSetSize;i++) 
		{
			if (featureVector.get(i))
			{
				double one = 1;
				nonZeroFeatureMap.put(i,one);
			}
	    }
     }

	public void setNonZeroFeatureMapForLibmoUsingIndices(Entity c1,
			Entity c2, Set<Integer> c1Indices) {
		for (Integer index : c1Indices) {
			Double c1Value = c1.nonZeroFeatureMap.get(index);
			Double c2Value = c2.nonZeroFeatureMap.get(index);
			
			Double newFeatureValue = null;
			if (c1Value == null && c2Value != null)
				newFeatureValue = Double.valueOf( (c2Value*c2.getNumEntities()) /(c1.getNumEntities()+c2.getNumEntities()));
			else if (c2Value == null && c1Value != null)
				newFeatureValue = Double.valueOf((c1Value*c1.getNumEntities())/(c1.getNumEntities()+c2.getNumEntities()));
			else if (c1Value != null && c2Value != null)
				newFeatureValue = Double.valueOf((c1Value*c1.getNumEntities()+ c2Value*c2.getNumEntities())/(c1.getNumEntities()+c2.getNumEntities()));
			
			if (newFeatureValue != null)
				nonZeroFeatureMap.put(index, newFeatureValue);
		}
	}

	public void normalizeFeatureVectorOfCluster(
			int featuresLength, double[] firstDist) { 
		for (int i = 0; i < featuresLength; i++) {
			if (this.nonZeroFeatureMap.get(i) != null) {
				double featureValue = this.nonZeroFeatureMap.get(i);
				firstDist[i] = featureValue/this.nonZeroFeatureMap.size();
			}	else // this feature is zero
				firstDist[i] = 0;
		}
	}
}