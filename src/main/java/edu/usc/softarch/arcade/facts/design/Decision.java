package edu.usc.softarch.arcade.facts.design;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class Decision {
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
	public void serialize(JsonGenerator generator) throws IOException {
		generator.writeStringField("description", this.description);
		generator.writeStringField("id", this.id);
		generator.writeStringField("version", this.version);

		generator.writeFieldName("addedElements");
		generator.writeArray(addedElements.toArray(new String[0]),
			0, addedElements.size());

		generator.writeFieldName("removedElements");
		generator.writeArray(removedElements.toArray(new String[0]),
			0, removedElements.size());
	}
	//endregion
}
