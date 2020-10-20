package edu.usc.softarch.arcade.clustering.acdc;

import javax.swing.tree.DefaultMutableTreeNode;

public interface OutputHandler {
	public void writeOutput(String outputName, DefaultMutableTreeNode root);
}