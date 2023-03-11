package edu.usc.softarch.arcade.metrics.data;

import edu.usc.softarch.arcade.clustering.data.ReadOnlyArchitecture;
import edu.usc.softarch.arcade.metrics.RenameFixer;
import edu.usc.softarch.arcade.util.CLI;
import edu.usc.softarch.arcade.util.FileUtil;
import edu.usc.softarch.arcade.util.McfpDriver;
import edu.usc.softarch.arcade.util.Version;
import edu.usc.softarch.util.Terminal;
import edu.usc.softarch.util.json.EnhancedJsonGenerator;
import edu.usc.softarch.util.json.EnhancedJsonParser;
import edu.usc.softarch.util.json.JsonSerializable;
import mojo.MoJoCalculator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class SystemMetrics implements JsonSerializable {
	//region PUBLIC INTERFACE
	/**
	 * systemDirPath = Path to where ARCADE put the version directories as output.
	 * depsDirPath   = Path to where all the deps.rsf files are located.
	 * outputPath    = Path to place the output file in.
	 */
	public static void main(String[] args) throws IOException {
		Map<String, String> parsedArgs = CLI.parseArguments(args);
		run(parsedArgs.get("systemdirpath"),
			parsedArgs.get("depsdirpath"), parsedArgs.get("outputpath"));
	}

	public static void run(String systemDirPath, String depsDirPath,
			String outputPath) throws IOException {
		SystemMetrics metrics = new SystemMetrics(systemDirPath, depsDirPath);
		metrics.serialize(outputPath + File.separator + "metricsObject.json");
		metrics.writeClusterMetrics(outputPath);
		metrics.writeVersionMetrics(
			outputPath + File.separator + "versionMetrics.csv");
		metrics.writeSystemMetrics(outputPath);
	}
	//endregion

	//region ATTRIBUTES
	private final Map<Version, ArchitectureMetrics> versionMetrics;

	private final Version[] versions;
	private final A2aSystemData a2a;
	private final EdgeA2aSystemData edgeA2a;
	private final WeightedEdgeA2aSystemData weightedEdgeA2a;
	private final EdgeA2aSystemData edgeA2a40;
	private final WeightedEdgeA2aSystemData weightedEdgeA2a40;
	private final EdgeA2aSystemData edgeA2aMin;
	private final WeightedEdgeA2aSystemData weightedEdgeA2aMin;
	private final CvgSystemData cvg;
	private final double[][] mojoFm;
	//endregion

	//region CONSTRUCTORS
	public SystemMetrics(String systemDirPath, String depsDirPath)
			throws IOException {
		ExecutorService executor = Executors.newFixedThreadPool(8);

		// Get and sort the architecture files
		List<File> archFilesList = FileUtil.sortFileListByVersion(
			FileUtil.getFileListing(systemDirPath, ".rsf"));
		Vector<File> archFiles = new Vector<>(archFilesList);

		// Get and sort the dependency files
		List<File> depsFilesList = List.of(
			new File(FileUtil.tildeExpandPath(depsDirPath)).listFiles());
		depsFilesList = depsFilesList.stream()
			.filter(f -> f.getName().contains(".rsf")).collect(Collectors.toList());
		depsFilesList = FileUtil.sortFileListByVersion(depsFilesList);
		Vector<File> depsFiles = new Vector<>(depsFilesList);

		// Initialize the version list
		this.versions = new Version[archFiles.size()];
		for (int i = 0; i < archFiles.size(); i++)
			this.versions[i] = new Version(archFiles.get(i));

		// Initialize metrics objects
		this.a2a = new A2aSystemData(this.versions);
		this.edgeA2a = new EdgeA2aSystemData(this.versions, 0.66);
		this.weightedEdgeA2a =
			new WeightedEdgeA2aSystemData(this.versions, 0.66);
		this.edgeA2a40 = new EdgeA2aSystemData(this.versions, 0.4);
		this.weightedEdgeA2a40 =
			new WeightedEdgeA2aSystemData(this.versions, 0.4);
		this.edgeA2aMin = new EdgeA2aSystemData(this.versions, 0.0);
		this.weightedEdgeA2aMin =
			new WeightedEdgeA2aSystemData(this.versions, 0.0);
		this.cvg = new CvgSystemData(this.versions);
		this.mojoFm = new double[this.versions.length - 1][];
		for (int i = 0; i < this.versions.length - 1; i++)
			this.mojoFm[i] = new double[this.versions.length - 1 - i];
		this.versionMetrics = new ConcurrentSkipListMap<>();

		final int opCount = archFiles.size() * (archFiles.size() - 1) / 2;
		AtomicInteger evoMetricsCount = new AtomicInteger(1);
		final int versionCount = this.versions.length;
		AtomicInteger versionMetricsCount = new AtomicInteger(1);
		for (int i = 0; i < archFiles.size(); i++) {
			int finalI = i;

			for (int j = i + 1; j < archFiles.size(); j++) {
				int finalJ = j;
				executor.submit(() -> {
					try {
						// Prep inputs
						ReadOnlyArchitecture v1 =
							ReadOnlyArchitecture.readFromRsf(archFiles.get(finalI));
						ReadOnlyArchitecture v2 =
							ReadOnlyArchitecture.readFromRsf(archFiles.get(finalJ));
						RenameFixer.fix(v1, v2);
						v1.buildGraphs(depsFiles.get(finalI).getAbsolutePath());
						v2.buildGraphs(depsFiles.get(finalJ).getAbsolutePath());
						McfpDriver driver = new McfpDriver(v1, v2);

						// Run metrics
						this.a2a.addValue(v1, v2, finalI, finalJ);
						this.edgeA2a.addValue(v1, v2, depsFiles.get(finalI),
							depsFiles.get(finalJ), driver, finalI, finalJ);
						this.weightedEdgeA2a.addValue(v1, v2, depsFiles.get(finalI),
							depsFiles.get(finalJ), driver, finalI, finalJ);
						this.edgeA2a40.addValue(v1, v2, depsFiles.get(finalI),
							depsFiles.get(finalJ), driver, finalI, finalJ);
						this.weightedEdgeA2a40.addValue(v1, v2, depsFiles.get(finalI),
							depsFiles.get(finalJ), driver, finalI, finalJ);
						this.edgeA2aMin.addValue(v1, v2, depsFiles.get(finalI),
							depsFiles.get(finalJ), driver, finalI, finalJ);
						this.weightedEdgeA2aMin.addValue(v1, v2, depsFiles.get(finalI),
							depsFiles.get(finalJ), driver, finalI, finalJ);
						this.cvg.addValue(v1, v2, finalI, finalJ);
						MoJoCalculator mojoCalc = new MoJoCalculator(
							archFiles.get(finalI).getAbsolutePath(),
							archFiles.get(finalJ).getAbsolutePath(), null);
						this.mojoFm[finalI][finalJ - finalI - 1] = mojoCalc.mojofm();
					} catch (IOException | ExecutionException | InterruptedException e) {
						e.printStackTrace();
						throw new RuntimeException(e);
					}
					Terminal.timePrint(evoMetricsCount.getAndIncrement() + "/"
						+ opCount + " evolutionary metrics computed.", Terminal.Level.INFO);
				});
			}

			executor.submit(() -> {
				try {
					this.versionMetrics.put(this.versions[finalI],
						new ArchitectureMetrics(archFiles.get(finalI).getAbsolutePath(),
						depsFiles.get(finalI).getAbsolutePath()));
				} catch (IOException e) {
					e.printStackTrace();
				}
				Terminal.timePrint(versionMetricsCount.getAndIncrement() + "/"
					+ versionCount + " version metrics computed.", Terminal.Level.INFO);
			});
		}
		executor.shutdown();
		try {
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.HOURS);
		} catch (InterruptedException e) {
			e.printStackTrace(); // Dunno why this would happen
		}
	}

	private SystemMetrics(Map<Version, ArchitectureMetrics> versionMetrics,
			Version[] versions, A2aSystemData a2a, EdgeA2aSystemData edgeA2a,
			WeightedEdgeA2aSystemData weightedEdgeA2a, EdgeA2aSystemData edgeA2a40,
			WeightedEdgeA2aSystemData weightedEdgeA2a40,EdgeA2aSystemData edgeA2aMin,
			WeightedEdgeA2aSystemData weightedEdgeA2aMin,
			CvgSystemData cvg, double[][] mojoFm) {
		this.versionMetrics = versionMetrics;
		this.versions = versions;
		this.a2a = a2a;
		this.edgeA2a = edgeA2a;
		this.weightedEdgeA2a = weightedEdgeA2a;
		this.edgeA2a40 = edgeA2a40;
		this.weightedEdgeA2a40 = weightedEdgeA2a40;
		this.edgeA2aMin = edgeA2aMin;
		this.weightedEdgeA2aMin = weightedEdgeA2aMin;
		this.cvg = cvg;
		this.mojoFm = mojoFm;
	}
	//endregion

	//region ACCESSORS
	public Collection<ArchitectureMetrics> getVersionMetrics() {
		return this.versionMetrics.values(); }
	public Version[] getVersions() {
		return Arrays.copyOf(this.versions, this.versions.length); }
	public A2aSystemData getA2a() {
		return new A2aSystemData(this.a2a); }
	public EdgeA2aSystemData getEdgeA2a() {
		return new EdgeA2aSystemData(this.edgeA2a); }
	public WeightedEdgeA2aSystemData getWeightedEdgeA2a() {
		return new WeightedEdgeA2aSystemData(this.weightedEdgeA2a); }
	public EdgeA2aSystemData getEdgeA2a40() {
		return new EdgeA2aSystemData(this.edgeA2a40); }
	public WeightedEdgeA2aSystemData getWeightedEdgeA2a40() {
		return new WeightedEdgeA2aSystemData(this.weightedEdgeA2a40); }
	public EdgeA2aSystemData getEdgeA2aMin() {
		return new EdgeA2aSystemData(this.edgeA2aMin); }
	public WeightedEdgeA2aSystemData getWeightedEdgeA2aMin() {
		return new WeightedEdgeA2aSystemData(this.weightedEdgeA2aMin); }
	public CvgSystemData getCvg() {
		return new CvgSystemData(this.cvg); }
	public double[][] getMojoFm() {
		return Arrays.copyOf(this.mojoFm, this.mojoFm.length); }
	//endregion

	//region SERIALIZATION
	public void writeClusterMetrics(String dirPath) throws IOException {
		FileUtil.checkDir(dirPath, true, false);

		// Initialize results map with maps of all relevant cluster metrics
		Map<String, Map<String, double[]>> results = new HashMap<>();
		results.put("intraConnectivity", new TreeMap<>());
		results.put("interConnectivityMean", new TreeMap<>());
		results.put("interConnectivityMin", new TreeMap<>());
		results.put("interConnectivityMax", new TreeMap<>());
		results.put("interConnectivityStDev", new TreeMap<>());
		results.put("basicMq", new TreeMap<>());
		results.put("clusterFactor", new TreeMap<>());
		results.put("fanIn", new TreeMap<>());
		results.put("fanOut", new TreeMap<>());
		results.put("instability", new TreeMap<>());

		// Get data from each individual version
		for (int i = 0; i < versions.length; i++) {
			ArchitectureMetrics version = this.versionMetrics.get(versions[i]);

			// Get data from each cluster of this version
			for (ClusterMetrics clusterMetrics : version.getClusterMetrics()) {
				String name = clusterMetrics.clusterName;

				// Create entries for this cluster, if they don't exist
				if (!results.get("intraConnectivity").containsKey(name))
					for (Map<String, double[]> metric : results.values())
						metric.put(name, initializeNanArray(versions.length));

				results.get("intraConnectivity").get(name)[i] =
					clusterMetrics.intraConnectivity;
				results.get("interConnectivityMean").get(name)[i] =
					clusterMetrics.interConnectivity.getMean();
				results.get("interConnectivityMin").get(name)[i] =
					clusterMetrics.interConnectivity.getMin();
				results.get("interConnectivityMax").get(name)[i] =
					clusterMetrics.interConnectivity.getMax();
				results.get("interConnectivityStDev").get(name)[i] =
					clusterMetrics.interConnectivity.getStDev();
				results.get("basicMq").get(name)[i] =
					clusterMetrics.basicMq;
				results.get("clusterFactor").get(name)[i] =
					clusterMetrics.clusterFactor;
				results.get("fanIn").get(name)[i] =
					clusterMetrics.fanIn;
				results.get("fanOut").get(name)[i] =
					clusterMetrics.fanOut;
				results.get("instability").get(name)[i] =
					clusterMetrics.instability;
			}
		}

		for (String metric : results.keySet())
			writeClusterMetricCsv(dirPath, metric, results);
	}

	private double[] initializeNanArray(int length) {
		double[] result = new double[length];
		for (int i = 0; i < length; i++)
			result[i] = Double.NaN;

		return result;
	}

	private void writeClusterMetricCsv(String dirPath, String metric,
			Map<String, Map<String, double[]>> results) throws IOException {
		try (FileWriter writer = new FileWriter(
			dirPath + File.separator + metric + ".csv")) {
			writer.write("Cluster");
			for (Version version : this.versions)
				writer.write("," + version);
			writer.write(System.lineSeparator());

			for (Map.Entry<String, double[]> cluster
				: results.get(metric).entrySet()) {
				writer.write(cluster.getKey());
				for (double v : cluster.getValue()) {
					writer.write(",");
					if (!Double.isNaN(v)) writer.write(Double.toString(v));
				}
				writer.write(System.lineSeparator());
			}
		}
	}

	public void writeVersionMetrics(String path) throws IOException {
		new File(path).getParentFile().mkdirs();

		// Initialize results map with arrays of all version metrics
		Map<String, double[]> results = new HashMap<>();
		results.put("intraConnectivity", new double[this.versions.length]);
		results.put("interConnectivity", new double[this.versions.length]);
		results.put("basicMq", new double[this.versions.length]);
		results.put("turboMq", new double[this.versions.length]);
		results.put("instability", new double[this.versions.length]);
		results.put("mojoFmGt", new double[this.versions.length]);

		// Get data from each version
		for (int i = 0; i < versions.length; i++) {
			ArchitectureMetrics version = this.versionMetrics.get(versions[i]);

			results.get("intraConnectivity")[i] = version.intraConnectivity;
			results.get("interConnectivity")[i] = version.interConnectivity;
			results.get("basicMq")[i] = version.basicMq;
			results.get("turboMq")[i] = version.turboMq;
			results.get("instability")[i] = version.instability;
			results.get("mojoFmGt")[i] = version.mojoFmGt;
		}

		try (FileWriter writer = new FileWriter(path)) {
			writer.write("Metric");
			for (Version version : this.versions)
				writer.write("," + version);
			writer.write(System.lineSeparator());

			this.writeVersionMetric(writer, "Intra-Connectivity",
				"intraConnectivity", results);
			this.writeVersionMetric(writer, "Inter-Connectivity",
				"interConnectivity", results);
			this.writeVersionMetric(writer, "BasicMQ",
				"basicMq", results);
			this.writeVersionMetric(writer, "TurboMQ",
				"turboMq", results);
			this.writeVersionMetric(writer, "Architectural Instability",
				"instability", results);
			this.writeVersionMetric(writer, "MoJoFM-GT",
				"mojoFmGt", results);
		}
	}

	private void writeVersionMetric(FileWriter writer, String metricName,
			String metricKey, Map<String, double[]> values) throws IOException {
		writer.write(metricName);
		writer.write(getVersionMetricValues(values, metricKey));
		writer.write(System.lineSeparator());
	}

	private String getVersionMetricValues(Map<String, double[]> versionMetrics,
			String metric) {
		StringBuilder sb = new StringBuilder();
		for (double v : versionMetrics.get(metric))
			sb.append(",").append(v);
		return sb.toString();
	}

	public void writeSystemMetrics(String dirPath) throws IOException {
		FileUtil.checkDir(dirPath, true, false);

		writeSystemMetric(this.a2a, "a2a", dirPath);
		writeSystemMetric(this.edgeA2a, "edgea2a", dirPath);
		writeSystemMetric(this.weightedEdgeA2a, "weightedEdgea2a", dirPath);
		writeSystemMetric(this.edgeA2aMin, "edgea2a40", dirPath);
		writeSystemMetric(this.weightedEdgeA2aMin, "weightedEdgea2a40", dirPath);
		writeSystemMetric(this.edgeA2aMin, "edgea2aMin", dirPath);
		writeSystemMetric(this.weightedEdgeA2aMin, "weightedEdgea2aMin", dirPath);
		writeSystemMetric(dirPath + File.separator + "mojofm.csv", this.mojoFm);

		this.cvg.writeFullCsv(dirPath + File.separator + "cvg.csv");
		this.cvg.writeSubCsv(dirPath + File.separator + "cvgMajor.csv",
			Version.IncrementType.MAJOR);
		this.cvg.writeSubCsv(dirPath + File.separator + "cvgMinor.csv",
			Version.IncrementType.MINOR);
		this.cvg.writeSubCsv(dirPath + File.separator + "cvgPatch.csv",
			Version.IncrementType.PATCH);
		this.cvg.writeSubCsv(dirPath + File.separator + "cvgPatchminor.csv",
			Version.IncrementType.PATCHMINOR);
		this.cvg.writeSubCsv(dirPath + File.separator + "cvgPre.csv",
			Version.IncrementType.SUFFIX);
		this.cvg.writeMinMajorCsv(dirPath + File.separator + "cvgMinMajor.csv");
	}

	private void writeSystemMetric(SystemData data, String name, String dirPath)
			throws IOException {
		data.writeFullCsv(dirPath + File.separator + name + ".csv");
		data.writeSubCsv(dirPath + File.separator + name + "Major.csv",
			Version.IncrementType.MAJOR);
		data.writeSubCsv(dirPath + File.separator + name + "Minor.csv",
			Version.IncrementType.MINOR);
		data.writeSubCsv(dirPath + File.separator + name + "Patch.csv",
			Version.IncrementType.PATCH);
		data.writeSubCsv(dirPath + File.separator + name + "Patchminor.csv",
			Version.IncrementType.PATCHMINOR);
		data.writeSubCsv(dirPath + File.separator + name + "Pre.csv",
			Version.IncrementType.SUFFIX);
		data.writeMinMajorCsv(dirPath + File.separator + name + "MinMajor.csv");
	}

	private void writeSystemMetric(String path, double[][] metric)
			throws IOException {
		try (FileWriter writer = new FileWriter(path)) {
			for (Version version : this.versions)
				writer.write("," + version);
			writer.write(System.lineSeparator());

			for (int i = 0; i < this.versions.length; i++) {
				writer.write(this.versions[i].toString());
				for (int j = 0; j < this.versions.length; j++) {
					writer.write(",");
					if (i > j)
						writer.write(Double.toString(metric[j][i - j - 1]));
					else if (i < j)
						writer.write(Double.toString(metric[i][j - i - 1]));
				}
				writer.write(System.lineSeparator());
			}
		}
	}

	public void serialize(String path) throws IOException {
		try (EnhancedJsonGenerator generator = new EnhancedJsonGenerator(path)) {
			serialize(generator);
		}
	}

	@Override
	public void serialize(EnhancedJsonGenerator generator) throws IOException {
		generator.writeField("archs", this.versionMetrics.values());
		generator.writeField("versions", Arrays.stream(this.versions)
			.map(Version::toString).collect(Collectors.toList()));
		generator.writeField("a2a", this.a2a.metric);
		generator.writeField("edgea2a", this.edgeA2a.metric);
		generator.writeField("weightedEdgea2a", this.weightedEdgeA2a.metric);
		generator.writeField("edgea2a40", this.edgeA2a40.metric);
		generator.writeField("weightedEdgea2a40", this.weightedEdgeA2a40.metric);
		generator.writeField("edgea2aMin", this.edgeA2aMin.metric);
		generator.writeField("weightedEdgea2aMin", this.weightedEdgeA2aMin.metric);
		generator.writeField("cvgF", this.cvg.cvgForwards);
		generator.writeField("cvgB", this.cvg.cvgBackwards);
		generator.writeField("mojo", this.mojoFm);
	}

	public static SystemMetrics deserialize(String path) throws IOException {
		try (EnhancedJsonParser parser = new EnhancedJsonParser(path)) {
			return deserialize(parser);
		}
	}

	public static SystemMetrics deserialize(EnhancedJsonParser parser)
			throws IOException {
		List<ArchitectureMetrics> versionMetricsCollection =
			new ArrayList<>(parser.parseCollection(ArchitectureMetrics.class));
		String[] versionStrings = parser.parseStringArray();
		Version[] versions = Arrays.stream(versionStrings)
			.map(Version::new).toArray(Version[]::new);

		Map<Version, ArchitectureMetrics> versionMetrics = new TreeMap<>();
		for (int i = 0; i < versions.length; i++)
			versionMetrics.put(versions[i], versionMetricsCollection.get(i));

		double[][] a2a = parser.parseDoubleMatrix();
		double[][] edgeA2a = parser.parseDoubleMatrix();
		double[][] weightedEdgeA2a = parser.parseDoubleMatrix();
		double[][] edgeA2a40 = parser.parseDoubleMatrix();
		double[][] weightedEdgeA2a40 = parser.parseDoubleMatrix();
		double[][] edgeA2aMin = parser.parseDoubleMatrix();
		double[][] weightedEdgeA2aMin = parser.parseDoubleMatrix();
		double[][] cvgForwards = parser.parseDoubleMatrix();
		double[][] cvgBackwards = parser.parseDoubleMatrix();
		double[][] mojoFm = parser.parseDoubleMatrix();

		return new SystemMetrics(versionMetrics, versions,
			new A2aSystemData(versions, a2a), new EdgeA2aSystemData(versions, edgeA2a),
			new WeightedEdgeA2aSystemData(versions, weightedEdgeA2a),
			new EdgeA2aSystemData(versions, edgeA2a40),
			new WeightedEdgeA2aSystemData(versions, weightedEdgeA2a40),
			new EdgeA2aSystemData(versions, edgeA2aMin),
			new WeightedEdgeA2aSystemData(versions, weightedEdgeA2aMin),
			new CvgSystemData(versions, cvgForwards, cvgBackwards), mojoFm);
	}
	//endregion
}
