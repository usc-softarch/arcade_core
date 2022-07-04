package edu.usc.softarch.arcade.antipattern;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

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
		try (EnhancedJsonParser parser = new EnhancedJsonParser(path)) {
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
}
