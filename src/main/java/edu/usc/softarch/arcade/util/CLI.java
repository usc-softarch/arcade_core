package edu.usc.softarch.arcade.util;

import java.util.HashMap;
import java.util.Map;

public class CLI {
	public static Map<String, String> parseArguments(String[] args) {
		Map<String, String> result = new HashMap<>();

		for (String arg : args) {
			String[] argKeyValue = arg.split("=");
			result.put(argKeyValue[0].toLowerCase(), argKeyValue[1]);
		}

		return result;
	}
}
