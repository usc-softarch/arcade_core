package edu.usc.softarch.arcade.classgraphs;

/**
 * @author joshua
 */
enum ArchElemType {
	// #region FIELDS ------------------------------------------------------------
	proc ("p", "[shape=box, style=\"rounded,filled\", fillcolor=purple]"),
	data ("d", "[shape=hexagon, style=\"filled\", fillcolor=darkgreen]"),
	conn ("c", "[shape=box, style=\"rounded,filled\", fillcolor=cyan]");

	private String typeString;
	private String styleString;
	// #endregion FIELDS ---------------------------------------------------------

	// #region CONSTRUCTORS ------------------------------------------------------
	private ArchElemType() {
		this.typeString = "u";
		this.styleString = 
			"[shape=doubleoctagon, style=\"rounded,filled\", fillcolor=white]";
	}

	private ArchElemType(String typeString, String styleString) {
		this.typeString = typeString;
		this.styleString = styleString;
	}
	// #endregion CONSTRUCTORS ---------------------------------------------------
	
	// #region ACCESSORS ---------------------------------------------------------
	public String typeToString() { return this.typeString; }
	public String typeToStyleString() { return this.styleString; }
	// #endregion ACCESSORS ------------------------------------------------------
}