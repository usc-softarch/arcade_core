package edu.usc.softarch.arcade.clustering;

import java.util.HashSet;

/**
 * @author joshua
 *
 * @param <T>
 */
public class Node<T> {
	public Node<T> parent = null;
	public Node<T> left = null;
	public Node<T> right = null;
	public HashSet<T> items;
	
	Node(Node<T> parent, Node<T> left,  Node<T> right) {
		this.parent = parent;
		this.left = left;
		this.right = right;
	}
	
	public void addParent(Node<T> inParent) {
		this.parent = inParent;
		inParent.addItems(items);
	}

	private void addItems(HashSet<T> inItems) {
		items.addAll(inItems);
	}
}
