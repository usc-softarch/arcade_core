package edu.usc.softarch.arcade.clustering.acdc.patterns;

import edu.usc.softarch.arcade.clustering.acdc.data.Node;
import edu.usc.softarch.arcade.clustering.acdc.data.Sortable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

/**
 * @author V. Tzerpos
 *
 * Clusters the children of the root based on their dependencies
 */
public class SubGraph extends Pattern {
	private final int clusterSize;

	public SubGraph(DefaultMutableTreeNode root, int size) {
		super(root);
		clusterSize = size;
	}

	public void execute() {
		Collection<Node> vModified = new ArrayList<>();
		Map<String, Node> vRootChildren = nodeChildrenAlt(root);

		//************************************************************************
		// Note: The nodes to be clustered are the children of the root only. 
		//       Their corresponding subtree should not be modified!             
		//************************************************************************

		Set<DefaultMutableTreeNode> setOfTargets;

		// put nodes to be clustered together with their corresponding number of
		// targets in a Map
		Map<Node,Integer> ht = new HashMap<>();

		int counter; // keeps track of #targets per node

		for (Node ncurr : vRootChildren.values()) {
			setOfTargets = new HashSet<>(); //reset

			setOfTargets.add(ncurr.getTreeNode());
			setOfTargets = findTargets(setOfTargets, vRootChildren);

			counter = 0; //reset

			//count only targets which are children of root
			for (DefaultMutableTreeNode c : setOfTargets) {
				Node n = (Node) c.getUserObject();

				if (vRootChildren.containsKey(n.getName())) counter++;
			}
			ht.put(ncurr, counter);
		}

		//sort ht in increasing order
		List<Sortable> myArray = new ArrayList<>();
		for (Map.Entry<Node, Integer> entry : ht.entrySet()) {
			// build a sortable object with the two pieces of information
			Sortable sBefore = new Sortable(entry.getValue(), entry.getKey());

			// add the sortable object to the sarray
			myArray.add(sBefore);
		}
		myArray.sort(null);

		Node tentativeDominator;
		int nextIndex = 0;
		boolean found;

		// This is where the big loop begins
		while (!ht.isEmpty()) {
			// some nodes (identified below as covered set nodes) might be removed from
			// the hashtable so we want to make sure that our ArrayList is also updated
			do {
				Sortable sAfter = myArray.get(nextIndex++);
				tentativeDominator = sAfter.getObject();
				// while hashtable doesn't contain it, skip over current node
				// to the next one
				found = ht.containsKey(tentativeDominator);
			} while (!found);

			DefaultMutableTreeNode tentativeDominatorTreeNode =
				tentativeDominator.getTreeNode();

			Set<DefaultMutableTreeNode> cS = 
				coveredSet(tentativeDominatorTreeNode, vRootChildren);
			
			if (cS.size() == 1)	ht.remove(tentativeDominator);
			else { //coveredSet returned tentativeRoot and its set of dominated nodes
				// max cluster size allowed is clusterSize!!
				Node ncurrCS;

				if (cS.size() < clusterSize) {
					for (DefaultMutableTreeNode curr_cS : cS) {
						ncurrCS = (Node) curr_cS.getUserObject();
						ht.remove(ncurrCS);
					}
				}
				else //if cS has > clusterSize elements, attempt deeper clustering
					ht.remove(tentativeDominator);

				// Create a new subsystem node
				Node ssNode = new Node(
					tentativeDominator.getBaseName() + ".ss", "Subsystem");
				if (!vModified.contains(ssNode)) vModified.add(ssNode);

				DefaultMutableTreeNode ssTreeNode = new DefaultMutableTreeNode(ssNode);
				ssNode.setTreeNode(ssTreeNode);

				// Find the most immediate common ancestor of all
				// elements in the covered set
				Iterator<DefaultMutableTreeNode> icS = cS.iterator();
				DefaultMutableTreeNode ancestor = icS.next();
				ancestor = (DefaultMutableTreeNode) ancestor.getSharedAncestor(
					icS.next());
				while (icS.hasNext())
					ancestor = (DefaultMutableTreeNode) ancestor.getSharedAncestor(
						icS.next());

				// Insert the new node just below the ancestor
				Iterator<DefaultMutableTreeNode> ics2 = cS.iterator();
				Set<DefaultMutableTreeNode> nodesToMove = new HashSet<>();

				int numOfElementsInPath;
				boolean continueLoop = true;
				while (ics2.hasNext() && continueLoop) {
					DefaultMutableTreeNode nnn = ics2.next();
					Enumeration<TreeNode> path = 
						nnn.pathFromAncestorEnumeration(ancestor);
					numOfElementsInPath = 0;
					while (path.hasMoreElements()) {
						path.nextElement();
						numOfElementsInPath++;
					}

					//if ancestor belongs to covered set, only ancestor should be moved
					if (numOfElementsInPath == 1) {
						nodesToMove.clear();
						nodesToMove.add(ancestor);
						continueLoop = false;
					} else {
						path = nnn.pathFromAncestorEnumeration(ancestor);

						//firstNode is same as ancestor!
						path.nextElement();

						DefaultMutableTreeNode secondNode =
							(DefaultMutableTreeNode) path.nextElement();
						nodesToMove.add(secondNode);
					}
				}

				//new node contains all the nodes in covered set and others too
				for (DefaultMutableTreeNode nextToMove : nodesToMove) {
					if (!nextToMove.equals(ssTreeNode))
						ssTreeNode.add(nextToMove);

					// there are no outside sources for coveredSet nodes, therefore
					// edgeInduction is only needed for the newly created cluster node 
					Enumeration<TreeNode> ecurr = 
						nextToMove.breadthFirstEnumeration();
					while (ecurr.hasMoreElements()) {
						DefaultMutableTreeNode em = 
							(DefaultMutableTreeNode) ecurr.nextElement();
						if (!vModified.contains(em.getUserObject()))
							vModified.add((Node) em.getUserObject());
					}
				}

				if (ancestor == root)	ancestor.add(ssTreeNode);
				else {
					//only child is the ancestor
					if (!ssTreeNode.equals(ancestor)) ancestor.add(ssTreeNode);
				}
			}
		}

		induceEdges(vModified);
	}

	/**
	* Returns the HashSet containing the passed arg, called "dominator node", and 
	* the set of its dominated nodes, N = n(i), i:1,2,...m,  which have 
	* the following properties:
	*
	* 1. there exists a path from tentativeRoot to every n(i)
	* 2. for any node v such that there exists a path from v to any n(i), either 
	*    tentativeRoot is in that path or v is one of n(i)
	*/
	private static Set<DefaultMutableTreeNode> coveredSet(
			DefaultMutableTreeNode tentativeRoot,
			Map<String, Node> vTree) {
		Set<DefaultMutableTreeNode> result = new HashSet<>();
		result.add(tentativeRoot);

		Set<DefaultMutableTreeNode> covered;
		Set<DefaultMutableTreeNode> falseOnes;
		Set<DefaultMutableTreeNode> fathers;
		Set<DefaultMutableTreeNode> both;

		do {
			covered = findTargets(result, vTree);
			covered.removeAll(result);
			falseOnes = new HashSet<>();
			do {
				both = new HashSet<>(covered);
				both.addAll(result);
				for (DefaultMutableTreeNode curr : covered) {
					fathers = findSources(curr, vTree);
					if (!both.containsAll(fathers)) falseOnes.add(curr);
				}
			} while (covered.removeAll(falseOnes));
			// will exit if covered doesn't change
		} while (result.addAll(covered)); // will exit if result doesn't change
		return result;
	}

	/**
	* Returns a HashSet containing the target nodes of all the
	* items in the passed HashSet parameter.
	*/
	private static Set<DefaultMutableTreeNode> findTargets(
			Set<DefaultMutableTreeNode> source, Map<String, Node> vRootChildren) {
		Set<DefaultMutableTreeNode> allTargets = new HashSet<>();

		//iterate thorough the passed HashSet
		for (DefaultMutableTreeNode curr : source) {
			Node ncurr = (Node) curr.getUserObject();

			//iterate through these targets adding each to the HashSet 'targets'
			for (Node n : ncurr.getTargets()) {
				if (vRootChildren.containsKey(n.getName())) {
					DefaultMutableTreeNode t = n.getTreeNode();
					allTargets.add(t);
				}
			}
		}
		return allTargets;
	}

	/**
	* Returns a Set containing the sources of the passed node parameter.
	*/
	private static Set<DefaultMutableTreeNode> findSources (
			DefaultMutableTreeNode target,
			Map<String, Node> vRootChildren) {
		Set<DefaultMutableTreeNode> allSources = new HashSet<>();

		// get sources of the passed node
		Node ntarget = (Node) target.getUserObject();

		// iterate through the sources of the passed node
		// and add them to the HashSet called 'sources'
		for (Node n : ntarget.getSources()) {
			if (vRootChildren.containsKey(n.getName())) {
				DefaultMutableTreeNode t = n.getTreeNode();
				allSources.add(t);
			}
		}
		return allSources;
	}
}
