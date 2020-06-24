package edu.usc.softarch.arcade.decay;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import edu.usc.softarch.arcade.clustering.StringGraph;
import edu.usc.softarch.arcade.clustering.util.ClusterUtil;
import edu.usc.softarch.arcade.config.Config;
import edu.usc.softarch.arcade.facts.ConcernCluster;
import edu.usc.softarch.arcade.facts.driver.ConcernClusterRsf;
import edu.usc.softarch.arcade.util.FileUtil;
import edu.usc.softarch.arcade.util.LogUtil;

import com.beust.jcommander.JCommander;
import com.google.common.base.Joiner;
public class ReflexionMethod {
	static Logger logger = Logger.getLogger(ReflexionMethod.class);
	
	public static void main(String[] args) throws FileNotFoundException {
		PropertyConfigurator.configure(Config.getLoggingConfigFilename());
		LogUtil.printLogFiles();
		
		VCOptions vco = new VCOptions();
		new JCommander(vco,args);
		
		String clustersFilename = FileUtil.tildeExpandPath(vco.clustersFilename);
		String depsFilename = FileUtil.tildeExpandPath(vco.depsFilename);
		String expectedDepsFilename = FileUtil.tildeExpandPath(vco.expectedDepsFilename);
		String ignoreClustersFilename = null;
		if (vco.ignoredClustersFilename != null) {
			ignoreClustersFilename = FileUtil
					.tildeExpandPath(vco.ignoredClustersFilename);
		}
		String outputFilename = FileUtil.tildeExpandPath(vco.outputFilename);
		
		Set<ConcernCluster> clusters = ConcernClusterRsf.extractConcernClustersFromRsfFile(clustersFilename);
		
		boolean showBuiltClusters = true;
		if (showBuiltClusters) {
			logger.debug("Found and built clusters:");
			for (ConcernCluster cluster : clusters) {
				logger.debug(cluster.getName());
			}
		}
		
		SimpleDirectedGraph<String, DefaultEdge> actualGraph = ClusterUtil.buildSimpleDirectedGraph(
				depsFilename, clusters);
		
		SimpleDirectedGraph<String, DefaultEdge> expectedGraph = ClusterUtil.buildConcernClustersDiGraph(
				clusters, expectedDepsFilename);
		
		
		Set<Pair<String,String>> convergenceEdges = new LinkedHashSet<Pair<String,String>>();
		Set<Pair<String,String>> divergenceEdges = new LinkedHashSet<Pair<String,String>>();
		Set<Pair<String,String>> absentEdges = new LinkedHashSet<Pair<String,String>>();
		
		// divergence edges start with all actual edges
		for (DefaultEdge actualEdge : actualGraph.edgeSet()) {
			String actualSrc = actualGraph.getEdgeSource(actualEdge);
			String actualTgt = actualGraph.getEdgeTarget(actualEdge);
			Pair<String,String> actualPair = new ImmutablePair<String,String>(actualSrc,actualTgt);
			divergenceEdges.add(actualPair);
		}
		
		for (DefaultEdge actualEdge : actualGraph.edgeSet()) {
			String actualSrc = actualGraph.getEdgeSource(actualEdge);
			String actualTgt = actualGraph.getEdgeTarget(actualEdge);
			Pair<String,String> actualPair = new ImmutablePair<String,String>(actualSrc,actualTgt);
			boolean foundMatchingEdge = false;
			Pair<String,String> expectedPair = null;
			for (DefaultEdge expectedEdge : expectedGraph.edgeSet()) {
				String expectedSrc = expectedGraph.getEdgeSource(expectedEdge);
				String expectedTgt = expectedGraph.getEdgeTarget(expectedEdge);
				expectedPair = new ImmutablePair<String,String>(expectedSrc,expectedTgt);
				if (actualSrc.equals(expectedSrc) && actualTgt.equals(expectedTgt)) {
					convergenceEdges.add(actualPair);
					foundMatchingEdge = true;
				}
			}
			// no matching eges of an expected pair is an absence
			if (!foundMatchingEdge && expectedPair != null) {
				absentEdges.add(expectedPair);
			}
		}
		
		// all actual edges - converge edges = divergence edges
		divergenceEdges.removeAll(convergenceEdges);
		
		PrintWriter writer = new PrintWriter(new File(outputFilename));
		writer.println("Convergence edges:");
		writer.println(Joiner.on("\n").join(convergenceEdges));
		writer.println();
		writer.println("Divergence edges:");
		writer.println(Joiner.on("\n").join(divergenceEdges));
		writer.println();
		writer.println("Absent edges:");
		writer.println(Joiner.on("\n").join(absentEdges));
		writer.close();
		
		
		
	}

}
