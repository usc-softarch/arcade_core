package acdc;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

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
  public static void main(String [] args) {
		IO.set_debug_level(0);
    
		String inputName;
		String outputName;
    
    int maxClusterSize = 20; //used by SubGraph pattern
    
		inputName = args[0]; 
    outputName = args[1];
 
		InputHandler input = new TAInput();
		OutputHandler output = new RSFOutput();
			
		// If it got to this line, we are dealing with at least two args
		// Code executes for 2 or more arguments 

		String selectedPatterns = "bso";
	
		// Create a tree with a dummy root
		Node dummy = new Node ("ROOT", "Dummy");
		DefaultMutableTreeNode root = new DefaultMutableTreeNode (dummy);
		dummy.setTreeNode(root);
		Pattern inducer = new DownInducer(root);
			   	
    IO.put("Input File: " + inputName,1);
    IO.put("Output File: " + outputName,1);
    IO.put("Patterns: " + selectedPatterns,1);
    IO.put("Cluster Size: " + maxClusterSize,1);

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
			IO.put("Executing " + p.getName() + " pattern...",1);
			p.execute();
    }

		// Take care of any objects that were not clustered
		Pattern c = new ClusterLast(root);
		c.execute();
	
    // Create output file
		IO.put("Creating output...",1);
		inducer.execute();
		output.writeOutput(outputName, root);
		IO.put("Finished!",1);
  }
}