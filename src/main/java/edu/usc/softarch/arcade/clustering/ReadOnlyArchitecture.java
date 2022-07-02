package edu.usc.softarch.arcade.clustering;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

public class ReadOnlyArchitecture extends TreeMap<String, ReadOnlyCluster> {
	//region CONSTRUCTORS
	public ReadOnlyArchitecture(Architecture arch) {
		super();

		for (Map.Entry<String, Cluster> entry : arch.entrySet())
			this.put(entry.getKey(), new ReadOnlyCluster(entry.getValue()));
	}

	private ReadOnlyArchitecture() { super(); }
	//endregion

	//region SERIALIZATION
	public static ReadOnlyArchitecture readFromRsf(String path)
			throws IOException {
		ReadOnlyArchitecture result = new ReadOnlyArchitecture();

		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
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
