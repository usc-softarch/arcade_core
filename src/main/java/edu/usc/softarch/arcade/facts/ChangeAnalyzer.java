package edu.usc.softarch.arcade.facts;

import edu.usc.softarch.arcade.clustering.data.ReadOnlyArchitecture;
import edu.usc.softarch.arcade.util.McfpDriver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public abstract class ChangeAnalyzer<T> {
	//region ATTRIBUTES
	private final McfpDriver mcfpDriver;
	private final Collection<Change<T>> changeList;
	protected final ReadOnlyArchitecture arch1;
	protected final ReadOnlyArchitecture arch2;
	//endregion

	//region CONSTRUCTORS
	protected ChangeAnalyzer(String path1, String path2) throws IOException {
		this(new File(path1), new File(path2));	}

	protected ChangeAnalyzer(File file1, File file2) throws IOException {
		this(ReadOnlyArchitecture.readFromRsf(file1),
			ReadOnlyArchitecture.readFromRsf(file2));
	}

	protected ChangeAnalyzer(
			ReadOnlyArchitecture arch1, ReadOnlyArchitecture arch2) {
		this.arch1 = arch1;
		this.arch2 = arch2;

		// Solve the MCFP to get the bipartite match
		this.mcfpDriver = new McfpDriver(arch1, arch2);

		// Load the changes for the matches from MCFP
		this.changeList = new ArrayList<>();
	}
	//endregion

	//region ACCESSORS
	public Collection<Change<T>> getChangeList() {
		if (this.changeList.isEmpty()) initializeChangeList();
		return new ArrayList<>(this.changeList);
	}
	//endregion

	//region PROCESSING
	private void initializeChangeList() {
		for (Map.Entry<String, String> edge
			: this.mcfpDriver.getMatchSet().entrySet()) {
			String c1 = edge.getKey();
			String c2 = edge.getValue();

			Set<T> addedElements = getAddedElements(c1, c2);
			Set<T> removedElements = getRemovedElements(c1, c2);

			// If nothing changed, move on to the next match
			if (addedElements.size() + removedElements.size() == 0) continue;

			this.changeList.add(new Change<>(c1, c2, addedElements, removedElements));
		}
	}
	protected abstract Set<T> getAddedElements(String source, String target);
	protected abstract Set<T> getRemovedElements(String source, String target);
	//endregion
}
