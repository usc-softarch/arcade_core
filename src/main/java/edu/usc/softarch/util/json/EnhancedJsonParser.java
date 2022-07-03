package edu.usc.softarch.util.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * A façade for the Jackson JsonParser, to make it less clunky.
 */
public class EnhancedJsonParser implements AutoCloseable {
	//region ATTRIBUTES
	public final JsonParser parser;
	public final JsonParser peekParser;
	//endregion

	//region CONSTRUCTORS
	public EnhancedJsonParser(String filePath) throws IOException {
		JsonFactory factory = new JsonFactory();
		this.parser = factory.createParser(new File(filePath));
		this.peekParser = factory.createParser(new File(filePath));

		this.peekParser.nextToken(); // initialize peeker
		this.nextToken(); // skip start object
	}
	//endregion

	//region PROCESSING
	public String parseString() throws IOException {
		this.nextToken(); // skip field name
		this.nextToken(); // move to value
		return this.parser.getText();
	}

	public Integer parseInt() throws IOException {
		this.nextToken(); // skip field name
		this.nextToken(); // move to value
		return this.parser.getIntValue();
	}

	public Double parseDouble() throws IOException {
		this.nextToken(); //skip field name
		this.nextToken(); // move to value
		return this.parser.getDoubleValue();
	}

	public <T> T parseObject(Class<T> type) throws IOException {
		this.nextToken(); // skip field name
		this.nextToken(); // skip start object
		T result = this.deserializeObject(type);
		this.nextToken(); // skip end object
		return result;
	}

	public <T> T parseObject(Class<T> type, String fieldName) throws IOException {
		if (this.peekText().equals(fieldName)) return parseObject(type);
		return null;
	}

	public <T> Collection<T> parseCollection(Class<T> type)
			throws IOException {
		switch (JsonSerializable.ascertainSerializableType(type)) {
			case "String":
				return (Collection<T>) this.parseStringCollection();
			case "Integer":
				return (Collection<T>) this.parseIntCollection();
			case "Double":
				return (Collection<T>) this.parseDoubleCollection();
			case "JsonSerializable":
				return this.parseObjectCollection(type);
			default:
				throw new IllegalArgumentException("Type " + type +
					" cannot be deserialized.");
		}
	}

	public <T> Map<Integer, T> parseMap(Class<T> type) throws IOException {
		return parseMap(type, false, null);
	}

	public <T> Map<Integer, T> parseMap(Class<T> type, boolean useTreeMap)
			throws IOException {
		return parseMap(type, useTreeMap, null);
	}

	public <T> Map<Integer, T> parseMap(Class<T> type, boolean useTreeMap,
			Class<?> subType) throws IOException {
		Map<Integer, T> result = new HashMap<>();
		if (useTreeMap)
			result = new TreeMap<>();

		this.nextToken(); // skip field name
		this.nextToken(); // skip start array

		int i = 0;

		while (this.nextToken().equals(JsonToken.START_OBJECT)) {
			T value = (T) parseFieldUnknownType(type, subType);
			result.put(i++, value);

			this.nextToken(); // skip end object
		}

		return result;
	}

	public <T, G> Map<T, G> parseMap(Class<T> typeA, Class<G> typeB)
			throws IOException {
		return parseMap(typeA, typeB, false, null, null);
	}

	public <T, G> Map<T, G> parseMap(Class<T> typeA, Class<G> typeB,
			boolean useTreeMap) throws IOException {
		return parseMap(typeA, typeB, useTreeMap, null, null);
	}

	public <T, G> Map<T, G> parseMap(Class<T> typeA, Class<G> typeB,
			boolean useTreeMap, Class<?> subTypeA, Class<?> subTypeB)
			throws IOException {
		Map<T, G> result = new HashMap<>();
		if (useTreeMap)
			result = new TreeMap<>();

		this.nextToken(); // skip field name
		this.nextToken(); // skip start array

		while (this.nextToken().equals(JsonToken.START_OBJECT)) {
			T key = (T) this.parseFieldUnknownType(typeA, subTypeA);
			G value = (G) this.parseFieldUnknownType(typeB, subTypeB);
			result.put(key, value);

			this.nextToken(); // skip end object
		}

		return result;
	}

	private Collection<String> parseStringCollection() throws IOException {
		Collection<String> result = new ArrayList<>();

		this.nextToken(); // skip field name
		this.nextToken(); // skip start array

		while (!this.nextToken().equals(JsonToken.END_ARRAY))
			result.add(this.parser.getText());

		return result;
	}

	private Collection<Integer> parseIntCollection() throws IOException {
		Collection<Integer> result = new ArrayList<>();

		this.nextToken(); // skip field name
		this.nextToken(); // skip start array

		while (!this.nextToken().equals(JsonToken.END_ARRAY))
			result.add(this.parser.getIntValue());

		return result;
	}

	private Collection<Double> parseDoubleCollection() throws IOException {
		Collection<Double> result = new ArrayList<>();

		this.nextToken(); // skip field name
		this.nextToken(); // skip start array

		while (!this.nextToken().equals(JsonToken.END_ARRAY))
			result.add(this.parser.getDoubleValue());

		return result;
	}

	private <T> Collection<T> parseObjectCollection(Class<T> type)
		throws IOException {
		Collection<T> result = new ArrayList<>();

		this.nextToken(); // skip field name
		this.nextToken(); // skip start array

		while (this.nextToken().equals(JsonToken.START_OBJECT)) {
			T value = this.deserializeObject(type);
			result.add(value);
			this.nextToken(); // skip end object
		}

		return result;
	}

	private Object parseFieldUnknownType(Class<?> type, Class<?> subType)
			throws IOException {
		switch (JsonSerializable.ascertainSerializableType(type)) {
			case "String":
				return this.parseString();
			case "Integer":
				return this.parseInt();
			case "Double":
				return this.parseDouble();
			case "JsonSerializable":
				return this.parseObject(type);
			case "Collection":
				return this.parseCollection(subType);
			default:
				throw new IllegalArgumentException("Type " + type +
					" cannot be deserialized.");
		}
	}

	private <T> T deserializeObject(Class<T> type) {
		try {
			Method deserialize = type.getMethod(
				"deserialize", EnhancedJsonParser.class);
			return (T) deserialize.invoke(null, this);
		} catch (NoSuchMethodException
						 | IllegalAccessException
						 | InvocationTargetException e) {
			throw new RuntimeException("Class " + type + " not deserializable.", e);
		}
	}

	private JsonToken nextToken() throws IOException {
		this.peekParser.nextToken();
		return this.parser.nextToken();
	}

	private JsonToken peekToken() { return this.peekParser.currentToken(); }
	private String peekText() throws IOException {
		return this.peekParser.getText(); }
	//endregion

	//region OBJECT METHODS
	@Override
	public void close() throws IOException {
		this.parser.close();
	}
	//endregion
}
