package edu.usc.softarch.arcade.facts.design;

import edu.usc.softarch.arcade.clustering.data.ReadOnlyArchitecture;
import edu.usc.softarch.arcade.facts.ChangeAnalyzer;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class ElementChangeAnalyzer extends ChangeAnalyzer<String> {
	//region CONSTRUCTORS
	public ElementChangeAnalyzer(String path1, String path2) throws IOException {
		super(path1, path2); }

	public ElementChangeAnalyzer(File file1, File file2) throws IOException {
		super(file1, file2); }

	public ElementChangeAnalyzer(
			ReadOnlyArchitecture arch1, ReadOnlyArchitecture arch2) {
		super(arch1, arch2);
	}
	//endregion

	//region PROCESSING
	@Override
	protected Set<String> getAddedElements(String source, String target) {
		if (source.contains("dummy"))
			return new HashSet<>(super.arch2.get(target).getEntities());

		if (target.contains("dummy"))
			return new HashSet<>();

		return super.arch2.get(target).difference(super.arch1.get(source));
	}

	@Override
	protected Set<String> getRemovedElements(String source, String target) {
		if (target.contains("dummy"))
			return new HashSet<>(super.arch1.get(source).getEntities());

		if (source.contains("dummy"))
			return new HashSet<>();

		return super.arch1.get(source).difference(super.arch2.get(target));
	}
	//endregion
}
