package edu.usc.softarch.arcade.clustering.acdc.patterns;

import edu.usc.softarch.arcade.clustering.acdc.data.Edge;
import edu.usc.softarch.arcade.clustering.acdc.data.Node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

public abstract class Pattern {
	//region ATTRIBUTES
	protected DefaultMutableTreeNode root;
	//endregion

	//region CONSTRUCTORS
	protected Pattern(DefaultMutableTreeNode root) {
		this.root = root; }
	//endregion

	//region PROCESSING
	public abstract void execute();

	/**
	 * Returns a collection of child nodes to the given parameter.
	 */
	protected Collection<Node> nodeChildren(DefaultMutableTreeNode node) {
		Collection<Node> result = new ArrayList<>();
		Collection<TreeNode> children = Collections.list(node.children());

		for (TreeNode child : children) {
			DefaultMutableTreeNode curr2 = (DefaultMutableTreeNode) child;
			Node ncurr2 = (Node) curr2.getUserObject();
			result.add(ncurr2);
		}

		return result;
	}

	/**
	 * Returns a collection of child nodes to the given parameter.
	 */
	protected Map<String, Node> nodeChildrenAlt(DefaultMutableTreeNode node) {
		Map<String, Node> result = new HashMap<>();
		Collection<TreeNode> children = Collections.list(node.children());

		for (TreeNode child : children) {
			DefaultMutableTreeNode curr2 = (DefaultMutableTreeNode) child;
			Node ncurr2 = (Node) curr2.getUserObject();
			result.put(ncurr2.getName(), ncurr2);
		}

		return result;
	}

	/**
	 * Returns a collection of orphans in the tree. Orphans are those child nodes
	 * of the root that are not of type cluster.
	 */
	protected Collection<Node> orphans() {
		Collection<Node> result = new ArrayList<>();
		for (Node curr : nodeChildren(root))
			if (!curr.isCluster()) result.add(curr);
		return result;
	}

	/**
	 * Returns a count of orphans in the tree. Orphans are those child nodes of
	 * the root that are not of type cluster.
	 */
	protected int orphanNumber() {
		int count = 0;
		for (Node curr : nodeChildren(root))
			if (!curr.isCluster()) count++;
		return count;
	}

	/**
	 * Returns all nodes in the tree rooted at the parameter, except for the root.
	 */
	public static Collection<Node> allNodes(DefaultMutableTreeNode root) {
		Collection<Node> result = new ArrayList<>();
		List<TreeNode> treeNodes = Collections.list(root.breadthFirstEnumeration());

		for (TreeNode node : treeNodes.subList(1, treeNodes.size())) {
			DefaultMutableTreeNode curr = (DefaultMutableTreeNode) node;
			Node ncurr = (Node) curr.getUserObject();
			result.add(ncurr);
		}

		return result;
	}

	public static void induceEdges(Collection<Node> v) {
		for (Node current : v) {
			inductionStep(current, false);
			inductionStep(current, true);
		}
	}

	/**
	 * @param current The node for which this inductionStep is being executed
	 * @param invert true if step should be done over sources, false over targets
	 */
	private static void inductionStep(Node current, boolean invert) {
		Collection<Node> set = invert
			? new HashSet<>(current.getSources())
			: new HashSet<>(current.getTargets());

		for (Node across : set) {
			// For each node from the root to across
			for (TreeNode k : across.getTreeNode().getPath()) {
				DefaultMutableTreeNode j = (DefaultMutableTreeNode) k;
				Node nj = (Node) j.getUserObject();

				if (!j.isRoot()) {
					if (invert)
						createEdge(nj, current);
					else
						createEdge(current, nj);
				}
			}
		}
	}

	private static void createEdge(Node n1, Node n2) {
		Edge e = new Edge(n1, n2, "induced");
		n1.addOutEdge(e);
		n2.addInEdge(e);
	}
	//endregion
}
