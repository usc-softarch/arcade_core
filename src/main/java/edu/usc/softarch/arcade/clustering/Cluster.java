package edu.usc.softarch.arcade.clustering;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.usc.softarch.arcade.clustering.data.ReadOnlyCluster;
import edu.usc.softarch.arcade.topics.DocTopicItem;
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
	private final double[] featureMap;
	private final Collection<Integer> featureIndices;
	//endregion

	//region CONSTRUCTORS
	/**
	 * Deserialization constructor.
	 */
	private Cluster(String name, Collection<String> entities, int numEntities,
			double[] featureMap, DocTopicItem dti) {
		super(name, entities, dti);
		this.numEntities = numEntities;
		this.featureMap = featureMap;
		this.featureIndices = buildFeatureIndices();
	}

	/**
	 * Default constructor for new Clusters.
	 *
	 * @param name Name of the new Cluster, typically its originating entity.
	 * @param featureSet Set of the new Cluster's entities.
	 */
	public Cluster(String name, BitSet featureSet, int numFeatures) {
		super(name);
		super.addEntity(name);

		this.featureMap = new double[numFeatures];
		for (int i = 0; i < numFeatures; i++) {
			if (featureSet.get(i))
				featureMap[i] = 1.0;
			else
				featureMap[i] = 0.0;
		}

		this.featureIndices = buildFeatureIndices();

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
		this.featureIndices = buildFeatureIndices();
	}

	/**
	 * Merge constructor.
	 */
	public Cluster(ClusteringAlgorithmType cat, Cluster c1, Cluster c2,
			String projectName, String projectVersion)
			throws UnmatchingDocTopicItemsException {
		super(cat, c1, c2, projectName, projectVersion);

		this.featureMap = new double[c1.getFeatureMap().length];

		Set<Integer> indices = new HashSet<>(c1.getFeatureIndices());
		indices.addAll(c2.getFeatureIndices());

		switch (cat) {
			case LIMBO:
			case ARC:
				setLimboFeatureMap(c1, c2, indices);
				break;

			case WCA:
				setWcaFeatureMap(c1, c2, indices);
		}

		this.numEntities = c1.getNumEntities() + c2.getNumEntities();
		this.featureIndices = buildFeatureIndices();
	}
	//endregion

	//region ACCESSORS
	/**
	 * Returns the {@link #featureMap} of this Cluster itself, NOT a copy.
	 */
	public double[] getFeatureMap() { return this.featureMap; }

	public Collection<Integer> getFeatureIndices() {
		return new ArrayList<>(this.featureIndices); }

	public int getFeatureCount() {
		return this.featureIndices.size(); }

	/**
	 * Returns the number of entities represented by this Cluster.
	 */
	public int getNumEntities() { return this.numEntities; }
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
			double c1Value = c1.getFeatureMap()[index];
			double c2Value = c2.getFeatureMap()[index];

			double newFeatureValue;
			if (c1Value == 0.0 && c2Value == 0.0)
				newFeatureValue = 0.0;
			else if (c1Value == 0.0)
				newFeatureValue = (c2Value * c2.getNumEntities()) /
					(c1.getNumEntities() + c2.getNumEntities());
			else if (c2Value == 0.0)
				newFeatureValue = (c1Value * c1.getNumEntities()) /
					(c1.getNumEntities() + c2.getNumEntities());
			else
				newFeatureValue = (c1Value * c1.getNumEntities()
					+ c2Value * c2.getNumEntities()) /
					(c1.getNumEntities() + c2.getNumEntities());

			featureMap[index] = newFeatureValue;
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
			double c1Value = c1.getFeatureMap()[index];
			double c2Value = c2.getFeatureMap()[index];

			double newFeatureValue;
			if (c1Value == 0.0 && c2Value == 0.0)
				newFeatureValue = 0.0;
			else if (c1Value == 0.0)
				newFeatureValue = c2Value / (c1.getNumEntities() + c2.getNumEntities());
			else if (c2Value == 0.0)
				newFeatureValue = c1Value / (c1.getNumEntities() + c2.getNumEntities());
			else
				newFeatureValue = (c1Value + c2Value) /
					(c1.getNumEntities() + c2.getNumEntities());

			featureMap[index] = newFeatureValue;
		}
	}

	private Collection<Integer> buildFeatureIndices() {
		Collection<Integer> result = new ArrayList<>();

		for (int i = 0; i < featureMap.length; i++)
			if (featureMap[i] != 0.0) result.add(i);

		return result;
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
		boolean condition4 =
			Arrays.equals(this.featureMap, toCompare.featureMap);

		return condition1 && condition2	&& condition3	&& condition4;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, numEntities); }

	@Override
	public int compareTo(Cluster o) { return this.name.compareTo(o.name);	}
	//endregion

	//region SERIALIZATION
	@Override
	public void serialize(EnhancedJsonGenerator generator) throws IOException {
		generator.writeField("name", name);
		generator.writeField("entities", super.getEntities());
		generator.writeField("numEntities", numEntities);
		generator.writeField("featureMap",
			Arrays.stream(featureMap).boxed().collect(Collectors.toList()));
		generator.writeField("dti", super.getDocTopicItem());
	}

	public static Cluster deserialize(EnhancedJsonParser parser)
			throws IOException {
		String name = parser.parseString();
		Collection<String> entities = parser.parseCollection(String.class);
		int numEntities = parser.parseInt();
		double[] featureMap = Stream.of(parser.parseCollection(Double.class)
			.toArray(new Double[0])).mapToDouble(Double::doubleValue).toArray();
		DocTopicItem dti = parser.parseObject(DocTopicItem.class, "dti");

		return new Cluster(name, entities, numEntities, featureMap, dti);
	}
	//endregion
}
