package acdc;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.*;

public class UpInducer extends Pattern {
	public UpInducer (DefaultMutableTreeNode _root)	{
		super(_root);
	}
	
	public void execute() {
		// Remove intermediate clusters from the tree
		List<Node> rootChildren = nodeChildren(root);
		Iterator<Node> iv = rootChildren.iterator();
		
		while (iv.hasNext()) {
			Node parent = iv.next();
			DefaultMutableTreeNode tparent = parent.getTreeNode();
			List<Node> subTree = allNodes(tparent);
			tparent.removeAllChildren();
			Iterator<Node> is = subTree.iterator();
			while (is.hasNext()) {
				Node child = is.next();
				if (child.isFile()) {
					DefaultMutableTreeNode tchild = child.getTreeNode();
					tchild.removeAllChildren();
					tparent.add(tchild);
				}
			}
		}
	}
}
