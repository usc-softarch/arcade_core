package edu.usc.softarch.util;

import java.util.ArrayList;
import java.util.Collection;

public class Tree<T> {
	//region ATTRIBUTES
	public final T node;
	private final Collection<Tree<T>> children;
	//endregion

	//region CONSTRUCTOR
	public Tree(T node) {
		this.node = node;
		this.children = new ArrayList<>();
	}
	//endregion

	//region ACCESSORS
	public Collection<Tree<T>> getChildren() {
		return new ArrayList<>(this.children); }
	public void addChild(T node) { this.children.add(new Tree<>(node)); }
	public void addChild(Tree<T> node) { this.children.add(node); }
	//endregion
}
