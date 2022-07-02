package edu.usc.softarch.util;

import org.jgrapht.graph.DefaultEdge;

public class LabeledEdge extends DefaultEdge {
	public final String label;

	public LabeledEdge(String label) { this.label = label; }
}
