package edu.usc.softarch.util.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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
		this(new File(filePath));
	}

	public EnhancedJsonParser(File file) throws IOException {
		JsonFactory factory = new JsonFactory();

		this.parser = factory.createParser(file);
		this.peekParser = factory.createParser(file);

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

	private Collection<String> parseStringCollection() throws IOException {
		Collection<String> result = new ArrayList<>();

		this.nextToken(); // skip field name
		this.nextToken(); // skip start array

		while (!this.nextToken().equals(JsonToken.END_ARRAY))
			result.add(this.parser.getText());

		return result;
	}

	public String[] parseStringArray() throws IOException {
		return parseStringCollection().toArray(new String[0]); }

	public String[][] parseStringMatrix() throws IOException {
		List<Collection<String>> resultAux = new ArrayList<>();

		this.nextToken(); // skip field name
		this.nextToken(); // skip start array

		while (this.nextToken().equals(JsonToken.START_ARRAY)) {
			Collection<String> array = new ArrayList<>();

			while (!this.nextToken().equals(JsonToken.END_ARRAY))
				array.add(this.parser.getText());

			resultAux.add(array);
		}

		String[][] result = new String[resultAux.size()][];

		for (int i = 0; i < result.length; i++)
			result[i] = resultAux.get(i).toArray(new String[0]);

		return result;
	}

	public Integer parseInt() throws IOException {
		this.nextToken(); // skip field name
		this.nextToken(); // move to value
		return this.parser.getIntValue();
	}

	private Collection<Integer> parseIntCollection() throws IOException {
		Collection<Integer> result = new ArrayList<>();

		this.nextToken(); // skip field name
		this.nextToken(); // skip start array

		while (!this.nextToken().equals(JsonToken.END_ARRAY))
			result.add(this.parser.getIntValue());

		return result;
	}

	public int[] parseIntArray() throws IOException {
		return this.unboxIntArray(parseIntCollection().toArray(new Integer[0])); }

	public int[][] parseIntMatrix() throws IOException {
		List<Collection<Integer>> resultAux = new ArrayList<>();

		this.nextToken(); // skip field name
		this.nextToken(); // skip start array

		while (this.nextToken().equals(JsonToken.START_ARRAY)) {
			Collection<Integer> array = new ArrayList<>();

			while (!this.nextToken().equals(JsonToken.END_ARRAY))
				array.add(this.parser.getIntValue());

			resultAux.add(array);
		}

		int[][] result = new int[resultAux.size()][];

		for (int i = 0; i < result.length; i++)
			result[i] = this.unboxIntArray(resultAux.get(i).toArray(new Integer[0]));

		return result;
	}

	public Double parseDouble() throws IOException {
		this.nextToken(); //skip field name
		this.nextToken(); // move to value
		return this.parser.getDoubleValue();
	}

	private Collection<Double> parseDoubleCollection() throws IOException {
		Collection<Double> result = new ArrayList<>();

		this.nextToken(); // skip field name
		this.nextToken(); // skip start array

		while (!this.nextToken().equals(JsonToken.END_ARRAY))
			result.add(this.parser.getDoubleValue());

		return result;
	}

	public double[] parseDoubleArray() throws IOException {
		return this.unboxDoubleArray(parseDoubleCollection().toArray(new Double[0]));
	}

	public double[][] parseDoubleMatrix() throws IOException {
		List<Collection<Double>> resultAux = new ArrayList<>();

		this.nextToken(); // skip field name
		this.nextToken(); // skip start array

		while (this.nextToken().equals(JsonToken.START_ARRAY)) {
			Collection<Double> array = new ArrayList<>();

			while (!this.nextToken().equals(JsonToken.END_ARRAY))
				array.add(this.parser.getDoubleValue());

			resultAux.add(array);
		}

		double[][] result = new double[resultAux.size()][];

		for (int i = 0; i < result.length; i++)
			result[i] = this.unboxDoubleArray(resultAux.get(i).toArray(new Double[0]));

		return result;
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

	private int[] unboxIntArray(Integer[] array) {
		return Arrays.stream(array).mapToInt(Integer::intValue).toArray(); }
	private double[] unboxDoubleArray(Double[] array) {
		return Arrays.stream(array).mapToDouble(Double::doubleValue).toArray(); }
	//endregion

	//region OBJECT METHODS
	@Override
	public void close() throws IOException {
		this.parser.close();
	}
	//endregion
}
