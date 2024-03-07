package edu.usc.softarch.arcade.util;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Version implements Comparable<Version> {
	//region ATTRIBUTES
	public enum IncrementType {
		NONE(Integer.MAX_VALUE),
		MAJOR(0),
		MINOR(1),
		PATCH(2),
		PATCHMINOR(3),
		SUFFIX(4);

		private final int value;

		IncrementType(int value) { this.value = value; }

		public int getValue() { return this.value; }

		public static IncrementType parse(int value) {
			switch(value) {
				case -1:
					return NONE;
				case 0:
					return MAJOR;
				case 1:
					return MINOR;
				case 2:
					return PATCH;
				case 3:
					return PATCHMINOR;
				case 4:
					return SUFFIX;
				default:
					throw new IllegalArgumentException();
			}
		}
	}

	public final String versionValue;
	//endregion

	//region CONSTRUCTORS
	public Version(String name) {
		this.versionValue = extractVersion(name); }

	public Version(File file) {
		this.versionValue = extractVersion(file); }
	//endregion

	//region PROCESSING
	public IncrementType getIncrementType(Version otherVersion) {
		String[] v1Parts = this.versionValue.split("\\.");
		String[] v2Parts = otherVersion.versionValue.split("\\.");

		int minLength = Math.min(v1Parts.length, v2Parts.length);
		for (int i = 0; i < minLength; i++) {
			int v1Part = getDigitsBeforeSuffix(v1Parts[i]);
			int v2Part = getDigitsBeforeSuffix(v2Parts[i]);
			if (v1Part != v2Part) return IncrementType.parse(i);
		}

		if (v1Parts.length != v2Parts.length)
			return IncrementType.parse(minLength + 1);

		String v1Suffix = getSuffix(this.versionValue);
		String v2Suffix = getSuffix(otherVersion.versionValue);
		if (v1Suffix == null && v2Suffix == null)
			return IncrementType.NONE;
		return IncrementType.SUFFIX;
	}

	/**
	 * Extracts the version string from the input string.
	 *
	 * @param s the input file
	 * @return the version string, or an empty string if no match was found
	 */
	private static String extractVersion(File s) {
		return extractVersion(s.getName()); }

	/**
	 * Extracts the version string from the input string.
	 *
	 * @param s the input string
	 * @return the version string, or an empty string if no match was found
	 */
	private static String extractVersion(String s) {
		// Define the regular expression pattern to match the version string
		// group(1) returns the first capturing group in the pattern. If the pattern includes parentheses, group(1) returns the part of the string matched by the content within the first set of parentheses.
		String pattern = "(v?\\d+(\\.\\d+)?(\\.\\d+)?(\\.\\d+)?"
			+ "(-?(alpha(\\d+)?|beta(\\d+)?|rc(\\d+)?|pre(\\d+)?)?))";

		// Create a Pattern object
		Pattern r = Pattern.compile(pattern);

		// Now create matcher object.
		Matcher m = r.matcher(s);
		if (m.find()) {
			// Return the matched string
			return m.group(1);
		} else {
			// Return an empty string if no match was found
			return "";
		}
	}

	private static int getDigitsBeforeSuffix(String s) {
		// Use a regular expression to match one or more digits followed by
		// either "alpha", "beta", or "rc"
		Pattern pattern = Pattern.compile("\\d+-?(alpha|beta|rc|pre)");
		Matcher matcher = pattern.matcher(s);
		if (matcher.find()) {
			// Return the group of digits as an int after removing the suffix
			return Integer.parseInt(matcher.group()
				.replaceAll("-?(alpha|beta|rc|pre)", ""));
		} else if (s.matches("\\d+")) {
			// If the input string only contains digits, return it as an int
			return Integer.parseInt(s);
		} else {
			// If no match was found and the input string does not contain only
			// digits, return 0
			return 0;
		}
	}

	private static String getSuffix(String input) {
		for (int i = 0; i < input.length(); i++) {
			char c = input.charAt(i);
			if (Character.isLetter(c)) {
				return input.substring(i);
			}
		}
		return null;
	}

	private static int getSuffixType(String suffix) {
		if (suffix.startsWith("-"))
			suffix = suffix.substring(1);

		if (suffix.startsWith("alpha"))
			return 0;
		else if (suffix.startsWith("beta"))
			return 1;
		else if (suffix.startsWith("pre"))
			return 2;
		else if (suffix.startsWith("rc"))
			return 3;
		return 4;
	}

	private static String getSuffixNumber(String suffix) {
		String[] parts = suffix.split("-?[a-zA-Z]+");
		if (parts.length == 0)
			return "0";
		return parts[1];
	}
	//endregion

	//region OBJECT METHODS
	@Override
	public int compareTo(Version otherVersion) {
		String[] v1Parts = this.versionValue.split("\\.");
		String[] v2Parts = otherVersion.versionValue.split("\\.");

		// Try to sort by the version numbers up to the position of the
		// smallest version pattern.
		int minLength = Math.min(v1Parts.length, v2Parts.length);
		for (int i = 0; i < minLength; i++) {
			int v1Part = getDigitsBeforeSuffix(v1Parts[i]);
			int v2Part = getDigitsBeforeSuffix(v2Parts[i]);
			if (v1Part != v2Part)
				return v1Part - v2Part;
		}

		// If the versions are equal up to the last position of the smallest
		// version pattern, then check whether they are of same length. If not,
		// return the difference of their lengths, such that having an extra
		// position puts a version last.
		if (v1Parts.length != v2Parts.length)
			return v1Parts.length - v2Parts.length;

		// If the versions are of equal length and all numerical positions are
		// equal, then check if either has a suffix. If neither does, they are
		// equal. If one does and the other does not, the one with a suffix is
		// placed last.
		String v1Suffix = getSuffix(v1Parts[v1Parts.length - 1]);
		String v2Suffix = getSuffix(v2Parts[v2Parts.length - 1]);
		if (v1Suffix == null && v2Suffix == null)
			return 0;
		else if (v1Suffix == null)
			return 1;
		else if (v2Suffix == null)
			return -1;

		// If both versions have a suffix, check their types. If they are different,
		// return their difference, such that alpha comes before beta, beta comes
		// pre, and pre comes before rc.
		int v1SuffixType = getSuffixType(v1Suffix);
		int v2SuffixType = getSuffixType(v2Suffix);
		if (v1SuffixType != v2SuffixType)
			return v1SuffixType - v2SuffixType;

		// If both versions have the same suffix, compare the numbers succeeding
		// them. If either one has no number, it will come first. If both have
		// numbers, return their difference.
		String v1SuffixNumber = getSuffixNumber(v1Suffix);
		String v2SuffixNumber = getSuffixNumber(v2Suffix);
		if (v1SuffixNumber.equals(v2SuffixNumber))
			return 0;
		return Integer.parseInt(v1SuffixNumber) - Integer.parseInt(v2SuffixNumber);
	}

	@Override
	public String toString() { return this.versionValue; }
	//endregion
}
