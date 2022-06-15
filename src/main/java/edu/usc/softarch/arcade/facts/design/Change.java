package edu.usc.softarch.arcade.facts.design;

import java.util.ArrayList;
import java.util.Collection;

public class Change {
	//region ATTRIBUTES
	public final String priorClusterName;
	public final String newClusterName;
	private final Collection<String> addedClasses;
	private final Collection<String> removedClasses;
	//endregion

	//region CONSTRUCTORS
	public Change(String priorClusterName, String newClusterName,
		Collection<String> addedClasses, Collection<String> removedClasses) {
		this.priorClusterName = priorClusterName;
		this.newClusterName = newClusterName;
		this.addedClasses = new ArrayList<>(addedClasses);
		this.removedClasses = new ArrayList<>(removedClasses);
	}
	//endregion

	//region ACCESSORS
	public Collection<String> getAddedClasses() {
		return new ArrayList<>(addedClasses); }

	public Collection<String> getRemovedClasses() {
		return new ArrayList<>(removedClasses); }
	//endregion
}
