package edu.usc.softarch.arcade.facts.design;

import edu.usc.softarch.arcade.util.json.EnhancedJsonGenerator;
import edu.usc.softarch.arcade.util.json.EnhancedJsonParser;
import edu.usc.softarch.arcade.util.json.JsonSerializable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class Decision implements JsonSerializable {
	//region ATTRIBUTES
	public final String description;
	public final String id;
	public final String version;
	private final Collection<String> addedElements;
	private final Collection<String> removedElements;
	//endregion

	//region CONSTRUCTORS
	public Decision(String description, String id, String version,
			Collection<String> addedElements, Collection<String> removedElements) {
		this.description = description;
		this.id = id;
		this.version = version;
		this.addedElements = addedElements;
		this.removedElements = removedElements;
	}
	//endregion

	//region ACCESSORS
	public Collection<String> getAddedElements() {
		return new ArrayList<>(this.addedElements);	}
	public Collection<String> getRemovedElements() {
		return new ArrayList<>(this.removedElements); }
	public boolean isEmpty() {
		return this.addedElements.isEmpty() && this.removedElements.isEmpty(); }
	//endregion

	//region SERIALIZATION
	@Override
	public void serialize(EnhancedJsonGenerator generator) throws IOException {
		generator.writeField("description", this.description);
		generator.writeField("id", this.id);
		generator.writeField("version", this.version);
		generator.writeField("addedElements", this.addedElements);
		generator.writeField("removedElements", this.removedElements);
	}

	public static Decision deserialize(EnhancedJsonParser parser)
			throws IOException {
		String description = parser.parseString();
		String id = parser.parseString();
		String version = parser.parseString();
		Collection<String> addedElements = parser.parseCollection(String.class);
		Collection<String> removedElements = parser.parseCollection(String.class);

		return new Decision(
			description, id, version, addedElements, removedElements);
	}
	//endregion
}
