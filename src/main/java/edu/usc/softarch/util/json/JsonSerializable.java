package edu.usc.softarch.util.json;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Classes that implement this interface should also implement a public static
 * T deserialize(EnhancedJsonParser parser) method, where T is the class type.
 * Not doing so will cause errors if one attempts deserialization.
 */
public interface JsonSerializable {
	void serialize(EnhancedJsonGenerator generator)
		throws IOException;

	static String ascertainSerializableType(Class<?> valueClass) {
		try {
			switch (valueClass.toString()) {
				case "class java.lang.String":
					return "String";
				case "class java.lang.Integer":
					return "Integer";
				case "class java.lang.Double":
					return "Double";
				case "class java.lang.Object":
					throw new IllegalArgumentException("Non-serializable type " + valueClass);
				default:
					try {
						Collection<String> interfaces =
							Arrays.stream(valueClass.getInterfaces()).
								map(Class::toString).collect(Collectors.toList());

						if (interfaces.contains(
							"interface edu.usc.softarch.util.json.JsonSerializable"))
							return "JsonSerializable";
						if (interfaces.contains(
							"interface java.util.Map"))
							return "Map";
						if (interfaces.contains(
							"interface java.util.Collection"))
							return "Collection";

						return ascertainSerializableType(valueClass.getSuperclass());
					} catch (IllegalArgumentException e) {
						throw new IllegalArgumentException("Non-serializable type " + valueClass);
					}
			}
		} catch (NullPointerException e) {
			throw new RuntimeException();
		}
	}
}
