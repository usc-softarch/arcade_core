package edu.usc.softarch.arcade.metrics;

import edu.usc.softarch.arcade.clustering.data.ReadOnlyArchitecture;
import edu.usc.softarch.arcade.clustering.data.ReadOnlyCluster;
import edu.usc.softarch.arcade.metrics.decay.ArchitecturalStability;
import edu.usc.softarch.arcade.metrics.decay.InterConnectivity;
import edu.usc.softarch.arcade.metrics.decay.IntraConnectivity;
import edu.usc.softarch.arcade.metrics.decay.TurboMQ;
import edu.usc.softarch.util.LabeledEdge;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.jgrapht.graph.SimpleDirectedGraph;

public class ClusterMetrics {
	//region ATTRIBUTES
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
	//endregion
}
