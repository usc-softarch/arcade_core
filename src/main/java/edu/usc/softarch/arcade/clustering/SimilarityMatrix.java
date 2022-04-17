package edu.usc.softarch.arcade.clustering;

import edu.usc.softarch.arcade.topics.DistributionSizeMismatchException;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.TreeSet;

public class SimilarityMatrix {
	//region ATTRIBUTES
	public enum SimMeasure { JS, SCM }
	private final HashMap<Cluster, HashMap<Cluster, SimData>> simMatrix;
	private final TreeSet<SimData> fastSimMatrix;
	private final SimMeasure simMeasure;
	//endregion

	//region CONSTRUCTORS
	public SimilarityMatrix(SimMeasure simMeasure, Architecture architecture)
		throws DistributionSizeMismatchException {
		this.simMatrix = new HashMap<>();
		this.fastSimMatrix = new TreeSet<>();
		this.simMeasure = simMeasure;

		for (Cluster cluster : architecture.values())
			this.addCluster(cluster);
	}
	//endregion

	//region ACCESSORS
	public int size() { return simMatrix.size(); }

	public Collection<HashMap<Cluster, SimData>> getColumns() {
		return this.simMatrix.values();	}

	public void addCluster(Cluster c)
			throws DistributionSizeMismatchException {
		// Add column to existing rows
		for (Entry<Cluster, HashMap<Cluster, SimData>> row : simMatrix.entrySet()) {
			SimData cellData = computeCellData(row.getKey(), c);
			row.getValue().put(c, cellData);
			this.fastSimMatrix.add(cellData);
		}

		// Create new row
		HashMap<Cluster, SimData> newRow =  new HashMap<>();
		this.simMatrix.put(c, newRow);

		// Add all cells of the new row
		Collection<Cluster> clusterSet = simMatrix.keySet();
		for (Cluster cluster : clusterSet) {
			SimData cellData = computeCellData(c, cluster);
			newRow.put(cluster, cellData);
			if(!c.equals(cluster)) this.fastSimMatrix.add(cellData);
		}
	}

	public void removeCluster(Cluster c) {
		for (SimData cell : simMatrix.get(c).values())
			this.fastSimMatrix.remove(cell);
		simMatrix.remove(c);
		for (HashMap<Cluster, SimData> column : simMatrix.values()) {
			this.fastSimMatrix.remove(column.get(c));
			column.remove(c);
		}
	}

	public SimData getMinCell() {	return fastSimMatrix.first(); }
	//endregion

	//region PROCESSING
	private SimData computeCellData(Cluster row, Cluster col)
			throws DistributionSizeMismatchException {
		int cellSize = Math.min(row.getNumEntities(), col.getNumEntities());
		int rowAge = row.getAge();
		int colAge = col.getAge();
		int cellAge = Math.min(rowAge, colAge) << 16;
		cellAge += Math.max(rowAge, colAge);

		switch(this.simMeasure) {
			case JS:
				return new SimData(row, col,
					row.docTopicItem.getJsDivergence(col.docTopicItem), cellSize, cellAge);
			case SCM:
				return new SimData(row, col,
					FastSimCalcUtil.getStructAndConcernMeasure(row, col), cellSize, cellAge);
		}

		throw new IllegalArgumentException("Invalid similarity measure: " + simMeasure);
	}
	//endregion
}
