package edu.usc.softarch.arcade.clustering;

import java.io.IOException;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import edu.usc.softarch.arcade.topics.Concern;
import edu.usc.softarch.arcade.topics.DocTopicItem;
import edu.usc.softarch.arcade.topics.DocTopics;
import edu.usc.softarch.arcade.topics.exceptions.UnmatchingDocTopicItemsException;
import edu.usc.softarch.util.json.EnhancedJsonGenerator;
import edu.usc.softarch.util.json.EnhancedJsonParser;
import edu.usc.softarch.util.json.JsonSerializable;

/**
 * Represents a cluster of entities in the subject system.
 */
public class Cluster extends ReadOnlyCluster
		implements JsonSerializable, Comparable<Cluster> {
	//region ATTRIBUTES
	//TODO numEntities should probably be in Architecture instead
	/**
	 * Total number of entities in the architecture to which this cluster belongs.
	 */
	private final int numEntities;
	/**
	 * Map of the values of each non-zero feature in this Cluster.
	 */
	private final Map<Integer, Double> featureMap;
	//endregion

	//region CONSTRUCTORS
	/**
	 * Deserialization constructor.
	 */
	private Cluster(String name, Collection<String> entities, int numEntities,
			Map<Integer, Double> featureMap, DocTopicItem dti) {
		super(name, entities, dti);
		this.numEntities = numEntities;
		this.featureMap = featureMap;
	}

	/**
	 * Default constructor for new Clusters.
	 *
	 * @param name Name of the new Cluster, typically its originating entity.
	 * @param featureSet Set of the new Cluster's entities.
	 */
	public Cluster(String name, BitSet featureSet, int numFeatures) {
		super(name);

		super.entities.add(name);

		this.featureMap = new HashMap<>();
		for (int i = 0; i < numFeatures; i++)
			if (featureSet.get(i)) // put if not 0
				featureMap.put(i, 1.0);

		// Cluster currently only has one entity, the node itself
		this.numEntities = 1;
	}

	/**
	 * Clone constructor.
	 */
	public Cluster(Cluster c1) {
		super(c1.name, c1.getEntities(), c1.getDocTopicItem());
		this.numEntities = c1.getNumEntities();
		this.featureMap = c1.getFeatureMap();
	}

	/**
	 * Merge constructor.
	 */
	public Cluster(ClusteringAlgorithmType cat, Cluster c1, Cluster c2)
			throws UnmatchingDocTopicItemsException {
		super(cat, c1, c2);

		this.featureMap = new HashMap<>();

		Set<Integer> indices = new HashSet<>(c1.getFeatureMap().keySet());
		indices.addAll(c2.getFeatureMap().keySet());

		switch (cat) {
			case LIMBO:
			case ARC:
				setLimboFeatureMap(c1, c2, indices);
				break;

			case WCA:
				setWcaFeatureMap(c1, c2, indices);
		}

		this.numEntities = c1.getNumEntities() + c2.getNumEntities();
	}
	//endregion

	//region ACCESSORS
	/**
	 * Returns the {@link #featureMap} of this Cluster itself, NOT a copy.
	 */
	public Map<Integer, Double> getFeatureMap() { return featureMap; }

	/**
	 * Returns the number of entities represented by this Cluster.
	 */
	public int getNumEntities() { return numEntities; }
	//endregion

	//region PROCESSING
	/**
	 * Merges the feature maps of two Clusters for Limbo or ARC.
	 *
	 * @param c1 First Cluster being merged.
	 * @param c2 Second Cluster being merged.
	 * @param indices The indices all features present in EITHER Cluster.
	 */
	private void setLimboFeatureMap(Cluster c1,	Cluster c2,
			Set<Integer> indices) {
		for (Integer index : indices) {
			Double c1Value = c1.getFeatureMap().get(index);
			Double c2Value = c2.getFeatureMap().get(index);

			if (c1Value == null && c2Value == null) continue;

			double newFeatureValue;
			if (c1Value == null)
				newFeatureValue = (c2Value * c2.getNumEntities()) /
					(c1.getNumEntities() + c2.getNumEntities());
			else if (c2Value == null)
				newFeatureValue = (c1Value * c1.getNumEntities()) /
					(c1.getNumEntities() + c2.getNumEntities());
			else
				newFeatureValue = (c1Value * c1.getNumEntities()
					+ c2Value * c2.getNumEntities()) /
					(c1.getNumEntities() + c2.getNumEntities());

			featureMap.put(index, newFeatureValue);
		}
	}

	/**
	 * Merges the feature maps of two Clusters for WCA.
	 *
	 * @param c1 First Cluster being merged.
	 * @param c2 Second Cluster being merged.
	 * @param indices The indices all features present in EITHER Cluster.
	 */
	private void setWcaFeatureMap(Cluster c1,	Cluster c2,
			Set<Integer> indices) {
		for (Integer index : indices) {
			Double c1Value = c1.getFeatureMap().get(index);
			Double c2Value = c2.getFeatureMap().get(index);

			if (c1Value == null && c2Value == null) continue;
			
			double newFeatureValue;
			if (c1Value == null)
				newFeatureValue = c2Value / (c1.getNumEntities() + c2.getNumEntities());
			else if (c2Value == null)
				newFeatureValue = c1Value / (c1.getNumEntities() + c2.getNumEntities());
			else
				newFeatureValue = (c1Value + c2Value) /
					(c1.getNumEntities() + c2.getNumEntities());

			featureMap.put(index, newFeatureValue);
		}
	}
	//endregion

	//region OBJECT METHODS
	public String toString() { return name; }

	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o == this) return true;
		if (!(o instanceof Cluster)) return false;

		Cluster toCompare = (Cluster) o;

		boolean condition1 = Objects.equals(this.name, toCompare.name);
		boolean condition2 = this.numEntities == toCompare.numEntities;
		boolean condition3 =
			Objects.equals(super.getDocTopicItem(), toCompare.getDocTopicItem());
		boolean condition4 = Objects.equals(this.featureMap, toCompare.featureMap);

		return condition1 && condition2	&& condition3	&& condition4;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, numEntities, featureMap, super.getDocTopicItem());
	}
	//endregion

	//region SERIALIZATION
	@Override
	public void serialize(EnhancedJsonGenerator generator) throws IOException {
		generator.writeField("name", name);
		generator.writeField("entities", entities);
		generator.writeField("numEntities", numEntities);
		generator.writeField("featureMap", featureMap,
			true, "featureIndex", "featureValue");
		generator.writeField("dti", super.getDocTopicItem());
	}

	public static Cluster deserialize(EnhancedJsonParser parser)
			throws IOException {
		String name = parser.parseString();
		Collection<String> entities = parser.parseCollection(String.class);
		int numEntities = parser.parseInt();
		Map<Integer, Double> featureMap =
			parser.parseMap(Integer.class, Double.class);
		DocTopicItem dti = parser.parseObject(DocTopicItem.class, "dti");

		return new Cluster(name, entities, numEntities, featureMap, dti);
	}

	@Override
	public int compareTo(Cluster o) {
		return this.name.compareTo(o.name);	}
	//endregion
}
