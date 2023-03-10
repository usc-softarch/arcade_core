package edu.usc.softarch.util.matrix;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

public class FastMatrix<T extends Comparable<T>, G extends Comparable<G>> {
	//region ATTRIBUTES
	private final Map<T, Map<T, Cell<T, G>>> matrix;
	private final TreeSet<Cell<T, G>> fastMatrix;
	private final CellValueCalculator<T, G> calculator;
	private final CellValueValidator<T, G> validator;
	//endregion

	//region CONSTRUCTORS
	public FastMatrix(CellValueCalculator<T, G> calculator,
			CellValueValidator<T, G> validator) {
		this.matrix = new HashMap<>();
		this.fastMatrix = new TreeSet<>();
		this.calculator = calculator;
		this.validator = validator;
	}
	//endregion

	//region ACCESSORS
	public int size() { return matrix.size(); }

	public Collection<Map<T, Cell<T, G>>> getColumns() {
		return this.matrix.values(); }

	public void addColumn(T value) {
		for (Map.Entry<T, Map<T, Cell<T, G>>> row : matrix.entrySet()) {
			Cell<T, G> cellData = computeCellData(row.getKey(), value);
			if (this.validator.validateCellValue(cellData)) {
				row.getValue().put(value, cellData);
				this.fastMatrix.add(cellData);
			}
		}
	}

	public void addRow(T c) {
		// Create new row
		Map<T, Cell<T, G>> newRow = new HashMap<>();
		this.matrix.put(c, newRow);
		Map<T, Cell<T, G>> row = this.matrix.get(c);

		for (T col : this.matrix.values().stream().findFirst().get().keySet()) {
			Cell<T, G> cellData = computeCellData(c, col);
			if (!c.equals(col) && this.validator.validateCellValue(cellData)) {
				row.put(col, cellData);
				this.fastMatrix.add(cellData);
			}
		}
	}
	
	public void removeRow(T row) {
		try {
			for (Cell<T, G> cell : this.matrix.get(row).values())
				this.fastMatrix.remove(cell);
		} catch (NullPointerException e) {
			e.printStackTrace(); //TODO handle it
		}
		this.matrix.remove(row);
	}
	
	public void removeColumn(T column) {
		try {
			for (Map<T, Cell<T, G>> row : this.matrix.values()) {
				Cell<T, G> cell = row.get(column);
				if (cell != null)
					this.fastMatrix.remove(cell);
				row.remove(column);
			}
		} catch (NullPointerException e) {
			e.printStackTrace(); //TODO handle it
		}
	}

	public Cell<T, G> getMinCell() { return this.fastMatrix.first(); }

	public Cell<T, G> pollMinCell() { return this.fastMatrix.pollFirst(); }
	//endregion

	//region PROCESSING
	protected Cell<T, G> computeCellData(T row, T col, int... values) {
		return new Cell<>(row, col,
			this.calculator.computeCellValue(row, col, values));
	}
	//endregion
}
