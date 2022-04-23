package edu.usc.softarch.arcade.clustering;

import edu.usc.softarch.arcade.topics.DistributionSizeMismatchException;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.Objects;
import java.util.TreeSet;

/**
 * A matrix of similarity values between pairs of clusters. This implementation
 * uses a Map of Maps as its basic representation of the matrix, but also holds
 * a TreeSet of SimData values for fast search. It is intentionally
 * memory-intensive, and should never be serialized or compared, as either of
 * those operations would defeat the purpose of the fast-search implementation.
 */
public class SimilarityMatrix {
	//region ATTRIBUTES
	public enum SimMeasure { JS, SCM }
	private final Map<Cluster, Map<Cluster, SimData>> simMatrix;
	/*TODO fastSimMatrix is preposterously big, it might cause problems when
	 * analyzing immense systems because of the JVM's memory limitations.
	 * There should be a toggle to bypass its creation and use the traditional
	 * calculation of the minCell each time it is required, in order to save up
	 * memory space. I will not implement this until it proves necessary, as
	 * the processing time savings is significant. */
	private final TreeSet<SimData> fastSimMatrix;
	public final SimMeasure simMeasure;
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

	/**
	 * Clone constructor.
	 */
	public SimilarityMatrix(SimilarityMatrix toClone) {
		this.simMeasure = toClone.simMeasure;

		// Because SimData is immutable, no cloning is required
		this.fastSimMatrix = new TreeSet<>(toClone.fastSimMatrix);

		// Clusters are mutable, so cloning is required
		this.simMatrix = new HashMap<>();
		for (Entry<Cluster, Map<Cluster, SimData>> row : toClone.simMatrix.entrySet()) {
			Map<Cluster, SimData> newCol = new HashMap<>();
			this.simMatrix.put(new Cluster(row.getKey()), newCol);
			for (Entry<Cluster, SimData> cell : row.getValue().entrySet())
				newCol.put(new Cluster(cell.getKey()), cell.getValue());
		}
	}
	//endregion

	//region ACCESSORS
	public int size() { return simMatrix.size(); }

	public Collection<Map<Cluster, SimData>> getColumns() {
		return this.simMatrix.values();	}

	public void addCluster(Cluster c)
			throws DistributionSizeMismatchException {
		// Add column to existing rows
		for (Entry<Cluster, Map<Cluster, SimData>> row : simMatrix.entrySet()) {
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
		for (Map<Cluster, SimData> column : simMatrix.values()) {
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

	//region OBJECT METHODS

	/**
	 * As mentioned in the class documentation, do NOT attempt to compare two
	 * SimilarityMatrix objects. There should never be a situation where this is
	 * desirable, and the only reason this method is implemented is for testing
	 * purposes. Attempting to call SimilarityMatrix.equals will likely result in
	 * an increase in processing time upwards of 2x the original execution.
	 */
	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o == this) return true;
		if (!(o instanceof SimilarityMatrix)) return false;

		SimilarityMatrix toCompare = (SimilarityMatrix) o;

		boolean condition1 = this.simMeasure == toCompare.simMeasure;
		boolean condition2 = compareMatrices(toCompare);
		boolean condition3 = Objects.equals(this.fastSimMatrix, toCompare.fastSimMatrix);

		return condition1 && condition2 && condition3;
	}

	@Override
	public int hashCode() {
		return Objects.hash(simMatrix, fastSimMatrix, simMeasure); }

	private boolean compareMatrices(SimilarityMatrix toCompare) {
		for (Cluster c1 : this.simMatrix.keySet()) {
			Map<Cluster, SimData> thisRow = this.simMatrix.get(c1);
			Map<Cluster, SimData> otherRow = toCompare.simMatrix.get(c1);

			if (otherRow == null)
				return false;

			for (Entry<Cluster, SimData> cell : thisRow.entrySet()) {
				SimData otherData = otherRow.get(cell.getKey());
				if (otherData == null)
					return false;
				if (!otherData.equals(cell.getValue()))
					return false;
			}
		}
		return true;
	}
	//endregion
}
