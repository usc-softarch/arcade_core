package edu.usc.softarch.arcade.metrics;

import edu.usc.softarch.arcade.clustering.data.ReadOnlyArchitecture;
import edu.usc.softarch.arcade.clustering.data.ReadOnlyCluster;
import edu.usc.softarch.arcade.metrics.decay.ArchitecturalStability;
import edu.usc.softarch.arcade.metrics.decay.BasicMQ;
import edu.usc.softarch.arcade.metrics.decay.InterConnectivity;
import edu.usc.softarch.arcade.metrics.decay.IntraConnectivity;
import edu.usc.softarch.arcade.metrics.decay.TurboMQ;
import edu.usc.softarch.util.LabeledEdge;
import mojo.MoJoCalculator;
import org.jgrapht.graph.SimpleDirectedGraph;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class ArchitectureMetrics {
	//region ATTRIBUTES
	private final Collection<ClusterMetrics> clusterMetrics;

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

		this.clusterMetrics = new ArrayList<>();
		for (ReadOnlyCluster cluster : arch.values())
			this.clusterMetrics.add(new ClusterMetrics(cluster, arch, graph));

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
	//endregion

	//region ACCESSORS
	public Collection<ClusterMetrics> getClusterMetrics() {
		return new ArrayList<>(this.clusterMetrics); }
	//endregion
}
