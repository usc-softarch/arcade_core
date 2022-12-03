package edu.usc.softarch.arcade.antipattern.detection.coupling;

import edu.usc.softarch.arcade.util.CLI;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Map;

public class CodeMaatHelper {
	//region PUBLIC INTERFACE
	/**
	 * input : Path to the input, either a file or a directory.
	 * output : Path to place the output, either a file or a directory.
	 * language : Language of the subject system.
	 * replacement : String to replace removed prefixes with.
	 * filelevel : Boolean indicating whether the output should be at file level
	 * 	or class level.
	 * prefixes : Prefixes to filter entries by. If multiple values are used,
	 *  they should be separated by commas.
	 */
	public static void main(String[] args) throws IOException {
		Map<String, String> parsedArguments = CLI.parseArguments(args);
		String input = parsedArguments.get("input");
		String output = parsedArguments.get("output");

		if (isBatch(input, output))
			batchRun(input, output, parsedArguments.get("language"),
				parsedArguments.get("replacement"), parsedArguments.get("filelevel"),
				parsedArguments.get("prefixes"));
		else
			run(input, output, parsedArguments.get("language"),
				parsedArguments.get("replacement"), parsedArguments.get("filelevel"),
				parsedArguments.get("prefixes"));
	}

	private static boolean isBatch(String input, String output) {
		File inputFile = new File(input);
		File outputFile = new File(output);

		if (inputFile.isDirectory() && outputFile.isDirectory())
			return true;
		if (inputFile.isFile() && outputFile.isFile())
			return false;
		throw new IllegalArgumentException("Mismatched input and output types: "
			+ "both input and output paths should be either files or directories.");
	}

	public static void batchRun(String input, String output, String language)
			throws IOException {
		batchRun(input, output, language, "");
	}

	public static void batchRun(String input, String output, String language,
			String replacement, String... prefix) throws IOException {
		batchRun(input, output, language, replacement, true, prefix);
	}

	public static void batchRun(String input, String output, String language,
			String replacement, boolean fileLevel, String... prefix)
			throws IOException {
		File[] inputFiles = new File(input).listFiles();
		for (File inputFile : inputFiles) {
			// If the file isn't a CSV, skip
			if (!inputFile.getName().endsWith("csv")) continue;

			String inputPath = inputFile.getAbsolutePath();
			String outputPath = output + File.separator
				+ inputFile.getName().split("\\.")[0] + "_clean.csv";
			run(inputPath, outputPath, language, replacement, fileLevel, prefix);
		}
	}

	public static void run(String input, String output, String language)
			throws IOException {
		String result = new CodeMaatHelper(language)
			.processFile(input);
		try (PrintWriter writer = new PrintWriter(output)) {
			writer.println(result);
		}
	}

	public static void run(String input, String output, String language,
			String replacement, String... prefix) throws IOException {
		String result = new CodeMaatHelper(language, replacement, prefix)
			.processFile(input);
		try (PrintWriter writer = new PrintWriter(output)) {
			writer.println(result);
		}
	}

	public static void run(String input, String output, String language,
			String replacement, boolean fileLevel, String... prefix)
			throws IOException {
		String result = new CodeMaatHelper(language,replacement, fileLevel, prefix)
			.processFile(input);
		try (PrintWriter writer = new PrintWriter(output)) {
			writer.println(result);
		}
	}
	//endregion

	//region ATTRIBUTES
	private final String language;
	private final String replacement;
	private final String[] prefixes;
	private final boolean fileLevel;
	//endregion

	//region CONSTRUCTORS
	public CodeMaatHelper(String language) { this(language, ""); }

	public CodeMaatHelper(String language, String replacement, String... prefix) {
		this(language, replacement, true, prefix); }

	public CodeMaatHelper(String language, String replacement,
			boolean fileLevel, String... prefix) {
		this.language = language;
		this.fileLevel = fileLevel;
		this.replacement = replacement != null ? replacement : "";
		this.prefixes = prefix != null ? prefix : new String[0];
	}
	//endregion

	//region PROCESSING
	public String processFile(String input)
			throws IOException {
		return this.processFile(new File(input)); }

	public String processFile(File input)
			throws IOException {
		StringBuilder result = new StringBuilder();

		try (BufferedReader reader = new BufferedReader(new FileReader(input))) {
			// Read the header
			result.append(reader.readLine());

			String line;
			while ((line = reader.readLine()) != null) {
				String entry = this.processEntry(line.split(","));
				if (entry != null) result.append(entry);
			}
		}

		return result.toString();
	}

	private String processEntry(String[] entry) {
		if (entry.length != 4)
			throw new IllegalArgumentException(
				"Invalid entry " + Arrays.toString(entry));

		switch (this.language.toLowerCase()) {
			case "java":
				return this.processJavaEntry(entry);
			case "c":
			case "cpp":
				return this.processCEntry(entry);
			case "python":
				return this.processPythonEntry(entry);
			default:
				throw new IllegalArgumentException(
					"Unsupported language " + this.language);
		}
	}

	private String processJavaEntry(String[] entry) {
		// Irrelevant entry
		if (!entry[0].endsWith(".java") && !entry[1].endsWith(".java"))
			return null;

		String source = this.parseEntity(
			entry[0].replace(".java", ""));
		String target = this.parseEntity(
			entry[1].replace(".java", ""));

		if (source == null || target == null) return null;

		return System.lineSeparator() + source + "," + target
			+ "," + entry[2] + "," + entry[3];
	}

	private String processCEntry(String[] entry) {
		throw new UnsupportedOperationException(); }

	private String processPythonEntry(String[] entry) {
		throw new UnsupportedOperationException(); }

	private String parseEntity(String entity) {
		String result = "";
		if (this.prefixes.length == 0) result = entity;

		for (String prefix : this.prefixes) {
			if (entity.contains(prefix)) {
				result = this.replacement + entity.split(prefix)[1];
				break;
			}
		}

		// Short-circuit if not a match to any given prefixes
		if (this.prefixes.length != 0 && result.isEmpty()) return null;

		return this.fileLevel
			? result.replace(".", "/")
			: result.replace("/", ".");
	}
	//endregion
}
