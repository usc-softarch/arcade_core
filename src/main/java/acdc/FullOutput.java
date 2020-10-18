package acdc;

import javax.swing.tree.DefaultMutableTreeNode;

import java.util.Iterator;
import java.util.List;

public class FullOutput extends Pattern {
	public FullOutput (DefaultMutableTreeNode _root, String _systemName) {
		super(_root);
		systemName = _systemName;
	}
	
	private String systemName;
	
	public void execute() {
		// Create an extra root here since OutputHandler ignores the root of the tree
		Node newDummy = new Node (systemName, "Dummy");
		DefaultMutableTreeNode newRoot = new DefaultMutableTreeNode (newDummy);
		newDummy.setTreeNode(newRoot);

		List<Node> rootChildren = nodeChildren(root);
		Iterator<Node> irC = rootChildren.iterator();
		while (irC.hasNext()) {
			Node n = irC.next();
			DefaultMutableTreeNode curr = n.getTreeNode();
			newRoot.add(curr);
		}

		root.add(newRoot);
	}
}