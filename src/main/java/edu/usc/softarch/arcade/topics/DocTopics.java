package edu.usc.softarch.arcade.topics;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import edu.usc.softarch.arcade.clustering.data.Cluster;
import edu.usc.softarch.arcade.topics.exceptions.UnmatchingDocTopicItemsException;
import edu.usc.softarch.arcade.util.CLI;
import edu.usc.softarch.util.EnhancedHashSet;
import edu.usc.softarch.util.EnhancedSet;
import edu.usc.softarch.util.Terminal;
import edu.usc.softarch.util.json.EnhancedJsonGenerator;
import edu.usc.softarch.util.json.EnhancedJsonParser;
import edu.usc.softarch.util.json.JsonSerializable;

/**
 * Representation of a collection of document topic distributions, or
 * {@link DocTopicItem}s. DocTopics are singletons bound to the name and version
 * of the system they represent. Modifications to a DocTopics object, including
 * its component {@link DocTopicItem}s, must be done through the DocTopics
 * itself; this guarantees that changes are reflected globally regardless of
 * where they are initiated from.
 *
 * @see MalletRunner
 * @see TopicCompositionParser
 * @see DocTopicItem
 */
public class DocTopics implements JsonSerializable {
	//region PUBLIC INTERFACE
	/**
	 * Entry point to pre-generate DocTopics, or to debug large DocTopics files
	 * that can't be opened in text editors. Defaults to generate mode.
	 *
	 * mode: "generate" to create a new docTopics file
	 *       "debug" to run the debugging utility
	 *
	 * Each mode has its own arguments.
	 *
	 * @see #runGeneration(String, String, String, boolean, boolean)
	 * @see #runDebugger(String, String)
	 */
	public static void main(String[] args) throws IOException {
		Map<String, String> parsedArgs = CLI.parseArguments(args);

		if (!parsedArgs.containsKey("mode")) runGeneration(parsedArgs);

		switch (parsedArgs.get("mode")) {
			case "generate":
				runGeneration(parsedArgs);
				break;

			case "debug":
				String docTopicsPath = parsedArgs.get("path");
				String filter = "";
				if (parsedArgs.containsKey("filter"))
					filter = parsedArgs.get("filter");
				runDebugger(docTopicsPath, filter);
				break;

			default:
				throw new IllegalArgumentException(
					"Unknown DocTopics mode " + parsedArgs.get("mode"));
		}
	}

	public static void runGeneration(Map<String, String> args) {
		String artifactsPath = args.get("artifacts");
		String projectName = args.get("project");
		boolean fileLevel = Boolean.parseBoolean(args.get("filelevel"));
		boolean overwrite = false;
		if (args.containsKey("overwrite"))
			overwrite = Boolean.parseBoolean(args.get("overwrite"));
		String projectVersion = "All";
		if (args.containsKey("version"))
			projectVersion = args.get("version");
		runGeneration(artifactsPath, projectName,
			projectVersion, fileLevel, overwrite);
	}

	public static void runGeneration(String artifactsPath, String projectName,
			boolean fileLevel, boolean overwrite) {
		runGeneration(artifactsPath, projectName,
			"All", fileLevel, overwrite);
	}

	/**
	 * Generates a DocTopics file from existing artifacts.
	 *
	 * artifacts: Path to the artifacts directory.
	 * project: Name of the project being analyzed.
	 * version: Version of the project being analyzed.
	 * filelevel: Whether the artifacts were obtained from file or class level
	 *            dependencies.
	 * overwrite: Whether to overwrite existing docTopics.json.
	 */
	public static void runGeneration(String artifactsPath, String projectName,
			String projectVersion, boolean fileLevel, boolean overwrite) {
		File docTopicsFile =
			new File(artifactsPath + File.separator + "docTopics.json");

		// Assert permission to overwrite, if file already exists
		if (docTopicsFile.exists()) {
			if (overwrite)
				Terminal.timePrint(
					"Overwrite flag set to True, overwriting " + docTopicsFile + ".",
					Terminal.Level.INFO);
			else
				throw new AssertionError(
					"Overwrite flag not set or set to False, cannot overwrite"
						+ " existing file " + docTopicsFile.getAbsolutePath());
		}

		Terminal.timePrint("Generating DocTopics.", Terminal.Level.INFO);

		try {
			DocTopics.initializeSingleton(
				artifactsPath, projectName, projectVersion, fileLevel);
			DocTopics.getSingleton(projectName, projectVersion)
				.serialize(artifactsPath + File.separator + "docTopics.json");
		} catch (Exception f) {
			f.printStackTrace();
		}
	}

	public static void runDebugger(String docTopicsPath) throws IOException {
		runDebugger(docTopicsPath, "");
	}

	/**
	 * Debugging utility for DocTopics. Currently just reads the file in; use a
	 * debugger to analyze the file contents by placing a breakpoint at the
	 * return statement.
	 *
	 * path: Path to the docTopics.json file.
	 * filter: Filter to apply to the DocTopicItem sources.
	 */
	public static void runDebugger(String docTopicsPath, String filter)
			throws IOException {
		Map<String, DocTopicItem> docTopics =
			DocTopics.deserialize(docTopicsPath).dtItemList;

		if (!filter.isEmpty())
			docTopics = docTopics.entrySet().stream()
				.filter(dti -> dti.getKey().contains(filter))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		return;
	}
	//endregion

	//region ATTRIBUTES
	/**
	 * Holds a static map of all analyzed system versions to their respective
	 * DocTopics singletons. While this currently has no direct use outside
	 * unit tests, it is a requirement for future parallelization.
	 */
	private static final Map<String, DocTopics> mingleton = new HashMap<>();
	/**
	 * The name of the project to which this DocTopics instance is bound.
	 */
	private final String projectName;
	/**
	 * The version of the project to which this DocTopics instace is bound.
	 */
	private final String projectVersion;
	private final boolean fileLevel;
	/**
	 * A map of each {@link DocTopicItem}'s source entity to the
	 * {@link DocTopicItem} itself.
	 */
	private final Map<String, DocTopicItem> dtItemList;
	/**
	 * A map of topic numbers to a list of the N most representative words in
	 * that topic.
	 *
	 * @see TopicCompositionParser
	 */
	private Map<Integer, List<String>> topicWordLists;
	//endregion
	
	//region CONSTRUCTORS
	/**
	 * Deserialization constructor.
	 */
	private DocTopics(String projectName, String projectVersion,
			boolean fileLevel) {
		this.projectName = projectName;
		this.projectVersion = projectVersion;
		this.fileLevel = fileLevel;
		this.dtItemList = new HashMap<>();
		this.topicWordLists = new TreeMap<>();
	}

	/**
	 * Initialization constructor. Creates a new DocTopics instance based on a
	 * MALLET {@link InstanceList} and {@link TopicInferencer}.
	 *
	 * This constructor is bound to the insertion order of the
	 * {@link InstanceList}; this is due to the use of a single random seed in
	 * {@link TopicInferencer#getSampledDistribution(Instance, int, int, int)}
	 * throughout all of its executions. While it is one of two critical
	 * bottlenecks in ARC, it cannot safely be parallelized at this time.
	 *
	 * @param artifactsDir Directory path containing a "vectors" and "topicmodel"
	 *                     files generated by MALLET.
	 * @param projectName The project name for which this is being constructed.
	 * @param projectVersion The project version for which this is being
	 *                			 constructed.
	 * @throws Exception Unknown precisely what this exception is related to due
	 * 									 to lacking documentation and use of raw Exception type
	 * 									 in MALLET. It is declared by
	 * 									 {@link TopicInferencer#read(File)}.
	 * @see MalletRunner
	 */
	private DocTopics(String artifactsDir, String projectName,
			String projectVersion, boolean fileLevel) throws Exception {
		this(projectName, projectVersion, fileLevel);
		// Begin by importing documents from text to feature sequences
		char fs = File.separatorChar;
		InstanceList instances = InstanceList.load(
			new File(artifactsDir + fs + "vectors"));
		TopicInferencer topicmodel = TopicInferencer.read(
			new File(artifactsDir + fs + "topicmodel"));

		// Load TopicItems
		for (Instance previousInstance : instances) {
			DocTopicItem dtItem = new DocTopicItem(
				previousInstance.getSource().toString());

			double[] topicDistribution = topicmodel.getSampledDistribution(
					previousInstance, 1000, 10, 10);

			for (int topicIdx = 0; topicIdx < topicDistribution.length; topicIdx++) {
				TopicItem t = new TopicItem(topicIdx, topicDistribution[topicIdx]);
				dtItem.addTopic(t);
			}
			addDocTopicItem(dtItem);
		}

		// Load WordLists
		TopicCompositionParser parser = new TopicCompositionParser(topicmodel);
		this.topicWordLists = parser.run();
	}

	/**
	 * Initializes a singleton instance of DocTopics. Serves as a sort of public
	 * constructor to a DocTopics object. All DocTopics singletons must be
	 * initialized either through this method or {@link #deserialize(String)}.
	 *
	 * @param artifactsDir Path to the directory containing a vectors and
	 *                     topicmodel files generated by MALLET.
	 * @param projectName Name of the project for which singleton is to
	 *                		be initialized.
	 * @param projectVersion Version of the project for which singleton is to
	 *                       be initialized.
	 * @throws Exception It is unknown exactly what causes this exception.
	 * 									 See {@link #DocTopics(String, String, boolean)}.
	 */
	public static void initializeSingleton(String artifactsDir,
			String projectName, String projectVersion, boolean fileLevel)
			throws Exception {
		mingleton.put(projectName.toLowerCase() + "-" + projectVersion,
			new DocTopics(artifactsDir, projectName, projectVersion, fileLevel));
	}

	/**
	 * Resets ALL singleton instances from this process. This method should almost
	 * NEVER be used unless a full restart of the entire process is desired.
	 * Should a thread use this method, all other threads will be jeopardized.
	 */
	public static void resetSingleton() { mingleton.clear(); }

	/**
	 * Resets the requested singleton.
	 *
	 * @param projectName Key project name of the singleton to reset.
	 * @param projectVersion Key project version of the singleton to reset.
	 */
	public static void resetSingleton(String projectName, String projectVersion) {
		mingleton.remove(projectName.toLowerCase() + "-" + projectVersion); }
	//endregion

	//region ACCESSORS
	/**
	 * Validates whether the given DocTopics singleton has been initialized
	 * and is ready to be accessed.
	 *
	 * @param projectName Key project version of the singleton to validate.
	 * @param projectVersion Key project version of the singleton to validate.
	 * @return True if the singleton exists and is not empty, False otherwise.
	 * @see #getSingleton(String, String)
	 */
	public static boolean isReady(String projectName, String projectVersion) {
		return mingleton.get(projectName.toLowerCase() + "-" + projectVersion) != null
			&& !mingleton.get(projectName.toLowerCase() + "-" + projectVersion).dtItemList.isEmpty();
	}

	/**
	 * Gets a DocTopics singleton instance.
	 *
	 * IMPORTANT NOTE: The DocTopics returned by this method should NOT be kept
	 * by the caller. To do so endangers the integrity of the singleton. If for
	 * any reason a local read-only copy is required, use {@link #getCopy()}
	 * instead.
	 *
	 * @param projectName The project name used to key the desired DocTopics
	 *                    instance.
	 * @param projectVersion The project version used to key the desired
	 *                			 DocTopics instance.
	 * @return The requested singleton instance.
	 * @throws IllegalStateException Thrown if the requested singleton has not
	 * 															 been initialized.
	 * @see #initializeSingleton(String, String, String, boolean)
	 * @see #deserialize(String)
	 * @see #isReady(String, String)
	 */
	public static DocTopics getSingleton(String projectName,
			String projectVersion) throws IllegalStateException {
		if (mingleton.get(projectName.toLowerCase() + "-" + projectVersion) == null)
			throw new IllegalStateException("DocTopics must be initialized.");

		return mingleton.get(projectName.toLowerCase() + "-" + projectVersion);
	}

	/**
	 * Inserts a {@link DocTopicItem} into this DocTopics.
	 *
	 * @param dti The {@link DocTopicItem} to be inserted.
	 * @throws IllegalArgumentException if there already exists a
	 * 																	{@link DocTopicItem} with the same
	 * 																	{@link DocTopicItem#source} as the given
	 * 																	argument.
	 */
	void addDocTopicItem(DocTopicItem dti) throws IllegalArgumentException {
		if (this.dtItemList.containsKey(dti.source))
			throw new IllegalArgumentException(
				"Can't add a DTI twice: " + dti.source);
		this.dtItemList.put(dti.source, dti);
	}

	/**
	 * Gets the {@link DocTopicItem} of a given name. Use this instead of
	 * {@link #setClusterDocTopic(Cluster, String)} when reloading from a previous
	 * execution where the {@link DocTopicItem}s have already been correctly
	 * renamed.
	 *
	 * @param name Name of the {@link DocTopicItem} to recover.
	 * @return Associated {@link DocTopicItem}, or null if none exists.
	 * @see #setClusterDocTopic(Cluster, String)
	 */
	public DocTopicItem getDocTopicItem(String name) {
		return dtItemList.get(name); }

	/**
	 * @return A map of {@link TopicItem#topicNum} to the most prominent words in
	 * 				 that topic.
	 */
	public Map<Integer, List<String>> getTopicWordLists() {
		return topicWordLists; }

	/**
	 * Creates a read-only copy of this DocTopics singleton instance. Can be used
	 * when frequently iterating over a DocTopics instance without modifying it,
	 * such as for GUI applications.
	 *
	 * Note that while the list itself is immutable, it is NOT a deep clone,
	 * meaning that changes to the comprising {@link DocTopicItem}s will be
	 * reflected in the ones contained in the singleton DocTopics. This is a
	 * known issue which will be fixed in the future, but for the time being,
	 * user beware.
	 *
	 * @return An unmodifiable copy of this DocTopics' {@link DocTopicItem}s.
	 */
	public Collection<DocTopicItem> getCopy() {
		//TODO make this a deep clone
		return List.copyOf(this.dtItemList.values());
	}

	/**
	 * @return the number of topics used to build this instance.
	 */
	public int getNumTopics() {
		return this.dtItemList.values()
			.stream().findFirst().get().getTopics().size();
	}
	//endregion

	//region PROCESSING
	/**
	 * Assigns an appropriate {@link DocTopicItem} instance to the given
	 * {@link Cluster}. If no appropriate {@link DocTopicItem} exists in this
	 * DocTopics singleton, null is assigned.
	 *
	 * Use {@link #getDocTopicItem(String)} if reloading from a previous execution
	 * where the {@link DocTopicItem}s have already been appropriately renamed.
	 *
	 * @param c The {@link Cluster} to assign a {@link DocTopicItem} to.
	 * @param language Source language of the subject system. This is used to
	 *                 determine the parsing strategy between {@link Cluster#name}
	 *                 and {@link DocTopicItem#source}.
	 * @see #getDocTopicItem(String)
	 */
	public void setClusterDocTopic(Cluster c, String language) {
		c.setDocTopicItem(this.getDocTopicItem(c.name, language)); }

	/**
	 * Gets the {@link DocTopicItem} for a given file name or entity. Parsing
	 * the {@link DocTopicItem#source} is bound by the selected project language.
	 *
	 * @param name Name of the file or entity.
	 * @param language Source language of the file.
	 * @return Associated {@link DocTopicItem}, or null if none is found.
	 * @throws IllegalArgumentException if selected language is unknown.
	 * @see #getDocTopicItemForC(String)
	 * @see #getDocTopicItemForJava(String)
	 */
	private DocTopicItem getDocTopicItem(String name, String language)
			throws IllegalArgumentException {
		if (language.equalsIgnoreCase("java"))
			return getDocTopicItemForJava(name);
		if (language.equalsIgnoreCase("c"))
			return getDocTopicItemForC(name);
		if (language.equalsIgnoreCase("python"))
			return getDocTopicItemForPython(name);

		throw new IllegalArgumentException("Unknown source language " + language);
	}

	/**
	 * Gets the {@link DocTopicItem} for a given Java file or class name.
	 *
	 * @param name Name of the file or Java class.
	 * @return Associated {@link DocTopicItem}, or null if none is found.
	 * @see #getDocTopicItem(String, String)
	 */
	private DocTopicItem getDocTopicItemForJava(String name) {
		if (!this.fileLevel)
			name = name.replace(".", "/");
		else
			name = name.replace("\\", "/");
		DocTopicItem toReturn = null;

		// Attempts to locate a DTI that contains the desired class name
		for (Map.Entry<String, DocTopicItem> entry : dtItemList.entrySet()) {
			String dtiSource = entry.getKey();
			dtiSource = dtiSource.replace("\\", "/")
				.replace("_temp", "").trim();

			if (!this.fileLevel)
				dtiSource = dtiSource.replace(".java", "");

			if (dtiSource.endsWith(name) && dtiSource.contains(
						"/" + this.projectName + "-" + this.projectVersion + "/")) {
				if (toReturn == null)
					toReturn = entry.getValue();
				else
					System.err.println("Two DocTopicItems found to match "
						+ name + ": " + toReturn.source + " and " + entry.getKey());
			}
		}

		return toReturn;
	}

	/**
	 * Gets the {@link DocTopicItem} for a given C file or entity name.
	 *
	 * @param name Name of the file or C entity.
	 * @return Associated {@link DocTopicItem}.
	 * @see #getDocTopicItem(String, String)
	 */
	private DocTopicItem getDocTopicItemForC(String name) {
		String nameWithoutQuotations = name.replace("\"", "")
			.replace("\\", "/")
			// This is a hack to deal with the CSourceToDepsBuilder being bad
			.replace(".cpp", ".c");

		DocTopicItem toReturn = null;

		for (DocTopicItem dti : dtItemList.values()) {
			String strippedSource;

			if (dti.isCSourced()) {
				//FIXME Make sure this works on Linux and find a permanent fix
				strippedSource = dti.source
					.replace("_temp" + File.separator, File.separator)
					.replace("\\", "/")
					.replace("./", "")
					// This is a hack to deal with the CSourceToDepsBuilder being bad
					.replace(".cpp", ".c");
				if (strippedSource.endsWith("/" + nameWithoutQuotations)
						&& strippedSource.contains(
							"/" + this.projectName.toLowerCase() + "-" + this.projectVersion + "/")) {
					if (toReturn == null)
						toReturn = dti;
					else
						/* This should throw an error, since technically ARCADE doesn't
						 * know what to do with this situation. However, since I don't
						 * really know how to treat it yet, I'm just letting it happen
						 * and solving it case-by-case.
						 */
						//throw new IllegalStateException("Two DocTopicItems found to match "
						//	+ name + ": " + toReturn.source + " and " + dti.source);
						System.err.println("Two DocTopicItems found to match "
							+ name + ": " + toReturn.source + " and " + dti.source);
				}
			}
		}
		return toReturn;
	}

	//TODO merge this and above into "getDocTopicItemForFile(name, language)"
	private DocTopicItem getDocTopicItemForPython(String name) {
		String nameWithoutQuotations = name.replace("\"", "")
			.replace("\\", "/");

		DocTopicItem toReturn = null;

		for (DocTopicItem dti : dtItemList.values()) {
			String strippedSource;

			if (dti.isPythonSourced()) {
				//FIXME Make sure this works on Linux and find a permanent fix
				strippedSource = dti.source.replace("\\", "/")
					.replace("_temp", "")
					.replace("./", "");
				if (strippedSource.endsWith("/" + nameWithoutQuotations)
					&& strippedSource.contains(
					"/" + this.projectName + "-" + this.projectVersion + "/")) {
					if (toReturn == null)
						toReturn = dti;
					else
						/* This should throw an error, since technically ARCADE doesn't
						 * know what to do with this situation. However, since I don't
						 * really know how to treat it yet, I'm just letting it happen
						 * and solving it case-by-case.
						 */
						//throw new IllegalStateException("Two DocTopicItems found to match "
						//	+ name + ": " + toReturn.source + " and " + dti.source);
						System.err.println("Two DocTopicItems found to match "
							+ name + ": " + toReturn.source + " and " + dti.source);
				}
			}
		}
		return toReturn;
	}

	/**
	 * Merges the {@link DocTopicItem}s of two {@link Cluster}s such that a new
	 * {@link DocTopicItem} representing a new merged {@link Cluster} is created
	 * and added to this DocTopics instance. This is the only appropriate way to
	 * merge two {@link DocTopicItem}s.
	 *
	 * @param c1 First {@link Cluster} being merged.
	 * @param c2 Second {@link Cluster} being merged.
	 * @param newSource Value to be assigned to the newly created
	 *                  {@link DocTopicItem#source}.
	 * @return The newly merged {@link DocTopicItem}.
	 * @throws UnmatchingDocTopicItemsException if the two {@link DocTopicItem}s
	 * 																					were made using different topic
	 * 																				 	distributions.
	 */
	public DocTopicItem mergeDocTopicItems(
			Cluster c1, Cluster c2, String newSource)
			throws UnmatchingDocTopicItemsException {
		return mergeDocTopicItems(c1.getDocTopicItem(),
			c2.getDocTopicItem(), newSource);
	}

	/**
	 * Merges the {@link DocTopicItem}s of two {@link Cluster}s such that a new
	 * {@link DocTopicItem} representing a new merged {@link Cluster} is created
	 * and added to this DocTopics instance. This is the only appropriate way to
	 * merge two {@link DocTopicItem}s.
	 *
	 * @param dti1 {@link DocTopicItem} of the first {@link Cluster} being merged.
	 * @param dti2 {@link DocTopicItem} of the second {@link Cluster}
	 *             being merged.
	 * @param newSource Value to be assigned to the newly created
	 *                  {@link DocTopicItem#source}.
	 * @return The newly merged {@link DocTopicItem}.
	 * @throws UnmatchingDocTopicItemsException if the two {@link DocTopicItem}s
	 * 																					were made using different topic
	 * 																				 	distributions.
	 */
	public DocTopicItem mergeDocTopicItems(
			DocTopicItem dti1, DocTopicItem dti2, String newSource)
			throws UnmatchingDocTopicItemsException {
		DocTopicItem newDti = new DocTopicItem(dti1, dti2, newSource);
		if (newDti.equals(dti1))
			return dti1;
		if (newDti.equals(dti2))
			return dti2;

		this.dtItemList.remove(dti1.source);
		this.dtItemList.remove(dti2.source);
		addDocTopicItem(newDti);

		return newDti;
	}

	/**
	 * Safely replaces a {@link DocTopicItem#source}. Used when renaming a
	 * {@link Cluster} without necessarily merging it with another, such as for
	 * serialization purposes.
	 *
	 * @param dti The {@link DocTopicItem} being renamed.
	 * @param newSource The value to replace the {@link DocTopicItem#source} with.
	 */
	public void renameDocTopicItem(DocTopicItem dti, String newSource) {
		this.dtItemList.remove(dti.source);
		dti.source = newSource;
		addDocTopicItem(dti);
	}

	/**
	 * Removes a collection of {@link DocTopicItem}s from this DocTopics singleton
	 * instance. Is used primarily in serialization tasks, as a DocTopics may
	 * contain a large number of irrelevant {@link DocTopicItem}s if it was loaded
	 * from a multi-version vectors file.
	 *
	 * @param toKeep The collection of {@link DocTopicItem}s which have been
	 *               validated to be relevant to this particular singleton's
	 *               project version.
	 * @see #initializeSingleton(String, String, String, boolean)
	 * @see #DocTopics(String, String, boolean)
	 */
	public void cleanDocTopics(Collection<DocTopicItem> toKeep) {
		EnhancedSet<DocTopicItem> toKeepSet = new EnhancedHashSet<>(toKeep);
		EnhancedSet<DocTopicItem> completeSet =
			new EnhancedHashSet<>(this.dtItemList.values());

		Collection<DocTopicItem> toRemoveSet = completeSet.difference(toKeepSet);

		for (DocTopicItem dti : toRemoveSet)
			this.dtItemList.remove(dti.source);
	}
	//endregion

	//region SERIALIZATION
	public void serialize(String filePath) throws IOException {
		try (EnhancedJsonGenerator generator =
				new EnhancedJsonGenerator(filePath)) {
			serialize(generator);
		}
	}

	@Override
	public void serialize(EnhancedJsonGenerator generator) throws IOException {
		generator.writeField("projectName", this.projectName);
		generator.writeField("projectVersion", this.projectVersion);
		generator.writeField("fileLevel", String.valueOf(this.fileLevel));
		generator.writeField("dtItemList", this.dtItemList.values());
		generator.writeField("topicWordLists",
			topicWordLists, false, "wordList");
	}

	public static DocTopics deserialize(String filePath) throws IOException {
		try (EnhancedJsonParser parser = new EnhancedJsonParser(filePath)) {
			return deserialize(parser, "");
		}
	}

	public static DocTopics deserialize(String filePath, String version)
		throws IOException {
		try (EnhancedJsonParser parser = new EnhancedJsonParser(filePath)) {
			return deserialize(parser, version);
		}
	}

	public static DocTopics deserialize(EnhancedJsonParser parser, String version)
			throws IOException {
		String name = parser.parseString();

		// Allows reassigning the same DocTopics to multiple versions
		if (version.isEmpty())
			version = parser.parseString();
		else
			parser.parseString();

		boolean fileLevel = Boolean.parseBoolean(parser.parseString());

		mingleton.put(name.toLowerCase() + "-" + version, new DocTopics(name, version, fileLevel));
		Collection<DocTopicItem> dtis = parser.parseCollection(DocTopicItem.class);

		for (DocTopicItem dti : dtis)
			mingleton.get(name.toLowerCase() + "-" + version).addDocTopicItem(dti);

		Map<Integer, List> topicWordLists = parser.parseMap(
			List.class, false, String.class);
		for (Map.Entry<Integer, List> entry : topicWordLists.entrySet())
			mingleton.get(name.toLowerCase() + "-" + version).topicWordLists.put(
				entry.getKey(), entry.getValue());

		return mingleton.get(name.toLowerCase() + "-" + version);
	}
	//endregion
}
