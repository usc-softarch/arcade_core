package edu.usc.softarch.arcade.clustering;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

public class SimilarityMatrix
		extends LinkedHashMap<String, LinkedHashMap<String, Double>> {
	//region CONSTRUCTORS
	public SimilarityMatrix() { super(); }
	//endregion

	//region ACCESSORS
	public void remove(Cluster c) {
		this.remove(c.getName());
		for (LinkedHashMap<String, Double> column : this.values())
			column.remove(c.getName());
	}

	public Entry<String, String> getMinCell()
			throws Exception {
		Entry<String, String> toReturn = null;
		double minValue = Double.MAX_VALUE;

		for (Entry<String, LinkedHashMap<String, Double>> row : this.entrySet()) {
			String c1 = row.getKey();
			for (Entry<String, Double> column : row.getValue().entrySet()) {
				String c2 = column.getKey();
				if (c1.equals(c2)) continue;
				Double cell = column.getValue();
				if (cell < minValue) {
					minValue = cell;
					toReturn = new SimpleEntry<>(c1, c2);
				}
			}
		}

		if (toReturn == null)
			throw new Exception(); //TODO throw appropriately

		return toReturn;
	}
	//endregion
}
