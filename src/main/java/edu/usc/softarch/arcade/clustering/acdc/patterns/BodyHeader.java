package edu.usc.softarch.arcade.clustering.acdc.patterns;

import edu.usc.softarch.arcade.clustering.acdc.data.Node;

import java.util.ArrayList;
import java.util.Collection;
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
	public BodyHeader(DefaultMutableTreeNode root) {
		super(root); }

	public void execute() {
		List<Node> vModified = new ArrayList<>(); //will contain nodes which were moved
		Collection<Node> vTree = allNodes(root);
		
		//traverse the files with extension .c or .h in the tree looking for
		//their counterpart file with extension .h and respectively .c 		
		for (Node ncurr : vTree) {
			DefaultMutableTreeNode curr = ncurr.getTreeNode();
                              			
      if (ncurr.isFile() || ncurr.getName().endsWith(".c")
					|| ncurr.getName().endsWith(".h")) {
				//if the current .h or .c file is not in the vector vTree, it means that its
				//counterpart file was checked and a cluster containing has been created
				DefaultMutableTreeNode currParent =
					(DefaultMutableTreeNode) curr.getParent();

				if (!alreadyClustered(currParent)) {
					String toFind = "";
					// Loop through the vector of remaining .h and .c
					// files to find counterpart file
					for (Node vnode : vTree) {
						DefaultMutableTreeNode vtnode = vnode.getTreeNode();
						if (ncurr.getName().endsWith(".c"))
							toFind = ncurr.getName().substring(0,
								ncurr.getName().length() - 2) + ".h";
						else if (ncurr.getName().endsWith(".h"))
							toFind = ncurr.getName().substring(0,
								ncurr.getName().length() - 2) + ".c";

						if (vnode.getName().equalsIgnoreCase(toFind)) {
							String filename =
								ncurr.getName().substring(0, ncurr.getName().length() - 2);
							//create the new cluster node which will have extension .ch
							Node clusterNode = new Node(filename + ".ch", "cModule");
							DefaultMutableTreeNode tcluster =
								new DefaultMutableTreeNode(clusterNode);
							clusterNode.setTreeNode(tcluster);

							//add the new cluster node under the parent of the current node in the traversal
							currParent.add(tcluster);

							//make the files with extension .c and .h children of the new cluster node
							tcluster.add(curr);
							tcluster.add(vtnode);

							Enumeration<TreeNode> evt = vtnode.breadthFirstEnumeration();
							while(evt.hasMoreElements()) {
								DefaultMutableTreeNode ec = (DefaultMutableTreeNode)evt.nextElement();
								if(!vModified.contains(ec.getUserObject()))
									vModified.add((Node) ec.getUserObject());
							}

							Enumeration<TreeNode> ecurr = curr.breadthFirstEnumeration();
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
	  induceEdges(vModified);
	}

	private boolean alreadyClustered(DefaultMutableTreeNode currParent) {
		Node ncurrParent = (Node) currParent.getUserObject();
		boolean isCModule = ncurrParent.getName().endsWith(".ch");
		boolean hasTwoKids = (currParent.getChildCount() == 2);
		DefaultMutableTreeNode firstChild =
			(DefaultMutableTreeNode) currParent.getFirstChild();
		Node nfirstChild = (Node)firstChild.getUserObject();
		DefaultMutableTreeNode secondChild =
			(DefaultMutableTreeNode) currParent.getLastChild();
		Node nsecondChild = (Node)secondChild.getUserObject();
		boolean sameBaseNames =
			ncurrParent.getBaseName().equalsIgnoreCase(nfirstChild.getBaseName()) &&
				ncurrParent.getBaseName().equalsIgnoreCase(nsecondChild.getBaseName());
		boolean dotCHFiles = (nfirstChild.getName().endsWith(".c") && nsecondChild.getName().endsWith(".h")) ||
			(nfirstChild.getName().endsWith(".h") && nsecondChild.getName().endsWith(".c"));
							 
		return isCModule && hasTwoKids && sameBaseNames && dotCHFiles;
	}
}
