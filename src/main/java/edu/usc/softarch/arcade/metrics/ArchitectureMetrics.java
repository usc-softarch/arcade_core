package edu.usc.softarch.arcade.metrics;

import edu.usc.softarch.arcade.clustering.data.ReadOnlyArchitecture;
import edu.usc.softarch.arcade.clustering.data.ReadOnlyCluster;
import edu.usc.softarch.arcade.metrics.decay.ArchitecturalStability;
import edu.usc.softarch.arcade.metrics.decay.BasicMQ;
import edu.usc.softarch.arcade.metrics.decay.InterConnectivity;
import edu.usc.softarch.arcade.metrics.decay.IntraConnectivity;
import edu.usc.softarch.arcade.metrics.decay.TurboMQ;
import edu.usc.softarch.util.LabeledEdge;
import edu.usc.softarch.util.json.EnhancedJsonGenerator;
import edu.usc.softarch.util.json.EnhancedJsonParser;
import edu.usc.softarch.util.json.JsonSerializable;
import mojo.MoJoCalculator;
import org.jgrapht.graph.SimpleDirectedGraph;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class ArchitectureMetrics implements JsonSerializable {
	//region ATTRIBUTES
	private final Map<String, ClusterMetrics> clusterMetrics;

	public final double intraConnectivity;
	public final double interConnectivity;
	public final double basicMq;
	public final double turboMq;
	public final double instability;
	public final double mojoFmGt;
	//endregion

	//region CONSTRUCTORS
	public ArchitectureMetrics(String archPath, String depsPath)
			throws IOException {
		this(archPath, depsPath, "");
	}

	public ArchitectureMetrics(String archPath, String depsPath,
			String gtPath) throws IOException {
		ReadOnlyArchitecture arch = ReadOnlyArchitecture.readFromRsf(archPath);

		SimpleDirectedGraph<String, LabeledEdge> graph =
			arch.buildFullGraph(depsPath);

		this.clusterMetrics = new TreeMap<>();
		for (ReadOnlyCluster cluster : arch.values())
			this.clusterMetrics.put(
				cluster.name, new ClusterMetrics(cluster, arch, graph));

		this.intraConnectivity =
			IntraConnectivity.computeIntraConnectivity(arch, graph);
		this.interConnectivity =
			InterConnectivity.computeInterConnectivity(arch, graph);
		this.basicMq = BasicMQ.computeBasicMq(arch, graph);
		this.turboMq = TurboMQ.computeTurboMq(arch, graph);
		this.instability = ArchitecturalStability.computeStability(arch, graph);

		if (gtPath.isEmpty())
			this.mojoFmGt = -1.0;
		else {
			MoJoCalculator mojoCalc = new MoJoCalculator(archPath, gtPath, null);
			this.mojoFmGt = mojoCalc.mojofm();
		}
	}

	private ArchitectureMetrics(Map<String, ClusterMetrics> clusterMetrics,
			double intraConnectivity, double interConnectivity, double basicMq,
			double turboMq, double instability, double mojoFmGt) {
		this.clusterMetrics = clusterMetrics;
		this.intraConnectivity = intraConnectivity;
		this.interConnectivity = interConnectivity;
		this.basicMq = basicMq;
		this.turboMq = turboMq;
		this.instability = instability;
		this.mojoFmGt = mojoFmGt;
	}
	//endregion

	//region ACCESSORS
	public Collection<ClusterMetrics> getClusterMetrics() {
		return this.clusterMetrics.values(); }
	//endregion

	//region SERIALIZATION
	@Override
	public void serialize(EnhancedJsonGenerator generator) throws IOException {
		generator.writeField("clusters", clusterMetrics.values());
		generator.writeField("intra", intraConnectivity);
		generator.writeField("inter", interConnectivity);
		generator.writeField("basicMq", basicMq);
		generator.writeField("turboMq", turboMq);
		generator.writeField("insta", instability);
		generator.writeField("mojogt", mojoFmGt);
	}

	public static ArchitectureMetrics deserialize(EnhancedJsonParser parser)
			throws IOException {
		Collection<ClusterMetrics> clusterMetricsCollection =
			parser.parseCollection(ClusterMetrics.class);

		Map<String, ClusterMetrics> clusterMetrics =
			clusterMetricsCollection.stream()
				.collect(Collectors.toMap(c -> c.clusterName, c -> c));

		double intraConnectivity = parser.parseDouble();
		double interConnectivity = parser.parseDouble();
		double basicMq = parser.parseDouble();
		double turboMq = parser.parseDouble();
		double instability = parser.parseDouble();
		double mojoFmGt = parser.parseDouble();

		return new ArchitectureMetrics(clusterMetrics, intraConnectivity,
			interConnectivity, basicMq, turboMq, instability, mojoFmGt);
	}
	//endregion
}
