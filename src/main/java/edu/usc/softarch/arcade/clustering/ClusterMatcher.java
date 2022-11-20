package edu.usc.softarch.arcade.clustering;

import edu.usc.softarch.arcade.clustering.data.ReadOnlyArchitecture;
import edu.usc.softarch.arcade.clustering.data.ReadOnlyCluster;
import edu.usc.softarch.arcade.topics.DocTopics;
import edu.usc.softarch.arcade.util.CLI;
import edu.usc.softarch.arcade.util.McfpDriver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ClusterMatcher {
	//region PUBLIC INTERFACE
	/**
	 * mode  = archonly -> only match two RSFs
	 *         full     -> match and rename the entire folder
	 * arch1 = archonly -> RSF file for the first architecture
	 *         full     -> root directory of the first version
	 * arch2 = archonly -> RSF file for the second architecture
	 *         full     -> root directory of the second version
	 */
	public static void main(String[] args) throws IOException {
		Map<String, String> parsedArgs = CLI.parseArguments(args);
		run(parsedArgs.get("mode"),
			parsedArgs.get("arch1"), parsedArgs.get("arch2"));
	}

	public static void run(String mode, String arch1, String arch2)
			throws IOException {
		ClusterMatcher matcher = new ClusterMatcher(mode, arch1, arch2);
		matcher.matchArchFile();

		if (matcher.mode.equals("full")) {
			try {
				matcher.matchDocTopics();
				matcher.matchConcerns();
			} catch (IOException e) {
				// Not ARC, so move on
			}

			matcher.matchDotFiles();
		}
	}
	//endregion

	//region ATTRIBUTES
	public final String mode;
	public final String arch1path;
	public final String arch2path;
	private final ReadOnlyArchitecture arch1;
	private final ReadOnlyArchitecture arch2;
	private final Map<String, String> matches;
	//endregion

	//region CONSTRUCTORS
	public ClusterMatcher(String mode, String arch1path, String arch2path)
			throws IOException {
		this.mode = mode.toLowerCase();
		this.arch1path = arch1path;
		this.arch2path = arch2path;

		if (this.mode.equals("archonly")) {
			this.arch1 = ReadOnlyArchitecture.readFromRsf(this.arch1path);
			this.arch2 = ReadOnlyArchitecture.readFromRsf(this.arch2path);
		} else if (this.mode.equals("full")) {
			this.arch1 =
				ReadOnlyArchitecture.readFromRsf(getArchFile(this.arch1path));
			this.arch2 =
				ReadOnlyArchitecture.readFromRsf(getArchFile(this.arch2path));
		} else
			throw new IllegalArgumentException("Unknown mode " + this.mode);

		McfpDriver mcfpDriver = new McfpDriver(this.arch1, this.arch2);
		this.matches = mcfpDriver.getMatchSet();
		match();
	}
	//endregion

	//region ACCESSORS
	private File getArchFile(String archPath) {
		return
			// List all files under this directory
			Arrays.stream((new File(archPath)).listFiles())
				// Get all files that end in _clusters.rsf
				.filter(f -> f.getName().matches(".*_clusters\\.rsf"))
				// Since there should be only one file, get that one
				.findFirst().get();
	}

	private File getDocTopicsFile(String archPath) throws IOException {
		try {
			return
				// List all files under this directory
				Arrays.stream((new File(archPath)).listFiles())
					// Get all files that end in _clusteredDocTopics.json
					.filter(f -> f.getName().matches(".*_clusteredDocTopics\\.json"))
					// Since there should be only one file, get that one
					.findFirst().get();
		} catch (NoSuchElementException e) {
			throw new IOException(e);
		}
	}

	private File getConcernsFile(String archPath) throws IOException {
		try {
			return
				// List all files under this directory
				Arrays.stream((new File(archPath)).listFiles())
					// Get all files that end in _concerns.txt
					.filter(f -> f.getName().matches(".*_concerns\\.txt"))
					// Since there should be only one file, get that one
					.findFirst().get();
		} catch (NoSuchElementException e) {
			throw new IOException(e);
		}
	}

	private Collection<File> getDotFiles(String archPath) {
		File dotPath = Arrays.stream(new File(archPath).listFiles())
			.filter(f -> f.getName().matches(".*_clusterDots"))
			.findFirst().get();

		return
			// List all files under this directory
			Arrays.stream(dotPath.listFiles())
				// Get all files that end in _concerns.txt
				.filter(f -> f.getName().matches(".*\\.dot"))
				.collect(Collectors.toList());
	}
	//endregion

	//region PROCESSING
	private void match() {
		Map<String, ReadOnlyCluster> newClusters = new HashMap<>();

		// For each MCFP match...
		for (Map.Entry<String, String> matchEntry : this.matches.entrySet()) {
			// ... get the relevant cluster from arch2,...
			ReadOnlyCluster cluster = this.arch2.get(matchEntry.getValue());
			// ... make a new cluster with the name from arch1...
			ReadOnlyCluster newCluster = new ReadOnlyCluster(matchEntry.getKey(),
				cluster.getEntities(), cluster.getDocTopicItem());
			// ... and add it to the newClusters map.
			newClusters.put(newCluster.name, newCluster);
		}

		// Then, delete all clusters from arch2...
		this.arch2.clear();
		// ... and add the renamed ones instead.
		this.arch2.putAll(newClusters);
	}
	//endregion

	//region SERIALIZATION
	public void matchArchFile() throws FileNotFoundException {
		File archFile;

		if (this.mode.equals("archonly"))
			archFile = new File(this.arch2path);
		else
			archFile = getArchFile(this.arch2path);

		// Easier to delete and rewrite than parse the whole file
		archFile.delete();
		this.arch2.writeToRsf(archFile);
	}

	public void matchDocTopics() throws IOException {
		// Load the unmatched DocTopics file
		File docTopicsFile = getDocTopicsFile(this.arch2path);
		DocTopics docTopics = DocTopics.deserialize(docTopicsFile.getPath());

		// Temporarily rename them all to avoid duplicate name issues
		for (String clusterName : this.matches.keySet())
			docTopics.renameDocTopicItem(
				docTopics.getDocTopicItem(clusterName), clusterName + "aux");

		// Finally, rename them to the correct name
		for (Map.Entry<String, String> entry : this.matches.entrySet())
			docTopics.renameDocTopicItem(
				docTopics.getDocTopicItem(entry.getValue() + "aux"),
				entry.getKey());

		// Easier to delete and rewrite than parse the whole file
		docTopicsFile.delete();
		docTopics.serialize(docTopicsFile.getPath());
	}

	public void matchConcerns() throws IOException {
		Map<String, String> concerns = new HashMap<>();
		File concernsFile = getConcernsFile(this.arch2path);

		// Read in the existing concerns file
		try (BufferedReader br = new BufferedReader(new FileReader(concernsFile))) {
			String line;

			// Read the concerns file into the map
			while ((line = br.readLine()) != null) {
				if (line.trim().isEmpty()) continue; // Sanity check

				Pattern pattern = Pattern.compile("(^\\d*)(.*)");
				Matcher matcher = pattern.matcher(line);
				matcher.find();

				concerns.put(matcher.group(1) + "aux", matcher.group(2));
			}
		}

		// Reassign the cluster numbers
		Map<String, String> reConcerns = new HashMap<>();
		for (Map.Entry<String, String> entry : this.matches.entrySet())
			reConcerns.put(entry.getKey(), concerns.get(entry.getValue() + "aux"));

		// Delete and re-write
		concernsFile.delete();
		try (FileWriter writer = new FileWriter(concernsFile)) {
			for (Map.Entry<String, String> entry : reConcerns.entrySet())
				writer.write(entry.getKey() + entry.getValue() + "\n");
		}
	}

	public void matchDotFiles() throws IOException {
		Collection<File> dotFiles = getDotFiles(this.arch2path);

		for (Map.Entry<String, String> matchEntry : matches.entrySet()) {
			if (matchEntry.getKey().equals(matchEntry.getValue())) continue;

			File currentFile = dotFiles.stream()
				.filter(f -> f.getName().equals(matchEntry.getValue() + ".dot"))
				.findFirst().get();

			String fileContent;
			try (Scanner sc = new Scanner(currentFile)) {
				fileContent = sc.useDelimiter("\\Z").next();
			} catch (FileNotFoundException e) {
				throw new IOException(e);
			}

			// Replace the name of the digraph
			fileContent = fileContent.replaceAll("digraph \"\\d*\"",
				"digraph \"" + matchEntry.getKey() + "\"");
			// Replace the subgraph name
			fileContent = fileContent.replaceAll("subgraph \"cluster_\\d*\"",
				"subgraph \"cluster_" + matchEntry.getKey() + "\"");
			// Replace label
			fileContent = fileContent.replaceAll("\"Cluster: \\d*",
				"\"Cluster: " + matchEntry.getKey());

			currentFile.delete();
			File newFile = new File(currentFile.getPath()
				.replace(".dot", "aux.dot"));
			try (FileWriter writer = new FileWriter(newFile)) {
				writer.write(fileContent);
			}

			dotFiles.remove(currentFile);
			dotFiles.add(newFile);
		}

		// All have been aux-renamed and re-written, now to fix the names
		for (Map.Entry<String, String> matchEntry : matches.entrySet()) {
			if (matchEntry.getKey().equals(matchEntry.getValue())) continue;

			File currentFile = dotFiles.stream()
				.filter(f -> f.getName().equals(matchEntry.getValue() + "aux.dot"))
				.findFirst().get();
			currentFile.renameTo(
				new File(currentFile.getParentFile().getPath()
					+ File.separator + matchEntry.getKey() + ".dot"));
		}
	}
	//endregion
}
