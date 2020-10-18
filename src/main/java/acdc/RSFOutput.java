package acdc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

/**
* This class has one method which creates an RSF file. The string representation
* of the output is of the format: contain parent_node node
*/
public class RSFOutput implements OutputHandler 
{
	public void writeOutput(String outputName, DefaultMutableTreeNode root) {
		(new File(outputName)).getParentFile().mkdirs();

		try (PrintWriter out = new PrintWriter(
				new BufferedWriter(new FileWriter(outputName)))) {
			Node ni;
			Node np;
			DefaultMutableTreeNode i;
			DefaultMutableTreeNode pi;

			Enumeration<TreeNode> allNodes = root.breadthFirstEnumeration();

			// Avoid output for the root node
			i = (DefaultMutableTreeNode) allNodes.nextElement();

			while (allNodes.hasMoreElements()) {
				i = (DefaultMutableTreeNode) allNodes.nextElement();
				ni = (Node) i.getUserObject();
				pi = (DefaultMutableTreeNode) i.getParent();
				np = (Node) pi.getUserObject();
				
				String cleanNpName = np.getName();
				if (np.getName().startsWith("\"") && !np.getName().endsWith("\""))
					cleanNpName = np.getName().substring(1, np.getName().length());

				if (pi != root)
					out.println("contain " + cleanNpName + " " + ni.getName());
			}
		} 
		catch (IOException e) { System.err.println(e.getMessage()); }
	}
}