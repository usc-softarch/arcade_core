package edu.usc.softarch.arcade.util;

import edu.usc.softarch.util.Terminal;

import java.util.HashMap;
import java.util.Map;

// TODO: The arguments are forced to lower cases which causes problems when parsing versions for projects like struts which have upper case version tags.

public class CLI {
	public static Map<String, String> parseArguments(String[] args) {
		Map<String, String> result = new HashMap<>();

		for (String arg : args) {
			// Set Terminal mode
			switch (arg.toLowerCase()) {
				case "quiet":
					Terminal.level = Terminal.Level.OFF;
					continue;
				case "debug":
					Terminal.level = Terminal.Level.DEBUG;
					continue;
				case "info":
					Terminal.level = Terminal.Level.INFO;
					continue;
			}

			// Argument is a value
			if (arg.contains("=")){
				String[] argKeyValue = null;
				if(arg.contains("projversion") || arg.contains("deps") || arg.contains("projpath")){
					argKeyValue = arg.split("=");
				}
				else{
					argKeyValue = arg.toLowerCase().split("=");
				}

				// Message level argument
				if (argKeyValue[0].equals("messagelevel"))
					setMessageLevel(argKeyValue[1]);
				if (argKeyValue[0].equals("messageperiod"))
					Terminal.period = Integer.parseInt(argKeyValue[1]);

				// Component argument
				result.put(argKeyValue[0], argKeyValue[1]);
			}

			// Argument is a flag
			else {
				result.put(arg.toLowerCase(), "true");
			}
		}

		return result;
	}

	private static void setMessageLevel(String argument) {
		switch(argument) {
			case "off":
			case "quiet":
			case "q":
				Terminal.level = Terminal.Level.OFF;
				break;
			case "warn":
			case "w":
				Terminal.level = Terminal.Level.WARN;
				break;
			case "info":
			case "i":
				Terminal.level = Terminal.Level.INFO;
				break;
			case "debug":
			case "d":
				Terminal.level = Terminal.Level.DEBUG;
				break;
			default:
				throw new IllegalArgumentException(
						"Unknown messagelevel " + argument);
		}
	}
}
