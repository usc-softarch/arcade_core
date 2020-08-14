package edu.usc.softarch.arcade.util.graph;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections4.Factory;
import org.apache.commons.collections4.functors.StringValueTransformer;

import edu.uci.ics.jung.algorithms.generators.random.BarabasiAlbertGenerator;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.io.GraphMLWriter;

public class SimpleGraphGenerator {

	static Factory<DirectedGraph<String,Integer>> graphFactory = 
	    	new Factory<DirectedGraph<String,Integer>>() {

				public DirectedGraph<String, Integer> create() {
					return new DirectedSparseMultigraph<>();
				}
	};

	static Factory<Integer> edgeFactory = new Factory<Integer>() {
		int i = 0;

		public Integer create() {
			return i++;
		}
	};

	static Factory<String> vertexFactory = new Factory<String>() {
		int i = 0;

		public String create() {
			return "V" + i++;
		}
	};
	
	//TODO BarabasiAlbertGenerator no longer works with version bumps,
	//TODO check if this method is still used for anything.
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// int numInitVertices = 1;
		// int numEdgesToAttach = 5;
		// int seed = 2;
		// Set<String> seedVertices = new HashSet<>();
		
		// BarabasiAlbertGenerator bag = new BarabasiAlbertGenerator(
		// 		graphFactory,
    //             vertexFactory,
    //             edgeFactory,
    //             numInitVertices,
    //             numEdgesToAttach,
    //             seed,
    //             seedVertices);
		
		// bag.evolveGraph(5);
		// Graph graph = bag.create();
		// System.out.println(graph);
		
		// try {
		// 	FileWriter fileWriter = new FileWriter("data" + File.separator + "generated" + File.separator + "s" + seed + "v" + numInitVertices + "e" + numEdgesToAttach + ".graphml");
		// 	GraphMLWriter gmlWriter = new GraphMLWriter();
		// 	gmlWriter.setEdgeIDs(StringValueTransformer.getInstance());
		// 	gmlWriter.save(graph,fileWriter);
		// } catch (IOException e) {
		// 	// TODO Auto-generated catch block
		// 	e.printStackTrace();
		// }

	}

}
