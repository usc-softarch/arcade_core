package edu.usc.softarch.arcade.clustering.acdc;

import javax.swing.tree.DefaultMutableTreeNode;

import java.util.List;

public class DownInducer extends Pattern {
	public DownInducer(DefaultMutableTreeNode root) {
		super(root);
	}
	
	public void execute() {
		// Remove all but fine-grain clusters from the tree	
		for (Node parent : allNodes(root)) {
			DefaultMutableTreeNode tparent = parent.getTreeNode();
			if (parent.isCluster()) {
				List<Node> subTree = nodeChildren(tparent);
				tparent.removeAllChildren();
				tparent.removeFromParent();
				boolean hasChildrenFiles = false;
				for (Node child : subTree) {
					if (child.isFile()) {
						DefaultMutableTreeNode tchild = child.getTreeNode();
						tchild.removeAllChildren();
						tparent.add(tchild);
						hasChildrenFiles = true;
					}
				}
				if (hasChildrenFiles) root.add(tparent);
			}
			else tparent.removeAllChildren();
		}
	}
}