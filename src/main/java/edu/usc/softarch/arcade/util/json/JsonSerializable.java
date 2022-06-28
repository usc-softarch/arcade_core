package edu.usc.softarch.arcade.util.json;

import edu.usc.softarch.arcade.util.json.EnhancedJsonGenerator;

import java.io.IOException;

/**
 * Classes that implement this interface should also implement a public static
 * T deserialize(EnhancedJsonParser parser) method, where T is the class type.
 * Not doing so will cause errors if one attempts deserialization.
 */
public interface JsonSerializable {
	void serialize(EnhancedJsonGenerator generator) throws IOException;

	static String ascertainSerializableType(Class<?> valueClass) {
		switch (valueClass.toString()) {
			case "java.lang.String":
				return "String";
			case "java.lang.Integer":
				return "Integer";
			case "java.lang.Double":
				return "Double";
			case "java.util.Collection":
				return "Collection";
			case "java.util.Map":
				return "Map";
			case "edu.usc.softarch.arcade.util.json.JsonSerializable":
				return "JsonSerializable";
			case "java.lang.Object":
				throw new IllegalArgumentException("Non-serializable type " + valueClass);
			default:
				try {
					return ascertainSerializableType(valueClass.getSuperclass());
				} catch (IllegalArgumentException e) {
					throw new IllegalArgumentException("Non-serializable type " + valueClass);
				}
		}
	}
}
