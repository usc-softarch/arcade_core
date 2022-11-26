package edu.usc.softarch.arcade.clustering.acdc;

import edu.usc.softarch.arcade.clustering.acdc.data.Node;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

/**
* This class has one method which creates an RSF file. The string representation
* of the output is of the format: contain parent_node node
*/
public class RSFOutput {
	public static void writeOutput(
			String outputName, DefaultMutableTreeNode root) throws IOException {
		(new File(outputName)).getParentFile().mkdirs();

		try (FileWriter writer = new FileWriter(outputName)) {
			Node ni;
			Node np;
			DefaultMutableTreeNode i;
			DefaultMutableTreeNode pi;

			Enumeration<TreeNode> allNodes = root.breadthFirstEnumeration();

			allNodes.nextElement(); // Avoid output for the root node

			while (allNodes.hasMoreElements()) {
				i = (DefaultMutableTreeNode) allNodes.nextElement();
				ni = (Node) i.getUserObject();
				pi = (DefaultMutableTreeNode) i.getParent();
				np = (Node) pi.getUserObject();
				
				String cleanNpName = np.getName();
				if (cleanNpName.startsWith("\"") && !cleanNpName.endsWith("\""))
					cleanNpName = cleanNpName.substring(1);

				if (pi != root)
					writer.write("contain " + cleanNpName + " " + ni.getName()
						+ System.lineSeparator());
			}
		}
	}
}
