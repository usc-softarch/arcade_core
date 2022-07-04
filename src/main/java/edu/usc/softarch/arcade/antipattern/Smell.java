package edu.usc.softarch.arcade.antipattern;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import edu.usc.softarch.util.json.EnhancedJsonGenerator;
import edu.usc.softarch.util.json.EnhancedJsonParser;
import edu.usc.softarch.util.json.JsonSerializable;

public class Smell implements Serializable, JsonSerializable {
	private static final long serialVersionUID = 1L;
	//region ATTRIBUTES
	public enum SmellType {
		bco, bdc, buo, spf
	}

	private final Collection<String> clusters;
	private final SmellType smellType;
	private final int topicNum;
	//endregion

	//region CONSTRUCTORS
	public Smell(SmellType smellType) {
		this.smellType = smellType;
		this.topicNum = -1;
		this.clusters = new ArrayList<>();
	}

	public Smell(int topicNum) {
		this.smellType = SmellType.spf;
		this.topicNum = topicNum;
		this.clusters = new ArrayList<>();
	}
	//endregion

	//region ACCESSORS
	public SmellType getSmellType() { return this.smellType; }
	public int getTopicNum() { return this.topicNum; }

	public boolean addCluster(String clusterName) {
		return this.clusters.add(clusterName); }
	public boolean addClusterCollection(Collection<String> clusters) {
		return this.clusters.addAll(clusters); }
	//endregion

	//region OBJECT METHODS
	public String toString() {
		Collection<String> orderedClusters =
			clusters.stream().sorted().collect(Collectors.toList());
		String result = smellType.toString();
		if (topicNum != -1) result += " " + topicNum;
		result += ": ";
		return result + String.join(",", orderedClusters);
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
			return this.clusters.equals(inSmell.clusters);
		}
	}
	
	public int hashCode() {
		Collection<String> orderedClusters =
			clusters.stream().sorted().collect(Collectors.toList());

		return orderedClusters.hashCode() + this.topicNum + this.smellType.ordinal();
	}
	//endregion

	//region SERIALIZATION
	@Override
	public void serialize(EnhancedJsonGenerator generator) throws IOException {
		generator.writeField("clusters", clusters);
		generator.writeField("type", smellType.toString());
		generator.writeField("topicNum", topicNum);
	}

	public static Smell deserialize(EnhancedJsonParser parser)
			throws IOException {
		Collection<String> clusters = parser.parseCollection(String.class);
		SmellType type = SmellType.valueOf(parser.parseString());
		int topicNum = parser.parseInt();

		Smell result;

		if (type.equals(SmellType.spf))
			result = new Smell(topicNum);
		else
			result = new Smell(type);
		result.clusters.addAll(clusters);

		return result;
	}
	//endregion
}
