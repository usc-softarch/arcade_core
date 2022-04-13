package edu.usc.softarch.arcade.topics;

import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.Alphabet;

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.*;

/**
 * TopicCompositionParser is meant to aid in the understanding of what a topic
 * represents. Its primary function is to reverse the typeTopicCounts attribute
 * contained in a Mallet Inferencer. typeTopicCounts represents, for each token,
 * how many times it occurs in a given topic. Here, the aim is to identify, for
 * each topic, which tokens are most prominent.
 *
 * The basis of this class is the way mallet's typeTopicCounts attribute works.
 * Each typeTopicCount is an INT32 (Integer) variable holding both the topic
 * index and the count of how many times a word occurs in that topic. This is
 * done by using binary operations.
 *
 * For example: say typeTopicCounts[15][12] == 270, and there are 100 topics.
 * This means that for the word represented by the index 15, the 12th most
 * prevalent topic is topic number 14 (270 & 127, where 127 is the binary mask
 * 1111111), and that it appears 2 times (270 >> 7, where 7 is the bit length
 * of 127).
 */
public class TopicCompositionParser {
	public static void main(String[] args)
			throws Exception {
		// Load the desired TopicInferencer file
		TopicInferencer inferencer = TopicInferencer.read(new File(args[0]));

		// Get the relevant attributes by reflection
		int[][] typeTopicCounts = getTypeTopicCounts(inferencer);
		List entries = getAlphabetEntries(inferencer);
		int numTopics = getNumTopics(inferencer);
		int topicMask = getTopicMask(inferencer);

		// Reverse the data in TopicInferencer so it suits our needs
		long[][] topicTypeCounts =
			reverseTypeTopicCounts(typeTopicCounts, entries.size(), numTopics, topicMask);

		// Get the 20 most prominent words of each topic
		Map<Integer, List<String>> mostProminentWords = getMostProminentWords(entries, topicTypeCounts);

		try(PrintWriter writer = new PrintWriter(args[1])) {
			for (int topicIndex = 0; topicIndex < mostProminentWords.size(); topicIndex++) {
				List<String> topicWords = mostProminentWords.get(topicIndex);

				writer.println(topicIndex);
				writer.println();
				for (String topicWord : topicWords) {
					writer.println(topicWord);
				}

				writer.println();
			}
		}
	}

	private static Map<Integer, List<String>> getMostProminentWords(List entries, long[][] topicTypeCounts) {
		// Instantiate the result map
		Map<Integer, List<String>> mostProminentWords = new HashMap<>();

		// Get the mask for extracting typeIndices from typeCountIndex
		int typeMask = getTypeMask(entries.size());

		// For each topic, get their index
		for (int topic = 0; topic < topicTypeCounts.length; topic++) {
			// Load the topicTypeCount array for this topic, cast to Long
			Long[] topicTypeCount = Arrays.stream(topicTypeCounts[topic]).boxed().toArray(Long[]::new);
			// Sort in ascending order
			Arrays.sort(topicTypeCount);
			// Cast to a List for further operation
			List<Long> topicTypeCountList = Arrays.asList(topicTypeCount);
			// Reverse into descending order
			Collections.reverse(topicTypeCountList);

			// Instantiate list to hold the most prominent words of the topic
			List<String> topicProminentWords = new ArrayList<>();

			// Get the 20 most prominent words of the topic
			for (int typeCountCell = 0; typeCountCell < 20; typeCountCell++) {
				Long typeCountIndex = topicTypeCountList.get(typeCountCell);
				long typeIndex = typeCountIndex & typeMask;
				topicProminentWords.add(entries.get((int) typeIndex).toString());
			}

			mostProminentWords.put(topic, topicProminentWords);
		}

		return mostProminentWords;
	}

	private static int getTypeMask(int alphabetSize) {
		if (Integer.bitCount(alphabetSize) == 1)
			return alphabetSize - 1;
		else
			return Integer.highestOneBit(alphabetSize) * 2 - 1;
	}

	private static long[][] reverseTypeTopicCounts(
			int[][] typeTopicCounts, int alphabetSize, int numTopics, int topicMask) {
		// Setting up auxiliary variables
		int topicMaskBitLength = Integer.SIZE - Integer.numberOfLeadingZeros(topicMask);
		int typeMaskBitLength = Integer.SIZE - Integer.numberOfLeadingZeros(alphabetSize);

		// Create the matrix to hold the reversed typeTopicCounts
		long[][] topicTypeCounts = new long[numTopics][alphabetSize];

		// For each type (token/word), get its topic distribution
		for (int typeIndex = 0; typeIndex < typeTopicCounts.length; typeIndex++) {
			int[] typeTopicCount = typeTopicCounts[typeIndex];
			// For each topic in the type's distribution, get its representation
			for (int topicCount : typeTopicCount) {
				// Disregard garbage/incomprehensible data from mallet
				if (topicCount == 0) continue;

				// Isolate the lower-end bits to identify which topic is represented
				int topicIndex = topicCount & topicMask;
				// Isolate the higher-end bits to identify the type count for this topic
				int typeCount = topicCount >> topicMaskBitLength;

				// Merge the type count and index into one variable
				long typeCountIndex = (typeCount << typeMaskBitLength) | typeIndex;

				// Register the typeCount
				topicTypeCounts[topicIndex][typeIndex] = typeCountIndex;
			}
		}

		return topicTypeCounts;
	}

	//region REFLECTION ACCESSORS
	private static int getTopicMask(TopicInferencer inferencer)
			throws NoSuchFieldException, IllegalAccessException {
		Class topicInferencerClass = inferencer.getClass();
		Field topicMaskField = topicInferencerClass.getDeclaredField("topicMask");
		topicMaskField.setAccessible(true);
		return (int) topicMaskField.get(inferencer);
	}

	private static int getNumTopics(TopicInferencer inferencer)
			throws NoSuchFieldException, IllegalAccessException {
		Class topicInferencerClass = inferencer.getClass();
		Field numTopicsField = topicInferencerClass.getDeclaredField("numTopics");
		numTopicsField.setAccessible(true);
		return (int) numTopicsField.get(inferencer);
	}

	private static int[][] getTypeTopicCounts(TopicInferencer inferencer)
			throws NoSuchFieldException, IllegalAccessException {
		Class topicInferencerClass = inferencer.getClass();
		Field typeTopicCountsField = topicInferencerClass.getDeclaredField("typeTopicCounts");
		typeTopicCountsField.setAccessible(true);
		return (int[][]) typeTopicCountsField.get(inferencer);
	}

	private static List getAlphabetEntries(TopicInferencer inferencer)
			throws NoSuchFieldException, IllegalAccessException {
		Class topicInferencerClass = inferencer.getClass();
		Field alphabetField = topicInferencerClass.getDeclaredField("alphabet");
		alphabetField.setAccessible(true);
		Alphabet alphInst = (Alphabet) alphabetField.get(inferencer);
		Class alphabetClass = alphInst.getClass();
		Field entriesField = alphabetClass.getDeclaredField("entries");
		entriesField.setAccessible(true);
		return (ArrayList) entriesField.get(alphInst);
	}
	//endregion
}
