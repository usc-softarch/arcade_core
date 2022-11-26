package edu.usc.softarch.arcade.clustering.acdc;

import edu.usc.softarch.arcade.clustering.acdc.data.Edge;
import edu.usc.softarch.arcade.clustering.acdc.data.Node;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

/**
* This class creates a tree from the info passed by the input file.
*/
public class TAInput {
	public static void readInput(
			String inputStr, DefaultMutableTreeNode treeModel) throws IOException {
		String str;
		String relationToken;
		String sourceToken;
		String targetToken;

		try (BufferedReader in = new BufferedReader(new FileReader(inputStr))) {
			while ((str = in.readLine()) != null) {
				str = str.trim();

				String[] tokens = str.split(" ");

				if (tokens.length != 3)
					throw new IllegalArgumentException("Syntax error: Tuple "
						+ str + " contains the wrong number of tokens.");

				relationToken  = tokens[0];
				sourceToken = tokens[1];
				targetToken  = tokens[2];

				DefaultMutableTreeNode root =
					(DefaultMutableTreeNode) treeModel.getRoot();
				Enumeration<TreeNode> allNodes = root.depthFirstEnumeration();

				DefaultMutableTreeNode tn1 = null;
				DefaultMutableTreeNode tn2 = null;
				Node n1 = null;
				Node n2 = null;

				//search all tree for nodes with names equal to source or target token
				while (allNodes.hasMoreElements()) {
					Object i = allNodes.nextElement();
					DefaultMutableTreeNode j = (DefaultMutableTreeNode) i;
					Node n = (Node)j.getUserObject();
					//node with name sourceToken was found in the tree
					if (n.getName().equals(sourceToken)) {
						tn1 = j;
						n1 = n;
					}
					//node with name targetToken was found in the tree
					if (n.getName().equals(targetToken)) {
						tn2 = j;
						n2 = n;
					}
				}

				// Source and target are the same, but haven't been added yet.
				if (sourceToken.equals(targetToken) && tn1 == null) {
					//create only one node and add it under root
					n1 = new Node(sourceToken, "Unknown");
					tn1 = new DefaultMutableTreeNode(n1);
					n1.setTreeNode(tn1);
					//add only one of the two tokens as nodes under the root
					root.add(tn1);
				} else if (!sourceToken.equals(targetToken)) {
					if (tn1 == null) {
						//create new node and add it under root
						n1 = new Node(sourceToken, "Unknown");
						tn1 = new DefaultMutableTreeNode(n1);
						n1.setTreeNode(tn1);
						root.add(tn1); //add it under root
					}
					if (tn2 == null) {
						//create new node and add it under root
						n2 = new Node(targetToken, "Unknown");
						tn2 = new DefaultMutableTreeNode(n2);
						n2.setTreeNode(tn2);
						root.add(tn2); //add it under root
					}

					Edge e = new Edge(n1, n2, relationToken);
					n1.addOutEdge(e);
					n2.addInEdge(e);
				}
			}
		}
	}
}
