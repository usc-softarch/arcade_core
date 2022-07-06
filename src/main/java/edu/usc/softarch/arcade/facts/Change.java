package edu.usc.softarch.arcade.facts;

import java.util.ArrayList;
import java.util.Collection;

public class Change<T> {
	//region ATTRIBUTES
	public final String priorClusterName;
	public final String newClusterName;
	private final Collection<T> addedElements;
	private final Collection<T> removedElements;
	//endregion

	//region CONSTRUCTORS
	public Change(String priorClusterName, String newClusterName,
			Collection<T> addedElements, Collection<T> removedElements) {
		this.priorClusterName = priorClusterName;
		this.newClusterName = newClusterName;
		this.addedElements = new ArrayList<>(addedElements);
		this.removedElements = new ArrayList<>(removedElements);
	}
	//endregion

	//region ACCESSORS
	public Collection<T> getAddedElements() {
		return new ArrayList<>(addedElements); }

	public Collection<T> getRemovedElements() {
		return new ArrayList<>(removedElements); }
	//endregion
}
