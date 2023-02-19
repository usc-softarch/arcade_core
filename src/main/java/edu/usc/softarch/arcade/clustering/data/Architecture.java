package edu.usc.softarch.arcade.clustering.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import edu.usc.softarch.arcade.clustering.Clusterer;
import edu.usc.softarch.arcade.clustering.ClusteringAlgorithmType;
import edu.usc.softarch.arcade.clustering.simmeasures.SimMeasure;
import edu.usc.softarch.arcade.facts.DependencyGraph;
import edu.usc.softarch.arcade.topics.Concern;
import edu.usc.softarch.arcade.topics.DocTopicItem;
import edu.usc.softarch.arcade.topics.DocTopics;
import edu.usc.softarch.arcade.topics.exceptions.UnmatchingDocTopicItemsException;
import edu.usc.softarch.arcade.util.FileUtil;
import edu.usc.softarch.util.json.EnhancedJsonGenerator;
import edu.usc.softarch.util.json.EnhancedJsonParser;
import edu.usc.softarch.util.json.JsonSerializable;

/**
 * Represents an architecture of clustered entities. An Architecture is a
 * specialization of a {@link TreeMap}&lt;{@link String}, {@link Cluster}&gt;,
 * meaning it is ordered by the names of its comprising {@link Cluster}s. This
 * ensures that all iterative operations done over an Architecture will follow
 * the same order, regardless of the order in which the Architecture was
 * constructed, i.e. the order of the clustering algorithm's inputs.
 */
public class Architecture extends TreeMap<String, Cluster>
		implements JsonSerializable {
	//region ATTRIBUTES
	/**
	 * The name of the subject system represented by this architecture.
	 */
	public final String projectName;
	/**
	 * The version of the subject system represented by this architecture.
	 */
	public final String projectVersion;
	/**
	 * The path to where this data structure should be serialized.
	 */
	public final String projectPath;
	/**
	 * The path to where this data structure should be serialized.
	 */
	public final String depsPath;
	public final SimMeasure.SimMeasureType simMeasure;
	/**
	 * The total number of features that can exist in any clusters of this
	 * architecture. For architectures constructed from structural data such as
	 * dependencies, this is the number of entities in the initial architecture,
	 * e.g. the number of files when using file-level granularity, or the number
	 * of functions when using function-level granularity.
	 */
	private final int numFeatures;

	protected final String language;
	//endregion

	//region CONSTRUCTORS
	private Architecture(String projectName, String projectVersion,
			String projectPath, String depsPath, SimMeasure.SimMeasureType simMeasure,
			int numFeatures, String language, Map<String, Cluster> architecture) {
		super();
		this.projectName = projectName;
		this.projectVersion = projectVersion;
		this.projectPath = projectPath;
		this.depsPath = depsPath;
		this.simMeasure = simMeasure;
		this.numFeatures = numFeatures;
		this.language = language;
		this.putAll(architecture);
	}

	/**
	 * Clone constructor.
	 */
  public Architecture(Architecture arch) {
		for (Cluster c : arch.values())
			this.add(new Cluster(c));

		this.projectName = arch.projectName;
		this.projectVersion = arch.projectVersion;
		this.projectPath = arch.projectPath;
		this.depsPath = arch.depsPath;
		this.simMeasure = arch.simMeasure;
		this.numFeatures = arch.numFeatures;
		this.language = arch.language;
	}

	/**
	 * Default constructor for new Architecture objects.
	 *
	 * @param projectName The name of the subject system represented by this
	 *                    architecture.
	 * @param projectVersion The version of the subject system represented by
	 *                       this architecture.
	 * @param projectPath The path to where this data structure should be
	 *                    serialized.
	 * @param depsPath The path to the dependencies RSF file for
	 *                 this architecture.
	 * @param language The language of the subject system.
	 * @param packagePrefix The package prefix to be considered in the
	 *                      initialization of the Architecture. Only used in
	 *                      Java systems.
	 */
	public Architecture(String projectName, String projectVersion,
			String projectPath, SimMeasure.SimMeasureType simMeasure,
			String depsPath, String language, String packagePrefix)
			throws IOException {
		this.projectName = projectName;
		this.projectVersion = projectVersion;
		this.projectPath = projectPath;
		this.depsPath = depsPath;
		this.simMeasure = simMeasure;

		FeatureVectors vectors =
			new FeatureVectors(DependencyGraph.readRsf(depsPath));
		this.numFeatures = vectors.getNamesInFeatureSet().size();
		this.language = language;
		initializeClusters(vectors, language, packagePrefix);
	}

	public Architecture(String projectName, String projectVersion,
			String projectPath, SimMeasure.SimMeasureType simMeasure,
			String depsPath, String language, String artifactsDir,
			String packagePrefix, boolean fileLevel)
			throws UnmatchingDocTopicItemsException, IOException {
		this(projectName, projectVersion, projectPath, simMeasure,
			depsPath, language, artifactsDir, packagePrefix,
			false, fileLevel);
	}

	public Architecture(String projectName, String projectVersion,
			String projectPath, SimMeasure.SimMeasureType simMeasure,
			String depsPath, String language, String artifactsDir,
			String packagePrefix, boolean reassignVersion, boolean fileLevel)
			throws UnmatchingDocTopicItemsException, IOException {
		this(projectName, projectVersion, projectPath, simMeasure,
			depsPath, language, packagePrefix);

		try	{
			if (reassignVersion)
				DocTopics.deserialize(artifactsDir
					+ File.separator + "docTopics.json", projectVersion);
			else
				DocTopics.deserialize(artifactsDir
					+ File.separator + "docTopics.json");
		} catch (IOException e) {
			System.out.println("No DocTopics file found, generating new one.");
			// Initialize DocTopics from files
			try {
				DocTopics.initializeSingleton(
					artifactsDir, this.projectName, this.projectVersion, fileLevel);
				DocTopics.getSingleton(this.projectName, this.projectVersion)
					.serialize(artifactsDir + File.separator + "docTopics.json");
			} catch (Exception f) {
				f.printStackTrace();
			}
		}

		initializeClusterDocTopics();
	}

	/**
	 * Convenience constructor for new Architecture objects in C-based systems.
	 * Package prefix is set to empty string.
	 *
	 * @param projectName The name of the subject system represented by this
	 *                    architecture.
	 * @param projectVersion The version of the subject system represented by
	 *                       this architecture.
	 * @param projectPath The path to where this data structure should be
	 *                    serialized.
	 * @param depsPath The path to the dependencies RSF file for
	 *                 this architecture.
	 * @param language The language of the subject system.
	 */
	public Architecture(String projectName, String projectVersion,
			String projectPath, SimMeasure.SimMeasureType simMeasure,
			String depsPath, String language) throws IOException {
		this(projectName, projectVersion, projectPath, simMeasure,
			depsPath, language, "");
	}

	/**
	 * Initializes the clusters of this architecture from a {@link FeatureVectors}
	 * object.
	 *
	 * @param vectors The {@link FeatureVectors} to use for initialization.
	 * @param language The language of the subject system.
	 * @param packagePrefix The package prefix to be considered in the
	 *                      initialization of the Architecture. Only used in
	 *                      Java systems.
	 */
	protected void initializeClusters(FeatureVectors vectors, String language,
			String packagePrefix) {
		// For each cell in the adjacency matrix
		for (String name : vectors.getFeatureVectorNames()) {
			// Get the vector relative to that cell
			BitSet featureSet = vectors.getNameToFeatureSetMap().get(name);
			// Create a cluster containing only that cell
			Cluster cluster = new Cluster(name, featureSet,
				vectors.getNamesInFeatureSet().size());

			// Add the cluster except extraordinary circumstances (assume always)
			addClusterConditionally(cluster, language, packagePrefix);
		}

		Clusterer.numberOfEntitiesToBeClustered = this.size();
	}

	/**
	 * Adds a cluster if and only if it satisfies a set of conditions.
	 *
	 * For systems in C-based languages, it checks that only entities from
	 * C-based files are accepted.
	 *
	 * For clustering analyses based on Function-level granularity, it ensures
	 * that no compiler-generated functions are accepted.
	 *
	 * For clustering of Java-based subject systems, it ensures that only entities
	 * within the given package prefix are accepted.
	 *
	 * @param cluster The {@link Cluster} to tentatively add to this architecture.
	 * @param language The language of the subject system.
	 * @param packagePrefix The package prefix to be considered. Only used in
	 *                      Java systems.
	 */
	private void addClusterConditionally(Cluster cluster, String language,
			String packagePrefix) {
		// If the source language is C or C++, add only the C-based entities
		if (language.equalsIgnoreCase("c")) {
			Pattern p = Pattern.compile(
				"\\.(c|cpp|cxx|cc|include|s|h|hh|hpp|hxx|m|mm|icc|ia|tbl|p)$");
			if (!cluster.name.startsWith("/") && p.matcher(cluster.name).find())
				this.put(cluster.name, cluster);
		}

		// If the source language is Python, same as above
		if (language.equalsIgnoreCase("python")) {
			Pattern p = Pattern.compile("\\.(py|py3|pyc|pyo|pyw|pyx|pyd|pxd|pyi"
				+ "|pyz|pywz|rpy|pyde|pyp|pyt|xpy|ipynb)$");
			if (!cluster.name.startsWith("/") && p.matcher(cluster.name).find())
				this.put(cluster.name, cluster);
		}

		// If the source language is Java, add all clusters that match prefix
		if (language.equalsIgnoreCase("java")
				&& (packagePrefix.isEmpty()
				|| cluster.name.startsWith(packagePrefix)))
			this.put(cluster.name, cluster);
	}

	protected void initializeClusterDocTopics()
			throws UnmatchingDocTopicItemsException {
		// Set the DocTopics of each Cluster
		for (Cluster c : super.values())
			DocTopics.getSingleton(this.projectName, this.projectVersion)
				.setClusterDocTopic(c, this.language);

		// Map inner classes to their parents
		Map<String,String> oldParentClassMap = new HashMap<>();
		for (Cluster c : super.values()) {
			if (c.name.contains("$")) {
				String[] tokens = c.name.split("\\$");
				String parentClassName = tokens[0];
				oldParentClassMap.put(c.name, parentClassName);
			}
		}

		Map<Cluster, Cluster> parentClassMap = new LinkedHashMap<>();
		for (Cluster c : super.values()) {
			if (c.name.contains("$")) {
				String[] tokens = c.name.split("\\$");
				String parentClassName = tokens[0];
				parentClassMap.put(c, super.get(parentClassName));
			}
		}

		removeClassesWithoutDTI(parentClassMap);
		removeInnerClasses(oldParentClassMap);

		Map<String, Cluster> clustersWithMissingDocTopics =	new HashMap<>();
		for (Cluster c : super.values())
			if (!c.hasDocTopicItem())
				clustersWithMissingDocTopics.put(c.name, c);

		this.removeAll(clustersWithMissingDocTopics);

		for (Cluster c : clustersWithMissingDocTopics.values())
			super.remove(c.name);
	}

	//TODO Change this to the correct format
	private void removeInnerClasses(Map<String, String> parentClassMap)
			throws UnmatchingDocTopicItemsException {
		for (Map.Entry<String, String> entry : parentClassMap.entrySet()) {
			Cluster nestedCluster = super.get(entry.getKey());
			if (nestedCluster == null) continue; // was already removed by WithoutDTI
			Cluster parentCluster = super.get(entry.getValue());
			Cluster mergedCluster =
				new Cluster(ClusteringAlgorithmType.ARC, nestedCluster, parentCluster,
					this.projectName, this.projectVersion);
			super.remove(parentCluster.name);
			super.remove(nestedCluster.name);
			super.put(mergedCluster.name, mergedCluster);
		}
	}

	private void removeClassesWithoutDTI(Map<Cluster, Cluster> parentClassMap) {
		// Locate non-inner classes without DTI
		Map<String, Cluster> excessClusters = new HashMap<>();
		for (Cluster c : super.values())
			if (!c.hasDocTopicItem() && !c.name.contains("$"))
				excessClusters.put(c.name, c);

		Map<String, Cluster> excessInners = new HashMap<>();
		// For each Child/Parent pair, if the parent is marked, mark the child
		for (Map.Entry<Cluster, Cluster> entry : parentClassMap.entrySet()) {
			Cluster child = entry.getKey();
			Cluster parent = entry.getValue();
			if (excessClusters.containsKey(parent.name))
				excessInners.put(child.name, child);
		}

		// Remove them from the analysis
		this.removeAll(excessClusters);
		this.removeAll(excessInners);
	}
	//endregion

	//region ACCESSORS
	/**
	 * Returns the total number of features present in this architecture.
	 */
	public int getNumFeatures() { return this.numFeatures; }

	/**
	 * Checks whether there exist any {@link Cluster} in this architecture with
	 * size == 1, that is, a {@link Cluster} with only 1 entity. Useful for
	 * some stopping criteria, as it indicates a {@link Cluster} which has not
	 * yet been considered by the clustering process.
	 *
	 * @return True if any {@link Cluster} has only 1 entity, False otherwise.
	 */
	public boolean hasOrphans() {
		for (Cluster c : this.values()) {
			if (c.getNumEntities() == 1)
				return true;
		}
		return false;
	}

	/**
	 * Adds the given {@link Cluster} to this architecture.
	 */
	public void add(Cluster c) {
		this.put(c.name, c);
	}

	/**
	 * Removes the given {@link Cluster}s from this architecture. Primarily used
	 * in pre-processing phases to remove unwanted entities from the analysis.
	 *
	 * @param clusters A map where the keys are the names of the {@link Cluster}s
	 *                 to be removed.
	 */
	public void removeAll(Map<String, Cluster> clusters) {
		for (String key : clusters.keySet())
			this.remove(key);
	}
	//endregion

	//region SERIALIZATION
	/**
	 * Writes an RSF file representing this architecture. Uses this object's
	 * {@link #projectPath} as the output location and its {@link #projectName}
	 * to determine the name of the output file.
	 */
	public void writeToRsf() throws FileNotFoundException {
		this.writeToRsf(true);
	}

	public void writeToRsf(boolean useIndices) throws FileNotFoundException {
		String fs = File.separator;
		String path = this.projectPath + fs + this.projectName + "-"
			+ this.projectVersion + "_" + this.simMeasure + "_" + this.size()
			+ "_clusters.rsf";
		this.writeToRsf(path, useIndices);
	}

	/**
	 * Writes an RSF file representing this architecture to the provided path.
	 *
	 * @param path A {@link String} pointing to the output file path, including
	 *             the desired output filename.
	 */
	public void writeToRsf(String path) throws FileNotFoundException {
		this.writeToRsf(path, true);
	}

	public void writeToRsf(String path, boolean useIndices)
			throws FileNotFoundException {
		File rsfFile = new File(path);
		rsfFile.getParentFile().mkdirs();

		if (useIndices) writeToRsfWithIndices(rsfFile);
		else writeToRsfWithoutIndices(rsfFile);
	}

	private void writeToRsfWithIndices(File rsfFile) throws FileNotFoundException {
		Map<Integer, Cluster> architectureIndex = computeArchitectureIndex();

		try (PrintWriter out = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(rsfFile), StandardCharsets.UTF_8))) {
			for (Map.Entry<Integer, Cluster> cluster : architectureIndex.entrySet()) {
				Integer clusterIndex = cluster.getKey();
				Collection<String> entities = cluster.getValue().getEntities();
				for (String entity : entities)
					out.println("contain " + clusterIndex + " " + entity);
			}
		}
	}

	private void writeToRsfWithoutIndices(File rsfFile)
			throws FileNotFoundException {
		try (PrintWriter out = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(rsfFile), StandardCharsets.UTF_8))) {
			for (Cluster cluster : this.values()) {
				Collection<String> entities = cluster.getEntities();
				for (String entity : entities)
					out.println("contain " + cluster.name + " " + entity);
			}
		}
	}

	public void writeToDot() throws IOException {
		String fs = File.separator;
		String path = this.projectPath + fs + this.projectName + "-"
			+ this.projectVersion + "_" + this.simMeasure + ".dot";
		writeToDot(path);
	}

	public void writeToDot(String outputPath)
			throws IOException {
		ReadOnlyArchitecture roArch = new ReadOnlyArchitecture(this);
		roArch.writeToDot(this.depsPath, outputPath);
	}

	public void writeToDotClusters() throws IOException {
		String fs = File.separator;
		String path = this.projectPath + fs + this.projectName + "-"
			+ this.projectVersion + "_" + this.simMeasure + "_clusterDots";
		FileUtil.checkDir(path, true, false);
		writeToDotClusters(path);
	}

	public void writeToDotClusters(String outputPath)
			throws IOException {
		ReadOnlyArchitecture roArch = new ReadOnlyArchitecture(this);
		roArch.writeToDotClusters(
			this.depsPath, outputPath, computeArchitectureIndex());
	}

	/**
	 * Returns a map indexing the {@link Cluster}s of this architecture. Used in
	 * serialization: as a {@link Cluster}'s name typically represents the names
	 * of its comprising entities, a number is assigned to the {@link Cluster} as
	 * a unique identifier. The names of the entities are broken down into
	 * separate entries by the serialization method.
	 */
	protected Map<Integer, Cluster> computeArchitectureIndex() {
		List<Cluster> orderedClusters = this.values().stream()
			.sorted().collect(Collectors.toList());

		Map<Integer, Cluster> architectureIndex = new TreeMap<>();
		for (int i = 0; i < this.size(); i++)
			architectureIndex.put(i, orderedClusters.get(i));

		return architectureIndex;
	}

	public void serialize(String path) throws IOException {
		try (EnhancedJsonGenerator generator = new EnhancedJsonGenerator(path)) {
			serialize(generator);
		}
	}

	@Override
	public void serialize(EnhancedJsonGenerator generator) throws IOException {
		generator.writeField("projectName", projectName);
		generator.writeField("projectVersion", projectVersion);
		generator.writeField("projectPath", projectPath);
		generator.writeField("depsPath", depsPath);
		generator.writeField("simMeasure", simMeasure.toString());
		generator.writeField("numFeatures", numFeatures);
		generator.writeField("language", language);
		generator.writeField("architecture", this, true,
			"clusterName", "cluster");
	}

	public static Architecture deserialize(String path) throws IOException {
		try (EnhancedJsonParser parser = new EnhancedJsonParser(path)) {
			return deserialize(parser);
		}
	}

	public static Architecture deserialize(EnhancedJsonParser parser)
			throws IOException {
		String projectName = parser.parseString();
		String projectVersion = parser.parseString();
		String projectPath = parser.parseString();
		String depsPath = parser.parseString();
		SimMeasure.SimMeasureType simMeasure =
			SimMeasure.SimMeasureType.valueOf(parser.parseString());
		int numFeatures = parser.parseInt();
		String language = parser.parseString();
		Map<String, Cluster> architecture =
			parser.parseMap(String.class, Cluster.class, true);

		return new Architecture(projectName, projectVersion, projectPath,
			depsPath, simMeasure, numFeatures, language, architecture);
	}

	public void serializeBagOfWords() throws FileNotFoundException {
		computeConcernWordBags();

		String fs = File.separator;
		String path = this.projectPath + fs + this.projectName + "-"
			+ this.projectVersion + "_" + this.simMeasure + "_"
			+ this.size() + "_concerns.txt";
		File outputFile = new File(path);
		outputFile.getParentFile().mkdirs();

		Map<Integer, Cluster> architectureIndex = computeArchitectureIndex();

		try (PrintWriter out = new PrintWriter(new OutputStreamWriter(
			new FileOutputStream(outputFile), StandardCharsets.UTF_8))) {
			StringBuilder output = new StringBuilder();

			for (Map.Entry<Integer, Cluster> cluster : architectureIndex.entrySet()) {
				DocTopicItem dti = cluster.getValue().getDocTopicItem();
				Concern concernWords = dti.getConcern();
				output.append(cluster.getKey());
				output.append(concernWords);
				output.append(System.lineSeparator());
			}

			out.print(output);
		}
	}

	public List<Concern> computeConcernWordBags() {
		List<Concern> concernList = new ArrayList<>();
		for (Cluster cluster : this.values())
			concernList.add(cluster.computeConcern(
				DocTopics.getSingleton(this.projectName, this.projectVersion)
					.getTopicWordLists()));

		return concernList;
	}

	public void serializeDocTopics() throws IOException {
		String fs = File.separator;

		Map<Integer, Cluster> architectureIndex = computeArchitectureIndex();
		Collection<DocTopicItem> toKeep = new HashSet<>();

		for (Map.Entry<Integer, Cluster> entry : architectureIndex.entrySet()) {
			DocTopicItem toRename = entry.getValue().getDocTopicItem();
			DocTopics.getSingleton(this.projectName, this.projectVersion)
				.renameDocTopicItem(toRename, entry.getKey().toString());
			toKeep.add(toRename);
		}

		DocTopics.getSingleton(this.projectName, this.projectVersion)
			.cleanDocTopics(toKeep);

		String path = this.projectPath + fs + this.projectName + "-"
			+ this.projectVersion + "_" + this.simMeasure + "_" + this.size()
			+ "_clusteredDocTopics.json";
		DocTopics.getSingleton(this.projectName, this.projectVersion)
			.serialize(path);
	}
	//endregion
}
