package acdc;

import java.util.Collections;
import java.util.ArrayList;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

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
 * algorithm deterministic
 */
public class OrphanAdoption extends Pattern {
	public OrphanAdoption(DefaultMutableTreeNode _root)	{
		super(_root);
		name = "Orphan Adoption";
	}

	public void execute()	{
		List<Node> vModified = new ArrayList<>();
		List<Node> vAdopted;
		// Run oneRoundForward as many times as the #orphans is decreasing
		manyRoundsForward();

		int on = orphanNumber();
		int prevon = on + 1;
		while(on < prevon) {
			IO.put("Orphan Number: " + on,2);
			prevon = on;
			vAdopted = oneRoundReverse();
			vModified.addAll(vAdopted);
			if (orphanNumber() > 0)	{
				vAdopted = manyRoundsForward();
				vModified.addAll(vAdopted);
			}
			on = orphanNumber();
		}
		induceEdges(vModified);
	}

	public List<Node> oneRoundReverse()	{
		List<Node> vReturn = new ArrayList<>();
		List<Node> vRootChildren = nodeChildren(root);

		// Keeps the cluster-nodes which are competing for this orphan
		Map<Node, Double> ht;

		for (int j = 0; j < vRootChildren.size(); j++) {
			Node ncurr = vRootChildren.get(j);
			DefaultMutableTreeNode curr = ncurr.getTreeNode();

			if(!ncurr.isCluster()) {
				ht = new LinkedHashMap<>(10000);
				IO.put("ROA:\torphan =: "+ ncurr.getName(),2);

				// a set of the nodes to which the current orphan points to
				SortedSet<Node> targets = new TreeSet<>();
				targets.addAll(ncurr.getTargets());

				Iterator<Node> itargets = targets.iterator();
				while (itargets.hasNext()) {
					Node ncurr_target = itargets.next();
					DefaultMutableTreeNode curr_target = (ncurr_target.getTreeNode());
					double counter = 0;

					/**********************************************************************
						NOTE: retain only targets of the orphan which are clusters <-- only 
						the cluster	which is lowest in the tree should adopt the orphan
					***********************************************************************/

					// ignore root and its children as targets of the orphan
					// also ignore targets of the orphan which are clusters
					if(curr_target.getLevel()>1 && !ncurr_target.isCluster())	{
						// the parent of this target is competing for the orphan
						DefaultMutableTreeNode parent = (DefaultMutableTreeNode) curr_target.getParent();
						Node nparent = (Node) parent.getUserObject();
						// if parent is orphan or root, do nothing
						if (!(nparent.getName().equalsIgnoreCase(ncurr.getName())
								|| nparent.getName().equalsIgnoreCase("ROOT"))) {
							boolean stop = false;
							while(!stop && !nparent.isCluster()) {
								parent = (DefaultMutableTreeNode) parent.getParent();
								nparent = (Node) parent.getUserObject();

								// parent of the source is root or is the orphan
								if(nparent.getName().equalsIgnoreCase(ncurr.getName())
										|| nparent.getName().equalsIgnoreCase("ROOT")) {
									stop = true;
								}
							}
						}

						if(nparent.isCluster()) {
							// Case1: parent already in the hashtable
							if(ht.containsKey(nparent)) {
								// counter value get incremented by one unit
								Double i = ht.get(nparent);
								counter = i.doubleValue();
								counter++;
								// remove source with old counter value from the hashtable
								ht.remove(nparent);
								// add the source to the hashtable with the updated counter value
								ht.put(nparent, Double.valueOf(counter));
							} else { // Case2: parent was not in the hashtable
								counter = 0;
								SortedSet<Node> c_targets = new TreeSet<>();

								//Enumeration is replaced with an iterator on a sorted list
								List<TreeNode> tempALBase =
									Collections.list(parent.breadthFirstEnumeration());
								List<DefaultMutableTreeNode> tempAL = new ArrayList<>();
								for(TreeNode node : tempALBase)
									tempAL.add((DefaultMutableTreeNode)node);
								Collections.sort(tempAL,
									(DefaultMutableTreeNode o1, DefaultMutableTreeNode o2) -> {
										Node n1 = (Node)o1.getUserObject();
										Node n2 = (Node)o2.getUserObject();
										return n1.getName().compareTo(n2.getName());
									}
								);
								Iterator<DefaultMutableTreeNode> ps = tempAL.iterator();

								while (ps.hasNext()) {
									DefaultMutableTreeNode ps_curr = ps.next();
									Node nps_curr = (Node) ps_curr.getUserObject();
									c_targets.clear();
									c_targets.addAll(nps_curr.getTargets());
									if (c_targets.contains(ncurr)) counter = counter + 0.000001;
								}

								//add parent to the hashtable with a counter value incremented
								// by one more unit
								ht.put(nparent, Double.valueOf(++counter));
							}
						}
					}
				}

				if(ht.isEmpty()) IO.put("\tNOT\t adopted", 2);
				//the hashtable now contains all the candidate nodes for adopting the orphan
				//the node in the hashtable with the highest counter value will get the orphan
				if(!ht.isEmpty()) {
					double max_value = 0;
					Node max_key = new Node("faraz","oana");

					Iterator<Node> keys = ht.keySet().iterator();
					while (keys.hasNext()) {
						Node curr_key = keys.next();

						Double curr_value = ht.get(curr_key);

						if(curr_value.doubleValue() >= max_value)	{
							max_value = curr_value.doubleValue() ;
							max_key = curr_key;
						}
					}

					DefaultMutableTreeNode max = max_key.getTreeNode();
					max.add(curr);

					List<TreeNode> tempALBase =
						Collections.list(curr.breadthFirstEnumeration());
					List<DefaultMutableTreeNode> tempAL = new ArrayList<>();
					for (TreeNode node : tempALBase)
						tempAL.add((DefaultMutableTreeNode)node);
					Collections.sort(tempAL,
						(DefaultMutableTreeNode o1, DefaultMutableTreeNode o2) -> {
							Node n1 = (Node) o1.getUserObject();
							Node n2 = (Node) o2.getUserObject();
							return n1.getName().compareTo(n2.getName());
						}
					);

					Iterator<DefaultMutableTreeNode> emax = tempAL.iterator();

					while (emax.hasNext()){
						DefaultMutableTreeNode ec = emax.next();
						if(!vReturn.contains(ec.getUserObject()))
							vReturn.add((Node)ec.getUserObject());
					}
					IO.put("\twas adopted by ***\t" + max_key.getName(),2);
				}
			}
		}
		return vReturn;
	}

	public List<Node> manyRoundsForward() {
		List<Node> result = new ArrayList<>();
		List<Node> vAdopted;
		// Run oneRoundForward as many times as the #orphans is decreasing
		do {
			vAdopted = oneRoundForward();
			IO.put("Before " + vAdopted.size(),2);
			result.addAll(vAdopted);
			IO.put("After " + vAdopted.size()+"",2);
		}	while(!vAdopted.isEmpty());
		return result;
	}

	public List<Node> oneRoundForward() {
		List<Node> vReturn = new ArrayList<>();
		//vector will contain the orphans adopted
		List<Node> vRootChildren = nodeChildren(root);

		// Map keeps track of the nodes competing for the current orphan
		Map<Node, Double> ht;
		for (int j = 0; j < vRootChildren.size(); j++) {
			Node ncurr = vRootChildren.get(j);
			DefaultMutableTreeNode curr = ncurr.getTreeNode();

			if (!ncurr.isCluster())	{
				//begin with an empty hashtable for each orphan
				ht = new LinkedHashMap<>(10000);
				IO.put("OA:\torphan  =: " + ncurr.getName(), 2);

				//sources = set of nodes which point to the current orphan
				SortedSet<Node> sources = new TreeSet<>();
				sources.addAll(ncurr.getSources());
				Iterator<Node> isources = sources.iterator();

				//iterate through the sources
				while (isources.hasNext()) {
					Node ncurr_source = isources.next();
					DefaultMutableTreeNode curr_source = (ncurr_source.getTreeNode());
					double counter = 0;

					/********************************************************************
						NOTE: retain only sources of the orphan which are clusters <-- only
						 the cluster which is lowest in the tree should adopt the orphan,
					 ********************************************************************/
					// ignore root and its children  as sources of the orphan
					// --> we ignore sources of the orphan which are clusters
					if (curr_source.getLevel() > 1 && !ncurr_source.isCluster()) {
						//the parent of this source is competing for the orphan
						DefaultMutableTreeNode parent =
							(DefaultMutableTreeNode) curr_source.getParent();
						Node nparent = (Node) parent.getUserObject();

						//if parent is orphan or root, do nothing
						if (!(nparent.getName().equalsIgnoreCase(ncurr.getName())
								|| nparent.getName().equalsIgnoreCase("ROOT"))) {
							boolean stop = false;
							while (!stop && !nparent.isCluster()) {
								parent = (DefaultMutableTreeNode) parent.getParent();
								nparent = (Node) parent.getUserObject();
								//parent of the source is root or is the orphan
								if (nparent.getName().equalsIgnoreCase(ncurr.getName())
										|| nparent.getName().equalsIgnoreCase("ROOT")) {
									stop = true;
								}
							}
						}

						if (nparent.isCluster()) {
							//Case1: parent already in the hashtable
							if (ht.containsKey(nparent)) {
								//counter value get incremented by one unit
								Double i = ht.get(nparent);
								counter = i.doubleValue();
								counter++;
								//remove source with old counter value from the map
								ht.remove(nparent);
								//add the source to the map with the updated counter value
								ht.put(nparent, Double.valueOf(counter));
							}
							//Case2: parent not in the hashtable
							else {
								counter = 0;
								SortedSet<Node> c_sources = new TreeSet<>() ;

								List<TreeNode> tempALBase = Collections.list(parent.breadthFirstEnumeration());
								List<DefaultMutableTreeNode> tempAL = new ArrayList<>();
								for(TreeNode node : tempALBase)
									tempAL.add((DefaultMutableTreeNode)node);
								Collections.sort(tempAL,
									(DefaultMutableTreeNode o1, DefaultMutableTreeNode o2) -> {
										Node n1 = (Node)o1.getUserObject();
										Node n2 = (Node)o2.getUserObject();
										return n1.getName().compareTo(n2.getName());
									}
								);
								Iterator<DefaultMutableTreeNode> ps = tempAL.iterator();

								ps.next();
								// don't count the parent itself which surely points to
								// curr due to edge induction
								while (ps.hasNext()) {
									DefaultMutableTreeNode ps_curr = ps.next();
									Node nps_curr =	(Node) ps_curr.getUserObject();
									c_sources.clear();
									c_sources.addAll(nps_curr.getSources());
									if (c_sources.contains(ncurr)) counter = counter + 0.000001;
								}

								// add parent to the hashtable with a counter value
								// incremented by one more unit
								ht.put(nparent, Double.valueOf(++counter));
							}
						}
					}
				}

				if (ht.isEmpty()) IO.put("\tNOT\t adopted", 2);

				// the map now contains all the candidate nodes for adopting the orphan
				// the node in the hashtable with the highest value will get the orphan
				if (!ht.isEmpty()) {
					double max_value = 0;
					Node max_key = new Node("faraz", "oana");

					Iterator<Node> keys = ht.keySet().iterator();
					while (keys.hasNext()) {
						Node curr_key = keys.next();
						Double curr_value = ht.get(curr_key);

						if (curr_value.doubleValue() >= max_value) {
							max_value = curr_value.doubleValue();
							max_key = curr_key;
						}
					}

					DefaultMutableTreeNode max = (max_key.getTreeNode());
					max.add(curr);

					ArrayList<TreeNode> tempALBase = Collections.list(curr.breadthFirstEnumeration());
					ArrayList<DefaultMutableTreeNode> tempAL = new ArrayList<>();
					for(TreeNode node : tempALBase)
						tempAL.add((DefaultMutableTreeNode)node);
					Collections.sort(tempAL,
						(DefaultMutableTreeNode o1, DefaultMutableTreeNode o2) -> {
							Node n1 = (Node)o1.getUserObject();
							Node n2 = (Node)o2.getUserObject();
							return n1.getName().compareTo(n2.getName());
						}
					);
					Iterator<DefaultMutableTreeNode> emax = tempAL.iterator();

					while (emax.hasNext()) {
						DefaultMutableTreeNode ec = emax.next();
						if (!vReturn.contains(ec.getUserObject()))
							vReturn.add((Node) ec.getUserObject());
					}

					IO.put("\twas adopted by ***\t" + max_key.getName(), 2);
				}
			}
		}
		return vReturn;
	}
}