package acdc;

import javax.swing.tree.DefaultMutableTreeNode;

import java.util.Iterator;
import java.util.List;

public class DownInducer extends Pattern {
	public DownInducer(DefaultMutableTreeNode _root) {
		super(_root);
	}
	
	public void execute() {
		// Remove all but fine-grain clusters from the tree	
		List<Node> allNodes = allNodes(root);
		Iterator<Node> iv = allNodes.iterator();
		
		while (iv.hasNext()) {
			Node parent = iv.next();
			DefaultMutableTreeNode tparent = parent.getTreeNode();
			if (parent.isCluster()) {
				List<Node> subTree = nodeChildren(tparent);
				tparent.removeAllChildren();
				tparent.removeFromParent();
				Iterator<Node> is = subTree.iterator();
				boolean hasChildrenFiles = false;
				while (is.hasNext()) {
					Node child = is.next();
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