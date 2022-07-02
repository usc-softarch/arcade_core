package edu.usc.softarch.arcade.clustering;

import edu.usc.softarch.util.EnhancedHashSet;
import edu.usc.softarch.util.EnhancedSet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ReadOnlyArchitecture extends TreeMap<String, ReadOnlyCluster> {
	//region CONSTRUCTORS
	public ReadOnlyArchitecture(Architecture arch) {
		super();

		for (Map.Entry<String, Cluster> entry : arch.entrySet())
			this.put(entry.getKey(), new ReadOnlyCluster(entry.getValue()));
	}

	private ReadOnlyArchitecture() { super(); }

	private ReadOnlyArchitecture(ReadOnlyArchitecture toClone) {
		for (Map.Entry<String, ReadOnlyCluster> entry : toClone.entrySet())
			this.put(entry.getKey(), new ReadOnlyCluster(entry.getValue()));
	}
	//endregion

	//region ACCESSORS
	public int countEntities() {
		int result = 0;

		for (ReadOnlyCluster cluster : this.values())
			result += cluster.getEntities().size();

		return result;
	}

	public EnhancedSet<String> getEntities() {
		EnhancedSet<String> result = new EnhancedHashSet<>();

		for (ReadOnlyCluster cluster : this.values())
			result.addAll(cluster.getEntities());

		return result;
	}

	public ReadOnlyArchitecture removeEntities(Set<String> entities) {
		ReadOnlyArchitecture result = new ReadOnlyArchitecture(this);
		result.values().forEach(c -> c.removeEntities(entities));

		return result;
	}
	//endregion

	//region SERIALIZATION
	public static ReadOnlyArchitecture readFromRsf(String path)
			throws IOException {
		return readFromRsf(new File(path));
	}

	public static ReadOnlyArchitecture readFromRsf(File file)
			throws IOException {
		ReadOnlyArchitecture result = new ReadOnlyArchitecture();

		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;

			while ((line = br.readLine()) != null) {
				if (line.trim().isEmpty()) continue;

				String[] entry = line.split(" ");
				result.putIfAbsent(entry[1], new ReadOnlyCluster(entry[1]));
				result.get(entry[1]).addEntity(entry[2]);
			}
		}

		return result;
	}
	//endregion
}
