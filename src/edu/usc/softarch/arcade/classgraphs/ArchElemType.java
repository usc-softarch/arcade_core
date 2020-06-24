/**
 * 
 */
package edu.usc.softarch.arcade.classgraphs;

/**
 * @author joshua
 *
 */
enum ArchElemType {
	proc, data, conn;	
	
	public static String typeToString(ArchElemType t) {
		if (t.equals(proc)) {
			return "p";
		}
		else if (t.equals(data)) {
			return "d";
		}
		else if (t.equals(conn)) {
			return "c";
		}
		else {
			return "u";
		}
	}
	
	public static String typeToStyleString(ArchElemType t) {
		if (t.equals(proc)) {
			return "[shape=box, style=\"rounded,filled\", fillcolor=purple]";
		}
		else if (t.equals(data)) {
			return "[shape=hexagon, style=\"filled\", fillcolor=darkgreen]";
		}
		else if (t.equals(conn)) {
			return "[shape=box, style=\"rounded,filled\", fillcolor=cyan]";
		}
		else {
			return "[shape=doubleoctagon, style=\"rounded,filled\", fillcolor=white]";
		}
	}
}