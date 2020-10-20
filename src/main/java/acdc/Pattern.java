package acdc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class Pattern {
	// #region ATTRIBUTES --------------------------------------------------------
	private static final Logger logger = LogManager.getLogger(Pattern.class);

	protected DefaultMutableTreeNode root;
	protected String name;
	// #endregion ATTRIBUTES -----------------------------------------------------

	// #region CONSTRUCTORS ------------------------------------------------------
	public Pattern(DefaultMutableTreeNode root) {
		this.root = root;
		name = "";
	}
	// #endregion CONSTRUCTORS ---------------------------------------------------
	
	// #region ACCESSORS ---------------------------------------------------------
	public String getName() { return name; }
	// #endregion ACCESSORS ------------------------------------------------------

	protected abstract void execute();

	/**
	 * Returns a list of child nodes to the given parameter.
	 */
	protected List<Node> nodeChildren(DefaultMutableTreeNode node) {
		List<Node> result = new ArrayList<>();
		List<TreeNode> children = Collections.list(node.children());

		for (TreeNode child : children) {
			DefaultMutableTreeNode curr2 = (DefaultMutableTreeNode) child;
			Node ncurr2 = (Node) curr2.getUserObject();
			result.add(ncurr2);
		}

		return result;
	}
	
	/**
	 * Returns a list of orphans in the tree. Orphans are those child nodes of
	 * the root that are not of type cluster.
	 */
	protected List<Node> orphans() {
		List<Node> result = new ArrayList<>();
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
	public static List<Node> allNodes(DefaultMutableTreeNode root) {
		List<Node> result = new ArrayList<>();
		List<TreeNode> treeNodes = Collections.list(root.breadthFirstEnumeration());

		for (TreeNode node : treeNodes.subList(1, treeNodes.size())) {
			DefaultMutableTreeNode curr = (DefaultMutableTreeNode) node;
			Node ncurr = (Node) curr.getUserObject();
			result.add(ncurr);
		}

		return result;
	}
	
	public static void induceEdges(List<Node> v) {
		logger.info("The following " + v.size()
			+ " nodes were selected for edge induction");
		
		for (Node current : v) {
			logger.info(current.getName());

			inductionStep(current, false);
			inductionStep(current, true);
		}
	}

	/**
	 * @param current The node for which this inductionStep is being executed
	 * @param invert true if step should be done over sources, false over targets
	 */
	private static void inductionStep(Node current, boolean invert) {
		Set<Node> set = invert 
									? new HashSet<>(current.getSources())
									: new HashSet<>(current.getTargets());
		// For each node in set of sources/targets
		for (Node across : set) {
			// For each note from the root to across
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
		logger.info("\tInduced edge from " + n1.getName() + " to " + n2.getName());
		n1.addOutEdge(e);
		n2.addInEdge(e);
	}
}