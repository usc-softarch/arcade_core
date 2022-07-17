package edu.usc.softarch.arcade.facts.design;

import edu.usc.softarch.arcade.facts.Decision;
import edu.usc.softarch.util.json.EnhancedJsonGenerator;
import edu.usc.softarch.util.json.EnhancedJsonParser;
import edu.usc.softarch.util.json.JsonSerializable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class CodeElementDecision extends Decision
		implements JsonSerializable {
	//region ATTRIBUTES
	private final Collection<String> addedElements;
	private final Collection<String> removedElements;
	//endregion

	//region CONSTRUCTORS
	public CodeElementDecision(String description, String id, String version,
			Collection<String> addedElements, Collection<String> removedElements) {
		super(description, id, version);

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
		generator.writeField("description", super.description);
		generator.writeField("id", super.id);
		generator.writeField("version", super.version);
		generator.writeField("addedElements", this.addedElements);
		generator.writeField("removedElements", this.removedElements);
	}

	public static CodeElementDecision deserialize(EnhancedJsonParser parser)
		throws IOException {
		String description = parser.parseString();
		String id = parser.parseString();
		String version = parser.parseString();
		Collection<String> addedElements = parser.parseCollection(String.class);
		Collection<String> removedElements = parser.parseCollection(String.class);

		return new CodeElementDecision(
			description, id, version, addedElements, removedElements);
	}
	//endregion
}