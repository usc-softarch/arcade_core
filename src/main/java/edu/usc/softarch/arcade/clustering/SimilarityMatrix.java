package edu.usc.softarch.arcade.clustering;

import java.util.HashMap;

public class SimilarityMatrix
		extends HashMap<Cluster, HashMap<Cluster, Double>> {
	//region CONSTRUCTORS
	public SimilarityMatrix() { super(); }
	//endregion

	//region ACCESSORS
	public void remove(Cluster c) {
		super.remove(c);
		for (HashMap<Cluster, Double> column : this.values())
			column.remove(c);
	}

	public Entry<Cluster, Cluster> getMinCell()
			throws Exception {
		Entry<Cluster, Cluster> toReturn = null;
		double minValue = Double.MAX_VALUE;

		for (Entry<Cluster, HashMap<Cluster, Double>> row : this.entrySet()) {
			Cluster c1 = row.getKey();
			for (Entry<Cluster, Double> column : row.getValue().entrySet()) {
				Cluster c2 = column.getKey();
				if (c1.equals(c2)) continue;
				Double cell = column.getValue();
				if (cell < minValue) {
					minValue = cell;
					toReturn = new SimpleEntry<>(c1, c2);
				} else if (cell == minValue) {
					toReturn = getSmallestMinCell(c1, c2, toReturn.getKey(), toReturn.getValue());
				}
			}
		}

		if (toReturn == null)
			throw new Exception(); //TODO throw appropriately

		return toReturn;
	}

	private Entry<Cluster, Cluster> getSmallestMinCell(
			Cluster c1, Cluster c2, Cluster c3, Cluster c4) {
		int entrySize1 = Math.min(c1.getNumEntities(), c2.getNumEntities());
		int entrySize2 = Math.min(c3.getNumEntities(), c4.getNumEntities());

		if (entrySize1 == entrySize2)
			return getOldestMinCell(c1, c2, c3, c4);

		return entrySize1 < entrySize2 ? new SimpleEntry<>(c1, c2) : new SimpleEntry<>(c3, c4);
	}

	private Entry<Cluster, Cluster> getOldestMinCell(
			Cluster c1, Cluster c2, Cluster c3, Cluster c4) {
		int entryAge1 = Math.min(c1.getAge(), c2.getAge());
		int entryAge2 = Math.min(c3.getAge(), c4.getAge());

		if (entryAge1 == entryAge2) {
			entryAge1 = c1.getAge() + c2.getAge();
			entryAge2 = c3.getAge() + c4.getAge();
		}

		return entryAge1 < entryAge2 ? new SimpleEntry<>(c1, c2) : new SimpleEntry<>(c3, c4);
	}
	//endregion
}
