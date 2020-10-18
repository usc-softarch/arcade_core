package acdc;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

/**
 * This pattern lumps a header file (a .h file) and a body file (a .c file)
 * into a cluster. The cluster is named using the common part of the component
 * files names followed by the suffix ".ch". 
 * If clustering has already been done, a message is output to  the user
 * Commented out lines that modify vTree on the fly
 */
 public class BodyHeader extends Pattern {
	public BodyHeader(DefaultMutableTreeNode _root) {
		super(_root);
		name = "Body Header";
	}

	public void execute() {
		List<Node> vModified = new ArrayList<>(); //will contain nodes which were moved
		List<Node> vTree = allNodes(root);
		
		//traverse the files with extension .c or .h in the tree looking for
		//their counterpart file with extension .h and respectively .c 		
		for (int i=0; i<vTree.size(); i++) {
			Node ncurr = vTree.get(i);
			DefaultMutableTreeNode curr = ncurr.getTreeNode();
                              			
      if (ncurr.isFile())	{
				IO.put("Considering file: " + ncurr.getName(),2);
				//if the current .h or .c file is not in the vector vTree, it means that its
				//counterpart file was checked and a cluster containing has been created 
				if (((ncurr.getName().endsWith(".c")) || (ncurr.getName().endsWith(".h")))) {
					DefaultMutableTreeNode curr_parent =
						(DefaultMutableTreeNode) curr.getParent();

					if (alreadyClustered(curr_parent))
						IO.put("\tAlready in a body-header cluster",2);
					else {	
						String toFind = "";
						// Loop through the vector of remaining .h and .c 
						// files to find counterpart file
						for(int j = 0; j < vTree.size(); j++) {	
							Node vnode = vTree.get(j);
							IO.put("\tConsidering other file: " + vnode.getName(),2);
							DefaultMutableTreeNode vtnode = vnode.getTreeNode();							
							if((ncurr.getName().endsWith(".c")))
								toFind = ncurr.getName().substring(0, ncurr.getName().length()-2) + ".h";
							else if((ncurr.getName().endsWith(".h")))
							  toFind = ncurr.getName().substring(0, ncurr.getName().length()-2) + ".c";
							
							if(vnode.getName().equalsIgnoreCase(toFind)) {	   
								String filename = ncurr.getName().substring(0, ncurr.getName().length()-2);
								//create the new cluster node which will have extension .ch
								Node cluster_node = new Node(filename + ".ch", "cModule");
								DefaultMutableTreeNode tcluster = new DefaultMutableTreeNode(cluster_node);
								cluster_node.setTreeNode(tcluster);
								
								//add the new cluster node under the parent of the current node in the traversal
								curr_parent.add(tcluster);
								
								//make the files with extension .c and .h children of the new cluster node
								tcluster.add(curr);
								tcluster.add(vtnode);
								
								IO.put("\tA cluster called " +
									cluster_node.getName() + " was created containing " +
                  ncurr.getName() + ", " + vnode.getName(),2);
								
		    				Enumeration<TreeNode> evt = vtnode.breadthFirstEnumeration();
							  while(evt.hasMoreElements()) {
		    					DefaultMutableTreeNode ec = (DefaultMutableTreeNode)evt.nextElement();
								  if(!vModified.contains(ec.getUserObject()))
										vModified.add((Node) ec.getUserObject());
							  }
		    
							  Enumeration<TreeNode> ecurr= curr.breadthFirstEnumeration();
							  while(ecurr.hasMoreElements()) {
		    					DefaultMutableTreeNode em = (DefaultMutableTreeNode)ecurr.nextElement();
								  if(!vModified.contains(em.getUserObject()))
								    vModified.add((Node) em.getUserObject());
							  }
								break;				
							}
						}
					}
      	}
			}
		}
	  induceEdges(vModified);
	}

	private boolean alreadyClustered(DefaultMutableTreeNode curr_parent) {	
		Node ncurr_parent = (Node) curr_parent.getUserObject();
		boolean isCModule = ncurr_parent.getName().endsWith(".ch");
		boolean hasTwoKids = (curr_parent.getChildCount() == 2);
		DefaultMutableTreeNode first_child = (DefaultMutableTreeNode) curr_parent.getFirstChild();
		Node nfirst_child = (Node)first_child.getUserObject();
		DefaultMutableTreeNode second_child = (DefaultMutableTreeNode) curr_parent.getLastChild();
		Node nsecond_child = (Node)second_child.getUserObject();
		boolean sameBaseNames = ncurr_parent.getBaseName().equalsIgnoreCase(nfirst_child.getBaseName()) &&
			ncurr_parent.getBaseName().equalsIgnoreCase(nsecond_child.getBaseName());
		boolean dotCHFiles = (nfirst_child.getName().endsWith(".c") && nsecond_child.getName().endsWith(".h")) ||
			(nfirst_child.getName().endsWith(".h") && nsecond_child.getName().endsWith(".c"));
							 
		return isCModule && hasTwoKids && sameBaseNames && dotCHFiles;
	}
}