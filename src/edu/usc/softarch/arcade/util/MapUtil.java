package edu.usc.softarch.arcade.util;

import java.util.*;

public class MapUtil {
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(
			Map<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(
				map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return (o1.getValue()).compareTo(o2.getValue());
			}
		});

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	public static <K, V> Map<String, V> sortByKeyVersion(
			Map<String, V> map) {
		List<Map.Entry<String, V>> list = new LinkedList<Map.Entry<String, V>>(
				map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<String, V>>() {
			public int compare(Map.Entry<String, V> o1, Map.Entry<String, V> o2) {
				String version1 = o1.getKey();
				String[] parts1 = version1.split("\\.");

				String version2 = o2.getKey();
				String[] parts2 = version2.split("\\.");

				int minLength = parts1.length > parts2.length ? parts2.length
						: parts1.length;
				for (int i = 0; i < minLength; i++) {
					try {
						Integer part1 = Integer.parseInt(parts1[i]);
						Integer part2 = Integer.parseInt(parts2[i]);
						int compareToVal = part1.compareTo(part2);
						if (compareToVal != 0) {
							System.out.println("compareTo " + version1 + " to "
									+ version2 + ": " + compareToVal);
							return compareToVal;
						}
					} catch (NumberFormatException e) {
						System.out
								.println("Invalid part using string comparison for "
										+ version1
										+ " to "
										+ version2
										+ ": "
										+ version1.compareTo(version2));
						return version1.compareTo(version2);
					}
				}
				return version1.compareTo(version2);
			}
		});

		Map<String, V> result = new LinkedHashMap<String, V>();
		for (Map.Entry<String, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;

	}
}
