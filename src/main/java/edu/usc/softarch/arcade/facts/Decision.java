package edu.usc.softarch.arcade.facts;

public abstract class Decision {
	//region ATTRIBUTES
	public final String description;
	public final String id;
	public final String version;
	//endregion

	//region CONSTRUCTORS
	protected Decision(String description, String id, String version) {
		this.description = description;
		this.id = id;
		this.version = version;
	}
	//endregion
}
