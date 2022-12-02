package edu.usc.softarch.arcade.metrics;

import edu.usc.softarch.arcade.util.CLI;
import edu.usc.softarch.arcade.util.FileUtil;
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
	private final Map<String, ArchitectureMetrics> versionMetrics;

	private final String[] versions;
	private final double[][] a2a;
	private final double[][] cvgForwards;
	private final double[][] cvgBackwards;
	private final double[][] mojoFm;
	//endregion

	//region CONSTRUCTORS
	public SystemMetrics(String systemDirPath, String depsDirPath)
			throws IOException {
		// Get and sort the architecture files
		List<File> archFiles = new ArrayList<>();
		List<File> archDirs = List.of(
			new File(FileUtil.tildeExpandPath(systemDirPath)).listFiles());
		for (File archDir : archDirs) {
			archFiles.add(Arrays.stream(archDir.listFiles())
				.filter(f -> f.getName().contains(".rsf"))
				.findFirst().get());
		}
		archFiles = FileUtil.sortFileListByVersion(archFiles);

		// Get and sort the dependency files
		List<File> depsFiles = List.of(
			new File(FileUtil.tildeExpandPath(depsDirPath)).listFiles());
		depsFiles = depsFiles.stream()
			.filter(f -> f.getName().contains(".rsf")).collect(Collectors.toList());
		depsFiles = FileUtil.sortFileListByVersion(depsFiles);

		// Initialize the version list from the version directories
		this.versions = new String[archDirs.size()];
		archDirs = FileUtil.sortFileListByVersion(archDirs);
		for (int i = 0; i < archDirs.size(); i++)
			this.versions[i] = archDirs.get(i).getName();

		// Run a2a
		this.a2a = new double[this.versions.length - 1][];
		for (int i = 0; i < this.versions.length - 1; i++) {
			this.a2a[i] = new double[this.versions.length - 1 - i];

			for (int j = i + 1; j < this.versions.length; j++)
				this.a2a[i][j - i - 1] =
					SystemEvo.run(archFiles.get(i), archFiles.get(j));
		}

		// Run cvg
		this.cvgForwards = new double[this.versions.length - 1][];
		this.cvgBackwards = new double[this.versions.length - 1][];
		for (int i = 0; i < this.versions.length - 1; i++) {
			this.cvgForwards[i] = new double[this.versions.length - 1 - i];
			this.cvgBackwards[i] = new double[this.versions.length - 1 - i];

			for (int j = i + 1; j < this.versions.length; j++) {
				this.cvgForwards[i][j - i - 1] =
					Cvg.run(archFiles.get(i), archFiles.get(j));
				this.cvgBackwards[i][j - i - 1] =
					Cvg.run(archFiles.get(j), archFiles.get(i));
			}
		}

		// Run MoJoFM
		this.mojoFm = new double[this.versions.length - 1][];
		for (int i = 0; i < this.versions.length - 1; i++) {
			this.mojoFm[i] = new double[this.versions.length - 1 - i];

			for (int j = i + 1; j < this.versions.length; j++) {
				MoJoCalculator mojoCalc = new MoJoCalculator(
					archFiles.get(i).getAbsolutePath(),
					archFiles.get(j).getAbsolutePath(), null);
				this.mojoFm[i][j - i - 1] = mojoCalc.mojofm();
			}
		}

		// Initialize ArchitectureMetrics
		this.versionMetrics = new TreeMap<>();
		for (int i = 0; i < this.versions.length; i++)
			this.versionMetrics.put(this.versions[i], new ArchitectureMetrics(
				archFiles.get(i).getAbsolutePath(),
				depsFiles.get(i).getAbsolutePath()));
	}

	private SystemMetrics(Map<String, ArchitectureMetrics> versionMetrics,
			String[] versions, double[][] a2a, double[][] cvgForwards,
			double[][] cvgBackwards, double[][] mojoFm) {
		this.versionMetrics = versionMetrics;
		this.versions = versions;
		this.a2a = a2a;
		this.cvgForwards = cvgForwards;
		this.cvgBackwards = cvgBackwards;
		this.mojoFm = mojoFm;
	}
	//endregion

	//region ACCESSORS
	public Collection<ArchitectureMetrics> getVersionMetrics() {
		return this.versionMetrics.values(); }
	public String[] getVersions() {
		return Arrays.copyOf(this.versions, this.versions.length); }
	public double[][] getA2a() {
		return Arrays.copyOf(this.a2a, this.a2a.length); }
	public double[][] getCvgForwards() {
		return Arrays.copyOf(this.cvgForwards, this.cvgForwards.length); }
	public double[][] getCvgBackwards() {
		return Arrays.copyOf(this.cvgBackwards, this.cvgBackwards.length); }
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
			for (String version : this.versions)
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
			for (String version : this.versions)
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

		this.writeSystemMetric(
			dirPath + File.separator + "a2a.csv", this.a2a);
		this.writeSystemMetric(
			dirPath + File.separator + "mojofm.csv", this.mojoFm);
		this.writeCvg(dirPath + File.separator + "cvg.csv");
	}

	private void writeSystemMetric(String path, double[][] metric)
			throws IOException {
		try (FileWriter writer = new FileWriter(path)) {
			for (String version : this.versions)
				writer.write("," + version);
			writer.write(System.lineSeparator());

			for (int i = 0; i < this.versions.length; i++) {
				writer.write(this.versions[i]);
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

	private void writeCvg(String path) throws IOException {
		try (FileWriter writer = new FileWriter(path)) {
			for (String version : this.versions)
				writer.write("," + version);
			writer.write(System.lineSeparator());

			for (int i = 0; i < this.versions.length; i++) {
				writer.write(this.versions[i]);
				for (int j = 0; j < this.versions.length; j++) {
					writer.write(",");
					if (i > j)
						writer.write(Double.toString(this.cvgBackwards[j][i - j - 1]));
					else if (i < j)
						writer.write(Double.toString(this.cvgForwards[i][j - i - 1]));
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
		generator.writeField("archs", versionMetrics.values());
		generator.writeField("versions", List.of(versions));
		generator.writeField("a2a", a2a);
		generator.writeField("cvgF", cvgForwards);
		generator.writeField("cvgB", cvgBackwards);
		generator.writeField("mojo", mojoFm);
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
		String[] versions = parser.parseStringArray();

		Map<String, ArchitectureMetrics> versionMetrics = new TreeMap<>();
		for (int i = 0; i < versions.length; i++)
			versionMetrics.put(versions[i], versionMetricsCollection.get(i));

		double[][] a2a = parser.parseDoubleMatrix();
		double[][] cvgForwards = parser.parseDoubleMatrix();
		double[][] cvgBackwards = parser.parseDoubleMatrix();
		double[][] mojoFm = parser.parseDoubleMatrix();

		return new SystemMetrics(versionMetrics, versions, a2a, cvgForwards,
			cvgBackwards, mojoFm);
	}
	//endregion
}
