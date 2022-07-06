package edu.usc.softarch.arcade.antipattern;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import edu.usc.softarch.util.json.EnhancedJsonGenerator;
import edu.usc.softarch.util.json.EnhancedJsonParser;
import edu.usc.softarch.util.json.JsonSerializable;

public class SmellCollection
		extends HashSet<Smell> implements JsonSerializable {
	//region ATTRIBUTES
  private static final long serialVersionUID = 1L;
	//endregion

	//region CONSTRUCTORS
  public SmellCollection() { super(); }
	//endregion

	//region ACCESSORS
	public Collection<Smell> getClusterSmells(String clusterName) {
		return this.stream().filter(s ->
			s.containsCluster(clusterName)).collect(Collectors.toList());
	}
	//endregion

	//region SERIALIZATION
	public void serialize(String path) throws IOException {
		try (EnhancedJsonGenerator generator = new EnhancedJsonGenerator(path)) {
			serialize(generator);
		}
	}

	@Override
	public void serialize(EnhancedJsonGenerator generator) throws IOException {
		generator.writeField("smellcollection", (Set<Smell>) this);
	}

	public static SmellCollection deserialize(String path) throws IOException {
		return deserialize(new File(path));
	}

	public static SmellCollection deserialize(File file) throws IOException {
		try (EnhancedJsonParser parser = new EnhancedJsonParser(file)) {
			return deserialize(parser);
		}
	}

	public static SmellCollection deserialize(EnhancedJsonParser parser)
			throws IOException {
		SmellCollection result = new SmellCollection();
		result.addAll(parser.parseCollection(Smell.class));
		return result;
	}
	//endregion

	//region OBJECT METHODS
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		for (Smell smell : this)
			result.append(smell).append(System.lineSeparator());

		return result.toString();
	}
	//endregion
}
