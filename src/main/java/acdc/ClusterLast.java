package acdc;

import javax.swing.tree.DefaultMutableTreeNode;

/**
* This pattern puts together all orphans under one cluster node named
* "orphanContainer", which in turn is inserted under the root of the tree.
*/
public class ClusterLast extends Pattern {
	public ClusterLast(DefaultMutableTreeNode root) {
		super(root);
		name = "Cluster Last";
	}

	public void execute() {
		if (orphanNumber() != 0) {
			IO.put("Clustering leftover nodes",2);
			// Create a node of type Subsystem which will contain all the remaining unclustered orphans 
			Node nOrphanContainer = new Node("orphanContainer.ss", "Subsystem");
			DefaultMutableTreeNode orphanContainer = new DefaultMutableTreeNode(nOrphanContainer);
			nOrphanContainer.setTreeNode(orphanContainer);
			root.add(orphanContainer);

			for (Node ncurr : orphans()) {
				DefaultMutableTreeNode curr = ncurr.getTreeNode();

				if (!ncurr.isCluster()) {
					orphanContainer.add(curr);
					IO.put("contain\t\t" + nOrphanContainer.getName() + "\t" + ncurr.getName(),	2);
				}
			}
		}
	}
}