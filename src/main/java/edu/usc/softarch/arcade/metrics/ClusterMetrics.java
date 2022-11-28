package edu.usc.softarch.arcade.metrics;

import edu.usc.softarch.arcade.clustering.data.ReadOnlyArchitecture;
import edu.usc.softarch.arcade.clustering.data.ReadOnlyCluster;
import edu.usc.softarch.arcade.metrics.decay.ArchitecturalStability;
import edu.usc.softarch.arcade.metrics.decay.InterConnectivity;
import edu.usc.softarch.arcade.metrics.decay.IntraConnectivity;
import edu.usc.softarch.arcade.metrics.decay.TurboMQ;
import edu.usc.softarch.util.LabeledEdge;
import edu.usc.softarch.util.json.EnhancedJsonGenerator;
import edu.usc.softarch.util.json.EnhancedJsonParser;
import edu.usc.softarch.util.json.JsonSerializable;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.jgrapht.graph.SimpleDirectedGraph;

import java.io.IOException;

public class ClusterMetrics implements JsonSerializable {
	//region ATTRIBUTES
	public final String clusterName;
	public final double intraConnectivity;
	public final DescriptiveStatistics interConnectivity;
	public final double basicMq;
	public final double clusterFactor;
	public final double fanIn;
	public final double fanOut;
	public final double instability;
	//endregion

	//region CONSTRUCTORS
	public ClusterMetrics(ReadOnlyCluster cluster, ReadOnlyArchitecture arch,
			SimpleDirectedGraph<String, LabeledEdge> graph) {
		this.clusterName = cluster.name;
		this.intraConnectivity =
			IntraConnectivity.computeIntraConnectivity(cluster, graph);
		this.interConnectivity =
			InterConnectivity.computeInterConnectivity(cluster, arch, graph);
		this.basicMq = this.intraConnectivity - this.interConnectivity.getMean();
		this.clusterFactor = TurboMQ.computeClusterFactor(cluster, graph);
		this.fanIn = ArchitecturalStability.computeFanIn(cluster, graph);
		this.fanOut = ArchitecturalStability.computeFanOut(cluster, graph);
		this.instability = ArchitecturalStability.computeStability(cluster, graph);
	}

	private ClusterMetrics(String clusterName, double intraConnectivity,
			DescriptiveStatistics interConnectivity, double basicMq,
			double clusterFactor, double fanIn, double fanOut, double instability) {
		this.clusterName = clusterName;
		this.intraConnectivity = intraConnectivity;
		this.interConnectivity = interConnectivity;
		this.basicMq = basicMq;
		this.clusterFactor = clusterFactor;
		this.fanIn = fanIn;
		this.fanOut = fanOut;
		this.instability = instability;
	}
	//endregion

	//region SERIALIZATION
	@Override
	public void serialize(EnhancedJsonGenerator generator) throws IOException {
		generator.writeField("name", clusterName);
		generator.writeField("intra", intraConnectivity);
		generator.writeField("inter", interConnectivity.getValues());
		generator.writeField("basicMq", basicMq);
		generator.writeField("cf", clusterFactor);
		generator.writeField("fanIn", fanIn);
		generator.writeField("fanOut", fanOut);
		generator.writeField("insta", instability);
	}

	public static ClusterMetrics deserialize(EnhancedJsonParser parser)
			throws IOException {
		String clusterName = parser.parseString();
		double intraConnectivity = parser.parseDouble();
		DescriptiveStatistics interConnectivity =
			new DescriptiveStatistics(parser.parseDoubleArray());
		double basicMq = parser.parseDouble();
		double clusterFactor = parser.parseDouble();
		double fanIn = parser.parseDouble();
		double fanOut = parser.parseDouble();
		double instability = parser.parseDouble();

		return new ClusterMetrics(clusterName, intraConnectivity, interConnectivity,
			basicMq, clusterFactor, fanIn, fanOut, instability);
	}
	//endregion
}
