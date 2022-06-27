package edu.usc.softarch.arcade.facts;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class VersionTree {
	//region ATTRIBUTES
	public final String node;
	private final Collection<VersionTree> children;
	//endregion

	//region CONSTRUCTOR
	public VersionTree(String node) {
		this.node = node;
		this.children = new ArrayList<>();
	}
	//endregion

	//region ACCESSORS
	public Collection<VersionTree> getChildren() {
		return new ArrayList<>(this.children); }
	public void addChild(String node) {
		this.children.add(new VersionTree(node)); }
	public void addChild(VersionTree node) { this.children.add(node); }
	public boolean containsVersion(String version) {
		if (this.node.equals(version)) return true;
		boolean found = false;
		for (VersionTree child : getChildren()) {
			found = child.containsVersion(version);
		}
		return found;
	}
	//endregion

	//region SERIALIZATION
	public void serialize(String path) throws IOException {
		try (PrintWriter writer = new PrintWriter(path, StandardCharsets.UTF_8)) {
			for (VersionTree child : children)
				child.serialize(writer, this.node);
		}
	}

	private void serialize(PrintWriter writer, String parent) {
		writer.println("parent-of " + parent + " " + this.node);
		for (VersionTree child : children)
			child.serialize(writer, this.node);
	}

	public static VersionTree deserialize(String path) throws IOException {
		VersionTree result;
		Map<String, VersionTree> temp = new HashMap<>();

		try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
			// Read root
			String line = reader.readLine();
			String[] rsfEntry = line.split(" ");
			result = new VersionTree(rsfEntry[1]);
			temp.put(result.node, result);

			// Read tree
			while (line != null) {
				rsfEntry = line.split(" ");
				VersionTree child = new VersionTree(rsfEntry[2]);
				temp.put(child.node, child);
				temp.get(rsfEntry[1]).addChild(child);
				line = reader.readLine();
			}
		}

		return result;
	}
	//endregion
}
