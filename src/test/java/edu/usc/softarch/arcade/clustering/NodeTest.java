package edu.usc.softarch.arcade.clustering;

import java.util.ArrayList;

import edu.usc.softarch.arcade.classgraphs.SootClassEdge;
import edu.usc.softarch.arcade.clustering.Feature;
import edu.usc.softarch.arcade.clustering.Node;


import junit.framework.TestCase;

/**
 * @author joshua
 *
 */
public class NodeTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void voidTestNode() {
		Node<ArrayList<Feature>> left = new Node<ArrayList<Feature>>(null,null,null);
		Node<ArrayList<Feature>> right = new Node<ArrayList<Feature>>(null,null,null);
		Node<ArrayList<Feature>> n = new Node<ArrayList<Feature>>(null,null,null);
		
		SootClassEdge ce1 = new SootClassEdge("A","B");
		SootClassEdge ce2 = new SootClassEdge("B","C");
		SootClassEdge ce3 = new SootClassEdge("C","A");
		
		
		Feature f1 = new Feature(ce1,1);
		Feature f2 = new Feature(ce2,1);
		Feature f3 = new Feature(ce3,1);
		
		left.parent = n;
		right.parent = n;
	}

}
