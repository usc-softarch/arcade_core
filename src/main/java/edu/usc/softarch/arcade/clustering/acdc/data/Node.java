package edu.usc.softarch.arcade.clustering.acdc.data;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.tree.DefaultMutableTreeNode;

/**
* This class encapsulates information about a Node node.
*/
public class Node implements Comparable<Node> {
	//region ATTRIBUTES
	private String name;
	private final String type;
	private final Set<Node> sources;
	private final Set<Node> targets;
	private final List<Edge> incomingEdges;
	private final List<Edge> outgoingEdges;
	private DefaultMutableTreeNode treeNode;
	//endregion

	//region CONSTRUCTORS
	/**
	 * Creates a node initialized with the passed parameters as its name and type
	 * and sets the remaining attributes to default values.
	 *
	 * @param name the name of this node
	 * @param type the type of this node
	 */
	public Node(String name, String type) {
		this.name = name;
		this.type = type;
		sources = new HashSet<>();
		targets = new HashSet<>();
		incomingEdges = new ArrayList<>();
		outgoingEdges = new ArrayList<>();
	}
	//endregion

	//region ACCESSORS
	/**
	 * Returns <code>this</code> node's name
	 *
	 * @return the name of this node
	 */
	public String getName() { return name; }

	/**
	 * Returns <code>this</code> node's base name, i.e. removes the last dot and
	 * anything after it
	 *
	 * @return the base name of this node
	 */
	public String getBaseName()
	{
		int pos = name.lastIndexOf('.');
		if (pos == -1)
			return name;
		else
			return name.substring(0, pos);
	}

	/**
	 * Sets the name of <code>this</code> node
	 *
	 * @param name the name of this node
	 */
	public void setName(String name) { this.name = name; }

	public boolean isNamedIgnoreCase(String name) {
		return getName().equalsIgnoreCase(name); }

	public boolean isNamedIgnoreCase(Node name) {
		return getName().equalsIgnoreCase(name.getName()); }

	/**
	 * Returns <code>this</code> node's type
	 * @return the type of this node
	 */
	public String getType() { return type; }

	public boolean isCluster() {
		return type.equalsIgnoreCase("cModule")
			|| type.equalsIgnoreCase("Subsystem");
	}

	/**
	 * Returns true if <code>this</code> node is of type cFile or Unknown
	 *
	 * @return true if this node is a file
	 */
	public boolean isFile() {
		return type.equals("Unknown"); }

	/**
	 * Returns a set of nodes which have edges directed towards <code>this</code>
	 * node
	 *
	 * @return a set of nodes representing the sources of the incoming edges to
	 *         this node
	 */
	public Set<Node> getSources() { return sources; }

	/**
	 * Adds an incoming edge to <code>this</code> node. If edge was already an
	 * incoming edge to this node, recalculates its weight. Also, the node from
	 * which this edge originates is added to the sources of this node.
	 *
	 * @param e The edge to be added to incomingEdges.
	 */
	public void addInEdge(Edge e)
	{
		Iterator<Edge> i = incomingEdges.iterator();
		boolean done = false;

		//test if edge e exists in incomingEdges
		//if yes, recalculate its weight, else add edge e to the incomingEdges
		//and add the node from which edge e originates to the sources of this node
		while (i.hasNext()) {
			Edge j = i.next();
			if ((j.getSourceName().equals(e.getSourceName()))
					&& (j.getType().equals(e.getType()))) {
				done = true;
				break;
			}
		}

		if (!done) incomingEdges.add(e);
		sources.add(e.getSource());
	}

	/**
	 * Returns a set of nodes towards which the edges of <code>this</code> node
	 * are directed
	 *
	 * @return a set of nodes representing the targets of the outgoing edges of
	 *         <code>this</code> node
	 */
	public Set<Node> getTargets() { return targets; }

	/**
	 * Adds an outgoing edge to <code>this</code> node. If edge was already an
	 * outgoing edge of this node, recalculates its weight. Also, the node towards
	 * which this edge is directed is added to the targets of this node.
	 *
	 * @param e The edge to be added to outgoingEdges.
	 */
	public void addOutEdge(Edge e) {
		Iterator<Edge> i = outgoingEdges.iterator();
		boolean done = false;

		//test if edge e exists in outgoingEdges
		//if yes, recalculate its weight, else add edge e to the outgoingEdges
		//and add the node towards which edge e is directed to the targets of this node
		while (i.hasNext()) {
			Edge j = i.next();
			if ((j.getTargetName().equals(e.getTargetName()))
					&& (j.getType().equals(e.getType()))) {
				done = true;
				break;
			}
		}
		if (!done) outgoingEdges.add(e);
		targets.add(e.getTarget());
	}

	/**
	 * Returns the tree node to which this node is referring
	 *
	 * @return the tree node
	 */
	public DefaultMutableTreeNode getTreeNode() { return treeNode; }

	public void setTreeNode(DefaultMutableTreeNode treeNode) {
		this.treeNode = treeNode; }
	//endregion

	//region OBJECT METHODS
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Node))
			return false;

		Node e = (Node) o;
		return (this.getName()).equals(e.getName());
	}

	/**
	 * Returns a string representation of <code>this</code> node
	 *
	 * @return name followed by the type of this node
	 */
	public String toString() { return(name + " " + type); }

	@Override
	public int compareTo(Node o) {
		return this.getName().compareTo(o.getName()); }
	//endregion
}
