package acdc;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.ArrayList;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This pattern finds and moves children of the root node which are not
 * clusters under the appropriate adoptive cluster-node.
 * An orphan would be placed under the cluster-node with
 * the largest number of children which point to the orphan.
 *
 * Tie-breakers are solved by checking the targets of the orphan.
 *
 * If cluster-nodes A and B are competing for the orphan, that
 * which has the largest number of children being targets of the
 * orphan, wins it.
 *
 * The clustring method has been modified in order to make the
 * algorithm deterministic.
 */
public class OrphanAdoption extends Pattern {
	private static final Logger logger = 
		LogManager.getLogger(OrphanAdoption.class);

	private Comparator<DefaultMutableTreeNode> nodeComparer = 
		(DefaultMutableTreeNode o1, DefaultMutableTreeNode o2) -> {
			Node n1 = (Node)o1.getUserObject();
			Node n2 = (Node)o2.getUserObject();
			return n1.getName().compareTo(n2.getName());
	};

	public OrphanAdoption(DefaultMutableTreeNode root)	{
		super(root);
		name = "Orphan Adoption";
	}

	public void execute()	{
		List<Node> vModified = new ArrayList<>();
		List<Node> vAdopted;
		manyRoundsForward();

		int on = orphanNumber();
		int prevon = on + 1;
		while(on < prevon) {
			logger.info("Orphan Number: " + on);
			prevon = on;
			vAdopted = oneRound(true);
			vModified.addAll(vAdopted);
			if (orphanNumber() > 0)	{
				vAdopted = manyRoundsForward();
				vModified.addAll(vAdopted);
			}
			on = orphanNumber();
		}
		induceEdges(vModified);
	}

	public List<Node> manyRoundsForward() {
		List<Node> result = new ArrayList<>();
		List<Node> vAdopted;
		// Run oneRoundForward as many times as the #orphans is decreasing
		do {
			vAdopted = oneRound(false);
			logger.info("Before " + vAdopted.size());
			result.addAll(vAdopted);
			logger.info("After " + vAdopted.size() + "");
		}	while (!vAdopted.isEmpty());
		return result;
	}

	/**
	 * Runs a round of orphan adoption. A Forward round runs over the Sources of
	 * the orphan, whereas a Reverse round runs over its Targets.
	 * 
	 * @param reverse Whether to run in reverse or not.
	 */
	public List<Node> oneRound(boolean reverse) {
		Set<Node> vReturn = new HashSet<>();

		for (Node orphan : nodeChildren(root)) {
			if(orphan.isCluster()) continue; // If child is a cluster, skip

			logger.info("ROA:\torphan =: " + orphan.getName());
			DefaultMutableTreeNode tOrphan = orphan.getTreeNode();
			Map<Node, Double> prospectiveParents = prospectParents(orphan, reverse);

			if(prospectiveParents.isEmpty()) logger.info("\tNOT\t adopted");
			else vReturn.addAll(chooseParent(prospectiveParents, tOrphan));
		}
		return new ArrayList<>(vReturn);
	}

	private Map<Node, Double> prospectParents(Node orphan, boolean reverse) {
		Map<Node, Double> prospectiveParents = new LinkedHashMap<>();
		SortedSet<Node> orphanAcrossNodes = reverse
																			? new TreeSet<>(orphan.getTargets())
																			: new TreeSet<>(orphan.getSources());

		for (Node orphanAcross : orphanAcrossNodes) {
			DefaultMutableTreeNode tOrphanAcross = orphanAcross.getTreeNode();
			double counter = 0;

			/********************************************************************
				NOTE: retain only across from the orphan which are clusters <-- only
					the cluster which is lowest in the tree should adopt the orphan,
				********************************************************************/
			// ignore root and its children as across from the orphan
			// --> we ignore across from the orphan which are clusters
			if (tOrphanAcross.getLevel() > 1 && !orphanAcross.isCluster()) {
				DefaultMutableTreeNode parent =
					(DefaultMutableTreeNode) tOrphanAcross.getParent();
				Node nparent = (Node) parent.getUserObject();

				// While parent is not orphan or root, and parent is not cluster
				while (!(nparent.isNamedIgnoreCase(orphan)
							|| nparent.isNamedIgnoreCase("ROOT"))
							&& !nparent.isCluster()) {
					parent = (DefaultMutableTreeNode) parent.getParent();
					nparent = (Node) parent.getUserObject();
				}

				if (!nparent.isCluster()) continue; // No ancestor is cluster, skip

				// If parent is already in map, add 1 to its counter
				if (prospectiveParents.containsKey(nparent)) {
					counter = prospectiveParents.get(nparent) + 1;
					// Original value MUST be removed to re-order LinkedHashMap
					prospectiveParents.remove(nparent);
					prospectiveParents.put(nparent, counter);
				}	else {
					counter = 0;
					SortedSet<Node> c_across = new TreeSet<>();

					List<TreeNode> tempALBase =
						Collections.list(parent.breadthFirstEnumeration());
					List<DefaultMutableTreeNode> tempAL = new ArrayList<>();
					for(TreeNode node : tempALBase)
						tempAL.add((DefaultMutableTreeNode)node);
					Collections.sort(tempAL, nodeComparer);

					List<DefaultMutableTreeNode> tempALit = tempAL;
					// If using targets, ignore parent itself
					// as it already points to orphan by induction
					if (!reverse) tempALit = tempAL.subList(1, tempAL.size());

					for (DefaultMutableTreeNode ps_curr : tempALit) {
						Node nps_curr =	(Node) ps_curr.getUserObject();
						c_across.clear();

						if (reverse) c_across.addAll(nps_curr.getTargets());
						else c_across.addAll(nps_curr.getSources());

						if (c_across.contains(orphan)) counter = counter + 0.000001;
					}
					prospectiveParents.put(nparent, ++counter);
				}
			}
		}

		return prospectiveParents;
	}

	/**
	 * Chooses the prospective parent with the highest value to adopt the orphan.
	 */
	private Set<Node> chooseParent(
			Map<Node, Double> prospectiveParents,
			DefaultMutableTreeNode tOrphan) {
		Set<Node> vReturn = new HashSet<>();
		double max_value = 0;
		Node max_key = new Node("faraz", "oana");

		for (Node curr_key : prospectiveParents.keySet()) {
			Double curr_value = prospectiveParents.get(curr_key);

			if (curr_value >= max_value) {
				max_value = curr_value;
				max_key = curr_key;
			}
		}

		DefaultMutableTreeNode max = (max_key.getTreeNode());
		max.add(tOrphan);

		List<TreeNode> tempALBase =
			Collections.list(tOrphan.breadthFirstEnumeration());
		List<DefaultMutableTreeNode> tempAL = new ArrayList<>();
		for(TreeNode node : tempALBase)
			tempAL.add((DefaultMutableTreeNode)node);
		Collections.sort(tempAL, nodeComparer);
		
		for (DefaultMutableTreeNode ec : tempAL)
			vReturn.add((Node) ec.getUserObject());

		logger.info("\twas adopted by ***\t" + max_key.getName());
		return vReturn;
	}
}