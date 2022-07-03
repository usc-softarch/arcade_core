package edu.usc.softarch.util.json;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * A façade for the Jackson JsonGenerator, to make it less clunky.
 */
public class EnhancedJsonGenerator implements AutoCloseable {
	//region ATTRIBUTES
	public final JsonGenerator generator;
	//endregion

	//region CONSTRUCTORS
	public EnhancedJsonGenerator(JsonGenerator generator) {
		this.generator = generator;
	}

	public EnhancedJsonGenerator(String filePath) throws IOException {
		JsonFactory factory = new JsonFactory();
		this.generator =
			factory.createGenerator(new File(filePath), JsonEncoding.UTF8);
		this.generator.writeStartObject();
	}
	//endregion

	//region PROCESSING
	public void writeField(String name, String value) throws IOException {
		this.generator.writeStringField(name, value);
	}

	public void writeField(String name, Integer value) throws IOException {
		this.generator.writeNumberField(name, value);
	}

	public void writeField(String name, Double value) throws IOException {
		this.generator.writeNumberField(name, value);
	}

	public void writeField(String name, JsonSerializable value)
			throws IOException {
		if (value == null) return;

		this.generator.writeFieldName(name);
		this.generator.writeStartObject();
		value.serialize(this);
		this.generator.writeEndObject();
	}

	/**
	 * This method can ONLY take collections of base types or JsonSerializable
	 * objects. Attempting to give it anything else will cause the program to
	 * explode. When serializing collections of collections, names must be
	 * provided for every level of depth.
	 */
	public void writeField(String name, Collection<?> values) throws IOException {
		if (values.isEmpty()) {
			this.generator.writeFieldName(name);
			this.generator.writeStartArray();
			this.generator.writeEndArray();
			return;
		}

		String type = JsonSerializable.ascertainSerializableType(
			values.iterator().next().getClass());

		switch (type) {
			case "String":
				writeStringArrayField(name, (Collection<String>) values);
				break;
			case "Integer":
				writeIntegerArrayField(name, (Collection<Integer>) values);
				break;
			case "Double":
				writeDoubleArrayField(name, (Collection<Double>) values);
				break;
			case "JsonSerializable":
				writeObjectArrayField(name, (Collection<JsonSerializable>) values);
				break;
			default:
				throw new IllegalArgumentException("Type " + type +
					" cannot be serialized.");
		}
	}

	public void writeField(String name, Map<?, ?> values, boolean keepIndex,
			String... subName) throws IOException {
		this.generator.writeArrayFieldStart(name);

		for (Map.Entry<?, ?> entry : values.entrySet()) {
			this.generator.writeStartObject();

			if (keepIndex) {
				this.writeFieldUnknownType(subName[0], entry.getKey());
				this.writeFieldUnknownType(subName[1], entry.getValue());
			} else {
				this.writeFieldUnknownType(subName[0], entry.getValue());
			}

			this.generator.writeEndObject();
		}

		this.generator.writeEndArray();
	}

	private void writeFieldUnknownType(String name, Object value)
			throws IOException {
		String type = JsonSerializable.ascertainSerializableType(value.getClass());

		switch (type) {
			case "String":
				writeField(name, (String) value);
				break;
			case "Integer":
				writeField(name, (Integer) value);
				break;
			case "Double":
				writeField(name, (Double) value);
				break;
			case "JsonSerializable":
				writeField(name, (JsonSerializable) value);
				break;
			case "Collection":
				writeField(name, (Collection<?>) value);
				break;
			default:
				throw new IllegalArgumentException("Type " + type +
					" cannot be serialized.");
		}
	}

	private void writeObjectArrayField(String name,
			Collection<JsonSerializable> values) throws IOException {
		this.generator.writeArrayFieldStart(name);
		for (JsonSerializable value : values) {
			this.generator.writeStartObject();
			value.serialize(this);
			this.generator.writeEndObject();
		}
		this.generator.writeEndArray();
	}

	private void writeStringArrayField(String name,
			Collection<String> values) throws IOException {
		this.generator.writeFieldName(name);
		this.generator.writeArray(
			values.toArray(new String[0]), 0, values.size());
	}

	private void writeIntegerArrayField(String name,
			Collection<Integer> values) throws IOException {
		this.generator.writeFieldName(name);
		this.generator.writeArray(
			values.stream().mapToInt(i->i).toArray(), 0, values.size());
	}

	private void writeDoubleArrayField(String name,
			Collection<Double> values) throws IOException {
		this.generator.writeFieldName(name);
		this.generator.writeArray(
			values.stream().mapToDouble(i->i).toArray(), 0, values.size());
	}
	//endregion

	//region OBJECT METHODS
	@Override
	public void close() throws IOException {
		this.generator.writeEndObject();
		this.generator.close();
	}
	//endregion
}
