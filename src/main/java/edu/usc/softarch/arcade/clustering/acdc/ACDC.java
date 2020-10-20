package edu.usc.softarch.arcade.clustering.acdc;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This application facilitates the task of recovering the structure
 * of large and complex software systems in an automatic way by clustering.
 * 
 * The data structure used to represent the components of the software system
 * is a tree, which is clustered incrementally by using several patterns. 
 * 
 * Assumptions made:
 * ACDC will cluster the children of the root in the input.
 * Once an object has been clustered, it is never removed from its cluster
 * (but it might get further clustered within its cluster)
 */
public class ACDC {
	private static final Logger logger = LogManager.getLogger(ACDC.class);

  public static void main(String [] args) {
		run(args[0], args[1]);
	}
	
	public static void run(String inputName, String outputName) {
		IO.set_debug_level(0);
    int maxClusterSize = 20; //used by SubGraph pattern
		InputHandler input = new TAInput();
		OutputHandler output = new RSFOutput();

		String selectedPatterns = "bso";
	
		// Create a tree with a dummy root
		Node dummy = new Node ("ROOT", "Dummy");
		DefaultMutableTreeNode root = new DefaultMutableTreeNode (dummy);
		dummy.setTreeNode(root);
		Pattern inducer = new DownInducer(root);
			   	
    logger.info("Input File: " + inputName);
    logger.info("Output File: " + outputName);
    logger.info("Patterns: " + selectedPatterns);
    logger.info("Cluster Size: " + maxClusterSize);

		// Populate the tree from the input file   
		input.readInput(inputName, root);
		
		List<Pattern> vpatterns = new ArrayList<>();
		vpatterns.add(new BodyHeader(root));
		vpatterns.add(new SubGraph(root,maxClusterSize));
		vpatterns.add(new OrphanAdoption(root));

		// Induce all edges
		List<Node> allNodes = Pattern.allNodes(root);
		Pattern.induceEdges(allNodes);

		// Execute the patterns
		for (Pattern p : vpatterns) {
			logger.info("Executing " + p.getName() + " pattern...");
			p.execute();
    }

		// Take care of any objects that were not clustered
		Pattern c = new ClusterLast(root);
		c.execute();
	
    // Create output file
		inducer.execute();
		output.writeOutput(outputName, root);
	}
}