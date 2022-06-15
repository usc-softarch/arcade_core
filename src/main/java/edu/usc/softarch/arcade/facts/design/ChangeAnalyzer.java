package edu.usc.softarch.arcade.facts.design;

import edu.usc.softarch.util.EnhancedHashSet;
import edu.usc.softarch.util.EnhancedSet;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ChangeAnalyzer {
	//region ATTRIBUTES
	private final McfpDriver mcfpDriver;
	private final Collection<Change> changeList;
	private final Map<String, EnhancedSet<String>> arch1;
	private final Map<String, EnhancedSet<String>> arch2;
	private Method getSource;
	private Method getTarget;
	//endregion

	//region CONSTRUCTORS
	public ChangeAnalyzer(String path1, String path2) throws IOException {
		// Read the architectures in
		this.arch1 = readArchitectureRsf(path1);
		this.arch2 = readArchitectureRsf(path2);

		// Balance the architectures
		balanceArchitectures(arch1, arch2);

		// Solve the MCFP to get the bipartite match
		this.mcfpDriver = new McfpDriver(arch1, arch2);

		// Load the changes for the matches from MCFP
		this.changeList = new ArrayList<>();
		jailbreakEdge();
		initializeChangeList();
	}

	private void balanceArchitectures(
			Map<String, EnhancedSet<String>> arch1,
			Map<String, EnhancedSet<String>> arch2) {
		int dummyCount = Math.abs(arch1.size() - arch2.size());
		Map<String, EnhancedSet<String>> smallerArch =
			arch1.size() < arch2.size() ? arch1 : arch2;

		for (int i = 0; i < dummyCount; i++)
			smallerArch.put("dummy" + i, new EnhancedHashSet<>());
	}

	private void initializeChangeList() {
		for (DefaultWeightedEdge edge : mcfpDriver.getChangeSet()) {
			String c1, c2;
			try {
				c1 = ((String) this.getSource.invoke(edge))
					.substring(7); // Strip "source_" from the name
				c2 = ((String) this.getTarget.invoke(edge))
					.substring(7); // Strip "target_" from the name
			} catch (InvocationTargetException | IllegalAccessException e) {
				/* This should never happen, as the NoSuchMethodException should never
				 * have a reason to be thrown in a half-decent software program. */
				throw new RuntimeException("An error occurred in the use of the "
					+ "JGraphT library's DefaultWeightedEdge class.", e);
			}

			Set<String> addedClasses = arch2.get(c2).difference(arch1.get(c1));
			Set<String> removedClasses = arch1.get(c1).difference(arch2.get(c2));

			// If nothing changed, move on to the next match
			if (addedClasses.size() + removedClasses.size() == 0) continue;

			this.changeList.add(new Change(c1, c2, addedClasses, removedClasses));
		}
	}

	/**
	 * Method to undo the ridiculous and pointless protected modifier for
	 * accessors.
	 */
	private void jailbreakEdge() {
		Class<?> defaultWeightedEdge = DefaultWeightedEdge.class;
		try {
			this.getSource = defaultWeightedEdge.getDeclaredMethod("getSource");
			this.getSource.setAccessible(true);
			this.getTarget = defaultWeightedEdge.getDeclaredMethod("getTarget");
			this.getTarget.setAccessible(true);
		} catch (NoSuchMethodException e) {
			/* This should never happen, as the NoSuchMethodException should never
			 * have a reason to be thrown in a half-decent software program. */
			throw new RuntimeException("An error occurred in the use of the "
				+ "JGraphT library's DefaultWeightedEdge class.", e);
		}
	}
	//endregion

	//region ACCESSORS
	public Collection<Change> getChangeList() {
		return new ArrayList<>(this.changeList); }
	//endregion

	//region IO
	private Map<String, EnhancedSet<String>> readArchitectureRsf(String path)
		throws IOException {
		Map<String, EnhancedSet<String>> architecture = new HashMap<>();

		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
			for (String line = br.readLine(); line != null; line = br.readLine()) {
				String[] splitLine = line.split(" ");
				architecture.putIfAbsent(splitLine[1], new EnhancedHashSet<>());
				EnhancedSet<String> cluster = architecture.get(splitLine[1]);
				cluster.add(splitLine[2]);
			}
		}

		return architecture;
	}
	//endregion
}
